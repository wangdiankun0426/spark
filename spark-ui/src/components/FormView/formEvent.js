import { onMounted } from 'vue'

/**
 * 表单组件事件契约：组件级事件、声明式动作、条件运算符的统一定义。
 * 运行时渲染引擎（FormView）与表单设计器的事件配置面板共用。
 */

/**
 * 组件级事件清单。12 个组件一律支持同一套事件，不做类型能力矩阵。
 * 注意 change 的触发时机因控件而异：el-input/el-input-number/el-date-picker 是失焦或回车提交才触发，
 * el-select/el-radio-group 是选中即触发，el-upload 由 syncConfigValue 触发。
 * 要实时联动文本类输入，请监听 click/focus 或走页面级 JS。
 */
export const WIDGET_EVENT_LIST = [
    { value: 'mounted', label: '初始化完成' },
    { value: 'change', label: '值改变' },
    { value: 'focus', label: '获得焦点' },
    { value: 'blur', label: '失去焦点' },
    { value: 'click', label: '点击' },
]

/**
 * 声明式动作清单。needValue 为 true 的动作需要配置动作值。
 * 不提供 set-readonly：number/select/radio 不读 config.readonly 且对应控件无只读能力，
 * 做成静默空操作只会污染配置；需要整表只读请用 FormView 的 disabled prop。
 */
export const FORM_ACTION_LIST = [
    { value: 'set-value', label: '赋值', needValue: true },
    { value: 'clear', label: '清空', needValue: false },
    { value: 'set-hidden', label: '是否隐藏', needValue: true },
    { value: 'set-disabled', label: '是否禁用', needValue: true },
    { value: 'set-required', label: '是否必填', needValue: true },
]

/**
 * 条件运算符清单
 */
export const FORM_CONDITION_OP_LIST = [
    { value: 'eq', label: '等于' },
    { value: 'neq', label: '不等于' },
    { value: 'in', label: '包含' },
    { value: 'notIn', label: '不包含' },
    { value: 'notEmpty', label: '不为空' },
    { value: 'isEmpty', label: '为空' },
]

/**
 * 支持被赋值（set-value）的组件类型：名称在本地 config.options 或可直接赋值，解释器可同步 showValue。
 * select-user/select-dept/select-role/select-knowledge/select-graph 的名称在子组件异步缓存中，
 * upload-attachment 的 showValue 是附件元数据 JSON 串，强改会出现"值已变、展示还是旧名字"。
 */
export const LOCAL_VALUE_TYPES = ['input', 'textarea', 'number', 'date', 'radio', 'select']

/**
 * 不支持被清空（clear）的组件类型：upload-attachment 的附件列表是组件内 ref，
 * 仅在 setup 时初始化一次，程序化清值不会让表格里的旧附件行消失。
 */
export const CLEAR_BLOCKED_TYPES = ['upload-attachment']

/**
 * 组件事件桥接：把各组件底层控件事件归一化为标准事件，统一组装事件对象后向外抛出。
 * defineEmits 是编译宏无法在组合式函数内调用，因此由各组件在 script setup 顶层声明后把 emit 传入。
 * @param widget 组件对象（widget.config 为配置引用）
 * @param emit 组件实例的 emit 函数
 * @returns {{ emitEvent: Function, emitChange: Function }}
 */
export function useWidgetEventBridge(widget, emit) {
    // 上一值：用于在 change 事件中携带 oldValue。初始化取 setup 时 config.value，
    // 多数调用方是先合并落库值再渲染表单，因此业务侧要按"是否为空"判断而非假定初始为空
    let lastValue = widget.config.value

    /**
     * 抛出标准组件事件
     * @param eventName 事件名（mounted/change/focus/blur/click）
     */
    function emitEvent(eventName) {
        const payload = {
            widget: widget,
            type: widget.type,
            code: widget.config.code,
            label: widget.config.label,
            eventName: eventName,
            value: widget.config.value,
            showValue: widget.config.showValue,
            oldValue: eventName === 'change' ? lastValue : undefined,
            config: widget.config,
        }
        lastValue = widget.config.value
        emit('widget-event', payload)
        return payload
    }

    /**
     * 值改变统一入口：由各组件在同步完 showValue 后调用。
     * 命名不用 handleChange，selectKnowledge/selectGraph 已有同名本地函数
     */
    function emitChange() {
        emitEvent('change')
    }

    // 挂载完成即抛出初始化事件；表单初始化时按组件顺序依次执行，可用于默认显隐与默认值
    onMounted(() => {
        emitEvent('mounted')
    })

    return { emitEvent: emitEvent, emitChange: emitChange }
}

import { LOCAL_VALUE_TYPES, CLEAR_BLOCKED_TYPES } from './formEvent.js'

/**
 * 表单声明式动作解释器：执行设计器事件配置面板保存的动作规则。
 * 规则存放在 widget.config.events[eventName]，随 form_json 持久化。
 * 解释器需要 widgetList 才能解析目标字段与 #{fieldX} 占位符，因此留在 FormView 层而不下沉到组件。
 */

/**
 * 执行某个组件事件上配置的全部动作规则。
 * 规则全量执行而非首中即停；动作直接改 config，不走 el-* 的 change 回调，不会递归派发事件，天然无循环。
 * @param widget 触发的组件
 * @param eventName 事件名
 * @param widgetList 当前表单组件列表
 * @param disabled 表单级只读态，值写入类动作在此状态下跳过
 */
export function runWidgetActions(widget, eventName, widgetList, disabled) {
    const rules = widget.config.events && widget.config.events[eventName]
    if (!rules || !Array.isArray(rules)) {
        return
    }
    for (const rule of rules) {
        if (!rule || !rule.target) {
            continue
        }
        if (!matchCondition(rule.when, widgetList)) {
            continue
        }
        applyAction(rule, widgetList, disabled)
    }
}

/**
 * 判断条件是否满足。无条件（when 为空）视为满足。
 * 字段不存在时取值为 undefined，按"为空"语义参与比较。
 * @param when 条件对象 { field, op, value }
 * @param widgetList 组件列表
 * @returns {boolean}
 */
function matchCondition(when, widgetList) {
    // 无条件（when 为空）视为满足；条件已开启但字段或运算符未配齐时跳过该条规则，避免误当无条件执行
    if (!when) {
        return true
    }
    if (!when.field || !when.op) {
        return false
    }
    const actual = resolveFieldValue(widgetList, when.field)
    const expected = when.value
    switch (when.op) {
        case 'eq':
            return String(actual == null ? '' : actual) === String(expected == null ? '' : expected)
        case 'neq':
            return String(actual == null ? '' : actual) !== String(expected == null ? '' : expected)
        case 'in':
        case 'notIn': {
            // 比较值按逗号拆成候选集，字段值也按逗号拆（兼容多选逗号串），任一命中即视为包含
            const expectedList = String(expected == null ? '' : expected)
                .split(',')
                .map(item => item.trim())
                .filter(Boolean)
            const actualList = String(actual == null ? '' : actual)
                .split(',')
                .map(item => item.trim())
                .filter(Boolean)
            const matched = actualList.some(item => expectedList.includes(item))
            return when.op === 'in' ? matched : !matched
        }
        case 'notEmpty':
            return actual != null && String(actual) !== ''
        case 'isEmpty':
            return actual == null || String(actual) === ''
        default:
            return false
    }
}

/**
 * 按字段编码取当前值（只取业务值 value，不取 showValue）。
 * 业务页面在 widget-event 里做自定义逻辑时可直接复用，保持与设计器动作同一取值语义。
 * @param widgetList 组件列表
 * @param code 字段编码
 * @returns {*}
 */
export function resolveFieldValue(widgetList, code) {
    const target = widgetList.find(widget => widget.config.code === code)
    return target ? target.config.value : undefined
}

/**
 * 解析 #{fieldX} 占位符为对应字段的当前 value；字段不存在替换为空串，不抛错。
 * 业务页面自定义逻辑需要同一占位符语义时可直接复用。
 * @param value 原始动作值
 * @param widgetList 组件列表
 * @returns {string}
 */
export function resolvePlaceholders(value, widgetList) {
    if (value == null) {
        return null
    }
    return String(value).replace(/#\{([^}]+)\}/g, (match, code) => {
        const fieldValue = resolveFieldValue(widgetList, String(code).trim())
        return fieldValue == null ? '' : String(fieldValue)
    })
}

/**
 * 执行单条动作
 * @param rule 动作规则 { action, target, value }
 * @param widgetList 组件列表
 * @param disabled 表单级只读态
 */
function applyAction(rule, widgetList, disabled) {
    const target = widgetList.find(widget => widget.config.code === rule.target)
    if (!target) {
        return
    }
    switch (rule.action) {
        case 'set-value':
            // 只读态下跳过值写入，避免覆盖已落库的展示值；显隐置灰必填仍执行，可见性逻辑只读态下也有意义
            if (disabled || LOCAL_VALUE_TYPES.indexOf(target.type) === -1) {
                return
            }
            const raw = resolvePlaceholders(rule.value, widgetList)
            target.config.value = normalizeValue(target, raw)
            target.config.showValue = resolveShowValue(target, target.config.value)
            break
        case 'clear':
            if (disabled || CLEAR_BLOCKED_TYPES.indexOf(target.type) !== -1) {
                return
            }
            target.config.value = null
            target.config.showValue = null
            break
        case 'set-hidden':
            target.config.hidden = toBoolean(rule.value)
            break
        case 'set-disabled':
            // 置灰不会关闭已配置的只读：input/textarea/date 等控件同时设 disabled 与 readonly 时可继续输入，
            // 需要锁死录入请配合 set-hidden 或页面级 JS
            target.config.disabled = toBoolean(rule.value)
            break
        case 'set-required':
            target.config.required = toBoolean(rule.value)
            break
    }
}

/**
 * 按组件类型强转赋值结果，避免 number 写入字符串破坏 el-input-number。
 * date 必须匹配该控件 dateType 的格式（YYYY-MM-DD 或 YYYY-MM-DD HH:mm:ss），由设计者输入保证；
 * radio/select 必须是既有选项值，否则展示层会显示成原始值。
 * @param widget 目标组件
 * @param raw 占位符解析后的原始值
 * @returns {*}
 */
function normalizeValue(widget, raw) {
    if (raw == null || raw === '') {
        return null
    }
    if (widget.type === 'number') {
        const num = Number(raw)
        return Number.isNaN(num) ? null : num
    }
    return raw
}

/**
 * 解析赋值后的展示值：选项类从 config.options 查标签，文本与日期类直接取业务值
 * @param widget 目标组件
 * @param value 业务值
 * @returns {*}
 */
function resolveShowValue(widget, value) {
    if (value == null) {
        return null
    }
    if (widget.type === 'radio' || widget.type === 'select') {
        const option = (widget.config.options || []).find(item => item.value === value)
        return option ? option.label : String(value)
    }
    return value
}

/**
 * 配置面板存入的动作值统一按布尔解释
 * @param value 动作值
 * @returns {boolean}
 */
function toBoolean(value) {
    return value === true || value === 1 || value === '1' || value === 'true'
}

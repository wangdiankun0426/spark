const widgetTypes = [
    'input', 'textarea', 'radio', 'select', 'number', 'date',
    'select-user', 'select-dept', 'select-role', 'select-knowledge', 'select-graph', 'upload-attachment',
]
const eventNames = ['mounted', 'change', 'focus', 'blur', 'click']
const actionNames = ['set-value', 'clear', 'set-hidden', 'set-disabled', 'set-required']
const conditionOps = ['eq', 'neq', 'in', 'notIn', 'notEmpty', 'isEmpty']
const decimalPattern = /^[+-]?(?:\d+(?:\.\d*)?|\.\d+)(?:[eE][+-]?\d+)?$/

/**
 * 校验表单模板结构、字段编码及事件字段引用
 * @param form 表单Schema
 * @returns {{valid: boolean, message: string}} 校验结果
 */
export function validateFormSchema(form) {
    if (!isObject(form)) {
        return invalidResult('表单Schema必须是对象')
    }
    if (!isObject(form.formConfig)) {
        return invalidResult('表单Schema缺少有效的formConfig')
    }
    if (!Array.isArray(form.widgetList)) {
        return invalidResult('表单Schema缺少有效的widgetList')
    }
    const codes = new Set()
    for (let index = 0; index < form.widgetList.length; index++) {
        const widget = form.widgetList[index]
        const location = `第${index + 1}个组件`
        if (!isObject(widget) || !isObject(widget.config)) {
            return invalidResult(`${location}缺少有效的config`)
        }
        if (!widgetTypes.includes(widget.type)) {
            return invalidResult(`${location}类型不受支持`)
        }
        const code = widget.config.code
        if (typeof code !== 'string' || !code.trim() || code.length > 36) {
            return invalidResult(`${location}的字段编码为空或超过36个字符`)
        }
        if (codes.has(code)) {
            return invalidResult(`字段编码【${code}】重复`)
        }
        codes.add(code)
        const label = widget.config.label
        if (typeof label !== 'string' || !label.trim() || label.length > 36) {
            return invalidResult(`字段【${code}】的标题为空或超过36个字符`)
        }
        if (!validSwitches(widget.config)) {
            return invalidResult(`字段【${code}】的开关配置必须是布尔值`)
        }
        if ((widget.type === 'radio' || widget.type === 'select') && !validOptions(widget.config.options)) {
            return invalidResult(`【${label}】的选项结构无效`)
        }
        if (widget.type === 'number' && !validNumberConfig(widget.config)) {
            return invalidResult(`【${label}】的数字配置无效`)
        }
        if (widget.type === 'date' && !['date', 'datetime'].includes(widget.config.dateType)) {
            return invalidResult(`【${label}】的日期类型无效`)
        }
    }
    for (let index = 0; index < form.widgetList.length; index++) {
        const error = validateWidgetEvents(form.widgetList[index], codes, index)
        if (error) {
            return invalidResult(error)
        }
    }
    return { valid: true, message: '' }
}

/**
 * 生成当前表单内唯一的字段编码
 * @param widgetList 当前组件列表
 * @returns {string} 字段编码
 */
export function generateFieldCode(widgetList) {
    const codes = new Set((Array.isArray(widgetList) ? widgetList : [])
        .filter(widget => widget && widget.config)
        .map(widget => widget.config.code))
    const baseCode = `field${Date.now()}`
    let code = baseCode
    let index = 1
    while (codes.has(code)) {
        code = `${baseCode}_${index}`
        index++
    }
    return code
}

/**
 * 构造可持久化的模板Schema，排除选中态并清空运行值
 * @param form 表单Schema或设计器状态
 * @returns {{formConfig: Object, widgetList: Array}} 模板Schema
 */
export function buildFormTemplate(form) {
    const template = {
        formConfig: JSON.parse(JSON.stringify(form.formConfig)),
        widgetList: JSON.parse(JSON.stringify(form.widgetList)),
    }
    template.widgetList.forEach(widget => {
        widget.config.value = null
        widget.config.showValue = null
    })
    return template
}

/**
 * 校验可见必填字段，数字零和布尔假不视为空值
 * @param form 表单Schema
 * @param action 校验失败提示中的操作名称
 * @returns {{valid: boolean, message: string}} 校验结果
 */
export function validateFormValues(form, action = '提交') {
    const widgetList = Array.isArray(form?.widgetList) ? form.widgetList : []
    const missingWidget = widgetList.find(widget => {
        const config = widget?.config
        if (!config || config.hidden || !config.required) {
            return false
        }
        return isEmptyValue(config.value)
    })
    if (!missingWidget) {
        return { valid: true, message: '' }
    }
    const config = missingWidget.config
    return {
        valid: false,
        message: `【${config.label || config.code}】为必填项，请填写后再${action}`,
    }
}

/**
 * 按既有协议收集可见字段的业务值和展示值
 * @param form 表单Schema
 * @returns {Array} 字段值列表
 */
export function collectFormValues(form) {
    const widgetList = Array.isArray(form?.widgetList) ? form.widgetList : []
    return widgetList
        .filter(widget => widget?.config && !widget.config.hidden && typeof widget.config.code === 'string' && widget.config.code.trim())
        .map(widget => ({
            code: widget.config.code,
            type: widget.type,
            value: widget.config.value,
            showValue: widget.config.showValue,
        }))
}

function isObject(value) {
    return value !== null && typeof value === 'object' && !Array.isArray(value)
}

function invalidResult(message) {
    return { valid: false, message }
}

function validSwitches(config) {
    return ['hidden', 'required', 'disabled', 'readonly', 'multiple', 'downloadable']
        .every(key => config[key] === null || config[key] === undefined || typeof config[key] === 'boolean')
}

function validOptions(options) {
    if (!Array.isArray(options) || options.length === 0) {
        return false
    }
    const values = new Set()
    for (const option of options) {
        if (!isObject(option) || option.value === null || option.value === undefined
            || option.label === null || option.label === undefined || !String(option.label).trim()) {
            return false
        }
        const valueType = typeof option.value
        const labelType = typeof option.label
        if (!['string', 'number', 'boolean'].includes(valueType) || !['string', 'number', 'boolean'].includes(labelType)
            || !String(option.value).trim() || String(option.value).length > 64 || String(option.label).length > 512
            || (valueType === 'number' && !Number.isFinite(option.value))) {
            return false
        }
        const value = String(option.value)
        if (values.has(value)) {
            return false
        }
        values.add(value)
    }
    return true
}

function validNumberConfig(config) {
    for (const key of ['min', 'max', 'step']) {
        const value = config[key]
        if (value === null || value === undefined) {
            continue
        }
        const valueType = typeof value
        if (!['number', 'string'].includes(valueType) || !Number.isFinite(Number(value))) {
            return false
        }
        if (valueType === 'string' && !decimalPattern.test(value)) {
            return false
        }
    }
    const min = config.min === null || config.min === undefined ? null : Number(config.min)
    const max = config.max === null || config.max === undefined ? null : Number(config.max)
    const step = config.step === null || config.step === undefined ? null : Number(config.step)
    return !(min !== null && max !== null && min > max) && !(step !== null && step <= 0)
}

function isEmptyValue(value) {
    return value === null || value === undefined || value === ''
        || (Array.isArray(value) && value.length === 0)
}

function validateWidgetEvents(widget, codes, index) {
    const events = widget.config.events
    const label = widget.config.label || widget.config.code || `第${index + 1}个组件`
    if (events === null || events === undefined) {
        return ''
    }
    if (!isObject(events)) {
        return `【${label}】的events必须是对象`
    }
    for (const [eventName, rules] of Object.entries(events)) {
        if (!eventNames.includes(eventName)) {
            return `【${label}】包含不支持的事件【${eventName}】`
        }
        if (!Array.isArray(rules)) {
            return `【${label}】的事件【${eventName}】必须是数组`
        }
        for (let ruleIndex = 0; ruleIndex < rules.length; ruleIndex++) {
            const error = validateEventRule(rules[ruleIndex], codes, label, eventName, ruleIndex)
            if (error) {
                return error
            }
        }
    }
    return ''
}

function validateEventRule(rule, codes, label, eventName, ruleIndex) {
    const location = `【${label}】的事件【${eventName}】第${ruleIndex + 1}条规则`
    if (!isObject(rule) || !actionNames.includes(rule.action)) {
        return `${location}动作无效`
    }
    if (typeof rule.target !== 'string' || !codes.has(rule.target)) {
        return `${location}目标字段【${rule.target || '未配置'}】不存在`
    }
    if (rule.action !== 'clear' && (rule.value === null || rule.value === undefined)) {
        return `${location}动作值不能为空`
    }
    if (rule.when !== null && rule.when !== undefined) {
        if (!isObject(rule.when) || typeof rule.when.field !== 'string' || !codes.has(rule.when.field)) {
            return `${location}条件字段不存在`
        }
        if (!conditionOps.includes(rule.when.op)) {
            return `${location}条件运算符无效`
        }
    }
    const value = rule.value === null || rule.value === undefined ? '' : String(rule.value)
    for (const match of value.matchAll(/#\{([^}]*)\}/g)) {
        const code = match[1].trim()
        if (!code || !codes.has(code)) {
            return `${location}引用字段【${code || '空'}】不存在`
        }
    }
    return ''
}

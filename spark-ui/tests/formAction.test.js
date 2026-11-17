import test from 'node:test'
import assert from 'node:assert/strict'
import {
    resolveFieldValue,
    resolvePlaceholders,
    runWidgetActions,
} from '../src/components/FormView/formAction.js'
import {
    collectFormValues,
    validateFormValues,
} from '../src/views/formDesigner/formSchema.js'

function createWidget(type, code, value = null, config = {}) {
    return {
        type,
        config: {
            code,
            label: code,
            value,
            showValue: config.showValue === undefined ? value : config.showValue,
            hidden: false,
            required: false,
            disabled: false,
            ...config,
        },
    }
}

function createSource(rules, eventName = 'change') {
    return createWidget('input', 'source', null, {
        events: { [eventName]: rules },
    })
}

test('没有规则或规则不是数组时不修改表单', () => {
    const target = createWidget('input', 'target', '原值')
    const source = createWidget('input', 'source', null, {
        events: { change: null, blur: {} },
    })

    runWidgetActions(source, 'change', [source, target], false)
    runWidgetActions(source, 'blur', [source, target], false)
    runWidgetActions(source, 'click', [source, target], false)

    assert.equal(target.config.value, '原值')
    assert.equal(target.config.showValue, '原值')
})

test('条件字段或目标字段缺失时跳过规则', () => {
    const target = createWidget('input', 'target', '原值')
    const source = createSource([
        {
            action: 'set-value',
            target: 'target',
            value: '不应写入',
            when: { field: 'missing', op: 'isEmpty' },
        },
        { action: 'set-value', target: 'missing', value: '不应写入' },
        { action: 'set-value', target: 'target', value: '不应写入', when: { field: 'target' } },
    ])

    runWidgetActions(source, 'change', [source, target], false)

    assert.equal(target.config.value, '原值')
    assert.equal(resolveFieldValue([source, target], 'missing'), undefined)
    assert.equal(resolvePlaceholders('A#{missing}B', [source, target]), 'AB')
})

test('动作按配置顺序执行且后续占位符读取最新值', () => {
    const target = createWidget('input', 'target')
    const mirror = createWidget('input', 'mirror')
    const source = createSource([
        { action: 'set-value', target: 'target', value: 'first' },
        { action: 'set-value', target: 'mirror', value: '#{target}' },
        { action: 'set-value', target: 'target', value: 'last' },
        { action: 'set-disabled', target: 'target', value: true },
        { action: 'set-required', target: 'target', value: '1' },
        { action: 'set-hidden', target: 'target', value: 'true' },
    ])

    runWidgetActions(source, 'change', [source, target, mirror], false)

    assert.equal(target.config.value, 'last')
    assert.equal(target.config.showValue, 'last')
    assert.equal(target.config.disabled, true)
    assert.equal(target.config.required, true)
    assert.equal(target.config.hidden, true)
    assert.equal(mirror.config.value, 'first')
})

test('占位符支持零、假、空值和缺失字段', () => {
    const widgetList = [
        createWidget('number', 'amount', 0),
        createWidget('input', 'enabled', false),
        createWidget('input', 'empty', null),
    ]

    assert.equal(
        resolvePlaceholders('amount=#{amount};enabled=#{enabled};empty=#{empty};missing=#{missing}', widgetList),
        'amount=0;enabled=false;empty=;missing=',
    )
    assert.equal(resolvePlaceholders(null, widgetList), null)
    assert.equal(resolveFieldValue(widgetList, 'amount'), 0)
})

test('数字赋值会转换类型，选项赋值同步业务值和展示值', () => {
    const number = createWidget('number', 'number')
    const select = createWidget('select', 'select', null, {
        options: [{ label: '两项', value: 2 }, { label: '三项', value: 3 }],
    })
    const radio = createWidget('radio', 'radio', null, {
        options: [{ label: '是', value: true }, { label: '否', value: false }],
    })
    const source = createSource([
        { action: 'set-value', target: 'number', value: '12.5' },
        { action: 'set-value', target: 'select', value: '2' },
        { action: 'set-value', target: 'radio', value: false },
    ])

    runWidgetActions(source, 'change', [source, number, select, radio], false)

    assert.equal(number.config.value, 12.5)
    assert.equal(number.config.showValue, 12.5)
    assert.equal(select.config.value, 2)
    assert.equal(select.config.showValue, '两项')
    assert.equal(radio.config.value, false)
    assert.equal(radio.config.showValue, '否')
})

test('表单禁用只阻断值写入和清空，显隐置灰必填仍执行', () => {
    const target = createWidget('input', 'target', '原值', { showValue: '原展示值' })
    const source = createSource([
        { action: 'set-value', target: 'target', value: '新值' },
        { action: 'clear', target: 'target' },
        { action: 'set-hidden', target: 'target', value: true },
        { action: 'set-disabled', target: 'target', value: true },
        { action: 'set-required', target: 'target', value: true },
    ])

    runWidgetActions(source, 'change', [source, target], true)

    assert.equal(target.config.value, '原值')
    assert.equal(target.config.showValue, '原展示值')
    assert.equal(target.config.hidden, true)
    assert.equal(target.config.disabled, true)
    assert.equal(target.config.required, true)
})

test('附件组件阻断程序化清空', () => {
    const upload = createWidget('upload-attachment', 'upload', 'file-id', {
        showValue: '[{"name":"附件.txt"}]',
    })
    const source = createSource([{ action: 'clear', target: 'upload' }])

    runWidgetActions(source, 'change', [source, upload], false)

    assert.equal(upload.config.value, 'file-id')
    assert.equal(upload.config.showValue, '[{"name":"附件.txt"}]')
})

test('重复字段编码在条件、目标和占位符中都使用首个组件', () => {
    const first = createWidget('input', 'duplicate', 'first')
    const second = createWidget('input', 'duplicate', 'second')
    const target = createWidget('input', 'target')
    const source = createSource([
        {
            action: 'set-value',
            target: 'duplicate',
            value: 'updated',
            when: { field: 'duplicate', op: 'eq', value: 'first' },
        },
        { action: 'set-value', target: 'target', value: '#{duplicate}' },
    ])
    const widgetList = [source, first, second, target]

    runWidgetActions(source, 'change', widgetList, false)

    assert.equal(first.config.value, 'updated')
    assert.equal(second.config.value, 'second')
    assert.equal(target.config.value, 'updated')
    assert.equal(resolveFieldValue(widgetList, 'duplicate'), 'updated')
    assert.equal(resolvePlaceholders('#{duplicate}', widgetList), 'updated')
})

test('条件命中后执行动作，条件不命中时保持原值', () => {
    const condition = createWidget('select', 'condition', 'a,b')
    const target = createWidget('input', 'target', '原值')
    const source = createSource([
        {
            action: 'set-value',
            target: 'target',
            value: '命中',
            when: { field: 'condition', op: 'in', value: 'b,c' },
        },
        {
            action: 'set-value',
            target: 'target',
            value: '不应写入',
            when: { field: 'condition', op: 'eq', value: 'c' },
        },
    ])

    runWidgetActions(source, 'change', [source, condition, target], false)

    assert.equal(target.config.value, '命中')
})

test('动作结果与必填校验和可见值收集联动', () => {
    const target = createWidget('number', 'amount', null, { required: true })
    const hiddenField = createWidget('input', 'hiddenField', '隐藏值', { hidden: true })
    const source = createSource([
        { action: 'set-value', target: 'amount', value: '0' },
    ])
    const form = { formConfig: {}, widgetList: [source, target, hiddenField] }

    assert.equal(validateFormValues(form).valid, false)

    runWidgetActions(source, 'change', form.widgetList, false)

    assert.deepEqual(validateFormValues(form), { valid: true, message: '' })
    assert.deepEqual(collectFormValues(form), [
        { code: 'source', type: 'input', value: null, showValue: null },
        { code: 'amount', type: 'number', value: 0, showValue: 0 },
    ])

    source.config.events.change = [{ action: 'set-hidden', target: 'amount', value: true }]
    runWidgetActions(source, 'change', form.widgetList, false)
    target.config.value = null

    assert.equal(validateFormValues(form).valid, true)
    assert.deepEqual(collectFormValues(form), [
        { code: 'source', type: 'input', value: null, showValue: null },
    ])
})

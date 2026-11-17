import test from 'node:test'
import assert from 'node:assert/strict'
import {
    buildFormTemplate,
    collectFormValues,
    generateFieldCode,
    validateFormSchema,
    validateFormValues,
} from '../src/views/formDesigner/formSchema.js'

function createWidget(code, config = {}, type = 'input') {
    return {
        type,
        config: {
            code,
            label: code,
            hidden: false,
            required: false,
            disabled: false,
            readonly: false,
            value: null,
            showValue: null,
            ...config,
        },
    }
}

function createForm(widgetList = [createWidget('field1')]) {
    return {
        formConfig: { size: 'default', position: 'right' },
        widgetList,
    }
}

test('合法Schema通过校验', () => {
    const form = createForm([
        createWidget('source'),
        createWidget('target', {
            events: {
                change: [{
                    action: 'set-value',
                    target: 'source',
                    value: '#{target}',
                    when: { field: 'source', op: 'notEmpty' },
                }],
            },
        }),
        createWidget('choice', {
            options: [{ label: '零', value: 0 }, { label: '否', value: false }],
        }, 'radio'),
    ])

    assert.deepEqual(validateFormSchema(form), { valid: true, message: '' })
})

test('重复字段编码不能通过Schema校验', () => {
    const result = validateFormSchema(createForm([
        createWidget('duplicate'),
        createWidget('duplicate'),
    ]))

    assert.equal(result.valid, false)
    assert.match(result.message, /字段编码【duplicate】重复/)
})

test('所有开关配置都只接受布尔值或空值', () => {
    for (const key of ['hidden', 'required', 'disabled', 'readonly', 'multiple', 'downloadable']) {
        assert.equal(validateFormSchema(createForm([
            createWidget('field1', { [key]: 'false' }),
        ])).valid, false, key)
    }

    assert.equal(validateFormSchema(createForm([
        createWidget('field1', { hidden: null, required: undefined, disabled: true }),
    ])).valid, true)
})

test('选项为空、重复或结构非法时Schema校验失败', () => {
    const invalidOptions = [
        [],
        [{ label: '一', value: 1 }, { label: '重复', value: '1' }],
        [{ label: '  ', value: 1 }],
        [{ label: '无穷', value: Number.POSITIVE_INFINITY }],
        [{ label: '缺少值' }],
    ]

    for (const options of invalidOptions) {
        const result = validateFormSchema(createForm([
            createWidget('choice', { options }, 'select'),
        ]))
        assert.equal(result.valid, false)
        assert.match(result.message, /选项结构无效/)
    }
})

test('事件目标、条件字段和占位符引用必须存在', () => {
    const cases = [
        {
            rule: { action: 'clear', target: 'missing' },
            message: /目标字段【missing】不存在/,
        },
        {
            rule: {
                action: 'clear',
                target: 'target',
                when: { field: 'missing', op: 'eq', value: '1' },
            },
            message: /条件字段不存在/,
        },
        {
            rule: { action: 'set-value', target: 'target', value: '#{missing}' },
            message: /引用字段【missing】不存在/,
        },
    ]

    for (const item of cases) {
        const source = createWidget('source', { events: { change: [item.rule] } })
        const result = validateFormSchema(createForm([source, createWidget('target')]))
        assert.equal(result.valid, false)
        assert.match(result.message, item.message)
    }
})

test('构造模板会清空运行值且不修改输入', () => {
    const form = createForm([
        createWidget('field1', {
            value: '运行值',
            showValue: '展示值',
            events: { change: [{ action: 'clear', target: 'field1' }] },
        }),
    ])
    const snapshot = structuredClone(form)

    const template = buildFormTemplate(form)

    assert.deepEqual(form, snapshot)
    assert.notStrictEqual(template, form)
    assert.notStrictEqual(template.formConfig, form.formConfig)
    assert.notStrictEqual(template.widgetList[0], form.widgetList[0])
    assert.equal(template.widgetList[0].config.value, null)
    assert.equal(template.widgetList[0].config.showValue, null)
    assert.deepEqual(template.widgetList[0].config.events, form.widgetList[0].config.events)
})

test('字段编码生成会避开当前表单已有编码', (t) => {
    t.mock.method(Date, 'now', () => 1700000000000)
    const code = generateFieldCode([])
    assert.match(code, /^field\d+$/)

    const uniqueCode = generateFieldCode([
        createWidget(code),
        createWidget(`${code}_1`),
    ])
    assert.equal(uniqueCode, `${code}_2`)
})

test('隐藏必填字段不参与校验，数字零和布尔假视为已填写', () => {
    const form = createForm([
        createWidget('hiddenRequired', { hidden: true, required: true, value: '' }),
        createWidget('zero', { required: true, value: 0 }, 'number'),
        createWidget('falseValue', { required: true, value: false }),
    ])

    assert.deepEqual(validateFormValues(form), { valid: true, message: '' })

    form.widgetList.push(createWidget('emptyRequired', { required: true, value: [] }))
    const result = validateFormValues(form, '保存')
    assert.equal(result.valid, false)
    assert.equal(result.message, '【emptyRequired】为必填项，请填写后再保存')
})

test('只收集可见且编码有效的字段并保留零、假和展示值', () => {
    const form = createForm([
        createWidget('zero', { value: 0, showValue: 0 }, 'number'),
        createWidget('enabled', { value: false, showValue: '否' }, 'radio'),
        createWidget('hidden', { hidden: true, value: '隐藏值', showValue: '隐藏值' }),
        createWidget('  ', { value: '无效编码' }),
    ])

    assert.deepEqual(collectFormValues(form), [
        { code: 'zero', type: 'number', value: 0, showValue: 0 },
        { code: 'enabled', type: 'radio', value: false, showValue: '否' },
    ])
})

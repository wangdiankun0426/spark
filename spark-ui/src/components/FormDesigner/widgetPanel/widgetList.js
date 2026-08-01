/**
 * 基础组件配置
 */
export const basicWidgetList = [
    {
        type: 'input',
        name: '单行文本',
        config: {
            code: '',
            label: '单行文本',
            width: 12,
            value: null,
            showValue: null,
            placeholder: '请输入',
            readonly: false,
            disabled: false,
            hidden: false,
        },
    },
    {
        type: 'textarea',
        name: '多行文本',
        config: {
            code: '',
            label: '多行文本',
            width: 12,
            value: null,
            showValue: null,
            placeholder: '请输入',
            readonly: false,
            disabled: false,
            hidden: false,
        },
    },
    {
        type: 'radio',
        name: '单选框',
        config: {
            code: '',
            label: '单选框',
            width: 12,
            value: null,
            showValue: null,
            disabled: false,
            hidden: false,
            options:[
                { value: '1', label: '选项 1' },
                { value: '2', label: '选项 2' },
            ],
        },
    },
    {
        type: 'select',
        name: '下拉选择',
        config: {
            code: '',
            label: '下拉选择',
            width: 12,
            value: null,
            showValue: null,
            placeholder: '请选择',
            disabled: false,
            hidden: false,
            options:[
                { value: '1', label: '选项 1' },
                { value: '2', label: '选项 2' },
            ],
        },
    },
]

export const sysWidgetList = [
    {
        type: 'select-user',
        name: '选择用户',
        config: {
            code: '',
            label: '选择用户',
            width: 12,
            value: null,
            showValue: null,
            placeholder: '请选择用户',
            disabled: false,
            hidden: false,
            multiple: false,
        },
    },
    {
        type: 'select-dept',
        name: '选择部门',
        config: {
            code: '',
            label: '选择部门',
            width: 12,
            value: null,
            showValue: null,
            placeholder: '请选择部门',
            disabled: false,
            hidden: false,
            multiple: false,
        },
    },
    {
        type: 'select-role',
        name: '选择角色',
        config: {
            code: '',
            label: '选择角色',
            width: 12,
            value: null,
            showValue: null,
            placeholder: '请选择角色',
            disabled: false,
            hidden: false,
            multiple: false,
        },
    }
]
<template>
  <el-tree-select
      ref="treeSelectRef"
      v-model="innerValue"
      :data="deptTree"
      :multiple="multiple"
      :disabled="disabled"
      :placeholder="placeholder"
      :clearable="clearable"
      :size="size"
      :collapse-tags="collapseTags"
      :check-strictly="checkStrictly"
      check-on-click-node
      :render-after-expand="false"
      :props="treeProps"
      node-key="id"
      :show-checkbox="multiple"
      filterable
      :loading="loading"
      @check="handleCheck"
      @change="handleChange"
  />
</template>

<script setup>
import { ref, watch, onMounted, getCurrentInstance } from 'vue'
import { treeDeptAPI } from '@/api/system/dept'

defineOptions({ name: 'selectDept' })

const { proxy } = getCurrentInstance()

const props = defineProps({
  // 多选绑 ID 数组；单选绑单个 ID
  modelValue: { type: [Array, Number, String], default: null },
  // 是否多选
  multiple: { type: Boolean, default: false },
  // 整体禁用
  disabled: { type: Boolean, default: false },
  // 占位文案
  placeholder: { type: String, default: '请选择部门' },
  // 是否可清空
  clearable: { type: Boolean, default: true },
  // 透传 el-tree-select size
  size: { type: String, default: '' },
  // 多选时是否折叠 tag
  collapseTags: { type: Boolean, default: false },
  // 父子不关联勾选（勾选父节点不会自动勾选子节点）
  checkStrictly: { type: Boolean, default: true },
  // 外部传入部门树（不传则内部自动加载 treeDeptAPI）
  data: { type: Array, default: undefined }
})

const emit = defineEmits(['update:modelValue', 'update:showValue', 'change'])

const deptTree = ref(props.data || [])
const loading = ref(false)
const treeProps = { label: 'name', value: 'id', children: 'children' }

// el-tree-select 内部值，由 props.modelValue 同步
const innerValue = ref(
  props.multiple
    ? (Array.isArray(props.modelValue) ? [...props.modelValue] : [])
    : props.modelValue
)

// 同步外部传入的 modelValue
watch(() => props.modelValue, (val) => {
  if (props.multiple) {
    innerValue.value = Array.isArray(val) ? [...val] : []
  } else {
    innerValue.value = val
  }
}, { deep: true })

// 外部传入 data 时同步
watch(() => props.data, (val) => {
  if (val !== undefined) deptTree.value = val
})

// 加载部门树
async function loadDeptTree() {
  // 外部已传 data 则不加载
  if (props.data !== undefined) return
  loading.value = true
  try {
    const res = await treeDeptAPI()
    if (res.code === 200) {
      deptTree.value = res.data || []
    }
  } finally {
    loading.value = false
  }
}

// 递归从部门树中按 ID 找节点名称
function findNameByIds(ids) {
  if (!Array.isArray(ids)) return ''
  const result = []
  const walk = (nodes) => {
    for (const node of nodes || []) {
      if (ids.includes(node.id)) result.push(node.name)
      if (node.children?.length) walk(node.children)
    }
  }
  walk(deptTree.value)
  return result
}

// 统一对外 emit
function emitChange(val) {
  emit('update:modelValue', val)
  if (props.multiple) {
    emit('update:showValue', findNameByIds(val))
  } else {
    const names = findNameByIds([val])
    emit('update:showValue', names[0] || '')
  }
  emit('change', val)
}

// 多选模式下复选框勾选触发（el-tree 的 check 事件，change 事件不触发）
function handleCheck() {
  if (!props.multiple) return
  const checkedKeys = proxy.$refs.treeSelectRef.getCheckedKeys()
  innerValue.value = checkedKeys
  emitChange(checkedKeys)
}

// 单选模式或清空操作触发 el-select 的 change 事件
function handleChange(val) {
  if (props.multiple) return
  emitChange(val)
}

onMounted(loadDeptTree)
</script>

<style scoped lang="scss">
// 复用 element.scss 中的 el-tree-select 变量
// el-tree-select 默认宽度 100%（2.2.1 上 inline style 透传不稳定，用 scoped 强制）
.el-select,
:deep(.el-select) {
  width: 100%;
}
</style>


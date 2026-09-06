<template>
  <el-select
      v-model="innerValue"
      :multiple="multiple"
      :disabled="disabled"
      :placeholder="placeholder"
      :clearable="clearable"
      :size="size"
      :collapse-tags="collapseTags"
      :fit-input-width="fitInputWidth"
      filterable
      :loading="loading"
      @change="handleChange"
  >
    <el-option
        v-for="user in userList"
        :key="user.id"
        :label="user.name"
        :value="user.id"
    />
  </el-select>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { pageUserListAPI } from '@/api/manage/sys/user'

defineOptions({ name: 'selectUser' })

const props = defineProps({
  // 多选绑 ID 数组；单选绑单个 ID
  modelValue: { type: [Array, Number, String], default: null },
  // 是否多选
  multiple: { type: Boolean, default: false },
  // 整体禁用
  disabled: { type: Boolean, default: false },
  // 占位文案
  placeholder: { type: String, default: '请选择用户' },
  // 是否可清空
  clearable: { type: Boolean, default: true },
  // 透传 el-select size
  size: { type: String, default: '' },
  // 多选时是否折叠 tag
  collapseTags: { type: Boolean, default: false },
  // 下拉宽度跟随输入框
  fitInputWidth: { type: Boolean, default: true }
})

const emit = defineEmits(['update:modelValue', 'update:showValue', 'change'])

const userList = ref([])
const loading = ref(false)

// el-select 内部值，由 props.modelValue 同步
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

// 加载全量用户列表
async function loadUserList() {
  loading.value = true
  try {
    const res = await pageUserListAPI({ page: false })
    if (res.code === 200) {
      userList.value = res.data.rows || []
    }
  } finally {
    loading.value = false
  }
}

// 根据 ID 数组反查名称数组
function getNamesByIds(ids) {
  if (!Array.isArray(ids)) return ''
  return ids
      .map(id => userList.value.find(u => u.id === id)?.name)
      .filter(Boolean)
}

// 选项变化时同步外部 v-model 与 showValue
function handleChange(val) {
  emit('update:modelValue', val)
  if (props.multiple) {
    emit('update:showValue', getNamesByIds(val))
  } else {
    const user = userList.value.find(u => u.id === val)
    emit('update:showValue', user?.name || '')
  }
  emit('change', val)
}

onMounted(loadUserList)
</script>

<style scoped lang="scss">
// 复用 element.scss 中的 el-select 变量，无额外样式
</style>

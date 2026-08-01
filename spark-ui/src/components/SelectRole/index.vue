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
        v-for="role in roleList"
        :key="role.id"
        :label="role.name"
        :value="role.id"
    />
  </el-select>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { pageRoleListAPI } from '@/api/system/role'

defineOptions({ name: 'selectRole' })

const props = defineProps({
  modelValue: { type: [Array, Number, String], default: null },
  multiple: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  placeholder: { type: String, default: '请选择角色' },
  clearable: { type: Boolean, default: true },
  size: { type: String, default: '' },
  collapseTags: { type: Boolean, default: false },
  fitInputWidth: { type: Boolean, default: true }
})

const emit = defineEmits(['update:modelValue', 'update:showValue', 'change'])

const roleList = ref([])
const loading = ref(false)

const innerValue = ref(
    props.multiple
      ? (Array.isArray(props.modelValue) ? [...props.modelValue] : [])
      : props.modelValue
)

watch(() => props.modelValue, (val) => {
  if (props.multiple) {
    innerValue.value = Array.isArray(val) ? [...val] : []
  } else {
    innerValue.value = val
  }
}, { deep: true })

async function loadRoleList() {
  loading.value = true
  try {
    const res = await pageRoleListAPI({ page: false })
    if (res.code === 200) {
      roleList.value = res.data.rows || []
    }
  } finally {
    loading.value = false
  }
}

function getNamesByIds(ids) {
  if (!Array.isArray(ids)) return ''
  return ids
      .map(id => roleList.value.find(r => r.id === id)?.name)
      .filter(Boolean)
}

function handleChange(val) {
  emit('update:modelValue', val)
  if (props.multiple) {
    emit('update:showValue', getNamesByIds(val))
  } else {
    const role = roleList.value.find(r => r.id === val)
    emit('update:showValue', role?.name || '')
  }
  emit('change', val)
}

onMounted(loadRoleList)
</script>

<style scoped lang="scss">
</style>
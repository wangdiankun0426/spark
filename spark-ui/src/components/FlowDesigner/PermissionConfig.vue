<template>
  <div class="permission-config">
    <el-form-item label="节点权限">
      <el-checkbox-group :model-value="selectedPermissions" @change="handleChange">
        <el-checkbox
            v-for="item in permissionOptions"
            :key="item.value"
            :label="item.value"
        >
          {{ item.label }}
        </el-checkbox>
      </el-checkbox-group>
    </el-form-item>
  </div>
</template>

<script setup>
import { computed } from 'vue'

defineOptions({ name: 'PermissionConfig' })

const props = defineProps({
  node: { type: Object, required: true }
})

/**
 * 权限选项：每个选项对应一个二进制位，
 * 最终权限值 = 选中选项值的累加（如 允许审批通过+允许审批驳回 = 3）
 */
const permissionOptions = [
  { label: '允许审批人审批通过', value: 1 },
  { label: '允许审批人审批驳回', value: 2 },
  { label: '没有审批人时自动通过', value: 4 }
]

/** 根据累加值还原选中的权限项 */
const selectedPermissions = computed(() => {
  const permission = Number(props.node.permission) || 0
  return permissionOptions
      .filter(item => (permission & item.value) === item.value)
      .map(item => item.value)
})

/** 选中项变化时，将各项值累加写入 node.permission */
function handleChange(val) {
  props.node.permission = (val || []).reduce((sum, item) => sum + Number(item), 0)
}
</script>

<style scoped lang="scss">
.permission-config {
  width: 100%;

  /* 每个权限类型占一行，纵向排列 */
  :deep(.el-checkbox-group) {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: $spacing-sm;
  }
}
</style>

<template>
  <div class="user-task-node" :class="{ selected }">
    <el-icon class="node-icon"><User /></el-icon>
    <span class="node-name">{{ node.name || '用户任务' }}</span>
    <div class="node-info">
      <span class="node-tag">{{ node.assigneeLabel || getAssigneeTypeLabel(node.assigneeType) }}</span>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  node: { type: Object, required: true },
  selected: { type: Boolean, default: false }
})

// 审批人类型选项
const assigneeTypeOptions = [
  { label: '流程发起人', value: '1' },
  { label: '系统自动通过', value: '2' },
  { label: '指定用户', value: '3' },
  { label: '指定部门', value: '4' },
  { label: '指定角色', value: '5' },
  { label: '表单数据', value: '6' }
]

/** 审批人类型回显 label */
function getAssigneeTypeLabel(type) {
  const find = assigneeTypeOptions.find(item => item.value === type)
  return find ? find.label : '未选择审批人'
}
</script>

<style scoped lang="scss">
.user-task-node {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  border-radius: 8px;
  background: #e6f7ff;
  border: 1.5px solid #1890ff;

  &.selected {
    border-width: 2.5px;
    box-shadow: 0 0 8px rgba(24, 144, 255, 0.35);
  }
}
.node-icon {
  font-size: 20px;
  line-height: 1;
  color: #1890ff;
}
.node-name {
  font-size: 13px;
  font-weight: 550;
  color: $color-text-primary;
  line-height: 1.2;
  word-break: break-all;
  max-width: 116px;
}
.node-info {
  margin-top: 1px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.node-tag {
  display: inline-block;
  max-width: 100%;
  padding: 0 8px;
  border-radius: 10px;
  font-size: 10px;
  line-height: 1.7;
  vertical-align: middle;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  background: #eaf3ff;
  color: #1890ff;
}
</style>

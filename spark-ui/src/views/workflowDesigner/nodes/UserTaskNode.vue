<template>
  <div class="user-task-node" :class="{ selected }">
    <el-icon class="node-icon"><User /></el-icon>
    <span class="node-name">{{ node.name || '用户任务' }}</span>
    <div class="node-info">
      <span class="node-tag">{{ node.assigneeLabel || getAssigneeTypeLabel(node.assigneeType) }}</span>
      <span class="node-tag approve-tag" :class="`approve-${node.approveType || '1'}`">{{ getApproveTypeLabel(node.approveType) }}</span>
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

// 审批类型选项（与后端 FlowApproveTypeEnum 对应）
const approveTypeOptions = [
  { label: '或签', value: '1' },
  { label: '会签', value: '2' },
  { label: '依次审批', value: '3' }
]

/** 审批类型回显 label（空值默认或签） */
function getApproveTypeLabel(type) {
  const find = approveTypeOptions.find(item => item.value === (type || '1'))
  return find ? find.label : '或签'
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
  background: $app-node-task-bg;
  border: 1.5px solid $app-node-task;

  &.selected {
    border-width: 2.5px;
    box-shadow: 0 0 8px rgba(24, 144, 255, 0.35);
  }
}
.node-icon {
  font-size: 20px;
  line-height: 1;
  color: $app-node-task;
}
.node-name {
  font-size: 13px;
  font-weight: 400;
  color: $app-text-primary;
  line-height: 1.2;
  word-break: break-all;
  max-width: 116px;
}
.node-info {
  margin-top: 1px;
  max-width: 120px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 2px;
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
  background: $app-node-task-tag-bg;
  color: $app-node-task-tag;
}
.approve-tag.approve-2 {
  background: $app-node-approve-ok-bg;
  color: $app-node-approve-ok;
}
.approve-tag.approve-3 {
  background: $app-node-approve-warn-bg;
  color: $app-node-approve-warn;
}

// 暗色主题节点边框改为同色系半透明，底色与标签色由节点 token 自动翻转
html.dark .user-task-node {
  border-color: rgba(120, 170, 255, 0.55);
}
</style>

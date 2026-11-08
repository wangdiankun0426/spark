<template>
  <div class="user-task-node" :class="{ selected }">
    <span class="node-ico">
      <el-icon :size="16"><User /></el-icon>
    </span>
    <span class="node-text">
      <b :title="node.name || '用户任务'">{{ node.name || '用户任务' }}</b>
      <span class="node-tags">
        <span class="node-tag">{{ node.assigneeLabel || getAssigneeTypeLabel(node.assigneeType) }}</span>
        <span class="node-tag approve-tag" :class="`approve-${node.approveType || '1'}`">{{ getApproveTypeLabel(node.approveType) }}</span>
      </span>
    </span>
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
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 12px;
  border-radius: 12px;
  // 白底卡片，与 workflow 设计器的节点一致：类型色只落在描边与图标上
  background: $app-bg-card;
  border: 1.5px solid color-mix(in srgb, $app-node-task 60%, $app-bg-card);
  box-shadow: 0 1px 4px rgba(31, 45, 74, 0.06);
  transition: box-shadow 0.15s;

  &.selected {
    border-width: 2.5px;
    border-color: $app-node-task;
    box-shadow: 0 0 8px rgba(24, 144, 255, 0.35);
  }
}

// 图标块：32px 圆角底 + 类型色图标，与 workflow 节点卡一致
.node-ico {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  background: $app-node-task-bg;
  color: $app-node-task;
}

.node-text {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;

  b {
    font-size: 13px;
    font-weight: 600;
    line-height: 18px;
    color: $app-text-primary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

// 审批人/审批方式标签：与名称同列，单行放不下时逐个省略
.node-tags {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 2px;
  overflow: hidden;
}

.node-tag {
  flex: 0 1 auto;
  min-width: 0;
  padding: 0 6px;
  border-radius: 10px;
  font-size: 10px;
  line-height: 17px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  background: $app-node-task-tag-bg;
  color: $app-node-task-tag;
}
// 审批方式标签很短，固定不压缩，保证「会签 / 依次审批」始终完整可见
.node-tags .approve-tag {
  flex: 0 0 auto;
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

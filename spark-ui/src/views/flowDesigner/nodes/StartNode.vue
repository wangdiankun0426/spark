<template>
  <div class="start-node" :class="{ selected }">
    <el-icon class="node-icon"><VideoPlay /></el-icon>
    <span class="node-name">{{ node.name || '开始' }}</span>
  </div>
</template>

<script setup>
defineProps({
  node: { type: Object, required: true },
  selected: { type: Boolean, default: false }
})
</script>

<style scoped lang="scss">
.start-node {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  border-radius: 8px;
  background: #f6ffed;
  border: 1.5px solid #52c41a;

  &.selected {
    border-width: 2.5px;
    box-shadow: 0 0 8px rgba(82, 196, 26, 0.35);
  }
}
.node-icon {
  font-size: 20px;
  line-height: 1;
  color: #52c41a;
}
.node-name {
  font-size: 13px;
  font-weight: 550;
  color: $color-text-primary;
  line-height: 1.2;
  word-break: break-all;
  max-width: 120px;
}
</style>

<template>
  <div class="end-node" :class="{ selected }">
    <el-icon class="node-icon"><CircleClose /></el-icon>
    <span class="node-name">{{ node.name || '结束' }}</span>
  </div>
</template>

<script setup>
defineProps({
  node: { type: Object, required: true },
  selected: { type: Boolean, default: false }
})
</script>

<style scoped lang="scss">
.end-node {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  border-radius: 8px;
  background: #fff2f0;
  border: 1.5px solid #ff4d4f;

  &.selected {
    border-width: 2.5px;
    box-shadow: 0 0 8px rgba(255, 77, 79, 0.35);
  }
}
.node-icon {
  font-size: 20px;
  line-height: 1;
  color: #ff4d4f;
}
.node-name {
  font-size: 13px;
  font-weight: 400;
  color: $color-text-primary;
  line-height: 1.2;
  word-break: break-all;
  max-width: 120px;
}
</style>

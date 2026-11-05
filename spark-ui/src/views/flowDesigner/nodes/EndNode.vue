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
  background: $app-node-end-bg;
  border: 1.5px solid $app-node-end;

  &.selected {
    border-width: 2.5px;
    box-shadow: 0 0 8px rgba(255, 77, 79, 0.35);
  }
}
.node-icon {
  font-size: 20px;
  line-height: 1;
  color: $app-node-end;
}
.node-name {
  font-size: 13px;
  font-weight: 400;
  color: $app-text-primary;
  line-height: 1.2;
  word-break: break-all;
  max-width: 120px;
}

// 暗色主题节点边框改为同色系半透明，底色与图标由节点 token 自动翻转
html.dark .end-node {
  border-color: rgba(255, 105, 100, 0.55);
}
</style>

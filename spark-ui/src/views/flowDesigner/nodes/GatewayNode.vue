<template>
  <div class="gateway-node" :class="{ selected }">
    <span class="node-ico">
      <el-icon :size="16"><Share /></el-icon>
    </span>
    <span class="node-text">
      <b :title="node.name || '条件分支'">{{ node.name || '条件分支' }}</b>
    </span>
  </div>
</template>

<script setup>
defineProps({
  node: { type: Object, required: true },
  selected: { type: Boolean, default: false }
})
</script>

<style scoped lang="scss">
.gateway-node {
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
  border: 1.5px solid color-mix(in srgb, $app-node-gateway 60%, $app-bg-card);
  box-shadow: 0 1px 4px rgba(31, 45, 74, 0.06);
  transition: box-shadow 0.15s;

  &.selected {
    border-width: 2.5px;
    border-color: $app-node-gateway;
    box-shadow: 0 0 8px rgba(250, 140, 22, 0.35);
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
  background: $app-node-gateway-bg;
  color: $app-node-gateway;
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

// 暗色主题节点边框改为同色系半透明，底色与图标由节点 token 自动翻转
html.dark .gateway-node {
  border-color: rgba(255, 169, 64, 0.55);
}
</style>

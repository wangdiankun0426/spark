<template>
  <div class="node-panel">
    <el-collapse :model-value="expanded">
      <el-collapse-item
          v-for="group in groups"
          :key="group.name"
          :name="group.name"
          :title="group.title"
      >
        <div
            v-for="node in group.nodes"
            :key="node.type"
            class="node-item"
            draggable="true"
            @dragstart="onDragStart($event, node.type)"
        >
          <span class="node-icon" :style="{ background: node.bgColor, color: node.color }">
            <el-icon><component :is="node.icon" /></el-icon>
          </span>
          <span>{{ node.name }}</span>
        </div>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup>
defineOptions({ name: 'NodePanel' })

const props = defineProps({
  /** 节点分组配置：{ name, title, nodes: [{ type, name, icon, color, bgColor }] } */
  groups: { type: Array, default: () => [] },
  /** 默认展开的分组 name 数组 */
  expanded: { type: Array, default: () => [] },
  /** 拖拽时写入的 dataTransfer key（Flow: node-type / Workflow: nodeType） */
  dataKey: { type: String, default: 'nodeType' }
})

/** 拖拽节点：写入节点类型，供 DesignerCanvas drop 解析 */
function onDragStart(event, nodeType) {
  event.dataTransfer.setData(props.dataKey, nodeType)
  event.dataTransfer.effectAllowed = 'copy'
}
</script>

<style scoped lang="scss">
.node-panel {
  width: 200px;
  background: $app-bg-card;
  border-right: 1px solid $border-color;
  flex-shrink: 0;
  overflow-y: auto;
}

.node-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin: 0 $spacing-sm $spacing-xs;
  padding: $spacing-sm;
  border: 1px solid transparent;
  border-radius: $border-radius-md;
  cursor: grab;
  font-size: 13px;
  color: $color-text-primary;
  transition: $transition-fast;

  &:hover {
    background: $app-bg-card;
    border-color: $app-border-light;
    box-shadow: $app-shadow-card;
  }
  &:active { cursor: grabbing; }
}

html.dark .node-item:hover {
  background: $app-bg-sub;
}

.node-icon {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 14px;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.25);
}

:deep(.el-collapse) { border: none; }
:deep(.el-collapse-item__header) {
  height: 40px;
  border: none;
  background: transparent;
  padding: 0 $spacing-md;
}
:deep(.el-collapse-item__wrap) {
  border: none;
  background: transparent;
}
</style>

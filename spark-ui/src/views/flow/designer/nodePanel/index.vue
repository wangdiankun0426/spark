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
  background: #fff;
  border-right: 1px solid $border-color;
  flex-shrink: 0;
  overflow-y: auto;
}

.node-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  cursor: grab;
  font-size: 13px;
  color: $color-text-primary;
  transition: background 0.15s;

  &:hover { background: $color-primary-light; }
  &:active { cursor: grabbing; }
}

.node-icon {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 14px;
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

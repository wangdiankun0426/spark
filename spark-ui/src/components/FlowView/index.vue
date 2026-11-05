<template>
  <div class="bpmn-designer-container" ref="containerEl" :style="{ '--fv-scale': viewScale }">
    <index
            :nodes="nodes"
            :sequences="sequences"
            source-key="sourceRef"
            target-key="targetRef"
            :selected-node-id="selectedNode?.id"
            readonly
            :canvas-width="canvasWidth"
            :canvas-height="canvasHeight"
            :node-components="NODE_COMPONENTS"
            :field-options="fieldOptions"
            @select-node="selectNode"
        />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import Index from '@/views/flowDesigner/canvas/index.vue'
import { NODE_COMPONENTS } from '@/views/flowDesigner/nodes'

const props = defineProps({
  bpmJson: Object,
  // 表单字段选项（label/value），用于分支条件标签的字段编码翻译
  fieldOptions: { type: Array, default: () => [] }
});

const containerEl = ref(null)
const nodes = ref([])
const sequences = ref([])
const selectedNode = ref(null)
const canvasWidth = ref(2000)
const canvasHeight = ref(1000)
// 画布内容缩放比例（内容超出容器时整体缩小，1 为原尺寸）
const viewScale = ref(1)

/** 原始节点数据（保留设计器坐标），每次适配时基于它重新映射位置 */
let rawNodes = []

onMounted(() => {
  if (!props.bpmJson) return
  rawNodes = props.bpmJson.nodes.map(el => ({
    id: el.id,
    type: el.type,
    name: el.name,
    x: el.x,
    y: el.y,
    assigneeType: el.assigneeType,
    assignee: el.assignee,
    assigneeLabel: el.assigneeLabel,
    approveType: el.approveType
  }))
  sequences.value = props.bpmJson.sequences.map(seq => ({
    id: seq.id,
    sourceRef: seq.sourceRef,
    targetRef: seq.targetRef,
    name: seq.name,
    conditionExpression: seq.conditionExpression
  }))
  fitCanvas()
  // 容器尺寸变化（弹窗缩放/页签切换）时重新适配
  resizeObserver = new ResizeObserver(() => fitCanvas())
  resizeObserver.observe(containerEl.value)
})

let resizeObserver = null

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
})

/**
 * 各类型节点的默认宽高（与设计器一致）
 */
const NODE_WIDTHS = { exclusiveGateway: 80, startEvent: 120, endEvent: 120 }
const NODE_HEIGHTS = { exclusiveGateway: 50, startEvent: 48, endEvent: 48 }
const DEFAULT_NODE_WIDTH = 110
const DEFAULT_NODE_HEIGHT = 72

/**
 * 自适应画布：内容在容器内居中；内容超出容器时整体等比缩小，保证完整可见
 */
const fitCanvas = () => {
  if (!rawNodes.length) {
    canvasWidth.value = 800
    canvasHeight.value = 600
    viewScale.value = 1
    nodes.value = []
    return
  }

  // 计算内容包围盒
  let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity
  rawNodes.forEach(node => {
    const w = NODE_WIDTHS[node.type] || DEFAULT_NODE_WIDTH
    const h = NODE_HEIGHTS[node.type] || DEFAULT_NODE_HEIGHT
    minX = Math.min(minX, node.x)
    minY = Math.min(minY, node.y)
    maxX = Math.max(maxX, node.x + w)
    maxY = Math.max(maxY, node.y + h)
  })
  const contentW = maxX - minX
  const contentH = maxY - minY

  // 容器可用尺寸（留边距）
  const MARGIN = 24
  const availW = Math.max((containerEl.value?.clientWidth || 800) - MARGIN * 2, 200)
  const availH = Math.max((containerEl.value?.clientHeight || 600) - MARGIN * 2, 200)

  // 内容超宽/超高时等比缩小
  const scale = Math.min(1, availW / contentW, availH / contentH)
  viewScale.value = scale

  // 缩放后画布的视口尺寸（预缩放坐标系）
  const viewW = availW / scale
  const viewH = availH / scale
  const padX = Math.max(MARGIN, (viewW - contentW) / 2)
  const padY = Math.max(MARGIN, (viewH - contentH) / 2)

  // 位置重映射：去掉设计器绝对坐标偏移，内容在画布内居中
  nodes.value = rawNodes.map(node => ({
    ...node,
    x: node.x - minX + padX,
    y: node.y - minY + padY
  }))

  canvasWidth.value = Math.max(viewW, contentW + padX * 2)
  canvasHeight.value = Math.max(viewH, contentH + padY * 2)
}

const selectNode = (node) => {
  selectedNode.value = node
}
</script>

<style scoped lang="scss">
.bpmn-designer-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: $app-canvas-bg;
  overflow: hidden;
}

// 内容超出容器时整体等比缩小，比例由 fitCanvas 计算后写入私有变量
.bpmn-designer-container :deep(.canvas-content) {
  // stylelint-disable-next-line function-disallowed-list
  transform: scale(var(--fv-scale, 1));
  transform-origin: 0 0;
}
</style>

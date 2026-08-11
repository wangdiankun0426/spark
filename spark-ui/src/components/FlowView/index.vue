<template>
  <div class="bpmn-designer-container">
    <index
            :nodes="nodes"
            :edges="sequences"
            source-key="sourceRef"
            target-key="targetRef"
            :selected-node-id="selectedNode?.id"
            readonly
            :canvas-width="canvasWidth"
            :canvas-height="canvasHeight"
            :node-components="NODE_COMPONENTS"
            @select-node="selectNode"
        />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Index from '@/views/flow/designer/canvas/index.vue'
import { NODE_COMPONENTS } from '@/views/flow/designer/nodes'

const props = defineProps({
  bpmJson: Object
});

const nodes = ref([])
const sequences = ref([])
const selectedNode = ref(null)
const canvasWidth = ref(2000)
const canvasHeight = ref(1000)

onMounted(() => {
  if (!props.bpmJson) return
  nodes.value = props.bpmJson.nodes.map(el => ({
    id: el.id,
    type: el.type,
    name: el.name,
    x: el.x,
    y: el.y,
    assigneeType: el.assigneeType,
    assignee: el.assignee,
    assigneeLabel: el.assigneeLabel
  }))
  sequences.value = props.bpmJson.sequences.map(seq => ({
    id: seq.id,
    sourceRef: seq.sourceRef,
    targetRef: seq.targetRef,
    name: seq.name,
    conditionExpression: seq.conditionExpression
  }))
  calcCanvasSize()
})

/**
 * 根据节点位置计算画布尺寸
 */
const calcCanvasSize = () => {
  const PADDING = 200
  const defaultNodeWidth = 110
  const defaultNodeHeight = 72
  const nodeWidths = { exclusiveGateway: 80, startEvent: 120, endEvent: 120 }
  const nodeHeights = { exclusiveGateway: 50, startEvent: 48, endEvent: 48 }

  let maxX = 0, maxY = 0
  if (nodes.value.length > 0) {
    nodes.value.forEach(node => {
      const w = nodeWidths[node.type] || defaultNodeWidth
      const h = nodeHeights[node.type] || defaultNodeHeight
      maxX = Math.max(maxX, node.x + w)
      maxY = Math.max(maxY, node.y + h)
    })
  } else {
    maxX = 800
    maxY = 600
  }
  canvasWidth.value = Math.max(maxX + PADDING, 800)
  canvasHeight.value = Math.max(maxY + PADDING, 600)
}

const selectNode = (node) => {
  selectedNode.value = node
}
</script>

<style scoped>
.bpmn-designer-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  overflow: hidden;
}
</style>
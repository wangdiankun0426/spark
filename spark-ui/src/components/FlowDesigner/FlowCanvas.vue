<template>
  <div class="canvas flow-canvas-scrollbar"
       @drop="onDrop"
       @dragover="onDragover"
       ref="canvasRef">
    <!-- 流程节点 -->
    <div
        v-for="node in nodes"
        :key="node.id"
        class="bpmn-node"
        :class="[getNodeClass(node.type), { 'selected-node': selectedNode && selectedNode.id === node.id }]"
        :style="{ top: node.y + 'px', left: node.x + 'px' }"
        @click.stop="handleSelectNode(node)"
        @mousedown="handleStartDragNode($event, node)"
        @mouseenter="showNodeControls(node)"
        @mouseleave="delayHideNodeControls(node)"
    >
      <!-- 开始/结束：圆形节点，图标与名称居中在圆内 -->
      <div v-if="isEventNode(node.type)" class="node-content">
        <el-icon class="node-main-icon">
          <component :is="getNodeIcon(node.type)" />
        </el-icon>
        <span class="node-name">{{ node.name }}</span>
      </div>
      <!-- 排他网关 -->
      <div v-else-if="node.type === 'exclusiveGateway'" class="node-content">
        <el-icon class="gateway-icon"><Share /></el-icon>
        <span class="gateway-name">{{ node.name }}</span>
      </div>
      <!-- 任务节点 -->
      <div v-else class="node-content">
        <el-icon class="node-main-icon">
          <component :is="getNodeIcon(node.type)" />
        </el-icon>
        <span class="node-name">{{ node.name }}</span>
        <div v-if="node.type === 'userTask'" class="node-info">
          <span class="assignee-type">
            {{ node.assigneeLabel || getAssigneeTypeLabel(node.assigneeType) }}
          </span>
        </div>
        <div v-if="node.type === 'serviceTask' && getWorkflowName(node)" class="node-info">
          <span class="ai-tag">{{ getWorkflowName(node) }}</span>
        </div>
      </div>

      <!-- 节点控制按钮 -->
      <div
          v-if="node.showControls && !readonly"
          class="node-controls"
          @mouseenter="keepNodeControls(node)"
          @mouseleave="delayHideNodeControls(node)"
      >
        <el-button size="small" type="danger" @click.stop="handleDeleteNode(node.id)">删除</el-button>
      </div>

      <!-- 四个方向的连接点 -->
      <template v-if="!readonly">
        <div class="sequence-point left-point" @mousedown="startSequence($event, node, 'left')"></div>
        <div class="sequence-point right-point" @mousedown="startSequence($event, node, 'right')"></div>
        <div class="sequence-point top-point" @mousedown="startSequence($event, node, 'top')"></div>
        <div class="sequence-point bottom-point" @mousedown="startSequence($event, node, 'bottom')"></div>
      </template>
    </div>

    <!-- 连线 SVG -->
    <svg class="sequences-svg" :width="canvasWidth" :height="canvasHeight">
      <defs>
        <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
          <polygon points="0 0, 10 3.5, 0 7" fill="#666" />
        </marker>
      </defs>

      <path
          v-for="sequence in sequences"
          :key="'hit-' + sequence.id"
          :d="getPolylinePath(sequence)"
          fill="none"
          stroke="transparent"
          stroke-width="12"
          class="sequence-hit-area"
          @click.stop="handleSelectSequence(sequence)"
          @mouseenter="showSequenceControls(sequence)"
          @mouseleave="delayHideSequenceControls(sequence)"
      />

      <path
          v-for="sequence in sequences"
          :key="sequence.id"
          :d="getPolylinePath(sequence)"
          stroke="#666"
          stroke-width="2"
          fill="none"
          stroke-linecap="round"
          stroke-linejoin="round"
          marker-end="url(#arrowhead)"
          :class="{ 'selected-sequence': selectedSequence && selectedSequence.id === sequence.id }"
          class="sequence-visible-line"
          @click.stop="handleSelectSequence(sequence)"
          @mouseenter="showSequenceControls(sequence)"
          @mouseleave="delayHideSequenceControls(sequence)"
      />

      <text
          v-for="sequence in sequences"
          :key="'text-' + sequence.id"
          :x="getSequenceTextPosition(sequence).x"
          :y="getSequenceTextPosition(sequence).y"
          fill="#5e6c84"
          font-size="11"
          text-anchor="middle"
          dominant-baseline="middle"
          class="sequence-label"
          @click.stop="handleSelectSequence(sequence)"
          @mouseenter="showSequenceControls(sequence)"
          @mouseleave="delayHideSequenceControls(sequence)"
      >
        {{ sequence.name }}
      </text>

      <path
          v-if="draggingSequence"
          :d="getDraggingPolylinePath()"
          stroke="#1890ff"
          stroke-width="2"
          fill="none"
          marker-end="url(#arrowhead)"
          stroke-dasharray="5,5"
      />
    </svg>

    <!-- 连线控制按钮 -->
    <div
        v-for="sequence in sequences"
        :key="'controls-' + sequence.id"
        v-show="sequence.showControls && !readonly"
        class="sequence-controls"
        :style="{
          top: getSequenceMidPoint(sequence).y - 20 + 'px',
          left: getSequenceMidPoint(sequence).x - 40 + 'px'
        }"
        @mouseenter="keepSequenceControls(sequence)"
        @mouseleave="delayHideSequenceControls(sequence)"
    >
      <el-button size="small" type="danger" @click.stop="handleDeleteSequence(sequence.id)">删除</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'FlowCanvas' })

const props = defineProps({
  nodes: { type: Array, default: () => [] },
  sequences: { type: Array, default: () => [] },
  selectedNode: { type: Object, default: null },
  selectedSequence: { type: Object, default: null },
  readonly: { type: Boolean, default: false },
  canvasWidth: { type: Number, default: 2000 },
  canvasHeight: { type: Number, default: 1000 },
  workflowList: { type: Array, default: () => [] }
})

const emit = defineEmits([
  'selectNode', 'selectSequence',
  'editNode', 'deleteNode',
  'editSequence', 'deleteSequence',
  'startDragNode', 'startSequence',
  'drop', 'dragover',
  'update:nodes', 'update:sequences'
])

const canvasRef = ref(null)
const draggingSequence = ref(null)
const sourceNode = ref(null)
const elementHideTimers = ref({})
const sequenceHideTimers = ref({})

// 节点配置（颜色与 AI 工作流设计器对齐）
const nodeConfig = {
  startEvent: { icon: 'video-play', label: '开始', color: '#52c41a', bgColor: '#f6ffed' },
  endEvent: { icon: 'circle-close', label: '结束', color: '#f5222d', bgColor: '#fff1f0' },
  userTask: { icon: 'user', label: '用户任务', color: '#1890ff', bgColor: '#e6f7ff' },
  exclusiveGateway: { icon: 'share', label: '排他网关', color: '#fa8c16', bgColor: '#fffbe6' },
  serviceTask: { icon: 'magic-stick', label: 'AI工作流', color: '#722ed1', bgColor: '#f9f0ff' }
}

// 节点尺寸（与 .bpmn-node 各类节点 CSS 保持一致，供连接点/命中检测使用）
const nodeSizeMap = {
  startEvent: { width: 48, height: 48 },
  endEvent: { width: 48, height: 48 },
  userTask: { width: 128, height: 72 },
  serviceTask: { width: 128, height: 72 },
  exclusiveGateway: { width: 108, height: 72 }
}

const getNodeSize = (node) => nodeSizeMap[node.type] || { width: 128, height: 72 }

/** 是否为开始/结束事件节点（圆形样式） */
const isEventNode = (type) => type === 'startEvent' || type === 'endEvent'

/** 获取 AI 工作流集成节点所选工作流名称，用于节点描述展示 */
const getWorkflowName = (node) => {
  if (!node.config || !node.config.workflowId) return ''
  const workflow = props.workflowList.find(item => item.id === node.config.workflowId)
  return workflow ? workflow.name : ''
}

// 审批人类型选项
const assigneeTypeOptions = [
  { label: '流程发起人', value: '1' },
  { label: '系统自动通过', value: '2' },
  { label: '指定用户', value: '3' },
  { label: '指定部门', value: '4' },
  { label: '指定角色', value: '5' },
  { label: '表单数据', value: '6' }
]

// ====== 节点/连线辅助函数 ======

const getNodeIcon = (type) => {
  const iconName = nodeConfig[type]?.icon || 'question'
  return iconName.charAt(0).toUpperCase() + iconName.slice(1)
}

const getNodeClass = (type) => {
  return `bpmn-${type.replace('Gateway', '-gateway')}`
}

const getAssigneeTypeLabel = (type) => {
  const find = assigneeTypeOptions.find(item => item.value === type)
  if (!find) return '未选择审批人'
  return find.label
}

// ====== 连接点计算 ======

const getNodeConnectionPoints = (node) => {
  const x = node.x
  const y = node.y
  const { width, height } = getNodeSize(node)

  return {
    left: { x: x, y: y + height / 2 },
    right: { x: x + width, y: y + height / 2 },
    top: { x: x + width / 2, y: y },
    bottom: { x: x + width / 2, y: y + height }
  }
}

const getAnchorPoints = (source, target) => {
  const sSize = getNodeSize(source)
  const tSize = getNodeSize(target)
  const sWidth = sSize.width
  const sHeight = sSize.height
  const tWidth = tSize.width
  const tHeight = tSize.height

  const sPoints = getNodeConnectionPoints(source)
  const tPoints = getNodeConnectionPoints(target)

  const verticalClearance = target.y - (source.y + sHeight)
  const revVerticalClearance = source.y - (target.y + tHeight)
  const horizontalClearance = target.x - (source.x + sWidth)
  const revHorizontalClearance = source.x - (target.x + tWidth)

  let sourcePoint, targetPoint

  if (verticalClearance > 20) {
    sourcePoint = { ...sPoints.bottom, dir: 'bottom' }
    targetPoint = { ...tPoints.top, dir: 'top' }
  } else if (revVerticalClearance > 20) {
    sourcePoint = { ...sPoints.top, dir: 'top' }
    targetPoint = { ...tPoints.bottom, dir: 'bottom' }
  } else if (horizontalClearance > 20) {
    sourcePoint = { ...sPoints.right, dir: 'right' }
    targetPoint = { ...tPoints.left, dir: 'left' }
  } else if (revHorizontalClearance > 20) {
    sourcePoint = { ...sPoints.left, dir: 'left' }
    targetPoint = { ...tPoints.right, dir: 'right' }
  } else {
    const dx = (target.x + tWidth / 2) - (source.x + sWidth / 2)
    const dy = (target.y + tHeight / 2) - (source.y + sHeight / 2)
    if (Math.abs(dx) > Math.abs(dy)) {
      if (dx > 0) {
        sourcePoint = { ...sPoints.right, dir: 'right' }
        targetPoint = { ...tPoints.left, dir: 'left' }
      } else {
        sourcePoint = { ...sPoints.left, dir: 'left' }
        targetPoint = { ...tPoints.right, dir: 'right' }
      }
    } else {
      if (dy > 0) {
        sourcePoint = { ...sPoints.bottom, dir: 'bottom' }
        targetPoint = { ...tPoints.top, dir: 'top' }
      } else {
        sourcePoint = { ...sPoints.top, dir: 'top' }
        targetPoint = { ...tPoints.bottom, dir: 'bottom' }
      }
    }
  }

  const sp = { ...sourcePoint }
  const tp = { ...targetPoint }

  if (sp.dir === 'top' || sp.dir === 'bottom') {
    if (Math.abs(sp.x - tp.x) < 20) tp.x = sp.x
  } else {
    if (Math.abs(sp.y - tp.y) < 20) tp.y = sp.y
  }

  return { sourcePoint: sp, targetPoint: tp }
}

const getPolylinePath = (sequence) => {
  const source = props.nodes.find(el => el.id === sequence.sourceRef)
  const target = props.nodes.find(el => el.id === sequence.targetRef)
  if (!source || !target) return ''

  const { sourcePoint, targetPoint } = getAnchorPoints(source, target)
  const sp = sourcePoint
  const tp = targetPoint

  if (sp.x === tp.x || sp.y === tp.y) return `M ${sp.x} ${sp.y} L ${tp.x} ${tp.y}`

  const offset = 20
  if (sp.dir === 'left' || sp.dir === 'right') {
    let midX = (sp.x + tp.x) / 2
    if (sp.dir === 'right') midX = Math.max(midX, sp.x + offset)
    else midX = Math.min(midX, sp.x - offset)
    if (tp.dir === 'right') midX = Math.max(midX, tp.x + offset)
    else if (tp.dir === 'left') midX = Math.min(midX, tp.x - offset)
    return `M ${sp.x} ${sp.y} L ${midX} ${sp.y} L ${midX} ${tp.y} L ${tp.x} ${tp.y}`
  } else {
    let midY = (sp.y + tp.y) / 2
    if (sp.dir === 'bottom') midY = Math.max(midY, sp.y + offset)
    else midY = Math.min(midY, sp.y - offset)
    if (tp.dir === 'bottom') midY = Math.max(midY, tp.y + offset)
    else if (tp.dir === 'top') midY = Math.min(midY, tp.y - offset)
    return `M ${sp.x} ${sp.y} L ${sp.x} ${midY} L ${tp.x} ${midY} L ${tp.x} ${tp.y}`
  }
}

const getDraggingPolylinePath = () => {
  if (!draggingSequence.value) return ''
  const { startX, startY, endX, endY, dir } = draggingSequence.value
  if (dir === 'left' || dir === 'right') {
    const midX = (startX + endX) / 2
    return `M ${startX} ${startY} L ${midX} ${startY} L ${midX} ${endY} L ${endX} ${endY}`
  } else {
    const midY = (startY + endY) / 2
    return `M ${startX} ${startY} L ${startX} ${midY} L ${endX} ${midY} L ${endX} ${endY}`
  }
}

const getSequenceMidPoint = (sequence) => {
  const source = props.nodes.find(el => el.id === sequence.sourceRef)
  const target = props.nodes.find(el => el.id === sequence.targetRef)
  if (source && target) {
    const { sourcePoint, targetPoint } = getAnchorPoints(source, target)
    return { x: (sourcePoint.x + targetPoint.x) / 2, y: (sourcePoint.y + targetPoint.y) / 2 }
  }
  return { x: 0, y: 0 }
}

const getSequenceTextPosition = (sequence) => {
  const mid = getSequenceMidPoint(sequence)
  return { x: mid.x, y: mid.y - 10 }
}

function handleSelectNode(node) {
  emit('selectNode', node)
}

function handleSelectSequence(sequence) {
  emit('selectSequence', sequence)
}

function handleDeleteNode(id) {
  emit('deleteNode', id)
}

function handleDeleteSequence(id) {
  emit('deleteSequence', id)
}

function handleStartDragNode(event, node) {
  if (sourceNode.value) return
  const startX = event.clientX
  const startY = event.clientY
  const nodeX = node.x
  const nodeY = node.y
  const onMouseMove = (moveEvent) => {
    node.x = nodeX + (moveEvent.clientX - startX)
    node.y = nodeY + (moveEvent.clientY - startY)
  }
  const onMouseUp = () => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

function startSequence(event, node, position) {
  event.stopPropagation()
  sourceNode.value = node
  const rect = canvasRef.value.getBoundingClientRect()
  const points = getNodeConnectionPoints(node)
  const startPoint = points[position] || points.right

  draggingSequence.value = {
    startX: startPoint.x,
    startY: startPoint.y,
    endX: event.clientX - rect.left,
    endY: event.clientY - rect.top,
    dir: position
  }

  const onMouseMove = (moveEvent) => {
    const rect = canvasRef.value.getBoundingClientRect()
    draggingSequence.value.endX = moveEvent.clientX - rect.left
    draggingSequence.value.endY = moveEvent.clientY - rect.top
  }

  const onMouseUp = (upEvent) => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
    const targetNode = findNodeAtPosition(upEvent.clientX, upEvent.clientY)
    if (targetNode && targetNode.id !== sourceNode.value.id) {
      emit('startSequence', { sourceId: sourceNode.value.id, targetId: targetNode.id })
    } else if (targetNode && targetNode.id === sourceNode.value.id) {
      ElMessage.warning('不能连接到自己')
    }
    draggingSequence.value = null
    sourceNode.value = null
  }
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

function findNodeAtPosition(clientX, clientY) {
  const rect = canvasRef.value.getBoundingClientRect()
  const x = clientX - rect.left
  const y = clientY - rect.top
  for (let i = props.nodes.length - 1; i >= 0; i--) {
    const node = props.nodes[i]
    const { width, height } = getNodeSize(node)
    if (x >= node.x && x <= node.x + width && y >= node.y && y <= node.y + height) {
      return node
    }
  }
  return null
}

function onDrop(event) {
  if (!props.readonly) {
    const nodeType = event.dataTransfer.getData('node-type')
    if (nodeType) {
      const rect = canvasRef.value.getBoundingClientRect()
      emit('drop', { nodeType, x: event.clientX - rect.left, y: event.clientY - rect.top })
    }
  }
}

function onDragover(event) {
  if (!props.readonly) event.preventDefault()
}

// ====== 控制按钮显示/隐藏 ======

function showNodeControls(node) {
  node.showControls = true
  if (elementHideTimers.value[node.id]) {
    clearTimeout(elementHideTimers.value[node.id])
    delete elementHideTimers.value[node.id]
  }
}

function delayHideNodeControls(node) {
  if (elementHideTimers.value[node.id]) clearTimeout(elementHideTimers.value[node.id])
  elementHideTimers.value[node.id] = setTimeout(() => {
    node.showControls = false
    delete elementHideTimers.value[node.id]
  }, 200)
}

function keepNodeControls(node) {
  if (elementHideTimers.value[node.id]) {
    clearTimeout(elementHideTimers.value[node.id])
    delete elementHideTimers.value[node.id]
  }
}

function showSequenceControls(sequence) {
  sequence.showControls = true
  if (sequenceHideTimers.value[sequence.id]) {
    clearTimeout(sequenceHideTimers.value[sequence.id])
    delete sequenceHideTimers.value[sequence.id]
  }
}

function delayHideSequenceControls(sequence) {
  if (sequenceHideTimers.value[sequence.id]) clearTimeout(sequenceHideTimers.value[sequence.id])
  sequenceHideTimers.value[sequence.id] = setTimeout(() => {
    sequence.showControls = false
    delete sequenceHideTimers.value[sequence.id]
  }, 200)
}

function keepSequenceControls(sequence) {
  if (sequenceHideTimers.value[sequence.id]) {
    clearTimeout(sequenceHideTimers.value[sequence.id])
    delete sequenceHideTimers.value[sequence.id]
  }
}
</script>

<style scoped lang="scss">
.canvas {
  flex: 1;
  position: relative;
  min-height: 500px;
  background-color: #fff;
  overflow: auto;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}

.canvas:hover {
  scrollbar-width: thin;
  -ms-overflow-style: auto;
}

.bpmn-node {
  position: absolute;
  z-index: 30;
  border-radius: 8px;
  background: #ffffff;
  cursor: move;
  user-select: none;
  text-align: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
  color: #333;
  border: 1.5px solid #b3d8ff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  transition: $transition-fast;

  &:hover {
    z-index: 40;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  }
}

.bpmn-startEvent,
.bpmn-endEvent {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border-width: 2px;
}

.bpmn-startEvent {
  border-color: #52c41a;
  background: #f6ffed;
}

.bpmn-endEvent {
  border-color: #f5222d;
  background: #fff1f0;
}

.bpmn-userTask {
  width: 128px;
  height: 72px;
  border-color: #1890ff;
  background: #e6f7ff;
}

.bpmn-exclusive-gateway {
  width: 108px;
  height: 72px;
  border-color: #fa8c16;
  background: #fffbe6;
}

.bpmn-node.selected-node {
  border-width: 2px;
}

.bpmn-startEvent.selected-node {
  border-color: #52c41a;
  box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.25), 0 6px 16px rgba(82, 196, 26, 0.2);
}
.bpmn-endEvent.selected-node {
  border-color: #f5222d;
  box-shadow: 0 0 0 2px rgba(245, 34, 45, 0.25), 0 6px 16px rgba(245, 34, 45, 0.2);
}
.bpmn-userTask.selected-node {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.25), 0 6px 16px rgba(24, 144, 255, 0.2);
}
.bpmn-exclusive-gateway.selected-node {
  border-color: #fa8c16;
  box-shadow: 0 0 0 2px rgba(250, 140, 22, 0.25), 0 6px 16px rgba(250, 140, 22, 0.2);
}

.node-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  width: 100%;
  height: 100%;
}

.node-main-icon {
  font-size: 20px;
  color: #606266;
}

.bpmn-startEvent .node-main-icon { color: #52c41a; }
.bpmn-endEvent .node-main-icon { color: #f5222d; }
.bpmn-userTask .node-main-icon { color: #1890ff; }
.bpmn-serviceTask .node-main-icon { color: #722ed1; }

.gateway-icon {
  font-size: 20px;
  color: #fa8c16;
}

.node-name {
  font-size: 13px;
  color: $color-text-primary;
  font-weight: 550;
  line-height: 1.2;
  word-break: break-all;
  max-width: 116px;
}

.gateway-name {
  font-size: 13px;
  max-width: 96px;
  font-weight: 550;
  color: $color-text-primary;
  line-height: 1.2;
  word-break: break-all;
}

.node-info {
  margin-top: 1px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.assignee-type,
.ai-tag {
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
}

.assignee-type {
  background: #eaf3ff;
  color: #1890ff;
}

.ai-tag {
  background: #f3e8ff;
  color: #722ed1;
}

.node-controls {
  position: absolute;
  top: -30px;
  right: 0;
  z-index: 1000;
  display: flex;
  gap: 4px;
}

.sequence-point {
  position: absolute;
  width: 10px;
  height: 10px;
  background: #fff;
  border: 1.5px solid #1890ff;
  border-radius: 50%;
  cursor: crosshair;
  z-index: 100;
  opacity: 0;
  transition: opacity 0.2s;
  box-shadow: 0 0 4px rgba(24, 144, 255, 0.4);
}

.sequence-point:hover { opacity: 1; }

.bpmn-node:hover .sequence-point { opacity: 0.9; }

.left-point   { top: 50%; left: -5px; transform: translateY(-50%); }
.right-point  { top: 50%; right: -5px; transform: translateY(-50%); }
.top-point    { top: -5px; left: 50%; transform: translateX(-50%); }
.bottom-point { bottom: -5px; left: 50%; transform: translateX(-50%); }

.sequences-svg {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 20;
}

.sequences-svg path {
  pointer-events: visibleStroke;
}

.selected-sequence {
  stroke: #1890ff !important;
  stroke-width: 3 !important;
}

.sequence-controls {
  position: absolute;
  z-index: 1001;
  display: flex;
  gap: 5px;
}

.sequence-hit-area {
  cursor: pointer;
}

.sequence-visible-line:hover {
  stroke: #1890ff !important;
  stroke-width: 3;
  cursor: pointer;
}

.sequence-label {
  pointer-events: all;
  cursor: pointer;
  font-size: 11px;
  font-weight: 500;
}

.sequence-label:hover {
  fill: #1890ff;
}
</style>

<style lang="scss">
/* 非 scoped 样式，确保 ::-webkit-scrollbar 伪元素生效 */
.flow-canvas-scrollbar {
  overflow: auto;
}

/* Webkit 浏览器默认隐藏滚动条 */
.flow-canvas-scrollbar::-webkit-scrollbar {
  width: 0;
  height: 0;
}

/* 鼠标悬停时显示滚动条 */
.flow-canvas-scrollbar:hover::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.flow-canvas-scrollbar::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.flow-canvas-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
</style>
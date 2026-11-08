<template>
  <div class="canvas-wrap">
    <div
        class="designer-canvas"
        ref="canvasRef"
        @drop="handleDrop"
        @dragover="handleDragOver"
        @mousedown="handleCanvasMouseDown"
        @dblclick="handleCanvasDblClick"
        @wheel.ctrl.prevent="handleWheelZoom"
    >
      <div class="canvas-sizer" :style="{ width: sizerW + 'px', height: sizerH + 'px' }">
        <div
            class="canvas-content"
            :style="{ width: canvasW + 'px', height: canvasH + 'px', transform: 'scale(' + scale + ')' }"
        >
          <!-- 节点 -->
          <div
              v-for="node in nodes"
              :key="node.id"
              class="cn-node"
              :class="{ 'cn-node--selected': selectedNodeId === node.id }"
              :style="nodeStyle(node)"
              @mousedown.stop="handleNodeMouseDown($event, node)"
              @click.stop="handleNodeClick(node, $event)"
              @dblclick.stop="handleNodeClick(node, $event)"
          >
            <node-card :node="node" :selected="selectedNodeId === node.id" />

            <!-- 左右连接点：右侧悬停变 ＋，可拖拽连线 -->
            <span v-if="node.type !== 'startEvent'" class="cn-port cn-port--left"></span>
            <span
                v-if="node.type !== 'endEvent' && node.type !== 'placeholder'"
                class="cn-port cn-port--right"
                :class="{ 'is-plus': true, 'is-always': !hasOutgoing(node.id) }"
                title="点击插入节点，拖拽连接下游"
                @mousedown.stop="startConnect(node, $event)"
                @click.stop
            ><el-icon :size="10"><Plus /></el-icon></span>
          </div>

          <!-- 连线 SVG -->
          <svg class="cn-lines" :width="canvasW" :height="canvasH">
            <defs>
              <marker
                  :id="markerId"
                  markerWidth="8"
                  markerHeight="8"
                  refX="6.5"
                  refY="3"
                  orient="auto"
                  markerUnits="userSpaceOnUse"
              >
                <path d="M0 0 L6 3 L0 6 z" class="cn-arrow" />
              </marker>
              <marker
                  :id="markerId + '-active'"
                  markerWidth="8"
                  markerHeight="8"
                  refX="6.5"
                  refY="3"
                  orient="auto"
                  markerUnits="userSpaceOnUse"
              >
                <path d="M0 0 L6 3 L0 6 z" class="cn-arrow-active" />
              </marker>
            </defs>

            <g
                v-for="edge in sequences"
                :key="edge.id"
                @mousedown.stop
                @click.stop="handleFocusEdge(edge)"
                @dblclick.stop="handleDeleteEdge(edge)"
                @mouseenter="hoverEdgeId = edge.id"
                @mouseleave="hoverEdgeId = null"
            >
              <path
                  :d="getEdgePath(edge)"
                  class="cn-line-hit"
              />
              <path
                  :d="getEdgePath(edge)"
                  class="cn-line"
                  :class="{ 'cn-line--selected': selectedEdgeId === edge.id }"
                  :marker-end="'url(#' + markerId + (selectedEdgeId === edge.id ? '-active' : '') + ')'"
              />
              <!-- 连线中点 ＋：选中后才浮出（避免挡住首次点击选中），分支线上已有分支标签，不再叠按钮 -->
              <g
                  v-if="selectedEdgeId === edge.id && !hasChip(edge)"
                  class="cn-edge-plus"
                  @click.stop="openPickerOnEdge(edge, $event)"
              >
                <circle :cx="edgeMidpoint(edge).x" :cy="edgeMidpoint(edge).y" r="11" />
                <text :x="edgeMidpoint(edge).x" :y="edgeMidpoint(edge).y + 4">＋</text>
              </g>
            </g>

            <!-- 分支分叉圆点 -->
            <circle
                v-for="fork in gatewayForks"
                :key="'fork-' + fork.id"
                :cx="fork.x"
                :cy="fork.y"
                r="3.5"
                class="cn-fork-dot"
            />

            <!-- 连线名称标签：分支名称已并入条件标签，这里只标普通连线 -->
            <template v-for="edge in sequences" :key="'text-' + edge.id">
              <text
                  v-if="edge.name && !hasChip(edge)"
                  :x="edgeMidpoint(edge).x"
                  :y="edgeMidpoint(edge).y - 10"
                  class="cn-line-label"
                  @mousedown.stop
                  @click.stop="handleSelectEdge(edge)"
              >{{ edge.name }}</text>
            </template>

            <!-- 分支条件标签：多条件逐行完整展示；点击进入分支配置 -->
            <template v-for="chip in conditionChips" :key="'chip-' + chip.edgeId">
              <g
                  class="cn-chip"
                  :class="{ 'cn-chip--multi': chip.multi, 'cn-chip--blank': chip.blank }"
                  @mousedown.stop
                  @click.stop="handleSelectEdge(chip.edge)"
              >
                <rect
                    :x="chip.x - chip.width / 2"
                    :y="chip.y"
                    :width="chip.width"
                    :height="chip.height"
                    :rx="chip.radius"
                />
                <text
                    v-for="(line, i) in chip.lines"
                    :key="i"
                    :x="chip.textX"
                    :y="chip.y + line.y"
                    :class="{ 'is-name': line.isName }"
                >{{ line.text }}</text>
              </g>
            </template>

            <!-- 临时连线（拖拽中） -->
            <path
                v-if="connecting"
                :d="getConnectingPath()"
                class="cn-line-temp"
                fill="none"
            />
          </svg>
        </div>
      </div>
    </div>

    <!-- 浮层：空状态提示 -->
    <div v-if="!nodes.length" class="canvas-empty">
      <p>双击画布放置第一个节点</p>
      <span>从「开始」节点开始，向右编排流程</span>
    </div>

    <!-- 浮层：缩放条 -->
    <div class="canvas-zoombar" @mousedown.stop>
      <span class="zoom-btn" @click="zoomOut"><el-icon><Minus /></el-icon></span>
      <span class="zoom-value" @click="resetZoom">{{ Math.round(scale * 100) }}%</span>
      <span class="zoom-btn" @click="zoomIn"><el-icon><Plus /></el-icon></span>
    </div>

    <!-- 节点选择器 -->
    <node-picker
        :visible="!!picker"
        :position="picker ? picker.screen : { x: 0, y: 0 }"
        @pick="handlePickNode"
        @close="picker = null"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import NodeCard from '../nodes/NodeCard.vue'
import NodePicker from './NodePicker.vue'
import { NODE_SIZE } from '../nodes/index.js'
import {
  bezierPath,
  buildConditionChips,
  buildGatewayForks,
  edgeMidpoint as geoEdgeMidpoint,
  getEdgePath as geoGetEdgePath
} from '../utils/flowGeometry.js'

defineOptions({ name: 'WorkflowCanvas' })

const props = defineProps({
  nodes: { type: Array, default: () => [] },
  sequences: { type: Array, default: () => [] },
  selectedNodeId: { type: String, default: null },
  selectedEdgeId: { type: String, default: null },
  canvasWidth: { type: Number, default: 2000 },
  canvasHeight: { type: Number, default: 1200 },
  /** 表单字段选项（label/value），用于连线条件标签的字段编码翻译 */
  fieldOptions: { type: Array, default: () => [] },
  /** 需要整体滚进可视区的节点 id（创建分支后用来露出 分支→占位→汇合点 全貌） */
  revealIds: { type: Array, default: () => [] }
})

const emit = defineEmits([
  'select-node', 'select-edge', 'focus-edge',
  'move-node', 'connect', 'delete-edge', 'drop-node',
  'insert-after', 'insert-on-edge', 'place-node', 'replace-node'
])

const canvasRef = ref(null)
const dragging = ref(null)
const connecting = ref(null)
const hoverEdgeId = ref(null)
/** 节点选择器状态：{ mode: after|edge|free, sourceId, edgeId, screen } */
const picker = ref(null)

const containerWidth = ref(0)
const containerHeight = ref(0)
const scale = ref(1)

const canvasW = computed(() => Math.max(props.canvasWidth, containerWidth.value / scale.value))
const canvasH = computed(() => Math.max(props.canvasHeight, containerHeight.value / scale.value))
const sizerW = computed(() => canvasW.value * scale.value)
const sizerH = computed(() => canvasH.value * scale.value)

let resizeObserver = null

onMounted(() => {
  const el = canvasRef.value
  if (!el) return
  const updateSize = () => {
    containerWidth.value = el.clientWidth
    containerHeight.value = el.clientHeight
  }
  updateSize()
  resizeObserver = new ResizeObserver(updateSize)
  resizeObserver.observe(el)
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
})

const markerId = 'wfc-arrow-' + Math.random().toString(36).slice(2, 8)

function getCanvasPoint(event) {
  const rect = canvasRef.value.getBoundingClientRect()
  return {
    x: (event.clientX - rect.left + canvasRef.value.scrollLeft) / scale.value,
    y: (event.clientY - rect.top + canvasRef.value.scrollTop) / scale.value
  }
}

/** 以鼠标点为参考做偏移，让浮层贴着触发点而不是盖住它 */
function offsetPoint(event, dx, dy) {
  return { x: event.clientX + dx, y: event.clientY + dy }
}

/**
 * 选中节点后把它滚进可视区
 * 右侧属性面板浮层宽 340，留 380 的内缩，避免新插入的节点被面板盖住
 */
const PANEL_INSET = 380

watch(() => props.selectedNodeId, (id) => {
  if (!id) return
  nextTick(() => revealNode(id))
})

/** 一组节点整体滚进可视区：能放下就居中，放不下就靠左对齐 */
watch(() => props.revealIds, (ids) => {
  if (!ids || !ids.length) return
  nextTick(() => revealNodes(ids))
})

function revealNodes(ids) {
  const el = canvasRef.value
  if (!el) return
  const boxes = ids.map(id => props.nodes.find(n => n.id === id)).filter(Boolean)
  if (!boxes.length) return
  const inset = (props.selectedNodeId || props.selectedEdgeId) ? PANEL_INSET : 0
  const usableW = Math.max(240, el.clientWidth - inset)
  const usableH = el.clientHeight
  const left = Math.min(...boxes.map(n => n.x)) * scale.value
  const right = Math.max(...boxes.map(n => n.x + NODE_SIZE.width)) * scale.value
  const top = Math.min(...boxes.map(n => n.y)) * scale.value
  const bottom = Math.max(...boxes.map(n => n.y + NODE_SIZE.height)) * scale.value
  el.scrollLeft = right - left <= usableW
    ? Math.max(0, (left + right) / 2 - usableW / 2)
    : Math.max(0, left - 40)
  el.scrollTop = bottom - top <= usableH
    ? Math.max(0, (top + bottom) / 2 - usableH / 2)
    : Math.max(0, top - 40)
}

function revealNode(id) {
  const el = canvasRef.value
  const node = props.nodes.find(n => n.id === id)
  if (!el || !node) return
  const x = node.x * scale.value
  const y = node.y * scale.value
  const w = NODE_SIZE.width * scale.value
  const h = NODE_SIZE.height * scale.value
  const usableW = Math.max(240, el.clientWidth - PANEL_INSET)
  // 分支节点：连带它右侧的分支节点一起滚进可视区，避免分支占位被属性面板盖住
  const right = props.sequences
      .filter(e => e.source === id)
      .reduce((max, e) => {
        const child = props.nodes.find(n => n.id === e.target)
        return child ? Math.max(max, (child.x + NODE_SIZE.width) * scale.value) : max
      }, x + w)
  if (x < el.scrollLeft) {
    el.scrollLeft = Math.max(0, x - 40)
  } else if (right > el.scrollLeft + usableW) {
    el.scrollLeft = right - usableW + 40
  }
  if (y < el.scrollTop) {
    el.scrollTop = Math.max(0, y - 40)
  } else if (y + h > el.scrollTop + el.clientHeight) {
    el.scrollTop = y + h - el.clientHeight + 40
  }
}

/** 是否已有下游连线：没有时出端口常显，方便直接接着画 */
function hasOutgoing(nodeId) {
  return props.sequences.some(edge => edge.source === nodeId)
}

function nodeStyle(node) {
  return {
    left: node.x + 'px',
    top: node.y + 'px',
    width: NODE_SIZE.width + 'px',
    height: NODE_SIZE.height + 'px'
  }
}

function handleSelectNode(node) {
  emit('select-node', node)
}

/**
 * 节点点击：占位节点直接弹出节点选择器（原地替换），其余节点打开属性面板
 */
function handleNodeClick(node, event) {
  if (node.type === 'placeholder') {
    picker.value = {
      mode: 'replace',
      nodeId: node.id,
      screen: offsetPoint(event, 16, -28)
    }
    return
  }
  handleSelectNode(node)
}

function handleSelectEdge(edge) {
  emit('select-edge', edge)
}

/** 点线本身只做高亮，不弹面板；面板从「分支条件」标签进入 */
function handleFocusEdge(edge) {
  emit('focus-edge', edge)
}

function handleDeleteEdge(edge) {
  emit('delete-edge', edge.id)
}

// 连线几何与分支标签排版复用 utils/flowGeometry.js，编辑画布与只读预览口径一致
const getEdgePath = (edge) => geoGetEdgePath(props.nodes, edge)
const edgeMidpoint = (edge) => geoEdgeMidpoint(props.nodes, edge)

/** 分叉圆点：同一节点存在多条出线时在出端口画点 */
const gatewayForks = computed(() => buildGatewayForks(props.nodes, props.sequences))

/** 各连线出线的分支条件：多条件逐行完整展示；分支未配条件时给出可点击的空态入口 */
const conditionChips = computed(() => buildConditionChips({
  nodes: props.nodes,
  sequences: props.sequences,
  fieldOptions: props.fieldOptions
}))
/** 该连线是否已有分支标签（配了条件，或是待配置的分支），有标签就不在中点叠 ＋ */
function hasChip(edge) {
  return conditionChips.value.some(chip => chip.edgeId === edge.id)
}

function getConnectingPath() {
  if (!connecting.value) return ''
  const { from, to } = connecting.value
  return bezierPath(from, to)
}

// 节点拖拽

function handleNodeMouseDown(event, node) {
  if (event.button !== 0) return
  const pt = getCanvasPoint(event)
  dragging.value = {
    nodeId: node.id,
    offsetX: pt.x - node.x,
    offsetY: pt.y - node.y
  }
  document.addEventListener('mousemove', onNodeMouseMove)
  document.addEventListener('mouseup', onNodeMouseUp)
}

function onNodeMouseMove(event) {
  if (!dragging.value) return
  const pt = getCanvasPoint(event)
  emit('move-node', dragging.value.nodeId, pt.x - dragging.value.offsetX, pt.y - dragging.value.offsetY)
}

function onNodeMouseUp() {
  document.removeEventListener('mousemove', onNodeMouseMove)
  document.removeEventListener('mouseup', onNodeMouseUp)
  dragging.value = null
}

// 连线：从右端口拖拽，单击则弹出选择器插入下游节点

const CONNECT_CLICK_TOLERANCE = 5

function startConnect(node, event) {
  if (event.button !== 0) return
  const anchor = { x: node.x + NODE_SIZE.width, y: node.y + NODE_SIZE.height / 2 }
  connecting.value = {
    sourceId: node.id,
    from: anchor,
    to: { ...anchor },
    startClient: { x: event.clientX, y: event.clientY },
    moved: false
  }
  document.addEventListener('mousemove', onConnectMouseMove)
  document.addEventListener('mouseup', onConnectMouseUp)
}

function onConnectMouseMove(event) {
  if (!connecting.value) return
  const pt = getCanvasPoint(event)
  connecting.value.to = { x: pt.x, y: pt.y }
  if (Math.hypot(event.clientX - connecting.value.startClient.x, event.clientY - connecting.value.startClient.y) > CONNECT_CLICK_TOLERANCE) {
    connecting.value.moved = true
  }
}

function onConnectMouseUp(event) {
  document.removeEventListener('mousemove', onConnectMouseMove)
  document.removeEventListener('mouseup', onConnectMouseUp)
  const state = connecting.value
  connecting.value = null
  if (!state) return
  if (!state.moved) {
    openPickerAfter(state.sourceId, event)
    return
  }
  const pt = getCanvasPoint(event)
  const target = findNodeAtPosition(pt)
  if (target && target.id !== state.sourceId) {
    emit('connect', state.sourceId, target.id)
  }
}

function findNodeAtPosition(pt) {
  for (let i = props.nodes.length - 1; i >= 0; i--) {
    const node = props.nodes[i]
    if (pt.x >= node.x && pt.x <= node.x + NODE_SIZE.width && pt.y >= node.y && pt.y <= node.y + NODE_SIZE.height) {
      return node
    }
  }
  return null
}

// 节点选择器三个入口

function openPickerAfter(sourceId, event) {
  picker.value = {
    mode: 'after',
    sourceId,
    screen: offsetPoint(event, 16, -28)
  }
}

function openPickerOnEdge(edge, event) {
  picker.value = {
    mode: 'edge',
    edgeId: edge.id,
    screen: offsetPoint(event, 18, -20)
  }
}

function handleCanvasDblClick(event) {
  if (event.target.closest('.cn-node')) return
  const pt = getCanvasPoint(event)
  picker.value = {
    mode: 'free',
    content: { x: Math.round(pt.x), y: Math.round(pt.y) },
    screen: offsetPoint(event, 8, 8)
  }
}

function handlePickNode(type) {
  const state = picker.value
  picker.value = null
  if (!state) return
  if (state.mode === 'after') emit('insert-after', state.sourceId, type)
  else if (state.mode === 'edge') emit('insert-on-edge', state.edgeId, type)
  else if (state.mode === 'replace') emit('replace-node', state.nodeId, type)
  else emit('place-node', type, state.content.x, state.content.y)
}

// 画布空白点击：取消选中并收起菜单

function handleCanvasMouseDown(event) {
  if (event.target.closest('.cn-node') || event.target.closest('.cn-lines path') || event.target.closest('.cn-chip')) return
  emit('select-node', null)
  emit('select-edge', null)
}

// 拖放新节点

function handleDragOver(event) {
  event.preventDefault()
}

function handleDrop(event) {
  const type = event.dataTransfer.getData('nodeType')
  if (!type) return
  const pt = getCanvasPoint(event)
  emit('drop-node', type, Math.round(pt.x), Math.round(pt.y))
}

// 缩放与小地图

function applyZoom(next, anchorX, anchorY) {
  const clamped = Math.min(2, Math.max(0.4, Math.round(next * 100) / 100))
  const el = canvasRef.value
  if (el && anchorX !== undefined) {
    const contentX = (el.scrollLeft + anchorX) / scale.value
    const contentY = (el.scrollTop + anchorY) / scale.value
    scale.value = clamped
    el.scrollLeft = contentX * clamped - anchorX
    el.scrollTop = contentY * clamped - anchorY
  } else {
    scale.value = clamped
  }
}

function handleWheelZoom(event) {
  const rect = canvasRef.value.getBoundingClientRect()
  const factor = event.deltaY < 0 ? 1.1 : 1 / 1.1
  applyZoom(scale.value * factor, event.clientX - rect.left, event.clientY - rect.top)
}

function zoomIn() {
  applyZoom(scale.value + 0.1)
}

function zoomOut() {
  applyZoom(scale.value - 0.1)
}

function resetZoom() {
  applyZoom(1)
}
</script>

<style scoped lang="scss">
@use '../styles/vars.scss' as *;
.canvas-wrap {
  position: relative;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.designer-canvas {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: auto;
  background-color: $wf-canvas-bg;
  background-image:
    radial-gradient(circle, $wf-canvas-dot 1.2px, transparent 1.8px),
    radial-gradient(circle, $wf-canvas-dot-soft 1px, transparent 1.6px);
  background-size: 22px 22px, 22px 22px;
  background-position: 0 0, 11px 11px;
  scrollbar-width: thin;

  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: $app-scrollbar-thumb;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }
}

.canvas-sizer {
  position: relative;
  overflow: hidden;
}

.canvas-content {
  position: relative;
  transform-origin: 0 0;
}

.cn-node {
  position: absolute;
  box-sizing: border-box;
  cursor: move;
  user-select: none;
  z-index: 30;

  &:hover {
    z-index: 40;
  }
}

.cn-port {
  position: absolute;
  top: 50%;
  width: 9px;
  height: 9px;
  background: $app-bg-card;
  border: 1.5px solid $wf-line;
  border-radius: 50%;
  transform: translateY(-50%);
  z-index: 6;
}

.cn-port--left {
  left: -5px;
}

.cn-port--right {
  right: -5px;
  display: none;
  width: 16px;
  height: 16px;
  align-items: center;
  justify-content: center;
  background: $wf-line-active;
  border: none;
  color: $wf-cat-active-text;
  cursor: crosshair;

  &.is-always,
  .cn-node:hover & {
    display: flex;
  }

  &:hover {
    box-shadow: 0 0 0 4px $wf-code-mark;
  }
}

.cn-lines {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 20;

  g {
    pointer-events: visibleStroke;
  }
}

.cn-line-hit {
  stroke: transparent;
  stroke-width: 10;
  fill: none;
  cursor: pointer;
}

.cn-line {
  stroke: $wf-line;
  stroke-width: 1.5;
  fill: none;
  stroke-linecap: round;

  g:hover & {
    stroke: $wf-line-active;
  }
}

.cn-line--selected {
  stroke: $wf-line-active;
  stroke-width: 2;
}

.cn-arrow {
  fill: $wf-line;
}

.cn-arrow-active {
  fill: $wf-line-active;
}

.cn-line-temp {
  stroke: $wf-line-active;
  stroke-width: 1.5;
  stroke-dasharray: 5, 5;
  pointer-events: none;
}

.cn-edge-plus {
  cursor: pointer;

  circle {
    fill: $wf-line-active;
  }

  text {
    fill: $wf-plus-ico;
    font-size: 13px;
    text-anchor: middle;
    user-select: none;
  }
}

.cn-fork-dot {
  fill: $app-bg-card;
  stroke: $wf-line;
  stroke-width: 1.5;
}

.cn-line-label {
  pointer-events: all;
  cursor: pointer;
  font-size: 11px;
  fill: $app-text-secondary;
  text-anchor: middle;
}

.cn-chip {
  cursor: pointer;
  pointer-events: visiblePainted;

  /* 不透明底压住连线，多行文案才读得清；边框略深，圆角块轮廓明确 */
  rect {
    fill: $app-bg-card;
    stroke: $wf-line;
  }

  text {
    fill: $app-text-secondary;
    font-size: 10.5px;
    text-anchor: middle;
    user-select: none;
  }

  /* 多条件时逐行左对齐，序号与文案成列 */
  &.cn-chip--multi text {
    text-anchor: start;
  }

  /* 分支未配条件：虚线空态入口，弱化处理 */
  &.cn-chip--blank {
    rect {
      fill: $app-bg-card;
      stroke: $wf-line;
      stroke-dasharray: 3 3;
    }

    text {
      fill: $app-text-secondary;
      opacity: 0.85;
    }

    &:hover rect {
      stroke: $wf-line-active;
    }

    &:hover text {
      fill: $wf-line-active;
      opacity: 1;
    }
  }

  &:hover rect {
    stroke: $wf-line-active;
  }

  /* 首行分支名称：加粗加重，与条件行区分（放在最后，压住空态的弱化色） */
  text.is-name {
    fill: $app-text-primary;
    font-weight: 600;
    opacity: 1;
  }
}

.canvas-empty {
  position: absolute;
  left: 50%;
  top: 42%;
  transform: translate(-50%, -50%);
  text-align: center;
  pointer-events: none;
  z-index: 10;

  p {
    margin: 0 0 6px;
    font-size: 15px;
    font-weight: 600;
    color: $app-text-secondary;
  }

  span {
    font-size: 12px;
    color: $app-text-placeholder;
  }
}

.canvas-zoombar {
  position: absolute;
  left: 14px;
  bottom: 14px;
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 3px;
  background: $wf-panel-bg;
  border: 1px solid $wf-panel-border;
  border-radius: 9px;
  box-shadow: 0 4px 16px rgba(31, 45, 74, 0.12);
  z-index: 50;
}

.zoom-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  color: $app-text-secondary;

  &:hover {
    background: $wf-sec-bg;
    color: $wf-line-active;
  }
}

.zoom-value {
  min-width: 44px;
  text-align: center;
  font-size: 12px;
  color: $app-text-secondary;
  cursor: pointer;
  user-select: none;

  &:hover {
    color: $wf-line-active;
  }
}
</style>

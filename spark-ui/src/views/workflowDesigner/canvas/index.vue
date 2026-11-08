<template>
  <div
      class="designer-canvas"
      ref="canvasRef"
      @drop="handleDrop"
      @dragover="handleDragOver"
      @mousedown="handleCanvasMouseDown"
  >
    <div
        class="canvas-content"
        :style="{ width: canvasW + 'px', height: canvasH + 'px' }"
    >
        <!-- 节点（div 定位，自由拖拽布局） -->
        <div
            v-for="node in nodes"
            :key="node.id"
            class="cn-node"
            :class="{ 'cn-node--selected': selectedNodeId === node.id }"
            :style="nodeStyle(node)"
            @mousedown.stop="handleNodeMouseDown($event, node)"
            @click.stop="handleSelectNode(node)"
        >
          <!-- 节点类型组件：每个类型独立组件，自带包裹/颜色/图标/标签等全部样式 -->
          <component
              v-if="getNodeComponent(node.type)"
              :is="getNodeComponent(node.type)"
              :node="node"
              :selected="selectedNodeId === node.id"
          />
          <!-- 未配置类型组件时的兜底渲染 -->
          <div v-else class="cn-node-fallback">
            <el-icon class="cn-node-icon" :size="20" :color="getNodeMeta(node.type).color || '#999'">
              <component :is="getNodeIcon(node.type)" />
            </el-icon>
            <span class="cn-node-name">{{ node.name || getNodeMeta(node.type).name || node.type }}</span>
          </div>

          <!-- 四向连接点：拖到目标节点即建立连线 -->
          <div class="cn-point cn-point--left" @mousedown.stop="startConnect(node, 'left', $event)"></div>
          <div class="cn-point cn-point--right" @mousedown.stop="startConnect(node, 'right', $event)"></div>
          <div class="cn-point cn-point--top" @mousedown.stop="startConnect(node, 'top', $event)"></div>
          <div class="cn-point cn-point--bottom" @mousedown.stop="startConnect(node, 'bottom', $event)"></div>
        </div>

        <!-- 连线 SVG -->
        <svg class="cn-lines" :width="canvasW" :height="canvasH">
          <defs>
            <marker
                :id="markerId"
                markerWidth="9"
                markerHeight="7"
                refX="7.6"
                refY="3.5"
                orient="auto"
                markerUnits="userSpaceOnUse"
            >
              <polygon points="0 0, 8 3.5, 0 7" class="cn-arrow" />
            </marker>
          </defs>

          <path
              v-for="edge in sequences"
              :key="edge.id"
              :d="getEdgePath(edge)"
              class="cn-line"
              :class="{ 'cn-line--selected': selectedEdgeId === edge.id }"
              :marker-end="'url(#' + markerId + ')'"
              @mousedown.stop
              @click.stop="handleSelectEdge(edge)"
              @dblclick.stop="handleDeleteEdge(edge)"
          />

          <!-- 连线名称标签（v-for 放 template 上，避免与 v-if 同元素时优先级问题） -->
          <template v-for="edge in sequences" :key="'text-' + edge.id">
            <text
                v-if="edge.name"
                :x="getEdgeLabelPosition(edge).x"
                :y="getEdgeLabelPosition(edge).y"
                class="cn-line-label"
                @mousedown.stop
                @click.stop="handleSelectEdge(edge)"
            >{{ edge.name }}</text>
          </template>

          <!-- 分支条件标签（条件分支连线，每个条件片段一行展示分支走向原因） -->
          <template v-for="edge in sequences" :key="'cond-' + edge.id">
            <text
                v-if="formatEdgeConditionLines(edge).length"
                :x="getConditionLabelPosition(edge).x"
                :y="getConditionLabelPosition(edge).y"
                class="cn-line-condition"
                @mousedown.stop
                @click.stop="handleSelectEdge(edge)"
            >
              <tspan
                  v-for="(line, idx) in formatEdgeConditionLines(edge)"
                  :key="idx"
                  :x="getConditionLabelPosition(edge).x"
                  :dy="idx === 0 ? 0 : 14"
              >{{ line }}</tspan>
            </text>
          </template>

          <!-- 临时连线（正在拖拽中） -->
          <path
              v-if="connecting"
              :d="getConnectingPath()"
              class="cn-line-temp"
              fill="none"
              :marker-end="'url(#' + markerId + ')'"
              stroke-dasharray="5,5"
          />
        </svg>
      </div>
  </div>
</template>

<script>
// 模块级计数器：保证多实例 marker id 唯一
let canvasUid = 0
</script>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { NODE_COMPONENTS, NODE_META } from '../nodes/index.js'
import { formatConditionLines } from '../utils/flowCondition.js'

defineOptions({ name: 'WorkflowCanvas' })

const props = defineProps({
  nodes: { type: Array, default: () => [] },
  sequences: { type: Array, default: () => [] },
  /** 连线源/目标字段名 */
  sourceKey: { type: String, default: 'source' },
  targetKey: { type: String, default: 'target' },
  selectedNodeId: { type: String, default: null },
  selectedEdgeId: { type: String, default: null },
  canvasWidth: { type: Number, default: 2000 },
  canvasHeight: { type: Number, default: 1000 },
  /** 表单字段选项（label/value），用于连线条件标签的字段编码翻译 */
  fieldOptions: { type: Array, default: () => [] }
})

const emit = defineEmits([
  'select-node', 'select-edge',
  'move-node', 'connect', 'delete-edge', 'drop-node'
])

const canvasRef = ref(null)
// 节点拖拽状态
const dragging = ref(null)
// 连线拖拽状态
const connecting = ref(null)

// 画布可视区尺寸：内容尺寸至少填满可视区，避免出现多余空白滚动区
const containerWidth = ref(0)
const containerHeight = ref(0)

/** 画布内容尺寸 = max(父级指定尺寸, 可视区尺寸) */
const canvasW = computed(() => Math.max(props.canvasWidth, containerWidth.value))
const canvasH = computed(() => Math.max(props.canvasHeight, containerHeight.value))

onMounted(() => {
  const el = canvasRef.value
  if (!el) return
  const updateSize = () => {
    containerWidth.value = el.clientWidth
    containerHeight.value = el.clientHeight
  }
  updateSize()
  const observer = new ResizeObserver(updateSize)
  observer.observe(el)
})

// 箭头 marker id 唯一化，避免多实例冲突
const markerId = 'cn-arrow-' + (++canvasUid)

/** 鼠标事件坐标换算为内容坐标（含滚动偏移） */
function getCanvasPoint(event) {
  const rect = canvasRef.value.getBoundingClientRect()
  return {
    x: event.clientX - rect.left + canvasRef.value.scrollLeft,
    y: event.clientY - rect.top + canvasRef.value.scrollTop
  }
}

/** 节点类型元数据（名称/尺寸/图标/配色），样式由节点类型组件负责 */
function getNodeMeta(type) {
  return NODE_META[type] || {}
}

function getNodeIcon(type) {
  const icon = getNodeMeta(type).icon || 'Circle'
  return icon.charAt(0).toUpperCase() + icon.slice(1)
}

/** 当前节点类型对应的独立组件（未配置则为 null，走兜底渲染） */
function getNodeComponent(type) {
  return NODE_COMPONENTS[type] || null
}

function getNodeSize(node) {
  const meta = getNodeMeta(node.type)
  return {
    width: node.width || meta.width || 160,
    height: node.height || meta.height || 60
  }
}

/** 节点定位与尺寸；视觉样式（背景/边框/选中高亮）由节点类型组件负责 */
function nodeStyle(node) {
  const { width, height } = getNodeSize(node)
  return {
    left: node.x + 'px',
    top: node.y + 'px',
    width: width + 'px',
    height: height + 'px'
  }
}

// 节点选中
function handleSelectNode(node) {
  emit('select-node', node)
}

// 连线计算（锚点 + 折线）

function findNode(id) {
  return props.nodes.find(n => n.id === id) || null
}

function getEdgeSource(edge) { return edge[props.sourceKey] }
function getEdgeTarget(edge) { return edge[props.targetKey] }

function getNodeAnchorPoints(node) {
  const { width, height } = getNodeSize(node)
  return {
    left: { x: node.x, y: node.y + height / 2 },
    right: { x: node.x + width, y: node.y + height / 2 },
    top: { x: node.x + width / 2, y: node.y },
    bottom: { x: node.x + width / 2, y: node.y + height }
  }
}

/** 根据两节点相对位置自动选择连线出/入方向 */
function getAnchorPoints(source, target) {
  const sW = getNodeSize(source).width
  const sH = getNodeSize(source).height
  const tW = getNodeSize(target).width
  const tH = getNodeSize(target).height
  const s = getNodeAnchorPoints(source)
  const t = getNodeAnchorPoints(target)
  const verticalClearance = target.y - (source.y + sH)
  const revVerticalClearance = source.y - (target.y + tH)
  const horizontalClearance = target.x - (source.x + sW)
  const revHorizontalClearance = source.x - (target.x + tW)
  let sourcePoint, targetPoint
  if (verticalClearance > 20) {
    sourcePoint = { ...s.bottom, dir: 'bottom' }; targetPoint = { ...t.top, dir: 'top' }
  } else if (revVerticalClearance > 20) {
    sourcePoint = { ...s.top, dir: 'top' }; targetPoint = { ...t.bottom, dir: 'bottom' }
  } else if (horizontalClearance > 20) {
    sourcePoint = { ...s.right, dir: 'right' }; targetPoint = { ...t.left, dir: 'left' }
  } else if (revHorizontalClearance > 20) {
    sourcePoint = { ...s.left, dir: 'left' }; targetPoint = { ...t.right, dir: 'right' }
  } else {
    const dx = (target.x + tW / 2) - (source.x + sW / 2)
    const dy = (target.y + tH / 2) - (source.y + sH / 2)
    if (Math.abs(dx) > Math.abs(dy)) {
      if (dx > 0) { sourcePoint = { ...s.right, dir: 'right' }; targetPoint = { ...t.left, dir: 'left' } }
      else { sourcePoint = { ...s.left, dir: 'left' }; targetPoint = { ...t.right, dir: 'right' } }
    } else {
      if (dy > 0) { sourcePoint = { ...s.bottom, dir: 'bottom' }; targetPoint = { ...t.top, dir: 'top' } }
      else { sourcePoint = { ...s.top, dir: 'top' }; targetPoint = { ...t.bottom, dir: 'bottom' } }
    }
  }
  // 对齐端点
  if (sourcePoint.dir === 'top' || sourcePoint.dir === 'bottom') {
    if (Math.abs(sourcePoint.x - targetPoint.x) < 20) targetPoint.x = sourcePoint.x
  } else {
    if (Math.abs(sourcePoint.y - targetPoint.y) < 20) targetPoint.y = sourcePoint.y
  }
  return { sourcePoint, targetPoint }
}

/** 折线拐角圆角半径 */
const CORNER_RADIUS = 10

/** 坐标取一位小数，避免路径字符串过长 */
const round1 = (num) => Math.round(num * 10) / 10

/**
 * 折线路径：每个拐角用二次贝塞尔做圆角，半径不超过相邻线段长度的一半
 * @param points 折点序列（首尾为端点）
 * @returns SVG path d
 */
function roundedPolyline(points) {
  if (points.length < 2) return ''
  let d = `M ${round1(points[0].x)} ${round1(points[0].y)}`
  for (let i = 1; i < points.length - 1; i++) {
    const prev = points[i - 1]
    const curr = points[i]
    const next = points[i + 1]
    const inLen = Math.hypot(curr.x - prev.x, curr.y - prev.y)
    const outLen = Math.hypot(next.x - curr.x, next.y - curr.y)
    const radius = Math.min(CORNER_RADIUS, inLen / 2, outLen / 2)
    if (radius < 1) {
      d += ` L ${round1(curr.x)} ${round1(curr.y)}`
      continue
    }
    const from = {
      x: curr.x - ((curr.x - prev.x) / inLen) * radius,
      y: curr.y - ((curr.y - prev.y) / inLen) * radius
    }
    const to = {
      x: curr.x + ((next.x - curr.x) / outLen) * radius,
      y: curr.y + ((next.y - curr.y) / outLen) * radius
    }
    d += ` L ${round1(from.x)} ${round1(from.y)}`
    d += ` Q ${round1(curr.x)} ${round1(curr.y)} ${round1(to.x)} ${round1(to.y)}`
  }
  const last = points[points.length - 1]
  d += ` L ${round1(last.x)} ${round1(last.y)}`
  return d
}

/** 生成连线路径（圆角折线） */
function getEdgePath(edge) {
  const source = findNode(getEdgeSource(edge))
  const target = findNode(getEdgeTarget(edge))
  if (!source || !target) return ''
  const { sourcePoint, targetPoint } = getAnchorPoints(source, target)
  const sp = sourcePoint
  const tp = targetPoint
  if (sp.x === tp.x || sp.y === tp.y) return roundedPolyline([sp, tp])
  const offset = 20
  if (sp.dir === 'left' || sp.dir === 'right') {
    let midX = (sp.x + tp.x) / 2
    if (sp.dir === 'right') midX = Math.max(midX, sp.x + offset)
    else midX = Math.min(midX, sp.x - offset)
    if (tp.dir === 'right') midX = Math.max(midX, tp.x + offset)
    else if (tp.dir === 'left') midX = Math.min(midX, tp.x - offset)
    return roundedPolyline([sp, { x: midX, y: sp.y }, { x: midX, y: tp.y }, tp])
  }
  let midY = (sp.y + tp.y) / 2
  if (sp.dir === 'bottom') midY = Math.max(midY, sp.y + offset)
  else midY = Math.min(midY, sp.y - offset)
  if (tp.dir === 'bottom') midY = Math.max(midY, tp.y + offset)
  else if (tp.dir === 'top') midY = Math.min(midY, tp.y - offset)
  return roundedPolyline([sp, { x: sp.x, y: midY }, { x: tp.x, y: midY }, tp])
}

/** 连线名称标签位置（中点上方） */
function getEdgeLabelPosition(edge) {
  const source = findNode(getEdgeSource(edge))
  const target = findNode(getEdgeTarget(edge))
  if (!source || !target) return { x: 0, y: 0 }
  const { sourcePoint, targetPoint } = getAnchorPoints(source, target)
  return { x: (sourcePoint.x + targetPoint.x) / 2, y: (sourcePoint.y + targetPoint.y) / 2 - 10 }
}

/** 分支条件标签位置（中点下方，无名称标签时上移） */
function getConditionLabelPosition(edge) {
  const pos = getEdgeLabelPosition(edge)
  return { x: pos.x, y: edge.name ? pos.y + 20 : pos.y + 8 }
}

/** 判断连线是否为条件分支连线（源节点为排他网关且配置了走向条件） */
function isConditionEdge(edge) {
  const source = findNode(getEdgeSource(edge))
  if (!source || source.type !== 'exclusiveGateway') return false
  return !!edge.conditionExpression
}

/** 条件表达式转多行可读文案 */
function formatEdgeConditionLines(edge) {
  if (!isConditionEdge(edge)) return []
  return formatConditionLines(edge.conditionExpression, props.fieldOptions)
}

function getConnectingPath() {
  if (!connecting.value) return ''
  const { from, to } = connecting.value
  const midX = (from.x + to.x) / 2
  return `M ${from.x} ${from.y} L ${midX} ${from.y} L ${midX} ${to.y} L ${to.x} ${to.y}`
}

/** 点击连线：交给上层打开连线配置 */
function handleSelectEdge(edge) {
  emit('select-edge', edge)
}

/** 双击删除连线 */
function handleDeleteEdge(edge) {
  emit('delete-edge', edge.id)
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

// 连线创建：从节点四周连接点拖到目标节点
function startConnect(node, dir, event) {
  event.stopPropagation()
  const points = getNodeAnchorPoints(node)
  const start = points[dir]
  connecting.value = {
    sourceId: node.id,
    from: { x: start.x, y: start.y },
    to: { x: start.x, y: start.y }
  }
  document.addEventListener('mousemove', onConnectMouseMove)
  document.addEventListener('mouseup', onConnectMouseUp)
}

function onConnectMouseMove(event) {
  if (!connecting.value) return
  const pt = getCanvasPoint(event)
  connecting.value.to = { x: pt.x, y: pt.y }
}

function onConnectMouseUp(event) {
  document.removeEventListener('mousemove', onConnectMouseMove)
  document.removeEventListener('mouseup', onConnectMouseUp)
  if (connecting.value) {
    const pt = getCanvasPoint(event)
    const target = findNodeAtPosition(pt)
    if (target && target.id !== connecting.value.sourceId) {
      emit('connect', connecting.value.sourceId, target.id)
    }
    connecting.value = null
  }
}

/** 根据内容坐标查找命中的节点 */
function findNodeAtPosition(pt) {
  for (let i = props.nodes.length - 1; i >= 0; i--) {
    const node = props.nodes[i]
    const { width, height } = getNodeSize(node)
    if (pt.x >= node.x && pt.x <= node.x + width && pt.y >= node.y && pt.y <= node.y + height) {
      return node
    }
  }
  return null
}

// 画布空白点击：取消选中
function handleCanvasMouseDown() {
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
</script>

<style scoped lang="scss">
.designer-canvas {
  position: relative;
  overflow: auto;
  flex: 1;
  // 星星点点网格颜色取主题令牌，亮暗主题自动切换
  background-color: $app-canvas-bg;
  background-image:
    radial-gradient(circle, $app-canvas-dot 1.2px, transparent 1.8px),
    radial-gradient(circle, $app-canvas-dot-soft 1px, transparent 1.6px);
  background-size: 22px 22px, 22px 22px;
  background-position: 0 0, 11px 11px;
  scrollbar-width: thin;
  -ms-overflow-style: auto;

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

.canvas-content {
  position: relative;
}

// 节点容器：仅负责定位，视觉样式由节点类型组件或兜底渲染负责
.cn-node {
  position: absolute;
  box-sizing: border-box;
  cursor: move;
  user-select: none;
  z-index: 30;

  &:hover { z-index: 40; }
}

// 未配置类型组件时的兜底节点样式
.cn-node-fallback {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  border-radius: 8px;
  background: $app-bg-card;
  border: 1.5px solid $app-border;
}

.cn-node-icon {
  line-height: 1;
}

.cn-node-name {
  margin-top: 2px;
  font-size: 13px;
  font-weight: 400;
  color: $app-text-primary;
  line-height: 1.2;
  word-break: break-all;
  max-width: 120px;
}

// 四向连接点
.cn-point {
  position: absolute;
  width: 10px;
  height: 10px;
  background: $app-bg-card;
  border: 1.5px solid $el-color-primary;
  border-radius: 50%;
  cursor: crosshair;
  z-index: 100;
  opacity: 0;
  transition: opacity 0.2s;
  box-shadow: 0 0 4px rgba(47, 123, 255, 0.4);

  &:hover { opacity: 0.9; }
}

.cn-node:hover .cn-point { opacity: 0.9; }

.cn-point--left { top: 50%; left: -5px; transform: translateY(-50%); }
.cn-point--right { top: 50%; right: -5px; transform: translateY(-50%); }
.cn-point--top { top: -5px; left: 50%; transform: translateX(-50%); }
.cn-point--bottom { bottom: -5px; left: 50%; transform: translateX(-50%); }

// 连线
.cn-lines {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 20;

  path {
    pointer-events: visibleStroke;
  }
}

.cn-line {
  stroke: $app-text-placeholder;
  stroke-width: 1.5;
  fill: none;
  stroke-linecap: round;
  stroke-linejoin: round;
  cursor: pointer;
}

.cn-line--selected {
  stroke: $el-color-primary !important;
  stroke-width: 2;
}

.cn-arrow {
  fill: $app-text-placeholder;
}

.cn-line-temp {
  stroke: $el-color-primary;
  stroke-width: 1.5;
}

.cn-line-label {
  pointer-events: all;
  cursor: pointer;
  font-size: 11px;
  fill: $app-text-secondary;
  text-anchor: middle;
  font-weight: 400;
}

.cn-line-condition {
  pointer-events: all;
  cursor: pointer;
  font-size: 11px;
  fill: $app-node-gateway;
  text-anchor: middle;
  font-weight: 400;
  // 画布底色描边光晕，保证文字在网格与连线交叉处可读
  paint-order: stroke;
  stroke: $app-canvas-bg;
  stroke-width: 3px;
  stroke-linejoin: round;
}
</style>

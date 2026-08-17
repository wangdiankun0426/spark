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
        <!-- 节点（div 定位） -->
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
              v-bind="nodeComponentProps"
          />
          <!-- 未配置类型组件时的兜底渲染 -->
          <template v-else-if="!$slots.nodeContent">
            <div class="cn-node-fallback">
              <el-icon class="cn-node-icon" :size="20" :color="getNodeMeta(node.type).color || '#999'">
                <component :is="getNodeIcon(node.type)" />
              </el-icon>
              <span class="cn-node-name">{{ node.name || getNodeMeta(node.type).name || node.type }}</span>
              <slot name="nodeExtra" :node="node" />
            </div>
          </template>
          <!-- 自定义节点内容（完整接管渲染，默认不使用） -->
          <slot v-else name="nodeContent" :node="node" />

          <!-- 四向连接点 -->
          <template v-if="!readonly">
            <div class="cn-point cn-point--left" @mousedown.stop="startConnect(node, 'left', $event)"></div>
            <div class="cn-point cn-point--right" @mousedown.stop="startConnect(node, 'right', $event)"></div>
            <div class="cn-point cn-point--top" @mousedown.stop="startConnect(node, 'top', $event)"></div>
            <div class="cn-point cn-point--bottom" @mousedown.stop="startConnect(node, 'bottom', $event)"></div>
          </template>
        </div>

        <!-- 连线 SVG -->
        <svg class="cn-lines" :width="canvasW" :height="canvasH">
          <defs>
            <marker :id="markerId" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
              <polygon points="0 0, 10 3.5, 0 7" fill="#666" />
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
              stroke="#1890ff"
              stroke-width="2"
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
import { NODE_META } from '@/views/flowDesigner/nodes/index.js'

defineOptions({ name: 'DesignerCanvas' })

const props = defineProps({
  nodes: { type: Array, default: () => [] },
  sequences: { type: Array, default: () => [] },
  /** 连线源/目标字段名（FlowDesigner 用 sourceRef/targetRef） */
  sourceKey: { type: String, default: 'source' },
  targetKey: { type: String, default: 'target' },
  selectedNodeId: { type: String, default: null },
  selectedEdgeId: { type: String, default: null },
  readonly: { type: Boolean, default: false },
  canvasWidth: { type: Number, default: 2000 },
  canvasHeight: { type: Number, default: 1000 },
  /** 节点类型组件映射（type → 组件），两设计器共用同一套节点组件库 */
  nodeComponents: { type: Object, default: () => ({}) },
  /** 透传给节点类型组件的额外 props（如 workflowList） */
  nodeComponentProps: { type: Object, default: () => ({}) },
  /** 表单字段选项（label/value），用于分支条件标签的字段编码翻译 */
  fieldOptions: { type: Array, default: () => [] },
  /** 拖放新节点时读取的 dataTransfer key */
  dropDataKey: { type: String, default: 'nodeType' }
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
  return props.nodeComponents[type] || null
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

// ====== 节点选中 ======
function handleSelectNode(node) {
  emit('select-node', node)
}

// ====== 连线计算（锚点 + 折线） ======

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

/** 生成连线折线路径 */
function getEdgePath(edge) {
  const source = props.nodes.find(n => n.id === getEdgeSource(edge))
  const target = props.nodes.find(n => n.id === getEdgeTarget(edge))
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

/** 连线名称标签位置（中点上方） */
function getEdgeLabelPosition(edge) {
  const source = props.nodes.find(n => n.id === getEdgeSource(edge))
  const target = props.nodes.find(n => n.id === getEdgeTarget(edge))
  if (!source || !target) return { x: 0, y: 0 }
  const { sourcePoint, targetPoint } = getAnchorPoints(source, target)
  return { x: (sourcePoint.x + targetPoint.x) / 2, y: (sourcePoint.y + targetPoint.y) / 2 - 10 }
}

/** 分支条件标签位置（中点下方，无名称标签时上移） */
function getConditionLabelPosition(edge) {
  const pos = getEdgeLabelPosition(edge)
  return { x: pos.x, y: edge.name ? pos.y + 20 : pos.y + 8 }
}

/** 判断连线是否为条件分支连线（源节点为排他网关且有条件表达式） */
function isConditionEdge(edge) {
  const source = props.nodes.find(n => n.id === getEdgeSource(edge))
  return !!source && source.type === 'exclusiveGateway' && !!edge.conditionExpression
}

/**
 * 条件表达式转多行可读文案，每个条件片段一行，连接符（且/或）在后续行行首
 * 如 ${amount > 80 && status == '1'} -> ['金额 大于 80', '且 状态 等于 1']
 * @param edge 连线
 * @returns {string[]} 文案行列表，非条件分支连线返回空数组
 */
function formatEdgeConditionLines(edge) {
  if (!isConditionEdge(edge)) {
    return [];
  }
  let s = String(edge.conditionExpression).trim();
  if (s.startsWith('${') && s.endsWith('}')) {
    s = s.slice(2, -1);
  }
  // 顶层按 && / || 拆分（引号内的连接符不拆分）
  const { parts, logics } = splitConditionFragments(s);
  const lines = [];
  for (let i = 0; i < parts.length; i++) {
    if (!parts[i]) {
      continue;
    }
    const connector = i === 0 ? '' : (logics[i - 1] === '||' ? '或 ' : '且 ');
    lines.push(connector + formatConditionFragment(parts[i]));
  }
  return lines;
}

/**
 * 在顶层按 && / || 拆分表达式（引号内的连接符不拆分）
 * @param s 表达式（不含 ${} 包裹）
 * @returns {{parts: string[], logics: string[]}} 片段列表与各片段前的连接符
 */
function splitConditionFragments(s) {
  const parts = [];
  const logics = [];
  let cur = '';
  let inQuote = false;
  for (let i = 0; i < s.length; i++) {
    const ch = s[i];
    if (ch === "'") {
      inQuote = !inQuote;
      cur += ch;
    } else if (!inQuote && (s.startsWith('&&', i) || s.startsWith('||', i))) {
      logics.push(s.substr(i, 2));
      parts.push(cur.trim());
      cur = '';
      i++;
    } else {
      cur += ch;
    }
  }
  parts.push(cur.trim());
  return { parts, logics };
}

/**
 * 单个条件片段转可读文案：amount > 80 -> 金额 大于 80
 * @param frag 条件片段
 * @returns {string} 可读文案
 */
function formatConditionFragment(frag) {
  let s = frag;
  // 字段编码替换为字段名称（长编码优先，避免前缀误替换）
  const codes = props.fieldOptions.map(f => f.value).filter(Boolean).sort((a, b) => b.length - a.length);
  codes.forEach(code => {
    const label = getFieldLabel(code);
    s = s.replace(new RegExp(`(?<![\\w])${escapeRegExp(code)}(?![\\w])`, 'g'), label);
  });
  return s
      .replace(/\.contains\(['"]?([^'")]+)['"]?\)/g, ' 包含 $1 ')
      .replace(/>=/g, ' 大于等于 ')
      .replace(/<=/g, ' 小于等于 ')
      .replace(/==/g, ' 等于 ')
      .replace(/!=/g, ' 不等于 ')
      .replace(/>/g, ' 大于 ')
      .replace(/</g, ' 小于 ')
      .replace(/['"]/g, '')
      .replace(/\s+/g, ' ')
      .trim();
}

/** 字段编码翻译为字段名称（无映射时返回原编码） */
function getFieldLabel(code) {
  const find = props.fieldOptions.find(f => f.value === code);
  return find ? find.label : code;
}

/** 转义正则特殊字符 */
function escapeRegExp(str) {
  return String(str).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function getConnectingPath() {
  if (!connecting.value) return ''
  const { from, to } = connecting.value
  const midX = (from.x + to.x) / 2
  return `M ${from.x} ${from.y} L ${midX} ${from.y} L ${midX} ${to.y} L ${to.x} ${to.y}`
}

function handleSelectEdge(edge) {
  emit('select-edge', edge)
}

function handleDeleteEdge(edge) {
  emit('delete-edge', edge.id)
}

// ====== 节点拖拽 ======
function handleNodeMouseDown(event, node) {
  if (props.readonly) return
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

// ====== 连线创建：从节点四周连接点拖到目标节点 ======
function startConnect(node, dir, event) {
  event.stopPropagation()
  if (props.readonly) return
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

// ====== 画布空白点击：取消选中 ======
function handleCanvasMouseDown() {
  emit('select-node', null)
  emit('select-edge', null)
}

// ====== 拖放新节点 ======
function handleDragOver(event) {
  if (!props.readonly) event.preventDefault()
}

function handleDrop(event) {
  if (props.readonly) return
  const type = event.dataTransfer.getData(props.dropDataKey)
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
  background-color: #fafbfc;
  background-image:
    linear-gradient(90deg, #eee 1px, transparent 1px),
    linear-gradient(180deg, #eee 1px, transparent 1px);
  background-size: 20px 20px;
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
  background: #fff;
  border: 1.5px solid #d9d9d9;
}

.cn-node-icon {
  line-height: 1;
}

.cn-node-name {
  margin-top: 2px;
  font-size: 13px;
  font-weight: 550;
  color: $color-text-primary;
  line-height: 1.2;
  word-break: break-all;
  max-width: 120px;
}

// 四向连接点
.cn-point {
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
.cn-point:hover,
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
}
.cn-lines path {
  pointer-events: visibleStroke;
}
.cn-line {
  stroke: #666;
  stroke-width: 2;
  fill: none;
  stroke-linecap: round;
  stroke-linejoin: round;
  cursor: pointer;
}
.cn-line--selected {
  stroke: #1890ff !important;
  stroke-width: 2.5;
}
.cn-line-label {
  pointer-events: all;
  cursor: pointer;
  font-size: 11px;
  fill: #5e6c84;
  text-anchor: middle;
  font-weight: 500;
}
.cn-line-condition {
  pointer-events: all;
  cursor: pointer;
  font-size: 11px;
  fill: #fa8c16;
  text-anchor: middle;
  font-weight: 500;
  // 白色描边光晕：保证文字在网格背景/连线交叉处可读
  paint-order: stroke;
  stroke: #fff;
  stroke-width: 3px;
  stroke-linejoin: round;
}
</style>

<style lang="scss">
/* 非 scoped：确保 ::-webkit-scrollbar 伪元素生效 */
.designer-canvas {
  scrollbar-width: thin;
  -ms-overflow-style: auto;
}
.designer-canvas::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.designer-canvas::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}
.designer-canvas::-webkit-scrollbar-track {
  background: transparent;
}
</style>

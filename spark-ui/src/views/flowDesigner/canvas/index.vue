<template>
  <div
      class="designer-canvas"
      :class="{ 'designer-canvas--bare': bare, 'designer-canvas--auto': autoLayout }"
      ref="canvasRef"
      @drop="handleDrop"
      @dragover="handleDragOver"
      @mousedown="handleCanvasMouseDown"
  >
    <div
        class="canvas-content"
        :style="{ width: canvasW + 'px', height: canvasH + 'px' }"
    >
        <!-- 分支泳道：条件标签（自动布局模式下替代连线上的条件文字）+ 空分支占位卡 -->
        <template v-for="lane in visibleLanes" :key="lane.key">
          <div
              v-if="!lane.pending && (lane.label || !readonly)"
              class="cn-lane-chip"
              :class="{
                'is-empty': !lane.label,
                'is-editable': !readonly,
                'is-multi': (lane.lines?.length || 0) > 1
              }"
              :style="{ left: lane.x + 'px', top: lane.y + 'px', width: lane.w + 'px', height: lane.h + 'px' }"
              :title="lane.label || (readonly ? '' : '点击配置该分支条件')"
              @mousedown.stop
              @click.stop="handleSelectBranch(lane)"
          >
            <template v-if="lane.lines && lane.lines.length">
              <span v-for="(line, idx) in lane.lines" :key="idx" class="cn-lane-chip-line">{{ line }}</span>
            </template>
            <span v-else class="cn-lane-chip-line">未设置条件</span>
          </div>
          <div
              v-else-if="!readonly"
              class="cn-lane-slot"
              :style="{ left: lane.x + 'px', top: lane.y + 'px', width: lane.w + 'px', height: lane.h + 'px' }"
              title="点击为该分支添加节点"
              @mousedown.stop
              @click.stop="emit('add-branch', lane, $event)"
          >
            <el-icon><Plus /></el-icon>
            <span>添加节点</span>
          </div>
        </template>

        <!-- 节点（div 定位） -->
        <div
            v-for="node in nodes"
            :key="node.id"
            class="cn-node"
            :class="{
              'cn-node--selected': selectedNodeId === node.id,
              'cn-node--static': autoLayout
            }"
            :style="nodeStyle(node)"
            @mousedown.stop="handleNodeMouseDown($event, node)"
            @click.stop="handleSelectNode(node)"
            @contextmenu.prevent.stop="handleNodeContext($event, node)"
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

          <!-- 四向连接点：自动布局模式由流程顺序推导连线，不再提供手动连线 -->
          <template v-if="!readonly && !autoLayout">
            <div class="cn-point cn-point--left" @mousedown.stop="startConnect(node, 'left', $event)"></div>
            <div class="cn-point cn-point--right" @mousedown.stop="startConnect(node, 'right', $event)"></div>
            <div class="cn-point cn-point--top" @mousedown.stop="startConnect(node, 'top', $event)"></div>
            <div class="cn-point cn-point--bottom" @mousedown.stop="startConnect(node, 'bottom', $event)"></div>
          </template>

          <!-- 自动布局模式：节点下沿悬停出现 ＋，等价于右键「添加节点」 -->
          <div
              v-if="autoLayout && !readonly && node.type !== 'endEvent'"
              class="cn-add"
              title="在此节点后添加节点"
              @mousedown.stop
              @click.stop="emit('add-after', node, $event)"
          >
            <el-icon><Plus /></el-icon>
          </div>
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
              v-for="edge in visibleSequences"
              :key="edge.id"
              :d="getEdgePath(edge)"
              class="cn-line"
              :class="{ 'cn-line--selected': selectedEdgeId === edge.id }"
              :marker-end="'url(#' + markerId + ')'"
              @mousedown.stop
              @click.stop="handleSelectEdge(edge)"
              @dblclick.stop="handleDeleteEdge(edge)"
          />
          <!-- 空分支占位卡的两段连线：分支节点 → 占位卡 → 汇合点，纯展示不可选 -->
          <path
              v-for="edge in slotEdges"
              :key="edge.id"
              :d="getEdgePath(edge)"
              class="cn-line cn-line--virtual"
              :marker-end="'url(#' + markerId + ')'"
          />

          <!-- 连线名称标签（v-for 放 template 上，避免与 v-if 同元素时优先级问题） -->
          <template v-for="edge in visibleSequences" :key="'text-' + edge.id">
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
          <template v-for="edge in visibleSequences" :key="'cond-' + edge.id">
            <text
                v-if="!autoLayout && formatEdgeConditionLines(edge).length"
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
import { Plus } from '@element-plus/icons-vue'
import { NODE_META as BUILTIN_NODE_META } from '@/views/flowDesigner/nodes/index.js'
import { formatConditionLines } from '@/views/flowDesigner/utils/flowCondition.js'

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
  /**
   * 自动布局模式：节点位置由流程顺序推导，禁止拖动与手动连线，
   * 支持右键菜单添加上下文节点、节点下沿 ＋ 快捷添加
   */
  autoLayout: { type: Boolean, default: false },
  /** 分支泳道条件标签（仅自动布局模式使用） */
  lanes: { type: Array, default: () => [] },
  /** 裸画布：不承担滚动容器，由父级决定尺寸（只读预览使用） */
  bare: { type: Boolean, default: false },
  canvasWidth: { type: Number, default: 2000 },
  canvasHeight: { type: Number, default: 1000 },
  /** 节点类型组件映射（type → 组件） */
  nodeComponents: { type: Object, default: () => ({}) },
  /** 表单字段选项（label/value），用于分支条件标签的字段编码翻译 */
  fieldOptions: { type: Array, default: () => [] },
  /** 不绘制的连线 id（空分支的占位连线，避免出现默认连到结束节点的线） */
  hiddenEdgeIds: { type: Array, default: () => [] }
})

const emit = defineEmits([
  'select-node', 'select-edge', 'select-branch',
  'move-node', 'connect', 'delete-edge', 'drop-node',
  'node-contextmenu', 'add-after', 'add-branch'
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

/** 自动布局下的分支条件标签（未配置条件时保留占位，便于在画布上定位） */
const visibleLanes = computed(() => (props.autoLayout ? props.lanes : []))

/**
 * 编辑态的空分支占位卡：只读预览不展示占位卡，直接呈现真实连线
 * （每项含 x/y/w/h 与 gatewayId / joinId，可作为虚拟节点参与连线计算）
 */
const pendingLanes = computed(() => (
  props.autoLayout && !props.readonly ? visibleLanes.value.filter(lane => lane.pending) : []
))

/** 空分支占位卡的虚拟节点 id 前缀：与真实节点 id 区分，避免冲突 */
const SLOT_NODE_PREFIX = '__slot-'

/** 占位卡虚拟节点表：id -> 几何信息，仅供连线锚点计算使用 */
const slotNodeMap = computed(() => {
  const map = new Map()
  pendingLanes.value.forEach(lane => {
    map.set(SLOT_NODE_PREFIX + lane.key, {
      id: SLOT_NODE_PREFIX + lane.key,
      x: lane.x,
      y: lane.y,
      width: lane.w,
      height: lane.h
    })
  })
  return map
})

/** 实际绘制的连线：编辑态过滤掉空分支的占位连线（改由占位卡的两段连线表示） */
const visibleSequences = computed(() => {
  if (props.readonly || !props.hiddenEdgeIds.length) return props.sequences
  const hidden = new Set(props.hiddenEdgeIds)
  return props.sequences.filter(edge => !hidden.has(edge.id))
})

/** 空分支占位卡的两段连线：分支节点 → 占位卡 → 汇合点 */
const slotEdges = computed(() => {
  const edges = []
  pendingLanes.value.forEach(lane => {
    const slotId = SLOT_NODE_PREFIX + lane.key
    if (lane.gatewayId) {
      edges.push({ id: `${slotId}-in`, [props.sourceKey]: lane.gatewayId, [props.targetKey]: slotId })
    }
    if (lane.joinId) {
      edges.push({ id: `${slotId}-out`, [props.sourceKey]: slotId, [props.targetKey]: lane.joinId })
    }
  })
  return edges
})

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
  return BUILTIN_NODE_META[type] || {}
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

// 节点选中
function handleSelectNode(node) {
  emit('select-node', node)
}

/** 节点右键：交给上层弹出「添加节点」菜单 */
function handleNodeContext(event, node) {
  if (props.readonly) return
  emit('node-contextmenu', { node, event })
}

// 连线计算（锚点 + 折线）

/** 节点查找：真实节点优先，空分支占位卡按虚拟节点参与连线计算 */
function findNode(id) {
  return props.nodes.find(n => n.id === id) || slotNodeMap.value.get(id) || null
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

/** 条件表达式转多行可读文案（解析逻辑与分支泳道标签共用） */
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

/**
 * 连线选中：自动布局模式下连线不可选，分支条件统一从泳道条件标签进入配置
 * （保留旧设计器的手动连线交互）
 */
function handleSelectEdge(edge) {
  if (props.autoLayout) return
  emit('select-edge', edge)
}

/** 双击删除连线：自动布局模式下连线结构由流程顺序推导，不允许删除 */
function handleDeleteEdge(edge) {
  if (props.autoLayout) return
  emit('delete-edge', edge.id)
}

/** 泳道条件标签点击：打开该分支的配置面板 */
function handleSelectBranch(lane) {
  if (props.readonly || lane.pending) return
  emit('select-branch', lane)
}

// 节点拖拽（自动布局模式下位置由流程顺序决定，不可拖动）
function handleNodeMouseDown(event, node) {
  if (props.readonly || props.autoLayout) return
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

// 画布空白点击：取消选中
function handleCanvasMouseDown() {
  emit('select-node', null)
  emit('select-edge', null)
}

// 拖放新节点
function handleDragOver(event) {
  if (!props.readonly && !props.autoLayout) event.preventDefault()
}

function handleDrop(event) {
  if (props.readonly || props.autoLayout) return
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
}

.canvas-content {
  position: relative;
}

// 裸画布：由父级给定尺寸，自身不滚动（只读预览按缩放比例整体变换）
.designer-canvas--bare {
  flex: none;
  width: 100%;
  height: 100%;
  overflow: visible;
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

// 自动布局模式：位置由流程顺序决定，只可点选
.cn-node--static { cursor: pointer; }

// 自动布局模式：节点下沿的 ＋ 快捷添加（等价于右键「添加节点」）
.cn-add {
  position: absolute;
  left: 50%;
  bottom: -30px;
  transform: translateX(-50%);
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: $app-bg-card;
  border: 1px dashed $el-color-primary;
  color: $el-color-primary;
  font-size: 11px;
  z-index: 60;
  opacity: 0;
  transition: opacity 0.15s ease;
}

.cn-node:hover .cn-add { opacity: 1; }

.cn-add:hover {
  background: $el-color-primary;
  border-style: solid;
  color: $color-text-white;
}

// 分支泳道条件标签：多条件逐行展示（行数与高度由布局推导，不做省略号截断）
.cn-lane-chip {
  position: absolute;
  box-sizing: border-box;
  z-index: 25;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  line-height: 16px;
  text-align: center;
  border-radius: 999px;
  border: 1px dashed $app-node-gateway;
  background: $app-node-gateway-bg;
  color: $app-node-gateway;
  font-size: 11px;
  white-space: normal;
  word-break: break-word;
  user-select: none;
  pointer-events: none;

  // 多行时圆角收敛为普通圆角卡片，条件逐行左对齐，便于分辨「且/或」层级
  &.is-multi {
    align-items: flex-start;
    border-radius: 10px;
    text-align: left;
  }

  // 分支尚未配置条件：用中性配色提示这里可以填，不与真实条件抢注意力
  &.is-empty {
    border-color: $border-color;
    background: $app-bg-sub;
    color: $app-text-placeholder;
  }

  // 编辑态：标签即分支配置入口
  &.is-editable {
    pointer-events: auto;
    cursor: pointer;
    transition: border-color 0.15s, background 0.15s, box-shadow 0.15s;

    &:hover {
      border-color: $el-color-primary;
      background: $app-bg-card;
      box-shadow: 0 2px 8px rgba(15, 34, 66, 0.1);
    }
  }
}

.cn-lane-chip-line {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// 空分支占位卡：点击后在当前分支插入节点
.cn-lane-slot {
  position: absolute;
  box-sizing: border-box;
  z-index: 25;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border-radius: 8px;
  border: 1.5px dashed $border-color;
  background: $app-bg-sub;
  color: $app-text-placeholder;
  font-size: 12px;
  cursor: pointer;
  user-select: none;
  transition: border-color 0.15s, color 0.15s, background 0.15s;

  &:hover {
    border-color: $el-color-primary;
    background: $app-bg-card;
    color: $el-color-primary;
  }
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
  stroke: $app-text-placeholder;
  stroke-width: 1.5;
  fill: none;
  stroke-linecap: round;
  stroke-linejoin: round;
  cursor: pointer;
}
// 自动布局模式：连线不可点选（分支条件从泳道标签进入配置），光标不显示手型
.designer-canvas--auto .cn-line {
  cursor: default;
}
.cn-line--selected {
  stroke: $el-color-primary !important;
  stroke-width: 2;
}
// 空分支占位卡的连线：只画不响应，占位卡本身才是点击目标
.cn-line--virtual {
  pointer-events: none;
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

// 画布滚动条收细并跟随主题滑块色
.designer-canvas {
  scrollbar-width: thin;
  -ms-overflow-style: auto;
}
.designer-canvas::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.designer-canvas::-webkit-scrollbar-thumb {
  background: $app-scrollbar-thumb;
  border-radius: 3px;
}
.designer-canvas::-webkit-scrollbar-track {
  background: transparent;
}
</style>

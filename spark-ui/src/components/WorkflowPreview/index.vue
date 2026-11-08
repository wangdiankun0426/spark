<template>
  <div ref="wrapRef" class="workflow-preview" :style="{ height: heightStyle }">
    <!--只读画布：节点/连线/分支条件全展示，不可点选，支持拖拽平移与缩放-->
    <div class="wp-stage" @mousedown.capture="onPanStart" @wheel.prevent="onWheel">
      <div class="wp-content" :style="contentStyle">
        <div
            v-for="node in layoutNodes"
            :key="node.id"
            class="wp-node"
            :style="nodeStyle(node)"
        >
          <node-card :node="node" />
        </div>

        <svg class="wp-lines" :width="content.w" :height="content.h">
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
              <path d="M0 0 L6 3 L0 6 z" class="wp-arrow" />
            </marker>
          </defs>

          <!--连线-->
          <path
              v-for="edge in sequences"
              :key="edge.id"
              :d="edgePath(edge)"
              class="wp-line"
              :marker-end="'url(#' + markerId + ')'"
          />

          <!--分叉圆点-->
          <circle
              v-for="fork in forks"
              :key="'fork-' + fork.id"
              :cx="fork.x"
              :cy="fork.y"
              r="3.5"
              class="wp-fork-dot"
          />

          <!--分支名称-->
          <text
              v-for="item in namedEdges"
              :key="'label-' + item.edge.id"
              :x="item.x"
              :y="item.y"
              class="wp-line-label"
          >{{ item.edge.name }}</text>

          <!--分支条件：多条件逐行完整展示-->
          <g
              v-for="chip in chips"
              :key="'chip-' + chip.edgeId"
              class="wp-chip"
              :class="{ 'wp-chip--multi': chip.multi }"
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
        </svg>
      </div>
    </div>

    <!--空态-->
    <div v-if="!layoutNodes.length" class="wp-empty">
      <p>{{ emptyText }}</p>
    </div>

    <!--缩放工具条-->
    <div class="wp-tools" @mousedown.stop>
      <button type="button" title="缩小" @click="zoomBy(-0.1)">−</button>
      <button type="button" class="wp-pct" title="恢复 100%" @click="resetZoom">{{ percent }}</button>
      <button type="button" title="放大" @click="zoomBy(0.1)">＋</button>
      <span class="wp-sep"></span>
      <button type="button" class="wp-fit" @click="fitView">适应</button>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import NodeCard from '@/views/workflowDesigner/nodes/NodeCard.vue'
import { NODE_SIZE } from '@/views/workflowDesigner/nodes/index.js'
import {
  buildConditionChips,
  buildGatewayForks,
  edgeMidpoint,
  getEdgePath
} from '@/views/workflowDesigner/utils/flowGeometry.js'

defineOptions({ name: 'WorkflowPreview' })

const props = defineProps({
  /** 节点：需带绝对坐标 { id, type, name, x, y, config, disabled } */
  nodes: { type: Array, default: () => [] },
  /** 连线：{ id, source, target, name, conditionExpression } */
  sequences: { type: Array, default: () => [] },
  /** 表单字段选项（label/value），用于分支条件文案翻译 */
  fieldOptions: { type: Array, default: () => [] },
  /** 组件高度：数字按 px，字符串原样（如 '100%'） */
  height: { type: [String, Number], default: 520 },
  emptyText: { type: String, default: '暂无流程节点' }
})

/** 工具条占位高度，避免内容被工具条压住 */
const TOOLS_HEIGHT = 46
/** 内容四周留白 */
const PAD = 36
/** 箭头 marker 需要全局唯一，同页多实例才不会互相覆盖 */
const markerId = `wp-arrow-${Math.random().toString(36).slice(2, 8)}`

const wrapRef = ref(null)
const size = reactive({ w: 0, h: 0 })
const scale = ref(1)
const pan = reactive({ x: 0, y: 0 })

const heightStyle = computed(() => (typeof props.height === 'number' ? `${props.height}px` : props.height))

const coord = (value) => (Number.isFinite(Number(value)) ? Number(value) : 0)

/**
 * 兜底排布：节点全都没有坐标时，按拓扑层级自动分列（列 = 层级，行 = 同层顺序）
 * 这样别处只有 DAG 结构（无 x/y）也能直接传进来预览
 */
function autoLayout(nodes, sequences) {
  const ids = new Set(nodes.map(n => n.id))
  const indegree = new Map(nodes.map(n => [n.id, 0]))
  const next = new Map(nodes.map(n => [n.id, []]))
  sequences.forEach(edge => {
    if (!ids.has(edge.source) || !ids.has(edge.target)) return
    next.get(edge.source).push(edge.target)
    indegree.set(edge.target, (indegree.get(edge.target) || 0) + 1)
  })

  // 从入度为 0 的节点开始按最长路径分层
  const depth = new Map(nodes.map(n => [n.id, 0]))
  const queue = nodes.filter(n => !indegree.get(n.id)).map(n => n.id)
  const visited = new Set(queue)
  while (queue.length) {
    const id = queue.shift()
    for (const targetId of next.get(id) || []) {
      depth.set(targetId, Math.max(depth.get(targetId) || 0, (depth.get(id) || 0) + 1))
      if (!visited.has(targetId)) {
        visited.add(targetId)
        queue.push(targetId)
      }
    }
  }

  const columns = new Map()
  nodes.forEach(node => {
    const d = depth.get(node.id) || 0
    if (!columns.has(d)) columns.set(d, [])
    columns.get(d).push(node.id)
  })

  const colGap = NODE_SIZE.width + 104
  const rowGap = NODE_SIZE.height + 48
  const positions = {}
  columns.forEach((colIds, d) => {
    colIds.forEach((id, i) => {
      positions[id] = {
        x: d * colGap,
        y: i * rowGap - ((colIds.length - 1) * rowGap) / 2
      }
    })
  })
  return positions
}

/** 数据自带坐标就用原坐标，否则走兜底排布 */
const autoPositions = computed(() => {
  const list = props.nodes || []
  if (!list.length) return null
  const allHasCoord = list.every(n => Number.isFinite(Number(n.x)) && Number.isFinite(Number(n.y)))
  return allHasCoord ? null : autoLayout(list, props.sequences || [])
})

const posOf = (node) => (autoPositions.value
    ? autoPositions.value[node.id] || { x: 0, y: 0 }
    : { x: coord(node.x), y: coord(node.y) })

/** 节点包围盒：预览不依赖外部给的画布尺寸，按内容自适应 */
const box = computed(() => {
  const list = props.nodes || []
  if (!list.length) return { minX: 0, minY: 0, w: 0, h: 0 }
  const points = list.map(posOf)
  const minX = Math.min(...points.map(pt => pt.x))
  const minY = Math.min(...points.map(pt => pt.y))
  const maxX = Math.max(...points.map(pt => pt.x)) + NODE_SIZE.width
  const maxY = Math.max(...points.map(pt => pt.y)) + NODE_SIZE.height
  return { minX, minY, w: maxX - minX, h: maxY - minY }
})

/** 画布内容尺寸（含留白） */
const content = computed(() => ({
  w: Math.max(1, box.value.w + PAD * 2),
  h: Math.max(1, box.value.h + PAD * 2)
}))

/** 把绝对坐标平移到内容坐标系，左上角对齐留白 */
const offset = computed(() => ({ x: PAD - box.value.minX, y: PAD - box.value.minY }))

const layoutNodes = computed(() => (props.nodes || []).map(node => {
  const pt = posOf(node)
  return {
    ...node,
    x: pt.x + offset.value.x,
    y: pt.y + offset.value.y
  }
}))

const forks = computed(() => buildGatewayForks(layoutNodes.value, props.sequences))

const chips = computed(() => buildConditionChips({
  nodes: layoutNodes.value,
  sequences: props.sequences,
  fieldOptions: props.fieldOptions,
  // 只读预览不给「＋ 分支条件」入口
  blankEntry: false
}))

/** 有分支标签的连线 id：分支名称已并入标签，不再单独画名称 */
const chipEdgeIds = computed(() => new Set(chips.value.map(chip => chip.edgeId)))

/** 普通连线（无分支标签）上的名称，标在中点上方 */
const namedEdges = computed(() => (props.sequences || [])
    .filter(edge => String(edge.name || '').trim() && !chipEdgeIds.value.has(edge.id))
    .map(edge => {
      const mid = edgeMidpoint(layoutNodes.value, edge)
      return { edge, x: mid.x, y: mid.y - 10 }
    }))

function edgePath(edge) {
  return getEdgePath(layoutNodes.value, edge)
}

function nodeStyle(node) {
  return {
    left: node.x + 'px',
    top: node.y + 'px',
    width: NODE_SIZE.width + 'px',
    height: NODE_SIZE.height + 'px'
  }
}

/** 缩放后的内容变换：水平居中，垂直扣除工具条高度后居中 */
const contentStyle = computed(() => {
  const s = scale.value
  const tx = (size.w - content.value.w * s) / 2 + pan.x
  const ty = (size.h - TOOLS_HEIGHT - content.value.h * s) / 2 + pan.y
  return {
    width: content.value.w + 'px',
    height: content.value.h + 'px',
    transform: `translate(${tx}px, ${ty}px) scale(${s})`
  }
})

const percent = computed(() => `${Math.round(scale.value * 100)}%`)

/** 自适应比例：内容完整显示，最大不超过 100% */
function fitView() {
  const availW = Math.max(size.w - PAD * 2, 80)
  const availH = Math.max(size.h - TOOLS_HEIGHT - PAD, 80)
  scale.value = Math.max(0.2, Math.min(1, availW / content.value.w, availH / content.value.h))
  pan.x = 0
  pan.y = 0
}

function zoomBy(delta) {
  scale.value = Math.min(3, Math.max(0.4, Number((scale.value + delta).toFixed(2))))
}

function resetZoom() {
  scale.value = 1
  pan.x = 0
  pan.y = 0
}

/* ---------------- 拖拽平移 ---------------- */

let panning = null

function onPanStart(event) {
  if (event.button !== 0) return
  panning = { startX: event.clientX, startY: event.clientY, baseX: pan.x, baseY: pan.y }
  document.addEventListener('mousemove', onPanMove)
  document.addEventListener('mouseup', onPanEnd)
}

function onPanMove(event) {
  if (!panning) return
  pan.x = panning.baseX + (event.clientX - panning.startX)
  pan.y = panning.baseY + (event.clientY - panning.startY)
}

function onPanEnd() {
  document.removeEventListener('mousemove', onPanMove)
  document.removeEventListener('mouseup', onPanEnd)
  panning = null
}

/** 滚轮缩放：预览容器内不触发页面滚动 */
function onWheel(event) {
  zoomBy(event.deltaY > 0 ? -0.08 : 0.08)
}

/* ---------------- 生命周期 ---------------- */

let observer = null

onMounted(() => {
  const el = wrapRef.value
  if (!el) return
  const update = () => {
    size.w = el.clientWidth
    size.h = el.clientHeight
  }
  update()
  fitView()
  observer = new ResizeObserver(() => {
    update()
    fitView()
  })
  observer.observe(el)
})

onBeforeUnmount(() => {
  observer?.disconnect()
  onPanEnd()
})

// 数据变化（切换流程）时重新自适应；只认引用变化，避免深监听在缩放时反复重置
watch(() => [props.nodes, props.sequences], () => fitView())
</script>

<style scoped lang="scss">
@use '../../views/workflowDesigner/styles/vars.scss' as *;

.workflow-preview {
  position: relative;
  min-height: 240px;
  overflow: hidden;
  border-radius: 10px;
  background-color: $app-canvas-bg;
  background-image:
      radial-gradient(circle, $app-canvas-dot 1.2px, transparent 1.8px),
      radial-gradient(circle, $app-canvas-dot-soft 1px, transparent 1.6px);
  background-size: 22px 22px, 22px 22px;
  background-position: 0 0, 11px 11px;
}

.wp-stage {
  position: absolute;
  inset: 0;
  overflow: hidden;
  cursor: grab;

  &:active {
    cursor: grabbing;
  }
}

.wp-content {
  position: absolute;
  top: 0;
  left: 0;
  transform-origin: 0 0;
}

/* 只读：节点不响应任何交互，光标沿用容器的拖拽样式 */
.wp-node {
  position: absolute;
  pointer-events: none;
  user-select: none;

  /* 占位节点在编辑态的 pointer 光标在预览里没有意义 */
  :deep(.node-card) {
    cursor: inherit;
  }
}

.wp-lines {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 20;
}

.wp-line {
  stroke: $wf-line;
  stroke-width: 1.5;
  fill: none;
  stroke-linecap: round;
}

.wp-arrow {
  fill: $wf-line;
}

.wp-fork-dot {
  fill: $app-bg-card;
  stroke: $wf-line;
  stroke-width: 1.5;
}

.wp-line-label {
  font-size: 11px;
  fill: $app-text-secondary;
  text-anchor: middle;
}

.wp-chip {
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

  /* 多条件逐行左对齐 */
  &.wp-chip--multi text {
    text-anchor: start;
  }

  /* 首行分支名称：加粗加重，与条件行区分 */
  text.is-name {
    fill: $app-text-primary;
    font-weight: 600;
  }
}

.wp-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $app-text-placeholder;
  font-size: 13px;
  pointer-events: none;
}

.wp-tools {
  position: absolute;
  left: 50%;
  bottom: 12px;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 3px;
  border-radius: 8px;
  background: $app-bg-card;
  border: 1px solid $border-color-light;
  box-shadow: $app-shadow-card;

  button {
    min-width: 26px;
    height: 24px;
    padding: 0 6px;
    border: none;
    border-radius: 5px;
    background: transparent;
    color: $app-text-secondary;
    font-size: 12px;
    cursor: pointer;

    &:hover {
      background: $app-bg-sub;
      color: $app-primary;
    }
  }

  .wp-pct {
    min-width: 46px;
    font-variant-numeric: tabular-nums;
  }
}

.wp-sep {
  width: 1px;
  height: 14px;
  margin: 0 2px;
  background: $border-color-light;
}
</style>

<style lang="scss">
/* 令牌声明在 :root 上，且预览可能被 teleport 到 body：组件自带一份，任何页面复用都不会丢变量 */
@use '../../views/workflowDesigner/styles/tokens.scss';
</style>

<template>
  <div class="flow-preview" ref="wrapRef">
    <!-- 画布：只读纯展示，可拖动平移，节点不可点选 -->
    <div class="fp-stage" @mousedown.capture="onPanStart" @wheel.prevent="onWheel">
      <div class="fp-content" :style="contentStyle">
        <canvas-index
            :nodes="viewNodes"
            :sequences="viewSequences"
            source-key="sourceRef"
            target-key="targetRef"
            :canvas-width="layout.width"
            :canvas-height="layout.height"
            :node-components="NODE_COMPONENTS"
            :field-options="fieldOptions"
            :lanes="layout.lanes"
            bare
            auto-layout
            readonly
        />
      </div>
    </div>

    <!-- 缩放条 -->
    <div class="fp-tools" @mousedown.stop>
      <button type="button" title="缩小" @click="zoomBy(-0.1)">−</button>
      <button type="button" class="fp-pct" title="恢复 100%" @click="resetZoom">{{ percent }}</button>
      <button type="button" title="放大" @click="zoomBy(0.1)">＋</button>
      <span class="fp-sep"></span>
      <button type="button" class="fp-fit" @click="fitView">适应</button>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import CanvasIndex from '@/views/flowDesigner/canvas/index.vue'
import { NODE_COMPONENTS } from '@/views/flowDesigner/nodes/index.js'
import { layoutFlow } from '@/views/flowDesigner/utils/flowLayout.js'

defineOptions({ name: 'FlowPreview' })

const props = defineProps({
  /** 流程图数据：{ nodes, sequences } */
  bpmJson: { type: Object, default: () => ({}) },
  /** 表单字段选项（label/value），用于分支条件文案翻译 */
  fieldOptions: { type: Array, default: () => [] }
})

const wrapRef = ref(null)
/** 容器尺寸 */
const size = reactive({ w: 0, h: 0 })
/** 当前缩放比例 */
const scale = ref(1)
/** 自适应比例 */
const fitScale = ref(1)
/** 平移偏移 */
const pan = reactive({ x: 0, y: 0 })

/** 底部缩放条占位高度，避免内容被工具条压住 */
const TOOLS_HEIGHT = 46

/** 布局：节点坐标、分支泳道、画布尺寸都由流程顺序推导 */
const layout = computed(() => layoutFlow(
    props.bpmJson?.nodes || [],
    props.bpmJson?.sequences || [],
    { fieldOptions: props.fieldOptions }
))

/** 带坐标的节点（复制一份，避免改动入参） */
const viewNodes = computed(() => (props.bpmJson?.nodes || []).map(node => {
  const pos = layout.value.positions[node.id]
  return pos ? { ...node, x: pos.x, y: pos.y, width: pos.w, height: pos.h } : { ...node }
}))

const viewSequences = computed(() => props.bpmJson?.sequences || [])

/** 缩放后的内容变换：水平居中，垂直扣除工具条高度后居中 */
const contentStyle = computed(() => {
  const contentW = layout.value.width || 1
  const contentH = layout.value.height || 1
  const s = scale.value
  const tx = (size.w - contentW * s) / 2 + pan.x
  const ty = (size.h - TOOLS_HEIGHT - contentH * s) / 2 + pan.y
  return {
    width: contentW + 'px',
    height: contentH + 'px',
    transform: `translate(${tx}px, ${ty}px) scale(${s})`
  }
})

const percent = computed(() => `${Math.round(scale.value * 100)}%`)

/**
 * 计算自适应比例：内容完整显示，最大不超过 100%
 */
const computeFit = () => {
  const contentW = layout.value.width || 1
  const contentH = layout.value.height || 1
  const availW = Math.max(size.w - 56, 80)
  const availH = Math.max(size.h - TOOLS_HEIGHT - 32, 80)
  fitScale.value = Math.max(0.2, Math.min(1, availW / contentW, availH / contentH))
  scale.value = fitScale.value
  pan.x = 0
  pan.y = 0
}

/** 缩放（0.4 ~ 3） */
const zoomBy = (delta) => {
  scale.value = Math.min(3, Math.max(0.4, Number((scale.value + delta).toFixed(2))))
}

const resetZoom = () => {
  scale.value = 1
  pan.x = 0
  pan.y = 0
}

const fitView = () => computeFit()

/* ---------------- 拖拽平移 ---------------- */

let panning = null

const onPanStart = (event) => {
  if (event.button !== 0) return
  panning = { startX: event.clientX, startY: event.clientY, baseX: pan.x, baseY: pan.y }
  document.addEventListener('mousemove', onPanMove)
  document.addEventListener('mouseup', onPanEnd)
}

const onPanMove = (event) => {
  if (!panning) return
  pan.x = panning.baseX + (event.clientX - panning.startX)
  pan.y = panning.baseY + (event.clientY - panning.startY)
}

const onPanEnd = () => {
  document.removeEventListener('mousemove', onPanMove)
  document.removeEventListener('mouseup', onPanEnd)
  panning = null
}

/** 滚轮缩放（ctrl 或普通滚动均按缩放处理，避免误触发页面滚动） */
const onWheel = (event) => {
  const delta = event.deltaY > 0 ? -0.08 : 0.08
  zoomBy(delta)
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
  computeFit()
  observer = new ResizeObserver(() => {
    update()
    computeFit()
  })
  observer.observe(el)
})

onBeforeUnmount(() => {
  observer?.disconnect()
  onPanEnd()
})

// 数据变化（切换流程）时重新自适应
watch(() => props.bpmJson, () => {
  computeFit()
}, { deep: true })
</script>

<style scoped lang="scss">
.flow-preview {
  position: relative;
  height: 100%;
  min-height: 260px;
  overflow: hidden;
  background-color: $app-canvas-bg;
  background-image:
      radial-gradient(circle, $app-canvas-dot 1.2px, transparent 1.8px),
      radial-gradient(circle, $app-canvas-dot-soft 1px, transparent 1.6px);
  background-size: 22px 22px, 22px 22px;
  background-position: 0 0, 11px 11px;
}

.fp-stage {
  position: absolute;
  inset: 0;
  overflow: hidden;
  cursor: grab;

  &:active { cursor: grabbing; }
}

.fp-content {
  position: absolute;
  top: 0;
  left: 0;
  transform-origin: 0 0;
}

// 裸画布背景透明，网格由预览容器统一绘制，缩小时不会露出内容边界的色块
.flow-preview :deep(.designer-canvas--bare) {
  background: transparent;
}

// 预览只做纯展示：节点/连线不响应点选，光标统一为拖拽样式
.flow-preview :deep(.cn-node) {
  cursor: inherit;
  pointer-events: none;
}

.flow-preview :deep(.cn-lines path),
.flow-preview :deep(.cn-line-label),
.flow-preview :deep(.cn-line-condition) {
  pointer-events: none;
}

.fp-tools {
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

  .fp-pct {
    min-width: 46px;
    font-variant-numeric: tabular-nums;
  }
}

.fp-sep {
  width: 1px;
  height: 14px;
  margin: 0 2px;
  background: $border-color-light;
}
</style>

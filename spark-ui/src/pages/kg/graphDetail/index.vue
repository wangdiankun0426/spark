<template>
  <div class="graph-detail-container">
    <!-- 顶部 header -->
    <div class="detail-header">
      <div class="detail-title">
        <el-icon class="detail-title-icon"><Connection /></el-icon>
        <span>{{ graph.name || '图谱详情' }}</span>
      </div>
      <div class="detail-meta">
        <el-tag size="small" effect="light">实体 {{ entityTotal }}</el-tag>
        <el-tag size="small" effect="light">关系 {{ relationList.length }}</el-tag>
      </div>
    </div>

    <!-- 主体：左侧实体列表 + 右侧图谱 -->
    <div class="detail-body">
      <!-- 左侧实体列表 -->
      <div class="entity-panel">
        <div class="entity-panel-header">
          <el-input
              v-model="keyword"
              placeholder="搜索实体名称"
              clearable
              :prefix-icon="Search"
              size="small"
          />
        </div>
        <div ref="entityListRef" class="entity-list" @scroll="handleEntityScroll">
          <div
              v-for="item in entityList"
              :key="item.id"
              class="entity-item"
              :class="{ 'entity-item-active': selectedEntityId === item.id }"
              @click="handleLocateEntity(item)"
          >
            <span
                class="entity-type-dot"
                :style="{ backgroundColor: getTypeColor(item.type) }"
            ></span>
            <span class="entity-item-name" :title="item.name">{{ item.name }}</span>
            <span
                class="entity-item-type"
                :style="{
                  color: getTypeColor(item.type),
                  backgroundColor: getTypeColor(item.type) + '1f'
                }"
            >
              {{ item.type || '未分类' }}
            </span>
          </div>
          <el-empty
              v-if="!entityList.length && !entityLoading"
              :image-size="60"
              description="暂无实体"
          />
          <div v-if="entityLoading" class="entity-load-tip">加载中...</div>
          <div v-else-if="entityFinished && entityList.length" class="entity-load-tip">没有更多了</div>
        </div>
      </div>

      <!-- 右侧图谱 -->
      <div class="graph-canvas" ref="canvasRef"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { Connection, Search } from '@element-plus/icons-vue'
import { queryGraphDetailAPI } from '@/api/kg/graph.js'
import { queryDocumentDetailAPI } from '@/api/kb/document.js'
import { pageEntityListAPI } from '@/api/kg/entity.js'
import { pageRelationListAPI } from '@/api/kg/relation.js'

const route = useRoute()
const graphId = route.query.graphId
const docId = route.query.docId

// 根据入口参数构造查询参数：docId 走 sourceId/docId，graphId 走 graphId/id
const listParam = docId ? { sourceId: docId } : { graphId }

const graph = ref({})
const entityList = ref([])
const relationList = ref([])
const keyword = ref('')
const selectedEntityId = ref(null)
const canvasRef = ref(null)
const entityListRef = ref(null)
let chart = null

// 实体分页状态
const entityPageNo = ref(1)
const entityPageSize = 30
const entityTotal = ref(0)
const entityLoading = ref(false)
const entityFinished = ref(false)

// 关键字搜索防抖句柄
let searchTimer = null

// 实体类型 -> 颜色 映射，按出现顺序分配调色板
const typeColorMap = new Map()
const palette = [
  '#4f8cf7', '#34c759', '#af52de', '#ff9500',
  '#00c7be', '#ff6b9d', '#5e72e4', '#fbbf24',
  '#10b981', '#f43f5e'
]

onMounted(() => {
  loadAll()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  if (chart) {
    chart.dispose()
    chart = null
  }
})

/**
 * 关键字搜索防抖，300ms 后重置分页重新加载
 */
watch(keyword, () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    loadEntityList(true)
  }, 300)
})

function handleResize() {
  if (chart) chart.resize()
}

/**
 * 并行拉取图谱信息与关系列表（关系全量，用于图谱连线），再加载第一页实体
 */
async function loadAll() {
  if (!graphId && !docId) return
  const detailPromise = docId
    ? queryDocumentDetailAPI({ id: docId })
    : queryGraphDetailAPI({ id: graphId })
  const [detailRes, relationRes] = await Promise.all([
    detailPromise,
    pageRelationListAPI({ page: false, ...listParam })
  ])
  if (detailRes.code === 200) graph.value = detailRes.data || {}
  if (relationRes.code === 200) relationList.value = relationRes.data?.rows || []
  await loadEntityList(true)
  window.addEventListener('resize', handleResize)
}

/**
 * 加载实体列表（分页累积），reset=true 时重置分页从头加载
 * @param {boolean} reset
 */
async function loadEntityList(reset = false) {
  if (!graphId && !docId) return
  if (entityLoading.value) return
  if (reset) {
    entityPageNo.value = 1
    entityFinished.value = false
    entityList.value = []
    selectedEntityId.value = null
  }
  if (entityFinished.value) return
  entityLoading.value = true
  const params = {
    ...listParam,
    pageNo: entityPageNo.value,
    pageSize: entityPageSize
  }
  if (keyword.value) params.name = keyword.value
  const res = await pageEntityListAPI(params)
  if (res.code === 200 && res.data) {
    const rows = res.data.rows || []
    entityList.value.push(...rows)
    entityTotal.value = res.data.total || 0
    if (entityList.value.length >= entityTotal.value || rows.length === 0) {
      entityFinished.value = true
    } else {
      entityPageNo.value++
    }
    await nextTick()
    renderChart()
  }
  entityLoading.value = false
}

/**
 * 实体列表滚动触底加载下一页
 * @param e
 */
function handleEntityScroll(e) {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  if (scrollHeight - scrollTop - clientHeight < 50) {
    loadEntityList(false)
  }
}

/**
 * 按实体类型分配稳定颜色
 * @param type
 */
function getTypeColor(type) {
  const key = type || '未分类'
  if (!typeColorMap.has(key)) {
    typeColorMap.set(key, palette[typeColorMap.size % palette.length])
  }
  return typeColorMap.get(key)
}

/**
 * 渲染 ECharts 力导向图（首次初始化 + 后续追加均用此函数）
 */
function renderChart() {
  if (!canvasRef.value) return
  if (!chart) {
    chart = echarts.init(canvasRef.value)
  }

  const nodes = entityList.value.map(e => ({
    id: String(e.id),
    name: e.name,
    symbolSize: 34,
    category: e.type || '未分类',
    itemStyle: { color: getTypeColor(e.type) },
    raw: e
  }))
  const links = relationList.value.map(r => ({
    source: String(r.headEntityId),
    target: String(r.tailEntityId),
    value: r.relationType,
    label: {
      show: true,
      formatter: r.relationType,
      fontSize: 11,
      color: '#5e6c84'
    },
    lineStyle: { color: '#c0c8d4', width: 1.5, curveness: 0.15 }
  }))
  const categoriesSet = new Set()
  entityList.value.forEach(e => categoriesSet.add(e.type || '未分类'))
  const categories = Array.from(categoriesSet).map(t => ({
    name: t,
    itemStyle: { color: getTypeColor(t) }
  }))

  const option = {
    tooltip: {
      formatter: p => {
        if (p.dataType === 'node') {
          const d = p.data.raw || {}
          const desc = d.description ? `<br/>描述：${d.description}` : ''
          return `<b>${d.name}</b><br/>类型：${d.type || '-'}${desc}`
        }
        if (p.dataType === 'edge') {
          return `关系：${p.data.value || '-'}`
        }
        return ''
      }
    },
    legend: {
      type: 'scroll',
      data: categories.map(c => c.name),
      orient: 'vertical',
      right: 10,
      top: 10,
      textStyle: { fontSize: 12 }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      roam: true,
      draggable: true,
      categories,
      data: nodes,
      links,
      label: { show: true, position: 'right', fontSize: 12 },
      lineStyle: { color: '#c0c8d4', curveness: 0.15 },
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 3 },
        label: { fontSize: 14, fontWeight: 'bold' }
      },
      force: {
        repulsion: 240,
        edgeLength: [80, 180],
        gravity: 0.08
      }
    }]
  }
  chart.setOption(option)
}

/**
 * 点击左侧实体，定位到右侧图谱节点
 * @param entity
 */
function handleLocateEntity(entity) {
  selectedEntityId.value = entity.id
  if (!chart) return
  const idx = entityList.value.findIndex(e => e.id === entity.id)
  if (idx < 0) return
  chart.dispatchAction({ type: 'downplay', seriesIndex: 0 })
  chart.dispatchAction({ type: 'highlight', seriesIndex: 0, dataIndex: idx })
  chart.dispatchAction({ type: 'focusNodeAdjacency', seriesIndex: 0, dataIndex: idx })
  chart.dispatchAction({ type: 'showTip', seriesIndex: 0, dataIndex: idx })
}
</script>

<style scoped lang="scss">
.graph-detail-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - #{$nav-height});
  background-color: $bg-page;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md $spacing-lg;
  background-color: $bg-card;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-card;
}

.detail-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  flex: 1;
  font-size: 18px;
  font-weight: 700;
  color: $color-text-primary;
}

.detail-title-icon {
  font-size: 22px;
  color: $color-primary;
}

.detail-meta {
  display: flex;
  gap: $spacing-sm;
}

.detail-body {
  flex: 1;
  display: flex;
  margin-top: $spacing-md;
  background-color: $bg-card;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-card;
  overflow: hidden;
}

.entity-panel {
  width: 280px;
  border-right: 1px solid $border-color-light;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.entity-panel-header {
  padding: $spacing-md;
  border-bottom: 1px solid $border-color-light;
}

.entity-list {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-xs;
}

.entity-load-tip {
  padding: $spacing-sm 0;
  text-align: center;
  font-size: 12px;
  color: $color-text-placeholder;
}

.entity-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-sm;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    background-color: $color-primary-soft;
  }
}

.entity-item-active {
  background-color: $color-primary-soft;

  .entity-item-name {
    color: $color-primary;
    font-weight: 600;
  }
}

.entity-type-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.entity-item-name {
  flex: 1;
  font-size: 13px;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.entity-item-type {
  flex-shrink: 0;
  padding: 2px $spacing-sm;
  font-size: 11px;
  font-weight: 500;
  border-radius: $border-radius-sm;
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.graph-canvas {
  flex: 1;
  min-width: 0;
}
</style>

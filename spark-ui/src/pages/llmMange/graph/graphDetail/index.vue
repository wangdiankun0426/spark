<template>
  <div class="graph-detail-container">
    <!-- 顶部 header -->
    <div class="detail-header">
      <div class="detail-header-left">
        <el-button text @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
        <el-divider direction="vertical" />
        <div class="detail-header-title">
          <el-icon class="detail-header-title-icon"><Connection /></el-icon>
          <span>{{ graph.name || '图谱详情' }}</span>
        </div>
      </div>
      <div class="detail-header-right">
        <el-tag size="small" effect="light">实体 {{ entityTotal }}</el-tag>
        <el-tag size="small" effect="light">关系 {{ relationList.length }}</el-tag>
        <el-tag size="small" effect="light" v-if="graphStats.typeDistribution">类型 {{ Object.keys(graphStats.typeDistribution).length }}种</el-tag>
        <el-tag size="small" effect="light" v-if="graphStats.componentCount >= 0">连通分量 {{ graphStats.componentCount }}</el-tag>
      </div>
    </div>

    <!-- 主体：左侧实体列表 + 中间图谱 + 右侧详情面板 -->
    <div class="detail-body">
      <!-- 左侧面板 -->
      <div class="entity-panel">
        <el-tabs v-model="leftTab" class="panel-tabs">
          <el-tab-pane label="实体" name="entity">
            <div class="panel-tab-header">
              <el-input v-model="keyword" placeholder="搜索实体名称" clearable :prefix-icon="Search" size="small" />
            </div>
            <!-- 类型筛选 -->
            <div class="type-filter" v-if="typeOptions.length > 1">
              <el-checkbox v-model="typeAll" @change="handleTypeAllChange">全选</el-checkbox>
              <el-checkbox-group v-model="selectedTypes" @change="handleTypeFilterChange" class="type-checkbox-group">
                <el-checkbox v-for="t in typeOptions" :key="t" :label="t">
                  <span class="type-dot" :style="{ backgroundColor: getTypeColor(t) }"></span>
                  {{ t }}
                </el-checkbox>
              </el-checkbox-group>
            </div>
            <div ref="entityListRef" class="entity-list" @scroll="handleEntityScroll">
              <div
                  v-for="item in filteredEntityList"
                  :key="item.id"
                  class="entity-item"
                  :class="{ 'entity-item-active': selectedEntityId === item.id }"
                  @click="handleLocateEntity(item)"
                  @dblclick="handleOpenDetail(item)"
              >
                <span class="entity-type-dot" :style="{ backgroundColor: getTypeColor(item.type) }"></span>
                <span class="entity-item-name" :title="item.name">{{ item.name }}</span>
                <span class="entity-item-type" :style="{ color: getTypeColor(item.type), backgroundColor: getTypeColor(item.type) + '1f' }">
                  {{ item.type || '未分类' }}
                </span>
              </div>
              <el-empty v-if="!filteredEntityList.length && !entityLoading" :image-size="60" description="暂无实体" />
              <div v-if="entityLoading" class="entity-load-tip">加载中...</div>
              <div v-else-if="entityFinished && entityList.length" class="entity-load-tip">没有更多了</div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      <!-- 中间图谱 + 工具栏 -->
      <div class="graph-main">
        <div class="graph-toolbar">
          <el-select
              v-model="layoutType"
              style="width: 200px"
              @change="handleLayoutChange"
          >
            <el-option label="力导向" value="force" />
            <el-option label="圆形" value="circular" />
            <el-option label="层次" value="dagre" />
          </el-select>
          <el-button size="small" @click="handleFitView">适配</el-button>
          <el-button size="small" @click="handleResetExpand" v-if="expandedNodeIds.size > 0">重置展开</el-button>
          <span class="toolbar-tip" v-if="entityTotal > MAX_GRAPH_NODES">已显示前 {{ MAX_GRAPH_NODES }} 个节点，共 {{ entityTotal }} 个实体</span>
        </div>
        <div class="graph-canvas" ref="canvasRef"></div>
      </div>
      <!-- 右侧详情面板 -->
      <div class="detail-panel" v-if="detailVisible">
        <div class="detail-panel-header">
          <span class="detail-panel-title">实体详情</span>
          <el-icon class="detail-panel-close" @click="detailVisible = false"><Close /></el-icon>
        </div>
        <div class="detail-panel-body">
          <div class="detail-row">
            <span class="detail-label">名称</span>
            <span class="detail-value">{{ detailEntity.name }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">类型</span>
            <span class="detail-value">{{ detailEntity.type || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">描述</span>
            <span class="detail-value">{{ detailEntity.description || '-' }}</span>
          </div>
          <div class="detail-sub-title">关联实体 ({{ detailNeighbors.length }})</div>
          <div class="detail-neighbor" v-for="n in detailNeighbors" :key="n.id" @click="handleLocateEntity(n)">
            <span class="neighbor-dot" :style="{ backgroundColor: getTypeColor(n.type) }"></span>
            <span class="neighbor-name">{{ n.name }}</span>
          </div>
          <el-empty v-if="!detailNeighbors.length" :image-size="40" description="暂无关联" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import {useRoute, useRouter} from 'vue-router'
import * as echarts from 'echarts'
import {Connection, Search, Close, ArrowLeft} from '@element-plus/icons-vue'
import { queryGraphDetailAPI, expandNodeAPI, queryGraphStatsAPI } from '@/api/kg/graph.js'
import { queryDocumentDetailAPI } from '@/api/dms/document.js'
import { pageEntityListAPI } from '@/api/kg/entity.js'
import { pageRelationListAPI } from '@/api/kg/relation.js'

const route = useRoute()
const graphId = route.query.graphId
const docId = route.query.docId
const MAX_GRAPH_NODES = 500
const listParam = docId ? { sourceId: docId } : { graphId }

const graph = ref({})
const entityList = ref([])
const relationList = ref([])
const keyword = ref('')
const selectedEntityId = ref(null)
const canvasRef = ref(null)
const entityListRef = ref(null)
const graphStats = ref({})
let chart = null

// 布局类型
const layoutType = ref('force')

// 实体分页状态
const entityPageNo = ref(1)
const entityPageSize = 30
const entityTotal = ref(0)
const entityLoading = ref(false)
const entityFinished = ref(false)
let searchTimer = null

// 左侧标签页
const leftTab = ref('entity')

// 类型筛选
const selectedTypes = ref([])
const typeAll = ref(true)
const typeOptions = ref([])

// 节点展开
const expandedNodeIds = ref(new Set())

// 详情面板
const detailVisible = ref(false)
const detailEntity = ref({})
const detailNeighbors = ref([])

// 实体类型 -> 颜色
const typeColorMap = new Map()
const palette = [
  '#4f8cf7', '#34c759', '#af52de', '#ff9500',
  '#00c7be', '#ff6b9d', '#5e72e4', '#fbbf24',
  '#10b981', '#f43f5e'
]

/**
 * 类型筛选后的实体列表
 */
const filteredEntityList = computed(() => {
  if (selectedTypes.value.length === typeOptions.value.length) {
    return entityList.value
  }
  return entityList.value.filter(e => selectedTypes.value.includes(e.type || '未分类'))
})

/**
 * 按实体类型分配稳定颜色
 */
function getTypeColor(type) {
  const key = type || '未分类'
  if (!typeColorMap.has(key)) {
    typeColorMap.set(key, palette[typeColorMap.size % palette.length])
  }
  return typeColorMap.get(key)
}

onMounted(() => {
  loadAll()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (searchTimer) clearTimeout(searchTimer)
  if (chart) { chart.dispose(); chart = null }
})

watch(keyword, () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => loadEntityList(true), 300)
})

function handleResize() {
  if (chart) chart.resize()
}

const router = useRouter();

/**
 * 返回上一页
 */
function handleBack() {
  router.back();
}

/**
 * 并行拉取图谱信息、关系列表、统计信息，再加载第一页实体
 */
async function loadAll() {
  if (!graphId && !docId) return
  const detailPromise = docId
    ? queryDocumentDetailAPI({ id: docId })
    : queryGraphDetailAPI({ id: graphId })
  const [detailRes, relationRes, statsRes] = await Promise.all([
    detailPromise,
    pageRelationListAPI({ page: false, ...listParam }),
    graphId ? queryGraphStatsAPI(graphId).catch(() => ({ data: {} })) : Promise.resolve({ data: {} })
  ])
  if (detailRes.code === 200) graph.value = detailRes.data || {}
  if (relationRes.code === 200) relationList.value = relationRes.data?.rows || []
  if (statsRes.code === 200) graphStats.value = statsRes.data || {}
  await loadEntityList(true)
  window.addEventListener('resize', handleResize)
}

/**
 * 加载实体列表（分页累积）
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
  const params = { ...listParam, pageNo: entityPageNo.value, pageSize: entityPageSize }
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
    // 提取类型选项（首次加载时）
    if (entityPageNo.value === 1 || typeOptions.value.length === 0) {
      const typeSet = new Set()
      entityList.value.forEach(e => typeSet.add(e.type || '未分类'))
      typeOptions.value = Array.from(typeSet)
      if (selectedTypes.value.length === 0) {
        selectedTypes.value = [...typeOptions.value]
      }
    }
    await nextTick()
    renderChart()
  }
  entityLoading.value = false
}

/**
 * 实体列表滚动触底加载下一页
 */
function handleEntityScroll(e) {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  if (scrollHeight - scrollTop - clientHeight < 50) {
    loadEntityList(false)
  }
}

/**
 * 类型筛选：全选
 */
function handleTypeAllChange(val) {
  selectedTypes.value = val ? [...typeOptions.value] : []
  renderChart()
}

/**
 * 类型筛选：单选变化
 */
function handleTypeFilterChange() {
  typeAll.value = selectedTypes.value.length === typeOptions.value.length
  renderChart()
}

/**
 * 布局切换
 */
function handleLayoutChange() {
  renderChart()
}

/**
 * 适配画布
 */
function handleFitView() {
  if (chart) chart.dispatchAction({ type: 'restore' })
}

/**
 * 重置展开
 */
async function handleResetExpand() {
  expandedNodeIds.value.clear()
  await loadEntityList(true)
}

/**
 * 按需展开节点的关联实体
 */
async function handleExpandNode(entity) {
  if (!entity || !entity.id || expandedNodeIds.value.has(entity.id)) return
  const res = await expandNodeAPI({ nodeId: entity.id, depth: 1 })
  if (res.code !== 200 || !res.data) return
  const { nodes: newNodes, edges: newEdges, degrees } = res.data
  if (!newNodes || !newNodes.length) return
  expandedNodeIds.value.add(entity.id)
  // 增量追加节点（去重）
  const existingIds = new Set(entityList.value.map(e => e.id))
  const toAdd = newNodes.filter(n => !existingIds.has(n.id))
  if (toAdd.length > 0) {
    entityList.value.push(...toAdd)
    // 补充类型选项
    const typeSet = new Set(typeOptions.value)
    toAdd.forEach(e => typeSet.add(e.type || '未分类'))
    typeOptions.value = Array.from(typeSet)
    if (selectedTypes.value.length < typeOptions.value.length) {
      selectedTypes.value = [...typeOptions.value]
    }
  }
  // 增量追加边（去重）
  const existingEdgeKeys = new Set(relationList.value.map(r => `${r.headEntityId}-${r.tailEntityId}-${r.relationType}`))
  const edgesToAdd = (newEdges || []).filter(e => !existingEdgeKeys.has(`${e.headEntityId}-${e.tailEntityId}-${e.relationType}`))
  relationList.value.push(...edgesToAdd)
  // 记录节点关联度
  if (degrees) {
    Object.entries(degrees).forEach(([id, deg]) => {
      nodeDegreeMap.value[Number(id)] = deg
    })
  }
  renderChart()
}

/**
 * 节点关联度映射（用于缩放大小）
 */
const nodeDegreeMap = ref({})

/**
 * 渲染 ECharts 图谱
 */
function renderChart() {
  if (!canvasRef.value) return
  if (!chart) {
    chart = echarts.init(canvasRef.value)
    // 监听节点点击：展开关联实体
    chart.on('click', (params) => {
      if (params.dataType === 'node' && params.data) {
        const entity = params.data.raw
        if (entity) {
          handleExpandNode(entity)
        }
      }
    })
    // 监听节点双击：打开详情面板
    chart.on('dblclick', (params) => {
      if (params.dataType === 'node' && params.data) {
        const entity = params.data.raw
        if (entity) {
          handleOpenDetail(entity)
        }
      }
    })
  }
  // 类型筛选过滤
  const visibleEntities = filteredEntityList.value
  // 大数据量采样
  let renderEntities = visibleEntities
  if (visibleEntities.length > MAX_GRAPH_NODES) {
    renderEntities = visibleEntities.slice(0, MAX_GRAPH_NODES)
  }
  const renderIds = new Set(renderEntities.map(e => e.id))
  const nodes = renderEntities.map(e => {
    const degree = nodeDegreeMap.value[e.id] || 0
    return {
      id: String(e.id),
      name: e.name,
      symbolSize: Math.max(20, Math.min(60, 20 + degree * 3)),
      category: e.type || '未分类',
      itemStyle: {
        color: getTypeColor(e.type),
        borderColor: expandedNodeIds.value.has(e.id) ? '#333' : undefined,
        borderWidth: expandedNodeIds.value.has(e.id) ? 3 : undefined
      },
      raw: e
    }
  })
  const links = relationList.value
    .filter(r => renderIds.has(r.headEntityId) && renderIds.has(r.tailEntityId))
    .map(r => ({
      source: String(r.headEntityId),
      target: String(r.tailEntityId),
      value: r.relationType,
      label: { show: true, formatter: r.relationType, fontSize: 11, color: '#5e6c84' },
      lineStyle: { color: '#c0c8d4', width: 1.5, curveness: 0.15 }
    }))
  const categoriesSet = new Set()
  renderEntities.forEach(e => categoriesSet.add(e.type || '未分类'))
  const categories = Array.from(categoriesSet).map(t => ({ name: t, itemStyle: { color: getTypeColor(t) } }))
  const seriesOption = {
    type: 'graph',
    layout: layoutType.value,
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
    }
  }
  if (layoutType.value === 'force') {
    seriesOption.force = { repulsion: 240, edgeLength: [80, 180], gravity: 0.08 }
  }
  const option = {
    tooltip: {
      formatter: p => {
        if (p.dataType === 'node') {
          const d = p.data.raw || {}
          const desc = d.description ? `<br/>描述：${d.description}` : ''
          const deg = nodeDegreeMap.value[d.id]
          const degStr = deg != null ? `<br/>关联度：${deg}` : ''
          return `<b>${d.name}</b><br/>类型：${d.type || '-'}${desc}${degStr}`
        }
        if (p.dataType === 'edge') return `关系：${p.data.value || '-'}`
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
    series: [seriesOption]
  }
  chart.setOption(option, true)
}

/**
 * 点击左侧实体，定位到图谱节点
 */
function handleLocateEntity(entity) {
  selectedEntityId.value = entity.id
  if (!chart) return
  const idx = filteredEntityList.value.findIndex(e => e.id === entity.id)
  if (idx < 0) return
  chart.dispatchAction({ type: 'downplay', seriesIndex: 0 })
  chart.dispatchAction({ type: 'highlight', seriesIndex: 0, dataIndex: idx })
  chart.dispatchAction({ type: 'focusNodeAdjacency', seriesIndex: 0, dataIndex: idx })
  chart.dispatchAction({ type: 'showTip', seriesIndex: 0, dataIndex: idx })
}

/**
 * 双击实体打开详情面板
 */
function handleOpenDetail(entity) {
  detailEntity.value = entity
  detailVisible.value = true
  // 计算关联实体
  const id = entity.id
  const neighbors = []
  relationList.value.forEach(r => {
    if (r.headEntityId === id) {
      const target = entityList.value.find(e => e.id === r.tailEntityId)
      if (target) neighbors.push(target)
    } else if (r.tailEntityId === id) {
      const source = entityList.value.find(e => e.id === r.headEntityId)
      if (source) neighbors.push(source)
    }
  })
  detailNeighbors.value = neighbors
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
.detail-header-left {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}
.detail-header-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  flex: 1;
  font-size: 18px;
  font-weight: 700;
  color: $color-text-primary;
}

.detail-header-title-icon {
  font-size: 22px;
  color: $color-primary;
}

.detail-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.detail-body {
  height: calc(100vh - 150px);
  display: flex;
  margin-top: $spacing-md;
  background-color: $bg-card;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-card;
  overflow: hidden;
}

/* 左侧面板 */
.entity-panel {
  width: 280px;
  border-right: 1px solid $border-color-light;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.panel-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: hidden;
  height: 0;
}

.panel-tabs :deep(.el-tab-pane) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.panel-tab-header {
  padding: $spacing-sm;
}

.type-filter {
  padding: $spacing-sm $spacing-md;
  border-bottom: 1px solid $border-color-light;
  max-height: 120px;
  overflow-y: auto;
}

.type-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 4px;
}

.type-checkbox-group :deep(.el-checkbox) {
  margin: 0;
  height: 24px;
}

.type-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 4px;
  vertical-align: middle;
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
  font-weight: 400;
  border-radius: $border-radius-sm;
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 中间图谱区域 */
.graph-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.graph-toolbar {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  border-bottom: 1px solid $border-color-light;
  flex-shrink: 0;
}

.toolbar-tip {
  font-size: 12px;
  color: $color-text-placeholder;
  margin-left: auto;
}

.graph-canvas {
  flex: 1;
  min-height: 0;
}

/* 右侧详情面板 */
.detail-panel {
  width: 280px;
  border-left: 1px solid $border-color-light;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.detail-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-sm;
  border-bottom: 1px solid $border-color-light;
}

.detail-panel-title {
  font-size: 14px;
  font-weight: 600;
  color: $color-text-primary;
}

.detail-panel-close {
  cursor: pointer;
  color: $color-text-placeholder;
  font-size: 18px;

  &:hover {
    color: $color-text-primary;
  }
}

.detail-panel-body {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-md;
}

.detail-row {
  display: flex;
  font-size: 13px;
  margin-bottom: $spacing-sm;
}

.detail-label {
  width: 60px;
  flex-shrink: 0;
  color: $color-text-secondary;
}

.detail-value {
  flex: 1;
  color: $color-text-primary;
  word-break: break-all;
}

.detail-sub-title {
  font-size: 13px;
  font-weight: 600;
  color: $color-text-primary;
  margin: $spacing-md 0 $spacing-sm 0;
  padding-top: $spacing-sm;
  border-top: 1px solid $border-color-light;
}

.detail-neighbor {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: 4px $spacing-sm;
  font-size: 12px;
  color: $color-text-primary;
  cursor: pointer;
  border-radius: $border-radius-sm;

  &:hover {
    background-color: $color-primary-soft;
  }
}

.neighbor-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.neighbor-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

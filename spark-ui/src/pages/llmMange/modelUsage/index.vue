<template>
  <div class="app-container usage-page">
    <div class="usage-panel">
      <div class="usage-toolbar">
        <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD HH:mm:ss"
            :shortcuts="dateShortcuts"
        />
        <el-select
            v-model="typeFilter"
            placeholder="模型类型"
            clearable
            style="width: 300px"
        >
          <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
        <el-select
            v-model="sceneFilter"
            placeholder="调用场景"
            clearable
            filterable
            style="width: 300px"
        >
          <el-option
              v-for="item in sceneOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </div>

      <div class="usage-overview">
        <div class="usage-chart-card">
          <div ref="chartRef" class="usage-chart"></div>
        </div>
        <div class="usage-stat-aside">
          <stat-card
              class="usage-stat-item"
              label="调用次数"
              :value="formatNumber(stat.callCount)"
              unit="次"
              type="primary"
          />
          <stat-card
              class="usage-stat-item"
              label="总 Token"
              :value="formatNumber(stat.totalTokens)"
              unit="tokens"
          />
          <stat-card
              class="usage-stat-item"
              label="输入 Token"
              :value="formatNumber(stat.inputTokens)"
              unit="tokens"
              type="success"
          />
          <stat-card
              class="usage-stat-item"
              label="输出 Token"
              :value="formatNumber(stat.outputTokens)"
              unit="tokens"
              type="warning"
          />
        </div>
      </div>

      <div class="usage-table-card">
        <div class="usage-table-fill">
          <el-table
              :data="tableList"
              v-loading="tableLoading"
              size="small"
              height="100%"
          >
            <el-table-column prop="createdDt" label="时间" width="170"/>
            <el-table-column prop="modelName" label="模型" min-width="160" show-overflow-tooltip/>
            <el-table-column label="类型" width="100" align="center">
              <template #default="scope">
                <el-tag :type="tagType(scope.row.type)" size="small">{{ scope.row.typeName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sceneName" label="场景" width="130" show-overflow-tooltip/>
            <el-table-column prop="userName" label="使用人" width="110" show-overflow-tooltip/>
            <el-table-column prop="inputTokens" label="输入Token" width="110" align="right"/>
            <el-table-column prop="outputTokens" label="输出Token" width="110" align="right"/>
            <el-table-column prop="totalTokens" label="总Token" width="110" align="right"/>
          </el-table>
        </div>
        <el-pagination
            class="usage-pagination"
            :current-page="query.pageNo"
            :page-size="query.pageSize"
            :page-sizes="[15, 30, 50]"
            :background="true"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import {
  pageModelUsageListAPI,
  queryModelUsageSummaryAPI,
  queryModelUsageTrendAPI
} from '@/api/llm/modelUsage.js'
import StatCard from '@/pages/siteManage/monitor/components/StatCard.vue'
import { useChart } from '@/utils/useChart'

const TYPE_LANGUAGE = 1
const TYPE_VECTOR = 2
const TYPE_RERANK = 3

const typeOptions = [
  { value: TYPE_LANGUAGE, label: '语言模型' },
  { value: TYPE_VECTOR, label: '向量模型' },
  { value: TYPE_RERANK, label: '排序模型' }
]

const sceneOptions = [
  { value: 'CHAT', label: '模型对话' },
  { value: 'AGENT_CHAT', label: '智能体对话' },
  { value: 'WORKFLOW_LLM', label: '工作流模型节点' },
  { value: 'WORKFLOW_AGENT', label: '工作流智能体节点' },
  { value: 'KG_REASON', label: '图谱推理' },
  { value: 'KG_EXTRACT', label: '图谱抽取' },
  { value: 'KG_ENTITY_EXTRACT', label: '提问实体抽取' },
  { value: 'QA_GENERATE', label: '问答对生成' },
  { value: 'DOCUMENT_VECTOR', label: '文档向量化' },
  { value: 'RETRIEVE_VECTOR', label: '检索向量化' },
  { value: 'RETRIEVE_RERANK', label: '检索重排' },
  { value: 'KG_ENTITY_VECTOR', label: '图谱实体向量化' },
  { value: 'OTHER', label: '其他' }
]

const dateShortcuts = [
  { text: '近7天', value: () => shortcutRange(6) },
  { text: '近30天', value: () => shortcutRange(29) },
  { text: '近90天', value: () => shortcutRange(89) }
]

const dateRange = ref(rangeByDays(6))
const typeFilter = ref(null)
const sceneFilter = ref(null)

const tableLoading = ref(false)
const tableList = ref([])
const total = ref(0)
const query = reactive({ pageNo: 1, pageSize: 15 })

const stat = reactive({ callCount: 0, inputTokens: 0, outputTokens: 0, totalTokens: 0 })

const { elRef: chartRef, setOption } = useChart()

let reloadTimer = null

onMounted(() => {
  loadSummary()
  loadTrend()
  loadTable()
})

watch([dateRange, typeFilter, sceneFilter], () => {
  if (reloadTimer) clearTimeout(reloadTimer)
  reloadTimer = setTimeout(() => {
    query.pageNo = 1
    loadSummary()
    loadTrend()
    loadTable()
  }, 300)
})

/**
 * 构造最近n天的起止时间字符串
 * @param days 往前推的天数，含今天
 */
function rangeByDays(days) {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - days)
  return [`${formatDate(start)} 00:00:00`, `${formatDate(end)} 23:59:59`]
}

/**
 * 日期快捷选项，返回Date对象
 * @param days 往前推的天数，含今天
 */
function shortcutRange(days) {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - days)
  return [start, end]
}

function formatDate(date) {
  const pad = num => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function buildParams() {
  return {
    startTime: dateRange.value ? dateRange.value[0] : undefined,
    endTime: dateRange.value ? dateRange.value[1] : undefined,
    type: typeFilter.value || undefined,
    scene: sceneFilter.value || undefined
  }
}

/**
 * 加载类型汇总并累加概览卡片
 */
function loadSummary() {
  queryModelUsageSummaryAPI(buildParams()).then(res => {
    if (res.code !== 200) return
    const list = res.data || []
    stat.callCount = list.reduce((sum, item) => sum + (item.callCount || 0), 0)
    stat.inputTokens = list.reduce((sum, item) => sum + (item.inputTokens || 0), 0)
    stat.outputTokens = list.reduce((sum, item) => sum + (item.outputTokens || 0), 0)
    stat.totalTokens = list.reduce((sum, item) => sum + (item.totalTokens || 0), 0)
  })
}

/**
 * 加载每日趋势并按类型拼装三条折线
 */
function loadTrend() {
  queryModelUsageTrendAPI(buildParams()).then(res => {
    if (res.code !== 200) return
    const labels = buildDateLabels()
    const valueMap = new Map()
    ;(res.data || []).forEach(item => {
      valueMap.set(`${item.dt}_${item.type}`, item.totalTokens || 0)
    })
    const seriesList = typeOptions.map((item, index) => ({
      name: item.label,
      color: ['#004fc5', '#34c759', '#ff9500'][index],
      data: labels.map(dt => valueMap.get(`${dt}_${item.value}`) || 0)
    }))
    setOption(buildLineOption(labels, seriesList))
  })
}

/**
 * 按当前筛选日期范围生成连续日期轴
 */
function buildDateLabels() {
  if (!dateRange.value) return []
  const start = new Date(dateRange.value[0].replace(/-/g, '/'))
  const end = new Date(dateRange.value[1].replace(/-/g, '/'))
  const labels = []
  const cursor = new Date(start)
  while (cursor <= end && labels.length < 92) {
    labels.push(formatDate(cursor))
    cursor.setDate(cursor.getDate() + 1)
  }
  return labels
}

function buildLineOption(labels, seriesList) {
  return {
    tooltip: { trigger: 'axis', valueFormatter: value => `${value} tokens` },
    legend: { top: 0, right: 0, itemWidth: 12, textStyle: { fontSize: 12 } },
    grid: { left: 56, right: 16, top: 36, bottom: 24 },
    xAxis: { type: 'category', data: labels, axisLabel: { fontSize: 10 } },
    yAxis: { type: 'value', scale: true },
    series: seriesList.map(item => ({
      name: item.name,
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: item.data,
      itemStyle: { color: item.color },
      areaStyle: { opacity: 0.08 }
    }))
  }
}

/**
 * 加载明细分页
 */
function loadTable() {
  tableLoading.value = true
  pageModelUsageListAPI({ ...buildParams(), pageNo: query.pageNo, pageSize: query.pageSize }).then(res => {
    if (res.code === 200) {
      tableList.value = res.data.rows || []
      total.value = res.data.total || 0
    }
  }).finally(() => {
    tableLoading.value = false
  })
}

function handleSizeChange(size) {
  query.pageSize = size
  query.pageNo = 1
  loadTable()
}

function handleCurrentChange(pageNo) {
  query.pageNo = pageNo
  loadTable()
}

function tagType(type) {
  if (type === TYPE_LANGUAGE) return 'primary'
  if (type === TYPE_VECTOR) return 'success'
  if (type === TYPE_RERANK) return 'warning'
  return 'info'
}

function formatNumber(value) {
  return (value || 0).toLocaleString()
}
</script>

<style scoped lang="scss">
.usage-page {
  height: 100%;
}

.usage-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.usage-toolbar {
  flex: none;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md $spacing-lg;
  background-color: $bg-card;
  border: 1px solid $border-color-light;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.usage-overview {
  flex: none;
  display: flex;
  align-items: stretch;
  gap: $spacing-md;
}

.usage-stat-aside {
  flex: none;
  width: 18%;
  min-width: 220px;
  max-width: 260px;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.usage-stat-item {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: $spacing-sm $spacing-md;

  :deep(.stat-card__label) {
    font-size: 12px;
  }

  :deep(.stat-card__value) {
    margin-top: $spacing-xs;
    font-size: 22px;
  }
}

.usage-chart-card {
  flex: 1;
  min-width: 0;
  height: 340px;
  padding: $spacing-md $spacing-lg;
  background-color: $bg-card;
  border: 1px solid $border-color-light;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
  box-sizing: border-box;
}

.usage-chart {
  width: 100%;
  height: 100%;
}

.usage-table-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: $spacing-md $spacing-lg;
  background-color: $bg-card;
  border: 1px solid $border-color-light;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.usage-table-fill {
  flex: 1;
  min-height: 0;
}

.usage-pagination {
  flex: none;
  margin-top: $spacing-sm;
  justify-content: flex-end;
}
</style>

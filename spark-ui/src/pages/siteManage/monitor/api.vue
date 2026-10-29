<template>
  <div class="app-container monitor-page">
    <MonitorToolbar
      v-model:node-key="nodeKey"
      v-model:range="range"
      :node-list="nodeList"
      allow-all
    />

    <div class="monitor-stat-row">
      <StatCard label="总请求数" :value="summary?.totalCount ?? 0" unit="次" />
      <StatCard label="平均 QPS" :value="avgQps" unit="/s" />
      <StatCard label="平均响应耗时" :value="summary?.avgCost ?? 0" unit="ms" />
      <StatCard label="错误率" :value="errorRate" unit="%" :type="Number(errorRate) > 5 ? 'danger' : 'primary'" />
    </div>

    <el-row :gutter="16" class="monitor-chart-row">
      <el-col :span="16">
        <el-card shadow="never" class="monitor-card">
          <template #header>QPS / 平均响应耗时</template>
          <div ref="trendElRef" class="monitor-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="monitor-card">
          <template #header>状态码分布</template>
          <div ref="codeElRef" class="monitor-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <div class="monitor-stack">
      <el-card shadow="never" class="monitor-card is-fill">
        <template #header>
          <div class="monitor-card__header">
            <span>接口调用统计</span>
            <span class="monitor-card__extra">{{ rangeText }}</span>
          </div>
        </template>
        <div class="table-fill">
          <el-table :data="uriStats" size="small" height="100%">
            <el-table-column prop="uri" label="URI" min-width="260" show-overflow-tooltip />
            <el-table-column prop="method" label="方法" width="90" align="center" />
            <el-table-column prop="totalCount" label="调用次数" width="100" align="center" sortable />
            <el-table-column prop="avgCost" label="平均耗时" width="110" align="center" sortable>
              <template #default="scope">{{ formatMs(scope.row.avgCost) }}</template>
            </el-table-column>
            <el-table-column prop="maxCost" label="最大耗时" width="110" align="center" sortable>
              <template #default="scope">{{ formatMs(scope.row.maxCost) }}</template>
            </el-table-column>
            <el-table-column prop="errorCount" label="错误次数" width="100" align="center" />
            <el-table-column label="错误率" min-width="150">
              <template #default="scope">
                <el-progress
                  :percentage="Number(scope.row.errorRate) || 0"
                  :status="Number(scope.row.errorRate) > 5 ? 'exception' : ''"
                />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>

      <el-card v-if="nodeKey" shadow="never" class="monitor-card api-slow-card">
        <template #header>
          <div class="monitor-card__header">
            <span>慢 SQL Top20</span>
            <span class="monitor-card__extra">{{ nodeKey }}</span>
          </div>
        </template>
        <el-table :data="slowSqlList" size="small" height="240">
          <el-table-column prop="dataSource" label="数据源" width="120" align="center" />
          <el-table-column prop="sql" label="SQL" min-width="320" show-overflow-tooltip />
          <el-table-column prop="executeCount" label="执行次数" width="100" align="center" />
          <el-table-column label="总耗时" width="110" align="center">
            <template #default="scope">{{ formatMs(scope.row.totalTime) }}</template>
          </el-table-column>
          <el-table-column label="最大耗时" width="110" align="center">
            <template #default="scope">
              <span :class="{ 'is-danger-text': scope.row.maxTime >= 1000 }">{{ formatMs(scope.row.maxTime) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import StatCard from '@/components/Dashboard/StatCard.vue'
import MonitorToolbar from '@/components/MonitorToolbar/index.vue'
import { useChart } from '@/utils/useChart.js'
import { RANGE_OPTIONS, buildTimeRange, formatMs, parseJson } from '@/utils/monitorUtil.js'
import {
  queryNodeListAPI, queryApiSummaryAPI, queryApiTrendAPI, queryUriStatsAPI, queryLatestDruidAPI
} from '@/api/monitor/monitor.js'

const REFRESH_INTERVAL = 30000

const nodeList = ref([])
const nodeKey = ref('')
const range = ref('1h')
const summary = ref(null)
const trend = ref([])
const uriStats = ref([])
const slowSqlList = ref([])

const rangeText = computed(() => (RANGE_OPTIONS.find(item => item.value === range.value) || {}).label || '')
const avgQps = computed(() => {
  if (!summary.value?.totalCount) return 0
  const option = RANGE_OPTIONS.find(item => item.value === range.value) || RANGE_OPTIONS[0]
  return (summary.value.totalCount / (option.hours * 3600)).toFixed(2)
})
const errorRate = computed(() => {
  if (!summary.value?.totalCount) return '0.00'
  const errors = (summary.value.count4xx || 0) + (summary.value.count5xx || 0)
  return ((errors / summary.value.totalCount) * 100).toFixed(2)
})

const { elRef: trendElRef, setOption: setTrendOption } = useChart()
const { elRef: codeElRef, setOption: setCodeOption } = useChart()

let timer = null

onMounted(() => {
  queryNodeListAPI().then(res => {
    if (res.code === 200) nodeList.value = res.data || []
  })
  loadData()
  timer = setInterval(() => {
    if (range.value === '1h') loadData()
  }, REFRESH_INTERVAL)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})

watch([nodeKey, range], () => loadData())

/**
 * 加载汇总、趋势、URI统计与慢SQL
 */
function loadData() {
  const query = { ...buildTimeRange(range.value), nodeKey: nodeKey.value }
  queryApiSummaryAPI(query).then(res => {
    if (res.code === 200) summary.value = res.data
  })
  queryApiTrendAPI(query).then(res => {
    if (res.code !== 200) return
    trend.value = res.data || []
    renderTrendChart()
    renderCodeChart()
  })
  queryUriStatsAPI(query).then(res => {
    if (res.code === 200) uriStats.value = res.data || []
  })
  loadSlowSql()
}

/**
 * 加载节点慢SQL快照
 */
function loadSlowSql() {
  if (!nodeKey.value) {
    slowSqlList.value = []
    return
  }
  queryLatestDruidAPI({ nodeKey: nodeKey.value }).then(res => {
    if (res.code !== 200 || !res.data?.sqlJson) {
      slowSqlList.value = []
      return
    }
    const groupList = parseJson(res.data.sqlJson) || []
    slowSqlList.value = groupList.flatMap(group =>
      (group.sqlList || []).map(item => ({ ...item, dataSource: shortDataSourceName(group.name) }))
    )
  })
}

/**
 * 渲染QPS与平均耗时双轴图
 */
function renderTrendChart() {
  const data = trend.value
  const labels = data.map(item => item.snapshotDt?.substring(11, 16))
  setTrendOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0, right: 0, itemWidth: 12, textStyle: { fontSize: 12 } },
    grid: { left: 48, right: 52, top: 36, bottom: 28 },
    xAxis: { type: 'category', data: labels, axisLabel: { fontSize: 10 } },
    yAxis: [
      { type: 'value', name: 'QPS', scale: true },
      { type: 'value', name: 'ms', scale: true }
    ],
    series: [
      {
        name: 'QPS',
        type: 'bar',
        data: data.map(item => Number(((item.totalCount || 0) / 60).toFixed(2))),
        itemStyle: { color: '#004fc5' }
      },
      {
        name: '平均耗时',
        type: 'line',
        smooth: true,
        showSymbol: false,
        yAxisIndex: 1,
        data: data.map(item => item.avgCost),
        itemStyle: { color: '#ff9500' }
      }
    ]
  })
}

/**
 * 渲染状态码分布饼图
 */
function renderCodeChart() {
  const sum = key => trend.value.reduce((total, item) => total + (item[key] || 0), 0)
  setCodeOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { fontSize: 12 } },
    series: [{
      type: 'pie',
      radius: ['40%', '68%'],
      center: ['50%', '46%'],
      label: { show: false },
      data: [
        { name: '2xx', value: sum('count2xx'), itemStyle: { color: '#34c759' } },
        { name: '3xx', value: sum('count3xx'), itemStyle: { color: '#004fc5' } },
        { name: '4xx', value: sum('count4xx'), itemStyle: { color: '#e6a23c' } },
        { name: '5xx', value: sum('count5xx'), itemStyle: { color: '#f56c6c' } }
      ]
    }]
  })
}

/**
 * 从JDBC URL提取库名作为数据源显示名
 * @param url 连接地址
 * @returns {string}
 */
function shortDataSourceName(url) {
  if (!url) return ''
  const tail = url.substring(url.lastIndexOf('/') + 1)
  return tail.split('?')[0]
}
</script>

<style scoped lang="scss">
@use "@/styles/monitor.scss";

.api-slow-card {
  flex: none;
  height: auto;
}
</style>

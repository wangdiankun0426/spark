<template>
  <div class="app-container monitor-page">
    <MonitorToolbar
      v-model:node-key="nodeKey"
      v-model:range="range"
      :node-list="nodeList"
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
          <template #header><div class="monitor-card__title"><el-icon><DataLine /></el-icon>QPS / 平均响应耗时</div></template>
          <div ref="trendElRef" class="monitor-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="monitor-card">
          <template #header><div class="monitor-card__title"><el-icon><PieChart /></el-icon>状态码分布</div></template>
          <div ref="codeElRef" class="monitor-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <div class="monitor-stack">
      <el-card shadow="never" class="monitor-card is-fill">
        <template #header>
          <div class="monitor-card__header">
            <span class="monitor-card__title"><el-icon><Histogram /></el-icon>接口调用统计</span>
          </div>
        </template>
        <div class="table-fill uri-table-fill">
          <div class="uri-table-wrap">
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
          <el-pagination
              class="uri-pagination"
              v-model:current-page="uriQuery.pageNo"
              v-model:page-size="uriQuery.pageSize"
              :total="uriTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @current-change="loadUriStats"
              @size-change="handleUriSizeChange"
          />
        </div>
      </el-card>

      <el-card shadow="never" class="monitor-card api-slow-card">
        <template #header>
          <div class="monitor-card__header">
            <span class="monitor-card__title"><el-icon><Timer /></el-icon>慢 SQL</span>
          </div>
        </template>
        <div class="table-fill slow-table-fill">
          <div class="slow-table-wrap">
            <el-table :data="slowSqlPage" size="small" height="240">
              <el-table-column prop="nodeKey" label="节点" width="120" align="center" show-overflow-tooltip />
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
          </div>
          <el-pagination
              class="uri-pagination"
              v-model:current-page="slowSqlQuery.pageNo"
              v-model:page-size="slowSqlQuery.pageSize"
              :total="slowSqlList.length"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { DataLine, PieChart, Histogram, Timer } from '@element-plus/icons-vue'
import StatCard from '@/components/Dashboard/StatCard.vue'
import MonitorToolbar from '@/components/MonitorToolbar/index.vue'
import { useChart } from '@/utils/useChart.js'
import { chartTokens } from '@/utils/chartTheme.js'
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
const uriTotal = ref(0)
const uriQuery = ref({ pageNo: 1, pageSize: 10 })
const slowSqlList = ref([])
const slowSqlQuery = ref({ pageNo: 1, pageSize: 10 })
const slowSqlPage = computed(() => {
  const start = (slowSqlQuery.value.pageNo - 1) * slowSqlQuery.value.pageSize
  return slowSqlList.value.slice(start, start + slowSqlQuery.value.pageSize)
})

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

// 主题切换后重建 option，图表色取自构建期 CSS 变量计算值
function handleThemeChange() {
  renderTrendChart()
  renderCodeChart()
}

onMounted(async () => {
  const nodeRes = await queryNodeListAPI()
  if (nodeRes.code === 200) nodeList.value = nodeRes.data || []
  const defaultNode = nodeList.value.find(node => node.online)
  if (defaultNode && !nodeKey.value) {
    // 默认选中第一个在线节点，nodeKey watcher 会触发首次加载
    nodeKey.value = defaultNode.nodeKey
  } else {
    loadData()
    loadUriStats()
  }
  window.addEventListener('app-theme-change', handleThemeChange)
  timer = setInterval(() => {
    if (range.value === '1h') loadData()
  }, REFRESH_INTERVAL)
})

onBeforeUnmount(() => {
  window.removeEventListener('app-theme-change', handleThemeChange)
  if (timer) clearInterval(timer)
})

watch(nodeKey, () => {
  uriQuery.value.pageNo = 1
  slowSqlQuery.value.pageNo = 1
  loadUriStats()
})
watch([nodeKey, range], () => loadData())

/**
 * 加载汇总、趋势与慢SQL（按时间范围）
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
  loadSlowSql()
}

/**
 * 加载全量URI聚合统计（分页，按调用次数倒排）
 */
function loadUriStats() {
  const query = { ...uriQuery.value, nodeKey: nodeKey.value }
  queryUriStatsAPI(query).then(res => {
    if (res.code !== 200 || !res.data) return
    uriStats.value = res.data.rows || []
    uriTotal.value = res.data.total || 0
  })
}

/**
 * 切换每页条数后回到第一页
 */
function handleUriSizeChange() {
  uriQuery.value.pageNo = 1
  loadUriStats()
}

/**
 * 加载节点慢SQL快照，未选节点时聚合全部在线节点
 */
function loadSlowSql() {
  if (nodeKey.value) {
    queryLatestDruidAPI({ nodeKey: nodeKey.value }).then(res => {
      slowSqlList.value = parseSlowSql(res.data, nodeKey.value)
      slowSqlQuery.value.pageNo = 1
    })
    return
  }
  const onlineNodes = nodeList.value.filter(node => node.online)
  Promise.all(onlineNodes.map(node =>
      queryLatestDruidAPI({ nodeKey: node.nodeKey }).catch(() => null)
  )).then(resList => {
    slowSqlList.value = resList
        .flatMap((res, index) => parseSlowSql(res?.data, onlineNodes[index]?.nodeKey))
        .sort((a, b) => Number(b.totalTime || 0) - Number(a.totalTime || 0))
    slowSqlQuery.value.pageNo = 1
  })
}

/**
 * 解析单节点 Druid 慢 SQL 快照
 * @param snapshot 节点快照
 * @param currentNodeKey 节点标识
 * @returns {*[]}
 */
function parseSlowSql(snapshot, currentNodeKey) {
  if (!snapshot?.sqlJson) return []
  const groupList = parseJson(snapshot.sqlJson) || []
  return groupList.flatMap(group =>
      (group.sqlList || []).map(item => ({
        ...item,
        nodeKey: currentNodeKey,
        dataSource: shortDataSourceName(group.name)
      }))
  )
}

/**
 * 渲染QPS与平均耗时双轴图
 */
function renderTrendChart() {
  const data = trend.value
  const tokens = chartTokens()
  const labels = data.map(item => item.snapshotDt?.substring(11, 16))
  setTrendOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0, left: 'center', itemWidth: 12, textStyle: { fontSize: 12 } },
    grid: { left: 48, right: 56, top: 40, bottom: 28 },
    xAxis: { type: 'category', data: labels, axisLabel: { fontSize: 10 } },
    yAxis: [
      { type: 'value', name: 'QPS', nameGap: 8, scale: true },
      { type: 'value', name: 'ms', nameGap: 8, scale: true }
    ],
    series: [
      {
        name: 'QPS',
        type: 'bar',
        data: data.map(item => Number(((item.totalCount || 0) / 60).toFixed(2))),
        itemStyle: { color: tokens.primary }
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
  const tokens = chartTokens()
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
        { name: '3xx', value: sum('count3xx'), itemStyle: { color: tokens.primary } },
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

.slow-table-fill {
  display: flex;
  flex-direction: column;
}

.slow-table-wrap {
  flex: none;

  :deep(.el-table__body-wrapper) {
    overflow-x: hidden !important;
  }
}

.uri-table-fill {
  display: flex;
  flex-direction: column;
}

.uri-table-wrap {
  flex: 1;
  min-height: 0;
}

.uri-pagination {
  flex: none;
  display: flex;
  justify-content: flex-end;
}
</style>

<template>
  <div class="app-container monitor-page">
    <MonitorToolbar v-model:range="range" :show-node="false" />

    <div class="monitor-stat-row">
      <StatCard label="监控队列" :value="latestList.length" unit="个" />
      <StatCard label="在线消费者" :value="totalConsumers" unit="个" />
      <StatCard label="积压消息" :value="totalMessages" unit="条" :type="totalMessages > 0 ? 'warning' : 'primary'" />
      <StatCard label="无消费者队列" :value="abnormalCount" unit="个" :type="abnormalCount > 0 ? 'danger' : 'primary'" />
    </div>

    <el-card shadow="never" class="monitor-card mq-trend-card">
      <template #header><div class="monitor-card__title"><el-icon><TrendCharts /></el-icon>队列积压趋势（条）</div></template>
      <div ref="trendElRef" class="monitor-chart monitor-chart--tall"></div>
    </el-card>

    <div class="monitor-stack">
      <el-card shadow="never" class="monitor-card is-fill">
        <template #header>
          <div class="monitor-card__header">
            <span class="monitor-card__title"><el-icon><List /></el-icon>队列明细</span>
            <span class="monitor-card__extra">每 30 秒刷新</span>
          </div>
        </template>
        <div class="table-fill">
          <el-table :data="latestList" size="small" height="100%">
            <el-table-column prop="queueLabel" label="队列" min-width="140" />
            <el-table-column prop="queueName" label="队列名" min-width="140" />
            <el-table-column prop="messageCount" label="消息总数" width="120" align="center" />
            <el-table-column prop="consumerCount" label="消费者数" width="100" align="center" />
            <el-table-column prop="snapshotDt" label="最近采集时间" width="180" align="center" />
            <el-table-column label="状态" min-width="180" align="center">
              <template #default="scope">
                <el-tag :type="statusOf(scope.row).type" size="small">{{ statusOf(scope.row).text }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { TrendCharts, List } from '@element-plus/icons-vue'
import StatCard from '@/components/Dashboard/StatCard.vue'
import MonitorToolbar from '@/components/MonitorToolbar/index.vue'
import { useChart } from '@/utils/useChart.js'
import { chartTokens } from '@/utils/chartTheme.js'
import { buildTimeRange } from '@/utils/monitorUtil.js'
import { queryLatestMqAPI, queryMqSeriesAPI } from '@/api/monitor/monitor.js'

const REFRESH_INTERVAL = 30000

const range = ref('1h')
const latestList = ref([])
const series = ref([])

const totalConsumers = computed(() =>
  latestList.value.reduce((sum, item) => sum + (item.consumerCount || 0), 0)
)
const totalMessages = computed(() =>
  latestList.value.reduce((sum, item) => sum + (item.messageCount || 0), 0)
)
const abnormalCount = computed(() =>
  latestList.value.filter(item => item.consumerCount === 0).length
)

const { elRef: trendElRef, setOption } = useChart()

let timer = null

// 主题切换后重建 option，图表色取自构建期 CSS 变量计算值
function handleThemeChange() {
  if (series.value.length) renderChart()
}

onMounted(() => {
  loadData()
  window.addEventListener('app-theme-change', handleThemeChange)
  timer = setInterval(() => {
    if (range.value === '1h') loadData()
  }, REFRESH_INTERVAL)
})

onBeforeUnmount(() => {
  window.removeEventListener('app-theme-change', handleThemeChange)
  if (timer) clearInterval(timer)
})

watch(range, () => loadData())

/**
 * 加载最新队列与积压趋势
 */
function loadData() {
  queryLatestMqAPI().then(res => {
    if (res.code === 200) latestList.value = res.data || []
  })
  queryMqSeriesAPI(buildTimeRange(range.value)).then(res => {
    if (res.code !== 200) return
    series.value = res.data || []
    renderChart()
  })
}

/**
 * 渲染积压趋势多线图
 */
function renderChart() {
  const labels = [...new Set(series.value.map(item => item.snapshotDt?.substring(11, 16)))]
  const queueNames = [...new Set(series.value.map(item => item.queueName))]
  const chartColors = [chartTokens().primary, '#ff9500', '#34c759']
  const chartSeries = queueNames.map((queueName, index) => {
    const rows = series.value.filter(item => item.queueName === queueName)
    const labelMap = new Map(rows.map(item => [item.snapshotDt?.substring(11, 16), item.messageCount]))
    const label = rows[0]?.queueLabel || queueName
    return {
      name: label,
      type: 'line',
      smooth: true,
      showSymbol: false,
      itemStyle: { color: chartColors[index % chartColors.length] },
      areaStyle: { opacity: 0.08 },
      data: labels.map(time => labelMap.get(time) ?? 0)
    }
  })
  setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0, right: 0, itemWidth: 12, textStyle: { fontSize: 12 } },
    grid: { left: 52, right: 16, top: 36, bottom: 28 },
    xAxis: { type: 'category', data: labels, axisLabel: { fontSize: 10 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: chartSeries
  })
}

/**
 * 判定队列状态
 * @param row 队列行
 * @returns {{type: string, text: string}}
 */
function statusOf(row) {
  if (row.consumerCount === 0 && row.messageCount > 0) {
    return { type: 'danger', text: '无消费者，消息堆积' }
  }
  if (row.consumerCount === 0) {
    return { type: 'warning', text: '无消费者' }
  }
  return { type: 'success', text: '正常' }
}
</script>

<style scoped lang="scss">
@use "@/pages/siteManage/monitor/monitor.scss";

.mq-trend-card {
  flex: none;
  height: auto;
  margin-bottom: $spacing-md;
}
</style>

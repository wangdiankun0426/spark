<template>
  <div class="app-container monitor-page">
    <MonitorToolbar v-model:range="range" :show-node="false" />

    <div class="monitor-stat-row">
      <StatCard label="连接客户端" :value="latest?.connectedClients ?? '--'" unit="个" />
      <StatCard label="阻塞客户端" :value="latest?.blockedClients ?? '--'" unit="个" :type="Number(latest?.blockedClients) > 0 ? 'danger' : 'primary'" />
      <StatCard label="每秒操作数" :value="latest?.opsPerSec ?? '--'" unit="ops/s" />
      <StatCard label="缓存命中率" :value="latest?.hitRate ?? '--'" unit="%" :type="hitRateType" />
    </div>

    <el-row :gutter="16" class="monitor-chart-row">
      <el-col :span="8">
        <el-card shadow="never" class="monitor-card">
          <template #header><div class="monitor-card__title"><el-icon><Coin /></el-icon>内存使用（GB）</div></template>
          <div ref="memElRef" class="monitor-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="monitor-card">
          <template #header><div class="monitor-card__title"><el-icon><Aim /></el-icon>命中率（%）</div></template>
          <div ref="hitElRef" class="monitor-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="monitor-card">
          <template #header><div class="monitor-card__title"><el-icon><Histogram /></el-icon>命令统计 Top10</div></template>
          <div ref="cmdElRef" class="monitor-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="monitor-row-fill">
      <el-col :span="12">
        <el-card shadow="never" class="monitor-card">
          <template #header><div class="monitor-card__title"><el-icon><InfoFilled /></el-icon>Redis 信息</div></template>
          <div v-if="latest" class="metric-grid cache-metric">
            <div class="metric-grid__cell"><label>版本</label><span>{{ latest.version || '--' }}</span></div>
            <div class="metric-grid__cell"><label>运行时长</label><span>{{ latest.uptimeDays }} 天</span></div>
            <div class="metric-grid__cell"><label>键总数</label><span>{{ latest.dbSize }}</span></div>
            <div class="metric-grid__cell">
              <label>内存碎片率</label>
              <span :class="fragAbnormal ? 'is-warning-text' : ''">{{ latest.fragmentationRatio }}</span>
            </div>
            <div class="metric-grid__cell metric-grid__cell--small"><label>常驻内存 RSS</label><span>{{ formatBytes(latest.usedMemoryRss) }}</span></div>
            <div class="metric-grid__cell metric-grid__cell--small"><label>内存峰值</label><span>{{ formatBytes(latest.usedMemoryPeak) }}</span></div>
            <div class="metric-grid__cell metric-grid__cell--small"><label>内存上限</label><span>{{ latest.maxmemory > 0 ? formatBytes(latest.maxmemory) : '未限制' }}</span></div>
            <div class="metric-grid__cell">
              <label>累计驱逐键</label>
              <span :class="{ 'is-danger-text': latest.evictedKeys > 0 }">{{ latest.evictedKeys }}</span>
            </div>
            <div class="metric-grid__cell"><label>累计过期键</label><span>{{ latest.expiredKeys }}</span></div>
            <div class="metric-grid__cell">
              <label>RDB 最近保存</label>
              <span :class="latest.rdbStatus === 'ok' ? 'is-success-text' : 'is-danger-text'">{{ latest.rdbStatus || '--' }}</span>
            </div>
            <div class="metric-grid__cell"><label>AOF</label><span>{{ latest.aofEnabled === 1 ? '已开启' : '未开启' }}</span></div>
            <div class="metric-grid__cell metric-grid__cell--small"><label>快照时间</label><span>{{ latest.snapshotDt }}</span></div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="60" class="monitor-empty" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="monitor-card">
          <template #header>
            <div class="monitor-card__header">
              <span class="monitor-card__title"><el-icon><Key /></el-icon>键空间（db）</span>
              <span class="monitor-card__extra">共 {{ keyspaceList.length }} 个库</span>
            </div>
          </template>
          <div class="table-fill">
            <el-table :data="keyspaceList" size="small" height="100%">
              <el-table-column prop="db" label="数据库" width="120" align="center" />
              <el-table-column prop="keys" label="键数量" min-width="120" align="center" />
              <el-table-column prop="expires" label="带过期键" min-width="120" align="center" />
              <el-table-column prop="avgTtl" label="平均 TTL（ms）" min-width="140" align="center" />
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Coin, Aim, Histogram, InfoFilled, Key } from '@element-plus/icons-vue'
import StatCard from '@/components/Dashboard/StatCard.vue'
import MonitorToolbar from '@/components/MonitorToolbar/index.vue'
import { useChart } from '@/utils/useChart.js'
import { chartTokens } from '@/utils/chartTheme.js'
import { formatBytes, buildTimeRange, parseJson } from '@/utils/monitorUtil.js'
import { queryLatestRedisAPI, queryRedisSeriesAPI } from '@/api/monitor/monitor.js'

const REFRESH_INTERVAL = 30000
const GB = 1024 * 1024 * 1024

const range = ref('1h')
const latest = ref(null)
const series = ref([])

const commandList = computed(() => parseJson(latest.value?.commandJson) || [])
const keyspaceList = computed(() => parseJson(latest.value?.keyspaceJson) || [])
const hitRateType = computed(() => {
  const value = Number(latest.value?.hitRate)
  if (value < 80) return 'warning'
  return 'primary'
})
const fragAbnormal = computed(() => {
  const value = Number(latest.value?.fragmentationRatio)
  return value > 1.5 || (value > 0 && value < 1)
})

const { elRef: memElRef, setOption: setMemOption } = useChart()
const { elRef: hitElRef, setOption: setHitOption } = useChart()
const { elRef: cmdElRef, setOption: setCmdOption } = useChart()

let timer = null

// 主题切换后重建 option，图表色取自构建期 CSS 变量计算值
function handleThemeChange() {
  if (series.value.length) renderTrendCharts()
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
 * 加载最新快照与趋势
 */
function loadData() {
  const timeRange = buildTimeRange(range.value)
  queryLatestRedisAPI().then(res => {
    if (res.code === 200) {
      latest.value = res.data
      renderCmdChart()
    }
  })
  queryRedisSeriesAPI(timeRange).then(res => {
    if (res.code !== 200) return
    series.value = res.data || []
    renderTrendCharts()
  })
}

/**
 * 渲染内存与命中率趋势
 */
function renderTrendCharts() {
  const data = series.value
  const labels = data.map(item => item.snapshotDt?.substring(11, 16))
  setMemOption(buildLineOption(labels, [
    { name: 'used', data: data.map(item => roundGb(item.usedMemory)), color: chartTokens().primary },
    { name: 'rss', data: data.map(item => roundGb(item.usedMemoryRss)), color: '#ff9500' }
  ], 'GB'))
  setHitOption(buildLineOption(labels, [
    { name: '命中率', data: data.map(item => item.hitRate), color: '#34c759' }
  ], '%'))
}

/**
 * 渲染命令统计饼图
 */
function renderCmdChart() {
  setCmdOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { type: 'scroll', orient: 'vertical', right: 0, top: 'middle', textStyle: { fontSize: 11 } },
    series: [{
      type: 'pie',
      radius: ['40%', '68%'],
      center: ['38%', '50%'],
      label: { show: false },
      data: commandList.value.map(item => ({ name: item.name, value: item.value }))
    }]
  })
}

/**
 * 构造折线图配置
 * @param labels 横轴
 * @param seriesList 序列
 * @param unit 单位
 * @returns {object} echarts配置
 */
function buildLineOption(labels, seriesList, unit) {
  return {
    tooltip: { trigger: 'axis', valueFormatter: value => `${value} ${unit}` },
    legend: { top: 0, right: 0, itemWidth: 12, textStyle: { fontSize: 12 } },
    grid: { left: 44, right: 12, top: 32, bottom: 24 },
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

function roundGb(bytes) {
  return Math.round((bytes / GB) * 10) / 10
}
</script>

<style scoped lang="scss">
@use "@/styles/monitor.scss";

.cache-metric {
  grid-template-rows: repeat(6, 1fr);
}
</style>

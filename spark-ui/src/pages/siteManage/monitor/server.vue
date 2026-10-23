<template>
  <div class="app-container monitor-page">
    <MonitorToolbar
      v-model:node-key="nodeKey"
      v-model:range="range"
      :node-list="nodeList"
    />

    <el-card shadow="never" class="node-banner">
      <div v-if="hostInfo" class="node-banner__inner">
        <div class="node-banner__main">
          <div class="node-banner__logo">
            <el-icon><Platform /></el-icon>
          </div>
          <div class="node-banner__title">
            <div class="node-banner__name">
              {{ hostInfo.hostName }}
              <span class="node-banner__status" :class="nodeOnline ? 'is-online' : 'is-offline'">
                <i class="node-banner__dot"></i>{{ nodeOnline ? '在线' : '离线' }}
              </span>
            </div>
            <div class="node-banner__ip">{{ hostInfo.ip }}</div>
          </div>
        </div>
        <div class="node-banner__meta">
          <div class="node-banner__meta-item">
            <label>操作系统</label>
            <span :title="hostInfo.os">{{ hostInfo.os }}</span>
          </div>
          <i class="node-banner__divider"></i>
          <div class="node-banner__meta-item">
            <label>CPU</label>
            <span :title="`${hostInfo.cpuName}（${hostInfo.cpuCores} 核）`">{{ hostInfo.cpuName }} · {{ hostInfo.cpuCores }} 核</span>
          </div>
          <i class="node-banner__divider"></i>
          <div class="node-banner__meta-item">
            <label>JDK</label>
            <span>{{ hostInfo.jvmVersion }}</span>
          </div>
          <i class="node-banner__divider"></i>
          <div class="node-banner__meta-item">
            <label>运行时长</label>
            <span>{{ uptimeText }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="该节点暂无监控数据" :image-size="60" />
    </el-card>

    <div class="monitor-stat-row">
      <StatCard label="CPU 使用率" :value="latest?.cpuUsage ?? '--'" unit="%" :type="rateType(latest?.cpuUsage)" />
      <StatCard label="内存使用率" :value="latest?.memUsage ?? '--'" unit="%" :type="rateType(latest?.memUsage)" :sub="memSub" />
      <StatCard label="Swap 使用率" :value="latest?.swapUsage ?? '--'" unit="%" :type="rateType(latest?.swapUsage)" />
      <StatCard label="JVM 堆使用率" :value="latest?.heapUsage ?? '--'" unit="%" :type="rateType(latest?.heapUsage)" :sub="heapSub" />
    </div>

    <el-row :gutter="16" class="monitor-chart-row">
      <el-col :span="8">
        <el-card shadow="never" class="monitor-card">
            <template #header>CPU 使用率（%）</template>
            <div ref="cpuElRef" class="monitor-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="monitor-card">
            <template #header>内存 / JVM 堆（GB）</template>
            <div ref="memElRef" class="monitor-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="monitor-card">
            <template #header>网络速率（KB/s）</template>
            <div ref="netElRef" class="monitor-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="monitor-row-fill">
      <el-col :span="14">
        <el-card shadow="never" class="monitor-card">
          <template #header>
            <div class="monitor-card__header">
              <span>磁盘分区</span>
              <span class="monitor-card__extra">共 {{ diskList.length }} 个分区</span>
            </div>
          </template>
          <div class="table-fill">
            <el-table :data="diskList" size="small" height="100%">
              <el-table-column prop="mount" label="挂载点" min-width="120" show-overflow-tooltip />
              <el-table-column prop="name" label="驱动器" width="110" align="center" show-overflow-tooltip />
              <el-table-column prop="type" label="类型" width="90" align="center" />
              <el-table-column label="总量" width="90" align="center">
                <template #default="scope">{{ formatBytes(scope.row.total) }}</template>
              </el-table-column>
              <el-table-column label="已用" width="90" align="center">
                <template #default="scope">{{ formatBytes(scope.row.used) }}</template>
              </el-table-column>
              <el-table-column label="使用率" min-width="150">
                <template #default="scope">
                  <el-progress
                    :percentage="Number(scope.row.usage) || 0"
                    :status="progressStatus(scope.row.usage)"
                  />
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="monitor-card">
          <template #header>JVM 运行状态</template>
          <div class="jvm-grid" v-if="latest">
            <div class="jvm-grid__item">
              <label>Young GC</label>
              <span>{{ latest.youngGcCount }}<em>次</em></span>
              <small>{{ formatMs(latest.youngGcCost) }}</small>
            </div>
            <div class="jvm-grid__item">
              <label>Old GC</label>
              <span>{{ latest.oldGcCount }}<em>次</em></span>
              <small>{{ formatMs(latest.oldGcCost) }}</small>
            </div>
            <div class="jvm-grid__item">
              <label>线程总数</label>
              <span>{{ latest.threadCount }}<em>个</em></span>
            </div>
            <div class="jvm-grid__item">
              <label>阻塞线程</label>
              <span :class="{ 'is-danger': latest.blockedCount > 0 }">{{ latest.blockedCount }}<em>个</em></span>
            </div>
            <div class="jvm-grid__item">
              <label>死锁线程</label>
              <span :class="{ 'is-danger': latest.deadlockedCount > 0 }">{{ latest.deadlockedCount }}<em>个</em></span>
            </div>
            <div class="jvm-grid__item">
              <label>已加载类</label>
              <span>{{ latest.loadedClassCount }}<em>个</em></span>
            </div>
            <div class="jvm-grid__item">
              <label>非堆已用</label>
              <span class="jvm-grid__bytes">{{ formatBytes(latest.nonheapUsed) }}</span>
            </div>
            <div class="jvm-grid__item">
              <label>快照时间</label>
              <span class="jvm-grid__time">{{ latest.snapshotDt }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="60" class="jvm-empty" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import StatCard from './components/StatCard.vue'
import MonitorToolbar from './components/MonitorToolbar.vue'
import { useChart } from '@/utils/useChart.js'
import { formatBytes, formatMs, buildTimeRange, parseJson } from '@/utils/monitorUtil.js'
import { queryNodeListAPI, queryLatestHostAPI, queryHostSeriesAPI } from '@/api/monitor/monitor.js'

const REFRESH_INTERVAL = 30000
const GB = 1024 * 1024 * 1024

const nodeList = ref([])
const nodeKey = ref('')
const range = ref('1h')
const latest = ref(null)
const series = ref([])

const hostInfo = computed(() => parseJson(latest.value?.hostInfoJson))
const diskList = computed(() => parseJson(latest.value?.diskJson) || [])
const nodeOnline = computed(() => nodeList.value.find(node => node.nodeKey === nodeKey.value)?.online)
const memSub = computed(() => latest.value ? `${formatBytes(latest.value.memUsed)} / ${formatBytes(latest.value.memTotal)}` : '')
const heapSub = computed(() => latest.value ? `${formatBytes(latest.value.heapUsed)} / ${formatBytes(latest.value.heapMax)}` : '')
const uptimeText = computed(() => {
  if (!hostInfo.value?.jvmStartTime) return '--'
  const hours = Math.floor((Date.now() - hostInfo.value.jvmStartTime) / 3600000)
  if (hours < 24) return `${hours} 小时`
  return `${Math.floor(hours / 24)} 天 ${hours % 24} 小时`
})

const { elRef: cpuElRef, setOption: setCpuOption } = useChart()
const { elRef: memElRef, setOption: setMemOption } = useChart()
const { elRef: netElRef, setOption: setNetOption } = useChart()

let timer = null

onMounted(() => {
  loadNodeList()
  timer = setInterval(() => {
    if (range.value === '1h') loadData()
  }, REFRESH_INTERVAL)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})

watch([nodeKey, range], () => {
  if (nodeKey.value) loadData()
})

/**
 * 加载节点清单并默认选中首个在线节点
 */
function loadNodeList() {
  queryNodeListAPI().then(res => {
    if (res.code !== 200) return
    nodeList.value = res.data || []
    const online = nodeList.value.find(node => node.online) || nodeList.value[0]
    if (online) {
      nodeKey.value = online.nodeKey
    }
  })
}

/**
 * 加载最新快照与趋势
 */
function loadData() {
  if (!nodeKey.value) return
  const timeRange = buildTimeRange(range.value)
  queryLatestHostAPI({ nodeKey: nodeKey.value }).then(res => {
    if (res.code === 200) latest.value = res.data
  })
  queryHostSeriesAPI({ nodeKey: nodeKey.value, ...timeRange }).then(res => {
    if (res.code !== 200) return
    series.value = res.data || []
    renderCharts()
  })
}

/**
 * 渲染三张趋势图
 */
function renderCharts() {
  const data = series.value
  const labels = data.map(item => item.snapshotDt?.substring(11, 16))
  setCpuOption(buildLineOption(labels, [
    { name: 'CPU', data: data.map(item => item.cpuUsage), color: '#004fc5' }
  ], '%'))
  setMemOption(buildLineOption(labels, [
    { name: '物理内存', data: data.map(item => roundGb(item.memUsed)), color: '#004fc5' },
    { name: 'JVM堆', data: data.map(item => roundGb(item.heapUsed)), color: '#ff9500' }
  ], 'GB'))
  setNetOption(buildLineOption(labels, [
    { name: '下行', data: data.map(item => Math.round(item.netRxRate / 1024)), color: '#004fc5' },
    { name: '上行', data: data.map(item => Math.round(item.netTxRate / 1024)), color: '#34c759' }
  ], 'KB/s'))
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

/**
 * 使用率告警配色
 * @param value 百分比
 * @returns {string}
 */
function rateType(value) {
  const number = Number(value)
  if (number >= 85) return 'danger'
  if (number >= 70) return 'warning'
  return 'primary'
}

/**
 * 进度条状态
 * @param value 百分比
 * @returns {string}
 */
function progressStatus(value) {
  const number = Number(value)
  if (number >= 85) return 'exception'
  if (number >= 70) return 'warning'
  return 'success'
}

function roundGb(bytes) {
  return Math.round((bytes / GB) * 10) / 10
}
</script>

<style scoped lang="scss">
@use "@/styles/monitor.scss";

.node-banner {
  flex: none;
  margin-bottom: $spacing-md;

  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-md;
    padding: $spacing-md $spacing-lg;

    .el-empty {
      flex: 1;
    }
  }

  &__inner {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    gap: $spacing-md;
  }

  &__main {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    flex: none;
  }

  &__logo {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 46px;
    height: 46px;
    border-radius: 50%;
    background-color: $color-primary-soft;
    color: $color-primary;
    font-size: 24px;
  }

  &__name {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    font-size: 17px;
    font-weight: 600;
    color: $color-text-primary;
  }

  &__status {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    font-weight: 400;

    &.is-online {
      color: var(--el-color-success);
    }

    &.is-offline {
      color: var(--el-color-info);
    }
  }

  &__dot {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background-color: currentColor;
  }

  &__ip {
    margin-top: 2px;
    font-size: 12px;
    color: $color-text-secondary;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: $spacing-lg;
    flex: 1;
    justify-content: flex-end;
    min-width: 0;
  }

  &__meta-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
    max-width: 200px;

    label {
      font-size: 11px;
      color: $color-text-placeholder;
    }

    span {
      font-size: 13px;
      color: $color-text-primary;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  &__divider {
    width: 1px;
    height: 30px;
    flex: none;
    background-color: $border-color;
  }
}

.jvm-grid {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(4, 1fr);
  gap: $spacing-sm;

  &__item {
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 4px;
    padding: $spacing-sm $spacing-md;
    border: 1px solid $border-color-light;
    border-radius: $border-radius-sm;
    background-color: $bg-card;
    min-width: 0;

    label {
      font-size: 12px;
      color: $color-text-secondary;
    }

    span {
      font-size: 18px;
      font-weight: 600;
      color: $color-text-primary;
      line-height: 1.2;

      em {
        margin-left: 2px;
        font-size: 12px;
        font-style: normal;
        font-weight: 400;
        color: $color-text-placeholder;
      }
    }

    small {
      font-size: 11px;
      color: $color-text-placeholder;
    }

    .is-danger {
      color: var(--el-color-danger);
    }
  }

  &__bytes,
  &__time {
    font-size: 15px !important;
  }
}

.jvm-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>

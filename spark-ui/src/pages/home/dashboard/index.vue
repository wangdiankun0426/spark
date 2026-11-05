<template>
  <div class="dashboard" v-loading="loading">
    <!-- 顶部渐变横幅（通铺到顶、左右不留白） -->
    <div class="dash-hero">
      <div class="dash-hero__left">
        <div class="dash-hero__date">
          <el-icon><Calendar/></el-icon>
          <span>{{ todayText }}</span>
        </div>
        <div class="dash-hero__title">{{ greetingText }}，<em>{{ userName || '朋友' }}</em></div>
        <div class="dash-hero__sub">
          欢迎使用星火云应用平台，聚微光成智，燃无限可能！
          <template v-if="todoCount > 0"> 今天有 <b>{{ todoCount }}</b> 件待办 等待处理。</template>
        </div>
      </div>
      <div class="dash-hero__right">
        <el-button class="dash-hero__ai" @click="goModel">
          <el-icon><ChatDotRound/></el-icon>
          开始 AI 对话
        </el-button>
      </div>
    </div>

    <div class="dash-body">
      <!-- 数据概览 -->
      <div class="dash-kpi">
        <StatCard label="模型调用总次数" :value="formatNumber(overview?.callCount)" unit="次"
                  sub="累计">
          <template #icon><el-icon><Promotion/></el-icon></template>
        </StatCard>
        <StatCard label="Token 总消耗" :value="formatNumber(overview?.totalTokens)" unit=""
                  :sub="`输入 ${formatNumber(overview?.inputTokens)} · 输出 ${formatNumber(overview?.outputTokens)}`">
          <template #icon><el-icon><Coin/></el-icon></template>
        </StatCard>
        <StatCard label="模型" :value="overview?.modelCount ?? 0" unit="个" sub="点击使用"
                  class="kpi-clickable" @click="goModel">
          <template #icon><el-icon><Cpu/></el-icon></template>
        </StatCard>
        <StatCard label="智能体" :value="overview?.agentCount ?? 0" unit="个" sub="点击使用"
                  class="kpi-clickable" @click="goAgent">
          <template #icon><el-icon><MagicStick/></el-icon></template>
        </StatCard>
        <StatCard label="工作流" :value="overview?.workflowCount ?? 0" unit="个" sub="点击使用"
                  class="kpi-clickable" @click="goWorkflow">
          <template #icon><el-icon><Share/></el-icon></template>
        </StatCard>
        <StatCard label="我的待办" :value="todoCount" unit="件"
                  sub="点击查看处理" badge="待处理" class="kpi-clickable" @click="goTodo">
          <template #icon><el-icon><Bell/></el-icon></template>
        </StatCard>
      </div>

      <!-- 趋势 + 智能推荐 -->
      <div class="dash-row dash-row--chart">
        <div class="panel trend-panel">
          <div class="panel__head">
            <div class="panel__title">
              <span class="panel__icon panel__icon--blue"><el-icon><Histogram/></el-icon></span>
              <span>用量趋势</span>
              <span class="trend-legend">
                <i class="trend-legend__dot" :style="{ background: chartPalette.blue }"></i>调用次数
                <i class="trend-legend__line" :style="{ background: chartPalette.cyan }"></i>Token 消耗
              </span>
            </div>
            <el-select v-model="trendDays" size="small" class="range-select" @change="loadTrend">
              <el-option :value="7" label="近一周" />
              <el-option :value="30" label="近一月" />
              <el-option :value="90" label="近三月" />
            </el-select>
          </div>
          <div ref="trendChartRef" class="trend-chart"/>
        </div>
        <RankCard title="智能推荐" :list="hotRecommend" unit="次" show-type-tag>
          <template #icon><StarFilled/></template>
        </RankCard>
      </div>

      <!-- 底部三块 -->
      <div class="dash-row dash-row--bottom">
        <div class="panel">
          <div class="panel__head">
            <div class="panel__title">
              <span class="panel__icon panel__icon--blue"><el-icon><BellFilled/></el-icon></span>
              <span>我的流程</span>
            </div>
            <el-button text type="primary" @click="goApply">全部</el-button>
          </div>
          <div class="flow-wrap">
            <div ref="todoChartRef" class="todo-chart"/>
            <div class="flow-legend">
              <div v-for="item in flowLegend" :key="item.name" class="flow-legend__row">
                <i :style="{ background: item.color }"></i>
                <span class="flow-legend__name">{{ item.name }}</span>
                <b>{{ item.value }}</b>
              </div>
            </div>
          </div>
        </div>

        <div class="panel apply-panel">
          <div class="panel__head">
            <div class="panel__title">
              <span class="panel__icon panel__icon--blue"><el-icon><CirclePlusFilled/></el-icon></span>
              <span>可申请流程</span>
            </div>
            <el-button text type="primary" @click="goApply">全部</el-button>
          </div>
          <div v-if="applyTemplates.length" class="apply-list">
            <button v-for="item in applyTemplates" :key="item.id" class="apply-chip" @click="goApply">
              <el-icon><Plus/></el-icon>
              <span class="apply-chip__name" :title="item.name">{{ item.name }}</span>
            </button>
          </div>
          <el-empty v-else :image-size="80" description="暂无可申请流程" class="panel__empty"/>
        </div>

        <div class="panel todo-panel">
          <div class="panel__head">
            <div class="panel__title">
              <span class="panel__icon panel__icon--blue"><el-icon><BellFilled/></el-icon></span>
              <span>我的待办</span>
            </div>
            <el-badge :value="todoCount" :max="99" :hidden="!todoCount">
              <el-button text type="primary" @click="goTodo">全部</el-button>
            </el-badge>
          </div>
          <div v-if="todoList.length" class="todo-list">
            <div v-for="item in todoList" :key="item.id" class="todo-item" @click="goTodo">
              <span class="todo-item__icon"><el-icon><Tickets/></el-icon></span>
              <span class="todo-item__name" :title="item.name">{{ item.name }}</span>
              <el-tag size="small" :type="levelTagType(item.level)" effect="light" class="todo-item__tag">{{ item.levelName || '一般' }}</el-tag>
              <span v-if="todoTime(item)" class="todo-item__time">{{ todoTime(item) }}</span>
            </div>
          </div>
          <el-empty v-else :image-size="80" description="暂无待办" class="panel__empty"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import {
  Bell, BellFilled, Calendar, ChatDotRound, CirclePlusFilled, Coin, Cpu,
  Histogram, MagicStick, Plus, Promotion, Share, StarFilled, Tickets
} from '@element-plus/icons-vue'
import {
  queryUsageTrendAPI, queryFlowCountAPI, queryRecommendAPI, queryOverviewAPI
} from '@/api/home/home'
import {pageMyTodoListAPI} from '@/api/flow/instance'
import {pageTemplateListAPI} from '@/api/flow/template'
import { useChart } from '@/utils/useChart'
import { chartTokens } from '@/utils/chartTheme'
import StatCard from '@/components/Dashboard/StatCard.vue'
import RankCard from '@/components/Dashboard/RankCard.vue'

const router = useRouter()
const store = useStore()
const loading = ref(false)
// 用量趋势独立时间范围，默认近一周
const trendDays = ref(7)

const userName = computed(() => store.getters['user/getUserInfo']?.name || '')

const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const todayText = computed(() => {
  const week = ['日', '一', '二', '三', '四', '五', '六']
  const now = new Date()
  return `${now.getMonth() + 1}月${now.getDate()}日 星期${week[now.getDay()]}`
})

const overview = ref(null)
const usageTrend = ref([])
const todoList = ref([])
const flowCount = ref({ todoCount: 0, doneCount: 0, applicationCount: 0, copyCount: 0 })
// 待办数取流程四项统计的 todoCount，全页单一数据源
const todoCount = computed(() => flowCount.value.todoCount || 0)
const applyTemplates = ref([])
const hotRecommend = ref([])

const { elRef: trendChartRef, setOption: setTrendOption } = useChart()
const { elRef: todoChartRef, setOption: setTodoOption } = useChart()

// 仪表盘图表色板，环形图与趋势图共用，canvas 不识别 CSS 变量故集中管理
const chartPalette = {
  blue: '#2f7bff',
  cyan: '#22c8d8',
  red: '#ff7875',
  purple: '#9a7bf7',
  white: '#ffffff',
  blueAreaTop: 'rgba(47,123,255,0.18)',
  blueAreaBottom: 'rgba(47,123,255,0)',
  cyanAreaTop: 'rgba(34,200,216,0.14)',
  cyanAreaBottom: 'rgba(34,200,216,0)'
}

// 图例与环形图同源共用色板
const flowLegend = computed(() => {
  const c = flowCount.value
  return [
    { name: '我的待办', value: c.todoCount || 0, color: chartPalette.red },
    { name: '我的已办', value: c.doneCount || 0, color: chartPalette.blue },
    { name: '我的申请', value: c.applicationCount || 0, color: chartPalette.cyan },
    { name: '抄送给我', value: c.copyCount || 0, color: chartPalette.purple }
  ]
})

/**
 * 加载用量趋势，时间范围由趋势卡选择器控制
 */
function loadTrend() {
  return queryUsageTrendAPI({ rangeDays: trendDays.value }).then(res => {
    usageTrend.value = res.data || []
    nextTick(() => renderTrend())
  })
}

function formatNumber(value) {
  const num = Number(value || 0)
  if (num >= 100000000) return `${(num / 100000000).toFixed(1)}亿`
  if (num >= 10000) return `${(num / 10000).toFixed(1)}万`
  return num.toLocaleString()
}

/**
 * 加载工作台全部卡片，统计与推荐不限时间范围，趋势时间窗由 loadTrend 独立控制
 */
function loadFixed() {
  const tasks = [
    queryOverviewAPI().then(res => { overview.value = res.data }),
    loadTrend(),
    pageMyTodoListAPI({pageNo: 1, pageSize: 8}).then(res => { todoList.value = res.data?.rows || [] }),
    queryFlowCountAPI().then(res => {
      flowCount.value = res.data || flowCount.value
      nextTick(() => renderTodoChart())
    }),
    pageTemplateListAPI({pageNo: 1, pageSize: 8}).then(res => { applyTemplates.value = res.data?.rows || [] }),
    queryRecommendAPI().then(res => { hotRecommend.value = res.data || [] })
  ]
  return Promise.allSettled(tasks)
}

function buildDateLabels() {
  const labels = []
  const fullLabels = []
  const map = {}
  const now = new Date()
  for (let i = trendDays.value - 1; i >= 0; i--) {
    const d = new Date(now.getTime() - i * 24 * 3600 * 1000)
    const full = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    fullLabels.push(full)
    labels.push(full.slice(5))
    map[full] = { callCount: 0, totalTokens: 0 }
  }
  return { labels, fullLabels, map }
}

function renderTrend() {
  if (!usageTrend.value.length) {
    setTrendOption({})
    return
  }
  const { labels, fullLabels, map } = buildDateLabels()
  usageTrend.value.forEach(item => {
    if (map[item.dt]) {
      map[item.dt].callCount += Number(item.callCount || 0)
      map[item.dt].totalTokens += Number(item.totalTokens || 0)
    }
  })
  const calls = fullLabels.map(full => map[full].callCount)
  const tokensData = fullLabels.map(full => map[full].totalTokens)
  const tokens = chartTokens()
  setTrendOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 52, right: 20, top: 24, bottom: 30 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLine: { lineStyle: { color: tokens.split } },
      axisTick: { show: false },
      axisLabel: { color: tokens.text }
    },
    yAxis: [
      {
        type: 'value',
        splitLine: { lineStyle: { color: tokens.split } },
        axisLabel: { color: tokens.text }
      },
      {
        type: 'value',
        splitLine: { show: false },
        axisLabel: { show: false }
      }
    ],
    series: [
      {
        name: '调用次数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        lineStyle: { color: chartPalette.blue, width: 2.5 },
        itemStyle: { color: chartPalette.blue, borderColor: chartPalette.white, borderWidth: 1.5 },
        areaStyle: {
          color: {
            type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: chartPalette.blueAreaTop },
              { offset: 1, color: chartPalette.blueAreaBottom }
            ]
          }
        },
        data: calls
      },
      {
        name: 'Token消耗',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: chartPalette.cyan, width: 2, type: 'dashed' },
        itemStyle: { color: chartPalette.cyan, borderColor: chartPalette.white, borderWidth: 1.5 },
        areaStyle: {
          color: {
            type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: chartPalette.cyanAreaTop },
              { offset: 1, color: chartPalette.cyanAreaBottom }
            ]
          }
        },
        data: tokensData
      }
    ]
  })
}

function levelTagType(level) {
  if (level === 3) return 'danger'
  if (level === 2) return 'primary'
  return 'info'
}

/**
 * 待办时间：今天显示「今天 HH:mm」，其余显示「MM-DD HH:mm」
 */
function todoTime(item) {
  const raw = item.createTime || item.updateTime
  if (!raw) return ''
  const d = new Date(String(raw).replace(/-/g, '/'))
  if (isNaN(d.getTime())) return ''
  const pad = n => String(n).padStart(2, '0')
  const hm = `${pad(d.getHours())}:${pad(d.getMinutes())}`
  const now = new Date()
  if (d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth() && d.getDate() === now.getDate()) {
    return `今天 ${hm}`
  }
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${hm}`
}

function renderTodoChart() {
  const c = flowCount.value
  const total = (c.todoCount || 0) + (c.doneCount || 0) + (c.applicationCount || 0) + (c.copyCount || 0)
  const tokens = chartTokens()
  setTodoOption({
    tooltip: { trigger: 'item' },
    title: {
      text: String(total),
      subtext: '流程总数',
      left: 'center',
      top: '32%',
      textStyle: { fontSize: 26, fontWeight: 700, color: tokens.primary },
      subtextStyle: { fontSize: 12, color: tokens.text }
    },
    series: [
      {
        name: '我的流程',
        type: 'pie',
        radius: ['56%', '76%'],
        center: ['50%', '46%'],
        avoidLabelOverlap: false,
        label: { show: false },
        itemStyle: { borderColor: tokens.split, borderWidth: 2 },
        data: flowLegend.value.map(item => ({ value: item.value, name: item.name, itemStyle: { color: item.color } }))
      }
    ]
  })
}

function goTodo() {
  router.push('/home/flow/myTodo')
}

function goApply() {
  router.push('/home/flow/application')
}

function goModel() {
  router.push('/home/aichat')
}

function goAgent() {
  router.push('/home/agent')
}

function goWorkflow() {
  router.push('/home/workflow')
}

// option 颜色是构建期读取的计算值，主题切换后需重建
function handleThemeChange() {
  nextTick(() => {
    renderTrend()
    renderTodoChart()
  })
}

onMounted(() => {
  loading.value = true
  window.addEventListener('app-theme-change', handleThemeChange)
  loadFixed().finally(() => {
    loading.value = false
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('app-theme-change', handleThemeChange)
})
</script>

<style scoped lang="scss">
.dashboard {
  display: flex;
  flex-direction: column;
  height: calc(100vh - $nav-height);
  overflow: hidden;
}

// 顶部渐变横幅，通铺到顶左右不留白
.dash-hero {
  position: relative;
  flex: none;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  padding: 22px 24px 24px;
  overflow: hidden;
  background:
    radial-gradient(460px 200px at 86% -30%, rgba(120, 119, 255, 0.18), transparent 60%),
    radial-gradient(380px 180px at 14% -40%, rgba(47, 123, 255, 0.20), transparent 55%),
    $app-hero-grad;

  // 右侧装饰圆环
  &::after {
    content: "";
    position: absolute;
    right: -60px;
    top: -90px;
    width: 230px;
    height: 230px;
    border-radius: 50%;
    border: 30px solid rgba(255, 255, 255, 0.4);
    pointer-events: none;
  }
}

html.dark .dash-hero {
  background:
    radial-gradient(460px 200px at 86% -30%, rgba(120, 119, 255, 0.14), transparent 60%),
    radial-gradient(380px 180px at 14% -40%, rgba(47, 123, 255, 0.16), transparent 55%),
    $app-hero-grad;

  &::after {
    border-color: rgba(255, 255, 255, 0.05);
  }
}

.dash-hero__left {
  position: relative;
  z-index: 1;
  min-width: 0;
}

.dash-hero__date {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.55);
  font-size: 12.5px;
  font-weight: 600;
  color: $el-text-color-regular;

  .el-icon {
    font-size: 14px;
    color: $ds-brand;
  }
}

html.dark .dash-hero__date {
  background: rgba(255, 255, 255, 0.08);
}

.dash-hero__title {
  margin-top: 10px;
  font-size: 24px;
  font-weight: 800;
  color: $el-text-color-primary;

  em {
    font-style: normal;
    background: $ds-grad;
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
  }
}

.dash-hero__sub {
  margin-top: 6px;
  font-size: 13px;
  color: $el-text-color-regular;

  b {
    color: $app-hero-accent;
    font-weight: 800;
    margin: 0 1px;
  }
}

.dash-hero__right {
  position: relative;
  z-index: 1;
  flex: none;
  display: flex;
  align-items: center;
  gap: 12px;
}

.dash-hero__ai {
  height: 36px;
  padding: 0 18px;
  border: none;
  border-radius: 10px;
  font-weight: 700;
  color: $color-text-white;
  background: $ds-grad;
  box-shadow: 0 6px 16px -6px rgba(26, 111, 232, 0.5);
  display: inline-flex;
  align-items: center;
  gap: 6px;

  &:hover {
    color: $color-text-white;
    opacity: 0.92;
  }
}

// 主体
.dash-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  padding: $spacing-md 16px 16px;
}

.dash-kpi {
  flex: none;
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: $spacing-sm;
}

.kpi-clickable {
  cursor: pointer;
}

.dash-row {
  flex: none;
  display: grid;
  gap: $spacing-md;
  align-items: stretch;
}

.dash-row--chart {
  grid-template-columns: 1.7fr 1fr;
}

.dash-row--bottom {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

// 面板
.panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  height: 320px;
  overflow: hidden;
  padding: $spacing-md;
  background-color: $bg-card;
  border: 1px solid $border-color-light;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;

  &__head {
    flex: none;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-bottom: $spacing-sm;
    border-bottom: 1px solid $border-color-light;
  }

  &__title {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    font-size: 15px;
    font-weight: 600;
    color: $color-text-primary;
  }

  &__icon {
    flex: none;
    width: 26px;
    height: 26px;
    display: grid;
    place-items: center;
    border-radius: 8px;

    .el-icon {
      font-size: 14px;
    }

    &--blue {
      color: $ds-brand;
      background: $app-primary-light;
    }
  }

  &__empty {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-height: 0;
  }
}

.trend-legend {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: 8px;
  font-size: 12px;
  font-weight: 400;
  color: $color-text-secondary;

  .trend-legend__dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }

  .trend-legend__line {
    width: 14px;
    height: 0;
    border-top: 2px dashed;
    border-radius: 1px;
  }
}

.range-select {
  width: 104px;
  flex: none;
}

.trend-chart {
  flex: 1;
  min-height: 0;
  width: 100%;
}

// 我的流程环形图与自定义图例
.flow-wrap {
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.todo-chart {
  flex: 1;
  min-width: 0;
  height: 100%;
}

.flow-legend {
  flex: none;
  width: 150px;
  display: flex;
  flex-direction: column;
  gap: 14px;

  &__row {
    display: flex;
    align-items: center;
    gap: 8px;

    i {
      flex: none;
      width: 9px;
      height: 9px;
      border-radius: 50%;
    }

    .flow-legend__name {
      flex: 1;
      font-size: 12.5px;
      color: $color-text-secondary;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    b {
      font-size: 14px;
      font-weight: 700;
      color: $el-text-color-primary;
      font-variant-numeric: tabular-nums;
    }
  }
}

// 可申请流程
.apply-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-top: $spacing-sm;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  gap: 10px;
}

.apply-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 14px;
  border: 1px solid $border-color-light;
  border-radius: 999px;
  background: transparent;
  font-size: 12.5px;
  color: $el-text-color-regular;
  cursor: pointer;
  transition: $transition-fast;
  max-width: 100%;

  .el-icon {
    font-size: 12px;
    color: $color-primary;
  }

  &__name {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &:hover {
    border-color: $ds-brand;
    color: $ds-brand;
    background: $app-primary-soft;
  }
}

// 我的待办
.todo-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-top: $spacing-sm;
  display: flex;
  flex-direction: column;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  height: 44px;
  padding: 0 $spacing-sm;
  border-radius: $border-radius-sm;
  cursor: pointer;
  transition: background-color $transition-normal;

  &:hover {
    background-color: $color-primary-light;
  }

  &__icon {
    flex: none;
    width: 26px;
    height: 26px;
    display: grid;
    place-items: center;
    border-radius: 8px;
    color: $ds-brand;
    background: $app-primary-soft;

    .el-icon {
      font-size: 13px;
    }
  }

  &__name {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;
    color: $color-text-primary;
  }

  &__tag {
    flex: none;
    font-size: 11px;
  }

  &__time {
    flex: none;
    font-size: 11.5px;
    color: $color-text-placeholder;
    font-variant-numeric: tabular-nums;
  }
}
</style>

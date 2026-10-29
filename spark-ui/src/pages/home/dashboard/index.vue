<template>
  <div class="app-container dashboard" v-loading="loading">
    <div class="dashboard__bar">
      <div class="greeting">
        <div class="greeting__title">{{ greetingText }}，{{ userName }}</div>
        <div class="greeting__sub">欢迎使用星火云应用平台，聚微光成智，燃无限可能！ · {{ todayText }}，这是您近 {{ rangeDays }} 天的工作台概览</div>
      </div>
      <el-radio-group v-model="rangeDays" @change="loadRanged">
        <el-radio-button :label="7">近7天</el-radio-button>
        <el-radio-button :label="30">近30天</el-radio-button>
        <el-radio-button :label="90">近90天</el-radio-button>
      </el-radio-group>
    </div>

    <div class="dashboard__body">
      <div class="dashboard__kpi">
        <StatCard label="模型调用总次数" :value="formatNumber(usageSummary?.callCount)" unit="次"
                  :sub="`近${rangeDays}天`" type="primary">
          <template #icon><el-icon><Promotion/></el-icon></template>
        </StatCard>
        <StatCard label="Token 总消耗" :value="formatNumber(usageSummary?.totalTokens)" unit="tokens"
                  :sub="`输入 ${formatNumber(usageSummary?.inputTokens)} · 输出 ${formatNumber(usageSummary?.outputTokens)}`"
                  type="success">
          <template #icon><el-icon><Coin/></el-icon></template>
        </StatCard>
        <StatCard label="模型" :value="asset?.modelCount ?? 0" unit="个" sub="点击使用"
                  type="primary" class="kpi-clickable" @click="goModel">
          <template #icon><el-icon><Cpu/></el-icon></template>
        </StatCard>
        <StatCard label="智能体" :value="asset?.agentCount ?? 0" unit="个" sub="点击使用"
                  type="success" class="kpi-clickable" @click="goAgent">
          <template #icon><AgentIcon/></template>
        </StatCard>
        <StatCard label="工作流" :value="asset?.workflowCount ?? 0" unit="个" sub="点击使用"
                  type="warning" class="kpi-clickable" @click="goWorkflow">
          <template #icon><WorkflowIcon/></template>
        </StatCard>
        <StatCard label="我的待办" :value="todoTotal" unit="件"
                  sub="点击查看处理" type="danger" class="kpi-clickable" @click="goTodo">
          <template #icon><el-icon><BellFilled/></el-icon></template>
        </StatCard>
      </div>

      <div class="dashboard__row dashboard__row--chart">
        <div class="panel trend-panel">
          <div class="panel__head">
            <div class="panel__title"><el-icon><Histogram/></el-icon><span>用量趋势</span></div>
          </div>
          <div ref="trendChartRef" class="trend-chart"/>
        </div>
        <RankCard title="热门推荐" :list="hotRecommend" unit="次" show-type-tag>
          <template #icon><StarFilled/></template>
        </RankCard>
      </div>

      <div class="dashboard__row dashboard__row--bottom">
        <div class="panel">
          <div class="panel__head">
            <div class="panel__title"><el-icon><BellFilled/></el-icon><span>我的流程</span></div>
          </div>
          <div ref="todoChartRef" class="todo-chart"/>
        </div>
        <div class="panel apply-panel">
          <div class="panel__head">
            <div class="panel__title"><el-icon><CirclePlusFilled/></el-icon><span>可申请流程</span></div>
            <el-button text type="primary" @click="goApply">全部</el-button>
          </div>
          <div v-if="applyTemplates.length" class="apply-list">
            <div v-for="item in applyTemplates" :key="item.id" class="apply-item" @click="goApply">
              <el-icon class="apply-item__icon"><CirclePlusFilled/></el-icon>
              <span class="apply-item__name" :title="item.name">{{ item.name }}</span>
            </div>
          </div>
          <el-empty v-else :image-size="80" description="暂无可申请流程" class="panel__empty"/>
        </div>
        <div class="panel todo-panel">
          <div class="panel__head">
            <div class="panel__title"><el-icon><BellFilled/></el-icon><span>我的待办</span></div>
            <el-badge :value="todoTotal">
              <el-button text type="primary" @click="goTodo">全部</el-button>
            </el-badge>
          </div>
          <div v-if="todoList.length" class="todo-list">
            <div v-for="item in todoList" :key="item.id" class="todo-item" @click="goTodo">
              <div class="todo-item__name" :title="item.name">{{ item.name }}</div>
              <el-tag size="small" :type="levelTagType(item.level)" effect="plain">{{ item.levelName || '一般' }}</el-tag>
            </div>
          </div>
          <el-empty v-else :image-size="80" description="暂无待办" class="panel__empty"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import {
  Promotion, Coin, BellFilled, Cpu, Histogram, StarFilled, CirclePlusFilled
} from '@element-plus/icons-vue'
import AgentIcon from '@/assets/icons/agent.vue'
import WorkflowIcon from '@/assets/icons/workflow.vue'
import {
  queryUsageSummaryAPI, queryUsageTrendAPI, queryTodoTotalAPI, queryTodoListAPI,
  queryFlowCountAPI, queryApplyTemplatesAPI, queryRecommendAPI, queryAssetAPI
} from '@/api/home/home'
import { useChart } from '@/utils/useChart'
import StatCard from '@/components/Dashboard/StatCard.vue'
import RankCard from '@/components/Dashboard/RankCard.vue'

const router = useRouter()
const store = useStore()
const loading = ref(false)
const rangeDays = ref(7)

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

const usageSummary = ref(null)
const usageTrend = ref([])
const todoTotal = ref(0)
const todoList = ref([])
const flowCount = ref({ todoCount: 0, doneCount: 0, applicationCount: 0, copyCount: 0 })
const applyTemplates = ref([])
const hotRecommend = ref([])
const asset = ref(null)

const { elRef: trendChartRef, setOption: setTrendOption } = useChart()
const { elRef: todoChartRef, setOption: setTodoOption } = useChart()

function rangeParams() {
  return { rangeDays: rangeDays.value }
}

function formatNumber(value) {
  const num = Number(value || 0)
  if (num >= 100000000) return `${(num / 100000000).toFixed(1)}亿`
  if (num >= 10000) return `${(num / 10000).toFixed(1)}万`
  return num
}

/**
 * 加载随时间范围变化的卡片
 */
function loadRanged() {
  const tasks = [
    queryUsageSummaryAPI(rangeParams()).then(res => { usageSummary.value = res.data }),
    queryRecommendAPI(rangeParams()).then(res => { hotRecommend.value = res.data || [] }),
    queryUsageTrendAPI(rangeParams()).then(res => {
      usageTrend.value = res.data || []
      nextTick(() => renderTrend())
    })
  ]
  return Promise.allSettled(tasks)
}

/**
 * 加载与时间范围无关的卡片
 */
function loadFixed() {
  const tasks = [
    queryTodoTotalAPI().then(res => { todoTotal.value = res.data ?? 0 }),
    queryTodoListAPI().then(res => { todoList.value = res.data || [] }),
    queryFlowCountAPI().then(res => {
      flowCount.value = res.data || flowCount.value
      nextTick(() => renderTodoChart())
    }),
    queryApplyTemplatesAPI().then(res => { applyTemplates.value = res.data || [] }),
    queryAssetAPI().then(res => { asset.value = res.data })
  ]
  return Promise.allSettled(tasks)
}

function buildDateLabels() {
  const labels = []
  const fullLabels = []
  const map = {}
  const now = new Date()
  for (let i = rangeDays.value - 1; i >= 0; i--) {
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
  const tokens = fullLabels.map(full => map[full].totalTokens)
  setTrendOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['调用次数', 'Token消耗'], right: 0, top: 0, textStyle: { color: '#5e6c84' } },
    grid: { left: 48, right: 56, top: 40, bottom: 32 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLine: { lineStyle: { color: '#eef2f8' } },
      axisLabel: { color: '#a0aec0' }
    },
    yAxis: [
      {
        type: 'value',
        name: '次数',
        splitLine: { lineStyle: { color: '#eef2f8' } },
        axisLabel: { color: '#a0aec0' }
      },
      {
        type: 'value',
        name: 'Token',
        splitLine: { show: false },
        axisLabel: { color: '#a0aec0' }
      }
    ],
    series: [
      {
        name: '调用次数',
        type: 'bar',
        barMaxWidth: 18,
        itemStyle: { color: '#004fc5', borderRadius: [3, 3, 0, 0] },
        data: calls
      },
      {
        name: 'Token消耗',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#34c759', width: 2 },
        itemStyle: { color: '#34c759' },
        areaStyle: { color: 'rgba(52,199,89,0.08)' },
        data: tokens
      }
    ]
  })
}

function levelTagType(level) {
  if (level === 3) return 'danger'
  if (level === 2) return 'warning'
  return 'info'
}

function renderTodoChart() {
  const c = flowCount.value
  const total = (c.todoCount || 0) + (c.doneCount || 0) + (c.applicationCount || 0) + (c.copyCount || 0)
  setTodoOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#5e6c84' } },
    title: {
      text: String(total),
      subtext: '流程总数',
      left: 'center',
      top: '34%',
      textStyle: { fontSize: 26, fontWeight: 600, color: '#004fc5' },
      subtextStyle: { fontSize: 12, color: '#a0aec0' }
    },
    series: [
      {
        name: '我的流程',
        type: 'pie',
        radius: ['50%', '70%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: false,
        label: { show: false },
        data: [
          { value: c.todoCount || 0, name: '待办', itemStyle: { color: '#f56c6c' } },
          { value: c.doneCount || 0, name: '已办', itemStyle: { color: '#34c759' } },
          { value: c.applicationCount || 0, name: '我的申请', itemStyle: { color: '#004fc5' } },
          { value: c.copyCount || 0, name: '抄送给我', itemStyle: { color: '#ff9500' } }
        ]
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

onMounted(() => {
  loading.value = true
  Promise.all([loadRanged(), loadFixed()]).finally(() => {
    loading.value = false
  })
})
</script>

<style scoped lang="scss">
.dashboard {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  height: calc(100vh - 70px);
  overflow: hidden;

  &__bar {
    flex: none;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-md $spacing-lg;
    background-color: $bg-card;
    border: 1px solid $border-color-light;
    border-radius: $border-radius-md;
    box-shadow: $shadow-card;
  }

  &__body {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: $spacing-md;
  }

  &__kpi {
    flex: none;
    display: grid;
    grid-template-columns: repeat(6, minmax(0, 1fr));
    gap: $spacing-sm;
  }

  &__row {
    flex: none;
    display: grid;
    gap: $spacing-md;
    align-items: stretch;
  }

  &__row--chart {
    grid-template-columns: 1.7fr 1fr;
  }

  &__row--bottom {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

.todo-chart {
  flex: 1;
  min-height: 0;
  width: 100%;
}

.apply-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-top: $spacing-sm;
}

.apply-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  height: 40px;
  padding: 0 $spacing-sm;
  border-radius: $border-radius-sm;
  cursor: pointer;
  transition: background-color $transition-normal;

  &:hover {
    background-color: $color-primary-light;
  }

  &__icon {
    flex: none;
    color: $color-primary;
  }

  &__name {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;
    color: $color-text-primary;
  }
}

.todo-panel {
  .todo-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: $spacing-sm;
    align-content: start;
  }
}

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

    .el-icon {
      color: $color-primary;
      font-size: 18px;
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

.greeting {
  &__title {
    font-size: 18px;
    font-weight: 600;
    color: $color-text-primary;
  }

  &__sub {
    margin-top: 4px;
    font-size: 12px;
    color: $color-text-secondary;
  }
}

.kpi-clickable {
  cursor: pointer;
}

.trend-chart {
  flex: 1;
  min-height: 0;
  width: 100%;
}

.todo-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-top: $spacing-sm;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-sm;
  min-width: 0;
  height: 48px;
  padding: 0 $spacing-md;
  background-color: $bg-page;
  border-radius: $border-radius-sm;
  cursor: pointer;
  transition: background-color $transition-normal;

  &:hover {
    background-color: $color-primary-light;
  }

  &__name {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;
    color: $color-text-primary;
  }
}

</style>

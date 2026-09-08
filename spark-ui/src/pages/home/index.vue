<template>
  <div class="app-container home-page">
    <el-row :gutter="20" class="home-row">
      <!-- 左侧：欢迎卡片 + 数据卡片 -->
      <el-col :span="18">
        <div class="left-col">
          <!-- 顶部欢迎卡片 -->
          <div class="welcome-card">
            <div class="welcome-left">
              <user-avatar :user-id="userId" :size="118" />
              <div class="welcome-text">
                <div class="greeting">Hello {{ userInfo.name }}（{{ userInfo.loginName }}），{{ greeting }} !</div>
                <div class="subtitle">
                  <span class="slogan">欢迎使用星火云应用平台，聚微光成智，燃无限可能</span>
                </div>
              </div>
            </div>
            <div class="welcome-right">
              <el-button
                  size="large"
                  class="welcome-btn"
                  type="primary"
                  @click="openProfile"
              >
                <el-icon><User /></el-icon>
                <span>个人中心</span>
              </el-button>
              <el-button
                  size="large"
                  class="welcome-btn"
                  type="danger"
                  @click="handleLogout"
              >
                <el-icon><SwitchButton /></el-icon>
                <span>退出系统</span>
              </el-button>
            </div>
          </div>

          <!-- 数据卡片 -->
          <div class="data-card-wrap">
            <div
              v-for="item in dataList"
              :key="item.name"
              class="data-card"
            >
              <div class="data-info">
                <div class="data-name">{{ item.name }}</div>
                <div class="data-value">{{ item.value }}</div>
              </div>
              <el-icon class="data-icon"><component :is="item.icon" /></el-icon>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 快捷功能入口 -->
      <el-col :span="6">
        <div class="shortcut-panel">
          <div class="section-title">快捷功能</div>
          <el-row v-if="visibleShortcuts.length" :gutter="12">
            <el-col v-for="item in visibleShortcuts" :key="item.path" :span="8">
              <div class="shortcut-card" @click="goShortcut(item.path)">
                <el-icon class="shortcut-icon"><component :is="item.icon" /></el-icon>
                <div class="shortcut-name">{{ item.name }}</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区：文件类型 / 抽取数量 / 实体新增 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 文件类型统计 - 饼状图 -->
      <el-col :span="8">
        <div class="chart-card">
          <div class="eval-header">
            <div class="eval-title">文件类型统计</div>
            <div class="eval-subtitle">知识库文档类型分布</div>
          </div>
          <div ref="fileTypeChartRef" class="chart-box"></div>
        </div>
      </el-col>

      <!-- 近7日抽取数量统计 - 柱状图 -->
      <el-col :span="8">
        <div class="chart-card">
          <div class="eval-header">
            <div class="eval-title">近 7 日抽取数量统计</div>
            <div class="eval-subtitle">三元组每日抽取量</div>
          </div>
          <div ref="extractChartRef" class="chart-box"></div>
        </div>
      </el-col>

      <!-- 近半年实体新增趋势 - 曲线图 -->
      <el-col :span="8">
        <div class="chart-card">
          <div class="eval-header">
            <div class="eval-title">近半年实体新增趋势</div>
            <div class="eval-subtitle">实体月度新增量</div>
          </div>
          <div ref="entityTrendChartRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>


    <!-- 评测区：知识库与知识图谱 -->
    <el-row :gutter="20" class="eval-row">
      <!-- 知识库解析与检索评测 -->
      <el-col :span="12">
        <div class="eval-card">
          <div class="eval-header">
            <div class="eval-title">知识库解析与检索评测</div>
            <div class="eval-subtitle">文档解析流水线状态</div>
          </div>
          <div class="eval-metrics with-progress">
            <div
                v-for="m in kbEvalMetrics"
                :key="m.name"
                class="eval-metric"
            >
              <div class="metric-row">
                <span class="metric-name">{{ m.name }}</span>
                <div class="metric-right">
                  <span class="metric-value">{{ m.value }}</span>
                  <el-icon v-if="m.trend === 'up'" class="trend-up"><CaretTop /></el-icon>
                  <span v-else-if="m.trend === '-'" class="trend-flat">-</span>
                </div>
              </div>
              <el-progress
                  :percentage="m.progress"
                  :stroke-width="6"
                  :show-text="false"
                  :color="m.color"
              />
            </div>
          </div>
        </div>
      </el-col>

      <!-- 知识图谱构建与提炼 -->
      <el-col :span="12">
        <div class="eval-card">
          <div class="eval-header">
            <div class="eval-title">知识图谱构建与提炼</div>
            <div class="eval-subtitle">三元组持续积累趋势</div>
          </div>
          <div ref="kgChartRef" class="kg-chart"></div>
          <div class="eval-metrics kg-metrics">
            <div
                v-for="m in kgEvalMetrics"
                :key="m.name"
                class="eval-metric"
            >
              <span class="metric-name">{{ m.name }}</span>
              <div class="metric-right">
                <span class="metric-value">{{ m.value }}</span>
                <el-icon v-if="m.trend === 'up'" class="trend-up"><CaretTop /></el-icon>
                <span v-else-if="m.trend === '-'" class="trend-flat">-</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区：AI应用使用 / 工作流执行 -->
    <el-row :gutter="20" class="eval-row">
      <!-- AI应用使用统计 - 柱状图 -->
      <el-col :span="12">
        <div class="eval-card">
          <div class="eval-header">
            <div class="eval-title">AI 应用使用统计</div>
            <div class="eval-subtitle">近 7 日各模块调用量</div>
          </div>
          <div ref="aiUsageChartRef" class="kg-chart"></div>
        </div>
      </el-col>

      <!-- 工作流执行统计 - 环形进度 -->
      <el-col :span="12">
        <div class="eval-card">
          <div class="eval-header">
            <div class="eval-title">工作流执行统计</div>
            <div class="eval-subtitle">本月工作流运行概况</div>
          </div>
          <div class="workflow-stats">
            <div class="workflow-ring-wrap">
              <div ref="workflowChartRef" class="workflow-ring"></div>
              <div class="workflow-ring-label">
                <div class="ring-value">86.5%</div>
                <div class="ring-name">执行成功率</div>
              </div>
            </div>
            <div class="workflow-metrics">
              <div
                  v-for="m in workflowMetrics"
                  :key="m.name"
                  class="workflow-metric"
              >
                <span class="metric-dot" :style="{ backgroundColor: m.color }"></span>
                <span class="metric-name">{{ m.name }}</span>
                <span class="metric-value">{{ m.value }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 个人中心弹窗 -->
    <user-profile v-model="profileVisible" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  SwitchButton,
  User,
  CaretTop
} from '@element-plus/icons-vue'
import { logoutAPI } from '@/api/manage/auth/login.js'
import UserAvatar from '@/components/UserAvatar'
import UserProfile from '@/components/UserProfile'
import FlowApplicationIcon from '@/assets/icons/flowApplication.vue'
import MyDoneIcon from '@/assets/icons/myDone.vue'
import KnowledgeIcon from '@/assets/icons/knowledge.vue'
import AgentIcon from '@/assets/icons/agent.vue'
import WorkflowIcon from '@/assets/icons/workflow.vue'
import ModelMarketIcon from '@/assets/icons/modelMarket.vue'
import GraphIcon from '@/assets/icons/graph.vue'
import {hasMenu} from '@/utils/menuUtil.js'
import SearchDocumentIcon from '@/assets/icons/searchDocument.vue'

const router = useRouter()
const store = useStore()
const userInfo = computed(() => store.getters['user/getUserInfo'] || {})
const userId = computed(() => userInfo.value.id)

// 数据卡片假数据
const dataList = ref([
  { name: '模型数量', value: 21, icon: ModelMarketIcon },
  { name: 'Agent数量', value: 12, icon: AgentIcon },
  { name: 'Workflow数量', value: 6, icon: WorkflowIcon },
  { name: '知识库数量', value: 30, icon: KnowledgeIcon },
  { name: '知识图谱数量', value: 20, icon: GraphIcon },
  { name: '文档数量', value: 8310, icon: SearchDocumentIcon }
])

// 快捷功能入口列表
const shortcutList = ref([
  { name: '流程申请', path: '/flow/application', icon: FlowApplicationIcon, menuId: 501 },
  { name: '我的待办', path: '/flow/myTodo', icon: MyDoneIcon, menuId: 504 },
  { name: '知识库', path: '/kb/knowledge', icon: KnowledgeIcon, menuId: 301 },
  { name: '知识图谱', path: '/kg/graph', icon: GraphIcon, menuId: 401 },
  { name: 'Agent', path: '/llm/agent', icon: AgentIcon, menuId: 201 },
  { name: '模型市场', path: '/llm/modelMarket', icon: ModelMarketIcon, menuId: 203 }
])

// 有菜单权限的快捷入口
const visibleShortcuts = computed(() => shortcutList.value.filter(item => hasMenu(item.menuId)))

// 知识库解析与检索评测假数据（后续接入真实接口后替换）
const kbEvalMetrics = ref([
  { name: '解析成功率（8,310 份）', value: '98.3%', trend: 'up', progress: 98.3 },
  { name: '多模态切片生成进度', value: '85%', trend: '', progress: 85 },
  { name: '向量化 Embedding 完成', value: '100%', trend: '', progress: 100 },
  { name: 'Top-3 平均召回率', value: '88.7%', trend: 'up', progress: 88.7 },
  { name: '混合检索模式占比', value: '76.0%', trend: '-', progress: 76.0 },
  { name: '检索超时告警', value: '0', trend: '', progress: 0, color: '#34c759' }
])

// 知识图谱构建与提炼假数据（后续接入真实接口后替换）
const kgEvalMetrics = ref([
  { name: '知识抽取准确率', value: '94.2%', trend: 'up' },
  { name: '实体对齐完成度', value: '96.5%', trend: 'up' },
  { name: '孤立实体节点数', value: '12', trend: '' }
])

// 三元组积累趋势曲线图容器引用
const kgChartRef = ref(null)

// 三元组积累趋势假数据（近 7 日累计三元组数量）
const kgTrendDates = ['07-12', '07-13', '07-14', '07-15', '07-16', '07-17', '07-18']
const kgTrendValues = [1820, 2145, 2510, 2980, 3420, 3890, 4356]

// 文件类型统计饼状图容器引用与假数据
const fileTypeChartRef = ref(null)
const fileTypeData = [
  { name: 'PDF', value: 2850 },
  { name: 'Word', value: 2280 },
  { name: 'Excel', value: 1460 },
  { name: 'PPT', value: 980 },
  { name: 'Markdown', value: 410 },
  { name: '其他', value: 330 }
]

// 近 7 日抽取数量柱状图容器引用与假数据
const extractChartRef = ref(null)
const extractDates = ['07-12', '07-13', '07-14', '07-15', '07-16', '07-17', '07-18']
const extractValues = [325, 398, 412, 286, 502, 455, 368]

// 近半年实体新增趋势曲线图容器引用与假数据
const entityTrendChartRef = ref(null)
const entityTrendMonths = ['2 月', '3 月', '4 月', '5 月', '6 月', '7 月']
const entityTrendValues = [1280, 1560, 1820, 2150, 2480, 2730]

/**
 * 渲染三元组积累趋势曲线图
 */
function renderKgChart() {
  if (!kgChartRef.value) {
    return
  }
  const chart = echarts.init(kgChartRef.value)
  chart.setOption({
    grid: { left: 36, right: 16, top: 16, bottom: 24 },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: kgTrendDates,
      axisLine: { lineStyle: { color: '#e4eaf4' } },
      axisLabel: { color: '#5e6c84', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#eef2f8' } },
      axisLabel: { color: '#5e6c84', fontSize: 11 }
    },
    series: [
      {
        name: '三元组数量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: kgTrendValues,
        lineStyle: { color: '#004fc5', width: 2 },
        itemStyle: { color: '#004fc5' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 79, 197, 0.25)' },
            { offset: 1, color: 'rgba(0, 79, 197, 0.02)' }
          ])
        }
      }
    ]
  })
}

/**
 * 渲染文件类型统计饼状图
 */
function renderFileTypeChart() {
  if (!fileTypeChartRef.value) {
    return
  }
  const chart = echarts.init(fileTypeChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: {
      bottom: 0,
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { color: '#5e6c84', fontSize: 11 }
    },
    color: ['#004fc5', '#34c759', '#ff9500', '#af52de', '#00c7be', '#5e6c84'],
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#ffffff', borderWidth: 2 },
        label: { show: false },
        labelLine: { show: false },
        data: fileTypeData
      }
    ]
  })
}

/**
 * 渲染近 7 日抽取数量柱状图
 */
function renderExtractChart() {
  if (!extractChartRef.value) {
    return
  }
  const chart = echarts.init(extractChartRef.value)
  chart.setOption({
    grid: { left: 36, right: 16, top: 16, bottom: 24 },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: extractDates,
      axisLine: { lineStyle: { color: '#e4eaf4' } },
      axisLabel: { color: '#5e6c84', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#eef2f8' } },
      axisLabel: { color: '#5e6c84', fontSize: 11 }
    },
    series: [
      {
        name: '抽取数量',
        type: 'bar',
        barWidth: '50%',
        data: extractValues,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#2c5aad' },
            { offset: 1, color: '#4f8cf7' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  })
}

/**
 * 渲染近半年实体新增趋势曲线图
 */
function renderEntityTrendChart() {
  if (!entityTrendChartRef.value) {
    return
  }
  const chart = echarts.init(entityTrendChartRef.value)
  chart.setOption({
    grid: { left: 36, right: 16, top: 16, bottom: 24 },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: entityTrendMonths,
      axisLine: { lineStyle: { color: '#e4eaf4' } },
      axisLabel: { color: '#5e6c84', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#eef2f8' } },
      axisLabel: { color: '#5e6c84', fontSize: 11 }
    },
    series: [
      {
        name: '实体新增',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: entityTrendValues,
        lineStyle: { color: '#34c759', width: 2 },
        itemStyle: { color: '#34c759' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(52, 199, 89, 0.25)' },
            { offset: 1, color: 'rgba(52, 199, 89, 0.02)' }
          ])
        }
      }
    ]
  })
}

// AI应用使用统计柱状图容器引用与假数据
const aiUsageChartRef = ref(null)
const aiUsageDates = ['08-19', '08-20', '08-21', '08-22', '08-23', '08-24', '08-25']
const aiUsageData = {
  agent: [125, 138, 96, 142, 168, 155, 120],
  knowledge: [280, 310, 265, 342, 298, 325, 290],
  workflow: [45, 52, 38, 62, 48, 55, 42]
}

// 工作流执行统计环形容器引用
const workflowChartRef = ref(null)

// 工作流执行统计指标
const workflowMetrics = ref([
  { name: '总执行次数', value: '1,256', color: '#004fc5' },
  { name: '成功次数', value: '1,087', color: '#34c759' },
  { name: '失败次数', value: '169', color: '#ff3b30' },
  { name: '平均耗时', value: '3.2s', color: '#ff9500' }
])

/**
 * 渲染AI应用使用统计柱状图
 */
function renderAiUsageChart() {
  if (!aiUsageChartRef.value) {
    return
  }
  const chart = echarts.init(aiUsageChartRef.value)
  chart.setOption({
    grid: { left: 40, right: 16, top: 16, bottom: 40 },
    tooltip: { trigger: 'axis' },
    legend: {
      bottom: 0,
      icon: 'roundRect',
      itemWidth: 12,
      itemHeight: 8,
      textStyle: { color: '#5e6c84', fontSize: 11 }
    },
    xAxis: {
      type: 'category',
      data: aiUsageDates,
      axisLine: { lineStyle: { color: '#e4eaf4' } },
      axisLabel: { color: '#5e6c84', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#eef2f8' } },
      axisLabel: { color: '#5e6c84', fontSize: 11 }
    },
    series: [
      {
        name: 'Agent',
        type: 'bar',
        barWidth: '20%',
        data: aiUsageData.agent,
        itemStyle: { color: '#004fc5', borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '知识库检索',
        type: 'bar',
        barWidth: '20%',
        data: aiUsageData.knowledge,
        itemStyle: { color: '#34c759', borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '工作流',
        type: 'bar',
        barWidth: '20%',
        data: aiUsageData.workflow,
        itemStyle: { color: '#ff9500', borderRadius: [4, 4, 0, 0] }
      }
    ]
  })
}

/**
 * 渲染工作流执行统计环形图
 */
function renderWorkflowChart() {
  if (!workflowChartRef.value) {
    return
  }
  const chart = echarts.init(workflowChartRef.value)
  chart.setOption({
    series: [
      {
        type: 'pie',
        radius: ['65%', '85%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        label: { show: false },
        labelLine: { show: false },
        data: [
          { value: 1087, name: '成功', itemStyle: { color: '#34c759' } },
          { value: 169, name: '失败', itemStyle: { color: '#ff3b30' } }
        ]
      }
    ]
  })
}

onMounted(() => {
  nextTick(() => {
    renderKgChart()
    renderFileTypeChart()
    renderExtractChart()
    renderEntityTrendChart()
    renderAiUsageChart()
    renderWorkflowChart()
  })
})

/**
 * 跳转快捷功能
 * @param path
 */
function goShortcut(path) {
  router.push(path).catch(() => {})
}

// 个人中心弹窗显隐
const profileVisible = ref(false)

// 根据当前时间生成问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

/**
 * 打开个人中心弹窗
 */
function openProfile() {
  profileVisible.value = true
}

/**
 * 退出系统
 */
function handleLogout() {
  ElMessageBox.confirm(
    '是否确定退出系统?',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    logoutAPI().then(() => {
      store.dispatch('user/logout')
      router.push({ path: '/login' }).catch(() => {})
    })
  }).catch(() => {})
}
</script>

<style scoped lang="scss">
.home-page {
  padding: $spacing-md;
  background-color: $bg-page;
  overflow: auto;
}

.home-row {
  align-items: stretch;
}

.left-col {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.welcome-card {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-xl $spacing-xl;
  background-color: $bg-card;
  border-radius: $border-radius-sm;
  box-shadow: $shadow-card;
}

.data-card-wrap {
  flex: 1;
  display: flex;
  gap: $spacing-md;
}

.data-card {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-lg $spacing-lg;
  background-color: $bg-card;
  border-radius: $border-radius-sm;
  box-shadow: $shadow-card;
  transition: $transition-normal;

  &:hover {
    box-shadow: $shadow-card-hover;
    transform: translateY(-2px);
  }
}

.data-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.data-name {
  font-size: 13px;
  color: $color-text-secondary;
}

.data-value {
  font-size: 26px;
  font-weight: 700;
  color: $color-text-primary;
  line-height: 1;
}

.data-icon {
  font-size: 36px;
}

.welcome-left {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.welcome-text {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.greeting {
  font-size: 22px;
  font-weight: 600;
  color: $color-text-primary;
  line-height: 1.4;
}

.subtitle {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 14px;
  color: $color-text-secondary;
}

.slogan {
  line-height: 1.4;
}

.welcome-right {
  display: flex;
  gap: $spacing-md;
}

.welcome-btn {
  height: 44px;
  padding: 0 $spacing-sm;
  font-size: 16px;
  font-weight: 400;

  span {
    color: $color-text-white;
  }

  .el-icon {
    color: $color-text-white;
  }
}

.shortcut-panel {
  padding: $spacing-md $spacing-lg;
  background-color: $bg-card;
  border-radius: $border-radius-sm;
  box-shadow: $shadow-card;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-sm;
  border-bottom: 1px solid $border-color-light;
}

.shortcut-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-lg 0;
  margin-bottom: $spacing-sm;
  background-color: $color-primary-soft;
  border-radius: $border-radius-sm;
  cursor: pointer;
  transition: $transition-normal;

  &:hover {
    background-color: $color-primary-light;
    transform: translateY(-2px);

    .shortcut-icon {
      color: $color-primary;
    }
    .shortcut-name {
      color: $color-primary;
    }
  }
}

.shortcut-icon {
  font-size: 30px;
  color: $color-text-secondary;
  margin-bottom: $spacing-xs;
  transition: $transition-fast;
}

.shortcut-name {
  font-size: 12px;
  color: $color-text-primary;
  transition: $transition-fast;
}

.eval-row {
  margin-top: $spacing-md;
}

.eval-card {
  background-color: $bg-card;
  border-radius: $border-radius-sm;
  box-shadow: $shadow-card;
  padding: $spacing-lg $spacing-xl;
}

.eval-header {
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-sm;
  border-bottom: 1px solid $border-color-light;
}

.eval-title {
  font-size: 16px;
  font-weight: 600;
  color: $color-text-primary;
}

.eval-subtitle {
  margin-top: $spacing-xs;
  font-size: 12px;
  color: $color-text-secondary;
}

.eval-metrics {
  display: flex;
  flex-direction: column;
}

.eval-metric {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-sm 0;
  font-size: 13px;

  & + & {
    border-top: 1px dashed $border-color-light;
  }
}

.with-progress .eval-metric {
  flex-direction: column;
  align-items: stretch;
}

.metric-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom: $spacing-xs;
}

.kg-metrics {
  margin-top: $spacing-md;
}

.kg-chart {
  width: 100%;
  height: 180px;
}

.chart-row {
  margin-top: $spacing-md;
}

.chart-card {
  background-color: $bg-card;
  border-radius: $border-radius-sm;
  box-shadow: $shadow-card;
  padding: $spacing-lg $spacing-xl;
}

.chart-box {
  width: 100%;
  height: 220px;
}

.metric-name {
  color: $color-text-secondary;
}

.metric-right {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.metric-value {
  font-size: 15px;
  font-weight: 600;
  color: $color-text-primary;
}

.trend-up {
  color: $agent-theme-green;
  font-size: 14px;
}

.trend-flat {
  color: $color-text-placeholder;
  font-size: 14px;
}

.workflow-stats {
  display: flex;
  align-items: center;
  gap: $spacing-xl;
}

.workflow-ring-wrap {
  position: relative;
  width: 160px;
  height: 160px;
  flex-shrink: 0;
}

.workflow-ring {
  width: 100%;
  height: 100%;
}

.workflow-ring-label {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.ring-value {
  font-size: 22px;
  font-weight: 700;
  color: $color-text-primary;
}

.ring-name {
  font-size: 12px;
  color: $color-text-secondary;
  margin-top: 2px;
}

.workflow-metrics {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.workflow-metric {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.metric-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.workflow-metric .metric-name {
  flex: 1;
  font-size: 13px;
  color: $color-text-secondary;
}

.workflow-metric .metric-value {
  font-size: 15px;
  font-weight: 600;
  color: $color-text-primary;
}
</style>

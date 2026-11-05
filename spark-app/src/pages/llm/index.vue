<template>
  <view class="llm-container safe-area-page" :class="themeClass">
    <!-- 顶部标题栏 -->
    <view class="llm-header" :style="navHeaderStyle()">
      <text class="header-title">AI应用</text>
      <view v-if="headTail" class="header-sub">
        <text v-if="headCount !== null" class="sub-num">{{ headCount }}</text>
        <text class="sub-text">{{ headTail }}</text>
      </view>
    </view>

    <!-- 分类页签 -->
    <view class="llm-tabs">
      <view class="tabs-track">
        <view
            v-for="(tab, index) in tabList"
            :key="index"
            class="seg-tab"
            :class="{ 'is-active': index === tabIndex }"
            @click="handleTabChange(index)"
        >
          <text class="seg-tab__text">{{ tab.name }}</text>
        </view>
      </view>
    </view>

    <!-- Agent列表 -->
    <scroll-view
        v-if="activeTabKey === 'agent'"
        class="llm-list"
        scroll-y
        @scrolltolower="loadMoreAgents"
    >
      <view v-if="agentLoading && agentList.length === 0" class="state-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <view v-else-if="agentList.length === 0" class="state-box state-box--empty">
        <EmptyState
            icon="star-fill"
            :title="emptyMeta.title"
            :desc="emptyMeta.sub"
            btn-text="刷新看看"
            @btn-click="refreshTab"
        />
      </view>

      <template v-else>
        <view class="section-head">
          <SectionTitle>全部智能体</SectionTitle>
          <text class="section-note">共 {{ agentTotal }} 个</text>
        </view>

        <view class="list-card">
          <template v-for="(item, index) in agentList" :key="item.id">
            <view class="row" @click="handleAgentClick(item)">
              <view class="row-icon">
                <AgentIcon :size="26"/>
              </view>
              <view class="row-main">
                <text class="row-name">{{ item.name }}</text>
                <text class="row-desc" :class="{ 'is-mute': !item.description }">
                  {{ item.description || '暂无描述' }}
                </text>
              </view>
              <view class="row-chev">
                <up-icon name="arrow-right" size="9" :color="chevronColor"/>
              </view>
            </view>
            <view v-if="index < agentList.length - 1" class="row-divider"></view>
          </template>
        </view>

        <view class="list-tip">{{ tipText(agentLoadStatus) }}</view>
      </template>
    </scroll-view>

    <!-- WorkFlow 运行实例列表 -->
    <scroll-view
        v-else-if="activeTabKey === 'workflow'"
        class="llm-list"
        scroll-y
        @scrolltolower="loadMoreInstances"
    >
      <view v-if="instanceLoading && instanceList.length === 0" class="state-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <view v-else-if="instanceList.length === 0" class="state-box state-box--empty">
        <EmptyState
            icon="list-dot"
            :title="emptyMeta.title"
            :desc="emptyMeta.sub"
            btn-text="刷新看看"
            @btn-click="refreshTab"
        />
      </view>

      <template v-else>
        <view class="section-head">
          <SectionTitle>运行记录</SectionTitle>
          <text class="section-note">共 {{ instanceTotal }} 条</text>
        </view>

        <view class="list-card">
          <template v-for="(item, index) in instanceList" :key="item.id">
            <view class="row" @click="showInstanceDetail(item)">
              <view class="row-icon">
                <WorkflowIcon :size="26"/>
              </view>
              <view class="row-main">
                <text class="row-name">{{ item.templateName || item.name || '未命名工作流' }}</text>
                <text class="row-desc">{{ instanceMeta(item) }}</text>
              </view>
              <text class="row-pill" :class="'pill-' + item.status">{{ item.statusName }}</text>
            </view>
            <view v-if="index < instanceList.length - 1" class="row-divider"></view>
          </template>
        </view>

        <view class="list-tip">{{ tipText(instanceLoadStatus) }}</view>
      </template>
    </scroll-view>

    <!-- AIChat：已启用的语言模型列表，点击进入对话 -->
    <scroll-view
        v-else-if="activeTabKey === 'chat'"
        class="llm-list"
        scroll-y
        @scrolltolower="loadMoreModels"
    >
      <view v-if="modelLoading && modelList.length === 0" class="state-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <view v-else-if="modelList.length === 0" class="state-box state-box--empty">
        <EmptyState
            icon="chat"
            :title="emptyMeta.title"
            :desc="emptyMeta.sub"
            btn-text="刷新看看"
            @btn-click="refreshTab"
        />
      </view>

      <template v-else>
        <view class="section-head">
          <SectionTitle>可用模型</SectionTitle>
          <text class="section-note">共 {{ modelTotal }} 个</text>
        </view>

        <view class="list-card">
          <template v-for="(model, index) in modelList" :key="model.id">
            <view class="row" @click="handleSelectModel(model)">
              <view class="row-icon">
                <ModelIcon :size="26"/>
              </view>
              <view class="row-main">
                <text class="row-name">{{ model.name }}</text>
                <text class="row-desc">{{ model.providerName ? '厂商：' + model.providerName : '未知厂商' }}</text>
              </view>
              <view class="row-talk">
                <text class="row-talk__text" :style="'color:' + brandColor">对话</text>
                <up-icon name="arrow-right" size="8" :color="brandColor"/>
              </view>
            </view>
            <view v-if="index < modelList.length - 1" class="row-divider"></view>
          </template>
        </view>

        <view class="list-tip">{{ tipText(modelLoadStatus) }}</view>
      </template>
    </scroll-view>

    <!-- 无任何页签权限时展示空态 -->
    <view v-else class="state-box state-box--empty">
      <EmptyState icon="grid-fill" :title="emptyMeta.title" :desc="emptyMeta.sub"/>
    </view>

    <!-- 弹层宿主：小程序端 picker 内部 popup 的插槽内容会参与 flex 分配顶高底栏 -->
    <view class="sheet-host">
      <ai-session-picker
          v-model:show="sessionPickerShow"
          :title="sessionPickerTitle"
          :target-type="sessionTargetType"
          :receiver-id="sessionTarget.id"
          @select="handleSessionSelect"
          @new="handleSessionNew"
      />
    </view>

    <AppTabbar :value="active" @change="handleOnTabChange"/>
  </view>
</template>

<script setup>
import {ref, computed} from "vue"
import {onLoad, onShow} from "@dcloudio/uni-app"
import {useStore} from "vuex"
import {navHeaderStyle} from "@/utils/navBarUtil";
import {getHeaderIconColor, getChevronColor} from "@/utils/themeUtil"
import {agentPageListAPI} from "@/api/llm/agent"
import {pageInstanceListAPI} from "@/api/workflow/instance"
import {pageModelListAPI} from "@/api/llm/model"
import AiSessionPicker from "@/components/AiSessionPicker/index.vue"
import {useAiSessionPicker} from "@/components/AiSessionPicker/aiSessionUtil"
import {useTheme} from "@/utils/useTheme"
import AppTabbar from "@/components/AppTabbar/index.vue";
import SectionTitle from "@/components/SectionTitle/index.vue";
import EmptyState from "@/components/EmptyState/index.vue";
import AgentIcon from "@/components/icons/AgentIcon.vue"
import WorkflowIcon from "@/components/icons/WorkflowIcon.vue"
import ModelIcon from "@/components/icons/ModelIcon.vue"

const store = useStore()
const active = ref("llm")
const {isDark, themeClass} = useTheme()

const brandColor = computed(() => getHeaderIconColor(isDark.value))
const chevronColor = computed(() => getChevronColor(isDark.value))


// 会话选择弹层：点击智能体/模型后先选会话再进入对话
const {
  show: sessionPickerShow,
  title: sessionPickerTitle,
  target: sessionTarget,
  targetType: sessionTargetType,
  open: openSessionPicker,
  handleSelect: handleSessionSelect,
  handleNew: handleSessionNew
} = useAiSessionPicker()


// AI应用页签配置：智能体放第一位，是平台最常用的入口
const allowedTabs = [
  {key: 'agent', name: '智能体'},
  {key: 'workflow', name: '工作流'},
  {key: 'chat', name: 'AI对话'}
]

const tabList = computed(() => allowedTabs.map(tab => ({name: tab.name})))
const tabIndex = ref(0)
// 当前激活页签 key
const activeTabKey = computed(() => {
  const tab = allowedTabs[tabIndex.value]
  return tab ? tab.key : ''
})

// 列表每页条数
const PAGE_SIZE = 15

// Agent 相关
const agentList = ref([])
const agentLoading = ref(false)
const agentLoaded = ref(false)
const agentQuery = ref({
  pageNo: 1,
  pageSize: PAGE_SIZE,
  status: 1
})
const agentTotal = ref(0)
const agentLoadedCount = ref(0)
// 列表底部加载提示状态：loadmore 上拉加载更多 / loading 加载中 / nomore 没有更多了
const agentLoadStatus = ref('loadmore')

// WorkFlow 实例相关
const instanceList = ref([])
const instanceLoading = ref(false)
const instanceLoaded = ref(false)
const instanceQuery = ref({
  pageNo: 1,
  pageSize: PAGE_SIZE
})
const instanceTotal = ref(0)
// 列表底部加载提示状态
const instanceLoadStatus = ref('loadmore')

// AIChat 模型列表相关
const modelList = ref([])
const modelLoading = ref(false)
const modelLoaded = ref(false)
const modelQuery = ref({
  pageNo: 1,
  pageSize: PAGE_SIZE,
  status: 1,
  // 只展示语言模型
  type: 1
})
const modelTotal = ref(0)
const modelLoadedCount = ref(0)
// 列表底部加载提示状态
const modelLoadStatus = ref('loadmore')

// 头部副标题：数字用品牌色单独一段，无数据时只留文案
const headCount = computed(() => {
  if (activeTabKey.value === 'agent') {
    return agentLoaded.value && agentTotal.value > 0 ? agentTotal.value : null
  }
  if (activeTabKey.value === 'workflow') {
    return instanceLoaded.value && instanceTotal.value > 0 ? instanceTotal.value : null
  }
  if (activeTabKey.value === 'chat') {
    return modelLoaded.value && modelTotal.value > 0 ? modelTotal.value : null
  }
  return null
})

const headTail = computed(() => {
  const loaded = {agent: agentLoaded.value, workflow: instanceLoaded.value, chat: modelLoaded.value}
  if (activeTabKey.value === 'agent') {
    if (!loaded.agent) return '正在加载智能体…'
    return agentTotal.value > 0 ? '个可用智能体' : '还没有可用的智能体'
  }
  if (activeTabKey.value === 'workflow') {
    if (!loaded.workflow) return '正在加载运行记录…'
    return instanceTotal.value > 0 ? '条运行记录' : '还没有运行记录'
  }
  if (activeTabKey.value === 'chat') {
    if (!loaded.chat) return '正在加载模型…'
    return modelTotal.value > 0 ? '个可用模型' : '还没有可用的模型'
  }
  return ''
})

// 空态文案按页签区分，说明「为什么空」和「缺什么权限」
const emptyMeta = computed(() => {
  if (activeTabKey.value === 'workflow') {
    return {title: '还没有运行记录', sub: '运行过工作流后，记录会按时间顺序出现在这里'}
  }
  if (activeTabKey.value === 'chat') {
    return {title: '还没有可用的模型', sub: '管理员启用语言模型后，就能在这里开始对话'}
  }
  return {title: '还没有可用的智能体', sub: '管理员为你开通智能体后，就能在这里和它对话了'}
})

/**
 * 按页签key懒加载对应列表
 * @param key 页签key，取 chat、agent、workflow
 */
function loadTab(key) {
  if (key === 'agent' && !agentLoading.value && !agentLoaded.value) {
    getAgentList()
  } else if (key === 'workflow' && !instanceLoading.value && !instanceLoaded.value) {
    loadInstances()
  } else if (key === 'chat' && !modelLoading.value && !modelLoaded.value) {
    loadModels()
  }
}

/**
 * 切换页签
 * @param index 页签下标
 */
function handleTabChange(index) {
  if (index === tabIndex.value) {
    return
  }
  tabIndex.value = index
  const tabDef = allowedTabs[index]
  if (tabDef) {
    loadTab(tabDef.key)
  }
}

/**
 * 页面加载：支持从首页带 tab 参数直达指定页签，下标 0 智能体、1 工作流、2 AI对话
 * @param options 页面参数
 */
onLoad(options => {
  const index = Number(options && options.tab)
  if (!Number.isNaN(index) && index >= 0 && index < allowedTabs.length) {
    tabIndex.value = index
  }
})

// 页面显示时懒加载当前可见页签
onShow(async () => {
  const userInfo = store.getters['user/getUserInfo']
  if (!userInfo || userInfo.appAiEnabled !== true) {
    uni.reLaunch({url: '/pages/error/noPermission'})
    return
  }
  loadTab(activeTabKey.value)
})

/**
 * 列表底部提示文案
 * @param status loadmore / loading / nomore
 * @returns {string} 提示文字
 */
function tipText(status) {
  if (status === 'loading') {
    return '正在加载...'
  }
  if (status === 'nomore') {
    return '没有更多了'
  }
  return '上拉加载更多'
}

/**
 * 智能体列表
 */
function getAgentList() {
  agentLoading.value = true
  agentLoadStatus.value = 'loading'
  agentQuery.value.pageNo = 1
  agentPageListAPI(agentQuery.value).then(res => {
    if (res.code !== 200 || res.data === undefined || res.data === null) {
      agentLoadStatus.value = 'nomore'
      agentLoaded.value = true
      return
    }
    const rows = res.data.rows || []
    agentList.value = rows.map(row => ({...row}))
    agentTotal.value = res.data.total || 0
    agentLoadedCount.value = rows.length
    agentLoaded.value = true
    agentLoadStatus.value = agentLoadedCount.value >= agentTotal.value ? 'nomore' : 'loadmore'
  }).finally(() => {
    agentLoading.value = false
  })
}

/**
 * 加载更多智能体
 */
function loadMoreAgents() {
  if (agentLoading.value || agentLoadStatus.value === 'nomore') return
  agentQuery.value.pageNo++
  agentLoading.value = true
  agentLoadStatus.value = 'loading'
  agentPageListAPI(agentQuery.value).then(res => {
    if (res.code === 200 && res.data) {
      const rows = (res.data.rows || []).map(row => ({...row}))
      agentList.value = agentList.value.concat(rows)
      agentLoadedCount.value += rows.length
      agentLoadStatus.value = agentLoadedCount.value >= agentTotal.value ? 'nomore' : 'loadmore'
    } else {
      agentLoadStatus.value = 'nomore'
    }
  }).finally(() => {
    agentLoading.value = false
  })
}

/**
 * 点击智能体：先选会话记录再进入对话
 */
function handleAgentClick(item) {
  openSessionPicker(item, 'agent')
}

/**
 * 运行实例的副标题：创建时间 + 耗时，两块信息都从接口来
 * @param item 实例行
 * @returns {string} 副标题
 */
function instanceMeta(item) {
  const parts = []
  const dt = formatDt(item.createdDt)
  if (dt) {
    parts.push(dt)
  }
  const duration = formatDuration(item.durationMs)
  if (duration) {
    parts.push('耗时 ' + duration)
  }
  return parts.length > 0 ? parts.join(' · ') : '暂无运行信息'
}

/**
 * 时间截成 MM-DD HH:mm，年份相同不必占位
 * @param value 后端时间串
 * @returns {string} 短时间
 */
function formatDt(value) {
  if (!value) {
    return '暂无时间'
  }
  const text = String(value).replace('T', ' ')
  const matched = text.match(/^(\d{4})-(\d{2})-(\d{2})[ ]?(\d{2}):(\d{2})/)
  if (matched) {
    return matched[2] + '-' + matched[3] + ' ' + matched[4] + ':' + matched[5]
  }
  return text
}

/**
 * 毫秒转可读耗时
 * @param ms 耗时毫秒
 * @returns {string} 如 820ms / 3.2s / 1m20s
 */
function formatDuration(ms) {
  if (ms === undefined || ms === null || ms <= 0) {
    return ''
  }
  if (ms < 1000) {
    return ms + 'ms'
  }
  if (ms < 60000) {
    return (ms / 1000).toFixed(1) + 's'
  }
  const minutes = Math.floor(ms / 60000)
  const seconds = Math.round((ms % 60000) / 1000)
  return minutes + 'm' + seconds + 's'
}

/**
 * 加载运行实例列表
 */
function loadInstances() {
  instanceLoading.value = true
  instanceLoadStatus.value = 'loading'
  instanceQuery.value.pageNo = 1
  pageInstanceListAPI(instanceQuery.value).then(res => {
    if (res.code === 200 && res.data) {
      instanceList.value = (res.data.rows || []).map(row => ({...row}))
      instanceTotal.value = res.data.total || 0
      instanceLoaded.value = true
      instanceLoadStatus.value = instanceList.value.length >= instanceTotal.value ? 'nomore' : 'loadmore'
    } else {
      instanceLoadStatus.value = 'nomore'
      instanceLoaded.value = true
    }
  }).finally(() => {
    instanceLoading.value = false
  })
}

/**
 * 加载更多实例
 */
function loadMoreInstances() {
  if (instanceLoading.value || instanceLoadStatus.value === 'nomore') return
  instanceQuery.value.pageNo++
  instanceLoading.value = true
  instanceLoadStatus.value = 'loading'
  pageInstanceListAPI(instanceQuery.value).then(res => {
    if (res.code === 200 && res.data) {
      const rows = (res.data.rows || []).map(row => ({...row}))
      instanceList.value = instanceList.value.concat(rows)
      instanceLoadStatus.value = instanceList.value.length >= instanceTotal.value ? 'nomore' : 'loadmore'
    } else {
      instanceLoadStatus.value = 'nomore'
    }
  }).finally(() => {
    instanceLoading.value = false
  })
}

/**
 * 显示实例详情
 */
function showInstanceDetail(item) {
  uni.navigateTo({
    url: '/pages/llm/wfInstanceDetail?id=' + item.id
  })
}

/**
 * 加载模型列表
 */
function loadModels() {
  modelLoading.value = true
  modelLoadStatus.value = 'loading'
  modelQuery.value.pageNo = 1
  pageModelListAPI(modelQuery.value).then(res => {
    if (res.code === 200 && res.data) {
      const rows = res.data.rows || []
      modelList.value = rows.map(row => ({...row}))
      modelTotal.value = res.data.total || 0
      modelLoadedCount.value = rows.length
      modelLoaded.value = true
      modelLoadStatus.value = modelLoadedCount.value >= modelTotal.value ? 'nomore' : 'loadmore'
    } else {
      modelLoadStatus.value = 'nomore'
      modelLoaded.value = true
    }
  }).finally(() => {
    modelLoading.value = false
  })
}

/**
 * 加载更多模型
 */
function loadMoreModels() {
  if (modelLoading.value || modelLoadStatus.value === 'nomore') return
  modelQuery.value.pageNo++
  modelLoading.value = true
  modelLoadStatus.value = 'loading'
  pageModelListAPI(modelQuery.value).then(res => {
    if (res.code === 200 && res.data) {
      const rows = (res.data.rows || []).map(row => ({...row}))
      modelList.value = modelList.value.concat(rows)
      modelLoadedCount.value += rows.length
      modelLoadStatus.value = modelLoadedCount.value >= modelTotal.value ? 'nomore' : 'loadmore'
    } else {
      modelLoadStatus.value = 'nomore'
    }
  }).finally(() => {
    modelLoading.value = false
  })
}

/**
 * 选择模型：先选会话记录再进入对话
 */
function handleSelectModel(model) {
  openSessionPicker(model, 'model')
}

/**
 * 空态里的刷新：清掉已加载标记后重新拉当前页签
 */
function refreshTab() {
  const key = activeTabKey.value
  if (key === 'agent' && !agentLoading.value) {
    agentLoaded.value = false
    getAgentList()
  } else if (key === 'workflow' && !instanceLoading.value) {
    instanceLoaded.value = false
    loadInstances()
  } else if (key === 'chat' && !modelLoading.value) {
    modelLoaded.value = false
    loadModels()
  }
}

/**
 * 底部导航栏
 */
function handleOnTabChange(index) {
  uni.reLaunch({
    url: '/pages/' + index + '/index'
  })
}
</script>

<style scoped lang="scss">
.llm-container {
  height: 100vh;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: $app-bg-page;
}

.llm-header {
  padding: 14px 16px 10px;
  background-color: $app-header-bg;
  flex-shrink: 0;

  .header-title {
    display: block;
    font-size: 24px;
    font-weight: 700;
    letter-spacing: 0.5px;
    color: $app-text-primary;
  }
}

.header-sub {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 5px;
  padding-left: 1px;
}

.sub-num {
  font-size: 11.5px;
  font-weight: 600;
  color: $app-brand;
}

.sub-text {
  font-size: 11.5px;
  color: $app-text-tertiary;
}

.llm-tabs {
  flex-shrink: 0;
  padding: 2px 14px 10px;
  background-color: $app-header-bg;
}

.tabs-track {
  display: flex;
  gap: 3px;
  height: 38px;
  padding: 3px;
  border-radius: 12px;
  background-color: $app-tab-track;
}

.seg-tab {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  transition: background 0.22s ease, box-shadow 0.22s ease;
}

.seg-tab__text {
  font-size: 13px;
  color: $app-text-secondary;
  transition: color 0.22s ease;
}

.seg-tab.is-active {
  background-image: $app-gradient-fill;
  box-shadow: $app-shadow-brand;

  .seg-tab__text {
    color: $app-fill-tx;
    font-weight: 600;
  }
}

.llm-list {
  width: 100%;
  padding: 0 14px;
  box-sizing: border-box;
  flex: 1;
  height: 0;
  min-height: 0;
}

.state-box {
  padding-top: 90px;
  display: flex;
  justify-content: center;
}

.state-box--empty {
  padding-top: 18px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 22px;
  margin: 16px 2px 10px;
}

.section-note {
  font-size: 11px;
  color: $app-text-tertiary;
}

.list-card {
  background-color: $app-bg-card;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: $app-shadow-card;
}

.row {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 11px 14px;

  &:active {
    background-color: $app-bg-hover;
  }
}

.row-icon {
  flex: none;
  width: 42px;
  height: 42px;
  border-radius: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: $app-bg-sub;
  border: 1px solid $app-border-light;
}

.row-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.row-name {
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.2px;
  color: $app-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row-desc {
  font-size: 11.5px;
  line-height: 16px;
  color: $app-text-tertiary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  &.is-mute {
    color: $app-text-placeholder;
  }
}

.row-chev {
  flex: none;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: $app-bg-sub;
}

.row-pill {
  flex: none;
  padding: 1px 8px;
  border-radius: 7px;
  font-size: 11px;
  line-height: 17px;

  &.pill-1 {
    color: $app-text-info;
    background-color: $app-bg-info;
  }
  &.pill-2 {
    color: $app-text-success;
    background-color: $app-bg-success;
  }
  &.pill-3 {
    color: $app-text-error;
    background-color: $app-bg-error;
  }
  &.pill-4 {
    color: $app-text-warning;
    background-color: $app-bg-warning;
  }
  &.pill-5 {
    color: $app-text-tertiary;
    background-color: $app-bg-sub;
  }
}

.row-talk {
  flex: none;
  display: flex;
  align-items: center;
  gap: 2px;
  height: 26px;
  padding: 0 9px;
  border-radius: 8px;
  background-color: $app-primary-soft;
}

.row-talk__text {
  font-size: 11.5px;
  font-weight: 600;
}

.row-divider {
  height: 1px;
  margin-left: 67px;
  background-color: $app-border-light;
}

.list-tip {
  padding: 14px 0 26px;
  text-align: center;
  font-size: 11.5px;
  color: $app-text-tertiary;
}

</style>

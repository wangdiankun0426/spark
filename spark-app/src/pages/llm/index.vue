<template>
  <view class="llm-container safe-area-page">
    <!-- 顶部标题栏 -->
    <view class="llm-header" :style="navHeaderStyle()">
      <text class="header-title">AI应用</text>
    </view>

    <!-- 分类页签 -->
    <view class="llm-tabs">
      <up-tabs
          :list="tabList"
          :current="tabIndex"
          lineColor="#2f7bff"
          :lineWidth="tabLineWidth"
          :activeStyle="{color: '#2f7bff', fontWeight: 700}"
          :scrollable="false"
          @change="handleTabChange"/>
    </view>

    <!-- Agent列表 -->
    <scroll-view
        v-if="activeTabKey === 'agent'"
        class="llm-list"
        scroll-y
        @scrolltolower="loadMoreAgents"
    >
      <!-- 加载中 -->
      <view v-if="agentLoading" class="loading-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <!-- 空状态 -->
      <view v-else-if="agentList.length === 0" class="empty-box">
        <up-empty text="暂无智能体" icon="grid-fill"/>
      </view>

      <!-- 智能体卡片：只展示名称与模型 -->
      <template v-else>
        <view
            class="llm-card"
            v-for="item in agentList"
            :key="item.id"
            @click="handleAgentClick(item)"
        >
          <view class="card-icon">
            <up-icon name="grid-fill" size="34" color="#2f7bff"></up-icon>
          </view>
          <view class="llm-info">
            <text class="llm-name">{{ item.name }}</text>
            <text class="llm-desc">{{ item.description || '暂无备注' }}</text>
          </view>
        </view>
        <up-loadmore :status="agentLoadStatus" class="list-loadmore"/>
      </template>
    </scroll-view>

    <!-- WorkFlow 运行实例列表 -->
    <scroll-view
        v-else-if="activeTabKey === 'workflow'"
        class="llm-list"
        scroll-y
        @scrolltolower="loadMoreInstances"
    >
      <!-- 加载中 -->
      <view v-if="instanceLoading && instanceList.length === 0" class="loading-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <!-- 空状态 -->
      <view v-else-if="instanceList.length === 0" class="empty-box">
        <up-empty text="暂无运行记录" icon="list"/>
      </view>

      <!-- 实例卡片：展示名称、创建时间与运行状态 -->
      <template v-else>
        <view
            class="instance-card"
            v-for="item in instanceList"
            :key="item.id"
            @click="showInstanceDetail(item)"
        >
          <view class="card-icon">
            <up-icon name="share-fill" size="34" color="#2f7bff"></up-icon>
          </view>
          <view class="instance-info">
            <text class="instance-name">{{ item.templateName || item.name || '未命名工作流' }}</text>
            <text class="instance-created">{{ item.createdDt }}</text>
          </view>
          <text class="instance-status" :class="'status-' + item.status">{{ item.statusName }}</text>
        </view>
        <up-loadmore :status="instanceLoadStatus" class="list-loadmore"/>
      </template>
    </scroll-view>

    <!-- AIChat：已启用的语言模型列表，点击进入对话 -->
    <scroll-view
        v-else-if="activeTabKey === 'chat'"
        class="llm-list"
        scroll-y
        @scrolltolower="loadMoreModels"
    >
      <!-- 加载中 -->
      <view v-if="modelLoading && modelList.length === 0" class="loading-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <!-- 空状态 -->
      <view v-else-if="modelList.length === 0" class="empty-box">
        <up-empty text="暂无可用模型" icon="list"/>
      </view>

      <!-- 模型卡片：只展示名称与厂商 -->
      <template v-else>
        <view
            class="model-card"
            v-for="model in modelList"
            :key="model.id"
            @click="handleSelectModel(model)"
        >
          <view class="card-icon">
            <up-icon name="chat-fill" size="34" color="#2f7bff"></up-icon>
          </view>
          <view class="model-info">
            <text class="model-name">{{ model.name }}</text>
            <text class="model-provider">{{ model.providerName ? '厂商：' + model.providerName : '未知厂商' }}</text>
          </view>
        </view>
        <up-loadmore :status="modelLoadStatus" class="list-loadmore"/>
      </template>
    </scroll-view>

    <!-- 无任何页签权限时展示空态 -->
    <view v-else class="empty-box">
      <up-empty text="暂无可用功能" icon="grid-fill"/>
    </view>

    <!-- 会话记录选择 -->
    <ai-session-picker
        v-model:show="sessionPickerShow"
        :title="sessionPickerTitle"
        :target-type="sessionTargetType"
        :receiver-id="sessionTarget.id"
        @select="handleSessionSelect"
        @new="handleSessionNew"
    />

    <up-tabbar :value="active" @change="handleOnTabChange" activeColor="#004fc5">
      <up-tabbar-item
          v-for="tab in tabBarItems"
          :key="tab.name"
          :name="tab.name"
          :icon="tab.icon"
          :text="tab.text"
      />
    </up-tabbar>
  </view>
</template>

<script setup>
import {ref, computed} from "vue"
import {onLoad, onShow} from "@dcloudio/uni-app"
import {visibleTabs} from "@/utils/menuUtil";
import {navHeaderStyle} from "@/utils/navBarUtil";
import {agentPageListAPI} from "@/api/llm/agent"
import {pageInstanceListAPI} from "@/api/workflow/instance"
import {pageModelListAPI} from "@/api/llm/model"
import AiSessionPicker from "@/components/AiSessionPicker/index.vue"
import {useAiSessionPicker} from "@/components/AiSessionPicker/aiSessionUtil"

const active = ref("llm")

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

// 按菜单权限过滤后的底部导航项
const tabBarItems = computed(() => visibleTabs())

// AI应用页签配置
const allowedTabs = [
  {key: 'chat', name: 'AIChat'},
  {key: 'agent', name: 'Agent'},
  {key: 'workflow', name: 'WorkFlow'}
]

const tabList = computed(() => allowedTabs.map(tab => ({name: tab.name})))
// 下划线宽度取页签的六成并自动居中：不可滚动时页签平分屏宽
const tabLineWidth = Math.floor(uni.getSystemInfoSync().windowWidth / allowedTabs.length * 0.6)
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

/**
 * 按页签key懒加载对应列表
 * @param key 页签key（chat/agent/workflow）
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
 */
function handleTabChange(tab) {
  const index = tab.index
  tabIndex.value = index
  const tabDef = allowedTabs[index]
  if (tabDef) {
    loadTab(tabDef.key)
  }
}

/**
 * 页面加载：支持从首页带 tab 参数直达指定页签（下标 0 AIChat / 1 Agent / 2 WorkFlow）
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
  loadTab(activeTabKey.value)
})

/**
 * 获取智能体列表
 */
function getAgentList() {
  agentLoading.value = true
  agentLoadStatus.value = 'loading'
  agentPageListAPI(agentQuery.value).then(res => {
    if (res.code !== 200) {
      agentLoadStatus.value = 'nomore'
      return
    }
    if (res.data === undefined || res.data === null) {
      agentLoadStatus.value = 'nomore'
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
 * 加载运行实例列表
 */
function loadInstances() {
  instanceLoading.value = true
  instanceLoadStatus.value = 'loading'
  pageInstanceListAPI(instanceQuery.value).then(res => {
    if (res.code === 200 && res.data) {
      instanceList.value = (res.data.rows || []).map(row => ({...row}))
      instanceTotal.value = res.data.total || 0
      instanceLoaded.value = true
      instanceLoadStatus.value = instanceList.value.length >= instanceTotal.value ? 'nomore' : 'loadmore'
    } else {
      instanceLoadStatus.value = 'nomore'
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
  background-color: $uni-bg-color-dashboard;
}

.llm-container :deep(.u-tabbar) {
  flex: none;
}

.llm-header {
  padding: 16px 16px;
  background-color: $spark-color-header-bg;
  border-bottom: 1rpx solid $spark-color-header-border;
  flex-shrink: 0;
  .header-title {
    font-size: 20px;
    font-style: italic;
    font-weight: 700;
    letter-spacing: 1px;
    /* #ifdef MP-WEIXIN */
    color: #2f7bff;
    /* #endif */
    /* #ifdef H5 */
    background-image: linear-gradient(92deg, #2563eb 0%, #3d8bff 100%);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    /* #endif */
  }
}

.llm-tabs {
  background-color: #fff;
  flex-shrink: 0;
}

.llm-list {
  width: 94%;
  margin: 0 auto;
  padding-top: 12px;
  /* #ifdef H5 */
  height: calc(100vh - 170px);
  /* #endif */
  /* #ifdef MP-WEIXIN */
  height: calc(100vh - 250px);
  /* #endif */
  overflow-y: auto;
}

.list-loadmore {
  padding-bottom: 30px;
}

.loading-box, .empty-box {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding-top: 80px;
}

/* Agent 卡片样式 */
/* Agent 卡片样式：一行图标 + 名称 + 模型，与 AIChat 卡片保持一致 */
.llm-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.llm-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.llm-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.llm-desc {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* WorkFlow 卡片样式：一行图标 + 名称 + 运行状态 */
.instance-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background-color: rgba(47, 123, 255, 0.1);
  border: 1rpx solid rgba(47, 123, 255, 0.18);
}

.instance-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.instance-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.instance-created {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.instance-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  flex-shrink: 0;

  &.status-1 {
    background-color: #e6f7ff;
    color: #0052cc;
  }
  &.status-2 {
    background-color: #f6ffed;
    color: #52c41a;
  }
  &.status-3 {
    background-color: #fff7e6;
    color: #faad14;
  }
  &.status-4 {
    background-color: #fff2f0;
    color: #ff4d4f;
  }
}

/* AIChat 模型卡片样式：一行图标 + 名称 + 厂商 */
.model-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.model-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.model-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-provider {
  font-size: 12px;
  color: #999;
}
</style>

<template>
  <view class="llm-container safe-area-page">
    <!-- 顶部标题栏 -->
    <view class="llm-header">
      <text class="header-title">AI应用</text>
    </view>

    <!-- 分类页签 -->
    <view class="llm-tabs">
      <up-tabs
          :list="tabList"
          :current="tabIndex"
          lineColor="#0052cc"
          :scrollable="false"
          @change="handleTabChange"/>
    </view>

    <!-- Agent列表 -->
    <scroll-view v-if="activeTabKey === 'agent'" class="llm-list" scroll-y>
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
          <!-- 卡片图标：本页签统一用同一图标，底色按条目取色区分 -->
          <view class="card-icon" :style="item.iconStyle">
            <up-icon name="grid-fill" size="20" color="#ffffff"></up-icon>
          </view>
          <view class="llm-info">
            <text class="llm-name">{{ item.name }}</text>
            <text class="llm-model">{{ item.chatModelName || '未配置模型' }}</text>
          </view>
        </view>
      </template>
    </scroll-view>

    <!-- WorkFlow 运行实例列表 -->
    <scroll-view v-else-if="activeTabKey === 'workflow'" class="llm-list" scroll-y @scrolltolower="loadMoreInstances">
      <!-- 加载中 -->
      <view v-if="instanceLoading && instanceList.length === 0" class="loading-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <!-- 空状态 -->
      <view v-else-if="instanceList.length === 0" class="empty-box">
        <up-empty text="暂无运行记录" icon="list"/>
      </view>

      <!-- 实例卡片：只展示名称与运行状态 -->
      <template v-else>
        <view
            class="instance-card"
            v-for="item in instanceList"
            :key="item.id"
            @click="showInstanceDetail(item)"
        >
          <!-- 卡片图标：本页签统一用同一图标，底色按条目取色区分 -->
          <view class="card-icon" :style="item.iconStyle">
            <up-icon name="share-fill" size="20" color="#ffffff"></up-icon>
          </view>
          <text class="instance-name">{{ item.templateName || item.name || '未命名工作流' }}</text>
          <text class="instance-status" :class="'status-' + item.status">{{ item.statusName }}</text>
        </view>
      </template>
    </scroll-view>

    <!-- AIChat：已启用的语言模型列表，点击进入对话 -->
    <scroll-view v-else-if="activeTabKey === 'chat'" class="llm-list" scroll-y>
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
          <!-- 卡片图标：本页签统一用同一图标，底色按条目取色区分 -->
          <view class="card-icon" :style="model.iconStyle">
            <up-icon name="chat-fill" size="20" color="#ffffff"></up-icon>
          </view>
          <view class="model-info">
            <text class="model-name">{{ model.name }}</text>
            <text class="model-provider">{{ model.providerName || '未知厂商' }}</text>
          </view>
        </view>
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

    <up-tabbar :value="active" @change="handleOnTabChange" activeColor="#0052cc">
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

// 卡片图标底色池：同一页签图标一致、底色按条目分散取色，避免整屏一个颜色
const ICON_THEMES = [
  ['#0052cc', '#1890ff'],
  ['#13c2c2', '#36cfc9'],
  ['#722ed1', '#b37feb'],
  ['#52c41a', '#95de64'],
  ['#fa8c16', '#ffc069'],
  ['#eb2f96', '#ff85c0']
]

/**
 * 按条目 id 取一个分散的图标底色
 * 用 id 取模而非 Math.random：既能拉开颜色，又不会每次刷新整屏换色闪烁
 * @param id 条目 id
 */
function pickIconStyle(id) {
  const num = Number(id)
  const index = Number.isFinite(num) ? Math.abs(num) % ICON_THEMES.length : 0
  const theme = ICON_THEMES[index]
  return {backgroundImage: 'linear-gradient(135deg, ' + theme[0] + ', ' + theme[1] + ')'}
}
const tabList = computed(() => allowedTabs.map(tab => ({name: tab.name})))
const tabIndex = ref(0)
// 当前激活页签 key
const activeTabKey = computed(() => {
  const tab = allowedTabs[tabIndex.value]
  return tab ? tab.key : ''
})

// Agent 相关
const agentList = ref([])
const agentLoading = ref(false)
const agentLoaded = ref(false)

// WorkFlow 实例相关
const instanceList = ref([])
const instanceLoading = ref(false)
const instanceLoaded = ref(false)
const instanceQuery = ref({pageNo: 1, pageSize: 15})
const instanceTotal = ref(0)

// AIChat 模型列表相关
const modelList = ref([])
const modelLoading = ref(false)
const modelLoaded = ref(false)

/**
 * 按页签key懒加载对应列表（已加载过则不重复请求）
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
 * 切换页签（按需加载）
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
  agentPageListAPI({page: false}).then(res => {
    if (res.code !== 200) return
    if (res.data === undefined || res.data === null) return
    agentList.value = (res.data.rows || []).map(row => ({...row, iconStyle: pickIconStyle(row.id)}))
    agentLoaded.value = true
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
  pageInstanceListAPI(instanceQuery.value).then(res => {
    if (res.code === 200 && res.data) {
      instanceList.value = (res.data.rows || []).map(row => ({...row, iconStyle: pickIconStyle(row.id)}))
      instanceTotal.value = res.data.total || 0
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
  if (instanceList.value.length >= instanceTotal.value) return
  instanceQuery.value.pageNo++
  instanceLoading.value = true
  pageInstanceListAPI(instanceQuery.value).then(res => {
    if (res.code === 200 && res.data) {
      const rows = (res.data.rows || []).map(row => ({...row, iconStyle: pickIconStyle(row.id)}))
      instanceList.value = instanceList.value.concat(rows)
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
 * 加载模型列表（只要已启用的语言模型）
 */
function loadModels() {
  modelLoading.value = true
  const query = {
    page: false,
    type: 1,
    status: 1
  }
  pageModelListAPI(query).then(res => {
    if (res.code === 200 && res.data) {
      // 后端未按 type/status 过滤时，这里再兜一层，保证只出现可用作对话的语言模型
      modelList.value = (res.data.rows || [])
          .filter(row => row.type === 1 && row.status === 1)
          .map(row => ({...row, iconStyle: pickIconStyle(row.id)}))
      modelLoaded.value = true
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
  padding: 10px 16px;
  background-color: #0052cc;
  flex-shrink: 0;

  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #ffffff;
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
  padding-bottom: 12px;
  /* #ifdef H5 */
  height: calc(100vh - 170px);
  /* #endif */
  /* #ifdef MP-WEIXIN */
  height: calc(100vh - 250px);
  /* #endif */
  overflow-y: auto;
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

.llm-model {
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

/* 卡片图标：同一页签图标一致，底色由 pickIconStyle 按条目生成 */
.card-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.instance-name {
  flex: 1;
  min-width: 0;
  font-size: 15px;
  font-weight: 600;
  color: #333;
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

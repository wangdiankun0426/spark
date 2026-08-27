<template>
  <view class="llm-container safe-area-page">
    <!-- 顶部标题栏 -->
    <view class="llm-header">
      <text class="header-title">AI+</text>
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
    <scroll-view v-if="tabIndex === 0" class="llm-list" scroll-y>
      <!-- 加载中 -->
      <view v-if="agentLoading" class="loading-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <!-- 空状态 -->
      <view v-else-if="agentList.length === 0" class="empty-box">
        <up-empty text="暂无智能体" icon="grid-fill"/>
      </view>

      <!-- 智能体卡片 -->
      <template v-else>
        <view
            class="llm-card"
            v-for="item in agentList"
            :key="item.id"
            @click="handleAgentClick(item)"
        >
          <view class="card-header">
            <view class="card-title-row">
              <user-avatar type="agent" :name="item.name" :size="48" shape="rounded"/>
              <view class="llm-info">
                <text class="llm-name">{{ item.name }}</text>
                <view class="llm-status">
                  <view class="status-dot" :class="item.status === 1 ? 'status-on' : 'status-off'"/>
                  <text class="status-text">{{ item.statusName }}</text>
                </view>
              </view>
            </view>
            <up-icon name="arrow-right" size="16" color="#b3b3b3"></up-icon>
          </view>
          <view class="card-divider"></view>
          <view class="card-content">
            <view class="info-row">
              <text class="info-label">模型：</text>
              <text class="info-value">{{ item.chatModelName }}</text>
            </view>
            <view class="info-row" v-if="item.kbNames && item.kbNames !== '无'">
              <text class="info-label">知识库：</text>
              <text class="info-value">{{ item.kbNames }}</text>
            </view>
            <view class="info-row" v-if="item.graphNames && item.graphNames !== '无'">
              <text class="info-label">知识图谱：</text>
              <text class="info-value">{{ item.graphNames }}</text>
            </view>
            <view class="info-row" v-if="item.tools">
              <text class="info-label">工具：</text>
              <text class="info-value">{{ item.toolNames || item.tools }}</text>
            </view>
          </view>
          <view class="card-footer">
            <up-icon name="clock" size="12" color="#b3b3b3"></up-icon>
            <text class="create-time">{{ item.createdDt }}</text>
          </view>
        </view>
      </template>
    </scroll-view>

    <!-- WorkFlow 运行实例列表 -->
    <scroll-view v-else-if="tabIndex === 1" class="llm-list" scroll-y @scrolltolower="loadMoreInstances">
      <!-- 加载中 -->
      <view v-if="instanceLoading && instanceList.length === 0" class="loading-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <!-- 空状态 -->
      <view v-else-if="instanceList.length === 0" class="empty-box">
        <up-empty text="暂无运行记录" icon="list"/>
      </view>

      <!-- 实例卡片 -->
      <template v-else>
        <view
            class="instance-card"
            v-for="item in instanceList"
            :key="item.id"
            @click="showInstanceDetail(item)"
        >
          <view class="instance-header">
            <view class="instance-title-row">
              <view class="instance-icon">
                <text class="icon-text">{{ (item.templateName || 'W').charAt(0) }}</text>
              </view>
              <text class="instance-name">{{ item.templateName || item.name || '未命名工作流' }}</text>
            </view>
            <text class="instance-status" :class="'status-' + item.status">{{ item.statusName }}</text>
          </view>
          <view class="instance-info">
            <view class="info-item">
              <text class="info-label">运行ID</text>
              <text class="info-value">#{{ item.id }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">开始时间</text>
              <text class="info-value">{{ item.createdDt }}</text>
            </view>
          </view>
          <view class="instance-footer" v-if="item.status === 1">
            <up-loading-icon size="14" text="运行中..." textSize="12"/>
          </view>
        </view>
      </template>
    </scroll-view>

    <!-- 模型市场 -->
    <view v-else-if="tabIndex === 2" class="market-container">
      <!-- 厂商筛选 -->
      <scroll-view class="provider-scroll" scroll-x>
        <view class="provider-tags">
          <view
              class="provider-tag"
              :class="{ 'provider-tag-active': activeProviderId === null }"
              @click="handleSelectProvider(null)"
          >全部</view>
          <view
              v-for="provider in providerList"
              :key="provider.id"
              class="provider-tag"
              :class="{ 'provider-tag-active': activeProviderId === provider.id }"
              @click="handleSelectProvider(provider.id)"
          >{{ provider.name }}</view>
        </view>
      </scroll-view>

      <!-- 模型列表 -->
      <scroll-view class="model-list" scroll-y>
        <!-- 加载中 -->
        <view v-if="modelLoading && modelList.length === 0" class="loading-box">
          <up-loading-icon text="加载中..."/>
        </view>

        <!-- 空状态 -->
        <view v-else-if="modelList.length === 0" class="empty-box">
          <up-empty text="暂无模型" icon="list"/>
        </view>

        <!-- 模型卡片 -->
        <template v-else>
          <view
              class="model-card"
              v-for="model in modelList"
              :key="model.id"
              @click="handleSelectModel(model)"
          >
            <view class="model-header">
              <view class="model-title-row">
                <view class="model-icon-wrap" :class="getModelIconClass(model.type)">
                  <up-icon :name="getModelIcon(model.type)" size="18" color="#fff"></up-icon>
                </view>
                <view class="model-info">
                  <text class="model-name">{{ model.name }}</text>
                  <text class="model-provider">{{ model.providerName || '未知厂商' }}</text>
                </view>
              </view>
              <view class="model-badges">
                <text class="type-badge" :class="getTypeBadgeClass(model.type)">
                  {{ getTypeText(model.type) }}
                </text>
                <text class="status-badge" :class="model.status === 1 ? 'status-on' : 'status-off'">
                  {{ model.status === 1 ? '已启用' : '已停用' }}
                </text>
              </view>
            </view>
            <view class="model-footer" v-if="model.type === 1">
              <text class="model-tag" v-if="model.enableThinking === 1">思考：开启</text>
              <text class="model-tag">温度：{{ model.temperature }}</text>
            </view>
          </view>
        </template>
      </scroll-view>
    </view>

    <up-tabbar :value="active" @change="handleOnTabChange" activeColor="#0052cc">
      <up-tabbar-item name="home" icon="home-fill" text="首页"/>
      <up-tabbar-item name="flow" icon="order" text="流程"/>
      <up-tabbar-item name="llm" icon="grid-fill" text="AI+"/>
      <up-tabbar-item name="message" icon="chat-fill" text="消息"/>
      <up-tabbar-item name="my" icon="account" text="我的"/>
    </up-tabbar>
  </view>
</template>

<script setup>
import {ref} from "vue"
import {agentPageListAPI} from "@/api/llm/agent"
import {pageInstanceListAPI} from "@/api/workflow/instance"
import {pageModelListAPI} from "@/api/llm/model"
import {pageProviderListAPI} from "@/api/llm/provider"
import UserAvatar from '@/components/UserAvatar/index.vue'

const active = ref("llm")
const tabList = [
  {name: 'Agent'},
  {name: 'WorkFlow'},
  {name: '模型市场'}
]
const tabIndex = ref(0)

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

// 模型市场相关
const providerList = ref([])
const modelList = ref([])
const modelLoading = ref(false)
const modelLoaded = ref(false)
const activeProviderId = ref(null)

// 初始化加载第一个页签
getAgentList()

/**
 * 切换页签（按需加载）
 */
function handleTabChange(tab) {
  const index = tab.index
  tabIndex.value = index

  if (index === 0 && !agentLoaded.value) {
    getAgentList()
  } else if (index === 1 && !instanceLoaded.value) {
    loadInstances()
  } else if (index === 2 && !modelLoaded.value) {
    loadProviders()
    loadModels()
  }
}

/**
 * 获取智能体列表
 */
function getAgentList() {
  agentLoading.value = true
  agentPageListAPI({page: false}).then(res => {
    if (res.code !== 200) return
    if (res.data === undefined || res.data === null) return
    agentList.value = res.data.rows || []
    agentLoaded.value = true
  }).finally(() => {
    agentLoading.value = false
  })
}

/**
 * 点击智能体进入对话
 */
function handleAgentClick(item) {
  const index = agentList.value.findIndex(a => a.id === item.id)
  uni.navigateTo({
    url: '/views/chat/index',
    events: {
      'space-created': function(data) {
        if (data.targetId === item.id && index !== -1) {
          agentList.value[index].chatSpaceId = data.spaceId
        }
      }
    },
    success: function(res) {
      res.eventChannel.emit('setTarget', {
        target: item,
        targetType: 'agent'
      })
    }
  })
}

/**
 * 加载运行实例列表
 */
function loadInstances() {
  instanceLoading.value = true
  pageInstanceListAPI(instanceQuery.value).then(res => {
    if (res.code === 200 && res.data) {
      instanceList.value = res.data.rows || []
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
      instanceList.value = instanceList.value.concat(res.data.rows || [])
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
 * 加载厂商列表
 */
function loadProviders() {
  pageProviderListAPI({page: false}).then(res => {
    if (res.code === 200 && res.data) {
      providerList.value = res.data.rows || []
    }
  })
}

/**
 * 加载模型列表
 */
function loadModels() {
  modelLoading.value = true
  const query = {
    page: false,
    providerId: activeProviderId.value || undefined
  }
  pageModelListAPI(query).then(res => {
    if (res.code === 200 && res.data) {
      modelList.value = res.data.rows || []
      modelLoaded.value = true
    }
  }).finally(() => {
    modelLoading.value = false
  })
}

/**
 * 选择厂商
 */
function handleSelectProvider(providerId) {
  activeProviderId.value = providerId
  loadModels()
}

/**
 * 选择模型（语言模型跳转对话页面）
 */
function handleSelectModel(model) {
  if (model.type !== 1) {
    uni.showToast({title: '仅语言模型支持对话', icon: 'none'})
    return
  }
  if (model.status !== 1) {
    uni.showToast({title: '该模型已停用', icon: 'none'})
    return
  }
  uni.navigateTo({
    url: '/views/chat/index',
    success: function(res) {
      res.eventChannel.emit('setTarget', {
        target: {id: model.id, name: model.name},
        targetType: 'model'
      })
    }
  })
}

/**
 * 获取模型图标
 */
function getModelIcon(type) {
  if (type === 1) return 'cpu'
  if (type === 2) return 'grid'
  return 'box'
}

/**
 * 获取模型图标样式类
 */
function getModelIconClass(type) {
  if (type === 1) return 'icon-llm'
  if (type === 2) return 'icon-embed'
  return 'icon-rerank'
}

/**
 * 获取类型徽章样式类
 */
function getTypeBadgeClass(type) {
  if (type === 1) return 'badge-llm'
  if (type === 2) return 'badge-embed'
  return 'badge-rerank'
}

/**
 * 获取类型文本
 */
function getTypeText(type) {
  if (type === 1) return '语言模型'
  if (type === 2) return '向量模型'
  return '排序模型'
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
.llm-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
}

.card-title-row {
  display: flex;
  align-items: center;
  flex: 1;
  gap: 12px;
}

.llm-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.llm-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.llm-status {
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;

  &.status-on {
    background-color: #52c41a;
  }
  &.status-off {
    background-color: #d9d9d9;
  }
}

.status-text {
  font-size: 12px;
  color: #999;
}

.card-divider {
  height: 1px;
  background-color: #f0f2f5;
  margin-bottom: 12px;
}

.card-content {
  padding-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: center;
}

.info-label {
  font-size: 13px;
  color: #999;
  flex-shrink: 0;
}

.info-value {
  font-size: 13px;
  color: #5a5a5a;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 4px;

  .create-time {
    font-size: 12px;
    color: #b3b3b3;
  }
}

/* WorkFlow 实例卡片样式 */
.instance-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.instance-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.instance-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.instance-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: linear-gradient(135deg, #722ed1, #b37feb);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-text {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.instance-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
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

.instance-info {
  display: flex;
  gap: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .info-label {
  font-size: 12px;
  color: #999;
}

.info-item .info-value {
  font-size: 13px;
  color: #333;
}

.instance-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
}

/* 模型市场样式 */
.market-container {
  /* #ifdef H5 */
  height: calc(100vh - 150px);
  /* #endif */
  /* #ifdef MP-WEIXIN */
  height: calc(100vh - 230px);
  /* #endif */
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.provider-scroll {
  background-color: #fff;
  white-space: nowrap;
  padding: 12px 16px;
  flex-shrink: 0;
}

.provider-tags {
  display: inline-flex;
  gap: 8px;
}

.provider-tag {
  display: inline-block;
  padding: 6px 16px;
  font-size: 13px;
  color: #666;
  background-color: #f5f5f5;
  border-radius: 20px;
  white-space: nowrap;

  &.provider-tag-active {
    background-color: #e6f7ff;
    color: #0052cc;
    font-weight: 600;
  }
}

.model-list {
  flex: 1;
  height: 0;
  min-height: 0;
  padding: 12px 16px;
  box-sizing: border-box;
  overflow-y: auto;
}

.model-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.model-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.model-title-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.model-icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.icon-llm {
    background: linear-gradient(135deg, #0052cc, #1890ff);
  }
  &.icon-embed {
    background: linear-gradient(135deg, #13c2c2, #36cfc9);
  }
  &.icon-rerank {
    background: linear-gradient(135deg, #722ed1, #b37feb);
  }
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

.model-badges {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-end;
  flex-shrink: 0;
}

.type-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;

  &.badge-llm {
    background-color: #e6f7ff;
    color: #0052cc;
  }
  &.badge-embed {
    background-color: #e6fffb;
    color: #13c2c2;
  }
  &.badge-rerank {
    background-color: #f9f0ff;
    color: #722ed1;
  }
}

.status-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;

  &.status-on {
    background-color: #f6ffed;
    color: #52c41a;
  }
  &.status-off {
    background-color: #fff7e6;
    color: #faad14;
  }
}

.model-footer {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f5f5f5;
}

.model-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background-color: #f5f5f5;
  color: #666;
}
</style>

<template>
  <view class="home-container safe-area-page">
    <!-- 顶部欢迎卡片 -->
    <view class="welcome-card" :style="welcomeCardStyle">
      <view class="welcome-top">
        <user-avatar
            type="user"
            :userId="userInfo.id"
            :name="userInfo.name"
            :size="100"
        />
        <view class="welcome-info">
          <text class="welcome-greeting">Hello {{ userInfo.name }}，{{ greeting }}</text>
          <text class="welcome-slogan">欢迎使用星火云应用平台，聚微光成智，燃无限可能！</text>
        </view>
      </view>
    </view>

    <!-- 滚动内容区 -->
    <view class="home-scroll-body">
      <view
          class="section-box"
          v-for="section in sections"
          :key="section.key"
      >
        <view class="section-head" @click="handleSectionMore(section.key)">
          <view class="section-head-left">
            <text class="section-title">{{ section.title }}</text>
          </view>
          <up-icon name="arrow-right" size="18" color="#b3b3b3"></up-icon>
        </view>
        <view class="section-body">
          <view class="item-grid" v-if="section.items.length > 0">
            <view
                class="item-card"
                v-for="item in section.items"
                :key="section.key + '-' + item.id"
                @click="handleItemClick(section.key, item)"
            >
              <view class="item-icon">
                <up-icon :name="section.icon" size="34" color="#2f7bff"></up-icon>
              </view>
              <view class="item-text">
                <text class="item-name">{{ item.name }}</text>
                <text class="item-meta">{{ item.meta }}</text>
                <text class="item-meta" v-if="item.meta2">{{ item.meta2 }}</text>
              </view>
            </view>
          </view>

          <!-- 空状态 -->
          <view class="section-empty" v-else-if="!section.loading">
            <text class="empty-text">{{ section.emptyText }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部导航栏 -->
    <up-tabbar :value="active" @change="handleOnTabChange" activeColor="#004fc5">
      <up-tabbar-item
          v-for="tab in tabBarItems"
          :key="tab.name"
          :name="tab.name"
          :icon="tab.icon"
          :text="tab.text"
      />
    </up-tabbar>

    <!-- 会话记录选择 -->
    <ai-session-picker
        v-model:show="sessionPickerShow"
        :title="sessionPickerTitle"
        :target-type="sessionTargetType"
        :receiver-id="sessionTarget.id"
        @select="handleSessionSelect"
        @new="handleSessionNew"
    />
  </view>
</template>

<script setup>
import { ref, reactive, computed } from "vue"
import { onShow } from "@dcloudio/uni-app"
import { useStore } from "vuex"
import UserAvatar from "@/components/UserAvatar/index.vue"
import AiSessionPicker from "@/components/AiSessionPicker/index.vue"
import {useAiSessionPicker} from "@/components/AiSessionPicker/aiSessionUtil"
import {getSessionAPI} from "@/api/auth/login";
import {visibleTabs} from "@/utils/menuUtil";
import {navBarInfo} from "@/utils/navBarUtil";
import {pageMyTodoListAPI} from "@/api/flow/instance"
import {agentPageListAPI} from "@/api/llm/agent"
import {pageModelListAPI} from "@/api/llm/model"
import {pageInstanceListAPI} from "@/api/workflow/instance"

const store = useStore()
const active = ref("home")

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

const userInfo = computed(() => store.getters["user/getUserInfo"] || {
  id: undefined,
  name: undefined,
})

// 每个板块最多展示的卡片数
const SECTION_LIMIT = 6

const sections = reactive([
  {key: 'todo', title: '我的待办', icon: 'clock-fill', emptyText: '暂无待办', loading: true, items: []},
  {key: 'chat', title: 'AIChat', icon: 'chat-fill', emptyText: '暂无可用模型', loading: true, items: []},
  {key: 'agent', title: 'Agent', icon: 'grid-fill', emptyText: '暂无智能体', loading: true, items: []},
  {key: 'workflow', title: 'Workflow', icon: 'share-fill', emptyText: '暂无运行记录', loading: true, items: []}
])

// 小程序端欢迎卡片延伸到状态栏底部，H5 走样式默认内边距
const welcomeCardStyle = computed(() => {
  // #ifdef MP-WEIXIN
  return {paddingTop: navBarInfo.statusBarHeight + 22 + 'px'}
  // #endif
  // #ifdef H5
  return {}
  // #endif
})

// 根据时间段显示问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

onShow(() => {
  loadUserInfo()
  loadTodoList()
  loadChatModelList()
  loadAgentList()
  loadWorkflowList()
})

function loadUserInfo() {
  getSessionAPI().then(res => {
    if (res.code === 200) {
      store.dispatch("user/setUserInfo", { userInfo: res.data })
    }
  }).catch(err => {
    console.error("获取用户信息失败", err)
  })
}

/**
 * 写入板块卡片数据
 */
function setSectionItems(key, items) {
  const section = sections.find(s => s.key === key)
  if (!section) {
    return
  }
  section.items = items.slice(0, SECTION_LIMIT)
  section.loading = false
}

/**
 * 加载我的待办
 */
function loadTodoList() {
  pageMyTodoListAPI({pageSize: SECTION_LIMIT}).then(res => {
    if (res.code !== 200 || !res.data) {
      setSectionItems('todo', [])
      return
    }
    const items = (res.data.rows || []).map(row => ({
      id: row.instanceId || row.id,
      name: row.instanceName || row.name || '未命名流程',
      // 第一行副信息： 流程类型 · 申请人
      meta: [row.typeName, row.createdByName].filter(Boolean).join(' · '),
      // 第二行副信息： 申请时间
      meta2: [row.createdDt].filter(Boolean).join(' · '),
      raw: row
    }))
    setSectionItems('todo', items)
  }).catch(() => {
    setSectionItems('todo', [])
  })
}

/**
 * 加载 AIChat 卡片
 */
function loadChatModelList() {
  pageModelListAPI({pageSize: SECTION_LIMIT, type: 1, status : 1}).then(res => {
    if (res.code !== 200 || !res.data) {
      setSectionItems('chat', [])
      return
    }
    const items = (res.data.rows || [])
        .filter(row => row.type === 1 && row.status === 1)
        .map(row => ({
          id: row.id,
          name: row.name,
          meta: row.providerName ? '厂商：' + row.providerName : '未知厂商',
          raw: row
        }))
    setSectionItems('chat', items)
  }).catch(() => {
    setSectionItems('chat', [])
  })
}

/**
 * 加载智能体列表
 */
function loadAgentList() {
  agentPageListAPI({pageSize: SECTION_LIMIT, status : 1}).then(res => {
    if (res.code !== 200 || !res.data) {
      setSectionItems('agent', [])
      return
    }
    const items = (res.data.rows || []).map(row => ({
      id: row.id,
      name: row.name,
      meta: row.description || '暂无备注',
      raw: row
    }))
    setSectionItems('agent', items)
  }).catch(() => {
    setSectionItems('agent', [])
  })
}

/**
 * 加载工作流运行记录
 */
function loadWorkflowList() {
  pageInstanceListAPI({pageSize: SECTION_LIMIT}).then(res => {
    if (res.code !== 200 || !res.data) {
      setSectionItems('workflow', [])
      return
    }
    const items = (res.data.rows || []).map(row => ({
      id: row.id,
      name: row.templateName || row.name || '未命名工作流',
      meta: row.statusName || '',
      meta2: row.createdDt || '',
      raw: row
    }))
    setSectionItems('workflow', items)
  }).catch(() => {
    setSectionItems('workflow', [])
  })
}

/**
 * 查看更多
 * @param key
 */
function handleSectionMore(key) {
  if (key === 'todo') {
    // 流程页「我的待办」页签
    uni.reLaunch({url: '/pages/flow/index?tab=1'})
  } else if (key === 'chat') {
    // AI应用页「AIChat」页签
    uni.reLaunch({url: '/pages/llm/index?tab=0'})
  } else if (key === 'agent') {
    // AI应用页「Agent」页签
    uni.reLaunch({url: '/pages/llm/index?tab=1'})
  } else {
    // AI应用页「WorkFlow」页签
    uni.reLaunch({url: '/pages/llm/index?tab=2'})
  }
}

/**
 * 点击卡片：按板块进入对应详情或对话
 * @param key 板块标识
 * @param item 卡片数据
 */
function handleItemClick(key, item) {
  const row = item.raw || {}
  if (key === 'todo') {
    // 流程详情，type=3 表示「我的待办」
    uni.navigateTo({url: '/pages/flow/instanceDetail?id=' + item.id + '&type=3'})
  } else if (key === 'chat') {
    // 与模型对话：先选会话记录
    openSessionPicker(row, 'model')
  } else if (key === 'agent') {
    // 与智能体对话：先选会话记录
    openSessionPicker(row, 'agent')
  } else {
    // 工作流运行详情
    uni.navigateTo({url: '/pages/llm/wfInstanceDetail?id=' + item.id})
  }
}

function handleOnTabChange(index) {
  uni.reLaunch({
    url: '/pages/' + index + '/index'
  })
}
</script>

<style scoped lang="scss">
.home-container {
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background-color: $uni-bg-color-dashboard;
}

.home-container :deep(.u-tabbar) {
  flex: none;
}

.welcome-card {
  flex-shrink: 0;
  box-sizing: border-box;
  padding: 52px 16px 32px;
  color: $spark-color-text;
  background-image: linear-gradient(180deg,
      #5b94f5 0%,
      #7babff 44px,
      #b6d2ff 92px,
      #eaf2ff 140px);
  background-color: #eaf2ff;
}
.welcome-top {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}
.welcome-info {
  flex: 1;
  min-width: 0;
  margin-left: 10px;
  line-height: 1.5;
  letter-spacing: 1.0px;
}
.welcome-greeting {
  font-size: 16px;
  font-weight: 800;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
  margin-bottom: 4px;
}
.welcome-slogan {
  font-size: 13px;
  font-weight: 500;
  color: rgba(23, 43, 77, 0.7);
}

/* 滚动内容区 */
.home-scroll-body {
  flex: 1;
  min-height: 0;
  box-sizing: border-box;
  padding: 20px 12px;
  overflow-y: auto;
}

.section-box {
  height: 260px;
  box-sizing: border-box;
  margin-bottom: 24px;
}

.section-box:last-child {
  margin-bottom: 12px;
}

/* 板块标题行 */
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 24px;
}

.section-head-left {
  display: flex;
  align-items: center;
}

.section-title {
  font-size: 17px;
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

.section-body {
  height: 156px;
  box-sizing: border-box;
  padding-top: 12px;
}

.item-grid {
  display: flex;
  flex-wrap: wrap;

  .item-card:nth-child(odd) {
    margin-right: 8px;
  }

  .item-card:nth-child(n+3) {
    margin-top: 8px;
  }
}

.item-card {
  width: calc(50% - 4px);
  height: 72px;
  box-sizing: border-box;
  padding: 8px 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  background-image: linear-gradient(135deg, #ffffff 0%, #f7faff 100%);
  border: 1px solid rgba(0, 79, 197, 0.06);
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(23, 43, 77, 0.08);
}

.item-icon {
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

.item-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.item-name {
  font-size: 13px;
  font-weight: 600;
  color: #172b4d;
  line-height: 17px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  font-size: 11px;
  line-height: 15px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 空状态 */
.section-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-text {
  font-size: 12px;
  color: #b3b3b3;
}
</style>

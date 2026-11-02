<template>
  <view class="home-container safe-area-page" :class="themeClass">
    <!-- 顶部欢迎卡片 -->
    <view class="welcome-card" :style="welcomeCardStyle">
      <view class="welcome-top">
        <user-avatar
            type="user"
            :userId="userInfo.id"
            :name="userInfo.name"
            :size="64"
        />
        <view class="welcome-info">
          <text class="welcome-greeting">{{ greeting }}，{{ userInfo.name }}</text>
          <text class="welcome-slogan">欢迎使用星火云应用平台，聚微光成智，燃无限可能！</text>
          <view class="welcome-date-row">
            <text class="welcome-date">{{ todayText }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 滚动内容区 -->
    <view class="home-scroll-body">
      <!-- 快捷入口 -->
      <view class="quick-bar">
        <view
            class="quick-cell"
            v-for="entry in quickEntries"
            :key="entry.key"
            @click="handleQuickClick(entry.key)"
        >
          <view class="quick-icon">
            <up-icon :name="entry.icon" size="22" :color="quickIconColor"/>
            <view v-if="entry.key === 'message' && unreadCount > 0" class="quick-badge">
              {{ unreadCount > 99 ? '99+' : unreadCount }}
            </view>
          </view>
          <text class="quick-text">{{ entry.text }}</text>
        </view>
      </view>

      <!-- 星火助手卡：一键打开 AI 应用 -->
      <view v-if="aiEnabled" class="assistant-card" @click="handleAssistant">
        <view class="assistant-ring assistant-ring--lg"></view>
        <view class="assistant-ring assistant-ring--md"></view>
        <view class="assistant-ring assistant-ring--sm"></view>
        <text class="assistant-title">星火助手</text>
        <text class="assistant-desc">问答、总结、写文档，一键打开 AI 应用</text>
        <view class="assistant-btn">
          <text class="assistant-btn__text">开始使用</text>
          <up-icon name="arrow-right" size="10" color="#ffffff"/>
        </view>
      </view>

      <!-- 智能推荐 -->
      <view v-if="aiEnabled" class="section-box">
        <view class="section-head" @click="handleRecommendMore">
          <view class="section-head-left">
            <text class="section-title">智能推荐</text>
          </view>
          <view class="section-more" v-if="recommendList.length > 0">
            <text class="section-more__text">更多</text>
            <up-icon name="arrow-right" size="12" :color="moreIconColor"></up-icon>
          </view>
        </view>
        <view class="card-grid" v-if="recommendList.length > 0">
          <view
              class="qcard"
              v-for="item in recommendTop"
              :key="item.hotType + '-' + item.id"
              @click="handleRecommendClick(item)"
          >
            <view class="qcard-top">
              <view class="qcard-icon">
                <ModelIcon v-if="item.hotType === 'model'" :size="18"/>
                <AgentIcon v-else-if="item.hotType === 'agent'" :size="18"/>
                <WorkflowIcon v-else :size="18"/>
              </view>
              <text class="qcard-count">{{ item.count }}次</text>
            </view>
            <text class="qcard-name">{{ item.name }}</text>
            <view class="qcard-meta">
              <i class="qcard-dot"></i>
              <text>{{ typeText(item.hotType) }}</text>
            </view>
          </view>
        </view>
        <view class="big-empty" v-else>
          <view class="big-empty__glow"></view>
          <view class="big-empty__ring"></view>
          <view class="big-empty__icon">
            <up-icon name="star-fill" size="22" :color="quickIconColor"/>
          </view>
          <text class="big-empty__title">还没有使用记录</text>
          <text class="big-empty__sub">你用过的智能体、工作流会按使用频次排在这里</text>
          <view class="big-empty__btn" @click.stop="handleRecommendMore">
            <text>去 AI 应用看看</text>
            <up-icon name="arrow-right" size="10" :color="quickIconColor"/>
          </view>
        </view>
      </view>

      <!-- 我的待办 -->
      <view class="section-box">
        <view class="section-head" @click="handleTodoMore">
          <view class="section-head-left">
            <text class="section-title">我的待办</text>
          </view>
          <view class="section-more" v-if="todoList.length > 0">
            <text class="section-more__text">更多</text>
            <up-icon name="arrow-right" size="12" :color="moreIconColor"></up-icon>
          </view>
        </view>
        <view class="card-grid" v-if="todoList.length > 0">
          <view
              class="qcard"
              v-for="item in todoList"
              :key="'todo-' + item.id"
              @click="handleTodoClick(item)"
          >
            <view class="qcard-top">
              <text class="todo-tag" :class="todoTagCls(item.raw)">{{ todoTagText(item.raw) }}</text>
              <text class="todo-time" :class="{'todo-time--warn': isUrgent(item.raw)}">{{ item.timeText }}</text>
            </view>
            <text class="qcard-name">{{ item.name }}</text>
            <view class="qcard-meta">
              <text>{{ item.meta }}</text>
            </view>
          </view>
        </view>
        <view class="big-empty" v-else>
          <view class="big-empty__glow"></view>
          <view class="big-empty__ring"></view>
          <view class="big-empty__icon">
            <up-icon name="calendar-fill" size="22" :color="quickIconColor"/>
          </view>
          <text class="big-empty__title">今天没有待处理</text>
          <text class="big-empty__sub">分配给你的审批任务会出现在这里</text>
          <view class="big-empty__btn" @click.stop="handleTodoMore">
            <text>查看全部待办</text>
            <up-icon name="arrow-right" size="10" :color="quickIconColor"/>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部导航栏 -->
    <AppTabbar :value="active" @change="handleOnTabChange"/>
  </view>
</template>

<script setup>
import { ref, computed } from "vue"
import { onShow } from "@dcloudio/uni-app"
import { useStore } from "vuex"
import UserAvatar from "@/components/UserAvatar/index.vue"
import {useTheme} from "@/utils/useTheme"
import {getHeaderIconColor, getTertiaryColor} from "@/utils/themeUtil"
import AppTabbar from "@/components/AppTabbar/index.vue";
import {getSessionAPI} from "@/api/auth/login";
import {navBarInfo} from "@/utils/navBarUtil";
import {pageMyTodoListAPI} from "@/api/flow/instance"
import {queryRecommendAPI} from "@/api/home/home"
import {queryMyMessageCountAPI} from "@/api/sys/message"
import ModelIcon from "@/components/icons/ModelIcon.vue"
import AgentIcon from "@/components/icons/AgentIcon.vue"
import WorkflowIcon from "@/components/icons/WorkflowIcon.vue"

const store = useStore()
const active = ref("home")
const recommendList = ref([])
const todoList = ref([])
const unreadCount = ref(0)
const {isDark, themeClass} = useTheme()

const quickIconColor = computed(() => getHeaderIconColor(isDark.value))
const moreIconColor = computed(() => getTertiaryColor(isDark.value))

const userInfo = computed(() => store.getters["user/getUserInfo"] || {
  id: undefined,
  name: undefined,
})

// 移动端AI应用开关
const aiEnabled = computed(() => userInfo.value && userInfo.value.appAiEnabled === true)

const allQuickEntries = [
  {key: 'todo', icon: 'file-text-fill', text: '待办'},
  {key: 'chat', icon: 'chat-fill', text: 'AI 对话'},
  {key: 'agent', icon: 'star-fill', text: '智能体'},
  {key: 'workflow', icon: 'share-fill', text: '工作流'},
  {key: 'message', icon: 'bell-fill', text: '消息'}
]
const quickEntries = computed(() => {
  if (aiEnabled.value) {
    return allQuickEntries
  }
  return allQuickEntries.filter(entry => ['todo', 'message'].includes(entry.key))
})

const recommendTop = computed(() => recommendList.value.slice(0, 6))

// 小程序端欢迎卡片延伸到状态栏底部，H5 走样式默认内边距
const welcomeCardStyle = computed(() => {
  // #ifdef MP-WEIXIN
  return {paddingTop: navBarInfo.statusBarHeight + 14 + 'px'}
  // #endif
  // #ifdef H5
  return {}
  // #endif
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
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

onShow(() => {
  loadUserInfo()
  if (aiEnabled.value) {
    loadRecommend()
  }
  loadTodoList()
  loadUnread()
})

function loadUserInfo() {
  getSessionAPI().then(res => {
    if (res.code === 200) {
      const wasEnabled = aiEnabled.value
      store.dispatch("user/setUserInfo", { userInfo: res.data })
      // 开关由关变开时补加载推荐数据
      if (!wasEnabled && res.data.appAiEnabled === true) {
        loadRecommend()
      }
    }
  }).catch(err => {
    console.error("loadUserInfo error", err)
  })
}

function loadRecommend() {
  queryRecommendAPI({rangeDays: 30}).then(res => {
    if (res.code === 200 && res.data) {
      recommendList.value = res.data
    }
  }).catch(() => {})
}

function loadTodoList() {
  pageMyTodoListAPI({pageSize: 6}).then(res => {
    if (res.code !== 200 || !res.data) {
      todoList.value = []
      return
    }
    todoList.value = (res.data.rows || []).map(row => ({
      id: row.instanceId || row.id,
      name: row.instanceName || row.name || '未命名流程',
      meta: [row.typeName, row.createdByName].filter(Boolean).join(' · '),
      timeText: formatTodoTime(row.createdDt),
      raw: row
    }))
  }).catch(() => {
    todoList.value = []
  })
}

function loadUnread() {
  queryMyMessageCountAPI().then(res => {
    if (res.code === 200 && res.data) {
      unreadCount.value = res.data.unread || 0
    }
  }).catch(() => {
    unreadCount.value = 0
  })
}

/**
 * 推荐类型文案
 * @param type 热度类型
 */
function typeText(type) {
  const map = {model: '模型', agent: '智能体', workflow: '工作流'}
  return map[type] || ''
}

/**
 * 点击快捷入口
 * @param key 入口标识
 */
function handleQuickClick(key) {
  if (key === 'todo') {
    uni.reLaunch({url: '/pages/flow/index?tab=1'})
  } else if (key === 'chat') {
    uni.reLaunch({url: '/pages/llm/index?tab=2'})
  } else if (key === 'agent') {
    uni.reLaunch({url: '/pages/llm/index?tab=0'})
  } else if (key === 'workflow') {
    uni.reLaunch({url: '/pages/llm/index?tab=1'})
  } else if (key === 'message') {
    uni.navigateTo({url: '/views/message/index'})
  }
}

/**
 * 星火助手卡：进入 AI 应用「AI 对话」页签
 */
function handleAssistant() {
  uni.reLaunch({url: '/pages/llm/index?tab=2'})
}

/**
 * 点击推荐项
 * @param item 推荐数据
 */
function handleRecommendClick(item) {
  const tabMap = {model: 2, agent: 0, workflow: 1}
  const tab = tabMap[item.hotType]
  if (tab === undefined) {
    return
  }
  uni.reLaunch({url: '/pages/llm/index?tab=' + tab})
}

function handleRecommendMore() {
  uni.reLaunch({url: '/pages/llm/index?tab=0'})
}

function handleTodoClick(item) {
  // 流程详情，type=3 表示「我的待办」
  uni.navigateTo({url: '/pages/flow/instanceDetail?id=' + item.id + '&type=3'})
}

function handleTodoMore() {
  uni.reLaunch({url: '/pages/flow/index?tab=1'})
}

/**
 * 紧急程度判断
 * @param row 待办原始数据
 */
function isUrgent(row) {
  return !!(row && row.levelName && row.levelName.indexOf('急') !== -1)
}

/**
 * 待办状态胶囊文案
 * @param row 待办原始数据
 */
function todoTagText(row) {
  if (!row) return '审批中'
  return row.levelName || '审批中'
}

/**
 * 待办状态胶囊样式类
 * @param row 待办原始数据
 */
function todoTagCls(row) {
  if (!row || !row.levelName) return 'todo-tag--primary'
  const lv = row.levelName
  if (lv.indexOf('急') !== -1) return 'todo-tag--warn'
  if (lv.indexOf('待') !== -1) return 'todo-tag--gray'
  return 'todo-tag--primary'
}

/**
 * 待办时间文案：今天 / 明天 / 昨天 / MM-DD
 * @param dt 后端时间字符串
 */
function formatTodoTime(dt) {
  if (!dt) return ''
  const d = new Date(String(dt).replace(/-/g, '/'))
  if (isNaN(d.getTime())) return ''
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const target = new Date(d.getFullYear(), d.getMonth(), d.getDate())
  const diffDays = Math.round((target - today) / 86400000)
  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '明天'
  if (diffDays === -1) return '昨天'
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return mm + '-' + dd
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
  background-color: var(--app-bg-page);
}

.welcome-card {
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
  box-sizing: border-box;
  padding: 36px 16px 26px;
  color: var(--app-fill-tx);
  border-radius: 0 0 20px 20px;
  box-shadow: var(--app-shadow-hero);
  background-image: var(--app-gradient-welcome);
  background-color: var(--app-bg-mid);
}

.welcome-card::after {
  content: '';
  position: absolute;
  right: -96px;
  top: -70px;
  width: 260px;
  height: 260px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.28);
  box-shadow: 0 0 0 26px rgba(255, 255, 255, 0.07) inset;
  pointer-events: none;
  z-index: 0;
}
.welcome-top {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}
.welcome-info {
  flex: 1;
  min-width: 0;
  margin-left: 10px;
  line-height: 1.5;
}
.welcome-greeting {
  font-size: 21px;
  font-weight: 700;
  line-height: 1.35;
  color: var(--app-fill-tx);
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
  margin-bottom: 4px;
}
.welcome-slogan {
  display: block;
  font-size: 12.5px;
  font-weight: 400;
  line-height: 1.5;
  color: var(--app-fill-tx);
  opacity: 0.82;
}

.welcome-date-row {
  margin-top: 7px;
}
.welcome-date {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 11.5px;
  line-height: 18px;
  color: var(--app-fill-tx);
  opacity: 0.92;
  background-color: rgba(255, 255, 255, 0.18);
}

.home-scroll-body {
  flex: 1;
  min-height: 0;
  box-sizing: border-box;
  padding: 14px 14px 18px;
  overflow-y: auto;
}

.quick-bar {
  display: flex;
  align-items: stretch;
  box-sizing: border-box;
  padding: 13px 4px 12px;
  margin-bottom: 12px;
  background-image: var(--app-gradient-card);
  border: 1px solid var(--app-border-light);
  border-radius: 16px;
  box-shadow: var(--app-shadow-card);
}

.quick-cell {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
  padding: 2px 0;

  &:active {
    opacity: 0.7;
  }
}

.quick-icon {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: 11px;
  background-color: var(--app-primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-badge {
  position: absolute;
  top: -7px;
  right: -9px;
  min-width: 15px;
  height: 15px;
  padding: 0 4px;
  border-radius: 999px;
  background-color: var(--app-error-solid);
  color: var(--app-fill-tx);
  font-size: 9.5px;
  line-height: 15px;
  text-align: center;
  box-shadow: 0 0 0 1.5px var(--app-bg-card);
}

.quick-text {
  font-size: 12px;
  color: var(--app-text-secondary);
  white-space: nowrap;
}

.assistant-card {
  position: relative;
  overflow: hidden;
  box-sizing: border-box;
  padding: 14px 15px;
  margin-bottom: 14px;
  border-radius: 18px;
  background-image: var(--app-gradient-assistant);
  box-shadow: var(--app-shadow-brand);

  &:active {
    opacity: 0.92;
  }
}

.assistant-ring {
  position: absolute;
  right: -46px;
  top: -58px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.16);
  pointer-events: none;

  &.assistant-ring--lg {
    width: 168px;
    height: 168px;
  }
  &.assistant-ring--md {
    width: 118px;
    height: 118px;
    border-color: rgba(255, 255, 255, 0.13);
  }
  &.assistant-ring--sm {
    width: 68px;
    height: 68px;
    border-color: rgba(255, 255, 255, 0.1);
  }
}

.assistant-title {
  position: relative;
  display: block;
  font-size: 17px;
  font-weight: 700;
  color: var(--app-fill-tx);
  letter-spacing: 0.3px;
}

.assistant-desc {
  position: relative;
  display: block;
  margin-top: 5px;
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.84);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.assistant-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  margin-top: 9px;
  height: 29px;
  padding: 0 12px;
  border-radius: 9px;
  background-color: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);

  .assistant-btn__text {
    font-size: 12px;
    font-weight: 600;
    color: var(--app-fill-tx);
  }
}

.section-box {
  box-sizing: border-box;
  margin-bottom: 16px;
}

.section-box:last-child {
  margin-bottom: 4px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 22px;
  margin-bottom: 9px;
}

.section-head-left {
  display: flex;
  align-items: center;
  gap: 7px;
}

.section-title {
  position: relative;
  padding-left: 11px;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.2px;
  color: var(--app-text-primary);
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 13px;
  border-radius: 2px;
  background-image: var(--app-gradient-brand);
}

.section-more {
  display: flex;
  align-items: center;
  gap: 1px;

  .section-more__text {
    font-size: 11px;
    color: var(--app-text-tertiary);
  }
}

.card-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  grid-auto-rows: 1fr;
}

.qcard {
  box-sizing: border-box;
  padding: 10px 13px 11px;
  display: flex;
  flex-direction: column;
  min-height: 90px;
  background-image: var(--app-gradient-card);
  border: 1px solid var(--app-border-light);
  border-radius: 16px;
  box-shadow: var(--app-shadow-card);

  &:active {
    background-color: var(--app-bg-hover);
  }
}

.qcard-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
}

.qcard-icon {
  width: 28px;
  height: 28px;
  border-radius: 9px;
  background-color: var(--app-primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
}

.qcard-count {
  font-size: 11px;
  color: var(--app-text-placeholder);
}

.qcard-name {
  margin-top: 8px;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.32;
  color: var(--app-text-primary);
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  word-break: break-all;
}

.qcard-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: auto;
  padding-top: 5px;
  font-size: 11px;
  color: var(--app-text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.qcard-dot {
  flex: none;
  width: 3.5px;
  height: 3.5px;
  border-radius: 50%;
  background-color: var(--app-text-placeholder);
}

.todo-tag {
  flex: none;
  padding: 2.5px 7px;
  border-radius: 6px;
  font-size: 11px;
  line-height: 16px;

  &.todo-tag--primary {
    color: var(--app-brand);
    background-color: var(--app-primary-soft);
  }
  &.todo-tag--warn {
    color: var(--app-text-warning);
    background-color: var(--app-bg-warning);
  }
  &.todo-tag--gray {
    color: var(--app-text-tertiary);
    background-color: var(--app-bg-sub);
  }
}

.todo-time {
  font-size: 11px;
  color: var(--app-text-placeholder);

  &.todo-time--warn {
    color: var(--app-text-warning);
  }
}

.big-empty {
  position: relative;
  overflow: hidden;
  box-sizing: border-box;
  padding: 22px 16px 20px;
  border-radius: 18px;
  background-image: var(--app-gradient-hero);
  border: 1px solid var(--app-hero-border);
  box-shadow: var(--app-shadow-card);
}

.big-empty__glow {
  position: absolute;
  right: -46px;
  top: -52px;
  width: 168px;
  height: 168px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(47, 123, 255, 0.14) 0%, rgba(47, 123, 255, 0) 66%);
  pointer-events: none;
}

.big-empty__ring {
  position: absolute;
  right: -16px;
  top: -22px;
  width: 92px;
  height: 92px;
  border-radius: 50%;
  border: 1px solid var(--app-hero-border);
  pointer-events: none;
}

.big-empty__icon {
  position: relative;
  width: 44px;
  height: 44px;
  border-radius: 15px;
  background-color: var(--app-primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
}

.big-empty__title {
  position: relative;
  display: block;
  margin-top: 12px;
  font-size: 13.5px;
  font-weight: 600;
  color: var(--app-text-primary);
}

.big-empty__sub {
  position: relative;
  display: block;
  margin-top: 6px;
  font-size: 11.5px;
  line-height: 1.5;
  color: var(--app-text-tertiary);
}

.big-empty__btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  margin-top: 14px;
  height: 29px;
  padding: 0 13px;
  border-radius: 9px;
  background-color: var(--app-primary-soft);
  border: 1px solid var(--app-brand-border);
  font-size: 11.5px;
  font-weight: 600;
  color: var(--app-brand);

  &:active {
    opacity: 0.85;
  }
}
</style>

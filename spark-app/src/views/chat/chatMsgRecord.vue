<template>
  <view class="record-container safe-area-page">
    <!-- 标题栏 -->
    <view class="record-header">
      <view class="header-back" @click="goBack">
        <up-icon name="arrow-left" size="20" color="#fff"></up-icon>
      </view>
      <view class="header-title">
        <text>{{ title }}</text>
      </view>
    </view>

    <!-- 检索区 -->
    <view class="record-search">
      <view class="record-search-field" @click="calendarShow = true">
        <up-icon name="calendar" size="16" color="#999"></up-icon>
        <text :class="dateRangeText ? 'record-search-value' : 'record-search-placeholder'">
          {{ dateRangeText || '选择时间范围' }}
        </text>
      </view>
      <view v-if="dateRangeText" class="record-search-clear" @click="clearDateRange">
        <up-icon name="close" size="14" color="#999"></up-icon>
      </view>
    </view>

    <!-- 记录列表 -->
    <scroll-view
        scroll-y="true"
        class="record-body"
        :scroll-top="scrollTop"
        :scroll-into-view="scrollIntoView"
        @scroll="onScroll"
    >
      <view v-if="historyLoading" class="record-tip">正在加载历史消息...</view>
      <view v-if="!chatHisMsgs.length && !historyLoading" class="record-tip">暂无聊天记录</view>

      <view
          v-for="(chatMsg, idx) in chatHisMsgs"
          :key="idx"
          :id="chatMsg.id === undefined ? '' : 'msg-' + chatMsg.id"
          class="record-item"
          :class="{ 'record-item-self': chatMsg.senderId === userInfo.id }"
      >
        <!-- 消息头部 -->
        <view class="record-item-header">
          <template v-if="chatMsg.senderId === userInfo.id">
            <view class="record-time">{{ chatMsg.createdDt }}</view>
            <user-avatar
                type="user"
                :userId="chatMsg.senderId"
                :size="32"
            />
          </template>
          <template v-else>
            <user-avatar
                v-if="targetType === 'user'"
                type="user"
                :userId="chatMsg.senderId"
                :name="targetName"
                :size="32"
            />
            <user-avatar
                v-else
                :type="targetType"
                :size="32"
            />
            <view class="record-time">{{ chatMsg.createdDt }}</view>
          </template>
        </view>
        <!-- 消息内容 -->
        <view class="record-bubble markdown-body">
          <up-parse
              :content="renderMarkdown(chatMsg.message)"
              :tag-style="MD_TAG_STYLE"
          ></up-parse>
        </view>
      </view>
    </scroll-view>

    <!-- 时间范围选择 -->
    <up-calendar
        mode="range"
        :show="calendarShow"
        :default-date="dateRange || []"
        :min-date="calendarMinDate"
        :max-date="calendarMaxDate"
        :month-num="calendarMonthNum"
        :allow-same-day="true"
        @confirm="handleDateConfirm"
        @close="calendarShow = false"
    ></up-calendar>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from "vue"
import { onLoad } from "@dcloudio/uni-app"
import { useStore } from "vuex"
import { renderMarkdown, MD_TAG_STYLE } from "@/utils/markdownUtil.js"
import { useChatHistory } from "@/views/chat/chatMessage.js"
import UserAvatar from "@/components/UserAvatar/index.vue"

const store = useStore()
const userInfo = computed(() => store.getters["user/getUserInfo"] || {})

const title = ref('聊天记录')
const targetName = ref('')
const targetType = ref('agent')
// 会话空间id由聊天页通过路由参数带入
const spaceId = ref(null)
// 检索时间范围 [开始日期, 结束日期]
const dateRange = ref(null)
const calendarShow = ref(false)

// 日历可选范围
const calendarMinDate = formatDay(new Date(new Date().setFullYear(new Date().getFullYear() - 2)))
const calendarMaxDate = formatDay(new Date())

// 每多一个月会多渲染一套日历格子，按需调整
const calendarMonthNum = 25

// 检索条件，传给分页接口
const historyFilter = computed(() => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    return {}
  }
  return {
    createdStartTime: dateRange.value[0] + ' 00:00:00',
    // 结束日期取当天最后一秒，保证结束当天的消息被包含
    createdEndTime: dateRange.value[1] + ' 23:59:59'
  }
})

// 检索范围文案
const dateRangeText = computed(() => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    return ''
  }
  return dateRange.value[0] + ' 至 ' + dateRange.value[1]
})

const {
  list: chatHisMsgs,
  loading: historyLoading,
  scrollTop,
  scrollIntoView,
  onScroll,
  scrollToBottom,
  reset: resetHistory,
  loadMore
} = useChatHistory(spaceId, historyFilter)

onLoad((options) => {
  spaceId.value = options && options.spaceId ? Number(options.spaceId) : null
  targetType.value = (options && options.targetType) || 'agent'
  targetName.value = options && options.targetName ? decodeURIComponent(options.targetName) : ''
  title.value = options && options.title ? decodeURIComponent(options.title) : '聊天记录'
})

onMounted(() => {
  loadChatRecords()
})

function goBack() {
  uni.navigateBack()
}

/**
 * 格式化为 YYYY-MM-DD
 * @param date 日期对象
 * @returns 日期字符串
 */
function formatDay(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 确认时间范围后重新加载
 */
function handleDateConfirm(dates) {
  calendarShow.value = false
  const list = (dates || []).filter(Boolean)
  if (list.length < 2) {
    return
  }
  dateRange.value = [list[0], list[list.length - 1]]
  loadChatRecords()
}

/**
 * 清空时间范围后重新加载
 */
function clearDateRange() {
  dateRange.value = null
  loadChatRecords()
}

/**
 * 加载聊天记录：取最新一页，更早的历史由滚动到顶部时按页加载
 */
function loadChatRecords() {
  if (!spaceId.value) {
    return
  }
  resetHistory()
  loadMore().then(scrollToBottom)
}
</script>

<style scoped lang="scss">
.record-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f7fa;
}

.record-header {
  display: flex;
  align-items: center;
  height: 48px;
  background-color: #0052cc;
  padding: 0 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.header-back {
  display: flex;
  align-items: center;
}

.header-title {
  flex: 1;
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 0 8px;
}

/* 检索区 */
.record-search {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  padding: 10px 12px 0;
}

.record-search-field {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 12px;
  background-color: #fff;
  border-radius: 8px;
  font-size: 14px;
}

.record-search-value {
  color: #333;
}

.record-search-placeholder {
  color: #999;
}

.record-search-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
}

.record-body {
  flex: 1;
  min-height: 0;
  padding: 8px 12px;
  box-sizing: border-box;
}

/* u-popup 根节点常驻，去掉 flex 占位，避免把记录列表挤成半屏、下方留白 */
.record-container :deep(.u-popup) {
  flex: none;
}

.record-tip {
  padding: 12px 0;
  text-align: center;
  font-size: 12px;
  color: #999;
}

/* 消息项：对方靠左、自己靠右，与聊天页保持一致 */
.record-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 16px;
}

.record-item-self {
  align-items: flex-end;
}

.record-item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.record-item-self .record-item-header {
  flex-direction: row-reverse;
}

.record-time {
  font-size: 12px;
  color: #999;
}

.record-bubble {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 8px;
  background-color: #fff;
  color: #333;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
}

.record-item-self .record-bubble {
  background-color: #0052cc;
  color: #fff;
}
</style>

<template>
  <view class="record-container safe-area-page" :class="themeClass">
    <PageHeader :title="title" @back="goBack"/>

    <!-- 时间范围：快捷胶囊 + 自定义日历入口 -->
    <view class="record-search">
      <view
          v-for="range in quickRanges"
          :key="range.key"
          class="range-chip"
          :class="{ 'is-on': activeRange === range.key }"
          @click="selectQuickRange(range)"
      >
        <text class="range-chip__text">{{ range.label }}</text>
      </view>
      <view class="range-chip range-chip--custom" :class="{ 'is-on': activeRange === 'custom' }" @click="calendarShow = true">
        <up-icon name="calendar" size="13" :color="activeRange === 'custom' ? customIconOn : customIconOff"/>
        <text class="range-chip__text">{{ activeRange === 'custom' ? dateRangeText : '自定义' }}</text>
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
          v-for="(item, idx) in displayItems"
          :key="item.key"
          class="record-item"
          :class="{ 'record-item-self': item.self }"
      >
        <!-- 日期分隔：同一天只出现一次，消息上只留 HH:mm -->
        <view v-if="item.divider" class="record-day">
          <text class="record-day__text">{{ item.label }}</text>
        </view>

        <!-- 消息行：头像在外侧，时间与气泡同一列 -->
        <view v-else class="record-row" :id="item.msg && item.msg.id !== undefined ? 'msg-' + item.msg.id : ''">
          <view class="record-avatar">
            <user-avatar
                v-if="item.msg.senderId === userInfo.id"
                type="user"
                shape="rounded"
                :userId="item.msg.senderId"
                :name="userInfo.name"
                :size="32"
            />
            <template v-else>
              <user-avatar
                  v-if="targetType === 'user'"
                  type="user"
                  shape="rounded"
                  :userId="item.msg.senderId"
                  :name="targetName"
                  :size="32"
              />
              <user-avatar v-else :type="targetType" shape="rounded" show-icon :size="32"/>
            </template>
          </view>

          <view class="record-main">
            <view class="record-time">{{ item.time }}</view>
            <!-- 消息内容：统一走Markdown，贴纸消息渲染为大表情 -->
            <view
                class="record-bubble"
                :class="parseSticker(item.msg.message) ? 'record-bubble-sticker' : 'markdown-body'"
            >
              <text v-if="parseSticker(item.msg.message)" class="record-sticker">{{ parseSticker(item.msg.message) }}</text>
              <up-parse
                  v-else
                  :content="renderMarkdown(item.msg.message)"
                  :tag-style="mdTagStyle"
              ></up-parse>
            </view>
          </view>
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
import { renderMarkdown, getMdTagStyle } from "@/utils/markdownUtil.js"
import { useChatHistory } from "@/views/chat/chatMessage.js"
import { parseSticker } from "@/views/chat/emojiData.js"
import { useTheme } from "@/utils/useTheme"
import { getOnFillColor, getSearchIconColor } from "@/utils/themeUtil"
import UserAvatar from "@/components/UserAvatar/index.vue"
import PageHeader from "@/components/PageHeader/index.vue"

const store = useStore()
const userInfo = computed(() => store.getters["user/getUserInfo"] || {})
const { isDark, themeClass } = useTheme()

// 富文本内联样式随皮肤切换
const mdTagStyle = computed(() => getMdTagStyle(isDark.value))

const customIconOff = computed(() => getSearchIconColor(isDark.value))
const customIconOn = computed(() => getOnFillColor(isDark.value))

const title = ref('聊天记录')
const targetName = ref('')
const targetType = ref('agent')
// 会话空间id由聊天页通过路由参数带入
const spaceId = ref(null)
// 检索时间范围 [开始日期, 结束日期]
const dateRange = ref(null)
const calendarShow = ref(false)
// 当前选中的快捷范围：all / 7 / today / 30 / custom
const activeRange = ref('all')

// 快捷时间范围，和后端按天过滤一一对应；「全部」不带时间条件，默认选中
const quickRanges = [
  { key: 'all', label: '全部' },
  { key: '7', label: '近7天', days: 7 },
  { key: 'today', label: '今天', days: 1 },
  { key: '30', label: '近30天', days: 30 }
]

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

// 渲染列表：按天插入日期分隔，消息自身只显示时分
const displayItems = computed(() => {
  const items = []
  let lastDay = ''
  const rows = chatHisMsgs.value || []
  rows.forEach((msg) => {
    const {day, time} = splitDateTime(msg.createdDt)
    if (day && day !== lastDay) {
      items.push({divider: true, key: 'day-' + items.length, label: day})
      lastDay = day
    }
    items.push({
      divider: false,
      key: 'msg-' + (msg.id === undefined ? items.length : msg.id),
      msg,
      time,
      self: msg.senderId === userInfo.value.id
    })
  })
  return items
})

/**
 * 拆出日期与时间：后端格式为 yyyy-MM-dd HH:mm:ss，缺失时降级为原文
 * @param value 创建时间
 * @returns {{day: string, time: string}}
 */
function splitDateTime(value) {
  const text = value === undefined || value === null ? '' : String(value).trim()
  const matched = text.match(/^(\d{4}-\d{2}-\d{2})[ T](\d{2}:\d{2})/)
  if (matched) {
    return {day: matched[1], time: matched[2]}
  }
  const timeMatched = text.match(/(\d{2}:\d{2})/)
  return {day: text, time: timeMatched ? timeMatched[1] : ''}
}

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
 * 选中快捷时间范围
 * @param range 快捷范围配置
 */
function selectQuickRange(range) {
  if (!range || activeRange.value === range.key) {
    return
  }
  if (range.key === 'all') {
    activeRange.value = 'all'
    dateRange.value = null
    loadChatRecords()
    return
  }
  const end = new Date()
  const start = new Date()
  // 近 7 天含今天，回溯 days-1 天
  start.setDate(end.getDate() - (range.days - 1))
  activeRange.value = range.key
  dateRange.value = [formatDay(start), formatDay(end)]
  loadChatRecords()
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
  activeRange.value = 'custom'
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
  box-sizing: border-box;
  background-color: $app-chat-bg;
}

.record-search {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  padding: 10px 14px 8px;
  overflow-x: auto;
}

.range-chip {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 5px;
  height: 30px;
  padding: 0 13px;
  border-radius: 999px;
  background-color: $app-bg-card;
  border: 1px solid $app-border-light;
  box-shadow: $app-shadow-card;
}

.range-chip__text {
  font-size: 12.5px;
  color: $app-text-secondary;
  font-weight: 500;
}

.range-chip.is-on {
  background-image: $app-gradient-fill;
  border-color: transparent;
  box-shadow: $app-shadow-brand;
}

.range-chip.is-on .range-chip__text {
  color: $app-fill-tx;
  font-weight: 600;
}

.range-chip--custom .range-chip__text {
  max-width: 118px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-body {
  flex: 1;
  min-height: 0;
  padding: 6px 14px 20px;
  box-sizing: border-box;
}

.record-tip {
  padding: 14px 0;
  text-align: center;
  font-size: 11.5px;
  color: $app-text-tertiary;
}

.record-item {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
}

.record-day {
  display: flex;
  justify-content: center;
  padding: 6px 0 10px;
}

.record-day__text {
  font-size: 11.5px;
  color: $app-text-tertiary;
  letter-spacing: 0.4px;
}

.record-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  width: 100%;
}

.record-item-self .record-row {
  flex-direction: row-reverse;
}

.record-avatar {
  flex-shrink: 0;
}

.record-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.record-item-self .record-main {
  align-items: flex-end;
}

.record-time {
  font-size: 10.5px;
  line-height: 14px;
  color: $app-text-tertiary;
  margin-bottom: 5px;
}

.record-bubble {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 14px;
  background-color: $app-bubble-agent-bg;
  color: $app-bubble-agent-text;
  box-shadow: $app-shadow-card;
}

.record-item-self .record-bubble {
  background-image: $app-gradient-fill;
  color: $app-fill-tx;
  box-shadow: $app-shadow-brand;
}

.record-bubble-sticker,
.record-item-self .record-bubble-sticker {
  padding: 0;
  background-color: transparent;
  background-image: none;
  box-shadow: none;
}

.record-sticker {
  font-size: 96rpx;
  line-height: 1.2;
}
</style>

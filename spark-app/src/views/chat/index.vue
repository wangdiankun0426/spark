<template>
  <view class="chat-container safe-area-page">
    <!-- 标题栏 -->
    <view class="chat-header">
      <view class="header-back" @click="goBack">
        <up-icon name="arrow-left" size="20" color="#fff"></up-icon>
      </view>
      <view class="header-title">
        <text v-if="target && target.id">{{ target.name }}</text>
        <text v-else class="header-tip">请选择聊天对象</text>
      </view>
      <!-- 聊天记录 -->
      <view
          v-if="target && target.chatSpaceId"
          class="header-record"
          @click="openChatRecord"
      >
        <up-icon name="clock" size="20" color="#fff"></up-icon>
      </view>
    </view>

    <!-- 消息区 -->
    <scroll-view
        scroll-y="true"
        class="chat-body"
        :scroll-top="scrollTop"
        :scroll-into-view="scrollIntoView"
        @scroll="onScroll"
    >
      <view v-if="historyLoading" class="chat-tip chat-history-loading">正在加载历史消息...</view>
      <view v-if="!chatMessageList.length" class="chat-divider">
        <text class="chat-tip">暂无最新消息</text>
      </view>

      <view
          v-for="(chatMsg, idx) in chatMessageList"
          :key="idx"
          :id="chatMsg.id === undefined ? '' : 'msg-' + chatMsg.id"
          class="chat-item"
          :class="{ 'chat-item-self': chatMsg.senderId === userInfo.id }"
      >
        <!-- 未读消息起点，向上加载的历史在该分隔线上方 -->
        <view v-if="chatMsg.unreadStart" class="chat-divider chat-unread-divider">
          <text class="chat-tip">以下是最新消息</text>
        </view>
        <!-- 消息头部 -->
        <view class="chat-msg-header">
          <template v-if="chatMsg.senderId === userInfo.id">
            <view class="chat-time">{{ chatMsg.createdDt }}</view>
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
                :name="target.name"
                :size="32"
            />
            <user-avatar
                v-else
                :type="targetType"
                :size="32"
            />
            <view class="chat-time">{{ chatMsg.createdDt }}</view>
          </template>
        </view>

        <!-- 消息内容 -->
        <view class="chat-msg-content">
          <!-- 自己发出的消息 -->
          <view
              v-if="chatMsg.senderId === userInfo.id"
              class="chat-bubble chat-bubble-user markdown-body"
          >
            <up-parse
                :content="renderMarkdown(chatMsg.message)"
                :tag-style="MD_TAG_STYLE"
            ></up-parse>
          </view>

          <!-- 对方消息 -->
          <view v-else class="chat-bubble chat-bubble-agent">
            <!-- 思考内容：占位期间只有状态，服务端推送思考过程后展示在状态下方，思考结束自动折叠 -->
            <template v-if="chatMsg.thinkingContent || chatMsg.thinkingPending">
              <view class="thinking-header" @click="toggleThinking(chatMsg)">
                <view v-if="!chatMsg.thinkingDone" class="thinking-loading">
                  <up-icon name="reload" size="14" color="#f5a623"></up-icon>
                </view>
                <text class="thinking-label">{{ thinkingLabel(chatMsg) }}</text>
                <up-icon
                    v-if="chatMsg.thinkingContent"
                    name="arrow-right"
                    size="12"
                    color="#999"
                    :class="{ 'thinking-arrow-open': !chatMsg.thinkingCollapsed }"
                ></up-icon>
              </view>
              <view
                  v-if="chatMsg.thinkingContent && !chatMsg.thinkingCollapsed"
                  class="thinking-content"
              >{{ chatMsg.thinkingContent }}</view>
            </template>
            <!-- 正式内容：统一走Markdown -->
            <template v-if="chatMsg.message">
              <view v-if="chatMsg.thinkingContent" class="thinking-divider"></view>
              <view class="markdown-body">
                <up-parse
                    :content="renderMarkdown(chatMsg.message)"
                    :tag-style="MD_TAG_STYLE"
                ></up-parse>
              </view>
            </template>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区-->
    <view class="chat-footer">
      <view class="input-wrapper">
        <input
            v-model="message"
            :maxlength="200"
            :disabled="!target || !target.id"
            placeholder="请输入聊天内容"
            confirm-type="send"
            :confirm-hold="true"
            class="chat-input"
            @confirm="handleConfirm"
        />
      </view>
    </view>

  </view>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, getCurrentInstance } from "vue"
import { onShow } from "@dcloudio/uni-app"
import { useStore } from "vuex"
import { createChatSpaceAPI, createAiChatSpaceAPI } from "@/api/chat/space.js"
import { renderMarkdown, MD_TAG_STYLE } from "@/utils/markdownUtil.js"
import { useChatHistory } from "@/views/chat/chatMessage.js"
import UserAvatar from "@/components/UserAvatar/index.vue"

const { proxy } = getCurrentInstance()
const store = useStore()
const message = ref("")

const websocket = ref(null)
const userInfo = computed(() => store.getters["user/getUserInfo"] || {})
const target = ref({})
const targetType = ref('agent')
const currentTargetId = computed(() => target.value && target.value.id)

// 消息列表与历史分页加载
const spaceIdRef = computed(() => target.value && target.value.chatSpaceId)
const {
  list: chatMessageList,
  loading: historyLoading,
  scrollTop,
  scrollIntoView,
  loadUnread,
  onScroll,
  scrollToBottom,
  reset: resetHistory
} = useChatHistory(spaceIdRef)

// 防止重复初始化
const userInfoLoaded = ref(false)
const eventChannelInited = ref(false)
const chatIniting = ref(false)

onShow(() => {
  // 页面从后台返回时，仅检查 WebSocket 是否需要重连
  if (!websocket.value) {
    initWebsocket()
  }
})

onMounted(() => {
  initEventChannel()
  initUserInfo().then(() => {
    initWebsocket()
    if (currentTargetId.value) {
      initChat()
    }
  })
})

onUnmounted(() => {
  closeWebsocket()
})

function initEventChannel() {
  if (eventChannelInited.value) return
  eventChannelInited.value = true
  try {
    const eventChannel = proxy.getOpenerEventChannel && proxy.getOpenerEventChannel()
    if (eventChannel && eventChannel.on) {
      eventChannel.on('setTarget', (data) => {
        target.value = data.target
        targetType.value = data.targetType
        if (currentTargetId.value) {
          initChat()
        }
      })
    }
  } catch (e) {
    console.error('eventChannel初始化失败', e)
  }
}

watch(currentTargetId, (newId, oldId) => {
  if (newId && newId !== oldId) {
    initChat()
  }
})

function initUserInfo() {
  if (userInfoLoaded.value && userInfo.value.id) {
    return Promise.resolve()
  }
  // 用户信息在登录时已存入 store，直接从 getter 获取即可
  userInfoLoaded.value = true
  return Promise.resolve(userInfo.value)
}

function initChat() {
  if (chatIniting.value) return
  chatIniting.value = true
  resetHistory()
  ensureChatSpace().finally(() => {
    chatIniting.value = false
  })
}

function ensureChatSpace() {
  if (target.value.chatSpaceId) {
    return loadNoReadMessageList()
  }
  if (!userInfo.value.id) {
    return initUserInfo().then(() => {
      return createChatSpace()
    })
  }
  return createChatSpace()
}

/**
 * 创建会话空间
 */
function createChatSpace() {
  if (targetType.value === 'user') {
    return createChatSpaceAPI({
      senderId: userInfo.value.id,
      receiverId: target.value.id
    }).then(handleSpaceCreated)
  }
  return createAiChatSpaceAPI({
    receiverId: target.value.id
  }).then(handleSpaceCreated)
}

/**
 * 会话创建成功后的公共处理：回填 spaceId、通知上一页、加载消息
 * @param res 创建接口响应
 */
function handleSpaceCreated(res) {
  if (res.code !== 200) {
    return
  }
  target.value.chatSpaceId = res.data.spaceId
  try {
    const eventChannel = proxy.getOpenerEventChannel && proxy.getOpenerEventChannel()
    if (eventChannel && eventChannel.emit) {
      eventChannel.emit('space-created', {
        targetId: target.value.id,
        spaceId: res.data.spaceId
      })
    }
  } catch (e) {
    console.error('通知space-created失败', e)
  }
  return loadNoReadMessageList()
}

/**
 * 加载首屏消息：未读消息 + 条数不足一页时补一页种子，更早的历史由滚动到顶部时按页加载
 * @returns {Promise<void>}
 */
function loadNoReadMessageList() {
  if (!target.value.chatSpaceId) return Promise.resolve()
  return loadUnread().then(scrollToBottom)
}

/**
 * 键盘「发送」/回车 提交
 * 小程序端 confirm 事件会带回最终输入值，优先取用，避免与 v-model 的同步时序打架
 * @param e 事件对象
 */
function handleConfirm(e) {
  const value = e && e.detail ? e.detail.value : undefined
  if (typeof value === 'string') {
    message.value = value
  }
  sendMessage()
}

function sendMessage() {
  if (!message.value || !message.value.trim()) {
    return
  }
  if (!websocket.value || websocket.value.readyState !== 1) {
    uni.showToast({ title: '连接未就绪', icon: 'none' })
    return
  }

  const chatMsg = {
    spaceId: target.value.chatSpaceId,
    senderId: userInfo.value.id,
    receiverId: target.value.id,
    message: message.value.trim(),
    createdDt: getCurrentDate()
  }
  const dataContent = { action: 2, chatMsg: chatMsg }
  sendWebSocketData(dataContent)
  chatMessageList.value.push(chatMsg)
  // AI首响有延迟，本地先挂一个占位气泡，服务端首个思考/回复消息会复用它
  if (targetType.value !== 'user') {
    createReply({
      spaceId: target.value.chatSpaceId,
      senderId: target.value.id,
      receiverId: userInfo.value.id
    }, true)
  }

  message.value = ''
  scrollToBottom()
}

function createPlatformSocket() {
  let wsUrl = process.env.BASE_HTTP_API + '/ws/chat';
  wsUrl = wsUrl.replace(/^http:/, 'ws:').replace(/^https:/, 'wss:')
  console.log('连接WebSocket:', wsUrl)
  // #ifdef H5
  return new WebSocket(wsUrl)
  // #endif
  // #ifdef MP-WEIXIN
  const task = wx.connectSocket({ url: wsUrl, header: { 'content-type': 'application/json' } })
  if (!task) {
    console.error('wx.connectSocket 返回为空，WebSocket 连接失败')
    return null
  }
  const wrapper = { _task: task, readyState: 0 }
  wrapper.setProp = (prop, fn) => { wrapper['_' + prop] = fn }
  task.onOpen(() => { wrapper.readyState = 1; wrapper._onopen && wrapper._onopen() })
  task.onMessage((res) => { wrapper._onmessage && wrapper._onmessage({ data: res.data }) })
  task.onError((err) => { wrapper._onerror && wrapper._onerror(err) })
  task.onClose(() => { wrapper.readyState = 3; wrapper._onclose && wrapper._onclose() })
  Object.defineProperty(wrapper, 'onopen', { set: (fn) => { wrapper._onopen = fn } })
  Object.defineProperty(wrapper, 'onmessage', { set: (fn) => { wrapper._onmessage = fn } })
  Object.defineProperty(wrapper, 'onerror', { set: (fn) => { wrapper._onerror = fn } })
  Object.defineProperty(wrapper, 'onclose', { set: (fn) => { wrapper._onclose = fn } })
  wrapper.send = (data) => { task.send({ data }) }
  wrapper.close = () => { task.close() }
  return wrapper
  // #endif
}

function initWebsocket() {
  if (websocket.value) {
    return
  }
  websocket.value = createPlatformSocket()
  if (!websocket.value) return
  websocket.value.onerror = setErrorMessage
  websocket.value.onopen = setOnopenMessage
  websocket.value.onmessage = setOnmessageMessage
  websocket.value.onclose = setOncloseMessage
}

function closeWebsocket() {
  if (!websocket.value) return
  websocket.value.onclose = null
  websocket.value.onerror = null
  websocket.value.onmessage = null
  websocket.value.onopen = null
  websocket.value.close()
  websocket.value = null
}

function setErrorMessage() {
  console.log('webSocket 连接发生错误')
}

function setOnopenMessage() {
  console.log('webSocket 连接成功')
  if (!userInfo.value.id) return
  const chatMsg = { senderId: userInfo.value.id }
  const dataContent = { action: 1, chatMsg: chatMsg }
  sendWebSocketData(dataContent)
}

function sendWebSocketData(data) {
  if (!websocket.value) return
  const dataStr = JSON.stringify(data)
  websocket.value.send(dataStr)
}

/**
 * 获取进行中的AI回复气泡
 * 服务端落库后的消息一定带id，用户自己发的消息senderId为自己，两者都不算进行中
 * @returns 进行中的气泡，没有则返回null
 */
function ongoingReply() {
  if (!chatMessageList.value.length) {
    return null
  }
  const lastMsg = chatMessageList.value[chatMessageList.value.length - 1]
  if (lastMsg.id !== undefined || lastMsg.senderId === userInfo.value.id) {
    return null
  }
  return lastMsg
}

/**
 * 思考区状态文案，智能体区分思考中与思考完成，模型只有等待回复
 * @param chatMsg 消息
 * @returns 状态文案
 */
function thinkingLabel(chatMsg) {
  if (targetType.value !== 'agent') {
    return '正在回复...'
  }
  return chatMsg.thinkingDone ? '思考完成' : '思考中...'
}

/**
 * 新建AI回复气泡，思考内容与正式内容分两段展示
 * @param chatMsg 服务端消息
 * @param pending 是否为本地占位气泡，等待服务端首个响应
 * @returns 新建的气泡
 */
function createReply(chatMsg, pending) {
  const reply = {
    spaceId: chatMsg.spaceId,
    senderId: chatMsg.senderId,
    receiverId: chatMsg.receiverId,
    createdDt: chatMsg.createdDt || getCurrentDate(),
    thinkingPending: !!pending,
    thinkingContent: '',
    thinkingDone: !pending,
    thinkingCollapsed: false,
    message: '',
    references: null
  }
  chatMessageList.value.push(reply)
  return reply
}

/**
 * 处理思考状态消息，思考内容追加到当前回复气泡
 * @param chatMsg 服务端消息
 */
function handleThinking(chatMsg) {
  const reply = ongoingReply() || createReply(chatMsg)
  // 思考消息先于正式内容到达，思考尚未结束，展开思考过程
  reply.thinkingDone = false
  reply.thinkingCollapsed = false
  reply.thinkingContent += chatMsg.message || ''
  scrollToBottom()
}

/**
 * 折叠/展开思考内容
 * @param chatMsg 消息
 */
function toggleThinking(chatMsg) {
  if (!chatMsg.thinkingContent) {
    return
  }
  chatMsg.thinkingCollapsed = !chatMsg.thinkingCollapsed
}

/**
 * 处理回复内容，流式分片逐块累加，完整消息直接覆盖，避免同一段内容重复展示
 * @param chatMsg 服务端消息
 */
function handleReply(chatMsg) {
  const isComplete = chatMsg.id !== undefined
  const reply = ongoingReply() || createReply(chatMsg)
  // 收到正式内容即视为思考结束，清除占位状态并自动折叠思考过程
  reply.thinkingDone = true
  reply.thinkingPending = false
  reply.thinkingCollapsed = true
  if (isComplete) {
    reply.id = chatMsg.id
    // 完整消息携带全量答案，覆盖已累加的分片
    if (chatMsg.message) {
      reply.message = chatMsg.message
    }
  } else {
    reply.message += chatMsg.message || ''
  }
  scrollToBottom()
}

function setOnmessageMessage(event) {
  let parse = null
  try {
    parse = JSON.parse(event.data)
  } catch (e) {
    console.error('解析消息失败', e)
    return
  }
  const chatMsg = parse.chatMsg
  // 仅处理当前会话的消息
  if (currentTargetId.value !== chatMsg.senderId) {
    return
  }
  // 思考状态消息
  if (parse.action === 6) {
    handleThinking(chatMsg)
    return
  }
  // 带id的是落库后的完整消息，说明本次回复结束
  if (chatMsg.id !== undefined) {
    const dataContent = { action: 3, chatMsg: chatMsg }
    sendWebSocketData(dataContent)
  }
  handleReply(chatMsg)
}

function setOncloseMessage() {
  console.log('webSocket 连接关闭')
  setTimeout(() => {
    initWebsocket()
  }, 3000)
}

function getCurrentDate() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hour = String(now.getHours()).padStart(2, '0')
  const minute = String(now.getMinutes()).padStart(2, '0')
  const second = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

function goBack() {
  uni.navigateBack()
}

/**
 * 打开聊天记录
 */
function openChatRecord() {
  if (!target.value.chatSpaceId) {
    return
  }
  uni.navigateTo({
    url: '/views/chat/chatMsgRecord?spaceId=' + target.value.chatSpaceId +
        '&targetType=' + targetType.value +
        '&targetName=' + encodeURIComponent(target.value.name || '') +
        '&title=' + encodeURIComponent(target.value.name || '聊天记录')
  })
}
</script>

<style scoped lang="scss">
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f7fa;
  overflow: hidden;
}

.chat-header {
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
  gap: 12px;
}

.header-title {
  flex: 1;
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  color: #fff;
}

/* 标题右侧的聊天记录入口 */
.header-record {
  display: flex;
  align-items: center;
  padding-left: 12px;
}

.header-tip {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.chat-body {
  flex: 1;
  padding: 8px 0 80px;
  min-height: 0;
}

.chat-divider {
  display: flex;
  justify-content: center;
  margin: 8px 0;
}

/* 未读起点分隔线：撑满消息行宽 */
.chat-unread-divider {
  align-self: stretch;
}

/* 向上加载历史时的提示 */
.chat-history-loading {
  text-align: center;
  padding: 8px 0;
}

.chat-tip {
  font-size: 12px;
  color: #999;
  padding: 4px 12px;
  background-color: #e9e9e9;
  border-radius: 100px;
}

.chat-item {
  display: flex;
  flex-direction: column;
  padding: 8px 16px;
}

.chat-item-self {
  align-items: flex-end;
}

.chat-msg-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.chat-item-self .chat-msg-header {
  justify-content: flex-end;
}


.chat-time {
  font-size: 11px;
  color: #999;
}

.chat-msg-content {
  max-width: 75%;
}

.chat-bubble {
  padding: 10px 14px;
  border-radius: 8px;
  word-break: break-word;
  position: relative;
}

.chat-bubble-user {
  background-color: #0052cc;
  color: #fff;
  border-top-right-radius: 2px;
}

.chat-bubble-agent {
  background-color: #fff;
  color: #333;
  border-top-left-radius: 2px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
}

.thinking-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.thinking-label {
  font-size: 12px;
  color: #666;
}

.thinking-arrow-open {
  transform: rotate(90deg);
}

.thinking-loading {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.thinking-content {
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px dashed #e9e9e9;
  font-size: 12px;
  color: #666;
  line-height: 1.6;
  white-space: pre-line;
}

.thinking-divider {
  height: 1px;
  background-color: #f0f0f0;
  margin: 8px 0;
}

.chat-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #fff;
  padding: 10px 12px calc(10px + env(safe-area-inset-bottom));
  border-top: 1px solid #e9e9e9;
  z-index: 100;
  box-sizing: border-box;
}

/* 单行输入框容器：发送按钮去掉后由输入框占满整行 */
.input-wrapper {
  display: flex;
  align-items: center;
  height: 44px;
  padding: 0 16px;
  background-color: #f5f7fa;
  border-radius: 10px;
}

.chat-input {
  flex: 1;
  height: 24px;
  font-size: 15px;
  background-color: transparent;
  border: none;
}

/* Markdown 基线排版，标签级样式由 up-parse 的 tag-style 内联下发 */
.markdown-body {
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

/* 代码高亮：小程序端 rich-text 节点不认外部样式，仅 H5 生效 */
:deep(.markdown-body) {
  .hljs-keyword { color: #d73a49; }
  .hljs-string { color: #032f62; }
  .hljs-number { color: #005cc5; }
  .hljs-comment { color: #6a737d; font-style: italic; }
  .hljs-function { color: #6f42c1; }
  .hljs-title { color: #6f42c1; }
  .hljs-params { color: #24292e; }
  .hljs-literal { color: #005cc5; }
  .hljs-built_in { color: #e36209; }
  .hljs-operator { color: #d73a49; }
  .hljs-punctuation { color: #24292e; }
  .hljs-property { color: #005cc5; }
  .hljs-variable { color: #e36209; }
  .hljs-attr { color: #6f42c1; }
  .hljs-tag { color: #22863a; }
  .hljs-name { color: #22863a; }
}
</style>

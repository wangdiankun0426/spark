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
    </view>

    <!-- 消息区 -->
    <scroll-view
        ref="scrollviewRef"
        scroll-y="true"
        class="chat-body"
        :scroll-top="scrollTop"
        @scroll="onScroll"
    >
      <view class="chat-divider">
        <text class="chat-tip">{{ chatMsgs.length ? '以下是最新消息' : '暂无最新消息' }}</text>
      </view>

      <view
          v-for="(chatMsg, idx) in chatMsgs"
          :key="idx"
          class="chat-item"
          :class="{ 'chat-item-self': chatMsg.senderId === userInfo.id }"
      >
        <!-- 时间 + 头像 -->
        <view class="chat-msg-header">
          <template v-if="chatMsg.senderId === userInfo.id">
            <view class="chat-time">{{ chatMsg.createdDt }}</view>
            <user-avatar type="self" name="我" :size="32"/>
          </template>
          <template v-else>
            <user-avatar v-if="targetType === 'user'" type="user" :userId="chatMsg.senderId" :name="target.name" :size="32"/>
            <user-avatar v-else :type="targetType" :size="32"/>
            <view class="chat-time">{{ chatMsg.createdDt }}</view>
          </template>
        </view>

        <!-- 消息内容 -->
        <view class="chat-msg-content">
          <!-- 自己发出的消息 -->
          <view v-if="chatMsg.senderId === userInfo.id" class="chat-bubble chat-bubble-user markdown-body">
            <up-parse :content="renderMarkdown(chatMsg.message)"></up-parse>
          </view>

          <!-- 对方消息 -->
          <view v-else class="chat-bubble chat-bubble-agent" :class="{ 'chat-bubble-thinking': chatMsg.thinkingContent }">
            <!-- 思考内容 -->
            <template v-if="chatMsg.thinkingContent">
              <view class="thinking-header">
                <view v-if="!chatMsg.thinkingDone" class="thinking-loading">
                  <up-icon name="reload" size="14" color="#f5a623"></up-icon>
                </view>
                <text class="thinking-label">{{ chatMsg.thinkingDone ? '思考完成' : '思考中...' }}</text>
              </view>
              <view class="thinking-content">{{ chatMsg.thinkingContent }}</view>
            </template>
            <!-- 正式内容 -->
            <template v-if="chatMsg.message">
              <view v-if="chatMsg.thinkingContent" class="thinking-divider"></view>
              <view class="chat-msg-text markdown-body">
                <up-parse :content="renderMarkdown(chatMsg.message)"></up-parse>
              </view>
            </template>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区 -->
    <view class="chat-footer">
      <view class="input-wrapper">
        <textarea
            v-model="message"
            :maxlength="200"
            :disabled="!target || !target.id"
            placeholder="请输入聊天内容"
            :show-confirm-bar="false"
            class="chat-input"
        />
      </view>
      <view class="send-row">
        <button
            class="send-btn"
            :disabled="!target || !target.id || !message"
            @click="sendMessage"
        >发送</button>
      </view>
    </view>

  </view>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, getCurrentInstance } from "vue"
import { onShow } from "@dcloudio/uni-app"
import { useStore } from "vuex"
import { createChatSpaceAPI } from "@/api/chat/space.js"
import { getNoReadMsgListAPI } from "@/api/chat/msg.js"
import { renderMarkdown } from "@/utils/markdownUtil.js"
import UserAvatar from "@/components/UserAvatar/index.vue"

const { proxy } = getCurrentInstance()
const store = useStore()
const scrollviewRef = ref(null)
const message = ref("")
const chatMsgs = ref([])
const scrollTop = ref(0)
const scrollHeight = ref(0)

const websocket = ref(null)
const userInfo = computed(() => store.getters["user/getUserInfo"] || {})
const target = ref({})
const targetType = ref('agent')
const currentTargetId = computed(() => target.value && target.value.id)

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
  chatMsgs.value = []
  ensureChatSpace().finally(() => {
    chatIniting.value = false
  })
}

function ensureChatSpace() {
  if (target.value.chatSpaceId) {
    return loadNoReadMsgs()
  }
  if (!userInfo.value.id) {
    return initUserInfo().then(() => {
      return createChatSpace()
    })
  }
  return createChatSpace()
}

function createChatSpace() {
  const data = {
    senderId: userInfo.value.id,
    receiverId: target.value.id
  }
  return createChatSpaceAPI(data).then(res => {
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
    return loadNoReadMsgs()
  })
}

function loadNoReadMsgs() {
  if (!target.value.chatSpaceId) return Promise.resolve()
  const query = { spaceId: target.value.chatSpaceId }
  return getNoReadMsgListAPI(query).then(res => {
    if (res.code !== 200) {
      return
    }
    if (res.data !== null) {
      chatMsgs.value = res.data
    }
    scrollToBottom()
  })
}

function scrollToBottom() {
  setTimeout(() => {
    scrollHeight.value = scrollHeight.value + 100
    scrollTop.value = scrollHeight.value
  }, 100)
}

function onScroll(e) {
  scrollHeight.value = e.detail.scrollHeight
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
  chatMsgs.value.push(chatMsg)

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

function handleThinkingMessage(chatMsg) {
  if (currentTargetId.value !== chatMsg.senderId) return
  if (chatMsgs.value.length > 0) {
    let lastChatMsg = chatMsgs.value[chatMsgs.value.length - 1]
    if (lastChatMsg.thinkingContent !== undefined && !lastChatMsg.hasReply) { lastChatMsg.thinkingContent = (lastChatMsg.thinkingContent || '') + (chatMsg.message || '')
      scrollToBottom()
      return
    }
  }
  const newMsg = {
    ...chatMsg,
    thinkingContent: chatMsg.message,
    message: '',
    hasReply: false
  }
  chatMsgs.value.push(newMsg)
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

  let chatMsg = parse.chatMsg

  // 服务端消息可能没有 createdDt，补充当前时间
  if (!chatMsg.createdDt) {
    chatMsg.createdDt = getCurrentDate()
  }

  if (parse.action === 6) {
    handleThinkingMessage(chatMsg)
    return
  }

  if (chatMsg.id !== undefined) {
    const dataContent = { action: 3, chatMsg: chatMsg }
    sendWebSocketData(dataContent)
  }

  if (currentTargetId.value !== chatMsg.senderId) return

  if (chatMsgs.value.length > 0) {
    let lastChatMsg = chatMsgs.value[chatMsgs.value.length - 1]
    if (lastChatMsg.thinkingContent !== undefined && lastChatMsg.senderId === chatMsg.senderId) {
      lastChatMsg.thinkingDone = true
      lastChatMsg.hasReply = true
      if (chatMsg.id !== undefined) {
        // 完整消息到达，替换为最终内容
        lastChatMsg.message = chatMsg.message || ''
        lastChatMsg.id = chatMsg.id
      } else {
        // 流式块，追加内容
        lastChatMsg.message = (lastChatMsg.message || '') + (chatMsg.message || '')
      }
      if (chatMsg.id !== undefined) {
        const dataContent = { action: 4, chatMsg: chatMsg }
        sendWebSocketData(dataContent)
      }
      scrollToBottom()
      return
    }

    if (lastChatMsg.id === undefined && lastChatMsg.senderId !== userInfo.value.id && lastChatMsg.thinkingContent === undefined) {
      if (chatMsg.id === undefined) {
        lastChatMsg.message = (lastChatMsg.message || '') + (chatMsg.message || '')
        scrollToBottom()
        return
      }
      lastChatMsg.id = chatMsg.id
      if (chatMsg.message) {
        lastChatMsg.message = chatMsg.message
      }
      const dataContent = { action: 4, chatMsg: chatMsg }
      sendWebSocketData(dataContent)
      scrollToBottom()
      return
    }
  }

  chatMsgs.value.push(chatMsg)
  if (chatMsg.id !== undefined) {
    const dataContent = { action: 4, chatMsg: chatMsg }
    sendWebSocketData(dataContent)
  }
  scrollToBottom()
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

.header-tip {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.chat-body {
  flex: 1;
  padding: 8px 0 180px;
  min-height: 0;
}

.chat-divider {
  display: flex;
  justify-content: center;
  margin: 8px 0;
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

.chat-bubble-thinking {
  border-left: 3px solid #f5a623;
}

.thinking-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  padding-bottom: 6px;
  border-bottom: 1px dashed #e9e9e9;
}

.thinking-label {
  font-size: 12px;
  color: #666;
}

.thinking-loading {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.thinking-content {
  font-size: 12px;
  color: #666;
  line-height: 1.6;
}

.thinking-divider {
  height: 1px;
  background-color: #f0f0f0;
  margin: 8px 0;
}

.chat-msg-text {
  font-size: 14px;
  line-height: 1.7;
}
.chat-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #fff;
  padding: 10px 10px 20px;
  border-top: 1px solid #e9e9e9;
  z-index: 100;
  box-sizing: border-box;
}

.input-wrapper {
  background-color: #f5f7fa;
  border-radius: 10px;
  padding: 8px 16px;
  min-height: 120px;
  max-height: 120px;
}

.chat-input {
  width: 100%;
  height: 110px;
  font-size: 15px;
  line-height: 1.6;
  background-color: transparent;
  border: none;
  overflow-y: auto;
  resize: none;
}

.send-row {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
}

.send-btn {
  background-color: #0052cc;
  color: #fff;
  border: none;
  border-radius: 8px;
  height: 40px;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
}

.send-btn[disabled] {
  background-color: #c9cdd4;
  color: #999;
}

/* Markdown 渲染样式（作用于 up-parse 内部节点） */
.markdown-body {
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}

.markdown-body .h1,
.markdown-body .h2,
.markdown-body .h3,
.markdown-body .h4,
.markdown-body .h5,
.markdown-body .h6 {
  font-weight: 600;
  margin: 12px 0 8px;
}

.markdown-body .h1 { font-size: 20px; }
.markdown-body .h2 { font-size: 18px; }
.markdown-body .h3 { font-size: 16px; }
.markdown-body .h4 { font-size: 15px; }
.markdown-body .h5,
.markdown-body .h6 { font-size: 14px; }

.markdown-body .p {
  margin: 0 0 8px;
}

.markdown-body .ul,
.markdown-body .ol {
  padding-left: 20px;
  margin: 8px 0;
}

.markdown-body .li {
  margin: 4px 0;
}

.markdown-body .pre {
  background-color: #f6f8fa;
  color: #24292e;
  padding: 12px;
  border-radius: 6px;
  margin: 8px 0;
  font-family: Consolas, Monaco, "Courier New", monospace;
  white-space: pre-wrap;
  word-break: break-all;
}

.markdown-body .code {
  font-family: Consolas, Monaco, "Courier New", monospace;
  background-color: rgba(255, 255, 255, 0.9);
  color: #24292e;
  padding: 2px 4px;
  border-radius: 3px;
  font-size: 13px;
}

.markdown-body .pre .code {
  background-color: transparent;
  padding: 0;
  color: #24292e;
}

.markdown-body .blockquote {
  border-left: 4px solid rgba(255, 255, 255, 0.4);
  padding-left: 12px;
  color: rgba(255, 255, 255, 0.9);
  margin: 8px 0;
}

.markdown-body .a {
  color: #e6f7ff;
  text-decoration: underline;
}

.markdown-body .table {
  width: 100%;
  border-collapse: collapse;
  margin: 8px 0;
}

.markdown-body .th,
.markdown-body .td {
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 6px;
  text-align: left;
}

.markdown-body .th {
  background-color: rgba(0, 0, 0, 0.1);
  font-weight: 600;
}

.markdown-body .hr {
  border: none;
  border-top: 1px solid rgba(255, 255, 255, 0.3);
  margin: 12px 0;
}

/* 代码高亮色（作为 highlight.js 主题的后备，同时适配小程序） */
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
</style>

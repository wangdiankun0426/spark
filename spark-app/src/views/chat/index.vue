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
      <view
          class="input-wrapper"
          :class="{ 'input-wrapper-disabled': inputDisabled }">
        <input
            v-model="message"
            :maxlength="200"
            :disabled="inputDisabled"
            :placeholder="inputPlaceholder"
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
// 连接是否可用，断开与重连期间为false，用于锁定输入框
const connected = ref(false)
const userInfo = computed(() => store.getters["user/getUserInfo"] || {})
const target = ref({})
const targetType = ref('agent')
const currentTargetId = computed(() => target.value && target.value.id)

// 重连间隔，连接断开后按此间隔持续尝试重连
const RECONNECT_DELAY = 3000
// 重连定时器
let reconnectTimer = null
// 页面是否已卸载，卸载后不再重连
let destroyed = false

// 未选择聊天对象或连接不可用时禁止输入
const inputDisabled = computed(() => !currentTargetId.value || !connected.value)
// 断开与重连期间给出提示文案
const inputPlaceholder = computed(() => (connected.value ? '请输入聊天内容' : '连接服务器中...'))

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
  // 页面从后台返回时，检查 WebSocket 是否需要重连
  if (!websocket.value || websocket.value.readyState === 3) {
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

/**
 * 初始化 websocket，已有未关闭的连接时不重复创建
 */
function initWebsocket() {
  clearReconnectTimer()
  if (destroyed) return
  // 已有连接且未关闭（含正在建立中）直接复用
  if (websocket.value && websocket.value.readyState !== 3) {
    return
  }
  websocket.value = createPlatformSocket()
  if (!websocket.value) {
    scheduleReconnect()
    return
  }
  websocket.value.onerror = setErrorMessage
  websocket.value.onopen = setOnopenMessage
  websocket.value.onmessage = setOnmessageMessage
  websocket.value.onclose = setOncloseMessage
}

/**
 * 清理重连定时器
 */
function clearReconnectTimer() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

/**
 * 安排一次重连，已有待执行的定时器时不重复安排
 */
function scheduleReconnect() {
  if (destroyed || reconnectTimer) {
    return
  }
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    initWebsocket()
  }, RECONNECT_DELAY)
}

function closeWebsocket() {
  destroyed = true
  clearReconnectTimer()
  connected.value = false
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
  connected.value = false
  // 当前连接已不可用（部分端报错后不触发 onclose，readyState 不再是 3），置空引用保证重连时能重新创建
  websocket.value = null
  scheduleReconnect()
}

function setOnopenMessage() {
  console.log('webSocket 连接成功')
  // 连接可用，解锁输入框
  connected.value = true
  clearReconnectTimer()
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

/**
 * 是否为当前会话的消息
 * 自己账号发出的消息按接收人判断（多端登录时服务端会回推），对方消息按发送人判断
 * @param chatMsg 服务端消息
 */
function isCurrentChatMsg(chatMsg) {
  if (chatMsg.senderId === userInfo.value.id) {
    return currentTargetId.value === chatMsg.receiverId
  }
  return currentTargetId.value === chatMsg.senderId
}

/**
 * 处理自己账号发出的消息：本端乐观插入后服务端回推的、或其他终端发出后广播过来的
 * 本端乐观插入的那条没有服务端id，按发送人+接收人+内容从列表尾部找最近一条未落库的回填id，找不到则直接追加
 * @param chatMsg 服务端消息
 */
function adoptSelfMsg(chatMsg) {
  // 自己发的消息一定是落库后的完整消息
  if (chatMsg.id === undefined) {
    return
  }
  // 同一条消息重复到达时按id去重
  if (chatMessageList.value.some(item => item.id === chatMsg.id)) {
    return
  }
  for (let i = chatMessageList.value.length - 1; i >= 0; i--) {
    const item = chatMessageList.value[i]
    // 只认尚未落库的本地乐观消息
    if (item.id !== undefined || item.senderId !== chatMsg.senderId
        || item.receiverId !== chatMsg.receiverId || item.message !== chatMsg.message) {
      continue
    }
    // 回填服务端id，后续按id去重与历史合并都依赖它；createdDt保留本地时间
    item.id = chatMsg.id
    return
  }
  // 本地没有乐观副本，说明是其他终端发出的，追加展示
  chatMessageList.value.push(chatMsg)
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
  // 自己账号发出的消息由其他端广播而来，不需要签收与已读
  const selfMsg = chatMsg.senderId === userInfo.value.id
  // 带id的是落库后的完整消息，推送到当前端即表示已送达，用户是否正在看这个会话不影响签收
  if (!selfMsg && chatMsg.id !== undefined) {
    const dataContent = { action: 3, chatMsg: chatMsg }
    sendWebSocketData(dataContent)
  }
  // 仅处理当前会话的消息
  if (!isCurrentChatMsg(chatMsg)) {
    return
  }
  // 自己账号发出的消息：多端同步回推，只做去重或追加，不走签收、已读与AI回复拼接
  if (selfMsg) {
    adoptSelfMsg(chatMsg)
    return
  }
  // 思考状态消息
  if (parse.action === 6) {
    handleThinking(chatMsg)
    return
  }
  // 带id的完整消息已展示在当前会话，用户看到了内容，回发已读
  if (chatMsg.id !== undefined) {
    sendWebSocketData({ action: 4, chatMsg: { id: chatMsg.id } })
  }
  handleReply(chatMsg)
}

/**
 * 收尾进行中的AI回复气泡，避免一直停在思考中
 */
function finalizePendingReply() {
  const reply = ongoingReply()
  if (reply) {
    reply.thinkingDone = true
    reply.thinkingPending = false
  }
}

/**
 * 连接关闭时回调，页面未卸载时持续尝试重连
 */
function setOncloseMessage() {
  // 已存在新连接时，忽略旧连接的关闭事件
  if (websocket.value && websocket.value.readyState !== 3) {
    return
  }
  console.log('webSocket 连接关闭')
  // 连接已断开，置空引用，重连时才能重新创建
  websocket.value = null
  connected.value = false
  // 连接断开后本次回复不会再送达，收尾进行中的气泡
  finalizePendingReply()
  scheduleReconnect()
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
  background-color: #0052cc;
  padding: 16px 16px;
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

.chat-unread-divider {
  align-self: stretch;
}

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
  align-self: flex-start;
  max-width: 75%;
}

.chat-item-self .chat-msg-content {
  align-self: flex-end;
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

.input-wrapper {
  display: flex;
  align-items: center;
  height: 44px;
  padding: 0 16px;
  background-color: #f5f7fa;
  border-radius: 10px;
}

.input-wrapper-disabled {
  background-color: #eef0f3;
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

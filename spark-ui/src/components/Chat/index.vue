<template>
  <div class="chat-panel">
    <!-- 标题栏 -->
    <div class="chat-panel-header">
      <slot name="header-left">
        <el-icon class="chat-panel-header-icon">
          <ChatDotRound />
        </el-icon>
        <span v-if="target && target.id">{{ target.name }}</span>
        <span v-else class="chat-panel-header-tip">请选择聊天对象</span>
      </slot>
      <div class="chat-panel-header-right">
        <slot name="header-right"></slot>
        <el-tooltip v-if="activeSpaceId" content="聊天记录" placement="bottom">
          <span class="toolbar-btn" @click="openChatMessage">
            <el-icon><ChatLineSquare /></el-icon>
          </span>
        </el-tooltip>
      </div>
    </div>

    <!-- 消息区 -->
    <el-scrollbar
        ref="scrollbarRef"
        class="chat-body"
        always
        @scroll="onScroll"
    >
      <div v-if="historyLoading" class="chat-tip chat-history-loading">正在加载历史消息...</div>
      <slot v-if="!chatMessageList.length" name="empty">
        <el-divider border-style="dashed">
          <span class="chat-tip">暂无最新消息</span>
        </el-divider>
      </slot>
      <div
          v-for="(chatMsg, idx) in chatMessageList"
          :key="idx"
          class="chat-item"
          :class="{ 'chat-item-self': chatMsg.senderId === userInfo.id }"
      >
        <el-divider
            v-if="chatMsg.unreadStart"
            class="chat-unread-divider"
            border-style="dashed"
        >
          <span class="chat-tip">以下是最新消息</span>
        </el-divider>
        <!-- 消息头部 -->
        <div class="chat-header">
          <user-avatar
              v-if="chatMsg.senderId === userInfo.id || targetType === 'user'"
              :user-id="chatMsg.senderId"
              :size="28"
          />
          <div
              v-else
              class="chat-avatar chat-avatar-ai"
              :class="'chat-avatar-' + targetType"
          >
            <el-icon>
              <MagicStick v-if="targetType === 'agent'" />
              <Cpu v-else />
            </el-icon>
          </div>
          <span class="chat-time">
            {{ chatMsg.createdDt }}
          </span>
        </div>
        <!-- 我方消息内容 -->
        <div
            v-if="chatMsg.senderId === userInfo.id"
            class="chat-bubble-wrap"
        >
          <!-- 悬浮工具条 -->
          <div class="chat-bubble-toolbar">
            <el-tooltip content="复制" placement="top">
              <span class="toolbar-btn" @click="copyMessage(chatMsg.message)">
                <el-icon><CopyDocument /></el-icon>
              </span>
            </el-tooltip>
          </div>
          <div class="chat-bubble chat-bubble-user">
            <v-md-preview class="chat-msg" :text="chatMsg.message" />
          </div>
        </div>
        <!-- 对方消息内容 -->
        <div
            v-else
            class="chat-bubble-wrap"
        >
          <!-- 悬浮工具条 -->
          <div v-if="chatMsg.message" class="chat-bubble-toolbar">
            <el-tooltip content="复制" placement="top">
              <span class="toolbar-btn" @click="copyMessage(chatMsg.message)">
                <el-icon><CopyDocument /></el-icon>
              </span>
            </el-tooltip>
          </div>
          <div class="chat-bubble chat-bubble-agent">
            <!-- 思考内容：占位期间只有状态，服务端推送思考过程后展示在状态下方，思考结束自动折叠 -->
            <template v-if="chatMsg.thinkingContent || chatMsg.thinkingPending">
              <div
                  class="thinking-header"
                  :class="{ 'thinking-toggle': chatMsg.thinkingContent }"
                  @click="toggleThinking(chatMsg)"
              >
                <el-icon v-if="!chatMsg.thinkingDone" class="thinking-loading"><Loading /></el-icon>
                <span class="thinking-label">{{ thinkingLabel(chatMsg) }}</span>
                <el-icon
                    v-if="chatMsg.thinkingContent"
                    class="thinking-arrow"
                    :class="{ 'thinking-arrow-open': !chatMsg.thinkingCollapsed }"
                ><ArrowRight /></el-icon>
              </div>
              <div
                  v-if="chatMsg.thinkingContent && !chatMsg.thinkingCollapsed"
                  class="thinking-content"
              >{{ chatMsg.thinkingContent }}</div>
            </template>
            <!-- 正式内容：统一走Markdown -->
            <template v-if="chatMsg.message">
              <div v-if="chatMsg.thinkingContent" class="thinking-divider"></div>
              <v-md-preview class="chat-msg" :text="chatMsg.message" />
            </template>
            <!-- 引用 -->
            <llm-references
                v-if="targetType === 'agent' && chatMsg.references && chatMsg.references.length"
                :references="chatMsg.references"
            />
          </div>
        </div>
      </div>
    </el-scrollbar>

    <!-- 输入区：回车发送、Shift + 回车换行 -->
    <div class="chat-footer">
      <el-input
          v-model="message"
          :rows="4"
          type="textarea"
          :maxlength="maxLength"
          show-word-limit
          :placeholder="inputPlaceholder"
          :disabled="inputDisabled"
          @keydown.enter="handleEnter"
      />
    </div>

    <!-- 聊天记录 -->
    <chat-msg-record
        v-model="recordDrawer"
        :title="recordTitle"
        :space-id="activeSpaceId"
        :target-type="targetType"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { MagicStick, Cpu, ChatDotRound, CopyDocument, Loading, ArrowRight, Clock } from '@element-plus/icons-vue'
import { createChatSpaceAPI } from '@/api/chat/space.js'
import { getCurrentDate } from '@/utils/dateUtil.js'
import { useChatHistory } from '@/components/Chat/chatMessage.js'
import UserAvatar from '@/components/UserAvatar/index.vue'
import ChatMsgRecord from '@/components/Chat/chatMsgRecord.vue'
import LlmReferences from '@/components/Chat/llmReferences.vue'

const props = defineProps({
  // 聊天对象 { id, name, chatSpaceId, sessionKey }
  target: { type: Object, default: () => ({}) },
  // 对方类型：agent=智能体，model=模型，user=用户
  targetType: { type: String, default: 'agent' },
  // 懒创建会话：非空时由父组件负责创建空间并返回 spaceId
  spaceCreator: { type: Function, default: null },
  // 流式回复期间锁定发送，避免两条回复交叉拼接
  lockWhileStreaming: { type: Boolean, default: false },
  // 输入内容长度上限
  maxLength: { type: Number, default: 200 },
  // 输入框占位文案
  placeholder: { type: String, default: '请输入聊天内容，回车发送，Shift + 回车换行' }
})

const emit = defineEmits([
  // 收到对方消息时触发，父组件用于更新用户列表未读数
  'message-received',
  // 聊天空间创建成功时触发，payload: { targetId, spaceId, sessionKey }
  'space-created'
])

const store = useStore()
const userInfo = computed(() => store.getters['user/getUserInfo'])

const websocket = ref(null)
// websocket 是否已连接，断开与重连期间为false，用于锁定输入框
const connected = ref(false)
const message = ref('')
const scrollbarRef = ref(null)
const recordDrawer = ref(false)
const recordTitle = ref('')
const activeSpaceId = ref((props.target && props.target.chatSpaceId) || null)
const sending = ref(false)

// 消息列表与历史分页加载
const {
  list: chatMessageList,
  loading: historyLoading,
  loadUnread,
  loadMore,
  onScroll,
  isScrollable,
  scrollToBottom,
  reset: resetHistory
} = useChatHistory(activeSpaceId, scrollbarRef)

// 组件是否已销毁，避免销毁后触发 onclose 自动重连
let destroyed = false

const currentTargetId = computed(() => props.target && props.target.id)

// 是否为AI会话：AI会话一个会话一个空间，按spaceId过滤消息；用户会话按发送人过滤
const aiChat = computed(() => props.targetType !== 'user')

/**
 * 会话身份键，AI会话由父组件提供稳定的 sessionKey，其余沿用对象id
 */
function targetKey() {
  const target = props.target || {}
  if (target.sessionKey) {
    return 'k:' + target.sessionKey
  }
  return 'i:' + (target.id || '')
}

// 已加载的会话身份键
let lastLoadedKey = targetKey()

/**
 * 空间id变化只同步，不重新加载，避免懒创建回填时清空刚发出的提问
 */
watch(() => props.target && props.target.chatSpaceId, (val) => {
  activeSpaceId.value = val || null
})

/**
 * 会话身份变化时重新初始化当前对话
 */
watch(() => [props.target && props.target.id, props.target && props.target.sessionKey], () => {
  const key = targetKey()
  if (key === lastLoadedKey) {
    return
  }
  lastLoadedKey = key
  activeSpaceId.value = (props.target && props.target.chatSpaceId) || null
  initChat()
})

onMounted(() => {
  initWebsocket()
  if (currentTargetId.value) {
    initChat()
  }
})

onBeforeUnmount(() => {
  destroyed = true
  if (websocket.value) {
    // 先清空回调再 close，避免异步 close 事件触发时 websocket.value 已为 null
    websocket.value.onclose = null
    websocket.value.onerror = null
    websocket.value.onmessage = null
    websocket.value.onopen = null
    websocket.value.close()
    websocket.value = null
  }
})

/**
 * 初始化当前对话
 */
function initChat() {
  resetHistory()
  ensureChatSpace()
}

/**
 * 确保聊天空间已创建，存在则加载历史消息
 */
function ensureChatSpace() {
  if (activeSpaceId.value) {
    loadChatMessageList()
    return
  }
  // 懒创建模式：空间由父组件在首条消息发出时创建
  if (props.spaceCreator) {
    return
  }
  const data = {
    senderId: userInfo.value.id,
    receiverId: props.target.id
  }
  createChatSpaceAPI(data).then(res => {
    if (res.code !== 200) {
      return
    }
    emit('space-created', {
      targetId: props.target.id,
      spaceId: res.data.spaceId,
      sessionKey: props.target.sessionKey
    })
  })
}

/**
 * 加载当前会话的首屏消息：未读消息 + 内容不足一屏时补一页种子，更早的历史由滚动到顶部时按页加载
 */
function loadChatMessageList() {
  loadUnread()
    .then(nextTick)
    .then(() => {
      // 只补一页，避免未滚动就把历史全部拉出来
      if (chatMessageList.value.length && isScrollable()) {
        return
      }
      return loadMore()
    })
    .then(scrollToBottom)
}

/**
 * 判断消息是否属于当前会话
 * 自己账号发出的消息按接收人判断（多端登录时服务端会回推），对方消息按发送人判断
 */
function isCurrentSpaceMsg(chatMsg) {
  if (!aiChat.value) {
    if (chatMsg.senderId === userInfo.value.id) {
      return currentTargetId.value === chatMsg.receiverId
    }
    return currentTargetId.value === chatMsg.senderId
  }
  if (!chatMsg.spaceId) {
    return false
  }
  return chatMsg.spaceId === activeSpaceId.value
}

/**
 * 输入框回车：回车直接发送，Shift + 回车换行
 * @param e 键盘事件
 */
function handleEnter(e) {
  // 中文输入法组词过程中的回车是「选词确认」，不能当作发送
  if (e.isComposing || e.keyCode === 229) {
    return
  }
  // Shift + 回车保留换行
  if (e.shiftKey) {
    return
  }
  e.preventDefault()
  sendMessage()
}

/**
 * 发送消息
 */
async function sendMessage() {
  const content = message.value.trim()
  if (!content) {
    return
  }
  // 会话创建中或回复期间不允许重复发送
  if (sending.value || replyLocked.value) {
    return
  }
  // 懒创建：首条消息发出前先创建空间
  if (!activeSpaceId.value) {
    if (!props.spaceCreator) {
      return
    }
    sending.value = true
    const spaceId = await props.spaceCreator()
    sending.value = false
    if (!spaceId) {
      return
    }
    activeSpaceId.value = spaceId
  }
  const chatMsg = {
    spaceId: activeSpaceId.value,
    senderId: userInfo.value.id,
    receiverId: props.target.id,
    message: content,
    createdDt: getCurrentDate()
  }
  const dataContent = { action: 2, chatMsg: chatMsg }
  websocket.value.send(JSON.stringify(dataContent))
  chatMessageList.value.push(chatMsg)
  // AI首响有延迟，本地先挂一个占位气泡，服务端首个思考/回复消息会复用它
  if (aiChat.value) {
    createReply({
      spaceId: activeSpaceId.value,
      senderId: props.target.id,
      receiverId: userInfo.value.id
    }, true)
  }
  scrollToBottom()
  message.value = ''
}

/**
 * 打开聊天记录
 */
function openChatMessage() {
  if (!activeSpaceId.value) {
    return
  }
  recordTitle.value = '与 ' + props.target.name + ' 的聊天记录'
  recordDrawer.value = true
}

/**
 * 复制消息内容到剪贴板
 */
function copyMessage(text) {
  if (!text) {
    return
  }
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text).then(() => {
      ElMessage.success('已复制')
    }).catch(() => {
      ElMessage.warning('复制失败 请手动选择文本复制')
    })
    return
  }
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.select()
  try {
    document.execCommand('copy')
    ElMessage.success('已复制')
  } catch {
    ElMessage.warning('复制失败 请手动选择文本复制')
  }
  document.body.removeChild(textarea)
}

/**
 * 初始化 websocket
 */
function initWebsocket() {
  if (!('WebSocket' in window)) {
    ElMessage.error('当前浏览器 不支持聊天通信协议')
    return
  }
  if (websocket.value) {
    websocket.value.close()
    websocket.value = null
  }
  websocket.value = new WebSocket(process.env.BASE_HTTP_API + '/ws/chat')
  websocket.value.onerror = setErrorMessage
  websocket.value.onopen = setOnopenMessage
  websocket.value.onmessage = setOnmessageMessage
  websocket.value.onclose = setOncloseMessage
}

/**
 * 连接错误时回调
 */
function setErrorMessage() {
  console.log('webSocket 连接发生错误 状态码：' + websocket.value.readyState)
  // 连接不可用，锁定输入框等待重连
  connected.value = false
}

/**
 * 连接成功时回调
 */
function setOnopenMessage() {
  console.log('webSocket 连接成功 状态码：' + websocket.value.readyState)
  // 连接可用，解锁输入框
  connected.value = true
  const chatMsg = { senderId: userInfo.value.id }
  const dataContent = { action: 1, chatMsg: chatMsg }
  websocket.value.send(JSON.stringify(dataContent))
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

// 是否正在回复中
const streaming = computed(() => !!ongoingReply())

// 回复期间锁定输入框，禁用并切换占位文案
const replyLocked = computed(() => props.lockWhileStreaming && streaming.value)

// 未选择聊天对象、连接不可用或回复中，均禁止输入
const inputDisabled = computed(() => !(props.target && props.target.id) || !connected.value || replyLocked.value)

// 连接不可用时优先提示重连，其次提示回复中
const inputPlaceholder = computed(() => {
  if (!connected.value) {
    return '连接服务器中...'
  }
  return replyLocked.value ? '正在回复中 请稍候' : props.placeholder
})

/**
 * 思考区状态文案，智能体区分思考中与思考完成，模型只有等待回复
 * @param chatMsg 消息
 * @returns 状态文案
 */
function thinkingLabel(chatMsg) {
  if (props.targetType !== 'agent') {
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
 * @param references RAG参考文档
 */
function handleReply(chatMsg, references) {
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
  if (references && references.length) {
    reply.references = references
  }
  scrollToBottom()
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

/**
 * 收到消息时回调
 */
function setOnmessageMessage(event) {
  const parse = JSON.parse(event.data)
  const chatMsg = parse.chatMsg
  // 自己账号发出的消息由其他端广播而来，不需要签收与已读
  const selfMsg = chatMsg.senderId === userInfo.value.id
  // 带id的是落库后的完整消息，推送到当前端即表示已送达，用户是否正在看这个会话不影响签收
  if (!selfMsg && chatMsg.id !== undefined) {
    const dataContent = { action: 3, chatMsg: chatMsg }
    websocket.value.send(JSON.stringify(dataContent))
  }
  // 仅处理当前会话的消息
  if (!isCurrentSpaceMsg(chatMsg)) {
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
    websocket.value.send(JSON.stringify({ action: 4, chatMsg: { id: chatMsg.id } }))
    // 通知父组件：收到对方消息，父组件用于刷新会话列表
    emit('message-received', chatMsg)
  }
  handleReply(chatMsg, parse.references)
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
 * 连接关闭时回调，组件未销毁时自动重连
 */
function setOncloseMessage() {
  console.log('webSocket 连接关闭 状态码：' + websocket.value.readyState)
  // 连接不可用，锁定输入框等待重连
  connected.value = false
  if (destroyed) {
    return
  }
  // 连接断开后本次回复不会再送达，收尾进行中的气泡
  finalizePendingReply()
  initWebsocket()
}
</script>

<style scoped lang="scss">
@use "@/components/Chat/chatMarkdown" as chatMd;

.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding-left: $spacing-md;
  overflow: hidden;
  box-sizing: border-box;
}

.chat-panel-header {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  font-size: 16px;
  margin-bottom: $spacing-md;
}

.chat-panel-header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.chat-panel-header-icon {
  font-size: 22px;
  position: relative;
  top: 2px;
}

.chat-panel-header-tip {
  font-size: 12px;
  color: $color-text-placeholder;
}

.chat-body {
  flex: 1;
  min-height: 0;
  background-color: $bg-page;
  border: 1px solid $color-primary;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-md;
  overflow: hidden;
}

.chat-tip {
  font-size: 12px;
  color: $color-text-placeholder;
}

.chat-history-loading {
  padding: $spacing-sm;
  text-align: center;
}

/* 未读起点分隔线：撑满消息行宽 */
.chat-unread-divider {
  align-self: stretch;
  margin: 0 0 $spacing-xs;
}

.chat-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  margin: $spacing-md $spacing-sm;
  align-items: flex-start;
  padding: 0 $spacing-sm;
}

.chat-item-self {
  align-items: flex-end;
  padding: 0 $spacing-sm;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.chat-item-self .chat-header {
  flex-direction: row-reverse;
}

.chat-avatar {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;

  .el-icon {
    font-size: 16px;
  }
}

.chat-avatar-ai {
  color: $color-text-white;
}

.chat-avatar-agent {
  background-color: $agent-theme-purple;
}

.chat-avatar-model {
  background-color: $agent-theme-cyan;
}

.chat-time {
  color: $color-text-placeholder;
  font-size: 12px;
}

.chat-bubble-wrap {
  position: relative;
}

.chat-item-self .chat-bubble-wrap {
  max-width: 80%;
}

.chat-bubble {
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-md;
  word-break: break-word;
}

.chat-bubble-toolbar {
  position: absolute;
  right: 0;
  top: 100%;
  padding-top: $spacing-xs;
  display: flex;
  gap: $spacing-xs;
  opacity: 0;
  pointer-events: none;
  transition: opacity $transition-fast;
}

.chat-bubble-wrap:hover .chat-bubble-toolbar {
  opacity: 1;
  pointer-events: auto;
}

.toolbar-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 25px;
  height: 25px;
  border-radius: $border-radius-sm;
  background-color: $bg-card;
  border: 1px solid $border-color;
  color: $color-text-secondary;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    color: $color-primary;
    border-color: $color-primary;
  }

  .el-icon {
    font-size: 16px;
  }
}

.chat-bubble-agent {
  background-color: $bg-card;
  color: $color-text-primary;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
}

.thinking-header {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.thinking-toggle {
  cursor: pointer;
}

.thinking-arrow {
  font-size: 12px;
  color: $color-text-placeholder;
  transition: transform $transition-fast;
}

.thinking-arrow-open {
  transform: rotate(90deg);
}

.thinking-label {
  font-size: 12px;
  color: $color-text-secondary;
}

.thinking-loading {
  font-size: 14px;
  color: $agent-theme-orange;
}

.thinking-content {
  margin-top: $spacing-xs;
  padding-top: $spacing-xs;
  border-top: 1px dashed $border-color;
  font-size: 12px;
  color: $color-text-secondary;
  line-height: $line-height-compact;
  white-space: pre-line;
}

.thinking-divider {
  height: 1px;
  background-color: $border-color-light;
  margin: $spacing-sm 0;
}

.chat-bubble-user {
  background-color: $color-primary;
  color: $color-text-white;
}

.chat-msg {
  font-size: 14px;
  font-weight: 500;
  overflow-wrap: break-word;
  line-height: $line-height-base;
  user-select: text;
}

.chat-footer {
  flex-shrink: 0;
}

::v-deep(.github-markdown-body) {
  @include chatMd.chat-markdown;
}
</style>

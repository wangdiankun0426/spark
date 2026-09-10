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
      </div>
    </div>

    <!-- 消息区 -->
    <el-scrollbar ref="scrollbarRef" class="chat-body" :class="{ 'chat-body-fill': fill }" always>
      <el-divider v-if="chatMsgs.length" border-style="dashed">
        <span class="chat-tip">以下是最新消息</span>
      </el-divider>
      <slot v-else name="empty">
        <el-divider border-style="dashed">
          <span class="chat-tip">暂无最新消息</span>
        </el-divider>
      </slot>
      <div
          v-for="(chatMsg, idx) in chatMsgs"
          :key="idx"
          class="chat-item"
          :class="{ 'chat-item-self': chatMsg.senderId === userInfo.id }"
      >
        <!-- 头像 + 时间 -->
        <div class="chat-header">
          <!-- 自己发出的消息：使用用户头像 -->
          <user-avatar
              v-if="chatMsg.senderId === userInfo.id"
              :user-id="chatMsg.senderId"
              :size="28"
          />
          <!-- 对方是用户：展示对方用户头像 -->
          <user-avatar
              v-else-if="targetType === 'user'"
              :user-id="chatMsg.senderId"
              :size="28"
          />
          <!-- 对方是智能体/模型：根据 targetType 显示固定 icon -->
          <div v-else class="chat-avatar chat-avatar-ai" :class="'chat-avatar-' + targetType">
            <el-icon>
              <MagicStick v-if="targetType === 'agent'" />
              <Cpu v-else />
            </el-icon>
          </div>
          <span class="chat-time">{{ chatMsg.createdDt }}</span>
        </div>
        <!-- 消息内容 -->
        <!-- 自己发出的消息：外层 wrap 包裹工具条与气泡，保证鼠标在两者间移动时 hover 不中断 -->
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
            <!-- 扩展位：后续工具按钮在此追加 -->
          </div>
          <div class="chat-bubble chat-bubble-user">
            <div class="chat-msg">{{ chatMsg.message }}</div>
          </div>
        </div>
        <!-- 对方消息 -->
        <div v-else class="chat-bubble chat-bubble-agent" :class="{ 'chat-bubble-thinking': chatMsg.thinkingContent }">
          <!-- 思考内容 -->
          <template v-if="chatMsg.thinkingContent">
            <div class="thinking-header">
              <el-icon v-if="!chatMsg.thinkingDone" class="thinking-loading"><Loading /></el-icon>
              <span class="thinking-label">{{ chatMsg.thinkingDone ? '思考完成' : '思考中...' }}</span>
            </div>
            <div class="thinking-content">{{ chatMsg.thinkingContent }}</div>
          </template>
          <!-- 正式内容 -->
          <template v-if="chatMsg.message">
            <div v-if="chatMsg.thinkingContent" class="thinking-divider"></div>
            <v-md-preview class="chat-msg" :text="chatMsg.message || ''" />
          </template>
          <!-- 引用 -->
          <llm-references
              v-if="targetType === 'agent' && chatMsg.references && chatMsg.references.length"
              :references="chatMsg.references"
          />
        </div>
      </div>
    </el-scrollbar>

    <!-- 输入区：回车发送、Shift + 回车换行，不再放发送按钮 -->
    <div class="chat-footer">
      <el-input
          v-model="message"
          :rows="4"
          type="textarea"
          :maxlength="maxLength"
          show-word-limit
          :placeholder="placeholder"
          :disabled="!target || !target.id"
          @keydown.enter="handleEnter"
      />
      <div class="chat-toolbar">
        <el-button
            v-if="activeSpaceId"
            link
            type="primary"
            @click="openChatMessage"
        >聊天记录</el-button>
      </div>
    </div>

    <!-- 聊天记录 -->
    <chat-message
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
import { MagicStick, Cpu, ChatDotRound, CopyDocument, Loading } from '@element-plus/icons-vue'
import { createChatSpaceAPI } from '@/api/chat/space.js'
import { getNoReadMsgListAPI, getMsgListAPI } from '@/api/chat/msg.js'
import { getCurrentDate } from '@/utils/dateUtil.js'
import UserAvatar from '@/components/UserAvatar/index.vue'
import ChatMessage from '@/components/Chat/chatMessage.vue'
import LlmReferences from '@/components/Chat/llmReferences.vue'

// 流式回复看门狗超时时间，超时后解除发送锁定
const STREAM_WATCHDOG_TIMEOUT = 1000 * 120

const props = defineProps({
  // 聊天对象 { id, name, chatSpaceId, sessionKey }
  target: { type: Object, default: () => ({}) },
  // 对方类型：agent=智能体，model=模型，user=用户
  targetType: { type: String, default: 'agent' },
  // 历史消息加载模式：unread=仅未读，all=全量
  historyMode: { type: String, default: 'unread' },
  // 多会话场景下按 spaceId 过滤消息，避免同一对象的不同会话串消息
  filterBySpaceId: { type: Boolean, default: false },
  // 懒创建会话：非空时由父组件负责创建空间并返回 spaceId
  spaceCreator: { type: Function, default: null },
  // 消息区高度自适应父容器
  fill: { type: Boolean, default: false },
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
const message = ref('')
const chatMsgs = ref([])
const scrollbarRef = ref(null)
const recordDrawer = ref(false)
const recordTitle = ref('')
// 当前会话空间id，懒创建场景下由父组件创建后回填
const activeSpaceId = ref((props.target && props.target.chatSpaceId) || null)
const sending = ref(false)
const streaming = ref(false)

// 流式回复看门狗定时器
let streamWatchdog = null
// 组件是否已销毁，避免销毁后触发 onclose 自动重连
let destroyed = false

const currentTargetId = computed(() => props.target && props.target.id)

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
  if (streamWatchdog) {
    clearTimeout(streamWatchdog)
    streamWatchdog = null
  }
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
  chatMsgs.value = []
  ensureChatSpace()
}

/**
 * 确保聊天空间已创建，存在则加载历史消息
 */
function ensureChatSpace() {
  if (activeSpaceId.value) {
    loadMsgs()
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
 * 加载当前空间的历史消息
 */
function loadMsgs() {
  const query = { spaceId: activeSpaceId.value }
  const api = props.historyMode === 'all' ? getMsgListAPI : getNoReadMsgListAPI
  api(query).then(res => {
    if (res.code !== 200) {
      return
    }
    if (res.data !== null) {
      chatMsgs.value = res.data
    }
    scrollToBottom()
  })
}

/**
 * 判断消息是否属于当前会话
 */
function isCurrentSpaceMsg(chatMsg) {
  if (!props.filterBySpaceId) {
    return currentTargetId.value === chatMsg.senderId
  }
  if (!chatMsg.spaceId) {
    return false
  }
  return chatMsg.spaceId === activeSpaceId.value
}

/**
 * 滚动到底部
 */
function scrollToBottom() {
  nextTick(() => {
    const scrollbar = scrollbarRef.value
    if (scrollbar) {
      scrollbar.update()
      scrollbar.scrollTo({ top: scrollbar.wrapRef.scrollHeight, behavior: 'smooth' })
    }
  })
}

/**
 * 启动流式回复看门狗，超时后自动解除发送锁定
 */
function startStreamWatchdog() {
  streaming.value = true
  if (streamWatchdog) {
    clearTimeout(streamWatchdog)
  }
  streamWatchdog = setTimeout(() => {
    streaming.value = false
    streamWatchdog = null
  }, STREAM_WATCHDOG_TIMEOUT)
}

/**
 * 结束流式回复看门狗
 */
function stopStreamWatchdog() {
  streaming.value = false
  if (streamWatchdog) {
    clearTimeout(streamWatchdog)
    streamWatchdog = null
  }
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
  if (!message.value) {
    return
  }
  if (sending.value) {
    return
  }
  if (props.lockWhileStreaming && streaming.value) {
    ElMessage.warning('正在回复中 请稍候')
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
    message: message.value,
    createdDt: getCurrentDate()
  }
  const dataContent = { action: 2, chatMsg: chatMsg }
  websocket.value.send(JSON.stringify(dataContent))
  chatMsgs.value.push(chatMsg)
  scrollToBottom()
  message.value = ''
  if (props.lockWhileStreaming) {
    startStreamWatchdog()
  }
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
}

/**
 * 连接成功时回调
 */
function setOnopenMessage() {
  console.log('webSocket 连接成功 状态码：' + websocket.value.readyState)
  const chatMsg = { senderId: userInfo.value.id }
  const dataContent = { action: 1, chatMsg: chatMsg }
  websocket.value.send(JSON.stringify(dataContent))
}

/**
 * 处理 agent 思考中消息（action === 6）
 */
function handleThinkingMessage(chatMsg) {
  // 仅处理当前会话的思考消息
  if (!isCurrentSpaceMsg(chatMsg)) {
    return
  }
  // 流式追加：若上一条消息有 thinkingContent（是同一次回复的思考消息）
  if (chatMsgs.value.length > 0) {
    let lastChatMsg = chatMsgs.value[chatMsgs.value.length - 1]
    if (lastChatMsg.thinkingContent !== undefined && !lastChatMsg.hasReply) {
      lastChatMsg.thinkingContent = (lastChatMsg.thinkingContent || '') + (chatMsg.message || '')
      scrollToBottom()
      return
    }
  }
  // 新建消息，包含 thinkingContent 字段，正式内容后续填入
  const newMsg = {
    ...chatMsg,
    thinkingContent: chatMsg.message,
    message: '', // 正式内容后续填这里
    hasReply: false
  }
  chatMsgs.value.push(newMsg)
  scrollToBottom()
}

/**
 * 收到消息时回调
 */
function setOnmessageMessage(event) {
  let parse = JSON.parse(event.data)
  let chatMsg = parse.chatMsg
  let references = parse.references
  // 处理 agent 思考中消息（action === 6）
  if (parse.action === 6) {
    handleThinkingMessage(chatMsg)
    return
  }
  if (chatMsg.id !== undefined) {
    const dataContent = { action: 3, chatMsg: chatMsg }
    websocket.value.send(JSON.stringify(dataContent))
  }
  // 通知父组件：收到对方消息，父组件用于更新用户列表未读数
  emit('message-received', chatMsg)
  // 仅处理当前会话的消息
  if (!isCurrentSpaceMsg(chatMsg)) {
    return
  }
  // 收到带id的完整消息说明本次回复结束，解除发送锁定
  if (chatMsg.id !== undefined) {
    stopStreamWatchdog()
  }
  // 检查上一条是否为思考消息（同一次回复）
  if (chatMsgs.value.length > 0) {
    let lastChatMsg = chatMsgs.value[chatMsgs.value.length - 1]
    // 只要上一条有 thinkingContent，就把正式内容合并进去
    if (lastChatMsg.thinkingContent !== undefined && lastChatMsg.senderId === chatMsg.senderId) {
      lastChatMsg.thinkingDone = true
      lastChatMsg.message = (lastChatMsg.message || '') + (chatMsg.message || '')
      if (chatMsg.id !== undefined) {
        lastChatMsg.id = chatMsg.id
      }
      if (references) {
        lastChatMsg.references = references
      }
      if (chatMsg.id !== undefined) {
        const dataContent = { action: 4, chatMsg: chatMsg }
        websocket.value.send(JSON.stringify(dataContent))
      }
      scrollToBottom()
      return
    }
    // 判断上一条是否为对方的流式临时消息（无 id 且非自己发送）
    if (lastChatMsg.id === undefined && lastChatMsg.senderId !== userInfo.value.id && lastChatMsg.thinkingContent === undefined) {
      if (chatMsg.id === undefined) {
        // 当前是流式 chunk，追加到临时消息
        lastChatMsg.message = (lastChatMsg.message || '') + (chatMsg.message || '')
        scrollToBottom()
        return
      }
      // 当前是完整消息，补全 id；message 非空则覆盖流式累积内容
      lastChatMsg.id = chatMsg.id
      if (chatMsg.message) {
        lastChatMsg.message = chatMsg.message
      }
      if (references) {
        lastChatMsg.references = references
      }
      const dataContent = { action: 4, chatMsg: chatMsg }
      websocket.value.send(JSON.stringify(dataContent))
      scrollToBottom()
      return
    }
  }
  // 否则作为新消息追加
  chatMsgs.value.push(chatMsg)
  if (chatMsg.id !== undefined) {
    const dataContent = { action: 4, chatMsg: chatMsg }
    websocket.value.send(JSON.stringify(dataContent))
  }
  scrollToBottom()
}

/**
 * 连接关闭时回调，组件未销毁时自动重连
 */
function setOncloseMessage() {
  console.log('webSocket 连接关闭 状态码：' + websocket.value.readyState)
  if (destroyed) {
    return
  }
  initWebsocket()
}
</script>

<style scoped lang="scss">
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
  height: calc(100vh - 274px);
  border: 1px solid $color-primary;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-md;
  overflow: hidden;
}

.chat-body-fill {
  height: auto;
  flex: 1;
  min-height: 0;
}

.chat-tip {
  font-size: 12px;
  color: $color-text-placeholder;
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
  // 用 padding 代替 margin：padding 属于工具条自身，鼠标经过间距区域时 wrap 仍保持 hover
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
  width: 24px;
  height: 24px;
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
    font-size: 14px;
  }
}

.chat-bubble-agent {
  background-color: $color-primary-light;
  color: $color-text-primary;
}

.chat-bubble-thinking {
  border-left: 3px solid $agent-theme-orange;
}

.thinking-header {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  margin-bottom: $spacing-xs;
  padding-bottom: $spacing-xs;
  border-bottom: 1px dashed $border-color;
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
  font-size: 12px;
  color: $color-text-secondary;
  line-height: $line-height-compact;
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
  overflow-wrap: break-word;
  line-height: $line-height-base;
  user-select: text;
}

.chat-footer {
  flex-shrink: 0;
}

.chat-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: $spacing-md;
  margin-top: $spacing-sm;
}

::v-deep(.github-markdown-body) {
  padding: 0;
}
</style>

<template>
  <div class="app-container agent-container">
    <!-- 主体：左侧会话记录 + 右侧聊天区 -->
    <div class="agent-body">
      <!-- 左侧会话记录 -->
      <session-pane
          ref="sessionPaneRef"
          :space-type="CHAT_SPACE_TYPE_AGENT"
          :new-disabled="!agentList.length"
          :active-space-id="currentSpaceId"
          @select="handleSelectSession"
          @new="handleNewSession"
          @loaded="handleSessionsLoaded"
      />

      <!-- 右侧聊天区 -->
      <div class="chat-pane">
        <index
            :target="currentTarget"
            target-type="agent"
            :lock-while-streaming="true"
            :max-length="2000"
            placeholder="给智能体发送消息，回车发送，Shift + 回车换行"
            :space-creator="createSpaceForChat"
            @message-received="handleMessageReceived"
        >
          <template #header-left>
            <el-icon class="chat-panel-title-icon"><MagicStick /></el-icon>
            <span class="chat-panel-title">{{ currentTitle }}</span>
          </template>
          <template #header-right>
            <el-select
                v-model="selectedAgentId"
                placeholder="请选择智能体"
                class="agent-select"
                :disabled="!agentList.length"
                @change="handleAgentChange"
            >
              <el-option
                  v-for="agent in agentList"
                  :key="agent.id"
                  :label="agent.name"
                  :value="agent.id"
              />
            </el-select>
          </template>
          <template #empty>
            <div class="chat-welcome">
              <el-icon class="chat-welcome-icon"><MagicStick /></el-icon>
              <div class="chat-welcome-title">有什么可以帮你的？</div>
              <div class="chat-welcome-subtitle">选择智能体后输入问题，即可开始对话</div>
            </div>
          </template>
        </index>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import { pageAgentListAPI } from '@/api/llm/agent.js'
import { createAiChatSpaceAPI } from '@/api/chat/space.js'
import SessionPane from '@/components/Chat/sessionPane.vue'
import Index from '@/components/Chat/index.vue'

// 会话列表刷新延迟，等待服务端回填会话标题
const SESSION_REFRESH_DELAY = 800
// 会话类型，对应后端 ChatSpaceTypeEnum 的取值
const CHAT_SPACE_TYPE_AGENT = 3

const agentList = ref([])
const selectedAgentId = ref(null)
const currentTarget = ref({})
// 会话记录面板
const sessionPaneRef = ref(null)
// 当前对话区标题
const currentTitle = ref('新对话')

// 草稿会话序号，用于生成稳定的会话身份键
let draftSeq = 0
let refreshTimer = null

/**
 * 当前会话空间id，草稿态为空
 */
const currentSpaceId = computed(() => currentTarget.value.chatSpaceId || null)

onMounted(() => {
  loadAgents()
})

onBeforeUnmount(() => {
  if (refreshTimer) {
    clearTimeout(refreshTimer)
  }
})

/**
 * 加载可用智能体，默认选中第一个，随后加载会话记录
 */
function loadAgents() {
  const query = { status: 1, page: false }
  pageAgentListAPI(query).then(res => {
    if (res.code !== 200) {
      return
    }
    agentList.value = res.data.rows || []
    if (agentList.value.length) {
      selectedAgentId.value = agentList.value[0].id
    } else {
      ElMessage.warning('暂无可用智能体 请先配置智能体')
    }
    sessionPaneRef.value.loadFirst()
  })
}

/**
 * 会话列表加载完成后同步当前会话标题
 * @param list 会话列表
 */
function handleSessionsLoaded(list) {
  const matched = list.find(item => item.spaceId === currentSpaceId.value)
  currentTitle.value = matched ? (matched.title || '新对话') : '新对话'
}

/**
 * 获取当前选中的智能体
 */
function getCurrentAgent() {
  return agentList.value.find(item => item.id === selectedAgentId.value)
}

/**
 * 开启草稿会话，不落库，发出首条消息时再创建空间
 */
function startDraftSession() {
  draftSeq += 1
  const agent = getCurrentAgent()
  currentTitle.value = '新对话'
  currentTarget.value = {
    id: selectedAgentId.value,
    name: agent ? agent.name : '智能体',
    chatSpaceId: null,
    sessionKey: 'draft-' + draftSeq
  }
}

/**
 * 新建对话
 */
function handleNewSession() {
  startDraftSession()
}

/**
 * 切换会话
 */
function handleSelectSession(session) {
  selectedAgentId.value = session.receiverId
  const agent = getCurrentAgent()
  currentTitle.value = session.title || '新对话'
  currentTarget.value = {
    id: session.receiverId,
    name: agent ? agent.name : session.receiverName,
    chatSpaceId: session.spaceId,
    sessionKey: session.spaceId
  }
}

/**
 * 懒创建会话空间，返回空间id
 */
function createSpaceForChat() {
  const agentId = currentTarget.value.id || selectedAgentId.value
  const data = { receiverId: agentId }
  return createAiChatSpaceAPI(data).then(res => {
    if (res.code !== 200) {
      return null
    }
    const spaceId = res.data.spaceId
    currentTarget.value.chatSpaceId = spaceId
    // 延迟刷新列表，等待服务端回填会话标题
    scheduleRefresh()
    return spaceId
  })
}

/**
 * 防抖刷新会话列表
 */
function scheduleRefresh() {
  if (refreshTimer) {
    clearTimeout(refreshTimer)
  }
  refreshTimer = setTimeout(() => {
    sessionPaneRef.value.refresh()
  }, SESSION_REFRESH_DELAY)
}

/**
 * 收到对方消息时刷新会话列表，同步最新的标题与最近消息
 */
function handleMessageReceived(chatMsg) {
  if (chatMsg.id === undefined) {
    return
  }
  scheduleRefresh()
}

/**
 * 切换智能体，草稿态仅切换后续使用的智能体，已有会话则开启新会话
 */
function handleAgentChange(agentId) {
  if (!currentSpaceId.value) {
    const agent = getCurrentAgent()
    currentTarget.value.id = agentId
    currentTarget.value.name = agent ? agent.name : '智能体'
    return
  }
  startDraftSession()
  ElMessage.info('已切换到新会话')
}
</script>

<style scoped lang="scss">
.agent-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 70px);
}

.agent-body {
  display: flex;
  align-items: stretch;
  gap: $spacing-lg;
  flex: 1;
  min-height: 0;
}

.chat-pane {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: $spacing-md;
  background-color: $bg-card;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.agent-select {
  width: 200px;
}

.chat-panel-title-icon {
  font-size: 20px;
  color: $agent-theme-purple;
  position: relative;
  top: 2px;
}

.chat-panel-title {
  margin-left: $spacing-xs;
  font-size: 16px;
  font-weight: 600;
  color: $color-text-primary;
}

.chat-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  min-height: 360px;
  padding: $spacing-xl 0;
}

.chat-welcome-icon {
  font-size: 56px;
  color: $agent-theme-purple;
  opacity: 0.85;
}

.chat-welcome-title {
  font-size: 18px;
  font-weight: 600;
  color: $color-text-primary;
}

.chat-welcome-subtitle {
  font-size: 13px;
  color: $color-text-secondary;
}
</style>

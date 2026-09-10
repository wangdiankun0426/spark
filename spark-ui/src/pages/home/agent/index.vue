<template>
  <div class="app-container agent-container">
    <!-- 主体：左侧会话记录 + 右侧聊天区 -->
    <div class="agent-body">
      <!-- 左侧会话列表 -->
      <div class="session-pane">
        <el-button
            style="height: 40px"
            :disabled="!agentList.length"
            @click="handleNewSession"
        >
          <el-icon><Plus /></el-icon>开启新对话
        </el-button>
        <div class="session-pane-title-row">
          <div class="session-pane-title">会话记录</div>
          <span class="session-pane-count">{{ sessions.length }} 条</span>
        </div>
        <el-scrollbar v-if="sessions.length" class="session-list" always>
          <div
              v-for="session in sessions"
              :key="session.spaceId"
              class="session-item"
              :class="{ 'session-item-active': session.spaceId === currentSpaceId }"
              @click="handleSelectSession(session)"
          >
            <div class="session-item-icon">
              <el-icon><MagicStick /></el-icon>
            </div>
            <div class="session-item-info">
              <div class="session-item-title">{{ session.title || '新对话' }}</div>
              <div class="session-item-meta">
                <span>{{ session.modelName }}</span>
                <span v-if="session.lastTime">{{ formatChatTime(session.lastTime) }}</span>
              </div>
            </div>
            <div class="session-item-actions" @click.stop>
              <el-dropdown
                  trigger="click"
                  placement="bottom-end"
                  @command="command => handleSessionCommand(command, session)"
              >
                <span class="s-more">
                  <el-icon><MoreFilled /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="rename">重命名</el-dropdown-item>
                    <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-scrollbar>
        <el-empty v-else class="session-empty" description="暂无会话记录" :image-size="90" />
      </div>

      <!-- 右侧聊天区 -->
      <div class="chat-pane">
        <chat-panel
            :target="currentTarget"
            target-type="agent"
            history-mode="all"
            filter-by-space-id
            :fill="true"
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
        </chat-panel>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick, MoreFilled } from '@element-plus/icons-vue'
import { pageAgentListAPI } from '@/api/llm/agent.js'
import {
  createAiChatSpaceAPI,
  getMyAiChatSpaceListAPI,
  updateAiChatSpaceTitleAPI,
  deleteAiChatSpaceAPI
} from '@/api/chat/space.js'
import { formatChatTime } from '@/utils/dateUtil.js'
import ChatPanel from '@/components/Chat/chatPanel.vue'

// 会话列表刷新延迟，等待服务端回填会话标题
const SESSION_REFRESH_DELAY = 800
// 会话类型，对应后端 ChatSpaceTypeEnum 的取值
const CHAT_SPACE_TYPE_AGENT = 3

const agentList = ref([])
const selectedAgentId = ref(null)
const sessions = ref([])
const currentTarget = ref({})

// 草稿会话序号，用于生成稳定的会话身份键
let draftSeq = 0
let refreshTimer = null

/**
 * 当前会话空间id，草稿态为空
 */
const currentSpaceId = computed(() => currentTarget.value.chatSpaceId || null)

/**
 * 当前对话区标题
 */
const currentTitle = computed(() => {
  const spaceId = currentSpaceId.value
  if (!spaceId) {
    return '新对话'
  }
  const matched = sessions.value.find(item => item.spaceId === spaceId)
  if (!matched) {
    return '新对话'
  }
  return matched.title || '新对话'
})

onMounted(() => {
  loadAgents()
})

onBeforeUnmount(() => {
  if (refreshTimer) {
    clearTimeout(refreshTimer)
  }
})

/**
 * 加载可用智能体，默认选中第一个
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
    loadSessions(true)
  })
}

/**
 * 加载我的AI会话列表
 * @param selectFirst 是否自动选中第一条会话
 */
function loadSessions(selectFirst) {
  getMyAiChatSpaceListAPI({ spaceType: CHAT_SPACE_TYPE_AGENT }).then(res => {
    if (res.code !== 200) {
      return
    }
    sessions.value = res.data || []
    if (!selectFirst) {
      return
    }
    if (sessions.value.length) {
      handleSelectSession(sessions.value[0])
    } else {
      startDraftSession()
    }
  })
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
  if (!agentList.value.length) {
    ElMessage.warning('暂无可用智能体')
    return
  }
  startDraftSession()
}

/**
 * 切换会话
 */
function handleSelectSession(session) {
  selectedAgentId.value = session.receiverId
  const agent = getCurrentAgent()
  currentTarget.value = {
    id: session.receiverId,
    name: agent ? agent.name : session.modelName,
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
    loadSessions(false)
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

/**
 * 处理会话操作菜单命令
 * @param command 命令标识
 * @param session 会话
 */
function handleSessionCommand(command, session) {
  if (command === 'rename') {
    handleRenameSession(session)
    return
  }
  if (command === 'delete') {
    handleDeleteSession(session)
  }
}

/**
 * 重命名会话
 */
function handleRenameSession(session) {
  ElMessageBox.prompt('请输入新的会话名称', '重命名', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: session.title || '',
    inputValidator: (value) => {
      if (!value || !value.trim()) {
        return '会话名称不能为空'
      }
      if (value.trim().length > 50) {
        return '会话名称不能超过 50 个字'
      }
      return true
    }
  }).then(({ value }) => {
    const title = value.trim()
    updateAiChatSpaceTitleAPI({ spaceId: session.spaceId, title }).then(res => {
      if (res.code !== 200) {
        return
      }
      ElMessage.success('重命名成功')
      session.title = title
    })
  }).catch(() => {})
}

/**
 * 删除会话
 */
function handleDeleteSession(session) {
  ElMessageBox.confirm('是否确定删除此条会话记录?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteAiChatSpaceAPI({ spaceId: session.spaceId }).then(res => {
      if (res.code !== 200) {
        return
      }
      ElMessage.success('删除会话成功')
      const isCurrent = session.spaceId === currentSpaceId.value
      const index = sessions.value.findIndex(item => item.spaceId === session.spaceId)
      if (index > -1) {
        sessions.value.splice(index, 1)
      }
      if (!isCurrent) {
        return
      }
      if (sessions.value.length) {
        handleSelectSession(sessions.value[0])
      } else {
        startDraftSession()
      }
    })
  }).catch(() => {})
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

.session-pane {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: $spacing-md;
  background-color: $bg-card;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.session-pane-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-sm;
  flex-shrink: 0;
  padding: $spacing-xs $spacing-sm $spacing-sm;
}

.session-pane-title {
  font-size: 13px;
  font-weight: 600;
  color: $color-text-secondary;
}

.session-pane-count {
  font-size: 11px;
  color: $color-text-placeholder;
}

.session-list {
  flex: 1;
  min-height: 0;
}

.session-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm;
  border: 1px solid transparent;
  border-radius: $border-radius-md;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    background-color: $color-primary-soft;
  }

  &.session-item-active {
    background-color: $color-primary-light;
    border-color: $color-primary;
  }
}

.session-item-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: $color-text-white;
  background-color: $agent-theme-purple;
  border-radius: $border-radius-md;
}

.session-item-info {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.session-item-title {
  font-size: 13px;
  font-weight: 600;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-item-meta {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  font-size: 11px;
  color: $color-text-placeholder;
}

.session-item-actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  opacity: 0;
  transition: $transition-fast;
}

.session-item:hover .session-item-actions {
  opacity: 1;
}

.s-more {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  color: $color-text-secondary;
  border-radius: $border-radius-sm;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    color: $color-primary;
    background-color: $color-primary-light;
  }

  .el-icon {
    font-size: 16px;
  }
}

.session-empty {
  flex: 1;
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

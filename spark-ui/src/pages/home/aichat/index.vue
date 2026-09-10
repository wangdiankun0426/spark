<template>
  <div class="app-container aichat-container">
    <!-- 主体：左侧会话记录 + 右侧聊天区 -->
    <div class="aichat-body">
      <!-- 左侧会话列表 -->
      <div class="session-pane">
        <el-button
            style="height: 40px"
            :disabled="!modelList.length"
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
              <el-icon><Cpu /></el-icon>
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
            target-type="model"
            history-mode="all"
            filter-by-space-id
            :fill="true"
            :lock-while-streaming="true"
            :max-length="2000"
            placeholder="给 AI 发送消息，回车发送，Shift + 回车换行"
            :space-creator="createSpaceForChat"
            @message-received="handleMessageReceived"
        >
          <template #header-left>
            <el-icon class="chat-panel-title-icon"><MagicStick /></el-icon>
            <span class="chat-panel-title">{{ currentTitle }}</span>
          </template>
          <template #header-right>
            <el-select
                v-model="selectedModelId"
                placeholder="请选择语言模型"
                class="model-select"
                :disabled="!modelList.length"
                @change="handleModelChange"
            >
              <el-option
                  v-for="model in modelList"
                  :key="model.id"
                  :label="model.name"
                  :value="model.id"
              />
            </el-select>
          </template>
          <template #empty>
            <div class="chat-welcome">
              <el-icon class="chat-welcome-icon"><MagicStick /></el-icon>
              <div class="chat-welcome-title">有什么可以帮你的？</div>
              <div class="chat-welcome-subtitle">选择语言模型后输入问题，即可开始对话</div>
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
import { Plus, Cpu, MagicStick, MoreFilled } from '@element-plus/icons-vue'
import { pageModelListAPI } from '@/api/llm/model.js'
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
const CHAT_SPACE_TYPE_MODEL = 2

const modelList = ref([])
const selectedModelId = ref(null)
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
  loadModels()
})

onBeforeUnmount(() => {
  if (refreshTimer) {
    clearTimeout(refreshTimer)
  }
})

/**
 * 加载可用语言模型，默认选中第一个
 */
function loadModels() {
  const query = { type: 1, status: 1, page: false }
  pageModelListAPI(query).then(res => {
    if (res.code !== 200) {
      return
    }
    modelList.value = res.data.rows || []
    if (modelList.value.length) {
      selectedModelId.value = modelList.value[0].id
    } else {
      ElMessage.warning('暂无可用语言模型 请先配置模型')
    }
    loadSessions(true)
  })
}

/**
 * 加载我的AI会话列表
 * @param selectFirst 是否自动选中第一条会话
 */
function loadSessions(selectFirst) {
  getMyAiChatSpaceListAPI({ spaceType: CHAT_SPACE_TYPE_MODEL }).then(res => {
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
 * 获取当前选中的模型
 */
function getCurrentModel() {
  return modelList.value.find(item => item.id === selectedModelId.value)
}

/**
 * 开启草稿会话，不落库，发出首条消息时再创建空间
 */
function startDraftSession() {
  draftSeq += 1
  const model = getCurrentModel()
  currentTarget.value = {
    id: selectedModelId.value,
    name: model ? model.name : 'AI 助手',
    chatSpaceId: null,
    sessionKey: 'draft-' + draftSeq
  }
}

/**
 * 新建对话
 */
function handleNewSession() {
  if (!modelList.value.length) {
    ElMessage.warning('暂无可用语言模型')
    return
  }
  startDraftSession()
}

/**
 * 切换会话
 */
function handleSelectSession(session) {
  selectedModelId.value = session.receiverId
  const model = getCurrentModel()
  currentTarget.value = {
    id: session.receiverId,
    name: model ? model.name : session.modelName,
    chatSpaceId: session.spaceId,
    sessionKey: session.spaceId
  }
}

/**
 * 懒创建会话空间，返回空间id
 */
function createSpaceForChat() {
  const modelId = currentTarget.value.id || selectedModelId.value
  const data = { receiverId: modelId }
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
 * 切换模型，草稿态仅切换后续使用的模型，已有会话则开启新会话
 */
function handleModelChange(modelId) {
  if (!currentSpaceId.value) {
    const model = getCurrentModel()
    currentTarget.value.id = modelId
    currentTarget.value.name = model ? model.name : 'AI 助手'
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
.aichat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 70px);
}

.aichat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  margin-bottom: $spacing-lg;
  padding: $spacing-md $spacing-xl;
  background-color: $bg-card;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.aichat-body {
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
  background-color: $color-primary;
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

.model-select {
  width: 200px;
}

.chat-panel-title-icon {
  font-size: 20px;
  color: $color-primary;
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
  color: $color-primary;
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

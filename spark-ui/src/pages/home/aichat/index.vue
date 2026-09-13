<template>
  <div class="app-container aichat-container">
    <div class="aichat-body">
      <!-- 左侧会话记录 -->
      <session-pane
          ref="sessionPaneRef"
          :space-type="CHAT_SPACE_TYPE_MODEL"
          :new-disabled="!modelList.length"
          :active-space-id="currentSpaceId"
          @select="handleSelectSession"
          @new="handleNewSession"
          @loaded="handleSessionsLoaded"
      />

      <!-- 右侧聊天区 -->
      <div class="chat-pane">
        <chat
            :target="currentTarget"
            target-type="model"
            :lock-while-streaming="true"
            :max-length="2000"
            placeholder="给 AI 发送消息，回车发送，Shift + 回车换行"
            :space-creator="createSpaceForChat"
            @message-received="handleMessageReceived"
        >
          <template #header-left>
            <el-icon class="chat-panel-title-icon"><Aichat /></el-icon>
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
              <el-icon class="chat-welcome-icon"><Aichat /></el-icon>
              <div class="chat-welcome-title">有什么可以帮你的？</div>
              <div class="chat-welcome-subtitle">点击右上角选择模型后输入问题，即可开始对话</div>
            </div>
          </template>
        </chat>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { pageModelListAPI } from '@/api/llm/model.js'
import { createAiChatSpaceAPI } from '@/api/chat/space.js'
import SessionPane from '@/components/Chat/sessionPane.vue'
import Chat from '@/components/Chat/index.vue'
import Aichat from "@/assets/icons/aichat.vue";

// 会话列表刷新延迟，等待服务端回填会话标题
const SESSION_REFRESH_DELAY = 800
// 会话类型，对应后端 ChatSpaceTypeEnum 的取值
const CHAT_SPACE_TYPE_MODEL = 2

const modelList = ref([])
const selectedModelId = ref(null)
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
  loadModels()
})

/**
 * 会话列表加载完成后同步当前会话标题
 * @param list 会话列表
 */
function handleSessionsLoaded(list) {
  const matched = list.find(item => item.spaceId === currentSpaceId.value)
  currentTitle.value = matched ? (matched.title || '新对话') : '新对话'
}

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
      return;
    }
    sessionPaneRef.value.loadFirst()
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
  currentTitle.value = '新对话'
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
  startDraftSession()
}

/**
 * 切换会话
 */
function handleSelectSession(session) {
  selectedModelId.value = session.receiverId
  const model = getCurrentModel()
  currentTitle.value = session.title || '新对话'
  currentTarget.value = {
    id: session.receiverId,
    name: model ? model.name : session.receiverName,
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
  font-size: 24px;
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
  font-size: 80px;
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

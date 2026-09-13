<template>
  <el-drawer
      v-model="visible"
      :title="title"
      direction="ltr"
      size="80%"
      append-to-body
      class="chat-record-drawer"
      :close-on-click-modal="false"
  >
    <!-- 检索区 -->
    <div class="record-search">
      <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          clearable
          @change="handleSearch"
      />
    </div>
    <el-scrollbar
        ref="scrollbarRef"
        class="record-body"
        always
        @scroll="onScroll"
    >
      <div v-if="historyLoading" class="record-loading">正在加载历史消息...</div>
      <div class="chat-list">
        <div
            v-for="(chatMsg, idx) in chatHisMsgs"
            :key="idx"
            class="chat-item"
            :class="{ 'chat-item-self': chatMsg.senderId === userInfo.id }"
        >
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
            <span class="record-time">{{ chatMsg.createdDt }}</span>
          </div>
          <!-- 消息内容 -->
          <div
              class="chat-bubble"
              :class="[chatMsg.senderId === userInfo.id ? 'chat-bubble-user' : 'chat-bubble-agent']"
          >
            <!-- 消息内容统一走Markdown -->
            <v-md-preview
                class="chat-msg"
                :text="chatMsg.message || ''"
            />
            <llm-references
                v-if="targetType === 'agent' && chatMsg.references && chatMsg.references.length"
                :references="chatMsg.references"
            />
          </div>
        </div>
      </div>
    </el-scrollbar>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useStore } from 'vuex'
import { MagicStick, Cpu } from '@element-plus/icons-vue'
import { useChatHistory } from '@/components/Chat/chatMessage.js'
import UserAvatar from '@/components/UserAvatar/index.vue'
import LlmReferences from '@/components/Chat/llmReferences.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '聊天记录' },
  spaceId: { type: [String, Number], default: null },
  // 对方类型：agent=智能体，model=模型，user=用户，决定对方头像展示形式
  targetType: { type: String, default: 'agent' }
})
const emit = defineEmits(['update:modelValue'])

const store = useStore()
const userInfo = computed(() => store.getters['user/getUserInfo'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const scrollbarRef = ref(null)
// 检索时间范围 [开始时间, 结束时间]
const dateRange = ref(null)

// 聊天记录列表与历史分页加载（向上滚动加载更早的历史）
const spaceIdRef = computed(() => props.spaceId)
// 检索条件，传给分页接口
const historyFilter = computed(() => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    return {}
  }
  return {
    createdStartTime: dateRange.value[0],
    // 结束日期取当天最后一秒，保证结束当天的消息被包含
    createdEndTime: dateRange.value[1].slice(0, 10) + ' 23:59:59'
  }
})
const {
  list: chatHisMsgs,
  loading: historyLoading,
  loadMore,
  onScroll,
  scrollToBottom,
  reset: resetHistory
} = useChatHistory(spaceIdRef, scrollbarRef, historyFilter)

/**
 * 抽屉打开时按 spaceId 加载聊天记录，检索条件重置
 */
watch(visible, (isOpen) => {
  if (isOpen && props.spaceId) {
    dateRange.value = null
    loadChatRecords()
  }
})

/**
 * 抽屉已打开时切换 spaceId 重新加载
 */
watch(() => props.spaceId, (newId) => {
  if (visible.value && newId) {
    loadChatRecords()
  }
})

/**
 * 检索条件变化后重新加载聊天记录
 * @param range 所选时间范围，清空时为 null
 */
function handleSearch(range) {
  dateRange.value = range || null
  loadChatRecords()
}

/**
 * 加载聊天记录：打开时取最新一页，更早的历史由滚动到顶部时按页加载
 */
function loadChatRecords() {
  resetHistory()
  loadMore().then(scrollToBottom)
}
</script>

<style scoped lang="scss">
@use "@/components/Chat/chatMarkdown" as chatMd;

:deep(.el-drawer__body) {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0;
}

.record-search {
  flex-shrink: 0;
  padding: $spacing-md $spacing-md 0;
}

.record-body {
  flex: 1;
  min-height: 0;
  height: calc(100vh - 160px);
  margin: $spacing-md;
  border: 1px solid $border-color;
  border-radius: $border-radius-md;
  padding: $spacing-sm $spacing-xs;
}

.record-loading {
  padding: $spacing-sm;
  text-align: center;
  font-size: 12px;
  color: $color-text-placeholder;
}

.chat-list {
  display: flex;
  flex-direction: column;
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
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

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

.record-time {
  color: $color-text-placeholder;
  font-size: 12px;
}

.chat-bubble {
  max-width: 80%;
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-md;
  word-break: break-word;
}

.chat-bubble-agent {
  background-color: $color-primary-light;
  color: $color-text-primary;
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
  white-space: pre-line;
}

::v-deep(.github-markdown-body) {
  @include chatMd.chat-markdown;
}
</style>

<template>
  <el-drawer
      v-model="visible"
      :title="title"
      direction="ltr"
      size="60%"
      append-to-body
      class="chat-record-drawer"
  >
    <el-scrollbar class="record-body" always>
      <div class="chat-list">
        <div
            v-for="(chatMsg, idx) in chatHisMsgs"
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
            <span class="record-time">{{ chatMsg.createdDt }}</span>
          </div>
          <!-- 消息内容 -->
          <div
              class="chat-bubble"
              :class="[
                chatMsg.senderId === userInfo.id ? 'chat-bubble-user' : 'chat-bubble-agent'
              ]"
          >
            <div class="chat-msg" v-if="chatMsg.senderId === userInfo.id">{{ chatMsg.message }}</div>
            <v-md-preview v-else class="chat-msg" :text="chatMsg.message || ''" />
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
import { getMsgListAPI } from '@/api/chat/msg.js'
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

const chatHisMsgs = ref([])

/**
 * 抽屉打开时按 spaceId 加载历史消息
 */
watch(visible, (isOpen) => {
  if (isOpen && props.spaceId) {
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
 * 加载聊天记录
 */
function loadChatRecords() {
  const query = { spaceId: props.spaceId }
  getMsgListAPI(query).then(res => {
    if (res.code !== 200) {
      return
    }
    chatHisMsgs.value = res.data || []
  })
}
</script>

<style scoped lang="scss">
:deep(.el-drawer__body) {
  padding: 0;
}

.record-body {
  height: calc(100vh - 110px);
  margin: $spacing-md;
  border: 1px solid $border-color;
  border-radius: $border-radius-md;
  padding: $spacing-sm $spacing-xs;
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
  overflow-wrap: break-word;
  line-height: $line-height-base;
  user-select: text;
}

::v-deep(.github-markdown-body) {
  padding: 0;
}
</style>

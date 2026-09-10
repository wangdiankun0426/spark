<template>
  <div class="app-container contact-container">
    <!-- 主体：左侧联系人 + 右侧聊天区 -->
    <div class="contact-body">
      <!-- 左侧联系人列表 -->
      <div class="session-pane">
        <el-input
            v-model="userKeyword"
            placeholder="请输入用户名称"
            clearable
            :prefix-icon="Search"
            class="session-search"
        />
        <div class="session-pane-title-row">
          <div class="session-pane-title">联系人</div>
          <span class="session-pane-count">{{ filteredUsers.length }} 人</span>
        </div>
        <el-scrollbar v-if="filteredUsers.length" class="session-list" always>
          <div
              v-for="user in filteredUsers"
              :key="user.id"
              class="session-item"
              :class="{ 'session-item-active': chatUser.id === user.id }"
              @click="selectedUser(user)"
          >
            <el-badge
                :value="user.noReadCount"
                :hidden="!user.noReadCount"
                class="session-item-badge"
            >
              <user-avatar :user-id="user.id" :size="32" />
            </el-badge>
            <div class="session-item-info">
              <div class="session-item-title">{{ user.name }}</div>
            </div>
          </div>
        </el-scrollbar>
        <el-empty v-else class="session-empty" description="暂无联系人" :image-size="90" />
      </div>

      <!-- 右侧聊天区 -->
      <div class="chat-pane">
        <chat-panel
            :target="chatUser"
            target-type="user"
            :fill="true"
            @message-received="handleMessageReceived"
            @space-created="handleSpaceCreated"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMyChatUserListAPI } from '@/api/chat/user.js'
import { Search } from '@element-plus/icons-vue'
import UserAvatar from '@/components/UserAvatar/index.vue'
import ChatPanel from '@/components/Chat/chatPanel.vue'

const chatUser = ref({})
const users = ref([])
const userKeyword = ref('')

/**
 * 按关键字过滤后的用户列表
 */
const filteredUsers = computed(() => {
  if (!userKeyword.value) return users.value
  const kw = userKeyword.value.toLowerCase()
  return users.value.filter(u => String(u.name || '').toLowerCase().includes(kw))
})

onMounted(() => {
  loadUsers();
})

/**
 * 加载联系人列表
 */
function loadUsers() {
  const query = { page: false }
  getMyChatUserListAPI(query).then(res => {
    if (res.data) {
      users.value = res.data
    }
  })
}

/**
 * 选择聊天用户
 */
function selectedUser(user) {
  user.noReadCount = 0
  chatUser.value = user
}

/**
 * 收到非当前聊天对象的消息时，更新用户列表未读数
 */
function handleMessageReceived(chatMsg) {
  if (chatUser.value.id === chatMsg.senderId) {
    return
  }
  users.value.forEach(item => {
    if (item.id === chatMsg.senderId) {
      item.noReadCount = (item.noReadCount || 0) + 1
    }
  })
  users.value.sort((a, b) => b.noReadCount - a.noReadCount)
}

/**
 * 聊天空间创建成功时同步到用户列表与当前选中对象
 */
function handleSpaceCreated({ targetId, spaceId }) {
  users.value.forEach(item => {
    if (item.id === targetId) {
      item.chatSpaceId = spaceId
    }
  })
  if (chatUser.value.id === targetId) {
    chatUser.value.chatSpaceId = spaceId
  }
}
</script>

<style scoped lang="scss">
.contact-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 70px);
}

.contact-body {
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

.session-search {
  flex-shrink: 0;
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

.session-item-badge {
  flex-shrink: 0;

  :deep(.el-badge__content) {
    top: 4px;
    right: 10px;
  }
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
</style>

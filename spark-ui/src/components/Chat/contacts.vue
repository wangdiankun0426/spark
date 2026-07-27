<template>
  <el-drawer
      v-model="visible"
      title="通讯录"
      direction="ltr"
      size="100%"
      :destroy-on-close="true"
      class="user-chat-drawer"
  >
    <div class="chat-container">
      <el-container>
        <!--左侧联系人-->
        <el-aside width="220px" class="space-el-aside">
          <el-card>
            <template #header>
              <el-input
                  v-model="userKeyword"
                  placeholder="请输入用户名称"
                  clearable
                  :prefix-icon="Search"
                  class="space-search-input"
              />
            </template>

            <div class="user-list">
              <el-card
                  v-for="user in filteredUsers"
                  :key="user.id"
                  @click="selectedUser(user)"
                  class="user-el-card"
              >
                <el-badge
                    :value="user.noReadCount"
                    class="item"
                    :hidden="user.noReadCount === 0"
                >
                  <span class="user-name" v-if="chatUser.id !== user.id">
                    <user-avatar :user-id="user.id" :size="26" class="user-avatar-icon" />
                    {{ user.name }}
                  </span>
                  <span class="user-name user-name-active" v-else>
                    <user-avatar :user-id="user.id" :size="26" class="user-avatar-icon" />
                    {{ user.name }}
                  </span>
                </el-badge>
              </el-card>
            </div>
          </el-card>
        </el-aside>
        <!--右侧聊天框-->
        <el-main class="chat-main">
          <chat-panel
              :target="chatUser"
              target-type="user"
              @message-received="handleMessageReceived"
              @space-created="handleSpaceCreated"
          />
        </el-main>
      </el-container>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { getMyChatUserListAPI } from '@/api/chat/user.js'
import { Search } from '@element-plus/icons-vue'
import UserAvatar from '@/components/UserAvatar/index.vue'
import ChatPanel from '@/components/Chat/chatPanel.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

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

/**
 * 抽屉打开时加载联系人列表
 */
watch(visible, (isOpen) => {
  if (isOpen && users.value.length === 0) {
    loadUsers()
  }
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
:deep(.el-drawer__body) {
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-container {
  margin: 0;
  padding: 0;
  flex: 1;
  min-height: 0;
  overflow: hidden;
  box-sizing: border-box;
  display: flex;

  :deep(.el-container) {
    width: 100%;
    height: 100%;
  }
}

.space-el-aside {
  height: calc(100vh - 80px);
  overflow: hidden;

  :deep(.el-card) {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  :deep(.el-card__header) {
    background-color: $bg-card;
    height: 46px !important;
    padding-top: 2px !important;
    flex-shrink: 0;
  }
}

.space-search-input {
  height: 42px;
  width: 100%;
  flex-shrink: 0;
}

.user-list {
  width: 100%;
  flex: 1;
  height: calc(100vh - 140px);
  overflow-y: auto;
}

.user-el-card {
  cursor: pointer;
  height: 44px !important;
  flex-shrink: 0;
}

.user-el-card :deep(.el-card__body) {
  margin-left: 40px;
}

.user-avatar-icon {
  vertical-align: middle;
  margin-right: 4px;
}

.user-name {
  font-size: 14px;
}

.user-name-active {
  font-weight: bolder;
  color: $color-primary;
}

.chat-main {
  padding: 0;
  margin: 0;
  height: 100%;
  overflow: hidden;
  background-color: white;
}
</style>

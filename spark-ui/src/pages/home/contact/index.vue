<template>
  <div class="app-container contact-container">
    <div class="contact-body">
      <!-- 左侧联系人列表 -->
      <div class="session-pane">
        <el-input
            v-model="userKeyword"
            v-debounce-input:500="searchUsers"
            placeholder="请输入用户名称"
            clearable
            :prefix-icon="Search"
            class="session-search"
        />
        <div class="session-pane-title-row">
          <div class="session-pane-title">联系人</div>
          <span class="session-pane-count">{{ users.length }} 人</span>
        </div>
        <el-scrollbar
            v-if="users.length"
            ref="sessionScrollRef"
            class="session-list"
            always
            @scroll="handleSessionScroll"
        >
          <div
              v-for="user in users"
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
              <div v-if="user.deptName" class="session-item-dept">{{ user.deptName }}</div>
            </div>
          </div>
          <div v-if="loadingUsers" class="session-tip">正在加载...</div>
          <div v-else-if="!hasMore" class="session-tip">没有更多联系人了</div>
        </el-scrollbar>
        <el-empty v-else class="session-empty" description="暂无联系人" :image-size="90" />
      </div>

      <!-- 右侧聊天区 -->
      <div class="chat-pane">
        <chat
            :target="chatUser"
            target-type="user"
            @message-received="handleMessageReceived"
            @space-created="handleSpaceCreated"
        >
          <template #header-left>
            <el-icon class="chat-panel-title-icon"><ChatIcon /></el-icon>
            <span v-if="chatUser.id" class="chat-panel-title">{{ chatUser.name }}</span>
            <span v-else class="chat-panel-header-tip">请选择聊天对象</span>
          </template>
          <template #empty>
            <div class="chat-welcome">
              <el-icon class="chat-welcome-icon"><ChatIcon /></el-icon>
              <div class="chat-welcome-title">选择联系人开始聊天</div>
              <div class="chat-welcome-subtitle">从左侧联系人列表中选择联系人，即可开始对话</div>
            </div>
          </template>
        </chat>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { pageMyChatUserListAPI } from '@/api/chat/user.js'
import { Search } from '@element-plus/icons-vue'
import UserAvatar from '@/components/UserAvatar/index.vue'
import Chat from '@/components/Chat/index.vue'
import ChatIcon from '@/assets/icons/chat.vue'

// 每页加载的联系人数量
const PAGE_SIZE = 20
// 距底部小于该距离时加载下一页
const LOAD_BOTTOM_OFFSET = 40

const chatUser = ref({})
const users = ref([])
const userKeyword = ref('')
const sessionScrollRef = ref(null)
// 是否正在加载联系人
const loadingUsers = ref(false)
// 是否还有下一页
const hasMore = ref(true)
// 已加载到的页码
let pageNo = 0
// 请求序号，用于丢弃过期响应
let requestSeq = 0

onMounted(() => {
  loadUsers()
})

/**
 * 加载联系人，检索条件变化时从头加载
 * @param reset 是否重置为第一页
 */
function loadUsers(reset) {
  if (reset) {
    users.value = []
    pageNo = 0
    hasMore.value = true
  } else if (loadingUsers.value) {
    // 触底加载去重，检索重置可直接打断进行中的请求
    return
  }
  if (!hasMore.value) {
    return
  }
  loadingUsers.value = true
  const nextPage = pageNo + 1
  const seq = ++requestSeq
  const query = { pageNo: nextPage, pageSize: PAGE_SIZE, name: userKeyword.value || null }
  pageMyChatUserListAPI(query).then(res => {
    if (seq !== requestSeq || res.code !== 200) {
      return
    }
    const rows = (res.data && res.data.rows) || []
    pageNo = nextPage
    hasMore.value = rows.length >= PAGE_SIZE
    users.value = users.value.concat(rows)
  }).finally(() => {
    // 过期请求不解除加载态，避免打断有新请求时的加载提示
    if (seq === requestSeq) {
      loadingUsers.value = false
    }
  })
}

/**
 * 按名称检索联系人
 */
function searchUsers() {
  loadUsers(true)
}

/**
 * 联系人列表滚动到底部附近时加载下一页
 */
function handleSessionScroll() {
  const scrollbar = sessionScrollRef.value
  const wrap = scrollbar ? scrollbar.wrapRef : null
  if (!wrap || loadingUsers.value || !hasMore.value) {
    return
  }
  if (wrap.scrollTop + wrap.clientHeight < wrap.scrollHeight - LOAD_BOTTOM_OFFSET) {
    return
  }
  loadUsers()
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

.session-tip {
  padding: $spacing-sm;
  text-align: center;
  font-size: 12px;
  color: $color-text-placeholder;
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
  margin-right: 10px;

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

/* 直属部门名称 */
.session-item-dept {
  font-size: 12px;
  color: $color-text-placeholder;
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

.chat-panel-header-tip {
  font-size: 12px;
  color: $color-text-placeholder;
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

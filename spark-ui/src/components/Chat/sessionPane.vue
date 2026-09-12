<template>
  <div class="session-pane">
    <el-input
        v-model="sessionKeyword"
        v-debounce-input:500="handleSessionSearch"
        placeholder="搜索会话名称"
        clearable
        :prefix-icon="Search"
        class="session-search"
    />
    <el-button
        style="height: 40px"
        :disabled="newDisabled"
        @click="emit('new')"
    >
      <el-icon><Plus /></el-icon>{{ newText }}
    </el-button>
    <div class="session-pane-title-row">
      <div class="session-pane-title">{{ title }}</div>
      <span class="session-pane-count">{{ sessions.length }} 条</span>
    </div>
    <el-scrollbar
        v-if="sessions.length"
        ref="sessionScrollRef"
        class="session-list"
        always
        @scroll="handleSessionScroll"
    >
      <div
          v-for="session in sessions"
          :key="session.spaceId"
          class="session-item"
          :class="{ 'session-item-active': session.spaceId === activeSpaceId }"
          @click="emit('select', session)"
      >
        <div class="session-item-icon" :class="'session-item-icon--' + theme">
          <el-icon><component :is="sessionIcon" /></el-icon>
        </div>
        <div class="session-item-info">
          <div class="session-item-title">{{ session.title || '新对话' }}</div>
          <div class="session-item-meta">
            <span v-if="session.receiverName">{{ session.receiverName }}</span>
            <span v-if="session.createdDt">{{ formatChatTime(session.createdDt) }}</span>
          </div>
        </div>
        <div class="session-item-actions" @click.stop>
          <el-dropdown
              trigger="click"
              placement="bottom-end"
              @command="command => handleCommand(command, session)"
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
      <div v-if="sessionLoading" class="session-tip">正在加载...</div>
      <div v-else-if="!hasMore" class="session-tip">没有更多会话了</div>
    </el-scrollbar>
    <el-empty
        v-else
        class="session-empty"
        :description="sessionKeyword ? '未找到匹配的会话' : '暂无会话记录'"
        :image-size="90"
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Cpu, Plus, MoreFilled, Search } from '@element-plus/icons-vue'
import { deleteChatSpaceAPI, updateChatSpaceTitleAPI } from '@/api/chat/space.js'
import { formatChatTime } from '@/utils/dateUtil.js'
import { useAiSessionList } from '@/components/Chat/aiSessionList.js'

// 会话类型，对应后端 ChatSpaceTypeEnum 的取值
const CHAT_SPACE_TYPE_AGENT = 3

const props = defineProps({
  // 会话类型：3=智能体，2=模型
  spaceType: { type: Number, required: true },
  // 列表标题
  title: { type: String, default: '会话记录' },
  // 新建按钮文案
  newText: { type: String, default: '开启新对话' },
  // 新建按钮是否禁用（无可选的智能体/模型时）
  newDisabled: { type: Boolean, default: false },
  // 当前选中的会话空间id
  activeSpaceId: { type: [String, Number], default: null }
})
const emit = defineEmits([
  // 选中某个会话
  'select',
  // 开启新对话
  'new',
  // 会话列表加载完成，父组件用于同步当前会话标题
  'loaded'
])

const sessionScrollRef = ref(null)
const {
  sessions,
  loading: sessionLoading,
  hasMore,
  keyword: sessionKeyword,
  loadFirst,
  refresh,
  handleScroll: handleSessionScroll,
  handleSearch: handleSessionSearch
} = useAiSessionList(props.spaceType, sessionScrollRef)

// 列表项图标与底色按会话类型区分
const theme = computed(() => props.spaceType === CHAT_SPACE_TYPE_AGENT ? 'agent' : 'model')
const sessionIcon = computed(() => props.spaceType === CHAT_SPACE_TYPE_AGENT ? MagicStick : Cpu)

// 是否已完成首次加载，首次加载后自动进入第一个会话
let initialized = false

/**
 * 加载第一页，首次加载后自动进入第一个会话（没有会话则开启新对话）
 * @returns {Promise<void>}
 */
function loadFirstSessions() {
  return loadFirst().then(() => {
    emit('loaded', sessions.value)
    if (initialized) {
      return
    }
    initialized = true
    if (sessions.value.length) {
      emit('select', sessions.value[0])
    } else {
      emit('new')
    }
  })
}

/**
 * 重新加载已加载的全部会话，用于同步标题与最近消息
 * @returns {Promise<void>}
 */
function refreshSessions() {
  return refresh().then(() => {
    emit('loaded', sessions.value)
  })
}

/**
 * 会话操作：重命名/删除
 * @param command 操作类型
 * @param session 会话
 */
function handleCommand(command, session) {
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
 * @param session 会话
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
    updateChatSpaceTitleAPI({ spaceId: session.spaceId, title }).then(res => {
      if (res.code !== 200) {
        return
      }
      ElMessage.success('重命名成功')
      session.title = title
      emit('loaded', sessions.value)
    })
  }).catch(() => {})
}

/**
 * 删除会话，删除的是当前会话时自动切到第一条或开启新对话
 * @param session 会话
 */
function handleDeleteSession(session) {
  ElMessageBox.confirm('是否确定删除此条会话记录?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteChatSpaceAPI({ spaceId: session.spaceId }).then(res => {
      if (res.code !== 200) {
        return
      }
      ElMessage.success('删除会话成功')
      const isActive = session.spaceId === props.activeSpaceId
      const index = sessions.value.findIndex(item => item.spaceId === session.spaceId)
      if (index > -1) {
        sessions.value.splice(index, 1)
      }
      if (!isActive) {
        return
      }
      if (sessions.value.length) {
        emit('select', sessions.value[0])
      } else {
        emit('new')
      }
    })
  }).catch(() => {})
}

defineExpose({ loadFirst: loadFirstSessions, refresh: refreshSessions })
</script>

<style scoped lang="scss">
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

.session-search {
  flex-shrink: 0;
  margin-bottom: $spacing-sm;
}

/* 分页加载提示 */
.session-tip {
  padding: $spacing-sm;
  text-align: center;
  font-size: 12px;
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
  border-radius: $border-radius-md;
}

.session-item-icon--agent {
  background-color: $agent-theme-purple;
}

.session-item-icon--model {
  background-color: $agent-theme-cyan;
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
</style>

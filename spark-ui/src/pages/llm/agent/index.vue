<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="agent-header">
      <div class="agent-header-left">
        <div class="agent-header-title">
          <el-icon class="agent-header-icon">
            <MagicStick />
          </el-icon>
          AGENT
        </div>
        <div class="agent-header-subtitle">选择合适的Agent，开启智能对话</div>
      </div>
      <div class="agent-header-right">
        <el-input
            v-model="keyword"
            placeholder="搜索 Agent 名称"
            clearable
            :prefix-icon="Search"
            style="width: 240px"
        />
      </div>
    </div>

    <!-- Agent卡片网格 -->
    <div class="agent-grid" v-if="agents.length">
      <info-card
          v-for="agent in agents"
          :key="agent.id"
          :theme="getTheme(agent)"
          :icon="MagicStick"
          :title="agent.name"
          :id-text="'编号 #' + agent.id"
          :description="getDescription(agent)"
          :disabled="agent.status === -1"
          :height="360"
          @click="handleSelectAgent(agent)"
      >
        <!-- 状态徽章 -->
        <template #badge>
          <span v-if="agent.status === -1" class="badge badge-muted">已停用</span>
        </template>

        <!-- 标签区：语言模型 / 工具 / 知识库 / MCP服务 -->
        <template #tags>
          <div class="tag-group" v-if="agent.chatModelName">
            <span class="tag-group-label">语言模型</span>
            <el-tag size="small" effect="light" round>{{ agent.chatModelName }}</el-tag>
          </div>
          <div class="tag-group" v-if="parseTools(agent.toolNames).length">
            <span class="tag-group-label">工具</span>
            <el-tag
                v-for="tool in parseTools(agent.toolNames)"
                :key="tool"
                size="small"
                effect="light"
                round
            >
              {{ tool }}
            </el-tag>
          </div>
          <div class="tag-group" v-if="parseTools(agent.kbNames).length">
            <span class="tag-group-label">知识库</span>
            <el-tag
                v-for="kb in parseTools(agent.kbNames)"
                :key="kb"
                size="small"
                effect="light"
                round
            >
              {{ kb }}
            </el-tag>
          </div>
          <div class="tag-group" v-if="parseTools(agent.mcpNames).length">
            <span class="tag-group-label">MCP服务</span>
            <el-tag
                v-for="mcp in parseTools(agent.mcpNames)"
                :key="mcp"
                size="small"
                effect="light"
                round
            >
              {{ mcp }}
            </el-tag>
          </div>
        </template>

        <!-- 底部元信息 -->
        <template #meta>
          <span class="meta-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>记忆 {{ agent.maxMessages }} 轮</span>
          </span>
          <span class="meta-item" v-if="agent.createdByName">
            <el-icon><User /></el-icon>
            <span>{{ agent.createdByName }}</span>
          </span>
        </template>

        <!-- 底部操作 -->
        <template #action>
          <span v-if="agent.status === -1" class="action-item action-muted">已停用</span>
          <span v-else class="action-item">
            使用
            <el-icon><ArrowRight /></el-icon>
          </span>
        </template>
      </info-card>
    </div>
    <!-- 空状态 -->
    <el-empty v-else :description="keyword ? '未找到匹配的 Agent' : '暂无 Agent'" :image-size="120" />

    <!-- llm聊天抽屉 -->
    <llm-chat
        v-model="drawerVisible"
        :target="selectedAgent"
        target-type="agent"
    />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { pageAgentListAPI } from '@/api/llm/agent.js'
import { MagicStick, Search, ArrowRight, User, ChatDotRound } from '@element-plus/icons-vue'
import LlmChat from '@/components/Chat/llmChat.vue'
import InfoCard from '@/components/InfoCard/index.vue'

const agents = ref([])
const keyword = ref('')
const drawerVisible = ref(false)
const selectedAgent = ref({})

// 主题色循环，配合 variables.scss 中的 agent 主题 token 使用
const themes = ['blue', 'green', 'purple', 'orange', 'cyan', 'pink', 'indigo']

let searchTimer = null

onMounted(() => {
  loadAgents()
})

onBeforeUnmount(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})

/**
 * 名称搜索防抖，300ms 后走接口查询
 */
watch(keyword, () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    loadAgents()
  }, 300)
})

/**
 * 加载智能体列表，携带名称关键字
 */
function loadAgents() {
  const query = { page: false }
  if (keyword.value) {
    query.name = keyword.value
  }
  pageAgentListAPI(query).then(res => {
    if (res.data && res.data.rows) {
      agents.value = res.data.rows
    }
  })
}

/**
 * 根据 agent.id 计算主题名
 * @param agent
 */
function getTheme(agent) {
  return themes[agent.id % themes.length]
}

/**
 * 获取描述，description 为空时回退到 systemPrompt 截断
 * @param agent
 */
function getDescription(agent) {
  if (agent.description) {
    return agent.description
  }
  if (agent.systemPrompt) {
    return agent.systemPrompt.length > 60 ? agent.systemPrompt.slice(0, 60) + '...' : agent.systemPrompt
  }
  return '暂无描述'
}

/**
 * 解析工具名，按逗号分割
 * @param toolNames
 */
function parseTools(toolNames) {
  if (!toolNames) return []
  return String(toolNames).split(',').map(t => t.trim()).filter(Boolean)
}

/**
 * 选中 agent，打开聊天抽屉；已停用的禁止对话
 * @param agent
 */
function handleSelectAgent(agent) {
  if (agent.status === -1) {
    ElMessage.warning('该 Agent已停用，暂无法对话')
    return
  }
  selectedAgent.value = agent
  drawerVisible.value = true
}
</script>

<style scoped lang="scss">
.agent-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  padding: $spacing-lg $spacing-xl;
  background-color: $bg-card;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-card;
}

.agent-header-left {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.agent-header-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 20px;
  font-weight: 700;
  color: $color-text-primary;
}

.agent-header-icon {
  font-size: 24px;
  color: $color-primary;
}

.agent-header-subtitle {
  font-size: 13px;
  color: $color-text-secondary;
}

.agent-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.agent-grid {
  height: calc(100vh - #{$nav-height} - 180px);
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: $spacing-lg;
  align-content: start;
}
</style>

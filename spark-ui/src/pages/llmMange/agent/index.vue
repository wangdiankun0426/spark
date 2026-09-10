<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="agent-header">
      <div class="agent-header-left">
        <div class="agent-header-title">
          AGENT
        </div>
        <div class="agent-header-subtitle">管理自定义agent</div>
      </div>
      <div class="agent-header-right">
        <el-input
            v-model="keyword"
            placeholder="搜索 Agent 名称"
            clearable
            :prefix-icon="Search"
            style="width: 300px"
        />
        <el-button type="primary" @click="handleOpenCreateForm">
          <el-icon><Plus /></el-icon>新增Agent
        </el-button>
      </div>
    </div>

    <!-- Agent卡片网格 -->
    <div
        class="agent-grid"
        v-if="agents.length"
    >
      <info-card
          v-for="agent in agents"
          :key="agent.id"
          :icon="MagicStick"
          :title="agent.name"
          :description="agent.description || '暂无描述'"
          :disabled="agent.status !== 1"
          :actions="cardActions(agent)"
      />
    </div>
    <!-- 空状态 -->
    <el-empty
        class="empty-grid"
        v-else
        :description="keyword ? '未找到匹配的 Agent' : '暂无 Agent'"
        :image-size="120"
    />
    <!-- 分页 -->
    <el-pagination
        :current-page="query.pageNo"
        :page-size="query.pageSize"
        :page-sizes="pageSizes"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
    />

    <!-- 新增 / 修改 Agent 表单抽屉 -->
    <el-drawer
        v-model="formVisible"
        :title="formTitle"
        direction="ltr"
        size="40%"
        :before-close="handleCloseForm"
    >
      <el-form
          :model="form"
          label-width="auto"
          :rules="formRules"
          ref="formRef"
      >
        <el-form-item label="名称" prop="name">
          <el-input
              v-model="form.name"
              placeholder="请输入智能体名称，如：客服助手、知识问答专家"
              maxlength="50"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="语言模型" prop="chatModelId">
          <el-select
              v-model="form.chatModelId"
              placeholder="请选择语言模型"
              style="width: 100%"
          >
            <el-option
                v-for="item in modelOptions"
                :key="item.id"
                :label="item.providerName + ' - ' + item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="系统提示词" prop="sysPrompt">
          <el-input
              v-model="form.sysPrompt"
              placeholder="请输入系统提示词，用于定义智能体的角色、行为与回答风格，如：你是一位专业的客服助手，请耐心解答用户问题"
              type="textarea"
              :rows="20"
              maxlength="2048"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="对话记忆大小" prop="maxMessages">
          <el-input-number
              v-model="form.maxMessages"
              :min="1" :max="100"
          />
        </el-form-item>
        <el-form-item label="工具" prop="tools">
          <el-select
              v-model="form.tools"
              placeholder="请选择工具，可多选"
              style="width: 100%"
              multiple
              collapse-tags
              collapse-tags-tooltip
          >
            <el-option
                v-for="item in toolOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="知识库" prop="kbIds" v-if="form.tools.includes('search_kb')">
          <el-select
              v-model="form.kbIds"
              placeholder="请选择知识库，可多选"
              style="width: 100%"
              multiple
              collapse-tags
              collapse-tags-tooltip
          >
            <el-option
                v-for="item in kbOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="知识图谱" prop="graphIds" v-if="form.tools.includes('search_kg')">
          <el-select
              v-model="form.graphIds"
              placeholder="请选择知识图谱，可多选"
              style="width: 100%"
              multiple
              collapse-tags
              collapse-tags-tooltip
          >
            <el-option
                v-for="item in graphOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="MCP服务" prop="mcpIds">
          <el-select
              v-model="form.mcpIds"
              placeholder="请选择 MCP 服务，可多选"
              style="width: 100%"
              multiple
              collapse-tags
              collapse-tags-tooltip
          >
            <el-option
                v-for="item in mcpOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="技能" prop="skills">
          <el-select
              v-model="form.skills"
              placeholder="请选择技能，可多选"
              style="width: 100%"
              multiple
              collapse-tags
              collapse-tags-tooltip
          >
            <el-option
                v-for="item in skillOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
              v-model="form.status"
              :active-value="1"
              :inactive-value="-1"
              active-text="已启用"
              inactive-text="已停用"
              inline-prompt
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
              v-model="form.description"
              placeholder="请输入描述，简要说明智能体的用途与适用场景，最多 200 字"
              type="textarea"
              :rows="5"
              maxlength="256"
              show-word-limit
          />
        </el-form-item>
      </el-form>
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          <div class="form-tip">
            <div>名称为必填项，建议简明体现智能体用途；</div>
            <div>语言模型为必填项，决定智能体对话时所使用的语言模型；</div>
            <div>系统提示词为必填项，用于约束智能体的角色与回答风格；</div>
            <div>对话记忆大小决定上下文轮数，值过大会增加 token 消耗，建议 10-20；</div>
            <div>选择"检索知识库"工具后可配置关联知识库；选择"检索知识图谱"工具后可配置关联知识图谱；</div>
            <div>MCP 服务与技能均可多选，技能需先在技能库中维护。</div>
          </div>
        </template>
      </el-alert>
      <template #footer>
        <div class="drawer-footer">
          <el-button type="primary" @click="handleSubmitForm">保存</el-button>
          <el-button @click="handleCloseForm">取消</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageAgentListAPI,
  createAgentAPI,
  updateAgentAPI,
  queryAgentDetailAPI,
  deleteAgentAPI
} from '@/api/llm/agent.js'
import { pageModelListAPI } from '@/api/llm/model.js'
import { pageKnowledgeListAPI } from '@/api/kb/knowledge.js'
import { pageGraphListAPI } from '@/api/kg/graph.js'
import { pageMcpListAPI } from '@/api/llm/mcp.js'
import { pageSkillListAPI } from '@/api/llm/skill.js'
import { MagicStick, Search, Plus } from '@element-plus/icons-vue'
import InfoCard from '@/components/InfoCard/index.vue'

const agents = ref([])
const total = ref(0)
const pageSizes = [10, 30, 50]
const keyword = ref('')
// 分页查询条件
const query = ref({
  pageNo: 1,
  pageSize: 10,
})

// 新增 / 修改 表单
const formVisible = ref(false)
const formTitle = ref('')
const formRef = ref(null)
const form = ref(createEmptyForm())
const formRules = {
  name: [{ required: true, trigger: 'blur', message: '请输入名称' }],
  chatModelId: [{ required: true, trigger: 'change', message: '请选择语言模型' }],
  sysPrompt: [{ required: true, trigger: 'blur', message: '请输入系统提示词' }],
  maxMessages: [{ required: true, trigger: 'blur', message: '请输入对话记忆大小' }],
}

// 工具选项
const toolOptions = [
  { label: '检索知识库', value: 'search_kb' },
  { label: '检索知识图谱', value: 'search_kg' },
]

// 语言模型 / 知识库 / 知识图谱 / MCP / 技能 选项
const modelOptions = ref([])
const kbOptions = ref([])
const graphOptions = ref([])
const mcpOptions = ref([])
const skillOptions = ref([])

let searchTimer = null

onMounted(() => {
  loadAgents()
  loadModelOptions()
  loadKbOptions()
  loadGraphOptions()
  loadMcpOptions()
  loadSkillOptions()
})

onBeforeUnmount(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})

/**
 * 名称搜索防抖，300ms 后回到第一页并重新查询
 */
watch(keyword, () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    query.value.pageNo = 1
    loadAgents()
  }, 300)
})

/**
 * 构造空表单对象
 */
function createEmptyForm() {
  return {
    id: undefined,
    name: undefined,
    chatModelId: undefined,
    sysPrompt: undefined,
    maxMessages: 10,
    tools: [],
    kbIds: [],
    graphIds: [],
    mcpIds: [],
    skills: [],
    description: undefined,
    status: 1,
  }
}

/**
 * 分页查询智能体列表，携带名称关键字
 */
function loadAgents() {
  const params = {
    pageNo: query.value.pageNo,
    pageSize: query.value.pageSize,
  }
  if (keyword.value) {
    params.name = keyword.value
  }
  pageAgentListAPI(params).then(res => {
    if (res.data && res.data.rows) {
      agents.value = res.data.rows
      total.value = res.data.total || 0
    }
  })
}

/**
 * 切换每页条数，回到第一页重新查询
 * @param size
 */
function handleSizeChange(size) {
  query.value.pageSize = size
  query.value.pageNo = 1
  loadAgents()
}

/**
 * 切换页码重新查询
 * @param pageNo
 */
function handleCurrentChange(pageNo) {
  query.value.pageNo = pageNo
  loadAgents()
}

/**
 * 卡片底部工具栏按钮
 * @param agent
 */
function cardActions(agent) {
  return [
    { key: 'edit', label: '修改', onClick: () => handleOpenUpdateForm(agent) },
    { key: 'delete', label: '删除', onClick: () => handleDelete(agent) }
  ]
}

/**
 * 加载语言模型选项列表（仅查询语言模型类型）
 */
function loadModelOptions() {
  pageModelListAPI({ page: false, type: 1 }).then(res => {
    if (res.code === 200 && res.data) {
      modelOptions.value = res.data.rows || []
    }
  })
}

/**
 * 加载知识库选项列表（一次性拉取全量）
 */
function loadKbOptions() {
  pageKnowledgeListAPI({ page: false }).then(res => {
    if (res.code === 200 && res.data) {
      kbOptions.value = res.data.rows || []
    }
  })
}

/**
 * 加载知识图谱选项列表（一次性拉取全量）
 */
function loadGraphOptions() {
  pageGraphListAPI({ page: false }).then(res => {
    if (res.code === 200 && res.data) {
      graphOptions.value = res.data.rows || []
    }
  })
}

/**
 * 加载 MCP 服务选项列表（一次性拉取全量）
 */
function loadMcpOptions() {
  pageMcpListAPI({ page: false }).then(res => {
    if (res.code === 200 && res.data) {
      mcpOptions.value = res.data.rows || []
    }
  })
}

/**
 * 加载技能选项列表（仅查询启用技能）
 */
function loadSkillOptions() {
  pageSkillListAPI({ page: false, status: 1 }).then(res => {
    if (res.code === 200 && res.data) {
      skillOptions.value = res.data.rows || []
    }
  })
}

/**
 * 打开新建表单
 */
function handleOpenCreateForm() {
  form.value = createEmptyForm()
  formTitle.value = '新增 Agent'
  formVisible.value = true
}

/**
 * 打开修改表单，先拉取详情回填
 * @param agent
 */
function handleOpenUpdateForm(agent) {
  queryAgentDetailAPI({ id: agent.id }).then(res => {
    if (!res.data) return
    const d = res.data
    form.value = {
      id: d.id,
      name: d.name,
      chatModelId: d.chatModelId,
      sysPrompt: d.sysPrompt,
      maxMessages: d.maxMessages,
      // 将逗号分隔的字符串转换为数组
      tools: d.tools ? d.tools.split(',').filter(t => t) : [],
      kbIds: Array.isArray(d.kbIds) ? d.kbIds : (d.kbIds ? String(d.kbIds).split(',').map(id => Number(id)).filter(id => id) : []),
      graphIds: Array.isArray(d.graphIds) ? d.graphIds : (d.graphIds ? String(d.graphIds).split(',').map(id => Number(id)).filter(id => id) : []),
      mcpIds: Array.isArray(d.mcpIds) ? d.mcpIds : (d.mcpIds ? String(d.mcpIds).split(',').map(id => Number(id)).filter(id => id) : []),
      skills: Array.isArray(d.skills) ? d.skills : (d.skills ? String(d.skills).split(',').map(id => Number(id)).filter(id => id) : []),
      description: d.description,
      status: d.status,
    }
    formTitle.value = '修改 Agent'
    formVisible.value = true
  })
}

/**
 * 关闭表单
 */
function handleCloseForm() {
  form.value = createEmptyForm()
  if (formRef.value) {
    formRef.value.clearValidate()
  }
  formTitle.value = ''
  formVisible.value = false
}

/**
 * 提交表单（新增 / 修改）
 */
function handleSubmitForm() {
  formRef.value.validate(valid => {
    if (!valid) return
    // 将工具/技能数组转换为逗号分隔的字符串
    const toolsStr = Array.isArray(form.value.tools) ? form.value.tools.join(',') : form.value.tools
    const skillsStr = Array.isArray(form.value.skills) ? form.value.skills.join(',') : form.value.skills
    const data = {
      id: form.value.id,
      name: form.value.name,
      chatModelId: form.value.chatModelId,
      sysPrompt: form.value.sysPrompt,
      maxMessages: form.value.maxMessages,
      tools: toolsStr,
      kbIds: form.value.kbIds,
      graphIds: form.value.graphIds,
      mcpIds: form.value.mcpIds,
      skills: skillsStr,
      description: form.value.description,
      status: form.value.status,
    }
    if (!data.id) {
      createAgentAPI(data).then(res => {
        if (res.code !== 200) return
        ElMessage.success('智能体创建成功')
        handleCloseForm()
        loadAgents()
      })
    } else {
      updateAgentAPI(data).then(res => {
        if (res.code !== 200) return
        ElMessage.success('智能体修改成功')
        handleCloseForm()
        loadAgents()
      })
    }
  })
}

/**
 * 删除智能体
 * @param agent
 */
function handleDelete(agent) {
  ElMessageBox.confirm('是否确定删除此条智能体?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteAgentAPI({ id: agent.id }).then(res => {
      if (res.code !== 200) return
      ElMessage.success('删除智能体成功')
      // 删除当前页最后一条时回退上一页，避免停留在空页
      if (agents.value.length === 1 && query.value.pageNo > 1) {
        query.value.pageNo -= 1
      }
      loadAgents()
    })
  }).catch(() => {})
}
</script>

<style scoped lang="scss">
.agent-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  padding: $spacing-md $spacing-xl;
  background-color: $bg-card;
  border-radius: $border-radius-md;
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
  height: calc(100vh - #{$nav-height} - 190px);
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: $spacing-lg;
  align-content: start;
}
.empty-grid {
  height: calc(100vh - #{$nav-height} - 190px);
}

.drawer-footer {
  padding: 0 $spacing-md;
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
}

.form-tip {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  font-size: 12px;
  line-height: 1.6;
}
</style>

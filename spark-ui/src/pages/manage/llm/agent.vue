<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-button
          type="primary"
          @click="handleOpenCreateAgentForm"
      >
        <el-icon><Plus /></el-icon>新建智能体
      </el-button>
      <el-button
          type="warning"
          @click="handleResetAgentQuery"
      >
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button
          type="info"
          @click="handleGetAgentList"
      >
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--智能体列表-->
    <div>
      <el-table
          ref="tableRef"
          height="calc(100vh - 155px)"
          :data="agentList"
          highlight-current-row
          @sort-change="handleSortChange"
          :header-cell-style="handleHeaderCellClass"
      >
        <el-table-column prop="id" label="编号" width="100" align="center"/>
        <el-table-column prop="name" label="名称"  width="200" align="center"/>
        <el-table-column prop="chatModelName" label="语言模型" width="200" align="center"/>
        <el-table-column prop="toolNames" label="工具"  width="200"  align="center"/>
        <el-table-column prop="kbNames" label="知识库" width="200" align="center"/>
        <el-table-column prop="mcpNames" label="MCP服务" width="200" align="center"/>
        <el-table-column prop="skillNames" label="技能" width="200" align="center"/>
        <el-table-column prop="maxMessages" label="对话记忆大小" width="120"  align="center"/>
        <el-table-column prop="statusName" label="状态" align="center"/>
        <el-table-column prop="createdByName" label="创建人" align="center"/>
        <el-table-column prop="createdDt" label="创建时间" width="160" align="center"/>
        <el-table-column prop="updatedByName" label="修改人" align="center"/>
        <el-table-column prop="updatedDt" label="修改时间" width="160" align="center"/>
        <el-table-column fixed="right" label="操作" width="140" align="center">
          <template #default="scope">
            <el-button
                type="success"
                text
                @click="handleOpenUpdateAgentForm(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              <span style="font-size: 12px; font-weight: 400">
               修改
              </span>
            </el-button>
            <el-button
                type="danger"
                text
                @click="handleDeleteAgent(scope.row.id)"
            >
              <el-icon><Delete /></el-icon>
              <span style="font-size: 12px; font-weight: 400">
               删除
              </span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!--分页组件-->
    <div>
      <el-pagination
          :current-page="agentQuery.pageNo"
          :page-size="agentQuery.pageSize"
          :page-sizes="pageSizes"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handlePageChangeSize"
          @current-change="handlePageChangeNo"
      />
    </div>
    <!--智能体表单-->
    <el-drawer
        v-model="agentFormVisible"
        :title="agentFormTitle"
        direction="ltr"
        size="40%"
        :before-close="handleCloseAgentForm"
    >
      <el-form
          :model="agentForm"
          label-width="auto"
          :rules="agentFormRules"
          ref="agentFormRef"
      >
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="名称" prop="name">
              <el-input
                  v-model="agentForm.name"
                  placeholder="请输入智能体名称，如：客服助手、知识问答专家"
                  maxlength="50"
                  show-word-limit
              />
            </el-form-item>
            <el-form-item label="语言模型" prop="chatModelId">
              <el-select
                  v-model="agentForm.chatModelId"
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
                  v-model="agentForm.sysPrompt"
                  placeholder="请输入系统提示词，用于定义智能体的角色、行为与回答风格，如：你是一位专业的客服助手，请耐心解答用户问题"
                  type="textarea"
                  :rows="20"
                  maxlength="2048"
                  show-word-limit
              />
            </el-form-item>
            <el-form-item label="对话记忆大小" prop="maxMessages">
              <el-input-number
                  v-model="agentForm.maxMessages"
                  :min="1" :max="100"
              />
            </el-form-item>
            <el-form-item label="工具" prop="tools">
              <el-select
                  v-model="agentForm.tools"
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
            <el-form-item label="知识库" prop="kbIds" v-if="agentForm.tools.includes('search_kb')">
              <el-select
                  v-model="agentForm.kbIds"
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
            <el-form-item label="知识图谱" prop="graphIds" v-if="agentForm.tools.includes('search_kg')">
              <el-select
                  v-model="agentForm.graphIds"
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
                  v-model="agentForm.mcpIds"
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
                  v-model="agentForm.skills"
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
                  v-model="agentForm.status"
                  :active-value="1"
                  :inactive-value="-1"
                  active-text="已启用"
                  inactive-text="已停用"
                  inline-prompt
              />
            </el-form-item>
            <el-form-item label="描述" prop="description">
              <el-input
                  v-model="agentForm.description"
                  placeholder="请输入描述，简要说明智能体的用途与适用场景，最多 200 字"
                  type="textarea"
                  :rows="5"
                  maxlength="256"
                  show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <!--必填字段填写提示-->
      <el-alert
          type="info"
          :closable="false"
          show-icon
      >
        <template #title>
          <div class="form-tip">
            <div>名称为必填项，建议简明体现智能体用途；</div>
            <div>语言模型为必填项，决定智能体对话时所使用的语言模型；</div>
            <div>系统提示词为必填项，用于约束智能体的角色与回答风格，建议明确身份、职责与边界；</div>
            <div>对话记忆大小决定上下文轮数，值过大会增加 token 消耗，建议 10-20；</div>
            <div>工具可多选，选择后智能体在对话中可调用对应能力；</div>
            <div>选择"检索知识库"工具后，可配置关联的知识库；选择"检索知识图谱"工具后，可配置关联的知识图谱；</div>
            <div>MCP服务可多选，选择后智能体可调用对应 MCP 服务器的工具能力；</div>
            <div>技能可多选，智能体命中技能时读取技能正文并按其执行，技能需在"技能库管理"中维护；</div>
            <div>描述用于辅助识别，建议填写适用场景与使用对象。</div>
          </div>
        </template>
      </el-alert>
      <template #footer>
        <div class="drawer-footer">
          <el-button
              type="primary"
              @click="handleSubmitAgentForm"
          >保存</el-button>
          <el-button
              @click="handleCloseAgentForm"
          >取消</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import {getCurrentInstance, ref} from 'vue';
import {pageAgentListAPI, createAgentAPI, updateAgentAPI, queryAgentDetailAPI, deleteAgentAPI} from '@/api/llm/agent.js';
import {pageModelListAPI} from '@/api/llm/model.js';
import {pageKnowledgeListAPI} from '@/api/kb/knowledge.js';
import {pageGraphListAPI} from '@/api/kg/graph.js';
import {pageMcpListAPI} from '@/api/llm/mcp.js';
import {pageSkillListAPI} from '@/api/llm/skill.js';
import {ElMessage, ElMessageBox} from "element-plus";
import { Search } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();
const agentQuery = ref({
  pageNo: 1,
  pageSize: 30,
  sorts: {},
});
const agentSearchFlag = ref({
});
const total = ref(0);
const pageSizes = [30,50,100];
const agentList = ref([]);
const agentFormVisible = ref(false);
const agentFormTitle = ref('');
const agentForm = ref({
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
});
const agentFormRules = {
  name: [{ required: true, trigger: "blur", message: "请输入名称" }],
  chatModelId: [{ required: true, trigger: "change", message: "请选择语言模型" }],
  sysPrompt: [{ required: true, trigger: "blur", message: "请输入系统提示词" }],
  maxMessages: [{ required: true, trigger: "blur", message: "请输入对话记忆大小" }],
};

handleGetAgentList();

/**
 * 删除智能体
 * @param id
 * */
function handleDeleteAgent(id) {
  ElMessageBox.confirm(
      '是否确定删除此条智能体?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    const data = {
      id: id
    };
    deleteAgentAPI(data).then(res => {
      handleGetAgentList();
      ElMessage.success("删除智能体成功");
    })
  }).catch(() => {})
}

/**
 * 打开修改智能体表单
 * @param row
 */
function handleOpenUpdateAgentForm(row) {
  const query = {
    id: row.id
  };
  queryAgentDetailAPI(query).then(res => {
    agentForm.value.id = res.data.id;
    agentForm.value.name = res.data.name;
    agentForm.value.chatModelId = res.data.chatModelId;
    agentForm.value.sysPrompt = res.data.sysPrompt;
    agentForm.value.maxMessages = res.data.maxMessages;
    // 将逗号分隔的字符串转换为数组
    agentForm.value.tools = res.data.tools ? res.data.tools.split(',').filter(t => t) : [];
    agentForm.value.kbIds = Array.isArray(res.data.kbIds) ? res.data.kbIds : (res.data.kbIds ? String(res.data.kbIds).split(',').map(id => Number(id)).filter(id => id) : []);
    agentForm.value.graphIds = Array.isArray(res.data.graphIds) ? res.data.graphIds : (res.data.graphIds ? String(res.data.graphIds).split(',').map(id => Number(id)).filter(id => id) : []);
    agentForm.value.mcpIds = Array.isArray(res.data.mcpIds) ? res.data.mcpIds : (res.data.mcpIds ? String(res.data.mcpIds).split(',').map(id => Number(id)).filter(id => id) : []);
    agentForm.value.skills = Array.isArray(res.data.skills) ? res.data.skills : (res.data.skills ? String(res.data.skills).split(',').map(id => Number(id)).filter(id => id) : []);
    agentForm.value.description = res.data.description;
    agentForm.value.status = res.data.status;
    agentFormTitle.value = "修改智能体";
    agentFormVisible.value = true;
  });
}

// 工具选项
const toolOptions = [
  {
    label: "检索知识库",
    value: "search_kb",
  },
  {
    label: "检索知识图谱",
    value: "search_kg",
  },
];

// 语言模型选项
const modelOptions = ref([]);

/**
 * 加载语言模型选项列表（仅查询语言模型类型）
 */
function loadModelOptions() {
  pageModelListAPI({ page: false, type: 1 }).then(res => {
    if (res.code === 200 && res.data) {
      modelOptions.value = res.data.rows || [];
    }
  });
}

loadModelOptions();

// 知识库选项
const kbOptions = ref([]);

/**
 * 加载知识库选项列表（一次性拉取全量）
 */
function loadKbOptions() {
  pageKnowledgeListAPI({ page: false }).then(res => {
    if (res.code === 200 && res.data) {
      kbOptions.value = res.data.rows || [];
    }
  });
}

loadKbOptions();

// 知识图谱选项
const graphOptions = ref([]);

/**
 * 加载知识图谱选项列表（一次性拉取全量）
 */
function loadGraphOptions() {
  pageGraphListAPI({ page: false }).then(res => {
    if (res.code === 200 && res.data) {
      graphOptions.value = res.data.rows || [];
    }
  });
}

loadGraphOptions();

// MCP 服务选项
const mcpOptions = ref([]);

/**
 * 加载 MCP 服务选项列表（一次性拉取全量）
 */
function loadMcpOptions() {
  pageMcpListAPI({ page: false }).then(res => {
    if (res.code === 200 && res.data) {
      mcpOptions.value = res.data.rows || [];
    }
  });
}

loadMcpOptions();

// 技能选项
const skillOptions = ref([]);

/**
 * 加载技能选项列表（仅查询启用技能）
 */
function loadSkillOptions() {
  pageSkillListAPI({ page: false, status: 1 }).then(res => {
    if (res.code === 200 && res.data) {
      skillOptions.value = res.data.rows || [];
    }
  });
}

loadSkillOptions();

/**
 * 打开创建智能体表单
 * */
function handleOpenCreateAgentForm() {
  agentForm.value.id = undefined;
  agentForm.value.name = undefined;
  agentForm.value.chatModelId = undefined;
  agentForm.value.sysPrompt = undefined;
  agentForm.value.maxMessages = 10;
  agentForm.value.tools = [];
  agentForm.value.kbIds = [];
  agentForm.value.graphIds = [];
  agentForm.value.mcpIds = [];
  agentForm.value.skills = [];
  agentForm.value.description = undefined;
  agentForm.value.status = 1;
  agentFormTitle.value = "创建智能体";
  agentFormVisible.value = true;
}

/**
 * 重置查询条件
 * */
function handleResetAgentQuery() {
  agentQuery.value.pageNo = 1;
  agentQuery.value.pageSize = 15;
  // 清除排序状态
  let columns = proxy.$refs.tableRef.store.states.columns.value;
  columns.forEach((column) => {
    column.order = null;
  });
  agentQuery.value.sorts = {};
  handleGetAgentList();
}

/**
 * 提交智能体表单
 * */
function handleSubmitAgentForm() {
  proxy.$refs.agentFormRef.validate(valid => {
    if (valid) {
      // 将工具数组转换为逗号分隔的字符串
      const toolsStr = Array.isArray(agentForm.value.tools) ? agentForm.value.tools.join(',') : agentForm.value.tools;
      // 将技能ID数组转换为逗号分隔的字符串
      const skillsStr = Array.isArray(agentForm.value.skills) ? agentForm.value.skills.join(',') : agentForm.value.skills;

      if (!agentForm.value.id) {
        const data = {
          name: agentForm.value.name,
          chatModelId: agentForm.value.chatModelId,
          sysPrompt: agentForm.value.sysPrompt,
          maxMessages: agentForm.value.maxMessages,
          tools: toolsStr,
          kbIds: agentForm.value.kbIds,
          graphIds: agentForm.value.graphIds,
          mcpIds: agentForm.value.mcpIds,
          skills: skillsStr,
          description: agentForm.value.description,
          status: agentForm.value.status,
        };
        createAgentAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("智能体创建成功");
          handleCloseAgentForm();
          handleGetAgentList();
        })
      } else {
        const data = {
          id:  agentForm.value.id,
          name: agentForm.value.name,
          chatModelId: agentForm.value.chatModelId,
          sysPrompt: agentForm.value.sysPrompt,
          maxMessages: agentForm.value.maxMessages,
          tools: toolsStr,
          kbIds: agentForm.value.kbIds,
          graphIds: agentForm.value.graphIds,
          mcpIds: agentForm.value.mcpIds,
          skills: skillsStr,
          description: agentForm.value.description,
          status: agentForm.value.status,
        };
        updateAgentAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("智能体修改成功");
          handleCloseAgentForm();
          handleGetAgentList();
        })
      }
    }
  });
}

/**
 * 关闭表单
 * */
function handleCloseAgentForm() {
  agentForm.value.id = undefined;
  agentForm.value.name = undefined;
  agentForm.value.chatModelId = undefined;
  agentForm.value.sysPrompt = undefined;
  agentForm.value.maxMessages = 10;
  agentForm.value.tools = [];
  agentForm.value.kbIds = [];
  agentForm.value.graphIds = [];
  agentForm.value.mcpIds = [];
  agentForm.value.skills = [];
  agentForm.value.description = undefined;
  agentForm.value.status = 1;
  agentFormTitle.value = "";
  agentFormVisible.value = false;
}

/**
 * 查询列表
 */
function handleGetAgentList() {
  pageAgentListAPI(agentQuery.value).then(res => {
    agentList.value = res.data.rows;
    total.value = res.data.total;
  })
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function handlePageChangeSize(pageSize) {
  agentQuery.value.pageSize = pageSize;
  handleGetAgentList();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handlePageChangeNo(pageNo) {
  agentQuery.value.pageNo = pageNo;
  handleGetAgentList();
}

/**
 * 多选排序
 * @param data
 */
function handleHeaderCellClass(data) {
  const property = data.column.property;
  const order = agentQuery.value.sorts[property];
  if (order === 'asc') {
    data.column.order = 'ascending';
  } else if (order === 'desc') {
    data.column.order = 'descending';
  } else {
    data.column.order = null;
  }
}

/**
 * 处理排序
 * @param column
 * @param prop
 * @param order
 */
function handleSortChange({ column, prop, order }) {
  if (order === 'ascending') {
    agentQuery.value.sorts[prop] = 'asc';
  } else if (order === 'descending') {
    agentQuery.value.sorts[prop] = 'desc';
  } else {
    agentQuery.value.sorts[prop] = null;
  }
  handleGetAgentList();
}
</script>

<style scoped lang="scss">
.form-tip {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  line-height: 1.6;
}

.drawer-footer {
  padding: 0 16px;
}
</style>

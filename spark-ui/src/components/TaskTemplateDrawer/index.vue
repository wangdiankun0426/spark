<template>
  <el-drawer
      :model-value="modelValue"
      :title="title"
      direction="ltr"
      size="80%"
      destroy-on-close
      :close-on-click-modal="false"
      @update:model-value="onUpdate"
  >
    <div class="template-container">
      <!--左侧任务模板列表-->
      <div class="template-left">
        <div class="panel-header">
          <el-button type="primary" @click="handleOpenCreateTemplate">
            <el-icon><Plus /></el-icon>新建任务模板
          </el-button>
          <el-input
              v-model="templateQuery.name"
              placeholder="请输入任务名称"
              clearable
              class="search-input"
              @keyup.enter="handleGetTemplateList"
          />
          <el-select
              v-model="templateQuery.taskType"
              placeholder="任务类型"
              clearable
              class="search-select"
              @change="handleGetTemplateList"
          >
            <el-option
                v-for="item in taskTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
          <el-button type="info" :icon="Search" @click="handleGetTemplateList">查询</el-button>
          <el-button type="warning" :icon="Refresh" @click="handleResetTemplateQuery">重置</el-button>
        </div>
        <el-table
            ref="templateTableRef"
            height="calc(100vh - 190px)"
            :data="templateList"
            highlight-current-row
            @current-change="handleSelectTemplate"
        >
          <el-table-column prop="id" label="编号" width="80" align="center" />
          <el-table-column prop="name" label="任务名称" align="center" />
          <el-table-column prop="taskTypeName" label="任务类型" width="150" align="center" />
          <el-table-column fixed="right" label="操作" width="150" align="center">
            <template #default="scope">
              <el-button
                  type="success"
                  text
                  @click.stop="handleOpenUpdateTemplate(scope.row)"
              >
                <el-icon><EditPen /></el-icon>
                <span style="font-size: 12px; font-weight: 500">修改</span>
              </el-button>
              <el-button
                  type="danger"
                  text
                  @click.stop="handleDeleteTemplate(scope.row.id)"
              >
                <el-icon><Delete /></el-icon>
                <span style="font-size: 12px; font-weight: 500">删除</span>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div>
          <el-pagination
              :current-page="templateQuery.pageNo"
              :page-size="templateQuery.pageSize"
              :page-sizes="pageSizes"
              :background="true"
              layout="total, sizes, prev, pager, next, jumper"
              :total="templateTotal"
              @size-change="s => { templateQuery.pageSize = s; handleGetTemplateList(); }"
              @current-change="p => { templateQuery.pageNo = p; handleGetTemplateList(); }"
          />
        </div>
      </div>
      <!--右侧任务模板参数列表-->
      <div class="template-right">
        <div class="panel-header">
          <el-button
              type="primary"
              :disabled="!selectedTemplate"
              @click="handleOpenCreateParam">
            <el-icon><Plus /></el-icon>新建参数
          </el-button>
        </div>
        <el-table
            height="calc(100vh - 190px)"
            :data="paramList"
            v-loading="paramLoading"
        >
          <el-table-column prop="name" label="参数名称" align="center" />
          <el-table-column prop="code" label="参数编码" align="center" />
          <el-table-column prop="typeName" label="参数类型" align="center" />
          <el-table-column fixed="right" label="操作" width="150" align="center">
            <template #default="scope">
              <el-button
                  type="success"
                  text
                  @click="handleOpenUpdateParam(scope.row)"
              >
                <el-icon><EditPen /></el-icon>
                <span style="font-size: 12px; font-weight: 500">修改</span>
              </el-button>
              <el-button
                  type="danger"
                  text
                  @click="handleDeleteParam(scope.row.id)"
              >
                <el-icon><Delete /></el-icon>
                <span style="font-size: 12px; font-weight: 500">删除</span>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!--任务模板表单-->
    <el-dialog
        v-model="templateFormVisible"
        :title="templateFormTitle"
        width="480px"
        :close-on-click-modal="false"
    >
      <el-form
          :model="templateForm"
          :rules="templateFormRules"
          ref="templateFormRef"
          label-width="auto"
      >
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="templateForm.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务类型" prop="taskType">
          <el-select v-model="templateForm.taskType" placeholder="请选择任务类型" style="width: 100%">
            <el-option v-for="item in taskTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="templateForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleSubmitTemplateForm">保存</el-button>
        <el-button @click="handleCloseTemplateForm">取消</el-button>
      </template>
    </el-dialog>

    <!--任务模板参数表单-->
    <el-dialog
        v-model="paramFormVisible"
        :title="paramFormTitle"
        width="480px"
        :close-on-click-modal="false"
    >
      <el-form
          :model="paramForm"
          :rules="paramFormRules"
          ref="paramFormRef"
          label-width="auto"
      >
        <el-form-item label="参数名称" prop="name">
          <el-input v-model="paramForm.name" placeholder="请输入参数名称" />
        </el-form-item>
        <el-form-item label="参数编码" prop="code">
          <el-input v-model="paramForm.code" placeholder="请输入参数编码" />
        </el-form-item>
        <el-form-item label="参数类型" prop="type">
          <el-select v-model="paramForm.type" placeholder="请选择参数类型" style="width: 100%">
            <el-option v-for="item in paramTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleSubmitParamForm">保存</el-button>
        <el-button @click="handleCloseParamForm">取消</el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script setup>
import { getCurrentInstance, nextTick, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Refresh, Plus, EditPen, Delete } from '@element-plus/icons-vue';
import {
  pageTaskTemplateListAPI,
  createTaskTemplateAPI,
  updateTaskTemplateAPI,
  deleteTaskTemplateAPI,
  listTaskTemplateParamAPI,
  createTaskTemplateParamAPI,
  updateTaskTemplateParamAPI,
  deleteTaskTemplateParamAPI,
} from '@/api/task/template';

defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '任务模板' }
})
const emit = defineEmits(['update:modelValue'])

/**
 * 外部 v-model 显隐控制
 * @param visible
 */
function onUpdate(visible) {
  emit('update:modelValue', visible)
}

const { proxy } = getCurrentInstance();
const templateTableRef = ref(null);
const templateQuery = ref({ pageNo: 1, pageSize: 30, name: undefined, taskType: undefined });
const templateTotal = ref(0);
const pageSizes = [30, 50, 100];
const templateList = ref([]);
const selectedTemplate = ref(null);
const paramList = ref([]);
const paramLoading = ref(false);

const taskTypeOptions = [
  { label: '流程催办任务', value: 1 },
  { label: '知识库文档归档任务', value: 2 },
];
const paramTypeOptions = [
  { label: '常量', value: 1 },
  { label: '表单数据', value: 2 },
];

const templateFormVisible = ref(false);
const templateFormTitle = ref('');
const templateForm = ref({ id: undefined, name: undefined, taskType: undefined, remark: undefined });
const templateFormRules = {
  name: [{ required: true, trigger: "blur", message: "请输入任务名称" }],
  taskType: [{ required: true, trigger: "change", message: "请选择任务类型" }],
};

const paramFormVisible = ref(false);
const paramFormTitle = ref('');
const paramForm = ref({ id: undefined, templateId: undefined, name: undefined, code: undefined, type: undefined });
const paramFormRules = {
  name: [{ required: true, trigger: "blur", message: "请输入参数名称" }],
  code: [{ required: true, trigger: "blur", message: "请输入参数编码" }],
  type: [{ required: true, trigger: "change", message: "请选择参数类型" }],
};

handleGetTemplateList();

/**
 * 分页查询任务模板列表
 */
function handleGetTemplateList() {
  pageTaskTemplateListAPI(templateQuery.value).then(res => {
    if (res.code === 200 && res.data) {
      templateList.value = res.data.rows || [];
      templateTotal.value = res.data.total;
      // 重新选中之前选中的模板，保持右侧参数面板联动
      nextTick(() => {
        if (selectedTemplate.value && selectedTemplate.value.id) {
          const match = templateList.value.find(item => item.id === selectedTemplate.value.id);
          templateTableRef.value.setCurrentRow(match || null);
          if (!match) {
            selectedTemplate.value = null;
            paramList.value = [];
          }
        }
      });
    }
  });
}

/**
 * 重置查询条件
 */
function handleResetTemplateQuery() {
  templateQuery.value = {
    pageNo: 1,
    pageSize: 30,
    name: undefined,
    taskType: undefined
  };
  handleGetTemplateList();
}

/**
 * 选中任务模板，加载其参数列表
 * @param row 当前选中行
 */
function handleSelectTemplate(row) {
  if (!row) {
    selectedTemplate.value = null;
    paramList.value = [];
    return;
  }
  selectedTemplate.value = row;
  handleGetParamList(row.id);
}

/**
 * 查询任务模板参数列表
 * @param templateId 模板id
 */
function handleGetParamList(templateId) {
  paramLoading.value = true;
  listTaskTemplateParamAPI({ templateId }).then(res => {
    if (res.code === 200 && res.data) {
      paramList.value = res.data;
    }
    paramLoading.value = false;
  });
}

/**
 * 打开创建任务模板表单
 */
function handleOpenCreateTemplate() {
  templateForm.value = {
    id: undefined,
    name: undefined,
    taskType: undefined,
    remark: undefined
  };
  templateFormTitle.value = "创建任务模板";
  templateFormVisible.value = true;
}

/**
 * 打开修改任务模板表单
 * @param row 模板数据
 */
function handleOpenUpdateTemplate(row) {
  templateForm.value = {
    id: row.id,
    name: row.name,
    taskType: row.taskType,
    remark: row.remark,
  };
  templateFormTitle.value = "修改任务模板";
  templateFormVisible.value = true;
}

/**
 * 提交任务模板表单（新增/修改）
 */
function handleSubmitTemplateForm() {
  proxy.$refs.templateFormRef.validate(valid => {
    if (!valid) {
      return;
    }
    const data = { ...templateForm.value };
    const api = data.id ? updateTaskTemplateAPI : createTaskTemplateAPI;
    api(data).then(res => {
      if (res.code !== 200) {
        return;
      }
      ElMessage.success(data.id ? "修改任务模板成功" : "创建任务模板成功");
      handleCloseTemplateForm();
      handleGetTemplateList();
    });
  });
}

/**
 * 关闭任务模板表单
 */
function handleCloseTemplateForm() {
  templateForm.value = { id: undefined, name: undefined, taskType: undefined, remark: undefined };
  templateFormTitle.value = "";
  templateFormVisible.value = false;
}

/**
 * 删除任务模板
 * @param id 模板id
 */
function handleDeleteTemplate(id) {
  ElMessageBox.confirm('是否确定删除此任务模板及其下参数?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteTaskTemplateAPI({ id }).then(res => {
      if (res.code !== 200) {
        return;
      }
      ElMessage.success("删除任务模板成功");
      if (selectedTemplate.value && selectedTemplate.value.id === id) {
        selectedTemplate.value = null;
        paramList.value = [];
      }
      handleGetTemplateList();
    });
  }).catch(() => {});
}

/**
 * 打开创建任务模板参数表单
 */
function handleOpenCreateParam() {
  if (!selectedTemplate.value) {
    ElMessage.warning("请先选择任务模板");
    return;
  }
  paramForm.value = {
    id: undefined,
    templateId: selectedTemplate.value.id,
    name: undefined,
    code: undefined,
    type: undefined,
  };
  paramFormTitle.value = "创建任务模板参数";
  paramFormVisible.value = true;
}

/**
 * 打开修改任务模板参数表单
 * @param row 参数数据
 */
function handleOpenUpdateParam(row) {
  paramForm.value = {
    id: row.id,
    templateId: row.templateId,
    name: row.name,
    code: row.code,
    type: row.type,
  };
  paramFormTitle.value = "修改任务模板参数";
  paramFormVisible.value = true;
}

/**
 * 提交任务模板参数表单（新增/修改）
 */
function handleSubmitParamForm() {
  proxy.$refs.paramFormRef.validate(valid => {
    if (!valid) {
      return;
    }
    const data = { ...paramForm.value };
    const api = data.id ? updateTaskTemplateParamAPI : createTaskTemplateParamAPI;
    api(data).then(res => {
      if (res.code !== 200) {
        return;
      }
      ElMessage.success(data.id ? "修改任务模板参数成功" : "创建任务模板参数成功");
      handleCloseParamForm();
      if (selectedTemplate.value) {
        handleGetParamList(selectedTemplate.value.id);
      }
    });
  });
}

/**
 * 关闭任务模板参数表单
 */
function handleCloseParamForm() {
  paramForm.value = {
    id: undefined,
    templateId: undefined,
    name: undefined,
    code: undefined,
    type: undefined,
  };
  paramFormTitle.value = "";
  paramFormVisible.value = false;
}

/**
 * 删除任务模板参数
 * @param id 参数id
 */
function handleDeleteParam(id) {
  ElMessageBox.confirm('是否确定删除此任务模板参数?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteTaskTemplateParamAPI({ id }).then(res => {
      if (res.code !== 200) {
        return;
      }
      ElMessage.success("删除任务模板参数成功");
      if (selectedTemplate.value) {
        handleGetParamList(selectedTemplate.value.id);
      }
    });
  }).catch(() => {});
}
</script>

<style scoped lang="scss">
.template-container {
  display: flex;
  height: 100%;
  gap: 16px;
}

.template-left {
  width: 60%;
  display: flex;
  flex-direction: column;
}

.template-right {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.search-input {
  width: 200px;
}

.search-select {
  width: 200px;
}
</style>

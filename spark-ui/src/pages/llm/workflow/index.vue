<template>
  <div class="app-container">
    <!--顶部头部-->
    <div class="wf-header">
      <div class="wf-header-left">
        <div class="wf-header-title">
          <el-icon class="wf-header-icon"><MagicStick /></el-icon>
          WORKFLOW
        </div>
        <div class="wf-header-subtitle">浏览AI工作流，快速运行或在线编辑</div>
      </div>
      <div class="wf-header-right">
        <el-input
            v-model="keyword"
            placeholder="搜索工作流名称"
            clearable
            :prefix-icon="Search"
            style="width: 300px"
        />
        <el-button type="primary" @click="handleOpenCreateForm">
          <el-icon><Plus /></el-icon>新增WorkFlow
        </el-button>
        <el-button @click="formTemplateVisible = true">
          <el-icon><Document /></el-icon>表单模板
        </el-button>
      </div>
    </div>

    <!--卡片网格-->
    <div
        class="wf-grid"
        v-if="workflowList.length"
    >
      <info-card
          v-for="item in workflowList"
          :key="item.id"
          :theme="getTheme(item)"
          :icon="MagicStick"
          :title="item.name"
          :id-text="'编号 #' + item.id"
          :description="item.description || '暂无描述'"
          :disabled="item.status !== 1"
          :height="280"
          @click="handleOpenRun(item)"
      >
        <!--状态徽章-->
        <template #badge>
          <span class="badge" :class="item.status === 1 ? 'badge-primary' : 'badge-muted'">
            {{ item.statusName }}
          </span>
        </template>

        <!--标签-->
        <template #tags>
          <el-tag size="small" effect="light" round>版本 {{ item.revNum || '-' }}</el-tag>
          <el-tag v-if="item.createdByName" size="small" effect="light" round>{{ item.createdByName }}</el-tag>
        </template>

        <!--底部操作-->
        <template #action>
          <span class="action-item" @click.stop="handleOpenRecord(item)">记录</span>
          <span class="action-item" @click.stop="handleOpenDesigner(item)">设计流程</span>
          <span class="action-item action-edit" @click.stop="handleOpenUpdateForm(item)">修改</span>
          <span class="action-item action-danger" @click.stop="handleDelete(item)">删除</span>
        </template>
      </info-card>
    </div>
    <el-empty
        class="empty-grid"
        v-else
        :description="keyword ? '未找到匹配的工作流' : '暂无工作流'"
        :image-size="120"
    />
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

    <!--快速运行抽屉-->
    <run-instance-drawer
        v-model="runVisible"
        :workflow="currentWorkflow"
        @success="handleRunSuccess"
    />

    <!--新增 / 修改工作流基本信息抽屉-->
    <el-drawer
        v-model="formVisible"
        :title="formTitle"
        direction="ltr"
        size="30%"
        :before-close="handleCloseForm"
    >
      <el-form :model="form" label-width="auto" :rules="formRules" ref="formRef">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入工作流名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="输入表单" prop="formId">
          <el-select v-model="form.formId" placeholder="请选择输入表单" clearable filterable style="width:100%">
            <el-option v-for="item in formOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width:100%">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button type="primary" @click="handleSubmitForm">保存</el-button>
          <el-button @click="handleCloseForm">取消</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 表单模板抽屉（复用管理端） -->
    <form-template-drawer v-model="formTemplateVisible" :type="4" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  pageWorkflowListAPI,
  createWorkflowAPI,
  updateWorkflowAPI,
  deleteWorkflowAPI,
  queryWorkflowDetailAPI
} from '@/api/workflow/template';
import { queryFormListAPI } from '@/api/form/form';
import RunInstanceDrawer from '@/components/WfRunInstanceDrawer/index.vue';
import FormTemplateDrawer from '@/components/FormTemplateDrawer/index.vue';
import InfoCard from '@/components/InfoCard/index.vue';
import { MagicStick, Search, Plus } from '@element-plus/icons-vue';

const router = useRouter();
const keyword = ref('');
const workflowList = ref([]);
const total = ref(0);
const pageSizes = [10, 30, 50];
// 分页查询条件
const query = ref({
  pageNo: 1,
  pageSize: 10,
});

const runVisible = ref(false);
const currentWorkflow = ref(null);

// 新增/修改表单
const formVisible = ref(false);
const formTitle = ref('');
const formRef = ref(null);
const form = ref(createEmptyForm());
const formRules = {
  name: [{ required: true, trigger: 'blur', message: '请输入工作流名称' }],
  formId: [{ required: true, trigger: 'blur', message: '请选择输入表单' }]
};

const formOptions = ref([]);
// 表单模板抽屉显隐
const formTemplateVisible = ref(false);
const statusOptions = [
  { label: '关闭', value: -1 },
  { label: '开启', value: 1 }
];

const themes = ['blue', 'green', 'purple', 'orange', 'cyan', 'pink', 'indigo'];
let searchTimer = null;

onMounted(() => {
  loadList();
  loadFormOptions();
});

onBeforeUnmount(() => {
  if (searchTimer) clearTimeout(searchTimer);
});

watch(keyword, () => {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    query.value.pageNo = 1;
    loadList();
  }, 300);
});

/**
 * 构造空表单对象
 */
function createEmptyForm() {
  return {
    id: undefined,
    name: undefined,
    description: undefined,
    status: -1,
    formId: undefined,
  };
}

/**
 * 加载工作流输入表单选项
 */
function loadFormOptions() {
  queryFormListAPI({ type: 4, page: false }).then(res => {
    if (res.code === 200 && res.data) {
      formOptions.value = res.data;
    }
  });
}

/**
 * 分页查询工作流列表，携带名称关键字
 */
async function loadList() {
  const params = {
    pageNo: query.value.pageNo,
    pageSize: query.value.pageSize,
    name: keyword.value || undefined,
  };
  const res = await pageWorkflowListAPI(params);
  if (res.code === 200 && res.data) {
    workflowList.value = res.data.rows || [];
    total.value = res.data.total || 0;
  }
}

/**
 * 切换每页条数，回到第一页重新查询
 * @param size
 */
function handleSizeChange(size) {
  query.value.pageSize = size;
  query.value.pageNo = 1;
  loadList();
}

/**
 * 切换页码重新查询
 * @param pageNo
 */
function handleCurrentChange(pageNo) {
  query.value.pageNo = pageNo;
  loadList();
}

function getTheme(item) {
  return themes[(item.id || 0) % themes.length];
}

/**
 * 打开快速运行抽屉
 * @param item
 */
function handleOpenRun(item) {
  currentWorkflow.value = item;
  runVisible.value = true;
}

function handleRunSuccess() {
  runVisible.value = false;
}

/**
 * 查看该工作流的运行记录
 * @param item
 */
function handleOpenRecord(item) {
  router.push({ path: '/llm/workflow/instance', query: { templateId: item.id } });
}

/**
 * 打开工作流设计器
 * @param item
 */
function handleOpenDesigner(item) {
  const revId = item.revId || 0;
  window.open('/workflow/designer/' + item.id + '/' + revId);
}

/**
 * 打开新建表单
 */
function handleOpenCreateForm() {
  form.value = createEmptyForm();
  formTitle.value = '新增 WorkFlow';
  formVisible.value = true;
}

/**
 * 打开修改表单，先拉取详情回填
 * @param item
 */
function handleOpenUpdateForm(item) {
  queryWorkflowDetailAPI({ id: item.id }).then(res => {
    if (res.code === 200 && res.data) {
      form.value = {
        id: res.data.id,
        name: res.data.name,
        description: res.data.description,
        status: res.data.status,
        formId: res.data.formId,
      };
      formTitle.value = '修改 WorkFlow';
      formVisible.value = true;
    }
  });
}

/**
 * 关闭表单
 */
function handleCloseForm() {
  form.value = createEmptyForm();
  if (formRef.value) {
    formRef.value.clearValidate();
  }
  formTitle.value = '';
  formVisible.value = false;
}

/**
 * 提交表单（新增 / 修改基本信息）
 */
function handleSubmitForm() {
  formRef.value.validate(valid => {
    if (!valid) return;
    const data = {
      id: form.value.id,
      name: form.value.name,
      description: form.value.description,
      status: form.value.status,
      formId: form.value.formId,
    };
    if (!data.id) {
      createWorkflowAPI(data).then(res => {
        if (res.code === 200) {
          ElMessage.success('创建成功');
          handleCloseForm();
          loadList();
        }
      });
    } else {
      updateWorkflowAPI(data).then(res => {
        if (res.code === 200) {
          ElMessage.success('修改成功');
          handleCloseForm();
          loadList();
        }
      });
    }
  });
}

/**
 * 删除工作流
 * @param item
 */
function handleDelete(item) {
  ElMessageBox.confirm('是否确定删除此Workflow?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteWorkflowAPI({ id: item.id }).then(res => {
      if (res.code === 200) {
        ElMessage.success('删除成功');
        // 删除当前页最后一条时回退上一页，避免停留在空页
        if (workflowList.value.length === 1 && query.value.pageNo > 1) {
          query.value.pageNo -= 1;
        }
        loadList();
      }
    });
  }).catch(() => {});
}
</script>

<style scoped lang="scss">
.wf-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  padding: $spacing-md $spacing-xl;
  background-color: $bg-card;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-card;
}

.wf-header-left {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.wf-header-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 20px;
  font-weight: 700;
  color: $color-text-primary;
}

.wf-header-icon {
  font-size: 24px;
  color: $color-primary;
}

.wf-header-subtitle {
  font-size: 13px;
  color: $color-text-secondary;
}

.wf-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.wf-grid {
  height: calc(100vh - #{$nav-height} - 190px);
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: $spacing-lg;
  align-content: start;
  :deep(.info-card-action) {
    gap: $spacing-sm;
    .action-edit {
      color: var(--el-color-warning);
    }
    .action-danger {
      color: var(--el-color-danger);
    }
  }
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
</style>

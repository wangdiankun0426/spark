<template>
  <div class="app-container">
    <!--顶部头部-->
    <div class="wf-header">
      <div class="wf-header-left">
        <div class="wf-header-title">
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
        <el-button
            @click="formTemplateVisible = true"
        >
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
          :icon="Workflow"
          :title="item.name"
          :description="item.description || '暂无描述'"
          :disabled="item.status !== 1"
          :actions="cardActions(item)"
      />
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

    <!--新增 / 修改工作流基本信息抽屉-->
    <el-drawer
        v-model="formVisible"
        :title="formTitle"
        direction="ltr"
        size="30%"
        :before-close="handleCloseForm"
        :close-on-click-modal="false"
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
          <el-switch
              v-model="form.status"
              :active-value="1"
              :inactive-value="-1"
              active-text="已启用"
              inactive-text="已停用"
              inline-prompt
          />
        </el-form-item>
      </el-form>
      <el-alert
          type="info"
          :closable="false"
          show-icon
      >
        <template #title>
          <div class="form-tip">
            <div>名称为必填项，用于在工作流列表中标识与检索；</div>
            <div>输入表单决定启动该工作流时需要用户填写的字段，请选择与该流程业务匹配的表单；</div>
            <div>描述建议写清该工作流的业务用途与适用场景，便于他人识别；</div>
            <div>停用的工作流不会出现在可启动列表中，已启动的实例不受影响。</div>
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

    <!-- 表单模板抽屉 -->
    <form-template-drawer
        v-model="formTemplateVisible"
        :type="4"
    />
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
} from '@/api/workflow/template.js';
import { queryFormListAPI } from '@/api/form/form.js';
import FormTemplateDrawer from '@/components/FormTemplateDrawer/index.vue';
import InfoCard from '@/components/InfoCard/index.vue';
import { Search, Plus } from '@element-plus/icons-vue';
import Workflow from "@/assets/icons/workflow.vue";

const router = useRouter();
const keyword = ref('');
const workflowList = ref([]);
const total = ref(0);
const pageSizes = [15, 30, 50];
// 分页查询条件
const query = ref({
  pageNo: 1,
  pageSize: 15,
});

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

/**
 * 卡片底部工具栏按钮
 * @param item
 */
function cardActions(item) {
  return [
    { key: 'record', label: '运行记录', icon: 'Files', onClick: () => handleOpenRecord(item) },
    { key: 'designer', label: '设计流程', icon: 'SetUp',onClick: () => handleOpenDesigner(item) },
    { key: 'edit', label: '修改', icon: 'Edit',onClick: () => handleOpenUpdateForm(item) },
    { key: 'delete', label: '删除', icon: 'Delete',onClick: () => handleDelete(item) }
  ];
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
  border-radius: $border-radius-md;
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

/* 抽屉底部表单填写说明 */
.form-tip {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  font-size: 12px;
  line-height: 1.6;
}
</style>

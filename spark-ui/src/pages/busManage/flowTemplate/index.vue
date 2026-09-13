<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="flow-header">
      <div class="flow-header-left">
        <div class="flow-header-title">
          流程模板
        </div>
      </div>
      <div class="flow-header-right">
        <el-input
            v-model="keyword"
            placeholder="搜索流程模板名称"
            clearable
            :prefix-icon="Search"
            style="width: 300px"
        />
        <el-button type="primary" @click="handleOpenCreateForm">
          <el-icon><Plus /></el-icon>新增流程模板
        </el-button>
        <el-button
            @click="formTemplateVisible = true"
        >
          <el-icon><Document /></el-icon>表单模板
        </el-button>
        <el-button
            @click="taskTemplateVisible = true"
        >
          <el-icon><List /></el-icon>任务模板
        </el-button>
      </div>
    </div>

    <!-- 流程模板卡片网格 -->
    <div
        class="flow-grid"
        v-if="templateList.length"
    >
      <info-card
          v-for="item in templateList"
          :key="item.id"
          :icon="FlowApplication"
          :title="item.name"
          :description="item.remark || '暂无备注'"
          :disabled="item.status !== 1"
          :actions="cardActions(item)"
      />
    </div>
    <!-- 空状态 -->
    <el-empty
        class="empty-grid"
        v-else
        :description="keyword ? '未找到匹配的流程模板' : '暂无流程模板'"
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

    <!-- 新增 / 修改 流程模板表单抽屉 -->
    <el-drawer
        v-model="templateFormVisible"
        :title="templateFormTitle"
        direction="ltr"
        size="30%"
        :before-close="handleCloseTemplateForm"
        :close-on-click-modal="false"
    >
      <el-form
          :model="templateForm"
          label-width="auto"
          :rules="templateFormRules"
          ref="templateFormRef"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="templateForm.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="表单" prop="formId">
          <el-select v-model="templateForm.formId" placeholder="请选择流程表单" style="width: 100%">
            <el-option
                v-for="item in formOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="templateForm.type" placeholder="请选择流程类型" style="width: 100%">
            <el-option
                v-for="item in typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
              v-model="templateForm.status"
              :active-value="1"
              :inactive-value="-1"
              active-text="已开启"
              inactive-text="已停用"
              inline-prompt
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
              v-model="templateForm.remark"
              type="textarea"
              placeholder="请输入备注"
              :rows="3"
          />
        </el-form-item>
      </el-form>
      <div class="form-tip">
        <el-alert type="info" :closable="false" show-icon>
          <template #title>
            <div class="form-tip-content">
              <div>已开启：流程模板可用，可发起新的流程实例。</div>
              <div>已停用：流程模板停用，无法发起新的流程实例，已有实例不受影响。</div>
              <div class="form-tip-warn">仅状态为"已停用"的流程模板可被删除，请谨慎操作。</div>
            </div>
          </template>
        </el-alert>
      </div>
      <template #footer>
        <div class="drawer-footer">
          <el-button type="primary" @click="handleSubmitTemplateDrawer">保存</el-button>
          <el-button @click="handleCloseTemplateForm">取消</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 表单模板抽屉 -->
    <form-template-drawer v-model="formTemplateVisible" :type="2" />
    <!-- 任务模板抽屉 -->
    <task-template-drawer v-model="taskTemplateVisible" />
  </div>
</template>

<script setup>
import {ref, watch, onMounted, onBeforeUnmount} from 'vue';
import {
  pageTemplateListAPI,
  createTemplateAPI,
  updateTemplateAPI,
  queryTemplateDetailAPI,
  deleteTemplateAPI
} from '@/api/flow/template.js';
import { queryFormListAPI } from '@/api/form/form.js';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Plus } from '@element-plus/icons-vue';
import FormTemplateDrawer from '@/components/FormTemplateDrawer/index.vue';
import TaskTemplateDrawer from '@/components/TaskTemplateDrawer/index.vue';
import InfoCard from '@/components/InfoCard/index.vue';
import { useRouter } from 'vue-router';
import FlowApplication from "@/assets/icons/flowApplication.vue";

const router = useRouter();

const templateList = ref([]);
const total = ref(0);
const pageSizes = [10, 30, 50];
const keyword = ref('');
// 分页查询条件
const query = ref({
  pageNo: 1,
  pageSize: 10,
});

// 新增/修改模板表单
const templateFormVisible = ref(false);
// 表单模板 / 任务模板 抽屉显隐
const formTemplateVisible = ref(false);
const taskTemplateVisible = ref(false);
const templateFormTitle = ref('');
const templateFormRef = ref(null);
const templateForm = ref(createEmptyTemplateForm());
const templateFormRules = {
  name: [{ required: true, trigger: 'blur', message: '请输入名称' }],
  formId: [{ required: true, trigger: 'change', message: '请选择流程表单' }],
  type: [{ required: true, trigger: 'change', message: '请选择流程类型' }],
  status: [{ required: true, trigger: 'change', message: '请选择状态' }],
};

const typeOptions = [
  { label: '普通流程', value: 1 },
  { label: '知识库归档流程', value: 2 },
];

const formOptions = ref([]);

let searchTimer = null;

onMounted(() => {
  loadTemplateList();
});

onBeforeUnmount(() => {
  if (searchTimer) {
    clearTimeout(searchTimer);
  }
});

/**
 * 名称搜索防抖，300ms 后回到第一页并重新查询
 */
watch(keyword, () => {
  if (searchTimer) {
    clearTimeout(searchTimer);
  }
  searchTimer = setTimeout(() => {
    query.value.pageNo = 1;
    loadTemplateList();
  }, 300);
});

/**
 * 构造空模板表单
 */
function createEmptyTemplateForm() {
  return {
    id: undefined,
    name: undefined,
    formId: undefined,
    type: 1,
    status: 1,
    remark: undefined,
  };
}

/**
 * 分页查询流程模板列表，携带名称关键字
 */
function loadTemplateList() {
  const params = {
    pageNo: query.value.pageNo,
    pageSize: query.value.pageSize,
  }
  if (keyword.value) {
    params.name = keyword.value;
  }
  pageTemplateListAPI(params).then(res => {
    if (res.code === 200 && res.data) {
      templateList.value = res.data.rows || [];
      total.value = res.data.total || 0;
    }
  });
}

/**
 * 切换每页条数，回到第一页重新查询
 * @param size
 */
function handleSizeChange(size) {
  query.value.pageSize = size;
  query.value.pageNo = 1;
  loadTemplateList();
}

/**
 * 切换页码重新查询
 * @param pageNo
 */
function handleCurrentChange(pageNo) {
  query.value.pageNo = pageNo;
  loadTemplateList();
}

/**
 * 卡片底部工具栏按钮
 * @param item
 */
function cardActions(item) {
  return [
    { key: 'instance', label: '流程实例', icon: 'Files', onClick: () => handleOpenInstance(item) },
    { key: 'designer', label: '设计流程', icon: 'SetUp',onClick: () => handleOpenDesigner(item) },
    { key: 'edit', label: '修改', icon: 'Edit',onClick: () => handleOpenUpdateForm(item) },
    { key: 'delete', label: '删除', icon: 'Delete',onClick: () => handleDelete(item) }
  ];
}

/**
 * 加载流程表单选项
 */
function loadTemplateFormOptions() {
  queryFormListAPI({ type: 2, page: false }).then(res => {
    if (res.code === 200 && res.data !== undefined) {
      formOptions.value = res.data.map(item => ({
        label: item.name,
        value: item.id,
      }));
    }
  });
}

/**
 * 打开新建模板表单
 */
function handleOpenCreateForm() {
  loadTemplateFormOptions();
  templateForm.value = createEmptyTemplateForm();
  templateFormTitle.value = '新建流程模板';
  templateFormVisible.value = true;
}

/**
 * 打开修改模板表单，先拉详情回填
 * @param item
 */
function handleOpenUpdateForm(item) {
  queryTemplateDetailAPI({ id: item.id }).then(res1 => {
    if (res1.code === 200 && res1.data) {
      const d = res1.data;
      loadTemplateFormOptions();
      templateForm.value = {
        id: d.id,
        name: d.name,
        formId: d.formId,
        type: d.type,
        status: d.status,
        remark: d.remark,
      };
      templateFormTitle.value = '修改流程模板';
      templateFormVisible.value = true;
    }
  });
}

/**
 * 关闭模板表单
 */
function handleCloseTemplateForm() {
  templateForm.value = createEmptyTemplateForm();
  if (templateFormRef.value) {
    templateFormRef.value.clearValidate();
  }
  templateFormTitle.value = '';
  templateFormVisible.value = false;
}

/**
 * 提交模板抽屉（新增 / 修改基本信息）
 */
function handleSubmitTemplateDrawer() {
  templateFormRef.value.validate(valid => {
    if (!valid) return;
    const data = {
      id: templateForm.value.id,
      name: templateForm.value.name,
      formId: templateForm.value.formId,
      type: templateForm.value.type,
      status: templateForm.value.status,
      remark: templateForm.value.remark,
    };
    if (!data.id) {
      createTemplateAPI(data).then(res => {
        if (res.code !== 200) return;
        ElMessage.success('流程模板创建成功');
        handleCloseTemplateForm();
        loadTemplateList();
      });
    } else {
      updateTemplateAPI(data).then(res => {
        if (res.code !== 200) return;
        ElMessage.success('流程模板修改成功');
        handleCloseTemplateForm();
        loadTemplateList();
      });
    }
  });
}

/**
 * 查看该流程模板的运行实例
 * @param item
 */
function handleOpenInstance(item) {
  router.push({ path: '/bus/flowInstance', query: { templateId: item.id } });
}

/**
 * 打开流程设计器（编辑模板，新开窗口）
 * @param item
 */
function handleOpenDesigner(item) {
  window.open('/flow/designer/' + item.id + '/' + (item.revId === undefined ? 0 : item.revId));
}

/**
 * 删除流程模板
 * @param item
 */
function handleDelete(item) {
  ElMessageBox.confirm('是否确定删除此条流程模板?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteTemplateAPI({ id: item.id }).then(res => {
      if (res.code !== 200) return;
      ElMessage.success('删除流程模板成功');
      if (templateList.value.length === 1 && query.value.pageNo > 1) {
        query.value.pageNo -= 1;
      }
      loadTemplateList();
    });
  }).catch(() => {});
}
</script>

<style scoped lang="scss">
.flow-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  padding: $spacing-md $spacing-xl;
  background-color: $bg-card;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.flow-header-left {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.flow-header-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 20px;
  font-weight: 700;
  color: $color-text-primary;
}

.flow-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.flow-grid {
  height: calc(100vh - #{$nav-height} - 160px);
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
}

.form-tip {
  margin-top: $spacing-md;
}

.form-tip-content {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  font-size: 12px;
  line-height: 1.6;
}

.form-tip-warn {
  color: $color-text-secondary;
  margin-top: $spacing-xs;
}
</style>

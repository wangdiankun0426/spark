<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-button type="primary" @click="handleOpenCreateForm">
        <el-icon><Plus /></el-icon>新建WorkFlow
      </el-button>
      <el-button type="warning" @click="handleResetQuery">
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button type="info" @click="handleGetList">
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--工作流列表-->
    <div>
      <el-table
          ref="tableRef"
          height="calc(100vh - 155px)"
          :data="tableList"
          highlight-current-row
          @sort-change="handleSortChange"
          :header-cell-style="handleHeaderCellClass"
      >
        <el-table-column prop="id" label="编号" width="120" align="center"/>
        <el-table-column prop="name" label="名称" align="center">
          <template #header>
            名称
            <el-popover :visible="searchFlag.name" placement="bottom" :width="200" trigger="click">
              <template #reference>
                <el-button :type="searchFlag.name ? 'primary':'info'" link :icon="Search"
                  @click.stop="searchFlag.name = !searchFlag.name" />
              </template>
              <div>
                <el-input v-model="query.name" placeholder="请输入名称" clearable @input="handleGetList" />
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="revNum" label="当前版本" width="80" align="center" />
        <el-table-column prop="statusName" label="状态" align="center" width="100">
          <template #header>
            状态
            <el-popover :visible="searchFlag.status" placement="bottom" :width="200" trigger="click">
              <template #reference>
                <el-button :type="searchFlag.status ? 'primary':'info'" link :icon="Search"
                  @click.stop="searchFlag.status = !searchFlag.status" />
              </template>
              <div>
                <el-select clearable v-model="query.status" @change="handleGetList" placeholder="请选择状态">
                  <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="createdByName" label="创建人" align="center" width="100"/>
        <el-table-column prop="createdDt" label="创建时间" width="160" align="center"/>
        <el-table-column prop="updatedByName" label="修改人" align="center" width="100"/>
        <el-table-column prop="updatedDt" label="修改时间" width="160" align="center"/>
        <el-table-column fixed="right" label="操作" width="360">
          <template #default="scope">
            <el-button type="primary" text @click="handleOpenDesigner(scope.row)">
              <el-icon><EditPen /></el-icon>
              <span style="font-size: 12px; font-weight: 400">编辑模板</span>
            </el-button>
            <el-button type="success" text @click="handleOpenUpdateForm(scope.row)">
              <el-icon><Edit /></el-icon>
              <span style="font-size: 12px; font-weight: 400">修改</span>
            </el-button>
            <el-button type="success" text @click="handleOpenRun(scope.row)" v-if="scope.row.status === 1">
              <el-icon><VideoPlay /></el-icon>
              <span style="font-size: 12px; font-weight: 400">运行</span>
            </el-button>
            <el-button type="danger" text @click="handleDelete(scope.row.id)">
              <el-icon><Delete /></el-icon>
              <span style="font-size: 12px; font-weight: 400">删除</span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!--分页组件-->
    <div>
      <el-pagination
          :current-page="query.pageNo"
          :page-size="query.pageSize"
          :page-sizes="pageSizes"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handlePageChangeSize"
          @current-change="handlePageChangeNo"
      />
    </div>
    <!--新建/修改抽屉-->
    <el-drawer
        v-model="formVisible"
        :title="formTitle"
        direction="ltr"
        size="30%"
        :before-close="handleCloseForm"
    >
      <el-form :model="form" label-width="auto" :rules="formRules" ref="formRef">
        <el-row :gutter="24">
          <el-col :span="24">
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
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleSubmitForm">保存</el-button>
          <el-button @click="handleCloseForm">取消</el-button>
        </div>
      </template>
    </el-drawer>
    <!--运行工作流抽屉（复用公共组件）-->
    <run-instance-drawer v-model="runVisible" :workflow="currentWorkflow" @success="handleRunSuccess" />
  </div>
</template>

<script setup>
import { getCurrentInstance, ref } from 'vue';
import { pageWorkflowListAPI, createWorkflowAPI, updateWorkflowAPI, deleteWorkflowAPI, queryWorkflowDetailAPI } from '@/api/workflow/template';
import { queryFormListAPI } from '@/api/form/form';
import RunInstanceDrawer from '@/components/WfRunInstanceDrawer/index.vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();
const query = ref({ pageNo: 1, pageSize: 30, name: undefined, status: undefined, sorts: {} });
const searchFlag = ref({ name: false, status: false });
const total = ref(0);
const pageSizes = [30, 50, 100];
const tableList = ref([]);
const formVisible = ref(false);
const formTitle = ref('');
const form = ref({ id: undefined, name: undefined, description: undefined, formId: undefined });
const formRules = { name: [{ required: true, trigger: 'blur', message: '请输入工作流名称' }] };
const formOptions = ref([]);
const statusOptions = [
  { label: '关闭', value: -1 },
  { label: '开启', value: 1 }
];

handleGetList();
// 加载AI工作流表单选项
queryFormListAPI({ type: 4, page: false }).then(res => {
  if (res.code === 200 && res.data) { formOptions.value = res.data; }
});

function handleDelete(id) {
  ElMessageBox.confirm('是否确定删除此AI工作流?', '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    deleteWorkflowAPI({ id }).then(res => {
      if (res.code === 200) { handleGetList(); ElMessage.success('删除成功'); }
    });
  }).catch(() => {});
}

function handleOpenUpdateForm(row) {
  queryWorkflowDetailAPI({ id: row.id }).then(res => {
    if (res.code === 200 && res.data) {
      form.value = { id: res.data.id, name: res.data.name, description: res.data.description, status: res.data.status, formId: res.data.formId };
      formTitle.value = '修改AI工作流';
      formVisible.value = true;
    }
  });
}

function handleOpenDesigner(row) {
  const revId = row.revId || 0;
  window.open('/workflow/designer/' + row.id + '/' + revId);
}

function handleOpenCreateForm() {
  form.value = { id: undefined, name: undefined, description: undefined, status: -1, formId: undefined };
  formTitle.value = '新建WorkFlow';
  formVisible.value = true;
}

function handleResetQuery() {
  query.value.pageNo = 1;
  query.value.name = undefined;
  searchFlag.value.name = false;
  query.value.status = undefined;
  searchFlag.value.status = false;
  let columns = proxy.$refs.tableRef.store.states.columns.value;
  columns.forEach(col => { col.order = null; });
  query.value.sorts = {};
  handleGetList();
}

function handleSubmitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      const data = { id: form.value.id, name: form.value.name, description: form.value.description, status: form.value.status, formId: form.value.formId };
      if (!form.value.id) {
        createWorkflowAPI(data).then(res => {
          if (res.code === 200) { ElMessage.success('创建成功'); handleCloseForm(); handleGetList(); }
        });
      } else {
        updateWorkflowAPI(data).then(res => {
          if (res.code === 200) { ElMessage.success('修改成功'); handleCloseForm(); handleGetList(); }
        });
      }
    }
  });
}

function handleCloseForm() {
  form.value = { id: undefined, name: undefined, description: undefined, status: -1, formId: undefined };
  formTitle.value = '';
  formVisible.value = false;
}

function handleGetList() {
  pageWorkflowListAPI(query.value).then(res => {
    if (res.code === 200 && res.data) { tableList.value = res.data.rows; total.value = res.data.total; }
  });
}

function handlePageChangeSize(pageSize) { query.value.pageSize = pageSize; handleGetList(); }
function handlePageChangeNo(pageNo) { query.value.pageNo = pageNo; handleGetList(); }

function handleHeaderCellClass(data) {
  const order = query.value.sorts[data.column.property];
  if (order === 'asc') data.column.order = 'ascending';
  else if (order === 'desc') data.column.order = 'descending';
  else data.column.order = null;
}

function handleSortChange({ prop, order }) {
  if (order === 'ascending') query.value.sorts[prop] = 'asc';
  else if (order === 'descending') query.value.sorts[prop] = 'desc';
  else query.value.sorts[prop] = null;
  handleGetList();
}

const runVisible = ref(false);
const currentWorkflow = ref(null);

function handleOpenRun(row) {
  currentWorkflow.value = row;
  runVisible.value = true;
}

function handleRunSuccess() {
  runVisible.value = false;
}
</script>

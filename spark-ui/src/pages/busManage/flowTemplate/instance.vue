<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="kd-header">
      <div class="kd-header-left">
        <el-button text @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
      </div>
      <div class="kd-header-right">
        <el-button type="warning" @click="handleResetQuery">
          <el-icon><Refresh /></el-icon>重置
        </el-button>
        <el-button type="info" @click="handleGetList">
          <el-icon><Search /></el-icon>查询
        </el-button>
      </div>
    </div>
    <!--流程实例列表-->
    <el-table
        ref="tableRef"
        height="calc(100vh - 200px)"
        :data="tableList"
        highlight-current-row
        @sort-change="handleSortChange"
        :header-cell-style="handleHeaderCellClass"
    >
      <el-table-column prop="name" label="流程标题" min-width="160" align="center">
        <template #header>
          流程标题
          <el-popover :visible="searchFlag.name" placement="bottom" :width="200" trigger="click">
            <template #reference>
              <el-button :type="searchFlag.name ? 'primary':'info'" link :icon="Search" @click.stop="searchFlag.name = !searchFlag.name" />
            </template>
            <div>
              <el-input v-model="query.name" placeholder="请输入流程标题" clearable @input="handleGetList" />
            </div>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column prop="levelName" label="紧急程度" width="120" align="center" />
      <el-table-column prop="statusName" label="状态" width="120" align="center">
        <template #header>
          状态
          <el-popover :visible="searchFlag.status" placement="bottom" :width="200" trigger="click">
            <template #reference>
              <el-button :type="searchFlag.status ? 'primary':'info'" link :icon="Search" @click.stop="searchFlag.status = !searchFlag.status" />
            </template>
            <div>
              <el-select clearable v-model="query.status" @change="handleGetList" placeholder="请选择状态">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </div>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column prop="createdByName" label="申请人" width="100" align="center" />
      <el-table-column prop="deptName" label="申请部门" width="120" align="center" />
      <el-table-column prop="createdDt" label="申请时间" width="160" align="center" />
      <el-table-column label="操作" width="330" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
              text
              @click="handleOpenDetail(row)"
          >
            <el-icon><View /></el-icon>
            <span style="font-size: 12px; font-weight: 500">
               详情
            </span>
          </el-button>
          <el-button
              type="warning"
              text
              @click="handleOpenTaskList(row)"
          >
            <el-icon><Tickets /></el-icon>
            <span style="font-size: 12px; font-weight: 500">
               任务
            </span>
          </el-button>
          <el-button
              type="success"
              text
              v-if="isAdmin && row.status === 2"
              v-debounce="() => handleAdminApproval(row, 3)"
          >
            <el-icon><Select /></el-icon>
            <span style="font-size: 12px; font-weight: 500">
               通过
            </span>
          </el-button>
          <el-button
              type="danger"
              text
              v-if="isAdmin && row.status === 2"
              v-debounce="() => handleAdminApproval(row, 4)"
          >
            <el-icon><CircleClose /></el-icon>
            <span style="font-size: 12px; font-weight: 500">
               驳回
            </span>
          </el-button>
          <el-button
              type="primary"
              text
              v-if="isAdmin && row.status === 2"
              v-debounce="() => handleOpenReplace(row)"
          >
            <el-icon><Switch /></el-icon>
            <span style="font-size: 12px; font-weight: 500">
               换人
            </span>
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!--分页-->
    <el-pagination
        :current-page="query.pageNo"
        :page-size="query.pageSize"
        :page-sizes="pageSizes"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="s => { query.pageSize = s; handleGetList(); }"
        @current-change="p => { query.pageNo = p; handleGetList(); }"
    />
    <!--流程实例详情抽屉-->
    <flow-detail-drawer
        :visible="detailVisible"
        :name="instanceName"
        :description="instanceDescription"
        :created-by-name="instanceApplicant"
        :dept-name="instanceDeptName"
        :level="instanceLevel"
        :formJson="formJson"
        :bpmJson="bpmJson"
        :nodes="nodes"
        :type="2"
        @close="handleCloseDetail"
    />
    <!--流程任务抽屉-->
    <el-drawer
        v-model="taskListVisible"
        :title="taskListTitle"
        direction="ltr"
        size="40%"
        :close-on-click-modal="false"
    >
      <el-table
          :data="taskList"
          highlight-current-row
          height="calc(100vh - 150px)"
      >
        <el-table-column prop="id" label="编号" width="80" align="center" />
        <el-table-column prop="taskTypeName" label="任务类型" width="140" align="center" />
        <el-table-column prop="statusName" label="任务状态" width="100" align="center" />
        <el-table-column prop="taskTime" label="下次执行时间" width="180" align="center" />
        <el-table-column prop="intervalHours" label="重复间隔(小时)" width="120" align="center" />
        <el-table-column prop="remark" label="备注" width="120"  align="center">
          <template #default="scope">
            <el-tooltip :content="scope.row.remark" placement="bottom" effect="dark">
              <span>{{ scope.row.remark }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="createdDt" label="创建时间" width="180" align="center" />
        <el-table-column fixed="right" label="操作" width="80" align="center">
          <template #default="scope">
            <el-button
                text
                @click="handleOpenTaskDetail(scope.row)"
            >
              <el-icon><View /></el-icon>
              <span style="font-size: 12px; font-weight: 500">
               详情
            </span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
          :current-page="taskQuery.pageNo"
          :page-size="taskQuery.pageSize"
          :page-sizes="taskPageSizes"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="taskTotal"
          @size-change="s => { taskQuery.pageSize = s; handleGetTaskList(); }"
          @current-change="p => { taskQuery.pageNo = p; handleGetTaskList(); }"
      />
    </el-drawer>
    <!--任务实例详情弹窗-->
    <task-instance-detail v-model="taskDetailVisible" :task-id="taskDetailId" />
    <!--替换审批人弹窗-->
    <el-dialog
        v-model="replaceVisible"
        title="替换审批人"
        width="560px"
        :close-on-click-modal="false"
    >
      <el-radio-group v-model="replaceMode" style="margin-bottom: 16px;">
        <el-radio label="oneToOne">一对一替换</el-radio>
        <el-radio label="batch">批量替换</el-radio>
      </el-radio-group>
      <div v-loading="replaceLoading" style="min-height: 120px;">
        <el-table
            v-if="replaceMode === 'oneToOne'"
            :data="replaceList"
            border
            max-height="320"
        >
          <el-table-column prop="assigneeName" label="原审批人" width="110" align="center" />
          <el-table-column prop="statusName" label="状态" width="90" align="center" />
          <el-table-column label="替换为" align="center">
            <template #default="{ row }">
              <select-user v-model="row.targetUserId" placeholder="不替换" style="width: 100%" />
            </template>
          </el-table-column>
        </el-table>
        <el-form v-else label-width="80px">
          <el-form-item label="新审批人">
            <select-user v-model="batchTargetIds" multiple clearable style="width: 100%" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="replaceVisible = false">取消</el-button>
        <el-button type="primary" :loading="replacing" @click="confirmReplace">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { getCurrentInstance, ref } from 'vue';
import {useRoute, useRouter} from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { pageInstanceListAPI, showInstanceDetailAPI, adminApprovalFlowInstanceAPI, queryInstanceAssigneeListAPI, replaceFlowInstanceAssigneeAPI } from '@/api/flow/instance.js';
import { pageTaskInstanceListAPI } from '@/api/task/instance.js';
import TaskInstanceDetail from '@/components/TaskInstanceDetail/index.vue';
import {ArrowLeft, CircleClose, Refresh, Search, Select, Switch} from '@element-plus/icons-vue';
import FlowDetailDrawer from '@/components/FlowDetailDrawer/index.vue';
import SelectUser from '@/components/SelectUser/index.vue';
import {isOrgAdmin, isSysAdmin} from '@/utils/utils.js';

const { proxy } = getCurrentInstance();
const route = useRoute();
const isAdmin = isSysAdmin() || isOrgAdmin();
const query = ref({
  pageNo: 1,
  pageSize: 30,
  name: undefined,
  status: undefined,
  // 从流程模板卡片「记录」进入时按对应模板过滤
  templateId: route.query.templateId || undefined,
  sorts: {}
});
const searchFlag = ref({ name: false, status: false });
const total = ref(0);
const pageSizes = [30, 50, 100];
const tableList = ref([]);
const statusOptions = [
  { label: '待处理', value: 1 },
  { label: '审批中', value: 2 },
  { label: '审批通过', value: 3 },
  { label: '审批驳回', value: 4 },
  { label: '自动通过', value: 5 },
];

handleGetList();

/**
 * 重置查询条件
 */
function handleResetQuery() {
  query.value = {
    pageNo: 1,
    pageSize: 30,
    name: undefined,
    status: undefined,
    templateId: route.query.templateId || undefined,
    sorts: {}
  };
  searchFlag.value.name = false;
  searchFlag.value.status = false;
  let columns = proxy.$refs.tableRef.store.states.columns.value;
  columns.forEach(col => { col.order = null; });
  handleGetList();
}

/**
 * 查询流程实例列表
 */
async function handleGetList() {
  query.value.templateId = route.query.templateId || undefined;
  const res = await pageInstanceListAPI(query.value);
  if (res.code === 200 && res.data) {
    tableList.value = res.data.rows || [];
    total.value = res.data.total;
  }
}

/**
 * 表头排序状态还原
 */
function handleHeaderCellClass(data) {
  const order = query.value.sorts[data.column.property];
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
 */
function handleSortChange({ prop, order }) {
  if (order === 'ascending') {
    query.value.sorts[prop] = 'asc';
  } else if (order === 'descending') {
    query.value.sorts[prop] = 'desc';
  } else {
    query.value.sorts[prop] = null;
  }
  handleGetList();
}

const detailVisible = ref(false);
const formJson = ref({});
const bpmJson = ref({});
const nodes = ref([]);
const instanceName = ref('');
const instanceDescription = ref('');
const instanceApplicant = ref('');
const instanceDeptName = ref('');
const instanceLevel = ref(1);

/**
 * 打开流程实例详情
 * @param row 行数据
 */
function handleOpenDetail(row) {
  showInstanceDetailAPI({ id: row.id }).then(res => {
    if (res.code === 200) {
      formJson.value = JSON.parse(res.data.formJson);
      bpmJson.value = JSON.parse(res.data.bpmJson);
      nodes.value = res.data.nodes;
      instanceName.value = res.data.name || '';
      instanceDescription.value = res.data.description || '';
      instanceApplicant.value = res.data.createdByName || '';
      instanceDeptName.value = res.data.deptName || '';
      instanceLevel.value = res.data.level;
      const values = res.data.values;
      if (values.length === 0) {
        detailVisible.value = true;
        return;
      }
      // 给模板中的字段赋值
      const codes = values.map(value => value.code);
      formJson.value.widgetList.forEach(widget => {
        const index = codes.indexOf(widget.config.code);
        if (index === -1) {
          return;
        }
        const value = values[index].value;
        const showValue = values[index].showValue;
        widget.config.value = value;
        widget.config.showValue = showValue;
      });
      detailVisible.value = true;
    }
  });
}

/**
 * 关闭流程实例详情
 */
function handleCloseDetail() {
  formJson.value = {};
  bpmJson.value = {};
  nodes.value = [];
  detailVisible.value = false;
}

const taskListVisible = ref(false);
const taskListTitle = ref('');
const taskList = ref([]);
const taskQuery = ref({ pageNo: 1, pageSize: 10, objId: undefined });
const taskTotal = ref(0);
const taskPageSizes = [10, 30, 50];
const taskDetailVisible = ref(false);
const taskDetailId = ref(undefined);

/**
 * 查询流程任务列表
 */
function handleGetTaskList() {
  pageTaskInstanceListAPI(taskQuery.value).then(res => {
    if (res.code === 200 && res.data) {
      taskList.value = res.data.rows || [];
      taskTotal.value = res.data.total;
    }
  });
}

/**
 * 打开流程任务列表抽屉
 * @param row 行数据
 */
function handleOpenTaskList(row) {
  taskQuery.value.pageNo = 1;
  taskQuery.value.objId = row.id;
  taskListTitle.value = '流程任务 - ' + (row.name || row.id);
  handleGetTaskList();
  taskListVisible.value = true;
}

/**
 * 打开任务实例详情弹窗
 * @param row 行数据
 */
function handleOpenTaskDetail(row) {
  taskDetailId.value = row.id;
  taskDetailVisible.value = true;
}

// 管理员干预审批动作配置
const adminApprovalConfig = {
  3: { title: '通过', successMessage: '审批通过成功' },
  4: { title: '驳回', successMessage: '审批驳回成功' }
};

/**
 * 管理员干预审批流程实例
 * @param row 行数据
 * @param status 审批状态 3通过 4驳回
 */
function handleAdminApproval(row, status) {
  const config = adminApprovalConfig[status];
  if (!isAdmin || row.status !== 2 || !config) {
    return;
  }
  ElMessageBox.prompt('请输入审批意见', config.title, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputProps: {
      rows: 4,
      placeholder: '选填'
    }
  }).then(({ value }) => {
    adminApprovalFlowInstanceAPI({
      id: row.id,
      status: status,
      discuss: value
    }).then(res => {
      if (res.code === 200) {
        ElMessage.success(config.successMessage);
        handleGetList();
      }
    }).catch(() => {});
  }).catch(() => {
    // 用户取消操作
  });
}

// 替换审批人弹窗状态
const replaceVisible = ref(false);
const replaceMode = ref('oneToOne');
const replaceLoading = ref(false);
const replacing = ref(false);
const replaceRow = ref({});
const replaceList = ref([]);
const batchTargetIds = ref([]);

/**
 * 打开替换审批人弹窗
 * @param row 行数据
 */
function handleOpenReplace(row) {
  if (!isAdmin || row.status !== 2) {
    return;
  }
  replaceRow.value = row;
  replaceMode.value = 'oneToOne';
  replaceList.value = [];
  batchTargetIds.value = [];
  replaceVisible.value = true;
  replaceLoading.value = true;
  queryInstanceAssigneeListAPI({ instanceId: row.id }).then(res => {
    if (res.code === 200) {
      const list = res.data || [];
      replaceList.value = list.map(item => ({ ...item, targetUserId: undefined }));
    }
    replaceLoading.value = false;
  }).catch(() => {
    replaceLoading.value = false;
  });
}

/**
 * 提交替换审批人
 */
function confirmReplace() {
  if (replaceMode.value === 'oneToOne') {
    const replacements = replaceList.value
        .filter(item => item.targetUserId)
        .map(item => ({ sourceId: item.id, targetUserId: item.targetUserId }));
    if (replacements.length === 0) {
      ElMessage.warning('请至少选择一位替换审批人');
      return;
    }
    const targetIds = replacements.map(item => item.targetUserId);
    if (new Set(targetIds).size !== targetIds.length) {
      ElMessage.warning('替换审批人不能重复');
      return;
    }
    handleSubmitReplace({ id: replaceRow.value.id, replacements });
    return;
  }
  if (batchTargetIds.value.length === 0) {
    ElMessage.warning('请选择新的审批人');
    return;
  }
  handleSubmitReplace({ id: replaceRow.value.id, assigneeIds: batchTargetIds.value });
}

/**
 * 调用替换审批人接口
 * @param data 提交参数
 */
function handleSubmitReplace(data) {
  replacing.value = true;
  replaceFlowInstanceAssigneeAPI(data).then(res => {
    if (res.code === 200) {
      ElMessage.success('替换审批人成功');
      replaceVisible.value = false;
      handleGetList();
    }
  }).catch(() => {}).finally(() => {
    replacing.value = false;
  });
}

const router = useRouter();

/**
 * 返回上一页
 */
function handleBack() {
  router.back();
}
</script>

<style scoped lang="scss">
.kd-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;
  padding: $spacing-md $spacing-lg;
  background-color: $bg-card;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.kd-header-left {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.kd-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

</style>

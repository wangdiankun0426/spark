<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-button
          type="warning"
          @click="handleResetInstanceQuery"
      >
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button
          type="info"
          @click="handleGetInstanceList"
      >
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--流程实例列表-->
    <div>
      <el-table
          ref="tableRef"
          height="calc(100vh - 155px)"
          :data="instanceList"
          highlight-current-row
          @row-click="handleRowClick"
      >
        <el-table-column prop="name" label="流程标题"  align="center"/>
        <el-table-column prop="levelName" label="紧急程度" align="center" width="180px"/>
        <el-table-column prop="statusName" label="状态"  align="center" width="180px"/>
        <el-table-column prop="createdByName" label="申请人" align="center" width="180px"/>
        <el-table-column prop="deptName" label="申请部门" align="center" width="180px"/>
        <el-table-column prop="createdDt" label="申请时间" align="center" width="220px"/>
      </el-table>
    </div>
    <!--分页组件-->
    <div>
      <el-pagination
          :current-page="instanceQuery.pageNo"
          :page-size="instanceQuery.pageSize"
          :page-sizes="pageSizes"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handlePageChangeSize"
          @current-change="handlePageChangeNo"
      />
    </div>

    <!--流程实例表单-->
    <flow-detail-drawer
        :visible="flowDetailVisible"
        :name="instanceName"
        :description="instanceDescription"
        :created-by-name="instanceApplicant"
        :dept-name="instanceDeptName"
        :level="instanceLevel"
        :formJson="formJson"
        :bpmJson="bpmJson"
        :nodes="nodes"
        :type="3"
        @close="handleCloseFlowDetail"
    >
      <template #footer>
        <div class="drawer-footer">
          <el-button
              v-if="nodeId !== undefined && (nodePermission & 1) === 1"
              @click="handleApprovalFlowInstance(3)"
              type="primary"
          >通过</el-button>
          <el-button
              v-if="nodeId !== undefined && (nodePermission & 2) === 2"
              @click="handleApprovalFlowInstance(4)"
              type="danger"
          >驳回</el-button>
          <el-button
              v-if="nodeId !== undefined && (nodePermission & 16) === 16"
              @click="openOperateDialog('transfer')"
              type="warning"
          >转办</el-button>
          <el-button
              v-if="nodeId !== undefined && (nodePermission & 32) === 32"
              @click="openOperateDialog('addSign')"
              type="primary"
          >加签</el-button>
        </div>
      </template>
    </flow-detail-drawer>

    <!-- 转办/加签选人弹窗 -->
    <el-dialog
        :title="dialogType === 'transfer' ? '转办' : '加签'"
        v-model="operateDialogVisible"
        width="400px"
        :close-on-click-modal="false"
    >
      <el-form label-width="70px">
        <el-form-item :label="dialogType === 'transfer' ? '转办人' : '加签人'">
          <select-user
              v-model="selectedUserIds"
              :multiple="dialogType === 'addSign'"
              clearable
              style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="operateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="operating" @click="confirmOperate">确定</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import {getCurrentInstance, ref, onMounted} from 'vue';
import {
  approvalFlowInstanceAPI,
  pageMyPendingListAPI, showInstanceDetailAPI,
  transferFlowInstanceAPI, addSignFlowInstanceAPI
} from '@/api/flow/instance';
import { Search } from '@element-plus/icons-vue';
import FlowDetailDrawer from '@/components/FlowDetailDrawer';
import SelectUser from '@/components/SelectUser/index.vue';
import {ElMessage, ElMessageBox} from "element-plus";
import { useRoute } from 'vue-router';

const { proxy } = getCurrentInstance();
const route = useRoute();
const instanceQuery = ref({
  pageNo: 1,
  pageSize: 30,
});
const total = ref(0);
const pageSizes = [30,50,100];
const instanceList = ref([]);

handleGetInstanceList();

// 若从消息跳转携带 id 参数，自动打开对应待办详情
onMounted(() => {
  const id = route.query.id;
  if (id) {
    handleOpenInstance(Number(id));
  }
});

/**
 * 重置查询条件
 * */
function handleResetInstanceQuery() {
  instanceQuery.value.pageNo = 1;
  instanceQuery.value.pageSize = 15;
  handleGetInstanceList();
}

/**
 * 关闭流程详情抽屉
 * */
function handleCloseFlowDetail() {
  formJson.value = {};
  bpmJson.value = {};
  nodes.value = [];
  nodeId.value = undefined;
  nodePermission.value = undefined;
  instanceId.value = undefined;
  flowDetailVisible.value = false;
  detailActiveTab.value = 'form';
  instanceName.value = '';
  instanceDescription.value = '';
  instanceApplicant.value = '';
  instanceDeptName.value = '';
  instanceLevel.value = 1;
}

/**
 * 审批流程实例
 * @param status 审批状态 3-通过 4-驳回
 */
const handleApprovalFlowInstance = (status) => {
  ElMessageBox.prompt('请输入审批意见', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputProps: {
      rows: 4
    },
    inputPattern: /\S/,
    inputErrorMessage: '审批原因不能为空'
  }).then(({ value }) => {
    const data = {
      id: instanceId.value,
      status: status,
      nodeId: nodeId.value,
      discuss: value
    }
    approvalFlowInstanceAPI(data).then(res => {
      if (res.code === 200) {
        ElMessage.success('审批成功');
        handleCloseFlowDetail();
        handleGetInstanceList();
      }
    })
  }).catch(() => {
    // 用户取消操作
  });
}

/**
 * 查询列表
 */
function handleGetInstanceList() {
  pageMyPendingListAPI(instanceQuery.value).then(res => {
    if (res.data !== undefined) {
      instanceList.value = res.data.rows;
      total.value = res.data.total;
    } else {
      instanceList.value = [];
      total.value = 0;
    }
  })
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function handlePageChangeSize(pageSize) {
  instanceQuery.value.pageSize = pageSize;
  handleGetInstanceList();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handlePageChangeNo(pageNo) {
  instanceQuery.value.pageNo = pageNo;
  handleGetInstanceList();
}

/**
 * 点击行打开对应流程
 * @param row
 */
function handleRowClick(row) {
  handleOpenInstance(row.id);
}

// 转办/加签弹窗状态
const operateDialogVisible = ref(false);
const dialogType = ref('transfer');
const selectedUserIds = ref(null);
const operating = ref(false);

/**
 * 打开转办/加签选人弹窗
 * @param type transfer-转办 addSign-加签
 */
function openOperateDialog(type) {
  dialogType.value = type;
  selectedUserIds.value = type === 'transfer' ? null : [];
  operateDialogVisible.value = true;
}

/**
 * 确认转办/加签
 */
function confirmOperate() {
  const ids = Array.isArray(selectedUserIds.value) ? selectedUserIds.value : [selectedUserIds.value];
  if (!ids.length) {
    ElMessage.warning(dialogType.value === 'transfer' ? '请选择转办人' : '请选择加签人');
    return;
  }
  const api = dialogType.value === 'transfer' ? transferFlowInstanceAPI : addSignFlowInstanceAPI;
  operating.value = true;
  api({
    id: instanceId.value,
    nodeId: nodeId.value,
    assigneeIds: ids
  }).then(res => {
    if (res.code === 200) {
      ElMessage.success(dialogType.value === 'transfer' ? '转办成功' : '加签成功');
      operateDialogVisible.value = false;
      handleCloseFlowDetail();
      handleGetInstanceList();
    }
  }).catch(() => {}).finally(() => {
    operating.value = false;
  });
}

const formJson = ref({});
const bpmJson = ref({});
const flowDetailVisible = ref(false);
const detailActiveTab = ref('form');
const nodes = ref([]);
const nodeId = ref(undefined);
const nodePermission = ref(undefined);
const instanceName = ref('');
const instanceDescription = ref('');
const instanceId = ref(undefined);
const instanceApplicant = ref('');
const instanceDeptName = ref('');
const instanceLevel = ref(1);

/**
 * 打开流程模板
 * @param id
 */
function handleOpenInstance(id) {
  const query = {
    id: id,
  }
  showInstanceDetailAPI(query).then(res => {
    if (res.code === 200) {
      formJson.value = JSON.parse(res.data.formJson);
      bpmJson.value = JSON.parse(res.data.bpmJson);
      nodes.value = res.data.nodes;
      instanceId.value = res.data.id;
      instanceName.value = res.data.name || '';
      instanceDescription.value = res.data.description || '';
      nodeId.value = res.data.nodeId;
      nodePermission.value = res.data.permission;
      instanceApplicant.value = res.data.createdByName || '';
      instanceDeptName.value = res.data.deptName || '';
      instanceLevel.value = res.data.level;
      const values = res.data.values;
      if (values.length === 0) {
        flowDetailVisible.value = true;
        return ;
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
      })
      flowDetailVisible.value = true;
    }
  }).catch(e => {})
}
</script>

<style scoped lang="scss">
:deep(.el-table__row) {
  cursor: pointer;
}
</style>


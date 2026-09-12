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
        <el-table-column prop="typeName" label="流程类型" align="center" width="180px"/>
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
        :type="2"
        @close="handleCloseFlowDetail"
    >
      <template #footer>
        <div class="drawer-footer">
          <el-button
              text
              v-if="nodeId !== undefined && instanceStatus === 2 && (nodePermission & 8) === 8"
              :loading="urging"
              type="warning"
              v-debounce="handleUrge"
              style="font-weight: 500; font-size: 14px"
          ><el-icon><BellFilled /></el-icon>催办</el-button>
          <el-button
              text
              v-if="instanceStatus === 2 && instanceCreatedBy === currentUserId && (nodePermission & 128) === 128"
              type="danger"
              v-debounce="handleRecall"
              style="font-weight: 500; font-size: 14px"
          ><el-icon><RefreshLeft /></el-icon>撤回</el-button>
          <el-button
              text
              v-if="instanceStatus === 9"
              type="primary"
              @click="handleReInitiate"
              style="font-weight: 500; font-size: 14px"
          ><el-icon><Refresh /></el-icon>重新发起</el-button>
        </div>
      </template>
    </flow-detail-drawer>

  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  pageMyApplicationListAPI, showInstanceDetailAPI, urgeFlowInstanceAPI, recallFlowInstanceAPI
} from '@/api/flow/instance';
import { Search } from '@element-plus/icons-vue';
import FlowDetailDrawer from '@/components/FlowDetailDrawer';
import { useRoute, useRouter } from 'vue-router';
import { useStore } from 'vuex';

const route = useRoute();
const router = useRouter();
const store = useStore();

const currentUserId = computed(() => {
  const userInfo = store.getters['user/getUserInfo'];
  return userInfo ? userInfo.id : undefined;
});

const instanceQuery = ref({
  pageNo: 1,
  pageSize: 30,
});
const total = ref(0);
const pageSizes = [30,50,100];
const instanceList = ref([]);

handleGetInstanceList();

// 若从消息跳转携带 id 参数，自动打开对应申请详情
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
  flowDetailVisible.value = false;
  detailActiveTab.value = 'form';
  instanceId.value = undefined;
  nodeId.value = undefined;
  nodePermission.value = undefined;
  instanceStatus.value = undefined;
  instanceCreatedBy.value = undefined;
  instanceName.value = '';
  instanceDescription.value = '';
  instanceApplicant.value = '';
  instanceDeptName.value = '';
  instanceLevel.value = 1;
  instanceTemplateId.value = undefined;
  instanceTemplateRevId.value = undefined;
  instanceFormId.value = undefined;
  instanceFormRevId.value = undefined;
  instanceValues.value = [];
}

/**
 * 查询列表
 */
function handleGetInstanceList() {
  pageMyApplicationListAPI(instanceQuery.value).then(res => {
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

// 催办按钮loading（防重复触发）
const urging = ref(false);

/**
 * 催办流程实例（向当前审批人发送催办通知）
 */
function handleUrge() {
  urging.value = true;
  urgeFlowInstanceAPI({ id: instanceId.value }).then(res => {
    if (res.code === 200) {
      ElMessage.success('催办通知已发送给当前审批人');
    }
  }).catch(() => {}).finally(() => {
    urging.value = false;
  });
}

/**
 * 撤回流程实例
 */
function handleRecall() {
  ElMessageBox.confirm('确定撤回该流程实例吗？撤回后流程将终止。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    recallFlowInstanceAPI({ id: instanceId.value }).then(res => {
      if (res.code === 200) {
        ElMessage.success('撤回成功');
        handleCloseFlowDetail();
        handleGetInstanceList();
      }
    });
  }).catch(() => {});
}

/**
 * 重新发起流程（基于原模板和表单数据创建新实例）
 */
function handleReInitiate() {
  const query = {
    templateId: instanceTemplateId.value,
    templateRevId: instanceTemplateRevId.value,
    formId: instanceFormId.value,
    formRevId: instanceFormRevId.value,
  };
  router.push({ path: '/home/flow/application', query });
}

const formJson = ref({});
const bpmJson = ref({});
const flowDetailVisible = ref(false);
const detailActiveTab = ref('form');
const nodes = ref([]);
const instanceId = ref(undefined);
const nodeId = ref(undefined);
const nodePermission = ref(undefined);
const instanceStatus = ref(undefined);
const instanceCreatedBy = ref(undefined);
const instanceName = ref('');
const instanceDescription = ref('');
const instanceApplicant = ref('');
const instanceDeptName = ref('');
const instanceLevel = ref(1);
const instanceTemplateId = ref(undefined);
const instanceTemplateRevId = ref(undefined);
const instanceFormId = ref(undefined);
const instanceFormRevId = ref(undefined);
const instanceValues = ref([]);

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
      nodeId.value = res.data.nodeId;
      nodePermission.value = res.data.permission;
      instanceStatus.value = res.data.status;
      instanceCreatedBy.value = res.data.createdBy;
      instanceDescription.value = res.data.description || '';
      instanceApplicant.value = res.data.createdByName || '';
      instanceDeptName.value = res.data.deptName || '';
      instanceLevel.value = res.data.level;
      instanceTemplateId.value = res.data.templateId;
      instanceTemplateRevId.value = res.data.templateRevId;
      instanceFormId.value = res.data.formId;
      instanceFormRevId.value = res.data.formRevId;
      instanceValues.value = res.data.values || [];
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

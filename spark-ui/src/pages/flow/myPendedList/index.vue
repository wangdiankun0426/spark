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
          height="calc(100vh - 165px)"
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
        :type="4"
        @close="handleCloseFlowDetail"
    />

  </div>
</template>

<script setup>
import {ref} from 'vue';
import {
  pageMyPendedListAPI,
  showInstanceDetailAPI
} from '@/api/flow/instance';
import { Search } from '@element-plus/icons-vue';
import FlowDetailDrawer from '@/components/FlowDetailDrawer';

const instanceQuery = ref({
  pageNo: 1,
  pageSize: 30,
});
const total = ref(0);
const pageSizes = [30,50,100];
const instanceList = ref([]);

handleGetInstanceList();

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
 * 查询列表
 */
function handleGetInstanceList() {
  pageMyPendedListAPI(instanceQuery.value).then(res => {
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

const formJson = ref({});
const bpmJson = ref({});
const flowDetailVisible = ref(false);
const detailActiveTab = ref('form');
const nodes = ref([]);
const nodeId = ref(undefined);
const instanceId = ref(undefined);
const instanceName = ref('');
const instanceDescription = ref('');
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
      nodeId.value = res.data.nodeId;
      instanceName.value = res.data.name || '';
      instanceDescription.value = res.data.description || '';
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
  height: 36px !important;
}
</style>


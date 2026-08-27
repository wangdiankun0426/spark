<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-button
          type="warning"
          @click="handleResetQuery"
      >
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button
          type="info"
          @click="handleGetList"
      >
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--抄送列表-->
    <div>
      <el-table
          ref="tableRef"
          height="calc(100vh - 155px)"
          :data="copyList"
          highlight-current-row
          @row-click="handleRowClick"
      >
        <el-table-column prop="name" label="流程标题" align="center"/>
        <el-table-column prop="levelName" label="紧急程度" align="center" width="180px"/>
        <el-table-column prop="statusName" label="状态" align="center" width="180px"/>
        <el-table-column prop="createdByName" label="抄送人" align="center" width="180px"/>
        <el-table-column prop="appByName" label="申请人" align="center" width="180px"/>
        <el-table-column prop="deptName" label="申请部门" align="center" width="180px"/>
        <el-table-column prop="createdDt" label="抄送时间" align="center" width="220px"/>
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

    <!--流程实例详情-->
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
        :type="5"
        @close="handleCloseFlowDetail"
    />

  </div>
</template>

<script setup>
import {ref} from 'vue';
import {
  pageCopyMyListAPI, showInstanceDetailAPI
} from '@/api/flow/instance';
import { Search } from '@element-plus/icons-vue';
import FlowDetailDrawer from '@/components/FlowDetailDrawer';

const query = ref({
  pageNo: 1,
  pageSize: 30,
});
const total = ref(0);
const pageSizes = [30,50,100];
const copyList = ref([]);

handleGetList();

/**
 * 重置查询条件
 */
function handleResetQuery() {
  query.value.pageNo = 1;
  query.value.pageSize = 30;
  handleGetList();
}

/**
 * 关闭流程详情抽屉
 */
function handleCloseFlowDetail() {
  formJson.value = {};
  bpmJson.value = {};
  nodes.value = [];
  flowDetailVisible.value = false;
  instanceName.value = '';
  instanceDescription.value = '';
  instanceApplicant.value = '';
  instanceDeptName.value = '';
  instanceLevel.value = 1;
}

/**
 * 查询列表
 */
function handleGetList() {
  pageCopyMyListAPI(query.value).then(res => {
    if (res.data !== undefined) {
      copyList.value = res.data.rows;
      total.value = res.data.total;
    } else {
      copyList.value = [];
      total.value = 0;
    }
  })
}

/**
 * 分页查询更改数量
 */
function handlePageChangeSize(pageSize) {
  query.value.pageSize = pageSize;
  handleGetList();
}

/**
 * 分页查询更改页码
 */
function handlePageChangeNo(pageNo) {
  query.value.pageNo = pageNo;
  handleGetList();
}

/**
 * 点击行打开对应流程
 */
function handleRowClick(row) {
  handleOpenInstance(row.instanceId);
}

const formJson = ref({});
const bpmJson = ref({});
const flowDetailVisible = ref(false);
const nodes = ref([]);
const instanceName = ref('');
const instanceDescription = ref('');
const instanceApplicant = ref('');
const instanceDeptName = ref('');
const instanceLevel = ref(1);

/**
 * 打开流程详情
 * @param id 流程实例id
 */
function handleOpenInstance(id) {
  showInstanceDetailAPI({ id }).then(res => {
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
        flowDetailVisible.value = true;
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
      flowDetailVisible.value = true;
    }
  }).catch(() => {});
}
</script>

<style scoped lang="scss">
:deep(.el-table__row) {
  cursor: pointer;
}
</style>

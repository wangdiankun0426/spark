<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-button type="warning" @click="handleResetQuery">
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button type="info" @click="handleGetList">
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--流程实例列表-->
    <el-table
        ref="tableRef"
        height="calc(100vh - 165px)"
        :data="tableList"
        highlight-current-row
        border
        @sort-change="handleSortChange"
        :header-cell-style="handleHeaderCellClass"
    >
      <el-table-column prop="id" label="编号" width="120" align="center" />
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
      <el-table-column prop="levelName" label="紧急程度" width="100" align="center" />
      <el-table-column prop="statusName" label="状态" width="100" align="center">
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
      <el-table-column label="操作" width="80" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" text size="small" @click="handleOpenDetail(row)">详情</el-button>
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
  </div>
</template>

<script setup>
import { getCurrentInstance, ref } from 'vue';
import { pageInstanceListAPI, showInstanceDetailAPI } from '@/api/flow/instance';
import { Search } from '@element-plus/icons-vue';
import FlowDetailDrawer from '@/components/FlowDetailDrawer';

const { proxy } = getCurrentInstance();
const query = ref({ pageNo: 1, pageSize: 30, name: undefined, status: undefined, sorts: {} });
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
</script>

<style scoped lang="scss">
</style>

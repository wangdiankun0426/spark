<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-button type="warning" @click="handleReset">
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button type="info" @click="loadList">
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--运行记录列表-->
    <el-table
        ref="tableRef"
        height="calc(100vh - 155px)"
        :data="list"
        highlight-current-row
        @sort-change="handleSortChange"
        :header-cell-style="handleHeaderCellClass"
    >
      <el-table-column prop="id" label="编号" width="120" align="center" />
      <el-table-column prop="templateName" label="工作流" min-width="160" align="center" />
      <el-table-column prop="revNum" label="版本" width="80" align="center" />
      <el-table-column prop="statusName" label="状态" width="100" align="center">
        <template #header>
          状态
          <el-popover :visible="searchFlag.status" placement="bottom" :width="200" trigger="click">
            <template #reference>
              <el-button :type="searchFlag.status ? 'primary':'info'" link :icon="Search" @click.stop="searchFlag.status = !searchFlag.status" />
            </template>
            <div>
              <el-select clearable v-model="query.status" @change="loadList" placeholder="请选择状态">
                <el-option label="运行中" :value="1" />
                <el-option label="成功" :value="2" />
                <el-option label="失败" :value="3" />
                <el-option label="超时" :value="4" />
              </el-select>
            </div>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column prop="durationMs" label="耗时" width="100" align="center">
        <template #default="{ row }">{{ row.durationMs != null ? row.durationMs + 'ms' : '-' }}</template>
      </el-table-column>
      <el-table-column prop="createdByName" label="触发人" width="100" align="center" />
      <el-table-column prop="createdDt" label="触发时间" width="160" align="center" />
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
        @size-change="s => { query.pageSize = s; loadList(); }"
        @current-change="p => { query.pageNo = p; loadList(); }"
    />
    <!--详情抽屉（复用公共组件）-->
    <instance-detail-drawer v-model="detailVisible" :instance-id="detailInstanceId" />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { pageInstanceHistoryAPI } from '@/api/workflow/instance';
import { Search } from '@element-plus/icons-vue';
import InstanceDetailDrawer from '@/components/WfInstanceDetailDrawer/index.vue';

const query = ref({ pageNo: 1, pageSize: 30, status: undefined, sorts: {} });
const searchFlag = ref({ status: false });
const total = ref(0);
const pageSizes = [30, 50, 100];
const list = ref([]);
const detailVisible = ref(false);
const detailInstanceId = ref(null);

function handleReset() {
  query.value = {
    pageNo: 1,
    pageSize: 30,
    status: undefined,
    sorts: {}
  };
  searchFlag.value.status = false;
  loadList();
}
async function loadList() {
  const res = await pageInstanceHistoryAPI(query.value);
  if (res.code === 200 && res.data) {
    list.value = res.data.rows || [];
    total.value = res.data.total;
  }
}
function handleOpenDetail(row) {
  detailInstanceId.value = row.id;
  detailVisible.value = true;
}
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
function handleSortChange({ prop, order }) {
  if (order === 'ascending') {
    query.value.sorts[prop] = 'asc';
  } else if (order === 'descending') {
    query.value.sorts[prop] = 'desc';
  } else {
    query.value.sorts[prop] = null;
  }
  loadList();
}
loadList();
</script>

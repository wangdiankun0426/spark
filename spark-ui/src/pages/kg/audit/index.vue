<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-button
          type="primary"
          :disabled="selectedIds.length === 0"
          @click="handleBatchAudit(1)"
      >
        <el-icon><Check /></el-icon>批量通过
      </el-button>
      <el-button
          type="danger"
          :disabled="selectedIds.length === 0"
          @click="handleBatchAudit(2)"
      >
        <el-icon><Close /></el-icon>批量拒绝
      </el-button>
      <el-button
          type="warning"
          @click="handleResetQuery"
      >
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button
          type="info"
          @click="handleGetEntityList"
      >
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--实体列表-->
    <div>
      <el-table
          ref="tableRef"
          height="calc(100vh - 155px)"
          :data="entityList"
          highlight-current-row
          @selection-change="handleSelectionChange"
          @sort-change="handleSortChange"
          :header-cell-style="handleHeaderCellClass"
          border
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="id" label="编号" width="80" align="center"/>
        <el-table-column prop="name" label="名称" min-width="100" align="left">
          <template #header>
            名称
            <el-popover
                :visible="searchFlag.name"
                placement="bottom"
                :width="200"
                trigger="click">
              <template #reference>
                <el-button
                    :type="searchFlag.name ? 'primary':'info'"
                    link
                    :icon="Search"
                    @click.stop="searchFlag.name = !searchFlag.name"
                />
              </template>
              <div>
                <el-input
                    v-model="query.name"
                    placeholder="请输入名称"
                    clearable
                    @input="handleGetEntityList"
                />
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="实体类型" min-width="100" align="center">
          <template #header>
            实体类型
            <el-popover
                :visible="searchFlag.type"
                placement="bottom"
                :width="200"
                trigger="click">
              <template #reference>
                <el-button
                    :type="searchFlag.type ? 'primary':'info'"
                    link
                    :icon="Search"
                    @click.stop="searchFlag.type = !searchFlag.type"
                />
              </template>
              <div>
                <el-input
                    v-model="query.type"
                    placeholder="请输入类型"
                    clearable
                    @input="handleGetEntityList"
                />
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="100" align="left" show-overflow-tooltip />
        <el-table-column prop="confidence" label="置信度" width="100" align="center"/>
        <el-table-column prop="sourceTypeName" label="来源" width="90" align="center"/>
        <el-table-column prop="graphName" label="所属图谱" width="140" align="center" />
        <el-table-column prop="auditStatusName" label="审核状态" width="120" align="center">
          <template #header>
            审核状态
            <el-popover
                :visible="searchFlag.auditStatus"
                placement="bottom"
                :width="200"
                trigger="click">
              <template #reference>
                <el-button
                    :type="searchFlag.auditStatus ? 'primary':'info'"
                    link
                    :icon="Search"
                    @click.stop="searchFlag.auditStatus = !searchFlag.auditStatus"
                />
              </template>
              <div>
                <el-select
                    v-model="query.auditStatus"
                    placeholder="请选择状态"
                    clearable
                    @change="handleGetEntityList"
                >
                  <el-option label="待审核" :value="0"/>
                  <el-option label="已通过" :value="1"/>
                  <el-option label="已拒绝" :value="2"/>
                </el-select>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="createdByName" label="创建人" width="110" align="center"/>
        <el-table-column prop="createdDt" label="创建时间" width="160" align="center"/>
        <el-table-column fixed="right" label="" width="140" align="center">
          <template #default="scope">
            <template v-if="scope.row.auditStatus === 0">
              <el-button
                  type="success"
                  text
                  @click="handleAudit(scope.row.id, 1)"
              >
                <el-icon><Check /></el-icon>
                <span style="font-size: 12px; font-weight: 400">通过</span>
              </el-button>
              <el-button
                  type="danger"
                  text
                  @click="handleAudit(scope.row.id, 2)"
              >
                <el-icon><Close /></el-icon>
                <span style="font-size: 12px; font-weight: 400">拒绝</span>
              </el-button>
            </template>
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
  </div>
</template>

<script setup>
import { getCurrentInstance, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Check, Close } from '@element-plus/icons-vue';
import {
  pageEntityListAPI,
  auditEntityAPI,
  batchAuditEntityAPI
} from '@/api/kg/entity.js';

const { proxy } = getCurrentInstance();

// 查询条件
const query = ref({
  pageNo: 1,
  pageSize: 30,
  name: undefined,
  type: undefined,
  auditStatus: 0,
  sorts: {},
});
const searchFlag = ref({
  name: false,
  type: false,
  auditStatus: false,
});
const total = ref(0);
const pageSizes = [30, 50, 100];
const entityList = ref([]);
const selectedIds = ref([]);

handleGetEntityList();

/**
 * 查询实体列表
 */
function handleGetEntityList() {
  pageEntityListAPI(query.value).then(res => {
    entityList.value = (res.data.rows || []);
    total.value = res.data.total;
  });
}

/**
 * 重置查询条件
 */
function handleResetQuery() {
  query.value.pageNo = 1;
  query.value.pageSize = 30;
  query.value.name = undefined;
  query.value.type = undefined;
  query.value.auditStatus = 0;
  searchFlag.value.name = false;
  searchFlag.value.type = false;
  searchFlag.value.auditStatus = false;
  const columns = proxy.$refs.tableRef.store.states.columns.value;
  columns.forEach((column) => {
    column.order = null;
  });
  query.value.sorts = {};
  selectedIds.value = [];
  handleGetEntityList();
}

/**
 * 多选变化
 * @param rows
 */
function handleSelectionChange(rows) {
  selectedIds.value = rows.map(row => row.id);
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function handlePageChangeSize(pageSize) {
  query.value.pageSize = pageSize;
  handleGetEntityList();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handlePageChangeNo(pageNo) {
  query.value.pageNo = pageNo;
  handleGetEntityList();
}

/**
 * 多选排序表头样式
 * @param data
 */
function handleHeaderCellClass(data) {
  const property = data.column.property;
  const order = query.value.sorts[property];
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
 * @param column
 * @param prop
 * @param order
 */
function handleSortChange({ column, prop, order }) {
  if (order === 'ascending') {
    query.value.sorts[prop] = 'asc';
  } else if (order === 'descending') {
    query.value.sorts[prop] = 'desc';
  } else {
    query.value.sorts[prop] = null;
  }
  handleGetEntityList();
}

/**
 * 审核单个实体
 * @param entityId
 * @param auditStatus
 */
function handleAudit(entityId, auditStatus) {
  const label = auditStatus === 1 ? '通过' : '拒绝';
  ElMessageBox.confirm(`是否确定${label}此条实体?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    auditEntityAPI({ entityId, auditStatus }).then(res => {
      if (res.code !== 200) return;
      ElMessage.success(`${label}成功`);
      handleGetEntityList();
    });
  }).catch(() => {});
}

/**
 * 批量审核实体
 * @param auditStatus
 */
function handleBatchAudit(auditStatus) {
  const label = auditStatus === 1 ? '通过' : '拒绝';
  ElMessageBox.confirm(`是否确定${label}选中的 ${selectedIds.value.length} 条实体?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    batchAuditEntityAPI({ entityIds: selectedIds.value.join(','), auditStatus }).then(res => {
      if (res.code !== 200) return;
      ElMessage.success(`批量${label}成功`);
      selectedIds.value = [];
      handleGetEntityList();
    });
  }).catch(() => {});
}
</script>

<style scoped lang="scss">
</style>

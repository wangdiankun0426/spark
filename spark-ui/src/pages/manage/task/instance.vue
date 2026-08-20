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
    <!--任务实例列表-->
    <el-table
        ref="tableRef"
        height="calc(100vh - 155px)"
        :data="tableList"
        highlight-current-row
    >
      <el-table-column prop="id" label="编号" width="100" align="center" />
      <el-table-column prop="taskTypeName" label="任务类型" width="140" align="center">
        <template #header>
          任务类型
          <el-popover :visible="searchFlag.taskType" placement="bottom" :width="200" trigger="click">
            <template #reference>
              <el-button :type="searchFlag.taskType ? 'primary':'info'" link :icon="Search" @click.stop="searchFlag.taskType = !searchFlag.taskType" />
            </template>
            <div>
              <el-select clearable v-model="query.taskType" @change="handleGetList" placeholder="请选择任务类型">
                <el-option v-for="item in taskTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </div>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column prop="statusName" label="任务状态" width="120" align="center">
        <template #header>
          任务状态
          <el-popover :visible="searchFlag.status" placement="bottom" :width="200" trigger="click">
            <template #reference>
              <el-button :type="searchFlag.status ? 'primary':'info'" link :icon="Search" @click.stop="searchFlag.status = !searchFlag.status" />
            </template>
            <div>
              <el-select clearable v-model="query.status" @change="handleGetList" placeholder="请选择任务状态">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </div>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column prop="objTypeName" label="业务对象类型" width="120" align="center" />
      <el-table-column prop="objId" label="业务对象id" width="100" align="center" />
      <el-table-column prop="taskTime" label="下次执行时间" width="180" align="center" />
      <el-table-column prop="intervalHours" label="重复间隔(小时)" width="120" align="center" />
      <el-table-column prop="remark" label="备注" align="center">
        <template #default="scope">
          <el-tooltip :content="scope.row.remark" placement="bottom" effect="dark">
            <span>{{ scope.row.remark }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="createdByName" label="创建人" width="100" align="center" />
      <el-table-column prop="createdDt" label="创建时间" width="180" align="center" />
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
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { pageTaskInstanceListAPI } from '@/api/task/instance';
import { Search } from '@element-plus/icons-vue';

const query = ref({ pageNo: 1, pageSize: 30, taskType: undefined, status: undefined });
const searchFlag = ref({ taskType: false, status: false });
const total = ref(0);
const pageSizes = [30, 50, 100];
const tableList = ref([]);
const taskTypeOptions = [
  { label: '流程催办任务', value: 1 },
  { label: '知识库文档归档任务', value: 2 },
];
const statusOptions = [
  { label: '待处理', value: 1 },
  { label: '成功', value: 2 },
  { label: '失败', value: 3 },
];

handleGetList();

/**
 * 重置查询条件
 */
function handleResetQuery() {
  query.value = {
    pageNo: 1,
    pageSize: 30,
    taskType: undefined,
    status: undefined
  };
  searchFlag.value.taskType = false;
  searchFlag.value.status = false;
  handleGetList();
}

/**
 * 查询任务实例列表
 */
function handleGetList() {
  pageTaskInstanceListAPI(query.value).then(res => {
    if (res.code === 200 && res.data) {
      tableList.value = res.data.rows || [];
      total.value = res.data.total;
    }
  });
}
</script>

<style scoped lang="scss">
</style>

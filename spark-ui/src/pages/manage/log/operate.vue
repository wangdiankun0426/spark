<template>
  <div class="app-container">
    <!--查询条件-->
    <el-card>
      <el-form :model="logQuery" label-width="auto">
        <el-row :gutter="24">
          <el-col :span="6">
            <el-form-item label="操作类型">
              <el-select v-model="logQuery.type" placeholder="选择选择操作类型" clearable filterable>
                <el-option
                    v-for="item in typeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                >
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态码">
              <el-input v-model="logQuery.code" clearable placeholder="选择输入状态码"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
    <div style="margin-top: 10px">
      <el-button type="warning" @click="resetLogQuery">
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button type="info" @click="getLogList">
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--日志列表-->
    <el-table
        :data="logList"
        highlight-current-row
        height="calc(100vh - 234px)">
      <el-table-column prop="id" label="编号" width="100" align="center"/>
      <el-table-column prop="typeName" label="操作类型" align="center"/>
      <el-table-column prop="consume" label="用时（毫秒）" align="center"/>
      <el-table-column prop="code" label="状态码" align="center"/>
      <el-table-column prop="remark" label="备注" align="center"/>
      <el-table-column prop="createdByName" label="操作人" align="center"/>
      <el-table-column prop="createdDt" label="操作时间" align="center"/>
    </el-table>
    <!--分页组件-->
    <div>
      <el-pagination
          :current-page="logQuery.pageNo"
          :page-size="logQuery.pageSize"
          :page-sizes="pageSizes"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="pageChangeSize"
          @current-change="pageChangeNo"
      />
    </div>
  </div>
</template>

<script setup>
import {pageLogOperateListAPI, logOperateTypeListAPI} from '@/api/log/logOperate.js';
import {ref} from 'vue';

const total = ref(0);
const logQuery = ref({
  pageNo: 1,
  pageSize: 30,
  type: undefined,
  code: undefined,
});
const pageSizes = [30,50,100];
const logList = ref([]);
const typeOptions = ref([]);

getLogOperateTypeList();
getLogList();

/**
 * 查询操作类型列表
 */
function getLogOperateTypeList() {
  logOperateTypeListAPI().then(res => {
    typeOptions.value = res.data;
  })
};

/**
 * 重置查询条件
 */
function resetLogQuery() {
  logQuery.value.pageNo = 1;
  logQuery.value.pageSize = 10;
  logQuery.value.type = undefined;
  logQuery.value.code = undefined;
  getLogList();
};

/**
 * 查询日志列表
 */
function getLogList() {
  pageLogOperateListAPI(logQuery.value).then(res => {
    logList.value = res.data.rows;
    total.value = res.data.total;
  })
};

/**
 * 分页查询更改数量
 * @param data
 */
function pageChangeSize(pageSize) {
  logQuery.value.pageSize = pageSize;
  getLogList();
};

/**
 * 分页查询更改页码
 * @param data
 */
function pageChangeNo(pageNo) {
  logQuery.value.pageNo = pageNo;
  this.getLogList();
}
</script>

<style scoped>

</style>

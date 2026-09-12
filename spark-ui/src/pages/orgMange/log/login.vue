<template>
  <div class="app-container">
    <!--查询条件-->
    <el-card>
      <el-form :model="logQuery" label-width="auto">
        <el-row :gutter="24">
          <el-col :span="6">
            <el-form-item label="IP地址">
              <el-input v-model="logQuery.ipaddress" clearable placeholder="选择输入ip地址"/>
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
        height="calc(100vh - 235px)"
    >
      <el-table-column prop="id" label="编号" width="100" align="center"/>
      <el-table-column prop="createdByName" label="登录人" align="center"/>
      <el-table-column prop="ipaddress" label="IP地址" align="center"/>
      <el-table-column prop="loginPlatformName" label="登录平台" align="center"/>
      <el-table-column prop="loginTypeName" label="登录类型" align="center"/>
      <el-table-column prop="sessionId" label="sessionId" width="270" align="center"/>
      <el-table-column prop="statusName" label="状态" align="center"/>
      <el-table-column prop="createdDt" label="登录时间" align="center"/>
      <el-table-column fixed="right" label="操作" width="100" align="center">
        <template #default="scope">
          <el-button
              text
              type="danger"
              @click="forceLogoutForm(scope.row.sessionId)"
              v-if="scope.row.status === 1"
          >
            <el-icon><SwitchButton /></el-icon>
            <span style="font-size: 12px; font-weight: 500">
               踢出
              </span>
          </el-button>
        </template>
      </el-table-column>
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
import {pageLogLoginListAPI} from '@/api/manage/log/logLogin.js';
import {ElMessage, ElMessageBox} from "element-plus";
import {forceLogoutAPI} from "@/api/manage/auth/login.js";
import {ref} from 'vue';

const total = ref(0);
const logQuery = ref({
 pageNo: 1,
 pageSize: 30,
 ipaddress: undefined
});
const pageSizes = [30,50,100];
const logList = ref([]);

getLogList();

/**
 * 重置查询条件
 */
function resetLogQuery() {
  logQuery.value.pageNo = 1;
  logQuery.value.pageSize = 10;
  logQuery.value.ipAddress = undefined;
  getLogList();
}

/**
 * 查询日志列表
 */
function getLogList() {
  pageLogLoginListAPI(logQuery.value).then(res => {
    logList.value = res.data.rows;
    total.value = res.data.total;
  })
}

/**
 * 分页查询更改数量
 * @param data
 */
function pageChangeSize(pageSize) {
  logQuery.value.pageSize = pageSize;
  getLogList();
}

/**
 * 分页查询更改页码
 * @param data
 */
function pageChangeNo(pageNo) {
  logQuery.value.pageNo = pageNo;
  getLogList();
}

/**
 * 强制退出
 */
function forceLogoutForm(sessionId) {
  ElMessageBox.confirm(
      '是否确定强制退出此用户?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    const data = {
      sessionId: sessionId
    };
    forceLogoutAPI(data).then(res => {
      getLogList();
      ElMessage.success("关闭成功");
    })
  }).catch(() => {})
}
</script>

<style scoped lang="scss">

</style>

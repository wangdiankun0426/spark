<template>
  <div class="app-container">
    <!--查询条件-->
    <el-form :model="chatMsgQuery" label-width="auto">
      <el-row :gutter="24">
        <el-col :span="6">
          <el-button
              type="warning"
              @click="handleResetChatMsgQuery">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
          <el-button
              type="info"
              @click="handleGetChatMsgList">
            <el-icon><Search /></el-icon>查询
          </el-button>
        </el-col>
      </el-row>
    </el-form>
    <!--聊天消息列表-->
    <el-table
        height="calc(100vh - 165px)"
        ref="tableRef"
        :data="chatMsgList"
        highlight-current-row
    >
      <el-table-column prop="id" label="编号" width="100" align="center"/>
      <el-table-column prop="message" label="内容" align="center"/>
      <el-table-column prop="senderName" label="发送人" width="100" align="center"/>
      <el-table-column prop="receiverName" label="接收人" width="100" align="center"/>
      <el-table-column prop="signStatusName" label="签收状态" width="100" align="center"/>
      <el-table-column prop="readStatusName" label="发送状态" width="100" align="center"/>
      <el-table-column prop="createdDt" label="发送时间" width="180" align="center"/>
    </el-table>
    <!--分页组件-->
    <div>
      <el-pagination
          :current-page="chatMsgQuery.pageNo"
          :page-size="chatMsgQuery.pageSize"
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
import {pageMsgListAPI} from '@/api/chat/msg';
import {ref} from 'vue';
import {Search} from "@element-plus/icons-vue";
const chatMsgQuery = ref({
  pageNo: 1,
  pageSize: 30,
});
const chatMsgList = ref([]);
const total = ref(0);
const pageSizes = [30,50,100];

handleGetChatMsgList();

/**
 * 查询列表
 */
function handleGetChatMsgList() {
  pageMsgListAPI(chatMsgQuery.value).then(res => {
    if (res.code !== 200) {
      return ;
    }
    chatMsgList.value = res.data.rows;
    total.value = res.data.total;
  });
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function handlePageChangeSize(pageSize) {
  chatMsgQuery.value.pageSize = pageSize;
  handleGetChatMsgList();
};

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handlePageChangeNo(pageNo) {
  chatMsgQuery.value.pageNo = pageNo;
  handleGetChatMsgList();
}

/**
 * 重置查询条件
 * */
function handleResetChatMsgQuery() {
  chatMsgQuery.value.pageNo = 1;
  chatMsgQuery.value.pageSize = 15;
  handleGetChatMsgList();
}
</script>

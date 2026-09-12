<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-button
          type="primary"
          @click="handleOpenSendMessageForm"
      >
        <el-icon><Promotion /></el-icon>发送消息
      </el-button>
      <el-button
          type="warning"
          @click="handleResetMessageQuery"
      >
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button
          type="info"
          @click="handleGetMessageList"
      >
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--消息列表-->
    <el-table
        :data="messageList"
        highlight-current-row
        height="calc(100vh - 165px)">
      <el-table-column prop="id" label="编号" width="100" align="center"/>
      <el-table-column prop="typeName" label="消息类型" width="140" align="center">
        <template #header>
          消息类型
          <el-popover :visible="searchFlag.type" placement="bottom" :width="200" trigger="click">
            <template #reference>
              <el-button :type="searchFlag.type ? 'primary':'info'" link :icon="Search"
                  @click.stop="searchFlag.type = !searchFlag.type" />
            </template>
            <div>
              <el-select
                  clearable
                  v-model="messageQuery.type"
                  @change="handleGetMessageList"
                  placeholder="请选择消息类型"
              >
                <el-option
                    v-for="item in messageTypeOption"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </div>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="消息标题" align="center">
        <template #header>
          消息标题
          <el-popover
              :visible="searchFlag.title"
              placement="bottom"
              :width="200"
              trigger="click"
          >
            <template #reference>
              <el-button :type="searchFlag.title ? 'primary':'info'" link :icon="Search"
                  @click.stop="searchFlag.title = !searchFlag.title" />
            </template>
            <div>
              <el-input v-model="messageQuery.title" placeholder="请输入消息标题" clearable @input="handleGetMessageList" />
            </div>
          </el-popover>
        </template>
        <template #default="scope">
          <el-tooltip :content="scope.row.title" placement="bottom" effect="dark">
            <span>{{ scope.row.title }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="消息内容" align="center">
        <template #default="scope">
          <el-tooltip :content="scope.row.content" placement="bottom" effect="dark">
            <span>{{ scope.row.content }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="userNames" label="接收人" width="220" align="center"/>
      <el-table-column prop="createdByName" label="创建人" align="center"/>
      <el-table-column prop="createdDt" label="创建时间" width="180" align="center"/>
      <el-table-column fixed="right" label="操作" width="180">
        <template #default="scope">
          <el-button
              text
              @click="handleViewMessage(scope.row)"
          >
            <el-icon><View /></el-icon>
            <span style="font-size: 12px; font-weight: 500">
               详情
            </span>
          </el-button>
          <el-button
              text
              type="danger"
              @click="handleDeleteMessage(scope.row)"
          >
            <el-icon><Delete /></el-icon>
            <span style="font-size: 12px; font-weight: 500">
               删除
            </span>
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!--分页组件-->
    <div>
      <el-pagination
          :current-page="messageQuery.pageNo"
          :page-size="messageQuery.pageSize"
          :page-sizes="pageSizes"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handlePageChangeSize"
          @current-change="handlePageChangeNo"
      />
    </div>
  </div>

  <!--发送消息抽屉-->
  <el-drawer
      v-model="messageFormVisible"
      title="发送消息"
      direction="ltr"
      size="30%"
      :before-close="handleCloseMessageForm"
  >
    <el-form :model="messageForm" label-width="auto" :rules="messageFormRules" ref="messageFormRef">
      <el-row :gutter="24">
        <el-col :span="24">
          <el-form-item label="消息标题" prop="title">
            <el-input v-model="messageForm.title" placeholder="请输入消息标题"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="24">
          <el-form-item label="消息内容" prop="content">
            <el-input
                v-model="messageForm.content"
                type="textarea"
                :rows="5"
                maxlength="512"
                show-word-limit
                placeholder="请输入消息内容"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="24">
          <el-form-item label="接收人" prop="userIds">
            <select-user
                v-model="messageForm.userIds"
                multiple
                placeholder="选择用户"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <!--必填字段填写提示-->
      <el-alert
          type="info"
          :closable="false"
          show-icon
      >
        <template #title>
          <div class="form-tip">
            <div>消息标题、内容与接收人均为必填项；</div>
            <div>消息类型固定为系统消息，发送后立即进入接收人的消息列表；</div>
            <div>消息内容最多 512 个字符。</div>
          </div>
        </template>
      </el-alert>
    </el-form>
    <template #footer>
      <div class="drawer-footer">
        <el-button type="primary" @click="handleSubmitMessageForm">发送</el-button>
        <el-button @click="handleCloseMessageForm">取消</el-button>
      </div>
    </template>
  </el-drawer>

  <!--消息详情抽屉-->
  <el-drawer
      v-model="messageDetailVisible"
      title="消息详情"
      direction="ltr"
      size="40%"
  >
    <el-descriptions :column="1" border>
      <el-descriptions-item label="编号">{{ messageDetail.id }}</el-descriptions-item>
      <el-descriptions-item label="消息类型">{{ messageDetail.typeName }}</el-descriptions-item>
      <el-descriptions-item label="消息标题">{{ messageDetail.title }}</el-descriptions-item>
      <el-descriptions-item label="消息内容">{{ messageDetail.content }}</el-descriptions-item>
      <el-descriptions-item label="接收人">{{ messageDetail.userNames }}</el-descriptions-item>
      <el-descriptions-item label="创建人">{{ messageDetail.createdByName }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ messageDetail.createdDt }}</el-descriptions-item>
    </el-descriptions>
  </el-drawer>

</template>

<script setup>
import {getCurrentInstance, ref} from 'vue';
import {pageMessageListAPI, sendMessageAPI, deleteMessageAPI} from '@/api/manage/sys/message.js';
import SelectUser from '@/components/SelectUser/index.vue';
import {ElMessage, ElMessageBox} from "element-plus";
import {Search} from '@element-plus/icons-vue';

const { proxy } = getCurrentInstance();
const messageQuery = ref({
  pageNo: 1,
  pageSize: 30,
  type: undefined,
  title: undefined
});
const searchFlag = ref({ type: false, title: false });
const total = ref(0);
const pageSizes = [30,50,100];
const messageList = ref([]);
const messageFormVisible = ref(false);
const messageForm = ref({
  title: undefined,
  content: undefined,
  userIds: []
});
const messageFormRules = {
  title: [{ required: true, trigger: "blur", message: "请输入消息标题" }],
  content: [{ required: true, trigger: "blur", message: "请输入消息内容" }],
  userIds: [{ required: true, trigger: "change", message: "请选择接收人" }]
};
const messageDetailVisible = ref(false);
const messageDetail = ref({});
const messageTypeOption = [
  {
    value: 1,
    label: '登录通知',
  },
  {
    value: 2,
    label: '流程待办通知',
  },
  {
    value: 3,
    label: '流程完结通知',
  },
  {
    value: 4,
    label: '流程驳回通知',
  },
  {
    value: 5,
    label: '流程催办通知',
  },
  {
    value: 6,
    label: '系统消息',
  }
];

handleGetMessageList();

/**
 * 删除消息
 * @param row 行数据
 * */
function handleDeleteMessage(row) {
  ElMessageBox.confirm(
      '是否确定删除此消息?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    const data = {
      id: row.id
    };
    deleteMessageAPI(data).then(res => {
      if (res.code !== 200) {
        return;
      }
      handleGetMessageList();
      ElMessage.success("删除消息成功");
    })
  }).catch(() => {})
}

/**
 * 打开消息详情
 * @param row 行数据
 * */
function handleViewMessage(row) {
  messageDetail.value = row;
  messageDetailVisible.value = true;
}

/**
 * 打开发送消息表单
 * */
function handleOpenSendMessageForm() {
  messageForm.value.title = undefined;
  messageForm.value.content = undefined;
  messageForm.value.userIds = [];
  messageFormVisible.value = true;
}

/**
 * 重置查询条件
 * */
function handleResetMessageQuery() {
  messageQuery.value.pageNo = 1;
  messageQuery.value.pageSize = 30;
  messageQuery.value.type = undefined;
  messageQuery.value.title = undefined;
  searchFlag.value.type = false;
  searchFlag.value.title = false;
  handleGetMessageList();
}

/**
 * 提交发送消息表单
 * */
function handleSubmitMessageForm() {
  proxy.$refs.messageFormRef.validate(valid => {
    if (valid) {
      const data = {
        title: messageForm.value.title,
        content: messageForm.value.content,
        userIds: messageForm.value.userIds.join(","),
      };
      sendMessageAPI(data).then(res => {
        if (res.code !== 200) {
          return ;
        }
        ElMessage.success("消息发送成功");
        handleCloseMessageForm();
        handleGetMessageList();
      })
    }
  });
}

/**
 * 关闭发送消息表单
 * */
function handleCloseMessageForm() {
  messageForm.value.title = undefined;
  messageForm.value.content = undefined;
  messageForm.value.userIds = [];
  messageFormVisible.value = false;
}

/**
 * 查询列表
 */
function handleGetMessageList() {
  pageMessageListAPI(messageQuery.value).then(res => {
    messageList.value = res.data.rows;
    total.value = res.data.total;
  })
}

/**
 * 分页查询更改数量
 * @param pageSize 每页数量
 */
function handlePageChangeSize(pageSize) {
  messageQuery.value.pageSize = pageSize;
  handleGetMessageList();
}

/**
 * 分页查询更改页码
 * @param pageNo 页码
 */
function handlePageChangeNo(pageNo) {
  messageQuery.value.pageNo = pageNo;
  handleGetMessageList();
}

</script>

<style scoped lang="scss">

</style>

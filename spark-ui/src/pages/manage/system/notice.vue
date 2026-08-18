<template>
  <div class="app-container">
    <!--查询条件-->
    <el-form :model="noticeQuery" label-width="auto">
      <el-row :gutter="24">
        <el-col :span="6">
          <el-button
              type="primary"
              @click="handleOpenCreateNoticeForm"
          >
            <el-icon><Plus /></el-icon>新建公告
          </el-button>
          <el-button
              type="warning"
              @click="handleResetNoticeQuery"
          >
            <el-icon><Refresh /></el-icon>重置
          </el-button>
          <el-button
              type="info"
              @click="handleGetNoticeList"
          >
            <el-icon><Search /></el-icon>查询
          </el-button>
        </el-col>
      </el-row>
    </el-form>
    <!--公告列表-->
    <el-table
        :data="noticeList"
        highlight-current-row
        height="calc(100vh - 155px)">
      <el-table-column prop="id" label="编号" width="100" align="center"/>
      <el-table-column prop="title" label="公告标题" align="center">
        <template #default="scope">
          <el-tooltip :content="scope.row.title" placement="bottom" effect="dark">
            <span>{{ scope.row.title }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="typeName" label="公告类型" align="center"/>
      <el-table-column prop="statusName" label="公告状态" align="center"/>
      <el-table-column prop="objNames" label="接受对象" width="300" align="center"/>
      <el-table-column prop="createdByName" label="创建人" align="center"/>
      <el-table-column prop="createdDt" label="创建时间" width="180" align="center"/>
      <el-table-column prop="updatedByName" label="修改人" align="center"/>
      <el-table-column prop="updatedDt" label="修改时间" width="180" align="center"/>
      <el-table-column fixed="right" label="操作" width="320">
        <template #default="scope">
          <el-button
              type="primary"
              text
              @click="handlePublishNotice(scope.row.id)"
              v-if="scope.row.status !== 2"
          >
            <el-icon><Top /></el-icon>
            <span style="font-size: 12px; font-weight: 400">
               发布
              </span>
          </el-button>
          <el-button
              type="info"
              text
              @click="handleDelistNotice(scope.row.id)"
              v-if="scope.row.status === 2"
          >
            <el-icon><Bottom /></el-icon>
            <span style="font-size: 12px; font-weight: 400">
               下架
              </span>
          </el-button>
          <el-button
              text
              @click="handleEditNotice(scope.row)"
              v-if="scope.row.status !== 2"
          >
            <el-icon><Edit /></el-icon>
            <span style="font-size: 12px; font-weight: 400">
               编辑
              </span>
          </el-button>
          <el-button
              text
              @click="handleViewNotice(scope.row)"
          >
            <el-icon><View /></el-icon>
            <span style="font-size: 12px; font-weight: 400">
               预览
              </span>
          </el-button>
          <el-button
              text
              type="success"
              @click="handleOpenUpdateNoticeForm(scope.row)"
              v-if="scope.row.status !== 2"
          >
            <el-icon><Edit /></el-icon>
            <span style="font-size: 12px; font-weight: 400">
               修改
              </span>
          </el-button>
          <el-button
              text
              type="danger"
              @click="handleDeleteNotice(scope.row)"
              v-if="scope.row.status !== 2"
          >
            <el-icon><Delete /></el-icon>
            <span style="font-size: 12px; font-weight: 400">
               删除
              </span>
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!--分页组件-->
    <div>
      <el-pagination
          :current-page="noticeQuery.pageNo"
          :page-size="noticeQuery.pageSize"
          :page-sizes="pageSizes"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handlePageChangeSize"
          @current-change="handlePageChangeNo"
      />
    </div>
  </div>

  <!--公告表单抽屉-->
  <el-drawer
      v-model="noticeFormVisible"
      :title="noticeFormTitle"
      direction="ltr"
      size="30%"
      :before-close="handleCloseNoticeForm"
  >
    <el-form :model="noticeForm" label-width="auto" :rules="noticeFormRules" ref="noticeFormRef">
      <el-row :gutter="24">
        <el-col :span="24">
          <el-form-item label="公告标题" prop="title">
            <el-input v-model="noticeForm.title" placeholder="请输入公告标题"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="24">
          <el-form-item label="公告类型" prop="type">
            <el-select
                v-model="noticeForm.type"
                clearable
                placeholder="选择公告类型"
            >
              <el-option
                  v-for="item in noticeTypeOption"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="24">
          <el-form-item label="接受对象" prop="objIds">
            <select-user
                v-model="userIds"
                multiple
                placeholder="选择用户"
            />
            <select-dept
                v-model="deptIds"
                multiple
                placeholder="选择部门"
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
            <div>公告标题为必填项，建议简明扼要概括公告主旨；</div>
            <div>公告类型为必选项，请按实际业务场景选择；</div>
            <div>接受对象可同时选择用户与部门，留空则默认全员可见；</div>
            <div>已发布状态的公告不可修改或删除，请先下架再调整；</div>
            <div>如需丰富内容排版，请在创建后通过"编辑"进入详情页维护正文。</div>
          </div>
        </template>
      </el-alert>
    </el-form>
    <template #footer>
      <div class="drawer-footer">
        <el-button type="primary" @click="handleSubmitNoticeForm">保存</el-button>
        <el-button @click="handleCloseNoticeForm">取消</el-button>
      </div>
    </template>
  </el-drawer>

</template>

<script setup>
import {getCurrentInstance, ref} from 'vue';
import {pageNoticeListAPI,createNoticeAPI, updateNoticeAPI, noticeDetailAPI, deleteNoticeAPI, delistNoticeAPI} from '@/api/system/notice';
import {pageUserListAPI} from '@/api/system/user';
import SelectUser from '@/components/SelectUser';
import SelectDept from '@/components/SelectDept';
import {ElMessage, ElMessageBox} from "element-plus";

const { proxy } = getCurrentInstance();
const noticeQuery = ref({
  pageNo: 1,
  pageSize: 30
});
const total = ref(0);
const pageSizes = [30,50,100];
const noticeList = ref([]);
const noticeFormVisible = ref(false);
const noticeFormTitle = ref('');
const noticeForm = ref({
  id: undefined,
  title: undefined,
  type: undefined,
});
// 接受对象：用户与部门分别维护，提交时合并为逗号串
const userIds = ref([]);
const deptIds = ref([]);
const noticeFormRules = {
  title: [{ required: true, trigger: "blur", message: "请输入公告标题" }],
  type: [{ required: true, trigger: "blur", message: "请选择公告类型" }],
};
const noticeTypeOption = [
  {
    value: 1,
    label: '任职公示',
  },
  {
    value: 2,
    label: '放假通知',
  }
];

handleGetNoticeList();

/**
 * 删除公告
 * @param id
 * */
function handleDeleteNotice(id) {
  ElMessageBox.confirm(
      '是否确定删除此公告?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    const data = {
      id: id
    };
    deleteNoticeAPI(data).then(res => {
      handleGetNoticeList();
      ElMessage.success("删除公告成功");
    })
  }).catch(() => {})
}

/**
 * 发布公告
 * */
function handlePublishNotice(id) {
  ElMessageBox.confirm(
      '是否确定发布此公告?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    const data = {
      id: id,
      status: 2,
      updateObjIds: false,
    };
    updateNoticeAPI(data).then(res => {
      handleGetNoticeList();
      ElMessage.success("公告发布成功");
    })
  }).catch(() => {})
}

/**
 * 下架公告
 * */
function handleDelistNotice(id) {
  ElMessageBox.confirm(
      '是否确定下架此公告?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    const data = {
      id: id
    };
    delistNoticeAPI(data).then(res => {
      if (res.code !== 200) {
        return;
      }
      handleGetNoticeList();
      ElMessage.success("公告下架成功");
    })
  }).catch(() => {})
}

/**
 * 打开修改公告表单
 * @param id
 */
function handleOpenUpdateNoticeForm(row) {
  const query = {
    id: row.id
  };
  noticeDetailAPI(query).then(async res => {
    noticeForm.value.id = res.data.id;
    noticeForm.value.title = res.data.title;
    noticeForm.value.type = res.data.type;
    // 后端返回的 objIds 是用户和部门混合数组，需通过用户列表区分
    const objIds = res.data.objIds !== undefined ? res.data.objIds : [];
    const userRes = await pageUserListAPI({ page: false });
    const userRows = userRes.code === 200 ? (userRes.data.rows || []) : [];
    const userIdSet = new Set(userRows.map(u => u.id));
    // 区分用户与部门：能匹配 userList 的为用户，其余为部门
    userIds.value = objIds.filter(id => userIdSet.has(Number(id)));
    deptIds.value = objIds.filter(id => !userIdSet.has(Number(id)));
    noticeFormTitle.value = "修改公告";
    noticeFormVisible.value = true;
  });
}

/**
 * 打开公告预览
 * */
function handleViewNotice(row) {
  window.open("/notice/view/"+row.id);
}

/**
 * 打开公告编辑
 * */
function handleEditNotice(row) {
  window.open("/manage/system/notice/edit/"+row.id);
}


/**
 * 打开创建表单
 * */
function handleOpenCreateNoticeForm() {
  noticeForm.value.id = undefined;
  noticeForm.value.title = undefined;
  noticeForm.value.type = undefined;
  userIds.value = [];
  deptIds.value = [];
  noticeFormTitle.value = "创建公告";
  noticeFormVisible.value = true;
}

/**
 * 重置查询条件
 * */
function handleResetNoticeQuery() {
  noticeQuery.value.pageNo = 1;
  noticeQuery.value.pageSize = 10;
  handleGetNoticeList();
}

/**
 * 提交表单
 * */
function handleSubmitNoticeForm() {
  proxy.$refs.noticeFormRef.validate(valid => {
    if (valid) {
      // 合并用户与部门 ID 为逗号串
      const objIdsStr = [...userIds.value, ...deptIds.value].join(",");
      if (!noticeForm.value.id) {
        const data = {
          title:  noticeForm.value.title,
          type:  noticeForm.value.type,
          objIds: objIdsStr,
        };
        createNoticeAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("公告创建成功");
          handleCloseNoticeForm();
          handleGetNoticeList();
        })
      } else {
        const data = {
          id:  noticeForm.value.id,
          title:  noticeForm.value.title,
          type:  noticeForm.value.type,
          objIds: objIdsStr,
          updateObjIds: true,
        };
        updateNoticeAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("公告修改成功");
          handleCloseNoticeForm();
          handleGetNoticeList();
        })
      }
    }
  });
}

/**
 * 关闭表单
 * */
function handleCloseNoticeForm() {
  noticeForm.value.id = undefined;
  noticeForm.value.title = undefined;
  noticeForm.value.type = undefined;
  userIds.value = [];
  deptIds.value = [];
  noticeFormTitle.value = "";
  noticeFormVisible.value = false;
}

/**
 * 查询列表
 */
function handleGetNoticeList() {
  pageNoticeListAPI(noticeQuery.value).then(res => {
    noticeList.value = res.data.rows;
    total.value = res.data.total;
  })
}

/**
 * 分页查询更改数量
 * @param data
 */
function handlePageChangeSize(pageSize) {
  noticeQuery.value.pageSize = pageSize;
  handleGetNoticeList();
}

/**
 * 分页查询更改页码
 * @param data
 */
function handlePageChangeNo(pageNo) {
  noticeQuery.value.pageNo = pageNo;
  handleGetNoticeList();
}

</script>

<style scoped>

</style>

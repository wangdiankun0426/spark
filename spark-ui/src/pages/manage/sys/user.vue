<template>
  <div class="app-container">
    <!--左侧部门树-->
    <div class="dept_tree_box">
      <el-card>
        <template #header>
          <div class="card-header">
            <div class="card-header-title">
              <el-icon><Histogram /></el-icon>
              <span>部门架构</span>
            </div>
            <el-button type="primary" link size="small" @click="getDeptTree">
              <el-icon><Refresh /></el-icon>
              <span>刷新</span>
            </el-button>
          </div>
        </template>
        <div style="height: calc(100vh - 126px); overflow-x: auto; overflow-y: auto">
          <el-tree
              :data="deptTree"
              @node-click="handleClickDeptNode"
              node-key="id"
              :default-expanded-keys="defaultExpandedKeys"
              highlight-current
              :props="{children: 'children', label: 'name' , value: 'id'}"
          />
        </div>
      </el-card>
    </div>

    <!--右侧用户列表-->
    <div class="user_list_box">
      <el-card>
        <!--查询条件-->
        <el-form :model="userQuery" label-width="auto">
          <el-row :gutter="24">
            <el-col :span="6">
              <el-form-item label="用户名">
                <el-input v-model="userQuery.name" clearable placeholder="选择输入用户名"/>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="登录名">
                <el-input v-model="userQuery.loginName" clearable placeholder="选择输入登录名"/>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="手机号">
                <el-input v-model="userQuery.phone" clearable placeholder="选择输入手机号"/>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="邮箱">
                <el-input v-model="userQuery.email" clearable placeholder="选择输入邮箱"/>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="性别">
                <el-select v-model="userQuery.sex" clearable placeholder="选择性别">
                  <el-option

                      v-for="item in sexOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="状态">
                <el-select v-model="userQuery.status" clearable placeholder="选择状态">
                  <el-option
                      v-for="item in statusOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
      <!--操作按钮-->
      <div style="margin-top: 10px">
        <el-button
            type="primary"
            @click="openCreateUserForm"
        >
          <el-icon><Plus /></el-icon>新建用户
        </el-button>
        <el-button
            type="success"
            :loading="syncLoading"
            @click="syncWeComOrganization"
        >
          <el-icon><Refresh /></el-icon>同步企微架构
        </el-button>
        <el-button
            type="warning"
            @click="resetUserQuery" style="float: right"
        >
          <el-icon><Refresh /></el-icon>重置
        </el-button>
        <el-button
            type="info"
            @click="getUserList"
            style="float: right"
        >
          <el-icon><Search /></el-icon>查询
        </el-button>
      </div>
      <!--用户列表-->
      <el-table
          :data="userList"
          highlight-current-row
          height="calc(100vh - 280px)"
      >
        <el-table-column label="" width="60" align="center">
          <template #default="scope">
            <user-avatar :user-id="scope.row.id" :size="36" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="用户名" width="80" align="center"/>
        <el-table-column prop="loginName" label="登录名" width="120" align="center"/>
        <el-table-column prop="sexName" label="性别" align="center"/>
        <el-table-column prop="statusName" label="状态" align="center"/>
        <el-table-column prop="phone" label="手机号" width="180" align="center"/>
        <el-table-column prop="email" label="邮箱" width="180" align="center"/>
        <el-table-column prop="createdByName" label="创建人" width="120" align="center"/>
        <el-table-column prop="createdDt" label="创建时间" width="180" align="center"/>
        <el-table-column prop="updatedByName" label="修改人" width="120" align="center"/>
        <el-table-column prop="updatedDt" label="修改时间" width="180" align="center"/>
        <el-table-column fixed="right" label="操作" width="140" align="center">
          <template #default="scope">
            <el-button
                type="success"
                text
                @click="openUpdateUserForm(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              <span style="font-size: 12px; font-weight: 400">
               修改
              </span>
            </el-button>
            <el-button
                type="danger"
                @click="deleteUser(scope.row.id)"
                text
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
            :current-page="userQuery.pageNo"
            :page-size="userQuery.pageSize"
            :page-sizes="pageSizes"
            :background="true"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="pageChangeSize"
            @current-change="pageChangeNo"
        />
      </div>
    </div>
  </div>

  <!--用户表单-->
  <el-drawer
      v-model="userFormVisible"
      :title="userFormTitle"
      direction="ltr"
      size="30%"
      :before-close="closeUserForm"
  >
    <el-form :model="userForm" label-width="auto" :rules="userFormRules" ref="userFormRef">
      <el-form-item label="用户名" prop="name">
        <el-input v-model="userForm.name" placeholder="请输入用户名"/>
      </el-form-item>
      <el-form-item label="登录名" prop="loginName">
        <el-input v-model="userForm.loginName" placeholder="请输入登录名"/>
      </el-form-item>
      <el-form-item label="用户部门" prop="deptId">
        <el-tree-select
            v-model="userForm.deptId"
            :data="deptTree"
            placeholder="选择用户部门"
            :props="{children: 'children', label: 'name' , value: 'id'}"
            check-strictly
            clearable
            :default-expanded-keys="defaultExpandedKeys"
        />
      </el-form-item>
      <el-form-item label="用户角色">
        <el-select v-model="userForm.roleIds" placeholder="选择用户角色" multiple>
          <el-option
              v-for="item in roleList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          >
            <span style="float: left">{{ item.name }}</span>
            <span style="float: right;color: var(--el-text-color-secondary);font-size: 13px; ">{{ item.dataScopeName }}</span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="userForm.phone" placeholder="请输入手机号"/>
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="userForm.email" placeholder="请输入邮箱"/>
      </el-form-item>
      <el-form-item label="性别" prop="sex">
        <el-radio-group v-model="userForm.sex">
          <el-radio
              v-for="item in sexOptions"
              :key="item.value"
              :label="item.value"
          >
            {{ item.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-switch
            v-model="userForm.status"
            :active-value="1"
            :inactive-value="-1"
            active-text="已启用"
            inactive-text="已停用"
            inline-prompt
        />
      </el-form-item>
      <!--必填字段填写提示-->
      <el-alert
          type="info"
          :closable="false"
          show-icon
      >
        <template #title>
          <div class="form-tip">
            <div>用户名、登录名、用户部门为必填项；</div>
            <div>登录名创建后不可修改，建议使用工号或姓名拼音；</div>
            <div>手机号需为 11 位有效号码，邮箱需符合标准邮箱格式；</div>
            <div>用户角色可多选，不选则该用户无任何权限；</div>
            <div>状态关闭后该用户将无法登录系统。</div>
          </div>
        </template>
      </el-alert>
    </el-form>
    <template #footer>
      <div class="drawer-footer">
        <el-button type="primary" @click="submitUserForm">保存</el-button>
        <el-button @click="closeUserForm">取消</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import {pageUserListAPI, createUserAPI, updateUserAPI, deleteUserAPI, userDetailAPI} from '@/api/manage/sys/user';
import {syncWeComOrganizationAPI} from '@/api/manage/external/weCom.js';
import {ElMessage, ElMessageBox} from "element-plus";
import {treeDeptAPI} from "@/api/manage/sys/dept";
import {pageRoleListAPI} from '@/api/manage/sys/role';
import {getCurrentInstance, ref} from 'vue';
import UserAvatar from '@/components/UserAvatar';

const { proxy } = getCurrentInstance();
const sexOptions = [
  {value: 1, label: '男'},
  {value: 2, label: '女'}
];
const statusOptions = [
  {value: 1, label: '开启'},
  {value: 0, label: '关闭'}
];

const deptTree = ref([]);
const userList = ref([]);
const userForm = ref({
  id: undefined,
  name: undefined,
  loginName: undefined,
  roleIds: undefined,
  deptId: undefined,
  phone: undefined,
  email: undefined,
  sex: undefined,
  status: undefined,
});
const userFormRules = {
  name: [{ required: true, trigger: "blur", message: "请输入用户名称" }],
  loginName: [{ required: true, trigger: "blur", message: "请输入登录名"}],
  deptId: [{ required: true, trigger: "blur", message: "请选择用户部门" }]
};
const userFormVisible = ref(false);
const userFormTitle = ref(undefined);
const total = ref(0);
const userQuery = ref({
  pageNo: 1,
  pageSize: 30,
  name: undefined,
  loginName: undefined,
  phone: undefined,
  email: undefined,
  sex: undefined,
  status: undefined
});
const pageSizes = [30,50,100];
const roleList = ref([]);
const currentDeptId = ref(undefined);
const defaultExpandedKeys = ref([]);
const syncLoading = ref(false);

getDeptTree();
getRoleList();
getUserList();


/**
 * 重置查询条件
 */
function resetUserQuery() {
   userQuery.value.pageNo = 1;
   userQuery.value.pageSize = 10;
   userQuery.value.name = undefined;
   userQuery.value.loginName = undefined;
   userQuery.value.phone = undefined;
   userQuery.value.email = undefined;
   userQuery.value.sex = undefined;
   userQuery.value.status = undefined;
   getUserList();
}

/**
 * 查询角色列表
 */
function getRoleList() {
  const query = {
    page: false
  };
  pageRoleListAPI(query).then(res => {
     roleList.value = res.data.rows;
  })
}

/**
 * 同步企业微信组织架构
 */
function syncWeComOrganization() {
  ElMessageBox.confirm(
      '同步企业组织架构将从企业微信拉取最新的部门与用户数据，是否继续？',
      '同步确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    syncLoading.value = true;
    syncWeComOrganizationAPI().then(res => {
      if (res.code !== 200) {
        return;
      }
      ElMessage.success("同步成功");
      getDeptTree();
      getUserList();
    }).finally(() => {
      syncLoading.value = false;
    });
  }).catch(() => {});
}

/**
 * 选中部门
 * @param data
 */
function handleClickDeptNode(data) {
   currentDeptId.value = data.id;
   getUserList();
}

/**
 * 查询部门树结构
 * */
function getDeptTree() {
  treeDeptAPI().then(res => {
     deptTree.value = res.data;
     if (res.data && res.data.length > 0) {
       defaultExpandedKeys.value = [res.data[0].id];
     }
  })
}

/**
 * 查询用户列表
 **/
function getUserList() {
  userQuery.value.deptId =  currentDeptId.value;
  pageUserListAPI(userQuery.value).then(res => {
     userList.value = res.data.rows;
     total.value = res.data.total;
  })
}

/**
 * 打开创建用户表单
 */
function openCreateUserForm() {
   userForm.value.id = undefined;
   userForm.value.name = undefined;
   userForm.value.loginName = undefined;
   userForm.value.roleIds = undefined;
   userForm.value.deptId = currentDeptId.value;
   userForm.value.phone = undefined;
   userForm.value.email = undefined;
   userForm.value.sex = 1;
   userForm.value.status = 1;
   userFormTitle.value = "创建用户";
   userFormVisible.value = true;
}

/**
 * 打开修改用户表单
 * @param row
 */
function openUpdateUserForm(row) {
  const query = {
    id: row.id
  };
  userDetailAPI(query).then(res => {
     userForm.value.id = res.data.id;
     userForm.value.name = res.data.name;
     userForm.value.loginName = res.data.loginName;
     userForm.value.deptId = res.data.deptId;
     userForm.value.roleIds = res.data.roleIds;
     userForm.value.phone = res.data.phone;
     userForm.value.email = res.data.email;
     userForm.value.sex = res.data.sex;
     userForm.value.status = res.data.status;
     userFormTitle.value = "修改用户";
     userFormVisible.value = true;
  });
}

/**
 * 提交用户表单
 */
function submitUserForm() {
  proxy.$refs.userFormRef.validate(valid => {
    if (valid) {
      if (!userForm.value.id) {
        const data = {
          name:  userForm.value.name,
          loginName:  userForm.value.loginName,
          deptId:  userForm.value.deptId,
          roleIds: userForm.value.roleIds !== undefined ?  userForm.value.roleIds.join(",") : undefined,
          phone:  userForm.value.phone,
          email:  userForm.value.email,
          sex:  userForm.value.sex,
          status:  userForm.value.status,
        };
        createUserAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("用户创建成功");
          closeUserForm();
          getUserList();
        })
      } else {
        const data = {
          id:  userForm.value.id,
          name:  userForm.value.name,
          loginName:  userForm.value.loginName,
          deptId:  userForm.value.deptId,
          roleIds:  userForm.value.roleIds !== undefined ?  userForm.value.roleIds.join(",") : undefined,
          phone:  userForm.value.phone,
          email:  userForm.value.email,
          sex:  userForm.value.sex,
          status:  userForm.value.status,
        };
        updateUserAPI(data).then(res => {
          if (res.code !== 200) {
            return ;
          }
          ElMessage.success("用户修改成功");
          closeUserForm();
          getUserList();
        })
      }
    }
  });
}

/**
 * 关闭用户表单弹窗
 */
function closeUserForm() {
   userForm.value.id = undefined;
   userForm.value.name = undefined;
   userForm.value.loginName = undefined;
   userForm.value.deptId = undefined;
   userForm.value.roleIds = undefined;
   userForm.value.phone = undefined;
   userForm.value.email = undefined;
   userForm.value.sex = undefined;
   userForm.value.status = undefined;
   userFormVisible.value = false;
}

/**
 * 删除用户
 * @param id
 * */
function deleteUser(id) {
  ElMessageBox.confirm(
      '是否确定删除此用户?',
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
    deleteUserAPI(data).then(res => {
      getUserList();
      ElMessage.success("删除用户成功");
    })
  }).catch(() => {})
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function pageChangeSize(pageSize) {
  userQuery.value.pageSize = pageSize;
  getUserList();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function pageChangeNo(pageNo) {
  userQuery.value.pageNo = pageNo;
  getUserList();
}
</script>

<style scoped lang="scss">
.dept_tree_box {
  border: 0;
  float: left;
  width: 19%;
  padding-right: 5px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header-title {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}
.user_list_box {
  border: 0;
  float: right;
  width: 80%;
}
.form-tip {
  font-size: 12px;
  line-height: 20px;
  color: $color-text-secondary;
  div {
    margin-bottom: 2px;
  }
}
</style>

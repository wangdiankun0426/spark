<template>
  <div class="app-container">
    <!--左侧角色列表-->
    <div class="role_box" style="float: left; width: 66%">
      <el-button
          type="primary"
          @click="openCreateRoleForm"
      >
        <el-icon><Plus /></el-icon>新建角色
      </el-button>
      <el-table
          :data="roleList"
          @current-change="selectRoleRow"
          highlight-current-row
          height="calc(100vh - 155px)">
        <el-table-column prop="name" label="角色名称" align="center"/>
        <el-table-column prop="dataScopeName" label="数据权限" width="300" align="center"/>
        <el-table-column prop="statusName" label="状态" align="center"/>
        <el-table-column prop="createdByName" label="创建人" align="center"/>
        <el-table-column prop="createdDt" label="创建时间" width="160" align="center"/>
        <el-table-column prop="updatedByName" label="修改人" align="center"/>
        <el-table-column prop="updatedDt" label="修改时间" width="160" align="center"/>
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="scope">
            <el-button
                type="success"
                text
                @click="openUpdateRoleForm(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              <span style="font-size: 12px; font-weight: 400">
               修改
              </span>
            </el-button>
            <el-button
                type="danger"
                @click="deleteRole(scope.row.id)"
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
            :current-page="roleQuery.pageNo"
            :page-size="roleQuery.pageSize"
            :page-sizes="rolePageSizes"
            :background="true"
            layout="total, sizes, prev, pager, next, jumper"
            :total="roleTotal"
            @size-change="rolePageChangeSize"
            @current-change="rolePageChangeNo"
        />
      </div>
    </div>
    <!--中间用户列表-->
    <div class="role_user_box" style="float: right;width: 33%;">
      <select-user
          v-model="selectedUserIds"
          multiple
          :disabled="currentRoleId == null"
          placeholder="请选择要添加的用户"
          @change="submitAddUser"
          style="width: 100%;"
      />
      <el-table
          :data="roleUserList"
          height="calc(100vh - 155px)">
        <el-table-column label="" width="60" align="center">
          <template #default="scope">
            <user-avatar :user-id="scope.row.userId" :size="36" />
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户名" width="80" align="center"/>
        <el-table-column prop="loginName" label="登录名" align="center"/>
        <el-table-column fixed="right" label="操作" width="100px">
          <template #default="scope">
            <el-button
                type="warning"
                text
                @click="removeUser(scope.row.userId)"
            >
              <el-icon><TurnOff /></el-icon>
              <span style="font-size: 12px; font-weight: 400">
               移除
              </span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!--分页组件-->
      <div>
        <el-pagination
            :current-page="roleUserQuery.pageNo"
            :page-size="roleUserQuery.pageSize"
            :page-sizes="roleUserPageSizes"
            :background="true"
            layout="total, sizes, prev, pager, next"
            :total="roleUserTotal"
            @size-change="roleUserPageChangeSize"
            @current-change="roleUserPageChangeNo"
        />
      </div>

    </div>
  </div>

  <!--角色表单抽屉-->
  <el-drawer
      v-model="roleFormVisible"
      :title="roleFormTitle"
      direction="ltr"
      size="30%"
      :before-close="closeRoleForm"
  >
    <el-form :model="roleForm" label-width="80px">
      <el-form-item label="角色名称">
        <el-input v-model="roleForm.name" placeholder="请输入角色名称"/>
      </el-form-item>
      <el-form-item label="数据权限">
        <el-select
            v-model="roleForm.dataScope"
            placeholder="选择数据权限"
        >
          <el-option
              v-for="item in dataScopeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-switch
            v-model="roleForm.status"
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
            <div>角色名称为必填项，建议体现职责范围，如"系统管理员"、"本部门员工"；</div>
            <div>数据权限决定该角色下用户可查看的数据范围，请按业务需要谨慎选择；</div>
            <div>"本部门及以下部门权限"包含本部门及其所有子部门的数据；</div>
            <div>角色创建后可在右侧用户列表中为该角色添加用户；</div>
            <div>删除角色前请先移除该角色下的所有用户；</div>
            <div>状态关闭后该角色将无法使用，已绑定该角色的用户将失去对应权限。</div>
          </div>
        </template>
      </el-alert>
    </el-form>
    <template #footer>
      <div class="drawer-footer">
        <el-button type="primary" @click="submitRoleForm">保存</el-button>
        <el-button @click="closeRoleForm">取消</el-button>
      </div>
    </template>
  </el-drawer>

  <!--选择用户组件-->

</template>

<script setup>
import {createRoleAPI, pageRoleListAPI, updateRoleAPI, deleteRoleAPI} from '@/api/sys/role.js';
import {pageRoleUserListAPI, addUserAPI, removeUserAPI} from '@/api/sys/roleUser.js';
import {ElMessage, ElMessageBox} from "element-plus";
import SelectUser from '@/components/SelectUser';
import {ref} from 'vue';
import UserAvatar from "@/components/UserAvatar/index.vue";

const dataScopeOptions = [
  {value: 1, label: '仅本人数据'},
  {value: 2, label: '仅本部门数据'},
  {value: 3, label: '本部门及以下部门数据'},
  {value: 4, label: '所有数据'},
];
const roleList = ref([]);
const roleForm = ref({
  id: undefined,
  name: undefined,
  dataScope: undefined,
  status: undefined,
});
const roleFormVisible = ref(false);
const roleFormTitle = ref('');
const roleUserList = ref([]);
const roleTotal = ref(0);
const roleUserTotal = ref(0);
const roleQuery = ref({
      pageNo: 1,
      pageSize: 30,
    });
const roleUserQuery = ref({
  pageNo: 1,
  pageSize: 30,
});
const rolePageSizes = [30,50,100];
const roleUserPageSizes = [30,50,100];
const currentRoleId = ref(null);
// 已选待添加用户 ID 数组（提交后清空，避免重复提交）
const selectedUserIds = ref([]);

getRoleList();

/**
 * 提交添加用户
 */
function submitAddUser(userIds) {
  const data = {
    roleId : currentRoleId.value,
    userIds: (userIds || []).join(",")
  };
  addUserAPI(data).then(res => {
    if (res.code !== 200) {
      return ;
    }
    ElMessage.success("添加成功");
    // 清空下拉选择，避免下次重复提交
    selectedUserIds.value = [];
    getRoleUserList();
  })
}

/**
 * 查询角色列表
 */
function getRoleList() {
  pageRoleListAPI(roleQuery.value).then(res => {
    roleList.value = res.data.rows;
    roleTotal.value = res.data.total;
  })
}

/**
 * 打开创建角色表单
 */
function openCreateRoleForm() {
  roleForm.value.name = undefined;
  roleForm.value.dataScope = 1;
  roleForm.value.status = 1;
  roleFormTitle.value = "创建角色";
  roleFormVisible.value = true;
}

/**
 * 打开修改角色表单
 */
function openUpdateRoleForm(data) {
  roleForm.value.id = data.id;
  roleForm.value.name = data.name;
  roleForm.value.dataScope = data.dataScope;
  roleForm.value.status = data.status;
  roleFormTitle.value = "修改角色";
  roleFormVisible.value = true;
}

/**
 * 关闭角色表单弹窗
 */
function closeRoleForm() {
  roleForm.value.name = undefined;
  roleForm.value.status = undefined;
  roleFormTitle.value = undefined;
  roleFormVisible.value = false;
}

/**
 * 提交角色表单
 */
function submitRoleForm() {
  if (!roleForm.value.id) {
    const data = {
      name: roleForm.value.name,
      dataScope: roleForm.value.dataScope,
      status: roleForm.value.status,
    };
    createRoleAPI(data).then(res => {
      ElMessage.success("角色创建成功");
      closeRoleForm();
      getRoleList();
    })
  } else {
    const data = {
      id: roleForm.value.id,
      name: roleForm.value.name,
      dataScope: roleForm.value.dataScope,
      status: roleForm.value.status,
    };
    updateRoleAPI(data).then(res => {
      ElMessage.success("角色修改成功");
      closeRoleForm();
      getRoleList();
    })
  }
}

/**
 * 删除角色
 * @param id
 * */
function deleteRole(id) {
  ElMessageBox.confirm(
      '是否确定删除此角色?',
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
    deleteRoleAPI(data).then(res => {
      getRoleList();
      ElMessage.success("删除成功");
    })
  })
  .catch(() => {
  })
}

/**
 * 角色单选框选中
 * @param row
 */
function selectRoleRow(row) {
  if (!row) {
    currentRoleId.value = null;
    roleUserList.value = [];
    roleUserTotal.value = 0;
    return;
  }
  currentRoleId.value = row.id;
  getRoleUserList();
}

function getRoleUserList() {
  roleUserQuery.value.roleId = currentRoleId.value;
  pageRoleUserListAPI(roleUserQuery.value).then(res => {
    roleUserList.value = res.data.rows;
    roleUserTotal.value = res.data.total;
  })
}

/**
 * 移除用户
 * @param id
 * */
function removeUser(id) {
  ElMessageBox.confirm(
      '是否确定移除此用户?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    const data = {
      roleId: currentRoleId.value,
      userId: id
    };
    removeUserAPI(data).then(res => {
      if (res.code !== 200) {
        return ;
      }
      getRoleUserList();
      ElMessage.success("移除成功");
    })
  })
  .catch(() => {
  })
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function rolePageChangeSize(pageSize) {
  roleQuery.value.pageSize = pageSize;
  getRoleList();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function rolePageChangeNo(pageNo) {
  roleQuery.value.pageNo = pageNo;
  getRoleList();
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function roleUserPageChangeSize(pageSize) {
  roleUserQuery.value.pageSize = pageSize;
  getRoleUserList();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function roleUserPageChangeNo(pageNo) {
  roleUserQuery.value.pageNo = pageNo;
  getRoleUserList();
}
</script>

<style scoped lang="scss">
.role_user_box .el-card {
  border: 0;
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

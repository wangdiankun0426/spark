<template>
  <div class="app-container">
    <!--左侧角色列表-->
    <div class="role_box" style="float: left; width: 66%">
      <el-button
          type="primary"
          v-hasPermi="['sys:role:create']"
          @click="openCreateRoleForm"
      >
        <el-icon><Plus /></el-icon>新建角色
      </el-button>
      <el-table
          :data="roleList"
          @current-change="selectRoleRow"
          highlight-current-row
          height="calc(100vh - 165px)">
        <el-table-column prop="name" label="角色名称" width="200" align="center"/>
        <el-table-column prop="dataScopeName" label="数据权限" width="300" align="center"/>
        <el-table-column prop="statusName" label="状态" align="center"/>
        <el-table-column prop="createdByName" label="创建人" width="120" align="center"/>
        <el-table-column prop="createdDt" label="创建时间" width="160" align="center"/>
        <el-table-column prop="updatedByName" label="修改人" width="120" align="center"/>
        <el-table-column prop="updatedDt" label="修改时间" width="160" align="center"/>
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="scope">
            <el-button
                type="success"
                text
                v-hasPermi="['sys:role:update']"
                @click="openUpdateRoleForm(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              <span style="font-size: 12px; font-weight: 500">
               修改
              </span>
            </el-button>
            <el-button
                type="danger"
                v-hasPermi="['sys:role:delete']"
                @click="deleteRole(scope.row.id)"
                text
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
    <!--右边用户列表-->
    <div class="role_user_box" style="float: right;width: 33%;">
      <select-user
          v-model="selectedUserIds"
          multiple
          :disabled="currentRoleId == null"
          placeholder="请选择要添加的用户"
          v-hasPermi="['sys:roleUser:add']"
          @change="submitAddUser"
          style="width: 100%;"
      />
      <el-table
          :data="roleUserList"
          height="calc(100vh - 165px)"
      >
        <el-table-column label="" width="60" align="center">
          <template #default="scope">
            <user-avatar :user-id="scope.row.userId" :size="36" />
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户名" width="120" align="center"/>
        <el-table-column prop="loginName" label="登录名" align="center"/>
        <el-table-column fixed="right" label="操作" width="100px">
          <template #default="scope">
            <el-button
                type="warning"
                text
                v-hasPermi="['sys:roleUser:remove']"
                @click="removeUser(scope.row.userId)"
            >
              <el-icon><TurnOff /></el-icon>
              <span style="font-size: 12px; font-weight: 500">
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
      size="40%"
      :before-close="closeRoleForm"
      :close-on-click-modal="false"
  >
    <el-form ref="roleFormRef" :model="roleForm" :rules="roleFormRules" label-width="80px">
      <el-form-item label="角色名称" prop="name">
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
      <el-form-item v-if="roleForm.dataScope === 5" label="数据部门">
        <select-dept
            v-model="roleForm.deptIds"
            multiple
            :check-strictly="true"
            placeholder="请选择自定义数据范围的部门"
        />
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
      <el-form-item label="菜单权限">
        <div class="menu-perm-box">
          <div class="menu-perm-toolbar">
            <el-button link type="primary" size="small" @click="checkAllMenu(true)">全部勾选</el-button>
            <el-button link type="info" size="small" @click="checkAllMenu(false)">全部清空</el-button>
          </div>
          <div class="menu-module-list">
            <el-tree
                ref="menuTreeRef"
                :data="menuTree"
                node-key="id"
                :props="menuTreeProps"
                show-checkbox
                :check-strictly="roleForm.menuCheckStrictly !== 1"
                :indent="16"
                default-expand-all
                :expand-on-click-node="false"
            />
          </div>
        </div>
      </el-form-item>
      <el-form-item label="父子联动">
        <el-switch
            v-model="roleForm.menuCheckStrictly"
            :active-value="1"
            :inactive-value="0"
            active-text="联动"
            inactive-text="独立"
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
            <div>选择"自定义数据"时需勾选具体部门，用户仅可见所选部门的数据；</div>
            <div>菜单权限按模块勾选，决定该角色下用户可进入的功能菜单；</div>
            <div>父子联动开启时勾选父菜单会同步勾选子菜单，关闭后可独立勾选；</div>
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

</template>

<script setup>
import {createRoleAPI, pageRoleListAPI, updateRoleAPI, deleteRoleAPI, detailRoleAPI} from '@/api/manage/sys/role.js';
import {pageRoleUserListAPI, addUserAPI, removeUserAPI} from '@/api/manage/sys/roleUser.js';
import {roleMenuTreeAPI} from '@/api/manage/sys/menu.js';
import {ElMessage, ElMessageBox} from "element-plus";
import SelectUser from '@/components/SelectUser/index.vue';
import SelectDept from '@/components/SelectDept/index.vue';
import {nextTick, ref} from 'vue';
import UserAvatar from "@/components/UserAvatar/index.vue";

const dataScopeOptions = [
  {value: 1, label: '仅本人数据'},
  {value: 2, label: '仅本部门数据'},
  {value: 3, label: '本部门及以下部门数据'},
  {value: 4, label: '所有数据'},
  {value: 5, label: '自定义数据'},
];
const roleList = ref([]);
const roleForm = ref({
  id: undefined,
  name: undefined,
  dataScope: undefined,
  status: undefined,
  menuCheckStrictly: 1,
  deptIds: [],
});
const roleFormVisible = ref(false);
const roleFormTitle = ref('');
const roleFormRef = ref();
// 角色表单校验规则
const roleFormRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
};
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
// 已选待添加用户 ID 数组
const selectedUserIds = ref([]);
// 角色授权菜单树，仅租户业务平台菜单
const menuTree = ref([]);
const menuTreeRef = ref();
// 菜单树全部节点 id 与叶子节点 id，用于全选与联动加载
const menuAllIds = ref([]);
const menuLeafIds = ref([]);
// el-tree 节点字段映射
const menuTreeProps = {label: 'name', children: 'children'};

getRoleList();
getMenuList();

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
 * 查询角色授权菜单树
 */
function getMenuList() {
  roleMenuTreeAPI().then(res => {
    menuTree.value = res.data || [];
    const ids = collectMenuIds(menuTree.value);
    menuAllIds.value = ids.allIds;
    menuLeafIds.value = ids.leafIds;
  })
}

/**
 * 收集菜单树全部节点 id 与叶子节点 id
 * @param nodes 菜单树
 * @returns {{allIds: Array, leafIds: Array}}
 */
function collectMenuIds(nodes) {
  const allIds = [];
  const leafIds = [];
  const walk = (items) => items.forEach(item => {
    allIds.push(item.id);
    if (item.children && item.children.length) {
      walk(item.children);
    } else {
      leafIds.push(item.id);
    }
  });
  walk(nodes || []);
  return { allIds, leafIds };
}

/**
 * 全部勾选或全部清空菜单
 * @param checked 是否勾选
 */
function checkAllMenu(checked) {
  if (!menuTreeRef.value) {
    return;
  }
  if (!checked) {
    menuTreeRef.value.setCheckedKeys([]);
    return;
  }
  // 联动模式只设叶子节点，父节点由 el-tree 自动推导，避免半选父被全选
  if (roleForm.value.menuCheckStrictly === 1) {
    menuTreeRef.value.setCheckedKeys(menuLeafIds.value);
  } else {
    menuTreeRef.value.setCheckedKeys(menuAllIds.value);
  }
}

/**
 * 按角色已授权菜单 id 回填勾选
 * @param menuIds 菜单 id 列表
 */
function applyMenuChecked(menuIds) {
  if (!menuTreeRef.value) {
    return;
  }
  if (roleForm.value.menuCheckStrictly === 1) {
    const leafIdSet = new Set(menuLeafIds.value);
    const leafChecked = menuIds.filter(id => leafIdSet.has(id));
    menuTreeRef.value.setCheckedKeys(leafChecked);
  } else {
    menuTreeRef.value.setCheckedKeys(menuIds);
  }
}

/**
 * 读取当前勾选的菜单 id，联动模式含半选父节点
 * @returns {Array}
 */
function collectCheckedMenuIds() {
  if (!menuTreeRef.value) {
    return [];
  }
  const checkedKeys = menuTreeRef.value.getCheckedKeys();
  if (roleForm.value.menuCheckStrictly === 1) {
    const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys();
    return [...checkedKeys, ...halfCheckedKeys];
  }
  return checkedKeys;
}

/**
 * 打开创建角色表单
 */
function openCreateRoleForm() {
  roleForm.value.id = undefined;
  roleForm.value.name = undefined;
  roleForm.value.dataScope = 1;
  roleForm.value.status = 1;
  roleForm.value.menuCheckStrictly = 1;
  roleForm.value.deptIds = [];
  roleFormTitle.value = "创建角色";
  roleFormVisible.value = true;
  nextTick(() => {
    if (menuTreeRef.value) {
      menuTreeRef.value.setCheckedKeys([]);
    }
  });
}

/**
 * 打开修改角色表单
 */
function openUpdateRoleForm(row) {
  detailRoleAPI({ id: row.id }).then(res => {
    if (res.code !== 200) {
      return;
    }
    const detail = res.data;
    roleForm.value.id = detail.id;
    roleForm.value.name = detail.name;
    roleForm.value.dataScope = detail.dataScope;
    roleForm.value.status = detail.status;
    roleForm.value.menuCheckStrictly = detail.menuCheckStrictly == null ? 1 : detail.menuCheckStrictly;
    roleForm.value.deptIds = detail.deptIds || [];
    roleFormTitle.value = "修改角色";
    roleFormVisible.value = true;
    nextTick(() => {
      applyMenuChecked(detail.menuIds || []);
    });
  });
}

/**
 * 关闭角色表单弹窗
 */
function closeRoleForm() {
  roleForm.value.name = undefined;
  roleForm.value.status = undefined;
  roleForm.value.deptIds = [];
  if (menuTreeRef.value) {
    menuTreeRef.value.setCheckedKeys([]);
  }
  roleFormTitle.value = undefined;
  roleFormVisible.value = false;
}

/**
 * 提交角色表单
 */
function submitRoleForm() {
  roleFormRef.value.validate(valid => {
    if (!valid) {
      return;
    }
    const menuIds = collectCheckedMenuIds();
    const deptIds = roleForm.value.dataScope === 5 ? (roleForm.value.deptIds || []) : [];
    // 菜单与部门 id 以逗号串传递，后端绑定为 List<Long>
    const data = {
      id: roleForm.value.id,
      name: roleForm.value.name,
      dataScope: roleForm.value.dataScope,
      status: roleForm.value.status,
      menuCheckStrictly: roleForm.value.menuCheckStrictly,
      menuIds: menuIds.join(','),
      deptIds: deptIds.join(','),
    };
    const api = data.id ? updateRoleAPI : createRoleAPI;
    api(data).then(res => {
      if (res.code !== 200) {
        return;
      }
      ElMessage.success(data.id ? "角色修改成功" : "角色创建成功");
      closeRoleForm();
      getRoleList();
    })
  });
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
.menu-perm-box {
  width: 100%;
  .menu-perm-toolbar {
    display: flex;
    justify-content: flex-end;
    margin-bottom: $spacing-xs;
  }
  .menu-module-list {
    max-height: 320px;
    overflow-x: hidden;
    overflow-y: auto;
    padding-left: 20px;
  }
}
</style>

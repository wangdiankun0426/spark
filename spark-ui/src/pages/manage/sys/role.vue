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
      size="40%"
      :before-close="closeRoleForm"
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
            <el-button link type="primary" size="small" @click="toggleAll(true)">全部勾选</el-button>
            <el-button link type="info" size="small" @click="toggleAll(false)">全部清空</el-button>
          </div>
          <div class="menu-module-list">
            <index :nodes="menuTree" :state-of="nodeStateOf" :on-toggle="toggleNode" />
          </div>
        </div>
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
            <div>菜单权限按模块勾选，决定该角色下用户可进入的功能菜单；</div>
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
import {createRoleAPI, pageRoleListAPI, updateRoleAPI, deleteRoleAPI} from '@/api/manage/sys/role.js';
import {pageRoleUserListAPI, addUserAPI, removeUserAPI} from '@/api/manage/sys/roleUser.js';
import {listMenuAPI} from '@/api/manage/sys/menu.js';
import {ElMessage, ElMessageBox} from "element-plus";
import SelectUser from '@/components/SelectUser';
import Index from '@/components/MenuPermGroup/index.vue';
import {computed, reactive, ref} from 'vue';
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
// 菜单清单（接口返回的一级+二级菜单）
const menuOptionList = ref([]);
// 勾选的菜单id映射（id -> 是否勾选）
const menuCheckedMap = reactive({});
// 按父级递归生成的菜单树（支持任意层级）
const menuTree = computed(() => {
  const buildTree = (parentId) => menuOptionList.value
      .filter(item => item.parentId === parentId)
      .map(item => ({
        id: item.id,
        name: item.name,
        children: buildTree(item.id),
      }));
  return buildTree(0);
});

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
 * 查询菜单清单
 */
function getMenuList() {
  listMenuAPI().then(res => {
    menuOptionList.value = res.data || [];
  })
}

// 显式点选的菜单集合（用户勾选的节点），有效勾选由其自动携带各级祖先生成
const explicitChecked = reactive({});

/**
 * 查询菜单节点
 * @param menuId 菜单id
 * @returns {object|undefined}
 */
function findMenuById(menuId) {
  return menuOptionList.value.find(item => item.id === menuId);
}

/**
 * 判断ancestorId是否为nodeId的祖先
 * @param ancestorId 祖先菜单id
 * @param nodeId 节点菜单id
 * @returns {boolean}
 */
function isAncestorOf(ancestorId, nodeId) {
  let current = findMenuById(nodeId);
  while (current) {
    if (current.id === ancestorId) {
      return true;
    }
    current = current.parentId ? findMenuById(current.parentId) : null;
  }
  return false;
}

/**
 * 收集节点及其所有子孙菜单id
 * @param node 菜单节点
 * @returns {number[]}
 */
function collectSubtreeIds(node) {
  const ids = [node.id];
  (node.children || []).forEach(child => {
    ids.push(...collectSubtreeIds(child));
  });
  return ids;
}

/**
 * 重算有效勾选：显式勾选的节点 + 其各级祖先
 */
function recomputeEffective() {
  clearMenuChecked();
  Object.keys(explicitChecked).forEach(idStr => {
    if (!explicitChecked[idStr]) {
      return;
    }
    const menuId = Number(idStr);
    let current = findMenuById(menuId);
    if (!current) {
      menuCheckedMap[menuId] = true;
      return;
    }
    while (current) {
      menuCheckedMap[current.id] = true;
      current = current.parentId ? findMenuById(current.parentId) : null;
    }
  });
}

/**
 * 节点的勾选展示状态
 * @param node 菜单节点
 * @returns {{checked: boolean, indeterminate: boolean}}
 */
function nodeStateOf(node) {
  const ids = collectSubtreeIds(node);
  const descendantIds = ids.filter(id => id !== node.id);
  const descendantChecked = descendantIds.filter(id => menuCheckedMap[id]).length;
  return {
    checked: !!menuCheckedMap[node.id],
    indeterminate: descendantChecked > 0 && descendantChecked < descendantIds.length,
  };
}

/**
 * 勾选或取消单个节点（勾选只影响自身；子层勾选会携带父层）
 * @param node 菜单节点
 * @param checked 是否勾选
 */
function toggleNode(node, checked) {
  if (checked) {
    explicitChecked[node.id] = true;
  } else {
    delete explicitChecked[node.id];
  }
  recomputeEffective();
}

/**
 * 全部勾选或全部清空
 * @param checked 是否勾选
 */
function toggleAll(checked) {
  if (checked) {
    menuOptionList.value.forEach(item => {
      explicitChecked[item.id] = true;
    });
  } else {
    Object.keys(explicitChecked).forEach(id => {
      delete explicitChecked[id];
    });
  }
  recomputeEffective();
}

/**
 * 清空有效勾选
 */
function clearMenuChecked() {
  Object.keys(menuCheckedMap).forEach(id => {
    delete menuCheckedMap[id];
  });
}

/**
 * 清空显式勾选
 */
function clearExplicitChecked() {
  Object.keys(explicitChecked).forEach(id => {
    delete explicitChecked[id];
  });
}

/**
 * 按勾选id填充（还原显式点选，去掉仅由子层携带的中间祖先）
 * @param ids 勾选id列表
 */
function setMenuChecked(ids) {
  clearMenuChecked();
  clearExplicitChecked();
  ids.forEach(id => {
    const carried = ids.some(other => other !== id && isAncestorOf(id, other));
    if (!carried) {
      explicitChecked[id] = true;
    }
  });
  recomputeEffective();
}

/**
 * 打开创建角色表单
 */
function openCreateRoleForm() {
  roleForm.value.name = undefined;
  roleForm.value.dataScope = 1;
  roleForm.value.status = 1;
  clearMenuChecked();
  clearExplicitChecked();
  // 新增角色默认勾选首页菜单权限
  explicitChecked[10] = true;
  recomputeEffective();
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
  const menuIdList = data.menuIds ? data.menuIds.split(',').filter(Boolean).map(Number) : [];
  setMenuChecked(menuIdList);
  roleFormTitle.value = "修改角色";
  roleFormVisible.value = true;
}

/**
 * 关闭角色表单弹窗
 */
function closeRoleForm() {
  roleForm.value.name = undefined;
  roleForm.value.status = undefined;
  clearMenuChecked();
  clearExplicitChecked();
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
    const selectedIds = Object.keys(menuCheckedMap).filter(id => menuCheckedMap[id]);
    const menuIds = selectedIds.map(Number).sort((a, b) => a - b).join(',');
    if (!roleForm.value.id) {
      const data = {
        name: roleForm.value.name,
        dataScope: roleForm.value.dataScope,
        status: roleForm.value.status,
        menuIds: menuIds,
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
        menuIds: menuIds,
      };
      updateRoleAPI(data).then(res => {
        ElMessage.success("角色修改成功");
        closeRoleForm();
        getRoleList();
      })
    }
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

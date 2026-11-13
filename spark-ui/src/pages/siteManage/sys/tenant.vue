<template>
  <div class="app-container">
    <!-- 左侧租户列表 -->
    <div class="tenant-box">
      <div class="tenant-toolbar">
        <el-input
            v-model="query.name"
            placeholder="请输入租户名称"
            clearable
            class="tenant-search-input"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button type="primary" class="tenant-create-btn" @click="openCreateForm">
          <el-icon><Plus /></el-icon>新建租户
        </el-button>
      </div>

      <el-table
          :data="list"
          ref="tenantTableRef"
          v-loading="loading"
          highlight-current-row
          height="calc(100vh - 160px)"
          @current-change="selectTenantRow"
      >
        <el-table-column prop="name" label="租户名称" min-width="140" align="left" show-overflow-tooltip/>
        <el-table-column prop="statusName" label="状态" width="120" align="center"/>
        <el-table-column prop="accountCount" label="账号数量" width="90" align="center"/>
        <el-table-column prop="deadline" label="截止时间" width="170" align="center"/>
        <el-table-column fixed="right" label="操作" width="220" align="center">
          <template #default="scope">
            <el-button
                type="primary"
                text
                @click="openConfig(scope.row)"
            >
              <el-icon><SetUp /></el-icon>
              <span style="font-size: 14px; font-weight: 500">
                配置
              </span>
            </el-button>
            <el-button
                type="success"
                text
                @click="openUpdateForm(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              <span style="font-size: 14px; font-weight: 500">
                修改
              </span>
            </el-button>
            <el-button
                type="danger"
                text
                @click="deleteRow(scope.row)"
            >
              <el-icon><Delete /></el-icon>
              <span style="font-size: 14px; font-weight: 500">
                删除
              </span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          v-model:current-page="query.pageNo"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="getTenantList"
          @size-change="handleSearch"
      />
    </div>

    <!-- 右侧租户用户列表 -->
    <div class="tenant-user-box">
      <div class="user-toolbar">
        <span class="user-title">{{ currentTenantName ? currentTenantName + ' 下的用户' : '请先在左侧选择租户' }}</span>
        <select-user
            v-model="selectedUserIds"
            ref="selectUserRef"
            multiple
            :disabled="currentTenantId == null"
            placeholder="请选择要加入的用户"
            @change="submitAddUser"
            class="user-select"
        />
        <el-button
            type="primary"
            :disabled="currentTenantId == null"
            @click="openCreateUserForm"
        >
          <el-icon><Plus /></el-icon>新建用户
        </el-button>
      </div>

      <el-table
          :data="tenantUserList"
          v-loading="userLoading"
          height="calc(100vh - 160px)"
      >
        <el-table-column label="" width="60" align="center">
          <template #default="scope">
            <user-avatar :user-id="scope.row.userId" :size="36" />
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户名称" min-width="80" align="center"/>
        <el-table-column prop="loginName" label="登录名" min-width="100" align="center"/>
        <el-table-column prop="roleTypeName" label="用户角色" min-width="96" align="center"/>
        <el-table-column fixed="right" label="操作" width="300" align="center">
          <template #default="scope">
            <el-button
                v-if="!isOrgAdminRole(scope.row.roleType)"
                type="success"
                text
                @click="openOrgMenuForm(scope.row)"
            >
              <el-icon><TurnOff /></el-icon>
              <span style="font-size: 14px; font-weight: 500">
                设为管理员
              </span>
            </el-button>
            <template v-else>
              <el-button
                  type="primary"
                  text
                  @click="openOrgMenuConfig(scope.row)"
              >
                <el-icon><SetUp /></el-icon>
                <span style="font-size: 14px; font-weight: 500">
                  菜单配置
                </span>
              </el-button>
              <el-button
                  type="danger"
                  text
                  @click="cancelTenantUserAdmin(scope.row)"
              >
                <el-icon><TurnOff /></el-icon>
                <span style="font-size: 14px; font-weight: 500">
                  取消管理员
                </span>
              </el-button>
            </template>
            <el-button
                type="warning"
                text
                @click="removeUser(scope.row.userId)"
            >
              <el-icon><Delete /></el-icon>
              <span style="font-size: 14px; font-weight: 500">移除</span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          v-model:current-page="tenantUserQuery.pageNo"
          v-model:page-size="tenantUserQuery.pageSize"
          :total="tenantUserTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="getTenantUserList"
          @size-change="getTenantUserList"
      />
    </div>

    <!-- 租户表单抽屉 -->
    <el-drawer
        v-model="formVisible"
        :title="formTitle"
        direction="ltr"
        size="40%"
        :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="租户名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入租户名称" maxlength="200"/>
        </el-form-item>
        <el-form-item label="账号数量" prop="accountCount">
          <el-input-number v-model="form.accountCount" :min="1" :max="100000"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
              v-model="form.status"
              :active-value="1"
              :inactive-value="-1"
              active-text="已启用"
              inactive-text="已停用"
              inline-prompt
          />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
              v-model="form.deadline"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择截止时间，为空表示不限制"
              style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">保存</el-button>
        <el-button @click="closeForm">取消</el-button>
      </template>
    </el-drawer>

    <!-- 新建用户抽屉 -->
    <el-drawer
        v-model="userFormVisible"
        title="新建用户"
        direction="ltr"
        size="40%"
        :close-on-click-modal="false"
    >
      <el-form :model="userForm" :rules="userFormRules" ref="userFormRef" label-width="90px">
        <el-form-item label="用户名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入用户名" maxlength="50"/>
        </el-form-item>
        <el-form-item label="登录名" prop="loginName">
          <el-input v-model="userForm.loginName" placeholder="请输入登录名" maxlength="50"/>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" maxlength="11"/>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" maxlength="100"/>
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
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitUserForm">保存</el-button>
        <el-button @click="closeUserForm">取消</el-button>
      </template>
    </el-drawer>

    <!-- 组织菜单配置抽屉 -->
    <el-drawer
        v-model="orgMenuFormVisible"
        :title="orgMenuFormTitle"
        direction="ltr"
        size="40%"
        :close-on-click-modal="false"
    >
      <div class="org-menu-box">
        <div class="org-menu-toolbar">
          <el-button link type="primary" size="small" @click="checkAllOrgMenu(true)">全部勾选</el-button>
          <el-button link type="info" size="small" @click="checkAllOrgMenu(false)">全部清空</el-button>
        </div>
        <div class="org-menu-tree">
          <el-tree
              ref="orgMenuTreeRef"
              :data="orgMenuTree"
              node-key="id"
              :props="orgMenuTreeProps"
              show-checkbox
              :check-strictly="false"
              :indent="16"
              default-expand-all
              :expand-on-click-node="false"
          />
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitOrgMenuForm">保存</el-button>
        <el-button @click="closeOrgMenuForm">取消</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import {pageTenantListAPI, createTenantAPI, updateTenantAPI, deleteTenantAPI} from '@/api/manage/sys/tenant.js';
import {addTenantUserAPI, pageTenantUserListAPI, removeTenantUserAPI, updateTenantUserAPI, queryOrgMenusAPI} from '@/api/manage/sys/tenantUser.js';
import {orgMenuTreeAPI} from '@/api/manage/sys/menu.js';
import {createUserAPI} from '@/api/manage/sys/user.js';
import {ElMessage, ElMessageBox} from 'element-plus';
import SelectUser from '@/components/SelectUser/index.vue';
import UserAvatar from '@/components/UserAvatar/index.vue';
import {nextTick, ref} from 'vue';
import {useRouter} from 'vue-router';
import {Delete} from "@element-plus/icons-vue";

const router = useRouter();

const list = ref([]);
const tenantTableRef = ref();
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNo: 1, pageSize: 10, name: undefined });
// 当前选中的租户
const currentTenantId = ref(null);
const currentTenantName = ref('');
// 已选待加入用户 ID 数组
const selectedUserIds = ref([]);
// 用户下拉组件引用，用于新建用户后刷新可选项
const selectUserRef = ref();
// 性别选项
const sexOptions = [
  { value: 1, label: '男' },
  { value: 2, label: '女' }
];
// 右侧租户用户数据
const tenantUserList = ref([]);
const tenantUserTotal = ref(0);
const userLoading = ref(false);
const tenantUserQuery = ref({ pageNo: 1, pageSize: 15 });
const formVisible = ref(false);
const formTitle = ref(undefined);
const formRef = ref();
const form = ref({ id: undefined, name: undefined, status: 1, accountCount: 10, deadline: undefined });
const formRules = {
  name: [{ required: true, trigger: 'blur', message: '请输入租户名称' }],
  accountCount: [{ required: true, trigger: 'blur', message: '请输入账号数量' }],
};
// 新建用户表单
const userFormVisible = ref(false);
const userFormRef = ref();
const userForm = ref({ name: undefined, loginName: undefined, phone: undefined, email: undefined, sex: 1, status: 1 });
const userFormRules = {
  name: [{ required: true, trigger: 'blur', message: '请输入用户名' }],
  loginName: [{ required: true, trigger: 'blur', message: '请输入登录名' }],
};
// 组织菜单配置
const orgMenuFormVisible = ref(false);
const orgMenuFormTitle = ref('');
const orgMenuForm = ref({ userId: undefined, userName: '' });
const orgMenuTree = ref([]);
const orgMenuTreeRef = ref();
// 组织菜单树全部节点 id 与叶子节点 id，用于全选与联动加载
const orgMenuAllIds = ref([]);
const orgMenuLeafIds = ref([]);
const orgMenuTreeProps = {label: 'name', children: 'children'};

getTenantList();
getOrgMenuTree();

/**
 * 分页查询租户列表
 */
function getTenantList() {
  loading.value = true;
  pageTenantListAPI(query.value).then(res => {
    list.value = res.data.rows;
    total.value = res.data.total;
    loading.value = false;
    // 列表刷新后恢复原选中租户的选中状态
    restoreTenantSelection();
  })
}

/**
 * 恢复左侧租户表格的选中行，并同步右侧标题的租户名称
 */
function restoreTenantSelection() {
  if (currentTenantId.value == null) {
    return;
  }
  nextTick(() => {
    const row = list.value.find(item => item.id === currentTenantId.value);
    if (!row) {
      return;
    }
    tenantTableRef.value.setCurrentRow(row);
    currentTenantName.value = row.name;
  });
}

/**
 * 搜索租户
 */
function handleSearch() {
  query.value.pageNo = 1;
  getTenantList();
}

/**
 * 选中租户
 * @param row 租户行数据
 */
function selectTenantRow(row) {
  if (!row || row.id === currentTenantId.value) {
    return;
  }
  currentTenantId.value = row.id;
  currentTenantName.value = row.name;
  getTenantUserList();
}

/**
 * 分页查询当前租户下的用户
 */
function getTenantUserList() {
  if (currentTenantId.value == null) {
    return;
  }
  userLoading.value = true;
  tenantUserQuery.value.tenantId = currentTenantId.value;
  pageTenantUserListAPI(tenantUserQuery.value).then(res => {
    tenantUserList.value = res.data.rows;
    tenantUserTotal.value = res.data.total;
    userLoading.value = false;
  })
}

/**
 * 提交加入用户
 * @param userIds 选中的用户id数组
 */
function submitAddUser(userIds) {
  if (currentTenantId.value == null || !userIds || userIds.length === 0) {
    return;
  }
  const data = {
    tenantId: currentTenantId.value,
    userIds: (userIds || []).join(','),
  };
  addTenantUserAPI(data).then(res => {
    if (res.code !== 200) {
      return;
    }
    ElMessage.success('添加成功');
    selectedUserIds.value = [];
    getTenantUserList();
  })
}

/**
 * 从当前租户移除用户
 * @param id 用户id
 */
function removeUser(id) {
  ElMessageBox.confirm('是否确定从此租户移除该用户?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    const data = {
      tenantId: currentTenantId.value,
      userId: id,
    };
    removeTenantUserAPI(data).then(res => {
      if (res.code !== 200) {
        return;
      }
      ElMessage.success('移除成功');
      getTenantUserList();
    })
  }).catch(() => {
  })
}

/**
 * 取消租户用户组织管理员角色
 * @param row 租户用户行数据
 */
function cancelTenantUserAdmin(row) {
  if (currentTenantId.value == null || !row || row.userId == null) {
    return;
  }
  ElMessageBox.confirm('是否确定取消用户【' + row.userName + '】的组织管理员角色?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    const data = {
      tenantId: currentTenantId.value,
      userId: row.userId,
      roleType: 1,
    };
    updateTenantUserAPI(data).then(res => {
      if (res.code !== 200) {
        return;
      }
      ElMessage.success('取消组织管理员成功');
      getTenantUserList();
    })
  }).catch(() => {
  })
}

/**
 * 查询组织菜单授权树
 */
function getOrgMenuTree() {
  orgMenuTreeAPI().then(res => {
    orgMenuTree.value = res.data || [];
    const ids = collectOrgMenuIds(orgMenuTree.value);
    orgMenuAllIds.value = ids.allIds;
    orgMenuLeafIds.value = ids.leafIds;
  })
}

/**
 * 收集组织菜单树全部节点 id 与叶子节点 id
 * @param nodes 组织菜单树
 * @returns {{allIds: Array, leafIds: Array}}
 */
function collectOrgMenuIds(nodes) {
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
 * 全部勾选或全部清空组织菜单
 * @param checked 是否勾选
 */
function checkAllOrgMenu(checked) {
  if (!orgMenuTreeRef.value) {
    return;
  }
  if (!checked) {
    orgMenuTreeRef.value.setCheckedKeys([]);
    return;
  }
  // 联动模式只设叶子节点，父节点由 el-tree 自动推导，避免半选父被全选
  orgMenuTreeRef.value.setCheckedKeys(orgMenuLeafIds.value);
}

/**
 * 按用户已授权组织菜单 id 回填勾选
 * @param menuIds 菜单 id 列表
 */
function applyOrgMenuChecked(menuIds) {
  if (!orgMenuTreeRef.value) {
    return;
  }
  // 联动模式只回填叶子节点，父节点由 el-tree 自动推导
  const leafIdSet = new Set(orgMenuLeafIds.value);
  const leafChecked = menuIds.filter(id => leafIdSet.has(id));
  orgMenuTreeRef.value.setCheckedKeys(leafChecked);
}

/**
 * 读取当前勾选的组织菜单 id，含半选父节点
 * @returns {Array}
 */
function collectCheckedOrgMenuIds() {
  if (!orgMenuTreeRef.value) {
    return [];
  }
  const checkedKeys = orgMenuTreeRef.value.getCheckedKeys();
  const halfCheckedKeys = orgMenuTreeRef.value.getHalfCheckedKeys();
  return [...checkedKeys, ...halfCheckedKeys];
}

/**
 * 打开设为管理员抽屉
 * @param row 租户用户行数据
 */
function openOrgMenuForm(row) {
  if (currentTenantId.value == null || !row || row.userId == null) {
    return;
  }
  orgMenuForm.value = { userId: row.userId, userName: row.userName };
  orgMenuFormTitle.value = '设为管理员 - ' + row.userName;
  orgMenuFormVisible.value = true;
  nextTick(() => {
    if (orgMenuTreeRef.value) {
      orgMenuTreeRef.value.setCheckedKeys([]);
    }
  });
}

/**
 * 打开组织菜单配置抽屉并回填已授权菜单
 * @param row 租户用户行数据
 */
function openOrgMenuConfig(row) {
  if (currentTenantId.value == null || !row || row.userId == null) {
    return;
  }
  queryOrgMenusAPI({ tenantId: currentTenantId.value, userId: row.userId }).then(res => {
    if (res.code !== 200) {
      return;
    }
    orgMenuForm.value = { userId: row.userId, userName: row.userName };
    orgMenuFormTitle.value = '菜单配置 - ' + row.userName;
    orgMenuFormVisible.value = true;
    const menuIds = res.data || [];
    nextTick(() => {
      applyOrgMenuChecked(menuIds);
    });
  })
}

/**
 * 关闭组织菜单配置抽屉
 */
function closeOrgMenuForm() {
  if (orgMenuTreeRef.value) {
    orgMenuTreeRef.value.setCheckedKeys([]);
  }
  orgMenuFormVisible.value = false;
}

/**
 * 提交组织菜单授权并设为组织管理员
 */
function submitOrgMenuForm() {
  const menuIds = collectCheckedOrgMenuIds();
  // 菜单 id 以逗号串传递，后端绑定为 List<Long>
  const data = {
    tenantId: currentTenantId.value,
    userId: orgMenuForm.value.userId,
    roleType: 2,
    orgMenuIds: menuIds.join(','),
  };
  updateTenantUserAPI(data).then(res => {
    if (res.code !== 200) {
      return;
    }
    ElMessage.success('组织菜单保存成功');
    closeOrgMenuForm();
    getTenantUserList();
  })
}

/**
 * 判断租户用户角色是否为组织管理员
 * @param roleType 角色类型
 * @returns {boolean} 是否为组织管理员
 */
function isOrgAdminRole(roleType) {
  if (roleType == null) {
    return false;
  }
  return (roleType & 2) === 2;
}

/**
 * 打开新建用户抽屉
 */
function openCreateUserForm() {
  if (currentTenantId.value == null) {
    return;
  }
  userForm.value = { name: undefined, loginName: undefined, phone: undefined, email: undefined, sex: 1, status: 1 };
  userFormVisible.value = true;
}

/**
 * 关闭新建用户抽屉
 */
function closeUserForm() {
  userFormVisible.value = false;
}

/**
 * 提交新建用户表单，用户直接归属当前选中租户
 */
function submitUserForm() {
  userFormRef.value.validate(valid => {
    if (!valid) {
      return;
    }
    const data = {
      ...userForm.value,
      tenantId: currentTenantId.value,
    };
    createUserAPI(data).then(res => {
      if (res.code !== 200) {
        return;
      }
      ElMessage.success('用户创建成功');
      closeUserForm();
      getTenantUserList();
      // 账号数量变化，刷新左侧租户列表
      getTenantList();
      // 刷新下拉可选用户
      selectUserRef.value.reload();
    })
  });
}

/**
 * 打开新建租户抽屉
 */
function openCreateForm() {
  form.value = { id: undefined, name: undefined, status: 1, accountCount: 10, deadline: undefined };
  formTitle.value = '新建租户';
  formVisible.value = true;
}

/**
 * 打开修改租户抽屉
 * @param row 租户行数据
 */
function openUpdateForm(row) {
  form.value = {
    id: row.id,
    name: row.name,
    status: row.status,
    accountCount: row.accountCount,
    deadline: row.deadline,
  };
  formTitle.value = '修改租户';
  formVisible.value = true;
}

/**
 * 关闭租户抽屉
 */
function closeForm() {
  formVisible.value = false;
}

/**
 * 提交租户表单（新增/修改）
 */
function submitForm() {
  formRef.value.validate(valid => {
    if (!valid) {
      return;
    }
    const api = form.value.id ? updateTenantAPI : createTenantAPI;
    api(form.value).then(res => {
      if (res.code !== 200) {
        return;
      }
      ElMessage.success(form.value.id ? '修改成功' : '创建成功');
      closeForm();
      getTenantList();
    })
  });
}

/**
 * 删除租户
 * @param row 租户行数据
 */
function deleteRow(row) {
  ElMessageBox.confirm('是否确定删除此租户?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteTenantAPI({ id: row.id }).then(res => {
      if (res.code !== 200) {
        return;
      }
      ElMessage.success('删除成功');
      // 删除的是当前选中租户时，清空右侧用户列表
      if (currentTenantId.value === row.id) {
        currentTenantId.value = null;
        currentTenantName.value = '';
        tenantUserList.value = [];
        tenantUserTotal.value = 0;
      }
      getTenantList();
    })
  }).catch(() => {
  })
}

/**
 * 跳转到当前租户的配置页面
 * @param row 租户行数据
 */
function openConfig(row) {
  router.push({
    path: '/site/sys/tenant/config',
    query: {
      tenantId: row.id,
      tenantName: row.name,
    },
  });
}
</script>

<style scoped lang="scss">
.app-container {
  display: flex;
  align-items: flex-start;
  gap: $spacing-md;
  height: calc(100vh - $nav-height);
  overflow: hidden;
}
.tenant-box {
  flex: 0 0 55%;
  width: 55%;
  min-width: 0;
}
.tenant-user-box {
  flex: 1 1 45%;
  width: 45%;
  min-width: 0;
}
.tenant-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: $spacing-sm;
  .tenant-search-input {
    width: 200px;
    margin-right: $spacing-sm;
  }
  .tenant-create-btn {
    margin-left: auto;
  }
}
.user-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: $spacing-sm;
  .user-title {
    flex: none;
    font-size: 14px;
    color: $color-text-primary;
    margin-right: $spacing-sm;
    white-space: nowrap;
  }
  .user-select {
    flex: 1;
    min-width: 0;
  }
}
.org-menu-box {
  width: 100%;
  .org-menu-toolbar {
    display: flex;
    justify-content: flex-end;
    margin-bottom: $spacing-xs;
  }
  .org-menu-tree {
    max-height: calc(100vh - 180px);
    overflow-x: hidden;
    overflow-y: auto;
  }
}
</style>

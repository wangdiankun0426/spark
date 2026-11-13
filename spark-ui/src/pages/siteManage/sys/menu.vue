<template>
  <div class="app-container">
    <div class="menu-toolbar">
      <el-select
          v-model="moduleCode"
          placeholder="全部模块"
          clearable
          class="menu-module-select"
      >
        <el-option
            v-for="item in moduleOptions"
            :key="item.code"
            :label="item.name"
            :value="item.code"
        />
      </el-select>
      <el-input
          v-model="searchKeyword"
          placeholder="搜索菜单名称 / 标识码 / 权限串"
          clearable
          class="menu-search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="success" @click="toggleExpandAll">
        <el-icon><Sort /></el-icon>{{ isAllExpanded ? '折叠' : '展开' }}
      </el-button>
      <el-button type="primary" class="menu-create-btn" @click="openCreateForm()">
        <el-icon><Plus /></el-icon>新建模块
      </el-button>
    </div>

    <el-table
        :data="displayTree"
        ref="menuTableRef"
        v-loading="loading"
        row-key="id"
        :tree-props="{ children: 'children' }"
        default-expand-all
        height="calc(100vh - 120px)"
    >
      <el-table-column prop="name" label="菜单名称" min-width="200" align="left">
        <template #default="scope">
          <el-icon v-if="scope.row.type !== 4" class="menu-name-icon">
            <component :is="resolveIcon(scope.row.icon)"/>
          </el-icon>
          <span>{{ scope.row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="80" align="center">
        <template #default="scope">
          <el-tag :type="typeTagType(scope.row.type)" size="small">{{ typeName(scope.row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="code" label="标识码" min-width="180" align="left" show-overflow-tooltip/>
      <el-table-column prop="perms" label="权限串" min-width="160" align="left" show-overflow-tooltip/>
      <el-table-column prop="path" label="路由地址" min-width="140" align="left" show-overflow-tooltip/>
      <el-table-column prop="orderNum" label="排序" width="70" align="center"/>
      <el-table-column label="显示" width="70" align="center">
        <template #default="scope">
          <span>{{ scope.row.visible === 1 ? '显示' : '隐藏' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="70" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
            {{ scope.row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="平台" width="70" align="center">
        <template #default="scope">
          <span>{{ platformName(scope.row.platform) }}</span>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="220" align="center">
        <template #default="scope">
          <el-button
              v-if="scope.row.type !== 4"
              type="primary"
              text
              @click="openCreateForm(scope.row)"
          >
            <el-icon><Plus /></el-icon>
            <span style="font-size: 14px; font-weight: 500">新增</span>
          </el-button>
          <el-button type="success" text @click="openUpdateForm(scope.row)">
            <el-icon><Edit /></el-icon>
            <span style="font-size: 14px; font-weight: 500">修改</span>
          </el-button>
          <el-button type="danger" text @click="deleteRow(scope.row)">
            <el-icon><Delete /></el-icon>
            <span style="font-size: 14px; font-weight: 500">删除</span>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 菜单表单抽屉 -->
    <el-drawer
        v-model="formVisible"
        :title="formTitle"
        direction="ltr"
        size="40%"
        :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="90px">
        <!-- 创建时上级由入口决定不可选，仅编辑可换上级 -->
        <el-form-item v-if="form.id" label="上级菜单">
          <el-tree-select
              v-model="form.parentCode"
              :data="parentOptions"
              node-key="code"
              :props="{ label: 'name', value: 'code', children: 'children' }"
              check-strictly
              :render-after-expand="false"
              clearable
              placeholder="顶级菜单"
              style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio v-for="item in formTypeOptions" :key="item.value" :label="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" maxlength="100"/>
        </el-form-item>
        <el-form-item label="标识码" prop="code">
          <el-input v-model="form.code" placeholder="如 menu.llm.agent" maxlength="100"/>
        </el-form-item>
        <el-form-item v-if="form.type === 4" label="权限串" prop="perms">
          <el-input v-model="form.perms" placeholder="如 llm:agent:create" maxlength="100"/>
        </el-form-item>
        <el-form-item v-if="form.type === 1 || form.type === 3" label="路由地址" prop="path">
          <el-input v-model="form.path" placeholder="如 /llm/agent" maxlength="200"/>
        </el-form-item>
        <el-form-item v-if="form.type === 1 || form.type === 3" label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="相对 src 无后缀，如 pages/llmMange/agent/index" maxlength="255"/>
        </el-form-item>
        <el-form-item v-if="form.type !== 4" label="图标">
          <div class="icon-field">
            <el-input v-model="form.icon" placeholder="图标名，如 Menu 或自定义图标名" maxlength="100"/>
            <el-icon class="icon-preview">
              <component :is="resolveIcon(form.icon)"/>
            </el-icon>
          </div>
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.orderNum" :min="0" :max="9999"/>
        </el-form-item>
        <el-form-item v-if="form.type !== 4" label="显示状态">
          <el-switch
              v-model="form.visible"
              :active-value="1"
              :inactive-value="0"
              active-text="显示"
              inactive-text="隐藏"
              inline-prompt
          />
        </el-form-item>
        <el-form-item label="菜单状态">
          <el-switch
              v-model="form.status"
              :active-value="1"
              :inactive-value="0"
              active-text="正常"
              inactive-text="停用"
              inline-prompt
          />
        </el-form-item>
        <el-form-item label="平台归属" prop="platform">
          <el-select v-model="form.platform" placeholder="选择平台归属">
            <el-option v-for="item in platformOptions" :key="item.value" :label="item.label" :value="item.value"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">保存</el-button>
        <el-button @click="closeForm">取消</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import {treeMenuAPI, detailMenuAPI, createMenuAPI, updateMenuAPI, deleteMenuAPI} from '@/api/manage/sys/menu.js';
import {resolveIcon} from '@/utils/iconRegistry.js';
import {ElMessage, ElMessageBox} from 'element-plus';
import {computed, ref, watch} from 'vue';

const typeOptions = [
  {value: 1, label: '模块'},
  {value: 2, label: '分组'},
  {value: 3, label: '菜单'},
  {value: 4, label: '按钮'},
];
const platformOptions = [
  {value: 0, label: '租户菜单'},
  {value: 1, label: '站点菜单'},
  {value: 2, label: '公共菜单'},
  {value: 3, label: '组织菜单'},
];
const menuTree = ref([]);
const menuTableRef = ref();
const searchKeyword = ref('');
const moduleCode = ref('');
const isAllExpanded = ref(true);
const loading = ref(false);
const formVisible = ref(false);
const formTitle = ref(undefined);
const formRef = ref();
const formParentType = ref(undefined);
const form = ref({
  id: undefined, name: undefined, parentCode: '', code: undefined, type: 1,
  perms: undefined, path: undefined, component: undefined, icon: undefined,
  orderNum: 99, visible: 1, status: 1, platform: 0,
});
// 校验规则随菜单类型动态变化，菜单需路由组件、按钮需权限串
const formRules = computed(() => {
  const rules = {
    name: [{required: true, trigger: 'blur', message: '请输入菜单名称'}],
    code: [{required: true, trigger: 'blur', message: '请输入标识码'}],
    type: [{required: true, trigger: 'change', message: '请选择菜单类型'}],
    platform: [{required: true, trigger: 'change', message: '请选择平台归属'}],
  };
  if (form.value.type === 3) {
    rules.path = [{required: true, trigger: 'blur', message: '请输入路由地址'}];
    rules.component = [{required: true, trigger: 'blur', message: '请输入组件路径'}];
  }
  if (form.value.type === 4) {
    rules.perms = [{required: true, trigger: 'blur', message: '请输入权限串'}];
  }
  return rules;
});
// 创建时按上级类型限定可选类型，顶级只建模块，编辑不限
const formTypeOptions = computed(() => {
  if (form.value.id) {
    return typeOptions;
  }
  const parentType = formParentType.value;
  if (!parentType) {
    return typeOptions.filter(item => item.value === 1);
  }
  if (parentType === 1) {
    return typeOptions.filter(item => item.value === 2);
  }
  if (parentType === 2) {
    return typeOptions.filter(item => item.value === 3);
  }
  return typeOptions.filter(item => item.value === 3 || item.value === 4);
});
// 上级菜单选项排除自身子树，防止选自身或后代成环
const parentOptions = computed(() => {
  if (!form.value.id || !form.value.code) {
    return menuTree.value;
  }
  return filterExcludeCode(menuTree.value, form.value.code);
});
// 模块下拉选项取菜单树顶级节点
const moduleOptions = computed(() => menuTree.value.map(node => ({code: node.code, name: node.name})));
// 模块筛选与关键字检索叠加，命中节点保留完整子树，未命中祖先仅保留命中路径
const displayTree = computed(() => {
  let nodes = menuTree.value;
  if (moduleCode.value) {
    nodes = nodes.filter(node => node.code === moduleCode.value);
  }
  const keyword = searchKeyword.value.trim().toLowerCase();
  if (!keyword) {
    return nodes;
  }
  return filterMenuTree(nodes, keyword);
});
// 数据变化时树表默认全展开，同步按钮状态
watch(displayTree, () => {
  isAllExpanded.value = true;
});

getMenuTree();

/**
 * 查询菜单管理树
 */
function getMenuTree() {
  loading.value = true;
  treeMenuAPI().then(res => {
    menuTree.value = res.data || [];
    loading.value = false;
  })
}

/**
 * 排除指定标识码节点及其子树
 * @param nodes 菜单树
 * @param excludeCode 排除的标识码
 * @returns {Array}
 */
function filterExcludeCode(nodes, excludeCode) {
  return (nodes || [])
      .filter(node => node.code !== excludeCode)
      .map(node => ({...node, children: filterExcludeCode(node.children, excludeCode)}));
}

/**
 * 按关键字过滤菜单树，匹配名称、标识码与权限串
 * @param nodes 菜单树
 * @param keyword 小写关键字
 * @returns {Array}
 */
function filterMenuTree(nodes, keyword) {
  const result = [];
  for (const node of nodes || []) {
    const name = (node.name || '').toLowerCase();
    const code = (node.code || '').toLowerCase();
    const perms = (node.perms || '').toLowerCase();
    if (name.includes(keyword) || code.includes(keyword) || perms.includes(keyword)) {
      result.push(node);
      continue;
    }
    const children = filterMenuTree(node.children, keyword);
    if (children.length) {
      result.push({...node, children});
    }
  }
  return result;
}

/**
 * 切换全部展开/折叠
 */
function toggleExpandAll() {
  if (!menuTableRef.value) {
    return;
  }
  isAllExpanded.value = !isAllExpanded.value;
  toggleExpandRows(displayTree.value, isAllExpanded.value);
}

/**
 * 递归切换所有行展开/折叠状态
 * @param rows 菜单树
 * @param expanded 是否展开
 */
function toggleExpandRows(rows, expanded) {
  (rows || []).forEach(row => {
    if (row.children && row.children.length > 0) {
      menuTableRef.value.toggleRowExpansion(row, expanded);
      toggleExpandRows(row.children, expanded);
    }
  });
}

/**
 * 打开新建菜单抽屉
 * @param parentRow 上级菜单行，为空则建顶级模块
 */
function openCreateForm(parentRow) {
  formParentType.value = parentRow ? parentRow.type : undefined;
  // 模块下建分组、分组下建菜单、菜单下默认按钮
  form.value = {
    id: undefined, name: undefined, parentCode: parentRow ? parentRow.code : '', code: undefined,
    type: parentRow ? (parentRow.type === 3 ? 4 : parentRow.type + 1) : 1,
    perms: undefined, path: undefined, component: undefined, icon: undefined,
    orderNum: 99, visible: 1, status: 1,
    platform: parentRow ? parentRow.platform : 0,
  };
  formTitle.value = '新建菜单';
  formVisible.value = true;
}

/**
 * 打开修改菜单抽屉
 * @param row 菜单行
 */
function openUpdateForm(row) {
  formParentType.value = undefined;
  detailMenuAPI({id: row.id}).then(res => {
    if (res.code !== 200) {
      return;
    }
    const menu = res.data;
    form.value = {
      id: menu.id, name: menu.name, parentCode: menu.parentCode || '', code: menu.code,
      type: menu.type, perms: menu.perms, path: menu.path, component: menu.component,
      icon: menu.icon, orderNum: menu.orderNum, visible: menu.visible, status: menu.status,
      platform: menu.platform,
    };
    formTitle.value = '修改菜单';
    formVisible.value = true;
  })
}

/**
 * 关闭菜单抽屉
 */
function closeForm() {
  formVisible.value = false;
}

/**
 * 提交菜单表单（新增/修改）
 */
function submitForm() {
  formRef.value.validate(valid => {
    if (!valid) {
      return;
    }
    const data = {...form.value};
    const api = data.id ? updateMenuAPI : createMenuAPI;
    api(data).then(res => {
      if (res.code !== 200) {
        return;
      }
      ElMessage.success(data.id ? '修改成功' : '创建成功');
      closeForm();
      getMenuTree();
    })
  });
}

/**
 * 删除菜单
 * @param row 菜单行
 */
function deleteRow(row) {
  ElMessageBox.confirm('是否确定删除菜单【' + row.name + '】?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteMenuAPI({id: row.id}).then(res => {
      if (res.code !== 200) {
        return;
      }
      ElMessage.success('删除成功');
      getMenuTree();
    })
  }).catch(() => {
  })
}

/**
 * 菜单类型名称
 * @param type 类型值
 * @returns {string}
 */
function typeName(type) {
  const item = typeOptions.find(o => o.value === type);
  return item ? item.label : '未知';
}

/**
 * 菜单类型标签配色
 * @param type 类型值
 * @returns {string}
 */
function typeTagType(type) {
  if (type === 1) {
    return 'primary';
  }
  if (type === 2) {
    return 'info';
  }
  if (type === 3) {
    return 'success';
  }
  return 'warning';
}

/**
 * 平台归属名称
 * @param platform 平台值
 * @returns {string}
 */
function platformName(platform) {
  const item = platformOptions.find(o => o.value === platform);
  return item ? item.label : '未知';
}
</script>

<style scoped lang="scss">
.menu-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: $spacing-sm;
  .menu-search-input {
    width: 280px;
    margin-right: $spacing-sm;
  }
  .menu-module-select {
    width: 160px;
    margin-right: $spacing-sm;
  }
  .menu-create-btn {
    margin-left: auto;
  }
}
.menu-name-icon {
  margin-right: $spacing-xs;
  vertical-align: middle;
}
.icon-field {
  display: flex;
  align-items: center;
  width: 100%;
  .icon-preview {
    flex: none;
    margin-left: $spacing-sm;
    font-size: 20px;
    color: $color-text-secondary;
  }
}
</style>

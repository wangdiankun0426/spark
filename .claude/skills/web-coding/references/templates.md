# spark-ui 前端代码模板（生成新文件前必读）

配套规范：`.claude/skills/web-coding/SKILL.md`（禁止项/必须项/红线）。

## 1. 页面模板（CRUD 管理页，参考 `manage/system/dept.vue`）

```vue
<template>
  <div class="app-container">
    <!-- 顶部按钮 -->
    <el-button type="primary" @click="openCreateForm()">
      <el-icon><Plus /></el-icon>新建
    </el-button>

    <!-- 数据表格 -->
    <el-table :data="list" ref="tableRef" v-loading="loading">
      <el-table-column prop="name" label="名称" min-width="160" align="left"/>
      <el-table-column prop="statusName" label="状态" width="100" align="center"/>
      <el-table-column prop="createdByName" label="创建人" width="120" align="center"/>
      <el-table-column prop="createdDt" label="创建时间" width="180" align="center"/>
      <el-table-column fixed="right" label="操作" width="160" align="center">
        <template #default="scope">
          <el-button type="success" text @click="openUpdateForm(scope.row)">修改</el-button>
          <el-button type="danger" text @click="deleteRow(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
        v-model:current-page="query.pageNo"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="getList"
        @size-change="getList"
    />

    <!-- 表单抽屉/弹窗 -->
    <el-drawer v-model="formVisible" :title="formTitle" direction="ltr" size="40%">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="auto">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称"/>
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
import {pageXxxListAPI, createXxxAPI, updateXxxAPI, deleteXxxAPI} from '@/api/xxx/xxx.js';
import {ElMessage, ElMessageBox} from 'element-plus';
import {getCurrentInstance, nextTick, ref} from 'vue';

const { proxy } = getCurrentInstance();
const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNo: 1, pageSize: 10 });
const formVisible = ref(false);
const formTitle = ref(undefined);
const form = ref({ id: undefined, name: undefined });
const formRules = {
  name: [{ required: true, trigger: "blur", message: "请输入名称" }]
};

getList();

/**
 * 分页查询列表
 */
function getList() {
  loading.value = true;
  pageXxxListAPI(query.value).then(res => {
    list.value = res.data.rows;
    total.value = res.data.total;
    loading.value = false;
  })
}

/**
 * 提交表单（新增/修改）
 */
function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (!valid) return;
    const data = { ...form.value };
    const api = data.id ? updateXxxAPI : createXxxAPI;
    api(data).then(res => {
      if (res.code !== 200) return;
      ElMessage.success(data.id ? "修改成功" : "创建成功");
      closeForm();
      getList();
    })
  });
}

/**
 * 删除数据（带确认）
 */
function deleteRow(id) {
  ElMessageBox.confirm('是否确定删除?', '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning',
  }).then(() => {
    deleteXxxAPI({ id }).then(res => {
      ElMessage.success("删除成功");
      getList();
    })
  }).catch(() => {})
}
</script>

<style scoped lang="scss">
</style>
```

**页面规范**：
- 结构严格：`<template>` → `<script setup>` → `<style scoped lang="scss">`
- 顶层容器 `class="app-container"`（布局容器/弹窗/全屏预览除外）
- 分页数据取自 `res.data.rows` / `res.data.total`
- 表单校验用 `proxy.$refs.xxxRef.validate`，校验规则 `{ required, trigger: "blur", message }`（中文）
- 删除用 `ElMessageBox.confirm`，成功用 `ElMessage.success`，失败由 request.js 统一提示
- 图标：`<el-icon><Plus /></el-icon>`，图标组件从 `@element-plus/icons-vue` 按需导入（已在 main.js 全局注册可直接用 `<Plus />`）
- 新增/修改编辑表单统一用左侧抽屉（`<el-drawer>` + `direction="ltr"`，参考 `role.vue`/`user.vue`/`dept.vue`），新建与"修改"操作打开同款抽屉
- 函数加中文 JSDoc 注释，注释在代码上方；只写一眼看不出来的核心信息（注释规范见 SKILL.md）

实际页面示例（参考现有代码）：
- `src/pages/manage/system/user.vue`、`dept.vue`、`role.vue`、`notice.vue`（后台管理 CRUD）
- `src/pages/kb/document/index.vue`、`src/pages/llm/agent/index.vue`（业务页）

## 2. API 模板（参考 `api/system/user.js`）

```js
import request from '@/api/request'

/**
 * 分页查询用户列表
 * @param query 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export function pageUserListAPI(query) {
  return request({
    url: '/system/user/pageList',
    method: 'get',
    params: query
  })
}

/**
 * 创建用户
 * @param data 表单数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export function createUserAPI(data) {
  return request({
    url: '/system/user/create',
    method: 'post',
    params: data
  })
}
```

- 命名 `功能+API`：`pageXxxListAPI` / `createXxxAPI` / `updateXxxAPI` / `deleteXxxAPI` / `xxxDetailAPI` / `xxxTreeAPI`
- GET 与 POST **都用 `params`**；例外：文件上传、大文本保存（拼 URL 后可能超 8KB 触发 431），改用 `data` 且后端对应加 `@RequestBody`
- 每个函数加中文 JSDoc
- URL 与后端 `@RequestMapping` 保持一致

## 3. 路由注册（参考 `router/index.js`）

- 模块路由文件：`src/router/modules/<模块>.routes.js`，导出路由数组
- 在 `router/index.js` 导入并注入 `layoutRoutes[0].children`（`...xxxRoutes`）
- 页面级路由设置 `meta.title`（中文，用于页面标题与菜单）
- 组件懒加载：`component: () => import('@/pages/xxx/index')`
- 后台管理路由挂 `/manage` 下，由全局前置守卫校验 `isAdmin()`

```js
// router/modules/xxx.routes.js
export default [
  {
    path: '/xxx',
    name: 'xxx',
    component: () => import('@/pages/xxx/index'),
    meta: { title: 'xxx' },
    children: [
      { path: '/xxx/index', name: 'xxx', component: () => import('@/pages/xxx/index') }
    ]
  }
]
```

## 4. Vuex 模块

- 新模块必须 `namespaced: true`，并在 `store/index.js` 注册
- 登录态与 token 在 `user` 模块（`store.getters['user/getToken']`，`store.dispatch('user/login', ...)`）
- 页面通过 `useStore()` 访问

## 5. 性能与质量

- 列表渲染 `:key` 必须唯一
- 大组件或路由页面用 `() => import(...)` 懒加载
- 频繁切换用 `v-show`，条件渲染用 `v-if`
- `v-for` 与 `v-if` 禁止同元素使用
- 异步逻辑用 `async/await`，避免回调嵌套

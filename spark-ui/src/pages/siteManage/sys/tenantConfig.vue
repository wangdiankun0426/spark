<template>
  <div class="app-container">
    <div class="config-toolbar">
      <span class="config-title">{{ tenantName ? tenantName + ' 的配置' : '租户配置' }}</span>
      <el-button class="config-back-btn" @click="goBack">
        <el-icon><Back /></el-icon>返回
      </el-button>
      <el-button type="primary" @click="openCreateForm">
        <el-icon><Plus /></el-icon>新建配置
      </el-button>
    </div>

    <el-table :data="list" v-loading="loading" height="calc(100vh - 200px)">
      <el-table-column prop="name" label="配置名称" min-width="160" align="left" show-overflow-tooltip/>
      <el-table-column prop="key" label="配置key" min-width="200" align="left" show-overflow-tooltip/>
      <el-table-column prop="value" label="配置值" min-width="200" align="left" show-overflow-tooltip/>
      <el-table-column prop="createdByName" label="创建人" width="120" align="center"/>
      <el-table-column prop="createdDt" label="创建时间" width="170" align="center"/>
      <el-table-column fixed="right" label="操作" width="130" align="center">
        <template #default="scope">
          <el-button type="success" text @click="openUpdateForm(scope.row)">修改</el-button>
          <el-button type="danger" text @click="deleteRow(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
        v-model:current-page="query.pageNo"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="getList"
        @size-change="handleSearch"
    />

    <!-- 租户配置表单抽屉 -->
    <el-drawer v-model="formVisible" :title="formTitle" direction="ltr" size="40%" :close-on-click-modal="false">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入配置名称" maxlength="100"/>
        </el-form-item>
        <el-form-item label="配置key" prop="key">
          <el-input v-model="form.key" placeholder="请输入配置key" maxlength="100" :disabled="form.id != null"/>
        </el-form-item>
        <el-form-item label="配置值" prop="value">
          <el-input v-model="form.value" placeholder="请输入配置值" maxlength="100"/>
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
import {pageTenantConfigListAPI, createTenantConfigAPI, updateTenantConfigAPI, deleteTenantConfigAPI} from '@/api/manage/sys/tenantConfig.js';
import {ElMessage, ElMessageBox} from 'element-plus';
import {ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';

const route = useRoute();
const router = useRouter();

// 当前租户，由租户列表跳转时带入
const tenantId = ref(route.query.tenantId);
const tenantName = ref(route.query.tenantName);
const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNo: 1, pageSize: 10, tenantId: route.query.tenantId });
const formVisible = ref(false);
const formTitle = ref(undefined);
const formRef = ref();
const form = ref({ id: undefined, tenantId: undefined, name: undefined, key: undefined, value: undefined });
const formRules = {
  name: [{ required: true, trigger: 'blur', message: '请输入配置名称' }],
  key: [{ required: true, trigger: 'blur', message: '请输入配置key' }],
};

init();

/**
 * 初始化页面，校验租户参数后加载配置列表
 */
function init() {
  if (!tenantId.value) {
    ElMessage.warning('缺少租户参数，请从租户列表进入');
    goBack();
    return;
  }
  getList();
}

/**
 * 分页查询租户配置列表
 */
function getList() {
  loading.value = true;
  pageTenantConfigListAPI(query.value).then(res => {
    list.value = res.data.rows;
    total.value = res.data.total;
    loading.value = false;
  })
}

/**
 * 搜索配置，重置到第一页
 */
function handleSearch() {
  query.value.pageNo = 1;
  getList();
}

/**
 * 返回租户列表
 */
function goBack() {
  router.push('/site/sys/tenant');
}

/**
 * 打开新建配置抽屉
 */
function openCreateForm() {
  form.value = { id: undefined, tenantId: tenantId.value, name: undefined, key: undefined, value: undefined };
  formTitle.value = '新建配置';
  formVisible.value = true;
}

/**
 * 打开修改配置抽屉
 * @param row 配置行数据
 */
function openUpdateForm(row) {
  form.value = {
    id: row.id,
    tenantId: row.tenantId,
    name: row.name,
    key: row.key,
    value: row.value,
  };
  formTitle.value = '修改配置';
  formVisible.value = true;
}

/**
 * 关闭配置抽屉
 */
function closeForm() {
  formVisible.value = false;
}

/**
 * 提交配置表单（新增/修改）
 */
function submitForm() {
  formRef.value.validate(valid => {
    if (!valid) {
      return;
    }
    const data = { ...form.value };
    const api = data.id ? updateTenantConfigAPI : createTenantConfigAPI;
    api(data).then(res => {
      if (res.code !== 200) {
        return;
      }
      ElMessage.success(data.id ? '修改成功' : '创建成功');
      closeForm();
      getList();
    })
  });
}

/**
 * 删除配置
 * @param row 配置行数据
 */
function deleteRow(row) {
  ElMessageBox.confirm('是否确定删除此配置?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteTenantConfigAPI({ id: row.id }).then(res => {
      if (res.code !== 200) {
        return;
      }
      ElMessage.success('删除成功');
      getList();
    })
  }).catch(() => {
  })
}
</script>

<style scoped lang="scss">
.config-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: $spacing-sm;
  .config-title {
    flex: none;
    font-size: 14px;
    color: $color-text-primary;
    margin-right: $spacing-sm;
    white-space: nowrap;
  }
  .config-back-btn {
    margin-left: auto;
  }
}
</style>

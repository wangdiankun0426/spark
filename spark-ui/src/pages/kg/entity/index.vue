<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-button
          @click="handleBack"
      >
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
      <el-button
          type="primary"
          @click="handleOpenCreateForm"
      >
        <el-icon><Plus /></el-icon>新建实体
      </el-button>
      <el-button
          type="warning"
          @click="handleResetQuery"
      >
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button
          type="info"
          @click="handleGetEntityList"
      >
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--实体列表-->
    <div>
      <el-table
          ref="tableRef"
          height="calc(100vh - 155px)"
          :data="entityList"
          highlight-current-row
          @sort-change="handleSortChange"
          :header-cell-style="handleHeaderCellClass"
          border
      >
        <el-table-column prop="id" label="编号" width="80" align="center"/>
        <el-table-column prop="name" label="名称" min-width="180" align="left">
          <template #header>
            名称
            <el-popover
                :visible="searchFlag.name"
                placement="bottom"
                :width="200"
                trigger="click">
              <template #reference>
                <el-button
                    :type="searchFlag.name ? 'primary':'info'"
                    link
                    :icon="Search"
                    @click.stop="searchFlag.name = !searchFlag.name"
                />
              </template>
              <div>
                <el-input
                    v-model="query.name"
                    placeholder="请输入名称"
                    clearable
                    @input="handleGetEntityList"
                />
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="实体类型" min-width="140" align="center">
          <template #header>
            实体类型
            <el-popover
                :visible="searchFlag.type"
                placement="bottom"
                :width="200"
                trigger="click">
              <template #reference>
                <el-button
                    :type="searchFlag.type ? 'primary':'info'"
                    link
                    :icon="Search"
                    @click.stop="searchFlag.type = !searchFlag.type"
                />
              </template>
              <div>
                <el-input
                    v-model="query.type"
                    placeholder="请输入类型"
                    clearable
                    @input="handleGetEntityList"
                />
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="100" align="center">
          <template #header>
            状态
            <el-popover
                :visible="searchFlag.status"
                placement="bottom"
                :width="200"
                trigger="click">
              <template #reference>
                <el-button
                    :type="searchFlag.status ? 'primary':'info'"
                    link
                    :icon="Search"
                    @click.stop="searchFlag.status = !searchFlag.status"
                />
              </template>
              <div>
                <el-select
                    v-model="query.status"
                    placeholder="请选择状态"
                    clearable
                    @change="handleGetEntityList"
                >
                  <el-option label="有效" :value="1"/>
                  <el-option label="无效" :value="0"/>
                </el-select>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="createdByName" label="创建人" width="110" align="center"/>
        <el-table-column prop="createdDt" label="创建时间" width="160" align="center"/>
        <el-table-column fixed="right" label="操作" width="140" align="center">
          <template #default="scope">
            <el-button
                type="success"
                text
                @click="handleOpenUpdateForm(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              <span style="font-size: 12px; font-weight: 400">修改</span>
            </el-button>
            <el-button
                type="danger"
                text
                @click="handleDelete(scope.row.id)"
            >
              <el-icon><Delete /></el-icon>
              <span style="font-size: 12px; font-weight: 400">删除</span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!--分页组件-->
    <div>
      <el-pagination
          :current-page="query.pageNo"
          :page-size="query.pageSize"
          :page-sizes="pageSizes"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handlePageChangeSize"
          @current-change="handlePageChangeNo"
      />
    </div>
    <!--实体表单-->
    <el-drawer
        v-model="formVisible"
        :title="formTitle"
        direction="ltr"
        size="30%"
        :before-close="handleCloseForm"
        :close-on-click-modal="false"
        destroy-on-close
    >
      <el-form
          :model="form"
          label-width="auto"
          :rules="formRules"
          ref="formRef"
      >
        <el-form-item label="名称" prop="name">
          <el-input
              v-model="form.name"
              placeholder="请输入名称"
              maxlength="128"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="实体类型" prop="type">
          <el-select
              v-model="form.type"
              placeholder="请选择实体类型"
              style="width: 100%"
              filterable
          >
            <el-option
                v-for="item in entityTypeOptions"
                :key="item"
                :label="item"
                :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
              v-model="form.status"
              :active-value="1"
              :inactive-value="-1"
              active-text="已启用"
              inactive-text="已停用"
              inline-prompt
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请输入描述"
              maxlength="512"
              show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button type="primary" @click="handleSubmitForm">保存</el-button>
          <el-button @click="handleCloseForm">取消</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { getCurrentInstance, ref } from 'vue';
import { useRoute, onBeforeRouteUpdate, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, ArrowLeft } from '@element-plus/icons-vue';
import {
  pageEntityListAPI,
  createEntityAPI,
  updateEntityAPI,
  deleteEntityAPI
} from '@/api/kg/entity.js';
import { queryGraphDetailAPI } from '@/api/kg/graph.js';

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

// 查询条件
const query = ref({
  pageNo: 1,
  pageSize: 30,
  graphId: route.query.graphId ? Number(route.query.graphId) : undefined,
  name: undefined,
  type: undefined,
  status: undefined,
  sorts: {},
});
const searchFlag = ref({
  name: false,
  type: false,
  status: false,
});
const total = ref(0);
const pageSizes = [30, 50, 100];
const entityList = ref([]);

// 表单
const formVisible = ref(false);
const formTitle = ref('');
const formRef = ref(null);

function getDefaultFormData() {
  return {
    id: undefined,
    graphId: query.value.graphId,
    name: '',
    type: '',
    description: '',
    status: 1,
  };
}
const form = ref(getDefaultFormData());

const formRules = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { max: 128, message: '名称不能超过 128 个字符', trigger: 'blur' },
  ],
  type: [
    { required: true, message: '请选择实体类型', trigger: 'change' },
  ],
  description: [
    { max: 512, message: '描述不能超过 512 个字符', trigger: 'blur' },
  ],
};

// 实体类型选项（基于当前 graphId 从图谱详情中加载）
const entityTypeOptions = ref([]);

/**
 * 解析 schema 字符串为数组
 * @param {string} schemaStr
 * @returns {Array}
 */
function parseSchema(schemaStr) {
  if (!schemaStr) return [];
  try {
    const parsed = JSON.parse(schemaStr);
    return Array.isArray(parsed) ? parsed : [];
  } catch (e) {
    return [];
  }
}

/**
 * 按当前 graphId 加载图谱详情，解析实体类型选项
 */
function loadGraphSchema() {
  if (!query.value.graphId) {
    entityTypeOptions.value = [];
    return;
  }
  queryGraphDetailAPI({ id: query.value.graphId }).then(res => {
    if (res.code === 200 && res.data) {
      entityTypeOptions.value = parseSchema(res.data.entityTypes);
    }
  });
}

handleGetEntityList();
loadGraphSchema();

/**
 * 返回上一页
 */
function handleBack() {
  router.back();
}

/**
 * 监听路由 graphId 变化（从图谱卡片跳转过来时会触发），同步查询条件与表单默认值
 */
onBeforeRouteUpdate(to => {
  const gid = to.query.graphId ? Number(to.query.graphId) : undefined;
  query.value.graphId = gid;
  form.value.graphId = gid;
  query.value.pageNo = 1;
  handleGetEntityList();
  loadGraphSchema();
});

/**
 * 查询实体列表
 */
function handleGetEntityList() {
  pageEntityListAPI(query.value).then(res => {
    entityList.value = (res.data.rows || []);
    total.value = res.data.total;
  });
}

/**
 * 重置查询条件
 */
function handleResetQuery() {
  query.value.pageNo = 1;
  query.value.pageSize = 30;
  query.value.name = undefined;
  query.value.type = undefined;
  query.value.status = undefined;
  searchFlag.value.name = false;
  searchFlag.value.type = false;
  searchFlag.value.status = false;
  // 清除排序状态
  const columns = proxy.$refs.tableRef.store.states.columns.value;
  columns.forEach((column) => {
    column.order = null;
  });
  query.value.sorts = {};
  handleGetEntityList();
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function handlePageChangeSize(pageSize) {
  query.value.pageSize = pageSize;
  handleGetEntityList();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handlePageChangeNo(pageNo) {
  query.value.pageNo = pageNo;
  handleGetEntityList();
}

/**
 * 多选排序表头样式
 * @param data
 */
function handleHeaderCellClass(data) {
  const property = data.column.property;
  const order = query.value.sorts[property];
  if (order === 'asc') {
    data.column.order = 'ascending';
  } else if (order === 'desc') {
    data.column.order = 'descending';
  } else {
    data.column.order = null;
  }
}

/**
 * 处理排序
 * @param column
 * @param prop
 * @param order
 */
function handleSortChange({ column, prop, order }) {
  if (order === 'ascending') {
    query.value.sorts[prop] = 'asc';
  } else if (order === 'descending') {
    query.value.sorts[prop] = 'desc';
  } else {
    query.value.sorts[prop] = null;
  }
  handleGetEntityList();
}

/**
 * 打开新建表单
 */
function handleOpenCreateForm() {
  Object.assign(form.value, getDefaultFormData());
  formTitle.value = '新建实体';
  formVisible.value = true;
}

/**
 * 打开修改表单
 * @param row
 */
function handleOpenUpdateForm(row) {
  Object.assign(form.value, getDefaultFormData(), row);
  formTitle.value = '修改实体';
  formVisible.value = true;
}

/**
 * 关闭表单
 */
function handleCloseForm() {
  Object.assign(form.value, getDefaultFormData());
  formTitle.value = '';
  formVisible.value = false;
}

/**
 * 提交表单
 */
function handleSubmitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (!valid) return;
    const data = {
      id: form.value.id,
      graphId: form.value.graphId,
      name: form.value.name,
      type: form.value.type,
      description: form.value.description,
      status: form.value.status,
    };
    if (!data.id) {
      createEntityAPI(data).then(res => {
        if (res.code !== 200) return;
        ElMessage.success('创建成功');
        handleCloseForm();
        handleGetEntityList();
      });
    } else {
      updateEntityAPI(data).then(res => {
        if (res.code !== 200) return;
        ElMessage.success('修改成功');
        handleCloseForm();
        handleGetEntityList();
      });
    }
  });
}

/**
 * 删除实体
 * @param id
 */
function handleDelete(id) {
  ElMessageBox.confirm('是否确定删除此条实体?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteEntityAPI({ id }).then(res => {
      if (res.code !== 200) return;
      ElMessage.success('删除成功');
      handleGetEntityList();
    });
  }).catch(() => {});
}
</script>

<style scoped lang="scss">
.drawer-footer {
  padding: 0 16px;
}
</style>

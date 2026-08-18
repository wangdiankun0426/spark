<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
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
          height="calc(100vh - 165px)"
          :data="entityList"
          highlight-current-row
          @sort-change="handleSortChange"
          :header-cell-style="handleHeaderCellClass"
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
        <el-table-column prop="graphName" label="所属图谱" min-width="160" align="center" show-overflow-tooltip>
          <template #header>
            所属图谱
            <el-popover
                :visible="searchFlag.graphId"
                placement="bottom"
                :width="220"
                trigger="click">
              <template #reference>
                <el-button
                    :type="searchFlag.graphId ? 'primary':'info'"
                    link
                    :icon="Search"
                    @click.stop="searchFlag.graphId = !searchFlag.graphId"
                />
              </template>
              <div>
                <el-select
                    v-model="query.graphId"
                    placeholder="请选择图谱"
                    clearable
                    filterable
                    style="width: 100%"
                    @change="handleGetEntityList"
                >
                  <el-option
                      v-for="item in graphOptions"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id"
                  />
                </el-select>
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
        <el-form-item label="所属图谱" prop="graphId">
          <el-select
              v-model="form.graphId"
              placeholder="请选择所属图谱"
              style="width: 100%"
              filterable
              @change="handleFormGraphChange"
          >
            <el-option
                v-for="item in graphOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="实体类型" prop="type">
          <el-select
              v-model="form.type"
              placeholder="请选择实体类型"
              style="width: 100%"
              filterable
              :disabled="!form.graphId"
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
              :inactive-value="0"
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
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import {
  pageEntityListAPI,
  updateEntityAPI,
  deleteEntityAPI
} from '@/api/kg/entity.js';
import { pageGraphListAPI } from '@/api/kg/graph.js';

const { proxy } = getCurrentInstance();

// 查询条件
const query = ref({
  pageNo: 1,
  pageSize: 30,
  graphId: undefined,
  name: undefined,
  type: undefined,
  status: undefined,
  sorts: {},
});
const searchFlag = ref({
  name: false,
  graphId: false,
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
    graphId: undefined,
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
  graphId: [
    { required: true, message: '请选择所属图谱', trigger: 'change' },
  ],
  type: [
    { required: true, message: '请选择实体类型', trigger: 'change' },
  ],
  description: [
    { max: 512, message: '描述不能超过 512 个字符', trigger: 'blur' },
  ],
};

// 图谱选项
const graphOptions = ref([]);
// 实体类型选项（随当前表单 graphId 联动）
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
 * 加载图谱选项列表（全量）
 */
function loadGraphOptions() {
  pageGraphListAPI({ page: false }).then(res => {
    if (res.code === 200 && res.data) {
      graphOptions.value = res.data.rows || [];
    }
  });
}

handleGetEntityList();
loadGraphOptions();

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
  query.value.graphId = undefined;
  query.value.name = undefined;
  query.value.type = undefined;
  query.value.status = undefined;
  searchFlag.value.name = false;
  searchFlag.value.graphId = false;
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
 * 表单切换图谱：清空已选实体类型并加载该图谱的实体类型选项
 */
function handleFormGraphChange() {
  form.value.type = '';
  if (!form.value.graphId) {
    entityTypeOptions.value = [];
    return;
  }
  const selected = graphOptions.value.find(g => g.id === form.value.graphId);
  entityTypeOptions.value = selected ? parseSchema(selected.entityTypes) : [];
}

/**
 * 打开修改表单
 * @param row
 */
function handleOpenUpdateForm(row) {
  Object.assign(form.value, getDefaultFormData(), row);
  formTitle.value = '修改实体';
  formVisible.value = true;
  // 加载该图谱的实体类型选项，以便回显
  if (form.value.graphId) {
    const selected = graphOptions.value.find(g => g.id === form.value.graphId);
    entityTypeOptions.value = selected ? parseSchema(selected.entityTypes) : [];
  }
}

/**
 * 关闭表单
 */
function handleCloseForm() {
  Object.assign(form.value, getDefaultFormData());
  entityTypeOptions.value = [];
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
    updateEntityAPI(data).then(res => {
      if (res.code !== 200) return;
      ElMessage.success('修改成功');
      handleCloseForm();
      handleGetEntityList();
    });
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

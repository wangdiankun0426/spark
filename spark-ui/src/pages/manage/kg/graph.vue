<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-button
          type="primary"
          @click="handleOpenCreateForm"
      >
        <el-icon><Plus /></el-icon>新建图谱
      </el-button>
      <el-button
          type="warning"
          @click="handleResetQuery"
      >
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button
          type="info"
          @click="handleGetGraphList"
      >
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--知识图谱列表-->
    <div>
      <el-table
          ref="tableRef"
          height="calc(100vh - 165px)"
          :data="graphList"
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
                    @input="handleGetGraphList"
                />
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="extractModelName" label="抽取模型" min-width="160" align="center" show-overflow-tooltip/>
        <el-table-column prop="entityCount" label="实体数量" width="100" align="center"/>
        <el-table-column prop="relationCount" label="关系数量" width="100" align="center"/>
        <el-table-column prop="entityTypes" label="实体类型" width="110" align="center"/>
        <el-table-column prop="relationTypes" label="关系类型" width="110" align="center"/>
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
                    @change="handleGetGraphList"
                >
                  <el-option label="已启用" :value="1"/>
                  <el-option label="已停用" :value="0"/>
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
              <span style="font-size: 12px; font-weight: 500">修改</span>
            </el-button>
            <el-button
                type="danger"
                text
                @click="handleDelete(scope.row.id)"
            >
              <el-icon><Delete /></el-icon>
              <span style="font-size: 12px; font-weight: 500">删除</span>
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
    <!--知识图谱表单-->
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
        <el-form-item label="抽取模型" prop="extractModelId">
          <el-select
              v-model="form.extractModelId"
              placeholder="请选择抽取模型"
              style="width: 100%"
              filterable
          >
            <el-option
                v-for="item in extractModelOptions"
                :key="item.id"
                :label="item.providerName + ' - ' + item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="块大小" prop="chunkSize">
          <el-input-number
              v-model="form.chunkSize"
              :min="1"
              :max="10000"
              :step="100"
              controls-position="right"
              style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="块重叠" prop="overlap">
          <el-input-number
              v-model="form.overlap"
              :min="0"
              :max="1000"
              :step="50"
              controls-position="right"
              style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              placeholder="请输入描述"
              maxlength="512"
              show-word-limit
          />
        </el-form-item>
        <el-divider content-position="left">实体类型 Schema</el-divider>
        <div class="schema-edit-wrapper">
          <el-table :data="form.entityTypeList" border size="small">
            <el-table-column label="序号" type="index" width="60" align="center"/>
            <el-table-column label="名称" min-width="280" align="center">
              <template #default="{ $index }">
                <el-input
                    :model-value="form.entityTypeList[$index]"
                    @update:model-value="val => form.entityTypeList[$index] = val"
                    placeholder="请输入名称"
                    maxlength="64"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button
                    type="danger"
                    text
                    @click="handleRemoveEntityType($index)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button
              class="schema-add-btn"
              type="primary"
              plain
              size="small"
              @click="handleAddEntityType"
          >
            <el-icon><Plus /></el-icon>添加实体类型
          </el-button>
        </div>
        <el-divider content-position="left">关系类型 Schema</el-divider>
        <div class="schema-edit-wrapper">
          <el-table :data="form.relationTypeList" border size="small">
            <el-table-column label="序号" type="index" width="60" align="center"/>
            <el-table-column label="名称" min-width="280" align="center">
              <template #default="{ $index }">
                <el-input
                    :model-value="form.relationTypeList[$index]"
                    @update:model-value="val => form.relationTypeList[$index] = val"
                    placeholder="请输入名称"
                    maxlength="64"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button
                    type="danger"
                    text
                    @click="handleRemoveRelationType($index)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button
              class="schema-add-btn"
              type="primary"
              plain
              size="small"
              @click="handleAddRelationType"
          >
            <el-icon><Plus /></el-icon>添加关系类型
          </el-button>
        </div>
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
  pageGraphListAPI,
  createGraphAPI,
  updateGraphAPI,
  deleteGraphAPI
} from '@/api/kg/graph.js';
import { pageModelListAPI } from '@/api/llm/model.js';

const { proxy } = getCurrentInstance();

// 查询条件
const query = ref({
  pageNo: 1,
  pageSize: 30,
  name: undefined,
  status: undefined,
  sorts: {},
});
const searchFlag = ref({
  name: false,
  status: false,
});
const total = ref(0);
const pageSizes = [30, 50, 100];
const graphList = ref([]);

// 表单
const formVisible = ref(false);
const formTitle = ref('');
const formRef = ref(null);

function getDefaultFormData() {
  return {
    id: undefined,
    name: '',
    description: '',
    entityTypes: '',
    relationTypes: '',
    entityTypeList: [],
    relationTypeList: [],
    extractModelId: undefined,
    chunkSize: 500,
    overlap: 50,
    status: 1,
  };
}
const form = ref(getDefaultFormData());

const formRules = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { max: 128, message: '名称不能超过 128 个字符', trigger: 'blur' },
  ],
  description: [
    { max: 512, message: '描述不能超过 512 个字符', trigger: 'blur' },
  ],
  extractModelId: [
    { required: true, message: '请选择抽取模型', trigger: 'change' },
  ],
  chunkSize: [
    { required: true, message: '请输入块大小', trigger: 'blur' },
    { type: 'number', min: 1, max: 10000, message: '块大小需在 1-10000 之间', trigger: 'blur' },
  ],
  overlap: [
    { required: true, message: '请输入块重叠', trigger: 'blur' },
    { type: 'number', min: 0, max: 1000, message: '块重叠需在 0-1000 之间', trigger: 'blur' },
  ],
};

// 抽取模型选项
const extractModelOptions = ref([]);

/**
 * 加载抽取模型选项列表（语言模型类型）
 */
function loadExtractModelOptions() {
  pageModelListAPI({ page: false, type: 1 }).then(res => {
    if (res.code === 200 && res.data) {
      extractModelOptions.value = res.data.rows || [];
    }
  });
}

handleGetGraphList();
loadExtractModelOptions();

/**
 * 查询知识图谱列表
 */
function handleGetGraphList() {
  pageGraphListAPI(query.value).then(res => {
    graphList.value = (res.data.rows || []);
    total.value = res.data.total;
  });
}

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
 * 重置查询条件
 */
function handleResetQuery() {
  query.value.pageNo = 1;
  query.value.pageSize = 30;
  query.value.name = undefined;
  query.value.status = undefined;
  searchFlag.value.name = false;
  searchFlag.value.status = false;
  // 清除排序状态
  const columns = proxy.$refs.tableRef.store.states.columns.value;
  columns.forEach((column) => {
    column.order = null;
  });
  query.value.sorts = {};
  handleGetGraphList();
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function handlePageChangeSize(pageSize) {
  query.value.pageSize = pageSize;
  handleGetGraphList();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handlePageChangeNo(pageNo) {
  query.value.pageNo = pageNo;
  handleGetGraphList();
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
  handleGetGraphList();
}

/**
 * 打开新建表单
 */
function handleOpenCreateForm() {
  Object.assign(form.value, getDefaultFormData());
  formTitle.value = '新建图谱';
  formVisible.value = true;
}

/**
 * 打开修改表单
 * @param row
 */
function handleOpenUpdateForm(row) {
  const entityTypeList = parseSchema(row.entityTypes);
  const relationTypeList = parseSchema(row.relationTypes);
  Object.assign(form.value, getDefaultFormData(), row, {
    entityTypeList,
    relationTypeList,
  });
  formTitle.value = '修改图谱';
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
 * 添加实体类型行
 */
function handleAddEntityType() {
  form.value.entityTypeList.push('');
}

/**
 * 删除实体类型行
 * @param index
 */
function handleRemoveEntityType(index) {
  form.value.entityTypeList.splice(index, 1);
}

/**
 * 添加关系类型行
 */
function handleAddRelationType() {
  form.value.relationTypeList.push('');
}

/**
 * 删除关系类型行
 * @param index
 */
function handleRemoveRelationType(index) {
  form.value.relationTypeList.splice(index, 1);
}

/**
 * 提交表单
 */
function handleSubmitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (!valid) return;
    const data = {
      id: form.value.id,
      name: form.value.name,
      description: form.value.description,
      entityTypes: JSON.stringify(form.value.entityTypeList || []),
      relationTypes: JSON.stringify(form.value.relationTypeList || []),
      extractModelId: form.value.extractModelId,
      chunkSize: form.value.chunkSize,
      overlap: form.value.overlap,
      status: form.value.status,
    };
    if (!data.id) {
      createGraphAPI(data).then(res => {
        if (res.code !== 200) return;
        ElMessage.success('创建成功');
        handleCloseForm();
        handleGetGraphList();
      });
    } else {
      updateGraphAPI(data).then(res => {
        if (res.code !== 200) return;
        ElMessage.success('修改成功');
        handleCloseForm();
        handleGetGraphList();
      });
    }
  });
}

/**
 * 删除知识图谱
 * @param id
 */
function handleDelete(id) {
  ElMessageBox.confirm('是否确定删除此条知识图谱?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteGraphAPI({ id }).then(res => {
      if (res.code !== 200) return;
      ElMessage.success('删除成功');
      handleGetGraphList();
    });
  }).catch(() => {});
}
</script>

<style scoped lang="scss">
.drawer-footer {
  padding: 0 16px;
}
.schema-edit-wrapper {
  padding: 0 8px;
}
.schema-add-btn {
  margin-top: 8px;
  width: 100%;
}
</style>

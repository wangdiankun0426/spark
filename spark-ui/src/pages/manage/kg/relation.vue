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
          @click="handleGetRelationList"
      >
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--关系列表-->
    <div>
      <el-table
          ref="tableRef"
          height="calc(100vh - 165px)"
          :data="relationList"
          highlight-current-row
          @sort-change="handleSortChange"
          :header-cell-style="handleHeaderCellClass"
          border
      >
        <el-table-column prop="id" label="编号" width="80" align="center"/>
        <el-table-column prop="graphName" label="所属图谱" min-width="150" align="center" show-overflow-tooltip>
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
                    @change="handleGraphChange"
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
        <el-table-column prop="headEntityName" label="头实体" min-width="150" align="center" show-overflow-tooltip/>
        <el-table-column prop="tailEntityName" label="尾实体" min-width="150" align="center" show-overflow-tooltip/>
        <el-table-column prop="relationType" label="关系类型" min-width="140" align="center">
          <template #header>
            关系类型
            <el-popover
                :visible="searchFlag.relationType"
                placement="bottom"
                :width="200"
                trigger="click">
              <template #reference>
                <el-button
                    :type="searchFlag.relationType ? 'primary':'info'"
                    link
                    :icon="Search"
                    @click.stop="searchFlag.relationType = !searchFlag.relationType"
                />
              </template>
              <div>
                <el-input
                    v-model="query.relationType"
                    placeholder="请输入关系类型"
                    clearable
                    @input="handleGetRelationList"
                />
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="关系权重" width="110" align="center"/>
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
                    @change="handleGetRelationList"
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
    <!--关系表单-->
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
        <el-form-item label="头实体" prop="headEntityId">
          <el-select
              v-model="form.headEntityId"
              placeholder="请选择头实体"
              style="width: 100%"
              filterable
              :disabled="!form.graphId"
          >
            <el-option
                v-for="item in entityOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="尾实体" prop="tailEntityId">
          <el-select
              v-model="form.tailEntityId"
              placeholder="请选择尾实体"
              style="width: 100%"
              filterable
              :disabled="!form.graphId"
          >
            <el-option
                v-for="item in entityOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关系类型" prop="relationType">
          <el-select
              v-model="form.relationType"
              placeholder="请选择关系类型"
              style="width: 100%"
              filterable
              :disabled="!form.graphId"
          >
            <el-option
                v-for="item in relationTypeOptions"
                :key="item"
                :label="item"
                :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关系权重" prop="weight">
          <el-input-number
              v-model="form.weight"
              :min="0"
              :max="999999"
              :step="0.01"
              :precision="2"
              controls-position="right"
              style="width: 100%"
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
  pageRelationListAPI,
  updateRelationAPI,
  deleteRelationAPI
} from '@/api/kg/relation.js';
import { pageGraphListAPI } from '@/api/kg/graph.js';
import { pageEntityListAPI } from '@/api/kg/entity.js';

const { proxy } = getCurrentInstance();

// 查询条件
const query = ref({
  pageNo: 1,
  pageSize: 30,
  graphId: undefined,
  relationType: undefined,
  status: undefined,
  sorts: {},
});
const searchFlag = ref({
  graphId: false,
  relationType: false,
  status: false,
});
const total = ref(0);
const pageSizes = [30, 50, 100];
const relationList = ref([]);

// 表单
const formVisible = ref(false);
const formTitle = ref('');
const formRef = ref(null);

function getDefaultFormData() {
  return {
    id: undefined,
    graphId: undefined,
    headEntityId: undefined,
    tailEntityId: undefined,
    relationType: '',
    weight: 1.00,
    status: 1,
  };
}
const form = ref(getDefaultFormData());

const formRules = {
  graphId: [
    { required: true, message: '请选择所属图谱', trigger: 'change' },
  ],
  headEntityId: [
    { required: true, message: '请选择头实体', trigger: 'change' },
  ],
  tailEntityId: [
    { required: true, message: '请选择尾实体', trigger: 'change' },
  ],
  relationType: [
    { required: true, message: '请选择关系类型', trigger: 'change' },
  ],
};

// 图谱选项
const graphOptions = ref([]);
// 实体选项（随当前表单 graphId 联动）
const entityOptions = ref([]);
// 关系类型选项（随当前表单 graphId 联动）
const relationTypeOptions = ref([]);

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

handleGetRelationList();
loadGraphOptions();

/**
 * 查询关系列表
 */
function handleGetRelationList() {
  pageRelationListAPI(query.value).then(res => {
    relationList.value = (res.data.rows || []);
    total.value = res.data.total;
  });
}

/**
 * 列表筛选切换图谱：同步重置实体无关条件并刷新
 */
function handleGraphChange() {
  handleGetRelationList();
}

/**
 * 表单切换图谱：清空头/尾实体与关系类型，并加载该图谱下的实体选项与关系类型选项
 */
function handleFormGraphChange() {
  form.value.headEntityId = undefined;
  form.value.tailEntityId = undefined;
  form.value.relationType = '';
  if (!form.value.graphId) {
    entityOptions.value = [];
    relationTypeOptions.value = [];
    return;
  }
  const selected = graphOptions.value.find(g => g.id === form.value.graphId);
  relationTypeOptions.value = selected ? parseSchema(selected.relationTypes) : [];
  pageEntityListAPI({ page: false, graphId: form.value.graphId }).then(res => {
    if (res.code === 200 && res.data) {
      entityOptions.value = res.data.rows || [];
    }
  });
}

/**
 * 重置查询条件
 */
function handleResetQuery() {
  query.value.pageNo = 1;
  query.value.pageSize = 30;
  query.value.graphId = undefined;
  query.value.relationType = undefined;
  query.value.status = undefined;
  searchFlag.value.graphId = false;
  searchFlag.value.relationType = false;
  searchFlag.value.status = false;
  // 清除排序状态
  const columns = proxy.$refs.tableRef.store.states.columns.value;
  columns.forEach((column) => {
    column.order = null;
  });
  query.value.sorts = {};
  handleGetRelationList();
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function handlePageChangeSize(pageSize) {
  query.value.pageSize = pageSize;
  handleGetRelationList();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handlePageChangeNo(pageNo) {
  query.value.pageNo = pageNo;
  handleGetRelationList();
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
  handleGetRelationList();
}

/**
 * 打开修改表单
 * @param row
 */
function handleOpenUpdateForm(row) {
  Object.assign(form.value, getDefaultFormData(), row);
  formTitle.value = '修改关系';
  formVisible.value = true;
  // 加载该图谱下的实体选项与关系类型选项，以便回显头/尾实体名称与关系类型
  if (form.value.graphId) {
    const selected = graphOptions.value.find(g => g.id === form.value.graphId);
    relationTypeOptions.value = selected ? parseSchema(selected.relationTypes) : [];
    pageEntityListAPI({ page: false, graphId: form.value.graphId }).then(res => {
      if (res.code === 200 && res.data) {
        entityOptions.value = res.data.rows || [];
      }
    });
  }
}

/**
 * 关闭表单
 */
function handleCloseForm() {
  Object.assign(form.value, getDefaultFormData());
  entityOptions.value = [];
  relationTypeOptions.value = [];
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
      headEntityId: form.value.headEntityId,
      tailEntityId: form.value.tailEntityId,
      relationType: form.value.relationType,
      weight: form.value.weight,
      status: form.value.status,
    };
    updateRelationAPI(data).then(res => {
      if (res.code !== 200) return;
      ElMessage.success('修改成功');
      handleCloseForm();
      handleGetRelationList();
    });
  });
}

/**
 * 删除关系
 * @param id
 */
function handleDelete(id) {
  ElMessageBox.confirm('是否确定删除此条关系?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteRelationAPI({ id }).then(res => {
      if (res.code !== 200) return;
      ElMessage.success('删除成功');
      handleGetRelationList();
    });
  }).catch(() => {});
}
</script>

<style scoped lang="scss">
.drawer-footer {
  padding: 0 16px;
}
</style>

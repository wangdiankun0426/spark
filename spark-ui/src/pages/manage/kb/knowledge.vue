<template>
  <div class="app-container">
    <!--操作按钮-->
    <div>
      <el-button
          type="primary"
          @click="handleOpenCreateForm"
      >
        <el-icon><Plus /></el-icon>新建知识库
      </el-button>
      <el-button
          type="warning"
          @click="handleResetQuery"
      >
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button
          type="info"
          @click="handleGetKnowledgeList"
      >
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>
    <!--知识库列表-->
    <div>
      <el-table
          ref="tableRef"
          height="calc(100vh - 165px)"
          :data="knowledgeList"
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
                    @input="handleGetKnowledgeList"
                />
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" align="center" show-overflow-tooltip/>
        <el-table-column prop="vectorModelName" label="向量模型" min-width="160" align="center" show-overflow-tooltip/>
        <el-table-column prop="rerankModelName" label="排序模型" min-width="160" align="center" show-overflow-tooltip/>
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
                    @change="handleGetKnowledgeList"
                >
                  <el-option label="已启用" :value="1"/>
                  <el-option label="已停用" :value="0"/>
                </el-select>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="parentChunkSize" label="父块大小" width="100" align="center"/>
        <el-table-column prop="childChunkSize" label="子块大小" width="100" align="center"/>
        <el-table-column prop="parentOverlap" label="父块重叠" width="100" align="center"/>
        <el-table-column prop="childOverlap" label="子块重叠" width="100" align="center"/>
        <el-table-column prop="retrieveTopK" label="召回TopK" width="100" align="center"/>
        <el-table-column prop="minSimilarity" label="最小相似度" width="110" align="center"/>
        <el-table-column prop="enableQa" label="生成QA" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.enableQa === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.enableQa === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="documentCount" label="文档数" width="90" align="center"/>
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
    <!--知识库表单-->
    <el-drawer
        v-model="formVisible"
        :title="formTitle"
        direction="ltr"
        size="40%"
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
              :rows="5"
              placeholder="请输入描述"
              maxlength="512"
              show-word-limit
          />
        </el-form-item>
        <el-divider content-position="left">分块配置</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="父块大小" prop="parentChunkSize">
              <el-input-number v-model="form.parentChunkSize" :min="1" :max="99999999" controls-position="right" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="子块大小" prop="childChunkSize">
              <el-input-number v-model="form.childChunkSize" :min="1" :max="99999999" controls-position="right" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="父块重叠" prop="parentOverlap">
              <el-input-number v-model="form.parentOverlap" :min="0" :max="99999999" controls-position="right" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="子块重叠" prop="childOverlap">
              <el-input-number v-model="form.childOverlap" :min="0" :max="99999999" controls-position="right" style="width: 100%"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">召回配置</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="向量模型" prop="vectorModelId">
              <el-select
                  v-model="form.vectorModelId"
                  placeholder="请选择向量模型"
                  style="width: 100%"
                  filterable
              >
                <el-option
                    v-for="item in vectorModelOptions"
                    :key="item.id"
                    :label="item.providerName + ' - ' + item.name"
                    :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序模型" prop="rerankModelId">
              <el-select
                  v-model="form.rerankModelId"
                  placeholder="请选择排序模型"
                  style="width: 100%"
                  filterable
                  clearable
              >
                <el-option
                    v-for="item in rerankModelOptions"
                    :key="item.id"
                    :label="item.providerName + ' - ' + item.name"
                    :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="向量召回TopK" prop="retrieveTopK">
              <el-input-number v-model="form.retrieveTopK" :min="1" :max="99999999" controls-position="right" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最小相似度" prop="minSimilarity">
              <el-input-number v-model="form.minSimilarity" :min="0.01" :max="1" :step="0.01" :precision="2" controls-position="right" style="width: 100%"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">高级选项</el-divider>
        <el-form-item label="生成QA" prop="enableQa">
          <el-switch v-model="form.enableQa" :active-value="1" :inactive-value="0"/>
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
  pageKnowledgeListAPI,
  createKnowledgeAPI,
  updateKnowledgeAPI,
  deleteKnowledgeAPI
} from '@/api/kb/knowledge.js';
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
const knowledgeList = ref([]);

// 表单
const formVisible = ref(false);
const formTitle = ref('');
const formRef = ref(null);

function getDefaultFormData() {
  return {
    id: undefined,
    name: '',
    description: '',
    vectorModelId: undefined,
    rerankModelId: undefined,
    parentChunkSize: 800,
    childChunkSize: 200,
    parentOverlap: 100,
    childOverlap: 20,
    enableQa: 1,
    retrieveTopK: 10,
    minSimilarity: 0.40,
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
  vectorModelId: [
    { required: true, message: '请选择向量模型', trigger: 'change' },
  ],
};

// 向量模型选项
const vectorModelOptions = ref([]);
// 排序模型选项
const rerankModelOptions = ref([]);

/**
 * 加载向量模型选项列表（仅查询向量模型类型）
 */
function loadVectorModelOptions() {
  pageModelListAPI({ page: false, type: 2 }).then(res => {
    if (res.code === 200 && res.data) {
      vectorModelOptions.value = res.data.rows || [];
    }
  });
}

/**
 * 加载排序模型选项列表（仅查询排序模型类型）
 */
function loadRerankModelOptions() {
  pageModelListAPI({ page: false, type: 3 }).then(res => {
    if (res.code === 200 && res.data) {
      rerankModelOptions.value = res.data.rows || [];
    }
  });
}

handleGetKnowledgeList();
loadVectorModelOptions();
loadRerankModelOptions();

/**
 * 查询知识库列表
 */
function handleGetKnowledgeList() {
  pageKnowledgeListAPI(query.value).then(res => {
    knowledgeList.value = res.data.rows || [];
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
  query.value.status = undefined;
  searchFlag.value.name = false;
  searchFlag.value.status = false;
  // 清除排序状态
  const columns = proxy.$refs.tableRef.store.states.columns.value;
  columns.forEach((column) => {
    column.order = null;
  });
  query.value.sorts = {};
  handleGetKnowledgeList();
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
function handlePageChangeSize(pageSize) {
  query.value.pageSize = pageSize;
  handleGetKnowledgeList();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
function handlePageChangeNo(pageNo) {
  query.value.pageNo = pageNo;
  handleGetKnowledgeList();
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
  handleGetKnowledgeList();
}

/**
 * 打开新建表单
 */
function handleOpenCreateForm() {
  Object.assign(form.value, getDefaultFormData());
  formTitle.value = '新建知识库';
  formVisible.value = true;
}

/**
 * 打开修改表单
 * @param row
 */
function handleOpenUpdateForm(row) {
  Object.assign(form.value, getDefaultFormData(), row);
  formTitle.value = '修改知识库';
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
    const data = { ...form.value };
    if (!data.id) {
      createKnowledgeAPI(data).then(res => {
        if (res.code !== 200) return;
        ElMessage.success('创建成功');
        handleCloseForm();
        handleGetKnowledgeList();
      });
    } else {
      updateKnowledgeAPI(data).then(res => {
        if (res.code !== 200) return;
        ElMessage.success('修改成功');
        handleCloseForm();
        handleGetKnowledgeList();
      });
    }
  });
}

/**
 * 删除知识库
 * @param id
 */
function handleDelete(id) {
  ElMessageBox.confirm('是否确定删除此条知识库?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteKnowledgeAPI({ id }).then(res => {
      if (res.code !== 200) return;
      ElMessage.success('删除成功');
      handleGetKnowledgeList();
    });
  }).catch(() => {});
}
</script>

<style scoped lang="scss">
.drawer-footer {
  padding: 0 16px;
}
</style>

<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="graph-header">
      <div class="graph-header-left">
        <div class="graph-header-title">
          知识图谱
        </div>
        <div class="graph-header-subtitle">浏览知识图谱的实体与关系 Schema，支撑图谱检索与问答</div>
      </div>
      <div class="graph-header-right">
        <el-input
            v-model="keyword"
            placeholder="搜索图谱名称"
            clearable
            :prefix-icon="Search"
            style="width: 300px"
        />
        <el-button type="primary" @click="handleOpenCreateForm">
          <el-icon><Plus /></el-icon>新增知识图谱
        </el-button>
      </div>
    </div>

    <!-- 知识图谱卡片网格 -->
    <div
        class="graph-grid"
        v-if="graphList.length">
      <info-card
          v-for="item in graphList"
          :key="item.id"
          :icon="Graph"
          :title="item.name"
          :description="item.description || '暂无描述'"
          :disabled="item.status === 0"
          :actions="cardActions(item)"
      />
    </div>
    <!-- 空状态 -->
    <el-empty
        class="empty-grid"
        v-else
        :description="keyword ? '未找到匹配的知识图谱' : '暂无知识图谱'"
        :image-size="120"
    />

    <!-- 分页 -->
    <el-pagination
        :current-page="query.pageNo"
        :page-size="query.pageSize"
        :page-sizes="pageSizes"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
    />
    <!-- 新增 / 修改 知识图谱表单抽屉 -->
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
          <el-table :data="form.entityTypeList" border>
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
                <el-button type="danger" text @click="handleRemoveEntityType($index)">
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
          <el-table :data="form.relationTypeList" border>
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
                <el-button type="danger" text @click="handleRemoveRelationType($index)">
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
      <el-alert
          type="info"
          :closable="false"
          show-icon
      >
        <template #title>
          <div class="form-tip">
            <div>名称为必填项，用于在知识图谱列表中标识与检索；</div>
            <div>抽取模型用于从文档中识别实体与它们之间的关系，建议选择语言类型模型；</div>
            <div>块大小决定每次送入模型抽取的文本长度，过大会超出模型上下文、过小会割裂语义；</div>
            <div>块重叠用于避免实体或关系正好落在块的边界被切断，通常取块大小的 10%~20%；</div>
            <div>实体类型与关系类型用于限定抽取结果中允许出现的类别，建议只保留业务真正关注的类型；</div>
            <div>停用的知识图谱不会参与新文档的抽取。</div>
          </div>
        </template>
      </el-alert>
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
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageGraphListAPI,
  createGraphAPI,
  updateGraphAPI,
  deleteGraphAPI
} from '@/api/kg/graph.js'
import { pageModelListAPI } from '@/api/llm/model.js'
import { Search, Plus, Delete } from '@element-plus/icons-vue'
import InfoCard from '@/components/InfoCard/index.vue'
import Graph from "@/assets/icons/graph.vue";

const router = useRouter()

const graphList = ref([])
const total = ref(0)
const pageSizes = [15, 30, 50]
const keyword = ref('')
// 分页查询条件
const query = ref({
  pageNo: 1,
  pageSize: 15,
})

let searchTimer = null

// 新增 / 修改 表单
const formVisible = ref(false)
const formTitle = ref('')
const formRef = ref(null)

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

onMounted(() => {
  loadGraphList()
  loadExtractModelOptions()
})

onBeforeUnmount(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})

/**
 * 名称搜索防抖，300ms 后回到第一页并重新查询
 */
watch(keyword, () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    query.value.pageNo = 1
    loadGraphList()
  }, 300)
})

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

/**
 * 分页查询知识图谱列表
 */
function loadGraphList() {
  const params = {
    pageNo: query.value.pageNo,
    pageSize: query.value.pageSize,
  }
  if (keyword.value) {
    params.name = keyword.value
  }
  pageGraphListAPI(params).then(res => {
    if (res.code === 200 && res.data) {
      graphList.value = (res.data.rows || []).map(row => ({
        ...row,
        entityTypeList: parseSchema(row.entityTypes),
        relationTypeList: parseSchema(row.relationTypes)
      }))
      total.value = res.data.total || 0
    }
  })
}

/**
 * 切换每页条数，回到第一页重新查询
 * @param size
 */
function handleSizeChange(size) {
  query.value.pageSize = size
  query.value.pageNo = 1
  loadGraphList()
}

/**
 * 切换页码重新查询
 * @param pageNo
 */
function handleCurrentChange(pageNo) {
  query.value.pageNo = pageNo
  loadGraphList()
}

/**
 * 卡片底部工具栏按钮
 * @param item
 */
function cardActions(item) {
  return [
    { key: 'document', label: '文档',icon: 'Document', onClick: () => handleOpenDocument(item) },
    { key: 'entity', label: '实体', icon: 'Ticket',onClick: () => handleOpenEntity(item) },
    { key: 'relation', label: '关系', icon: 'List',onClick: () => handleOpenRelation(item) },
    { key: 'graph', label: '图谱', icon: 'Discount',onClick: () => handleOpenGraphDetail(item) },
    { key: 'edit', label: '修改', icon: 'Edit',onClick: () => handleOpenUpdateForm(item) },
    { key: 'delete', label: '删除', icon: 'Delete',onClick: () => handleDelete(item) }
  ]
}

/**
 * 打开图谱的文档列表
 * @param item
 */
function handleOpenDocument(item) {
  router.push({ path: '/llm/graph/document', query: { prtId: item.id } })
}

/**
 * 打开图谱可视化详情页
 * @param item
 */
function handleOpenGraphDetail(item) {
  router.push({ path: '/llm/graph/detail', query: { graphId: item.id } })
}

/**
 * 跳转到实体列表
 * @param item
 */
function handleOpenEntity(item) {
  router.push({ path: '/llm/graph/entity', query: { graphId: item.id } })
}

/**
 * 跳转到关系列表
 * @param item
 */
function handleOpenRelation(item) {
  router.push({ path: '/llm/graph/relation', query: { graphId: item.id } })
}

/**
 * 解析 schema 字符串为数组
 * @param {string} schemaStr
 * @returns {Array}
 */
function parseSchema(schemaStr) {
  if (!schemaStr) return []
  try {
    const parsed = JSON.parse(schemaStr)
    return Array.isArray(parsed) ? parsed : []
  } catch (e) {
    return []
  }
}

/**
 * 打开新建表单
 */
function handleOpenCreateForm() {
  Object.assign(form.value, getDefaultFormData());
  formTitle.value = '新增知识图谱';
  formVisible.value = true;
}

/**
 * 打开修改表单
 * @param item
 */
function handleOpenUpdateForm(item) {
  const entityTypeList = parseSchema(item.entityTypes);
  const relationTypeList = parseSchema(item.relationTypes);
  Object.assign(form.value, getDefaultFormData(), item, {
    entityTypeList,
    relationTypeList,
  });
  formTitle.value = '修改知识图谱';
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
 * 提交表单（新增 / 修改）
 */
function handleSubmitForm() {
  formRef.value.validate(valid => {
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
        loadGraphList();
      });
    } else {
      updateGraphAPI(data).then(res => {
        if (res.code !== 200) return;
        ElMessage.success('修改成功');
        handleCloseForm();
        loadGraphList();
      });
    }
  });
}

/**
 * 删除知识图谱
 * @param item
 */
function handleDelete(item) {
  ElMessageBox.confirm('是否确定删除此条知识图谱?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteGraphAPI({ id: item.id }).then(res => {
      if (res.code !== 200) return;
      ElMessage.success('删除成功');
      // 删除当前页最后一条时回退上一页，避免停留在空页
      if (graphList.value.length === 1 && query.value.pageNo > 1) {
        query.value.pageNo -= 1
      }
      loadGraphList();
    });
  }).catch(() => {});
}
</script>

<style scoped lang="scss">
.graph-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  padding: $spacing-md $spacing-xl;
  background-color: $bg-card;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.graph-header-left {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.graph-header-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 20px;
  font-weight: 700;
  color: $color-text-primary;
}

.graph-header-subtitle {
  font-size: 13px;
  color: $color-text-secondary;
}

.graph-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.graph-grid {
  height: calc(100vh - #{$nav-height} - 190px);
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: $spacing-lg;
  align-content: start;
}
.empty-grid {
  height: calc(100vh - #{$nav-height} - 190px);
}

.drawer-footer {
  padding: 0 $spacing-md;
}

/* 抽屉底部表单填写说明 */
.form-tip {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  font-size: 12px;
  line-height: 1.6;
}

.schema-edit-wrapper {
  padding: 0 $spacing-sm;
}

.schema-add-btn {
  margin-top: $spacing-sm;
  width: 100%;
}
</style>

<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="knowledge-header">
      <div class="knowledge-header-left">
        <div class="knowledge-header-title">
          知识库
        </div>
        <div class="knowledge-header-subtitle">浏览知识库切片与召回参数，支撑语义检索与问答</div>
      </div>
      <div class="knowledge-header-right">
        <el-input
            v-model="keyword"
            placeholder="搜索知识库名称"
            clearable
            :prefix-icon="Search"
            style="width: 300px"
        />
        <el-button
            type="primary"
            @click="handleOpenCreateForm"
        >
          <el-icon><Plus /></el-icon>新增知识库
        </el-button>
      </div>
    </div>
    <!-- 知识库卡片网格 -->
    <div
        class="knowledge-grid"
        v-if="knowledgeList.length"
    >
      <info-card
          v-for="item in knowledgeList"
          :key="item.id"
          :theme="getTheme(item)"
          :icon="Collection"
          :title="item.name"
          :id-text="'编号 #' + item.id"
          :description="item.description || '暂无描述'"
          :disabled="item.status === 0"
          :height="340"
      >
        <!-- 状态徽章 -->
        <template #badge>
          <span class="badge" :class="item.status === 1 ? 'badge-primary' : 'badge-muted'">
            {{ item.statusName}}
          </span>
        </template>
        <!-- 配置标签 + 模型信息（中间内容区，标签下方展示） -->
        <template #tags>
          <el-tag size="small" effect="light" round>分块策略：{{ item.chunkStrategyName }}</el-tag>
          <el-tag size="small" effect="light" round>父块：{{ item.parentChunkSize }}</el-tag>
          <el-tag size="small" effect="light" round>子块：{{ item.childChunkSize }}</el-tag>
          <el-tag size="small" effect="light" round>TopK：{{ item.retrieveTopK }}</el-tag>
          <el-tag size="small" effect="light" round>相似度：{{ item.minSimilarity }}</el-tag>
          <el-tag size="small" effect="light" round>QA：{{item.enableQaName}}</el-tag>
          <div class="knowledge-model">
            <div class="knowledge-model-item"
                 v-if="item.vectorModelName"
                 :title="'向量模型：' + item.vectorModelName"
            >
              <el-icon><Histogram /></el-icon>
              <span class="knowledge-model-label">向量模型：</span>
              <span class="knowledge-model-value">{{ item.vectorModelName }}</span>
            </div>
            <div class="knowledge-model-item"
                 v-if="item.rerankModelName"
                 :title="'排序模型：' + item.rerankModelName"
            >
              <el-icon><Sort /></el-icon>
              <span class="knowledge-model-label">排序模型：</span>
              <span class="knowledge-model-value">{{ item.rerankModelName }}</span>
            </div>
          </div>
        </template>
        <!-- 底部操作 -->
        <template #action>
           <span
               v-if="hasMenu(3011)"
               class="action-item action-edit"
               @click="handleOpenDocument(item)"
           >知识库文档</span>
          <span
              class="action-item action-edit"
              @click.stop="handleOpenUpdateForm(item)"
          >修改</span>
          <span
              class="action-item action-danger"
              @click.stop="handleDelete(item)"
          >删除</span>
        </template>
      </info-card>
    </div>
    <!-- 空状态 -->
    <el-empty
        class="empty-grid"
        v-else
        :description="keyword ? '未找到匹配的知识库' : '暂无知识库'"
        :image-size="120"
    />
    <!-- 分页 -->
    <div>
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
    </div>
    <!-- 新增 / 修改 知识库表单抽屉 -->
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
              :rows="5"
              placeholder="请输入描述"
              maxlength="512"
              show-word-limit
          />
        </el-form-item>
        <el-divider content-position="left">分块配置</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分块策略" prop="chunkStrategy">
              <el-select
                  v-model="form.chunkStrategy"
                  placeholder="请选择分块策略"
                  style="width: 100%"
              >
                <el-option label="按段落分割" value="paragraph" />
                <el-option label="按行分割" value="line" />
                <el-option label="按句子分割" value="sentence" />
                <el-option label="按单词分割" value="word" />
                <el-option label="按字符分割" value="character" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="父块大小" prop="parentChunkSize">
              <el-input-number
                  v-model="form.parentChunkSize"
                  :min="1"
                  :max="9999"
                  controls-position="right"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="子块大小" prop="childChunkSize">
              <el-input-number
                  v-model="form.childChunkSize"
                  :min="1"
                  :max="9999"
                  controls-position="right"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="父块重叠" prop="parentOverlap">
              <el-input-number
                  v-model="form.parentOverlap"
                  :min="0"
                  :max="9999"
                  controls-position="right"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="子块重叠" prop="childOverlap">
              <el-input-number
                  v-model="form.childOverlap"
                  :min="0"
                  :max="9999"
                  controls-position="right"
                  style="width: 100%"
              />
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
              <el-input-number
                  v-model="form.retrieveTopK"
                  :min="1"
                  :max="99"
                  controls-position="right"
                  style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最小相似度" prop="minSimilarity">
              <el-input-number
                  v-model="form.minSimilarity"
                  :min="0.01"
                  :max="1"
                  :step="0.01"
                  :precision="2"
                  controls-position="right"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">高级选项</el-divider>
        <el-form-item label="生成QA" prop="enableQa">
          <el-switch
              v-model="form.enableQa"
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
          <el-button
              type="primary"
              @click="handleSubmitForm"
          >保存</el-button>
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
  pageKnowledgeListAPI,
  createKnowledgeAPI,
  updateKnowledgeAPI,
  deleteKnowledgeAPI
} from '@/api/kb/knowledge.js'
import { pageModelListAPI } from '@/api/llm/model.js'
import {
  Collection, Search, Plus, Histogram, Sort
} from '@element-plus/icons-vue'
import InfoCard from '@/components/InfoCard/index.vue'
import {hasMenu} from "@/utils/menuUtil.js";

const router = useRouter()

const knowledgeList = ref([])
const total = ref(0)
const pageSizes = [10, 30, 50]
const keyword = ref('')
// 分页查询条件
const query = ref({
  pageNo: 1,
  pageSize: 10,
})

// 主题色循环，配合 variables.scss 中的 agent 主题 token 使用
const themes = ['blue', 'green', 'purple', 'orange', 'cyan', 'pink', 'indigo']

let searchTimer = null

// 新增 / 修改 表单
const formVisible = ref(false)
const formTitle = ref('')
const formRef = ref(null)

/**
 * 获取表单默认值
 * @returns {{id: undefined, name: string, status: number, description: string,
 *   vectorModelId: undefined, rerankModelId: undefined, parentChunkSize: number,
 *   childChunkSize: number, parentOverlap: number, childOverlap: number,
 *   chunkStrategy: string, enableQa: number, retrieveTopK: number, minSimilarity: number}}
 */
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
    chunkStrategy: 'paragraph',
    enableQa: 0,
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
  rerankModelId: [
    { required: true, message: '请选择排序模型', trigger: 'change' },
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

onMounted(() => {
  loadKnowledgeList()
  loadVectorModelOptions()
  loadRerankModelOptions()
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
    loadKnowledgeList()
  }, 300)
})

/**
 * 分页查询知识库列表
 */
function loadKnowledgeList() {
  const params = {
    pageNo: query.value.pageNo,
    pageSize: query.value.pageSize,
  }
  if (keyword.value) {
    params.name = keyword.value
  }
  pageKnowledgeListAPI(params).then(res => {
    if (res.code === 200 && res.data) {
      knowledgeList.value = res.data.rows || []
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
  loadKnowledgeList()
}

/**
 * 切换页码重新查询
 * @param pageNo
 */
function handleCurrentChange(pageNo) {
  query.value.pageNo = pageNo
  loadKnowledgeList()
}

/**
 * 根据 id 计算主题名
 * @param item
 */
function getTheme(item) {
  return themes[item.id % themes.length]
}

/**
 * 打开知识库的文档列表页
 * @param item
 */
function handleOpenDocument(item) {
  router.push({ path: '/kb/knowledge/document', query: { prtId: item.id } })
}

/**
 * 打开新建表单
 */
function handleOpenCreateForm() {
  Object.assign(form.value, getDefaultFormData());
  formTitle.value = '新增知识库';
  formVisible.value = true;
}

/**
 * 打开修改表单
 * @param item
 */
function handleOpenUpdateForm(item) {
  Object.assign(form.value, getDefaultFormData(), item);
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
 * 提交表单（新增 / 修改）
 */
function handleSubmitForm() {
  formRef.value.validate(valid => {
    if (!valid) return;
    const data = { ...form.value };
    if (!data.id) {
      createKnowledgeAPI(data).then(res => {
        if (res.code !== 200) return;
        ElMessage.success('创建成功');
        handleCloseForm();
        loadKnowledgeList();
      });
    } else {
      updateKnowledgeAPI(data).then(res => {
        if (res.code !== 200) return;
        ElMessage.success('修改成功');
        handleCloseForm();
        loadKnowledgeList();
      });
    }
  });
}

/**
 * 删除知识库
 * @param item
 */
function handleDelete(item) {
  ElMessageBox.confirm('是否确定删除此条知识库?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteKnowledgeAPI({ id: item.id }).then(res => {
      if (res.code !== 200) return;
      ElMessage.success('删除成功');
      // 删除当前页最后一条时回退上一页，避免停留在空页
      if (knowledgeList.value.length === 1 && query.value.pageNo > 1) {
        query.value.pageNo -= 1
      }
      loadKnowledgeList();
    });
  }).catch(() => {});
}
</script>

<style scoped lang="scss">
.knowledge-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  padding: $spacing-md $spacing-xl;
  background-color: $bg-card;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-card;
}

.knowledge-header-left {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.knowledge-header-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 20px;
  font-weight: 700;
  color: $color-text-primary;
}

.knowledge-header-subtitle {
  font-size: 13px;
  color: $color-text-secondary;
}

.knowledge-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.knowledge-grid {
  height: calc(100vh - #{$nav-height} - 190px);
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: $spacing-lg;
  align-content: start;
  :deep(.info-card-action) {
    gap: $spacing-sm;
    .action-edit {
      color: var(--el-color-warning);
    }
    .action-danger {
      color: var(--el-color-danger);
    }
  }
}

.empty-grid {
  height: calc(100vh - #{$nav-height} - 190px);
}

// 模型信息
.knowledge-model {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  width: 100%;
  margin-top: $spacing-xs;
  padding-top: $spacing-xs;
  border-top: 1px dashed $border-color-light;
}

.knowledge-model-item {
  display: inline-flex;
  align-items: center;
  gap: $spacing-xs;
  min-width: 0;
  font-size: 12px;
  color: $color-text-secondary;

  .el-icon {
    font-size: 14px;
    color: var(--info-theme, $color-primary);
  }
}

.knowledge-model-label {
  color: $color-text-placeholder;
  flex-shrink: 0;
}

.knowledge-model-value {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.drawer-footer {
  padding: 0 $spacing-md;
}
</style>

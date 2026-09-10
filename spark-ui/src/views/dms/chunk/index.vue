<template>
  <div class="document-chunk-page">
    <!--顶部信息栏-->
    <div class="document-chunk-page__header">
      <span class="document-chunk-page__title">
        文档分块<span v-if="documentName"> - {{ documentName }}</span>
      </span>
      <div class="document-chunk-page__actions">
        <el-tag v-if="chunkStats.chunkStrategy" type="info" size="large">
          策略: {{ getStrategyName(chunkStats.chunkStrategy) }}
        </el-tag>
        <el-tag v-if="chunkStats.chunkCount !== undefined" type="success" size="large">
          分块数: {{ chunkStats.chunkCount }}
        </el-tag>
        <el-button type="primary" @click="handleRechunk" :loading="rechunkLoading">
          <el-icon><RefreshRight /></el-icon>
          重新分块
        </el-button>
      </div>
    </div>
    <!--主体-->
    <div class="document-chunk-page__body">
      <!--左侧：分片列表-->
      <div class="chunk-pane chunk-pane--left">
        <div class="chunk-pane__header">
          <span class="chunk-pane__title">分片列表</span>
          <span class="chunk-pane__count">共 {{ chunkTotal }} 条</span>
        </div>
        <div class="chunk-pane__list">
          <div v-if="chunkLoading" class="chunk-pane__placeholder">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <el-empty v-else-if="chunkList.length === 0" description="暂无分片数据"/>
          <template v-else>
            <div
                v-for="item in chunkList"
                :key="item.chunkIndex"
                class="chunk-card"
                :class="{ 'chunk-card--active': item.chunkIndex === selectedChunkIndex }"
                @click="handleSelectChunk(item)"
            >
              <div class="chunk-card__head">
                <el-tag>#{{ item.chunkIndex }}</el-tag>
                <span class="chunk-card__hint">点击查看 QA</span>
                <el-button type="primary" link size="small" @click.stop="handleEditChunk(item)">编辑</el-button>
              </div>
              <div class="chunk-card__content">{{ item.content }}</div>
            </div>
          </template>
        </div>
        <div class="chunk-pane__pager">
          <el-pagination
              small
              :current-page="chunkQuery.pageNo"
              :page-size="chunkQuery.pageSize"
              :page-sizes="chunkPageSizes"
              :background="true"
              layout="total, sizes, prev, pager, next"
              :total="chunkTotal"
              @size-change="handleChunkPageChangeSize"
              @current-change="handleChunkPageChangeNo"
          />
        </div>
      </div>
      <!--右侧：分片QA-->
      <div class="chunk-pane chunk-pane--right">
        <div class="chunk-pane__header">
          <span class="chunk-pane__title">分片 QA</span>
          <span v-if="selectedChunk" class="chunk-pane__count">分片 #{{ selectedChunk.chunkIndex }}</span>
        </div>
        <div class="chunk-pane__list">
          <div v-if="!selectedChunkIndex" class="chunk-pane__placeholder">
            <el-icon><InfoFilled /></el-icon>
            <span>请先在左侧选择一个分片</span>
          </div>
          <div v-else-if="chunkQALoading" class="chunk-pane__placeholder">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <el-empty v-else-if="chunkQAList.length === 0" description="暂无分片 QA 数据"/>
          <template v-else>
            <div
                v-for="(item, index) in chunkQAList"
                :key="index"
                class="qa-card"
            >
              <div class="qa-card__item">
                <span class="qa-card__label qa-card__label--q">Q</span>
                <span class="qa-card__text">{{ item.question }}</span>
              </div>
              <div class="qa-card__item">
                <span class="qa-card__label qa-card__label--a">A</span>
                <span class="qa-card__text">{{ item.answer }}</span>
              </div>
            </div>
          </template>
        </div>
        <div v-if="selectedChunkIndex" class="chunk-pane__pager">
          <el-pagination
              small
              :current-page="chunkQAQuery.pageNo"
              :page-size="chunkQAQuery.pageSize"
              :page-sizes="chunkQAPageSizes"
              :background="true"
              layout="total, sizes, prev, pager, next"
              :total="chunkQATotal"
              @size-change="handleChunkQAPageChangeSize"
              @current-change="handleChunkQAPageChangeNo"
          />
        </div>
      </div>
    </div>

    <!-- 编辑分块对话框 -->
    <el-dialog
        v-model="editDialogVisible"
        title="编辑分块"
        width="60%"
        :close-on-click-modal="false"
    >
      <el-form label-width="80px">
        <el-form-item label="分块序号">
          <el-tag>#{{ editForm.chunkIndex }}</el-tag>
        </el-form-item>
        <el-form-item label="分块内容">
          <el-input
              v-model="editForm.content"
              type="textarea"
              :rows="12"
              placeholder="请输入分块内容，保存后触发重新向量化"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleSaveChunk">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Loading, InfoFilled, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageDocumentChunkListAPI, pageDocumentChunkQAListAPI, getDocumentChunkStatsAPI, rechunkDocumentAPI, updateChunkAPI } from '@/api/dms/documentChunk.js'
import { queryDocumentDetailAPI } from '@/api/dms/document.js'

const route = useRoute()
const documentId = computed(() => route.query.id)
const documentName = ref('')
const chunkStats = ref({})
const rechunkLoading = ref(false)

// 分块策略映射
const strategyMap = {
  'paragraph': '按段落分割',
  'line': '按行分割',
  'sentence': '按句子分割',
  'word': '按单词分割',
  'character': '按字符分割'
}

const chunkQuery = ref({
  id: undefined,
  pageNo: 1,
  pageSize: 20
})
const chunkList = ref([])
const chunkTotal = ref(0)
const chunkLoading = ref(false)
const chunkPageSizes = [10, 20, 50, 100]

const selectedChunkIndex = ref(undefined)
const selectedChunk = computed(() => chunkList.value.find(c => c.chunkIndex === selectedChunkIndex.value))

const chunkQAQuery = ref({
  id: undefined,
  chunkIndex: undefined,
  pageNo: 1,
  pageSize: 20
})
const chunkQAList = ref([])
const chunkQATotal = ref(0)
const chunkQALoading = ref(false)
const chunkQAPageSizes = [10, 20, 50, 100]

// 编辑分块
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editForm = ref({ docId: undefined, chunkIndex: undefined, content: '' })

watch(documentId, (val) => {
  if (!val) return
  loadDocumentDetail()
  loadChunkList()
  loadChunkStats()
}, { immediate: true })

/**
 * 获取策略名称
 */
function getStrategyName(strategy) {
  return strategyMap[strategy] || strategy || '未知'
}

/**
 * 加载文档详情
 */
function loadDocumentDetail() {
  queryDocumentDetailAPI({ id: documentId.value }).then(res => {
    if (res.code !== 200) return
    documentName.value = res.data.name
  })
}

/**
 * 加载分块统计信息
 */
function loadChunkStats() {
  if (!documentId.value) return
  getDocumentChunkStatsAPI(documentId.value).then(res => {
    if (res.code === 200) {
      chunkStats.value = res.data || {}
    }
  })
}

/**
 * 重新分块
 */
function handleRechunk() {
  ElMessageBox.confirm('确定要重新分块该文档吗？这将删除现有分块并重新处理。', '确认重新分块', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    rechunkLoading.value = true
    rechunkDocumentAPI(documentId.value).then(res => {
      if (res.code === 200) {
        ElMessage.success('重新分块任务已提交')
        // 刷新分块列表
        setTimeout(() => {
          loadChunkList()
          loadChunkStats()
        }, 1000)
      } else {
        ElMessage.error(res.message || '重新分块失败')
      }
    }).finally(() => {
      rechunkLoading.value = false
    })
  }).catch(() => {
    // 用户取消
  })
}

/**
 * 打开编辑分块对话框
 * @param chunk 分块对象
 */
function handleEditChunk(chunk) {
  editForm.value = {
    docId: documentId.value,
    chunkIndex: chunk.chunkIndex,
    content: chunk.content || ''
  }
  editDialogVisible.value = true
}

/**
 * 保存编辑后的分块内容
 */
function handleSaveChunk() {
  if (!editForm.value.content || !editForm.value.content.trim()) {
    ElMessage.warning('分块内容不能为空')
    return
  }
  editLoading.value = true
  updateChunkAPI(editForm.value).then(res => {
    if (res.code === 200) {
      ElMessage.success('分块内容已更新，重新向量化任务已提交')
      editDialogVisible.value = false
      loadChunkList()
      loadChunkStats()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  }).finally(() => {
    editLoading.value = false
  })
}

/**
 * 加载分片列表
 */
function loadChunkList() {
  chunkLoading.value = true
  chunkQuery.value.id = documentId.value
  pageDocumentChunkListAPI(chunkQuery.value).then(res => {
    chunkList.value = res.data.rows || []
    chunkTotal.value = res.data.total || 0
    selectedChunkIndex.value = undefined
    chunkQAList.value = []
    chunkQATotal.value = 0
  }).finally(() => {
    chunkLoading.value = false
  })
}

/**
 * 选中分片并加载其 QA
 * @param chunk 分片对象
 */
function handleSelectChunk(chunk) {
  selectedChunkIndex.value = chunk.chunkIndex
  chunkQAQuery.value = {
    id: documentId.value,
    chunkIndex: chunk.chunkIndex,
    pageNo: 1,
    pageSize: chunkQAQuery.value.pageSize
  }
  loadChunkQAList()
}

/**
 * 加载分片 QA 列表
 */
function loadChunkQAList() {
  chunkQALoading.value = true
  pageDocumentChunkQAListAPI(chunkQAQuery.value).then(res => {
    chunkQAList.value = res.data.rows || []
    chunkQATotal.value = res.data.total || 0
  }).finally(() => {
    chunkQALoading.value = false
  })
}

/**
 * 分片分页数量变更
 * @param pageSize
 */
function handleChunkPageChangeSize(pageSize) {
  chunkQuery.value.pageSize = pageSize
  chunkQuery.value.pageNo = 1
  loadChunkList()
}

/**
 * 分片分页页码变更
 * @param pageNo
 */
function handleChunkPageChangeNo(pageNo) {
  chunkQuery.value.pageNo = pageNo
  loadChunkList()
}

/**
 * 分片 QA 分页数量变更
 * @param pageSize
 */
function handleChunkQAPageChangeSize(pageSize) {
  chunkQAQuery.value.pageSize = pageSize
  chunkQAQuery.value.pageNo = 1
  loadChunkQAList()
}

/**
 * 分片 QA 分页页码变更
 * @param pageNo
 */
function handleChunkQAPageChangeNo(pageNo) {
  chunkQAQuery.value.pageNo = pageNo
  loadChunkQAList()
}
</script>

<style scoped lang="scss">
.document-chunk-page {
  display: flex;
  flex-direction: column;
  height: 100vh;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-sm;
    padding: $spacing-sm $spacing-md;
    background-color: $bg-card;
    border-bottom: 1px solid $border-color-light;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__title {
    font-size: 16px;
    font-weight: 400;
    color: $color-text-primary;
  }

  &__body {
    flex: 1;
    display: flex;
    gap: $spacing-md;
    padding: $spacing-md;
    overflow: hidden;
    background-color: $bg-page;
  }
}

.chunk-pane {
  display: flex;
  flex-direction: column;
  background-color: $bg-card;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
  overflow: hidden;

  &--left {
    flex: 0 0 40%;
  }

  &--right {
    flex: 1;
    min-width: 0;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-sm $spacing-md;
    border-bottom: 1px solid $border-color-light;
    flex-shrink: 0;
  }

  &__title {
    font-size: 14px;
    font-weight: 600;
    color: $color-text-primary;
  }

  &__count {
    font-size: 12px;
    color: $color-text-secondary;
  }

  &__list {
    flex: 1;
    overflow-y: auto;
    padding: $spacing-sm $spacing-md;
  }

  &__pager {
    padding: $spacing-sm $spacing-md;
    border-top: 1px solid $border-color-light;
    display: flex;
    justify-content: flex-end;
    flex-shrink: 0;
  }

  &__placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    gap: $spacing-sm;
    color: $color-text-secondary;
    font-size: 14px;

    .el-icon {
      font-size: 32px;
    }
  }
}

.chunk-card {
  padding: $spacing-sm $spacing-md;
  margin-bottom: $spacing-sm;
  border: 1px solid $border-color-light;
  border-radius: $border-radius-sm;
  cursor: pointer;
  transition: $transition-fast;
  background-color: $bg-card;

  &:hover {
    border-color: $color-primary;
    background-color: $color-primary-light;
  }

  &--active {
    border-color: $color-primary;
    background-color: $color-primary-light;
  }

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-xs;
  }

  &__hint {
    font-size: 12px;
    color: $color-text-secondary;
  }

  &__content {
    font-size: 13px;
    line-height: 1.6;
    color: $color-text-primary;
    white-space: pre-wrap;
    word-break: break-word;
    max-height: 120px;
    overflow-y: auto;
  }
}

.qa-card {
  padding: $spacing-sm $spacing-md;
  margin-bottom: $spacing-sm;
  border: 1px solid $border-color-light;
  border-left: 3px solid $color-primary;
  border-radius: $border-radius-sm;
  background-color: $bg-card;

  &__item {
    display: flex;
    gap: $spacing-sm;

    & + & {
      margin-top: $spacing-xs;
    }
  }

  &__label {
    flex-shrink: 0;
    width: 18px;
    height: 18px;
    line-height: 18px;
    text-align: center;
    font-size: 12px;
    font-weight: 600;
    color: $color-text-white;
    border-radius: $border-radius-sm;
    margin-top: 2px;

    &--q {
      background-color: $color-primary;
    }

    &--a {
      background-color: $agent-theme-green;
    }
  }

  &__text {
    flex: 1;
    font-size: 13px;
    line-height: 1.6;
    color: $color-text-primary;
    white-space: pre-wrap;
    word-break: break-word;
  }
}
</style>

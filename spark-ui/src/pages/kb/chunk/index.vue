<template>
  <div class="document-chunk-page">
    <!--顶部信息栏-->
    <div class="document-chunk-page__header">
      <span class="document-chunk-page__title">
        文档分块<span v-if="documentName"> - {{ documentName }}</span>
      </span>
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
                <el-tag size="small" type="primary">#{{ item.chunkIndex }}</el-tag>
                <span class="chunk-card__hint">点击查看 QA</span>
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
  </div>
</template>

<script setup name="documentChunk">
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Loading, InfoFilled } from '@element-plus/icons-vue'
import { pageDocumentChunkListAPI, pageDocumentChunkQAListAPI } from '@/api/kb/documentChunk'
import { queryDocumentDetailAPI } from '@/api/kb/document'

const route = useRoute()
const documentId = computed(() => route.query.id)
const documentName = ref('')

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

watch(documentId, (val) => {
  if (!val) return
  loadDocumentDetail()
  loadChunkList()
}, { immediate: true })

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
    gap: $spacing-sm;
    padding: $spacing-sm $spacing-md;
    background-color: $bg-card;
    border-bottom: 1px solid $border-color-light;
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

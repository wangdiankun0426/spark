<template>
  <div class="app-container">
    <!--操作按钮-->
    <div class="search-header">
      <div class="search-type-switch">
        <div
            class="search-type-btn"
            :class="{'active-btn': searchQuery.searchType === 'keyword'}"
            @click="handleSearchTypeChange('keyword')"
            title="全文检索"
        >
          <SearchText />
        </div>
        <div
            class="search-type-btn"
            :class="{'active-btn': searchQuery.searchType === 'semantic'}"
            @click="handleSearchTypeChange('semantic')"
            title="语义检索"
        >
          <SearchSemantic />
        </div>
      </div>
      <el-select
          v-model="searchQuery.prtId"
          placeholder="所属知识库"
          clearable
          style="width: 200px"
          @change="searchDocument"
      >
        <el-option
            v-for="item in knowledgeList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
        />
      </el-select>
      <el-input
          v-model="searchQuery.keyWord"
          class="search-input"
          placeholder="请输入检索内容"
          clearable
          @change="searchDocument"
          @keyup.enter="searchDocument"
      >
        <template #append>
          <el-button @click="searchDocument">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>
    <!--文档列表-->
    <div class="results-container">
      <div v-if="documentList != null && documentList.length > 0">
        <el-card
            v-for="(document, key) in documentList"
            :key="key"
            class="result-card"
            shadow="hover"
        >
          <div class="card-content">
            <!--第一层：文件类型图标 + 文件名 + 操作按钮-->
            <div class="card-title">
              <DocumentIcon :ext="document.ext" class="doc-icon"/>
              <span class="doc-name">{{document.name}}</span>
              <div class="card-actions">
                <el-button text type="primary" @click="handlePreviewDocument(document)">
                  <el-icon><Tickets /></el-icon>
                  <span>预览</span>
                </el-button>
                <el-button text type="primary" @click="handleDownloadDocument(document)">
                  <el-icon><Download /></el-icon>
                  <span>下载</span>
                </el-button>
              </div>
            </div>
            <!--第二层：大小 / 所有者 / 创建时间-->
            <div class="card-meta">
              <span class="meta-item">
                <el-icon><Document /></el-icon>
                <span>大小: {{document.sizeStr}}</span>
              </span>
              <span class="meta-item">
                <el-icon><User /></el-icon>
                <span>所有者: {{document.ownerName}}</span>
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                <span>创建时间: {{formatDate(document.createdDt)}}</span>
              </span>
            </div>
            <!--第三层：命中文本片段-->
            <div class="card-fragment" v-if="document.fragment !== undefined">
              <pre v-html="document.fragment"/>
            </div>
          </div>
        </el-card>
      </div>
      <el-empty description="暂无文档数据" v-else/>
    </div>
    <!--分页组件-->
    <div>
      <el-pagination
          :current-page="searchQuery.pageNo"
          :page-size="searchQuery.pageSize"
          :page-sizes="pageSizes"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handlePageChangeSize"
          @current-change="handlePageChangeNo"
      />
    </div>
  </div>
</template>
<script setup name="search">
import {searchDocumentAPI, downloadDocumentAPI} from '@/api/dms/document.js'
import {pageKnowledgeListAPI} from '@/api/kb/knowledge.js'
import {ref, onMounted} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {Search, Document, User, Clock, View, Download} from "@element-plus/icons-vue"
import {ElMessage} from "element-plus";
import {formatDate} from "@/utils/dateUtil.js";
import DocumentIcon from '@/components/DocumentIcon';
import SearchText from '@/assets/icons/searchText.vue';
import SearchSemantic from '@/assets/icons/searchSemantic.vue';
const searchQuery = ref({
  pageNo: 1,
  pageSize: 30,
  keyWord: undefined,
  searchType: 'keyword',
  documentType: 14,
  prtId: undefined
});
const total = ref(0);
const pageSizes = [30,50,100];
const documentList = ref([]);
const knowledgeList = ref([]);

const route = useRoute();
const router = useRouter();
searchQuery.value.keyWord = route.query.keyWord;
const searchDocument = () => {
  searchDocumentAPI(searchQuery.value).then(res => {
    documentList.value = res.data.rows;
    total.value = res.data.total;
  })
}

searchDocument();

onMounted(() => {
  loadKnowledgeList();
})

/**
 * 加载知识库列表
 */
function loadKnowledgeList() {
  pageKnowledgeListAPI({ page: false }).then(res => {
    if (res.code === 200 && res.data) {
      knowledgeList.value = res.data.rows || []
    }
  })
}

/**
 * 打开文档预览（新开标签页）
 * @param row
 */
const handlePreviewDocument = (row) => {
  const {href} = router.resolve({path: '/document/preview', query: {id: row.id}});
  window.open(href, '_blank');
}

/**
 * 下载文档
 * @param row
 */
const handleDownloadDocument = (row) => {
  downloadDocumentAPI({id: row.id, ext: row.ext}).then(blob => {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = row.name;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }).catch(() => {
    ElMessage.error('文件下载失败');
  })
}

/**
 * 切换检索类型
 * @param type
 */
const handleSearchTypeChange = (type) => {
  searchQuery.value.searchType = type;
  searchDocument();
}

/**
 * 分页查询更改数量
 * @param pageSize
 */
const handlePageChangeSize = (pageSize) => {
  searchQuery.value.pageSize = pageSize;
  searchDocument();
}

/**
 * 分页查询更改页码
 * @param pageNo
 */
const handlePageChangeNo = (pageNo) => {
  searchQuery.value.pageNo = pageNo;
  searchDocument();
}
</script>

<style lang="scss" scoped>
.search-header {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: flex-end;
  margin-bottom: 24px;
  flex-shrink: 0;
}
.search-type-switch {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  flex-shrink: 0;
}
.search-type-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: $border-radius-sm;
  color: $color-text-placeholder;
  cursor: pointer;
  transition: $transition-fast;
  :deep(svg) {
    width: 22px;
    height: 22px;
  }
}
.search-type-btn:hover {
  color: $color-text-secondary;
}
.search-type-btn.active-btn {
  color: $color-primary;
}
.search-type-btn.active-btn:hover {
  color: $color-primary;
}
.search-input {
  height: 36px;
  max-width: 600px;
  flex: 1;
}
.results-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  height: calc(100vh - 184px);
}
.results-container::-webkit-scrollbar {
  width: 6px;
}
.results-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}
.results-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}
.results-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
.result-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  margin-bottom: 5px;
  margin-left: 5px;
  margin-right: 10px;
  :deep(.el-card__body) {
    padding: 0;
    margin: 0 !important;
    background-color: #fff;
  }
}
.result-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(64, 158, 255, 0.15);
}
.card-content {
  padding: $spacing-sm;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}
.card-title {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  min-width: 0;
}
.card-title :deep(.document-icon) {
  font-size: 20px;
  margin-right: 0;
}
.doc-name {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-meta {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  flex-wrap: wrap;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  font-size: 12px;
  color: $color-text-secondary;
  white-space: nowrap;
}
.meta-item .el-icon {
  color: $color-primary;
  font-size: 12px;
}
.card-fragment {
  background-color: #fafafa;
  border-radius: $border-radius-sm;
  padding: $spacing-sm;
  height: 60px;
  overflow-y: auto;
}
.card-fragment pre {
  margin: 0;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  font-size: 12px;
  line-height: 1.5;
  color: $color-text-secondary;
  white-space: pre-wrap;
  word-wrap: break-word;
}
.card-fragment pre em {
  color: #f56c6c;
  font-style: normal;
  font-weight: 600;
  background-color: #fef0f0;
  padding: 2px 4px;
  border-radius: 3px;
}
.card-actions {
  display: flex;
  align-items: center;
  margin-left: auto;
  gap: $spacing-xs;
}
.card-actions .el-button {
  font-size: 13px;
  font-weight: 400;
  padding: 0 !important;
  margin: 0 !important;
  margin-right: 10px !important;
}
.card-actions .el-button .el-icon {
  font-size: 14px;
}

</style>

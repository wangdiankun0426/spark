<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="knowledge-header">
      <div class="knowledge-header-left">
        <div class="knowledge-header-title">
          <el-icon class="knowledge-header-icon">
            <Collection />
          </el-icon>
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
            style="width: 240px"
        />
      </div>
    </div>

    <!-- 知识库卡片网格 -->
    <div class="knowledge-grid" v-if="knowledgeList.length">
      <info-card
          v-for="item in knowledgeList"
          :key="item.id"
          :theme="getTheme(item)"
          :icon="Collection"
          :title="item.name"
          :id-text="'编号 #' + item.id"
          :description="getDescription(item)"
          :disabled="item.status === 0"
          :height="300"
          @click="handleOpenDocument(item)"
      >
        <!-- 状态徽章 -->
        <template #badge>
          <span class="badge" :class="item.status === 1 ? 'badge-primary' : 'badge-muted'">
            {{ item.status === 1 ? '已启用' : '已停用' }}
          </span>
        </template>

        <!-- 配置标签 -->
        <template #tags>
          <el-tag size="small" effect="light" round v-if="item.chunkStrategyName">{{ item.chunkStrategyName }}</el-tag>
          <el-tag size="small" effect="light" round>父块 {{ item.parentChunkSize }}</el-tag>
          <el-tag size="small" effect="light" round>子块 {{ item.childChunkSize }}</el-tag>
          <el-tag size="small" effect="light" round>TopK {{ item.retrieveTopK }}</el-tag>
          <el-tag size="small" effect="light" round>相似度 {{ item.minSimilarity }}</el-tag>
          <el-tag size="small" effect="light" round v-if="item.enableQa === 1">QA</el-tag>
        </template>

        <!-- 底部元信息 -->
        <template #meta>
          <span class="meta-item" v-if="item.vectorModelName" :title="'向量模型：' + item.vectorModelName">
            <el-icon><Histogram /></el-icon>
            <span>{{ item.vectorModelName }}</span>
          </span>
          <span class="meta-item" v-if="item.rerankModelName" :title="'排序模型：' + item.rerankModelName">
            <el-icon><Sort /></el-icon>
            <span>{{ item.rerankModelName }}</span>
          </span>
        </template>

        <!-- 底部操作 -->
        <template #action>
          <el-button text type="primary" @click.stop="handleOpenDocument(item)">
            <el-icon><Document /></el-icon>文档({{ item.documentCount || 0 }})
          </el-button>
        </template>
      </info-card>
    </div>
    <!-- 空状态 -->
    <el-empty v-else :description="keyword ? '未找到匹配的知识库' : '暂无知识库'" :image-size="120" />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { pageKnowledgeListAPI } from '@/api/kb/knowledge.js'
import {
  Collection, Search, User, Document, Histogram, Sort
} from '@element-plus/icons-vue'
import InfoCard from '@/components/InfoCard/index.vue'

const router = useRouter()

const knowledgeList = ref([])
const keyword = ref('')

// 主题色循环，配合 variables.scss 中的 agent 主题 token 使用
const themes = ['blue', 'green', 'purple', 'orange', 'cyan', 'pink', 'indigo']

let searchTimer = null

onMounted(() => {
  loadKnowledgeList()
})

onBeforeUnmount(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})

/**
 * 名称搜索防抖，300ms 后重新查询
 */
watch(keyword, () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    loadKnowledgeList()
  }, 300)
})

/**
 * 加载知识库列表，一次性拉取全量
 */
function loadKnowledgeList() {
  const params = { page: false }
  if (keyword.value) {
    params.name = keyword.value
  }
  pageKnowledgeListAPI(params).then(res => {
    if (res.code === 200 && res.data) {
      knowledgeList.value = res.data.rows || []
    }
  })
}

/**
 * 根据 id 计算主题名
 * @param item
 */
function getTheme(item) {
  return themes[item.id % themes.length]
}

/**
 * 获取描述，为空时回退占位文案
 * @param item
 */
function getDescription(item) {
  if (item.description) {
    return item.description
  }
  return '暂无描述'
}

/**
 * 打开知识库的文档列表页
 * @param item
 */
function handleOpenDocument(item) {
  router.push({ path: '/kb/knowledge/document', query: { kbId: item.id } })
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

.knowledge-header-icon {
  font-size: 24px;
  color: $color-primary;
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
  height: calc(100vh - #{$nav-height} - 180px);
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: $spacing-lg;
  align-content: start;
}
</style>

<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="graph-header">
      <div class="graph-header-left">
        <div class="graph-header-title">
          <el-icon class="graph-header-icon">
            <Connection />
          </el-icon>
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
            style="width: 240px"
        />
      </div>
    </div>

    <!-- 知识图谱卡片网格 -->
    <div class="graph-grid" v-if="graphList.length">
      <info-card
          v-for="item in graphList"
          :key="item.id"
          :theme="getTheme(item)"
          :icon="Connection"
          :title="item.name"
          :id-text="'编号 #' + item.id"
          :description="getDescription(item)"
          :disabled="item.status === 0"
          :height="300"
          @click="handleOpenGraph(item)"
      >
        <!-- 状态徽章 -->
        <template #badge>
          <span class="badge" :class="item.status === 1 ? 'badge-primary' : 'badge-muted'">
            {{ item.status === 1 ? '已启用' : '已停用' }}
          </span>
        </template>

        <!-- 配置标签 -->
        <template #tags>
          <el-tag size="small" effect="light" round v-if="item.extractModelName">
            {{ item.extractModelName }}
          </el-tag>
          <div class="tag-group">
            <span class="tag-group-label">实体类型</span>
            <el-tag
                v-for="t in item.entityTypeList.slice(0, 3)"
                :key="'e-' + t"
                size="small"
                effect="light"
                round
            >
              {{ t }}
            </el-tag>
            <el-tooltip
                v-if="item.entityTypeList.length > 3"
                placement="top"
                :content="item.entityTypeList.join('、')"
            >
              <el-tag size="small" effect="light" round class="more-tag">
                +{{ item.entityTypeList.length - 3 }}
              </el-tag>
            </el-tooltip>
            <span v-if="!item.entityTypeList.length" class="tag-empty">暂无</span>
          </div>
          <div class="tag-group">
            <span class="tag-group-label">关系类型</span>
            <el-tag
                v-for="t in item.relationTypeList.slice(0, 3)"
                :key="'r-' + t"
                size="small"
                effect="light"
                round
            >
              {{ t }}
            </el-tag>
            <el-tooltip
                v-if="item.relationTypeList.length > 3"
                placement="top"
                :content="item.relationTypeList.join('、')"
            >
              <el-tag size="small" effect="light" round class="more-tag">
                +{{ item.relationTypeList.length - 3 }}
              </el-tag>
            </el-tooltip>
            <span v-if="!item.relationTypeList.length" class="tag-empty">暂无</span>
          </div>
        </template>

        <!-- 底部操作 -->
        <template #action>
          <el-button text type="primary" @click.stop="handleOpenEntity(item)">
            <el-icon><Box /></el-icon>实体 {{ item.entityCount ?? 0 }}
          </el-button>
          <el-button text type="primary" @click.stop="handleOpenRelation(item)">
            <el-icon><Share /></el-icon>关系 {{ item.relationCount ?? 0 }}
          </el-button>
          <el-button text type="primary" @click.stop="handleOpenDocument(item)">
            <el-icon><Document /></el-icon>文档 {{ item.docCount ?? 0 }}
          </el-button>
        </template>
      </info-card>
    </div>
    <!-- 空状态 -->
    <el-empty v-else :description="keyword ? '未找到匹配的知识图谱' : '暂无知识图谱'" :image-size="120" />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { pageGraphListAPI } from '@/api/kg/graph.js'
import { Connection, Search, Box, Share, Document } from '@element-plus/icons-vue'
import InfoCard from '@/components/InfoCard/index.vue'

const router = useRouter()
const graphList = ref([])
const keyword = ref('')

// 主题色循环，配合 variables.scss 中的 agent 主题 token 使用
const themes = ['blue', 'green', 'purple', 'orange', 'cyan', 'pink', 'indigo']

let searchTimer = null

onMounted(() => {
  loadGraphList()
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
    loadGraphList()
  }, 300)
})

/**
 * 加载知识图谱列表，一次性拉取全量
 */
function loadGraphList() {
  const params = { page: false }
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
 * 打开图谱详情页（新开标签页）
 * @param item
 */
function handleOpenGraph(item) {
  const { href } = router.resolve({ path: '/graph/detail', query: { graphId: item.id } })
  window.open(href, '_blank')
}

/**
 * 跳转到实体列表（按当前图谱过滤）
 * @param item
 */
function handleOpenEntity(item) {
  router.push({ path: '/kg/entity', query: { graphId: item.id } })
}

/**
 * 跳转到关系列表（按当前图谱过滤）
 * @param item
 */
function handleOpenRelation(item) {
  router.push({ path: '/kg/relation', query: { graphId: item.id } })
}

/**
 * 跳转到文档列表（按当前图谱过滤）
 * @param item
 */
function handleOpenDocument(item) {
  router.push({ path: '/kg/graph/document', query: { graphId: item.id } })
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
</script>

<style scoped lang="scss">
.graph-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  padding: $spacing-lg $spacing-xl;
  background-color: $bg-card;
  border-radius: $border-radius-lg;
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

.graph-header-icon {
  font-size: 24px;
  color: $color-primary;
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
  height: calc(100vh - #{$nav-height} - 180px);
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: $spacing-lg;
  align-content: start;
}

.tag-empty {
  font-size: 12px;
  color: $color-text-placeholder;
}

.more-tag {
  cursor: help;
}
</style>

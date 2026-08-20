<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="market-header">
      <div class="market-header-left">
        <div class="market-header-title">
          <el-icon class="market-header-icon">
            <Cpu />
          </el-icon>
          模型市场
        </div>
        <div class="market-header-subtitle">浏览各厂商提供的语言模型、向量模型与排序模型，按需选用</div>
      </div>
      <div class="market-header-right">
        <el-input
            v-model="keyword"
            placeholder="搜索模型名称"
            clearable
            :prefix-icon="Search"
            style="width: 240px"
            @change="handleSearch"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
        />
      </div>
    </div>

    <!-- 主体：左厂商 + 右模型 -->
    <div class="market-body" v-loading="loading">
      <!-- 左侧厂商列表 -->
      <div class="provider-pane">
        <div class="provider-pane-title">厂商列表</div>
        <div class="provider-list">
          <div
              class="provider-item"
              :class="{ 'provider-item-active': activeProviderId === null }"
              @click="handleSelectProvider(null)"
          >
            <div class="provider-item-icon provider-item-icon-all">
              <el-icon><Grid /></el-icon>
            </div>
            <div class="provider-item-info">
              <div class="provider-item-name">全部厂商</div>
              <div class="provider-item-count">{{ providerList.length }} 家厂商</div>
            </div>
          </div>
          <div
              v-for="provider in providerList"
              :key="provider.id"
              class="provider-item"
              :class="{ 'provider-item-active': activeProviderId === provider.id }"
              @click="handleSelectProvider(provider.id)"
          >
            <el-image
                v-if="provider.icon"
                :src="provider.icon"
                fit="cover"
                class="provider-item-icon"
            >
              <template #error>
                <div class="provider-item-icon provider-item-icon-fallback">
                  {{ provider.name?.charAt(0) }}
                </div>
              </template>
            </el-image>
            <div v-else class="provider-item-icon provider-item-icon-fallback">
              {{ provider.name?.charAt(0) }}
            </div>
            <div class="provider-item-info">
              <div class="provider-item-name">{{ provider.name }}</div>
              <div class="provider-item-count" v-if="provider.description">{{ provider.description }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧模型卡片网格 -->
      <div class="model-pane">
        <div class="model-pane-toolbar">
          <div class="model-pane-current">
            <el-icon><Collection /></el-icon>
            <span>{{ currentProviderName }}</span>
            <el-divider direction="vertical" />
            <span class="model-pane-total">共 {{ total }} 个模型</span>
          </div>
          <el-radio-group v-model="modelTypeFilter" size="small">
            <el-radio-button :label="0">全部</el-radio-button>
            <el-radio-button :label="1">语言模型</el-radio-button>
            <el-radio-button :label="2">向量模型</el-radio-button>
            <el-radio-button :label="3">排序模型</el-radio-button>
          </el-radio-group>
        </div>

        <div class="model-grid" v-if="modelList.length">
          <info-card
              v-for="model in modelList"
              :key="model.id"
              :theme="getTheme(model)"
              :icon="getIcon(model)"
              :title="model.name"
              :id-text="'编号 #' + model.id"
              :description="model.description || '暂无描述'"
              :disabled="!canChat(model)"
              :height="300"
              @click="handleSelectModel(model)"
          >
            <!-- 模型类型 + 启用状态 -->
            <template #badge>
              <span class="badge" :class="badgeClass(model.type)">
                {{ model.typeName || typeText(model.type) }}
              </span>
              <el-tag :type="model.status === 1 ? 'success' : 'info'" size="small">
                {{ model.status === 1 ? '已启用' : '已停用' }}
              </el-tag>
            </template>

            <!-- 参数标签（仅语言模型） -->
            <template #tags>
              <template v-if="model.type === 1">
                <el-tag size="small" :type="model.enableThinking === 1 ? 'success' : 'info'">
                  {{ model.enableThinking === 1 ? '思考：开启' : '思考：未开启' }}
                </el-tag>
                <el-tag size="small" type="warning">温度：{{ model.temperature }}</el-tag>
              </template>
            </template>

            <!-- 底部元信息：厂商 + 备注 -->
            <template #meta>
              <span class="meta-item" v-if="model.providerName">
                <el-icon><OfficeBuilding /></el-icon>
                <span>{{ model.providerName }}</span>
              </span>
              <span class="meta-item" v-if="model.remark">
                <el-icon><InfoFilled /></el-icon>
                <span>{{ model.remark }}</span>
              </span>
            </template>

            <!-- 底部操作 -->
            <template #action>
              <span v-if="canChat(model)" class="action-item">
                使用
                <el-icon><ArrowRight /></el-icon>
              </span>
            </template>
          </info-card>
        </div>
        <el-empty v-else :description="emptyText" :image-size="120" />
      </div>
    </div>

    <!-- llm聊天抽屉 -->
    <llm-chat
        v-model="drawerVisible"
        :target="selectedModelTarget"
        target-type="model"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { pageModelListAPI } from '@/api/llm/model.js'
import { pageProviderListAPI } from '@/api/llm/provider.js'
import LlmChat from '@/components/Chat/llmChat.vue'
import InfoCard from '@/components/InfoCard/index.vue'
import {
  Search, Cpu, Grid, Collection, Histogram, Box,
  OfficeBuilding, InfoFilled, ArrowRight
} from '@element-plus/icons-vue'

const loading = ref(false)
const keyword = ref('')
const modelTypeFilter = ref(0)
const providerList = ref([])
const modelList = ref([])
const total = ref(0)
const activeProviderId = ref(null)
const drawerVisible = ref(false)
const selectedModelTarget = ref({})

let searchTimer = null

// 主题色循环，配合 variables.scss 中的 agent 主题 token 使用
const themes = ['blue', 'green', 'purple', 'orange', 'cyan', 'pink', 'indigo']

onMounted(() => {
  loadProviders()
  loadModels()
})

onBeforeUnmount(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})

/**
 * 关键字搜索防抖，300ms 后调用接口检索
 */
watch(keyword, () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    loadModels()
  }, 300)
})

/**
 * 模型类型切换，立即调用接口检索
 */
watch(modelTypeFilter, () => {
  loadModels()
})

/**
 * 当前选中厂商名称
 */
const currentProviderName = computed(() => {
  if (activeProviderId.value === null) {
    return '全部厂商'
  }
  const matched = providerList.value.find(p => p.id === activeProviderId.value)
  return matched ? matched.name : '全部厂商'
})

/**
 * 空状态文案
 */
const emptyText = computed(() => {
  if (keyword.value) {
    return '未找到匹配的模型'
  }
  if (modelTypeFilter.value !== 0) {
    return modelTypeFilter.value === 1 ? '暂无语言模型' : modelTypeFilter.value === 2 ? '暂无向量模型' : '暂无排序模型'
  }
  return activeProviderId.value === null ? '暂无模型' : '该厂商暂无模型'
})

/**
 * 加载模型厂商列表（拉全量）
 */
function loadProviders() {
  pageProviderListAPI({ pageNo: 1, pageSize: 1000 }).then(res => {
    providerList.value = res.data.rows || []
  })
}

/**
 * 加载模型列表（按当前厂商、类型、关键字从接口查询）
 */
function loadModels() {
  loading.value = true
  const query = {
    pageNo: 1,
    pageSize: 1000,
    providerId: activeProviderId.value || undefined,
    type: modelTypeFilter.value || undefined,
    name: keyword.value || undefined
  }
  pageModelListAPI(query).then(res => {
    modelList.value = res.data.rows || []
    total.value = res.data.total || 0
  }).finally(() => {
    loading.value = false
  })
}

/**
 * 立即触发模型检索（回车、失焦、清空时调用）
 */
function handleSearch() {
  if (searchTimer) {
    clearTimeout(searchTimer)
    searchTimer = null
  }
  loadModels()
}

/**
 * 切换选中厂商并重新查询模型列表
 * @param providerId
 */
function handleSelectProvider(providerId) {
  activeProviderId.value = providerId
  loadModels()
}

/**
 * 根据模型 id 计算主题名
 * @param model
 */
function getTheme(model) {
  return themes[model.id % themes.length]
}

/**
 * 根据模型类型返回头像图标
 * @param model
 */
function getIcon(model) {
  if (model.type === 1) return Cpu
  if (model.type === 2) return Histogram
  return Box
}

/**
 * 根据模型类型返回 badge 样式类名
 * @param type
 */
function badgeClass(type) {
  if (type === 1) return 'badge-llm'
  if (type === 2) return 'badge-embed'
  return 'badge-rerank'
}

/**
 * 根据模型类型返回中文名称
 * @param type
 */
function typeText(type) {
  if (type === 1) return '语言模型'
  if (type === 2) return '向量模型'
  return '排序模型'
}

/**
 * 判断模型是否可对话（仅启用状态的语言模型可对话）
 * @param model
 */
function canChat(model) {
  return model.type === 1 && model.status === 1
}

/**
 * 选中模型，打开聊天抽屉；向量模型、排序模型或已停用模型禁止对话
 * @param model
 */
function handleSelectModel(model) {
  if (model.type !== 1) {
    ElMessage.warning('该类型模型不支持对话，请选择语言模型')
    return
  }
  if (model.status !== 1) {
    ElMessage.warning('该模型已停用，暂无法对话')
    return
  }
  selectedModelTarget.value = { id: model.id, name: model.name }
  drawerVisible.value = true
}
</script>

<style scoped lang="scss">
.market-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  padding: $spacing-md $spacing-xl;
  background-color: $bg-card;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-card;
}

.market-header-left {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.market-header-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 20px;
  font-weight: 700;
  color: $color-text-primary;
}

.market-header-icon {
  font-size: 24px;
  color: $color-primary;
}

.market-header-subtitle {
  font-size: 13px;
  color: $color-text-secondary;
}

.market-body {
  display: flex;
  gap: $spacing-lg;
  align-items: flex-start;
}

.provider-pane {
  width: 240px;
  flex-shrink: 0;
  background-color: $bg-card;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-card;
  padding: $spacing-md;
  position: sticky;
  top: $spacing-md;
}

.provider-pane-title {
  font-size: 13px;
  font-weight: 600;
  color: $color-text-secondary;
  padding: $spacing-xs $spacing-sm $spacing-sm;
}

.provider-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  height: calc(100vh - 270px);
  overflow-y: auto;
}

.provider-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm;
  border-radius: $border-radius-md;
  cursor: pointer;
  transition: $transition-fast;
  border: 1px solid transparent;

  &:hover {
    background-color: $color-primary-soft;
  }

  &.provider-item-active {
    background-color: $color-primary-light;
    border-color: $color-primary;
  }
}

.provider-item-icon {
  width: 32px;
  height: 32px;
  border-radius: $border-radius-sm;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: $color-text-white;
  background-color: $color-primary;
}

.provider-item-icon-all {
  background-color: $color-text-secondary;
}

.provider-item-icon-fallback {
  background-color: $color-primary-mid;
}

.provider-item-info {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.provider-item-name {
  font-size: 13px;
  font-weight: 600;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.provider-item-count {
  font-size: 11px;
  color: $color-text-placeholder;
}

.model-pane {
  flex: 1;
  min-width: 0;
}

.model-pane-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md $spacing-lg;
  background-color: $bg-card;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-card;
  margin-bottom: $spacing-lg;
}

.model-pane-current {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 14px;
  font-weight: 600;
  color: $color-text-primary;

  .el-icon {
    color: $color-primary;
  }
}

.model-pane-total {
  font-size: 13px;
  font-weight: 400;
  color: $color-text-secondary;
}

.model-grid {
  height: calc(100vh - #{$nav-height} - 230px);
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: $spacing-lg;
  align-content: start;
}

// badge slot 内：语言模型 / 向量模型 / 排序模型 类型徽章配色（不跟随主题色，保留语义区分）
// slot 内容会带本组件 data-v 属性，普通 scoped 选择器即可命中
.badge-llm {
  color: $color-primary;
  background-color: $color-primary-light;
}

.badge-embed {
  color: $agent-theme-cyan;
  background-color: $agent-theme-cyan-soft;
}

.badge-rerank {
  color: $agent-theme-purple;
  background-color: $agent-theme-purple-soft;
}
</style>

<template>
  <div class="app-container">
    <!-- 检索测试表单 -->
    <el-card class="test-form-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>检索测试</span>
          <el-tag type="info" size="small">测试检索效果和参数调优</el-tag>
        </div>
      </template>
      <el-form
          :model="testForm"
          label-width="100px"
          label-position="top"
      >
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="选择知识库" required>
              <el-select
                  v-model="testForm.kbId"
                  placeholder="请选择知识库"
                  style="width: 100%"
                  @change="handleKnowledgeChange"
              >
                <el-option
                    v-for="item in knowledgeList"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="召回数量 (TopK)">
              <el-input-number
                  v-model="testForm.topK"
                  :min="1"
                  :max="50"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="最小相似度">
              <el-input-number
                  v-model="testForm.minSimilarity"
                  :min="0"
                  :max="1"
                  :step="0.05"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="启用QA检索">
              <el-switch
                  v-model="testForm.enableQa"
                  :active-value="1"
                  :inactive-value="0"
                  active-text="是"
                  inactive-text="否"
              />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="启用Rerank">
              <el-switch
                  v-model="testForm.enableRerank"
                  :active-value="1"
                  :inactive-value="0"
                  active-text="是"
                  inactive-text="否"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="查询文本" required>
              <el-input
                  type="textarea"
                  rows="2"
                  v-model="testForm.query"
                  placeholder="请输入查询内容"
                  clearable
                  @keyup.enter="handleTest"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <div class="form-actions">
            <div class="action-buttons">
              <el-button type="primary" @click="handleTest" :loading="loading">
                <el-icon><Search /></el-icon>
                开始测试
              </el-button>
              <el-button @click="handleReset">
                <el-icon><RefreshRight /></el-icon>
                重置
              </el-button>
            </div>
            <!-- 检索结果统计 -->
            <div v-if="testResult" class="inline-stats">
              <el-divider direction="vertical" />
              <div class="stat-chip">
                <span class="stat-label">召回</span>
                <span class="stat-value">{{ testResult.retrieveCount || 0 }}</span>
              </div>
              <div class="stat-chip">
                <span class="stat-label">耗时</span>
                <span class="stat-value">{{ testResult.costTime || 0 }}ms</span>
              </div>
              <div class="stat-chip">
                <span class="stat-label">策略</span>
                <span class="stat-value">{{ getStrategyName(testResult.strategy) }}</span>
              </div>
              <div class="stat-chip">
                <span class="stat-label">QA命中</span>
                {{ testResult.qaHit ? '是' : '否' }}
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 检索结果列表 -->
    <el-card v-if="testResult && testResult.items && testResult.items.length > 0" class="result-list-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>检索结果</span>
          <el-tag type="success" size="small">共 {{ testResult.items.length }} 条</el-tag>
        </div>
      </template>
      <div class="result-scroll-container">
        <div class="result-list">
          <el-card
              v-for="(item, index) in testResult.items"
              :key="index"
              class="result-item-card"
              shadow="hover"
          >
            <div class="result-item-header">
              <div class="result-rank">
                <el-tag :type="getRankTagType(index)" size="small" effect="dark">
                  #{{ item.rank || index + 1 }}
                </el-tag>
              </div>
              <div class="result-meta">
                <el-tag v-if="item.docName" type="info" size="small" class="meta-tag">
                  <el-icon><Document /></el-icon>
                  {{ item.docName }}
                </el-tag>
                <el-tag v-if="item.sourceType" type="warning" size="small" class="meta-tag">
                  {{ item.sourceType }}
                </el-tag>
                <el-tag v-if="item.score" type="success" size="small" class="meta-tag">
                  相似度: {{ (item.score * 100).toFixed(1) }}%
                </el-tag>
              </div>
            </div>
            <div class="result-content">
              <pre>{{ item.content }}</pre>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>

    <!-- 空状态 -->
    <el-empty description="暂无文档数据" v-else/>
  </div>
</template>

<script setup name="kbTest">
import {ref, onMounted, reactive} from 'vue'
import {Search, RefreshRight, Document} from "@element-plus/icons-vue"
import {ElMessage} from "element-plus"
import {pageKnowledgeListAPI} from '@/api/kb/knowledge.js'
import {testRetrieveAPI} from '@/api/kb/knowledge.js'

const loading = ref(false)
const knowledgeList = ref([])
const testResult = ref(null)

// 检索策略映射
const strategyMap = {
  'qa': 'QA检索',
  'vector': '向量检索',
  'bm25': 'BM25检索',
  'vector+bm25': '向量+BM25混合检索',
  'vector+bm25+rerank': '向量+BM25+Rerank检索',
  'empty': '无结果'
}

const testForm = reactive({
  kbId: null,
  query: '',
  topK: 15,
  minSimilarity: 0.4,
  enableQa: 0,
  enableRerank: 1
})

onMounted(() => {
  loadKnowledgeList()
})

/**
 * 加载知识库列表
 */
function loadKnowledgeList() {
  pageKnowledgeListAPI({page: false}).then(res => {
    if (res.code === 200 && res.data) {
      knowledgeList.value = res.data.rows || []
    }
  })
}

/**
 * 知识库变更
 */
function handleKnowledgeChange() {
  const knowledge = knowledgeList.value.find(item => item.id === testForm.kbId)
  if (knowledge) {
    testForm.topK = knowledge.retrieveTopK || 15
    testForm.minSimilarity = knowledge.minSimilarity || 0.4
    testForm.enableQa = knowledge.enableQa || 0
  }
}

/**
 * 执行检索测试
 */
function handleTest() {
  if (!testForm.kbId) {
    ElMessage.warning('请选择知识库')
    return
  }
  if (!testForm.query) {
    ElMessage.warning('请输入查询内容')
    return
  }
  loading.value = true
  testResult.value = null
  testRetrieveAPI(testForm).then(res => {
    if (res.code === 200) {
      testResult.value = res.data
      ElMessage.success('检索测试完成')
    } else {
      ElMessage.error(res.message || '检索测试失败')
    }
  }).catch(err => {
    ElMessage.error('检索测试失败')
  }).finally(() => {
    loading.value = false
  })
}

/**
 * 重置表单
 */
function handleReset() {
  testForm.kbId = null
  testForm.query = ''
  testForm.topK = 15
  testForm.minSimilarity = 0.4
  testForm.enableQa = 0
  testForm.enableRerank = 1
  testResult.value = null
}

/**
 * 获取排名标签类型
 */
function getRankTagType(index) {
  if (index === 0) return 'danger'
  if (index === 1) return 'warning'
  if (index === 2) return 'success'
  return 'info'
}

/**
 * 获取策略名称
 */
function getStrategyName(strategy) {
  return strategyMap[strategy] || strategy || '-'
}
</script>

<style lang="scss" scoped>
.test-form-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.form-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.inline-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  justify-content: flex-end;
}

.stat-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 13px;
}

.stat-chip .stat-label {
  color: #909399;
  font-size: 12px;
}

.stat-chip .stat-value {
  color: #303133;
  font-weight: 600;
  font-size: 13px;
}

.qa-tag {
  margin-left: 2px;
}

.result-list-card {
  margin-bottom: 20px;
}

.result-scroll-container {
  max-height: calc(100vh - 420px);
  overflow-y: auto;
  padding-right: 4px;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-item-card {
  transition: all 0.3s ease;
}

.result-item-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.result-item-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.result-rank {
  flex-shrink: 0;
}

.result-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.meta-tag {
  display: flex;
  align-items: center;
  gap: 4px;
}

.result-content {
  background-color: #fafafa;
  border-radius: 8px;
  padding: 16px;
}

.result-content pre {
  margin: 0;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  font-size: 13px;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 200px;
  overflow-y: auto;
}

</style>

<template>
  <div class="app-container">
    <!-- 使用情况统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">知识库数量</div>
          <div class="stat-value primary">{{ usageStats.knowledgeCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">文档数量</div>
          <div class="stat-value primary">{{ usageStats.documentCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">今日检索</div>
          <div class="stat-value success">{{ usageStats.todayRetrieveCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">本周检索</div>
          <div class="stat-value success">{{ usageStats.weekRetrieveCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">本月检索</div>
          <div class="stat-value success">{{ usageStats.monthRetrieveCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">总检索次数</div>
          <div class="stat-value">{{ statsData.totalRetrieveCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">平均相似度</div>
          <div class="stat-value">{{ formatSimilarity(statsData.avgSimilarity) }}</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">平均耗时</div>
          <div class="stat-value">{{ statsData.avgCostTime || 0 }}ms</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 查询条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="知识库">
          <el-select
            v-model="query.kbId"
            placeholder="请选择知识库"
            clearable
            filterable
            style="width: 200px;"
          >
            <el-option
              v-for="item in knowledgeList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="检索策略">
          <el-select v-model="query.strategy" placeholder="请选择策略" clearable style="width: 150px;">
            <el-option label="QA优先" value="QA" />
            <el-option label="向量检索" value="VECTOR" />
            <el-option label="BM25" value="BM25" />
            <el-option label="混合检索" value="VECTOR_BM25" />
            <el-option label="重排检索" value="VECTOR_BM25_RERANK" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD HH:mm:ss"
            :default-time="defaultTime"
            style="width: 240px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 热门查询和用户排行 -->
    <el-row :gutter="20" class="rank-section">
      <el-col :span="16">
        <el-card class="rank-card">
          <template #header>
            <div class="card-header">
              <span>热门查询 TOP10</span>
            </div>
          </template>
          <el-table
              :data="usageStats.hotQueries || []"
              stripe
              style="width: 100%;"
              height="220"
          >
            <el-table-column type="index" label="排名" width="60" align="center">
              <template #default="{ $index }">
                {{ $index + 1 }}
              </template>
            </el-table-column>
            <el-table-column prop="query" label="查询内容" min-width="200" show-overflow-tooltip align="center"/>
            <el-table-column prop="queryCount" label="查询次数" width="100" align="center" />
            <el-table-column label="平均相似度" width="100" align="center">
              <template #default="{ row }">
                {{ formatSimilarity(row.avgSimilarity) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="rank-card">
          <template #header>
            <div class="card-header">
              <span>用户使用排行 TOP10</span>
            </div>
          </template>
          <el-table
              :data="usageStats.userRankList || []"
              stripe
              style="width: 100%;"
              height="220"
          >
            <el-table-column type="index" label="排名" width="60" align="center">
              <template #default="{ $index }">
                {{ $index + 1 }}
              </template>
            </el-table-column>
            <el-table-column prop="userName" label="用户名称" min-width="150" align="center" />
            <el-table-column prop="retrieveCount" label="检索次数" width="100" align="center" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 检索日志表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>检索日志</span>
        </div>
      </template>
      <el-table
          :data="logList"
          height="calc(100vh - 620px)"
          border
          style="width: 100%;"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="knowledgeName" label="知识库" min-width="120" align="center" />
        <el-table-column prop="query" label="检索内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="strategy" label="检索策略" width="120" align="center">
          <template #default="{ row }">
            {{ strategyMap[row.strategy] || row.strategy }}
          </template>
        </el-table-column>
        <el-table-column prop="retrieveCount" label="召回数" width="80" align="center" />
        <el-table-column prop="qaHitName" label="QA命中" width="80" align="center" />
        <el-table-column label="相似度" width="100" align="center">
          <template #default="{ row }">
            {{ formatSimilarity(row.avgSimilarity) }}
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时" width="80" align="center">
          <template #default="{ row }">
            {{ row.costTime }}ms
          </template>
        </el-table-column>
        <el-table-column label="反馈评分" width="120" align="center">
          <template #default="{ row }">
            <el-rate
                v-if="row.feedbackScore"
                v-model="row.feedbackScore"
                disabled
                size="small"
            />
            <span v-else class="no-feedback">未评分</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdByName" label="查询人" width="100" align="center" />
        <el-table-column prop="createdDt" label="查询时间" width="160" align="center" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleFeedback(row)">反馈</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div>
        <el-pagination
            :current-page="query.pageNo"
            :page-size="query.pageSize"
            :page-sizes="pageSizes"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 反馈对话框 -->
    <el-dialog
        v-model="feedbackDialogVisible"
        title="检索反馈"
        width="500px"
    >
      <el-form :model="feedbackForm" label-width="80px">
        <el-form-item label="检索内容">
          <el-input v-model="feedbackForm.query" disabled />
        </el-form-item>
        <el-form-item label="评分">
          <el-rate v-model="feedbackForm.feedbackScore" show-text :texts="['很差', '较差', '一般', '较好', '很好']" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="feedbackForm.feedbackRemark" type="textarea" :rows="3" placeholder="请输入反馈备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="feedbackDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitFeedback">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getRetrieveLogPageListAPI, getRetrieveStatsAPI, submitFeedbackAPI, getUsageStatsAPI } from '@/api/kb/retrieveLog'
import { pageKnowledgeListAPI } from '@/api/kb/knowledge'

const query = reactive({
  kbId: null,
  strategy: null,
  createdStartTime: null,
  createdEndTime: null,
  pageNo: 1,
  pageSize: 20
})

const dateRange = ref([])
const logList = ref([])
const total = ref(0)
const pageSizes = [30,50,100];
const knowledgeList = ref([])
const statsData = ref({})
const usageStats = ref({})
const feedbackDialogVisible = ref(false)
const feedbackForm = reactive({
  id: null,
  query: '',
  feedbackScore: null,
  feedbackRemark: ''
})

const strategyMap = {
  'QA': 'QA优先',
  'VECTOR': '向量检索',
  'BM25': 'BM25',
  'VECTOR_BM25': '混合检索',
  'VECTOR_BM25_RERANK': '重排检索'
}

// 日期范围默认时间
const defaultTime = [
  new Date(2000, 0, 1, 0, 0, 0),
  new Date(2000, 0, 1, 23, 59, 59)
]

// 格式化相似度
const formatSimilarity = (val) => {
  if (val === null || val === undefined || isNaN(val)) {
    return '-'
  }
  return (val * 100).toFixed(1) + '%'
}

// 获取检索日志列表
const getLogList = async () => {
  if (dateRange.value && dateRange.value.length === 2) {
    query.createdStartTime = dateRange.value[0]
    query.createdEndTime = dateRange.value[1]
  } else {
    query.createdStartTime = null
    query.createdEndTime = null
  }
  const res = await getRetrieveLogPageListAPI(query)
  if (res.code === 200) {
    logList.value = res.data.rows || []
    total.value = res.data.total || 0
  }
}

// 获取统计数据
const getStats = async () => {
  const params = {
    kbId: query.kbId,
    createdStartTime: query.createdStartTime,
    createdEndTime: query.createdEndTime
  }
  const res = await getRetrieveStatsAPI(params)
  if (res.code === 200) {
    statsData.value = res.data || {}
  }
}

// 获取使用情况统计
const getUsageStats = async () => {
  const res = await getUsageStatsAPI()
  if (res.code === 200) {
    usageStats.value = res.data || {}
  }
}

// 获取知识库列表
const getKnowledgeList = async () => {
  const res = await pageKnowledgeListAPI({ page: false })
  if (res.code === 200) {
    knowledgeList.value = res.data?.rows || []
  }
}

const handleQuery = () => {
  query.pageNo = 1
  getLogList()
  getStats()
}

const handleReset = () => {
  query.kbId = null
  query.strategy = null
  query.createdStartTime = null
  query.createdEndTime = null
  dateRange.value = []
  query.pageNo = 1
  getLogList()
  getStats()
}

const handleSizeChange = (val) => {
  query.pageSize = val
  getLogList()
}

const handleCurrentChange = (val) => {
  query.pageNo = val
  getLogList()
}

const handleFeedback = (row) => {
  feedbackForm.id = row.id
  feedbackForm.query = row.query
  feedbackForm.feedbackScore = row.feedbackScore || null
  feedbackForm.feedbackRemark = row.feedbackRemark || ''
  feedbackDialogVisible.value = true
}

const submitFeedback = async () => {
  if (!feedbackForm.feedbackScore) {
    ElMessage.warning('请选择评分')
    return
  }
  const res = await submitFeedbackAPI({
    id: feedbackForm.id,
    feedbackScore: feedbackForm.feedbackScore,
    feedbackRemark: feedbackForm.feedbackRemark
  })
  if (res.code === 200) {
    ElMessage.success('反馈成功')
    feedbackDialogVisible.value = false
    getLogList()
    getStats()
  }
}

onMounted(() => {
  getKnowledgeList()
  getLogList()
  getStats()
  getUsageStats()
})
</script>

<style scoped lang="scss">
.stats-cards {
  flex-shrink: 0;
  margin-bottom: 10px;
}

.stat-card {
  text-align: center;

  .stat-title {
    font-size: 13px;
    color: #909399;
    margin-bottom: 8px;
  }

  .stat-value {
    font-size: 22px;
    font-weight: bold;
    color: #303133;

    &.primary {
      color: #409eff;
    }

    &.success {
      color: #67c23a;
    }

    &.warning {
      color: #e6a23c;
    }
  }
}

.filter-card {
  flex-shrink: 0;
  margin-bottom: 10px;

  .filter-form {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
}

.rank-section {
  flex-shrink: 0;
  margin-bottom: 10px;
}

.rank-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.table-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;

  :deep(.el-card__body) {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.no-feedback {
  color: #909399;
  font-size: 12px;
}
</style>
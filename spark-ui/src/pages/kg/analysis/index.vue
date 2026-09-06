<template>
  <div class="analysis-container">
    <div class="kd-header">
      <div class="kd-header-left">
        <div class="kd-header-title">
          <el-icon class="kd-header-icon"><DataAnalysis /></el-icon>
          <span>图谱分析 - {{ graphName }}</span>
        </div>
      </div>
      <div class="kd-header-right">
        <el-select
            v-model="graphId"
            placeholder="请选择要分析的图谱"
            style="width: 220px"
            @change="handleGraphChange"
        >
          <el-option
              v-for="item in graphOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </div>
    </div>

    <!-- 无图谱可选时展示空状态 -->
    <div class="analysis-empty" v-if="!graphId">
      <el-empty
          :description="graphOptions.length ? '请选择要分析的图谱' : '暂无可分析的知识图谱'"
          :image-size="120"
      />
    </div>
    <div class="analysis-body" v-else>
      <!-- PageRank -->
      <div class="analysis-card">
        <div class="card-title">PageRank（节点重要性）</div>
        <div class="card-tip">基于链接结构评估每个实体的重要性，分数越高表示越核心</div>
        <el-table :data="pagerankData" max-height="400" highlight-current-row>
          <el-table-column type="index" label="排名" width="70" align="center" />
          <el-table-column prop="name" label="实体名称" min-width="160" />
          <el-table-column prop="type" label="类型" width="100" align="center" />
          <el-table-column label="分数" width="120" align="center">
            <template #default="{ row }">
              <span class="score-value">{{ row.score.toFixed(4) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="可视化" min-width="180">
            <template #default="{ row }">
              <div class="score-bar" :style="{ width: (row.score / maxPageRank * 100) + '%' }"></div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 中心度 -->
      <div class="analysis-card">
        <div class="card-title">中心度（关键节点）</div>
        <div class="card-tip">识别图中的关键桥梁节点，分数越高表示该实体连接越多不同区域</div>
        <el-table :data="centralityData" max-height="400" highlight-current-row>
          <el-table-column type="index" label="排名" width="70" align="center" />
          <el-table-column prop="name" label="实体名称" min-width="160" />
          <el-table-column prop="type" label="类型" width="100" align="center" />
          <el-table-column label="分数" width="120" align="center">
            <template #default="{ row }">
              <span class="score-value">{{ row.score.toFixed(4) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="可视化" min-width="180">
            <template #default="{ row }">
              <div class="score-bar score-bar-green" :style="{ width: (row.score / maxCentrality * 100) + '%' }"></div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 连通分量 -->
      <div class="analysis-card">
        <div class="card-title">连通分量</div>
        <div class="card-tip">识别图中互不连通的子图，共 {{ components.length }} 个连通分量</div>
        <el-table :data="components" max-height="400" highlight-current-row>
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="分量大小" width="120" align="center">
            <template #default="{ row }">{{ row.entityIds.length }} 个实体</template>
          </el-table-column>
          <el-table-column label="包含实体" min-width="400">
            <template #default="{ row }">
              <el-tag v-for="eid in row.entityIds.slice(0, 10)" :key="eid" size="small" class="component-tag">
                {{ entityNameMap[eid] || eid }}
              </el-tag>
              <el-tag v-if="row.entityIds.length > 10" size="small" type="info">+{{ row.entityIds.length - 10 }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { DataAnalysis } from '@element-plus/icons-vue'
import {
  pageGraphListAPI, queryGraphDetailAPI,
  pageRankAPI, centralityAPI, connectedComponentsAPI
} from '@/api/kg/graph.js'
import { pageEntityListAPI } from '@/api/kg/entity.js'

const router = useRouter()

// 当前要分析的图谱 id（图谱下拉选择）
const graphId = ref(undefined)
const graphName = ref('')

// 图谱下拉选项
const graphOptions = ref([])

// PageRank
const pagerankData = ref([])
const maxPageRank = ref(1)

// 中心度
const centralityData = ref([])
const maxCentrality = ref(1)

// 连通分量
const components = ref([])

// 实体 id -> 名称映射
const entityNameMap = ref({})

onMounted(() => {
  loadGraphOptions()
})

/**
 * 加载图谱下拉选项；默认选中第一个图谱进行分析
 */
async function loadGraphOptions() {
  const res = await pageGraphListAPI({ page: false })
  if (res.code === 200 && res.data) {
    graphOptions.value = res.data.rows || []
    if (!graphId.value && graphOptions.value.length) {
      graphId.value = graphOptions.value[0].id
    }
    if (graphId.value) {
      await loadAll()
    }
  }
}

/**
 * 切换图谱下拉后重新加载分析结果
 */
function handleGraphChange() {
  graphName.value = ''
  loadAll()
}

async function loadAll() {
  const gid = graphId.value
  if (!gid) return
  // 清空上一次结果，避免切换图谱时闪现旧数据
  pagerankData.value = []
  maxPageRank.value = 1
  centralityData.value = []
  maxCentrality.value = 1
  components.value = []
  entityNameMap.value = {}
  // 并行加载
  const [detailRes, entityRes, pagerankRes, centralityRes, componentsRes] = await Promise.all([
    queryGraphDetailAPI({ id: gid }),
    pageEntityListAPI({ graphId: gid, page: false }),
    pageRankAPI({ graphId: gid, maxIterations: 20 }),
    centralityAPI(gid),
    connectedComponentsAPI(gid)
  ])
  if (detailRes.code === 200) graphName.value = detailRes.data?.name || ''
  // 实体名称映射
  if (entityRes.code === 200 && entityRes.data) {
    const rows = entityRes.data.rows || []
    rows.forEach(e => { entityNameMap.value[e.id] = e.name })
  }
  // PageRank
  if (pagerankRes.code === 200 && pagerankRes.data) {
    const entries = Object.entries(pagerankRes.data)
      .map(([id, score]) => ({ id: Number(id), score, name: entityNameMap.value[Number(id)] || id, type: '' }))
      .sort((a, b) => b.score - a.score)
    pagerankData.value = entries.slice(0, 50)
    maxPageRank.value = entries.length > 0 ? entries[0].score : 1
  }
  // 中心度
  if (centralityRes.code === 200 && centralityRes.data) {
    const entries = Object.entries(centralityRes.data)
      .map(([id, score]) => ({ id: Number(id), score, name: entityNameMap.value[Number(id)] || id, type: '' }))
      .sort((a, b) => b.score - a.score)
    centralityData.value = entries.slice(0, 50)
    maxCentrality.value = entries.length > 0 ? entries[0].score : 1
  }
  // 连通分量
  if (componentsRes.code === 200 && componentsRes.data) {
    components.value = Object.entries(componentsRes.data)
      .map(([compId, entityIds]) => ({ compId: Number(compId), entityIds }))
      .sort((a, b) => b.entityIds.length - a.entityIds.length)
  }
  // 补充实体类型
  if (entityRes.code === 200 && entityRes.data) {
    const rows = entityRes.data.rows || []
    const typeMap = {}
    rows.forEach(e => { typeMap[e.id] = e.type })
    pagerankData.value.forEach(r => { r.type = typeMap[r.id] || '-' })
    centralityData.value.forEach(r => { r.type = typeMap[r.id] || '-' })
  }
}
</script>

<style scoped lang="scss">
.analysis-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - #{$nav-height});
  background-color: $bg-page;
  overflow: hidden;
}

.kd-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;
  padding: $spacing-md $spacing-lg;
  background-color: $bg-card;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-card;
}

.kd-header-left {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.kd-header-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 18px;
  font-weight: 700;
  color: $color-text-primary;
}

.kd-header-icon {
  font-size: 22px;
  color: $color-primary;
}

.kd-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.analysis-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.analysis-body {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.analysis-card {
  background-color: $bg-card;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-card;
  padding: $spacing-lg;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: $spacing-xs;
}

.card-tip {
  font-size: 12px;
  color: $color-text-secondary;
  margin-bottom: $spacing-md;
}

.score-value {
  font-weight: 600;
  color: $color-primary;
}

.score-bar {
  height: 8px;
  border-radius: 4px;
  background-color: $color-primary;
  min-width: 4px;
  transition: width 0.3s;
}

.score-bar-green {
  background-color: #34c759;
}

.component-tag {
  margin: 2px;
}
</style>

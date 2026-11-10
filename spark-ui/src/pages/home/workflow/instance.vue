<template>
  <div class="app-container wfi-container">
    <!-- 顶部：返回 + 状态筛选 -->
    <div class="wfi-header">
      <el-button class="wfi-back-btn" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
      <div class="wfi-header-right">
        <el-select
            v-model="query.status"
            class="wfi-filter"
            placeholder="状态筛选"
            clearable
            @change="handleSearch"
        >
          <el-option label="运行中" :value="1" />
          <el-option label="成功" :value="2" />
          <el-option label="失败" :value="3" />
          <el-option label="超时" :value="4" />
          <el-option label="已取消" :value="5" />
          <el-option label="已暂停" :value="6" />
          <el-option label="已驳回" :value="7" />
        </el-select>
        <el-button type="info" @click="handleSearch">
          <el-icon><Search /></el-icon>查询
        </el-button>
      </div>
    </div>

    <!-- 实例列表 -->
    <div class="wfi-body">
      <el-table
          v-loading="loading"
          class="wfi-table"
          :data="list"
          border
          highlight-current-row
          height="100%"
          empty-text="暂无运行实例"
          @row-click="handleOpenDetail"
      >
        <el-table-column prop="templateName" label="工作流" min-width="180" align="center" show-overflow-tooltip />
        <el-table-column label="版本" width="90" align="center">
          <template #default="{ row }">{{ row.revNum ? 'v' + row.revNum : '-' }}</template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="100" align="center" />
        <el-table-column label="耗时" width="110" align="center">
          <template #default="{ row }">{{ row.durationMs != null ? row.durationMs + 'ms' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdByName" label="触发人" width="140" align="center" />
        <el-table-column prop="createdDt" label="触发时间" width="180" align="center" />
      </el-table>

      <div class="wfi-footer">
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
    </div>

    <!-- 运行详情弹窗 -->
    <wf-instance-detail-dialog
        v-model="detailVisible"
        :instance-id="detailInstanceId"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Search } from '@element-plus/icons-vue'
import { pageInstanceHistoryAPI } from '@/api/workflow/instance.js'
import WfInstanceDetailDialog from '@/components/WfInstanceDetailDialog/index.vue'

const route = useRoute()
const router = useRouter()

/** 从工作流运行页带过来的模板 id */
const queryTemplateId = route.query.templateId ? Number(route.query.templateId) : undefined

const query = ref({
  pageNo: 1,
  pageSize: 30,
  templateId: queryTemplateId,
  status: undefined
})
const pageSizes = [30, 50, 100]
const list = ref([])
const total = ref(0)
const loading = ref(false)

const detailVisible = ref(false)
const detailInstanceId = ref(null)

onMounted(() => {
  loadList()
})

/**
 * 分页查询运行实例；清空筛选时不传空串，避免后端 Long 转换异常
 */
async function loadList() {
  loading.value = true
  try {
    const res = await pageInstanceHistoryAPI({
      pageNo: query.value.pageNo,
      pageSize: query.value.pageSize,
      templateId: query.value.templateId || undefined,
      status: query.value.status || undefined
    })
    if (res.code === 200 && res.data) {
      list.value = res.data.rows || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.value.pageNo = 1
  loadList()
}

function handleSizeChange(size) {
  query.value.pageSize = size
  query.value.pageNo = 1
  loadList()
}

function handleCurrentChange(pageNo) {
  query.value.pageNo = pageNo
  loadList()
}

function handleOpenDetail(row) {
  detailInstanceId.value = row.id
  detailVisible.value = true
}

function handleBack() {
  // 带上模板 id，返回运行页时仍停在这个工作流上
  router.push({
    path: '/home/workflow',
    query: query.value.templateId ? { id: query.value.templateId } : {}
  })
}
</script>

<style scoped lang="scss">
:deep(.el-table__row) {
  cursor: pointer;
}

.wfi-container {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  height: calc(100vh - 60px);
}

.wfi-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  background-color: $bg-card;
  border: 1px solid $border-color-light;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.wfi-back-btn {
  flex-shrink: 0;
}

.wfi-header-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.wfi-filter {
  width: 140px;
}

.wfi-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding: $spacing-md;
  background-color: $bg-card;
  border: 1px solid $border-color-light;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.wfi-table {
  flex: 1;
  min-height: 0;
}

.wfi-footer {
  flex-shrink: 0;
  display: flex;
  justify-content: flex-end;
}

</style>

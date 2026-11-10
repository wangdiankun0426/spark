<template>
  <div class="app-container">
    <!--操作按钮-->
    <div class="action-bar">
      <el-button
          type="warning"
          @click="handleResetQuery"
      >
        <el-icon><Refresh /></el-icon>重置
      </el-button>
      <el-button
          type="info"
          @click="handleRefresh"
      >
        <el-icon><Search /></el-icon>刷新
      </el-button>
    </div>
    <!--待审核实例列表-->
    <div>
      <el-table
          v-loading="loading"
          :data="list"
          highlight-current-row
          height="calc(100vh - 160px)"
          empty-text="暂无待审核实例"
          @row-click="handleRowOpen"
      >
        <el-table-column prop="templateName" label="工作流" align="center" show-overflow-tooltip/>
        <el-table-column prop="prompt" label="审核提示" align="center" show-overflow-tooltip/>
        <el-table-column prop="revNum" label="版本" align="center" width="120px"/>
        <el-table-column prop="createdByName" label="提交人" align="center" width="180px"/>
        <el-table-column prop="createdDt" label="提交时间" align="center" width="220px"/>
      </el-table>
    </div>
    <!--分页组件-->
    <div>
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

    <wf-instance-detail-dialog
        v-model="detailVisible"
        :instance-id="detailInstanceId"
        @reviewed="loadList"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { pageMyReviewListAPI } from '@/api/workflow/instance.js'
import WfInstanceDetailDialog from '@/components/WfInstanceDetailDialog/index.vue'

const route = useRoute()

const query = ref({
  pageNo: 1,
  pageSize: 30
})
const pageSizes = [30, 50, 100]
const list = ref([])
const total = ref(0)
const loading = ref(false)

const detailVisible = ref(false)
const detailInstanceId = ref(null)

onMounted(() => {
  loadList()
  // 消息中心点击跳转带实例 id，直接打开详情弹窗
  if (route.query.id) {
    detailInstanceId.value = Number(route.query.id)
    detailVisible.value = true
  }
})

/**
 * 分页查询待我审核实例
 */
async function loadList() {
  loading.value = true
  try {
    const res = await pageMyReviewListAPI({
      pageNo: query.value.pageNo,
      pageSize: query.value.pageSize
    })
    if (res.code === 200 && res.data) {
      list.value = res.data.rows || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

function handleResetQuery() {
  query.value.pageNo = 1
  query.value.pageSize = 30
  loadList()
}

/** 保持当前页重新拉取 */
function handleRefresh() {
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

/** 行点击打开详情弹窗，审核操作在弹窗底部进行 */
function handleRowOpen(row) {
  detailInstanceId.value = row.instanceId
  detailVisible.value = true
}
</script>

<style scoped lang="scss">
:deep(.el-table__row) {
  cursor: pointer;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
}
</style>

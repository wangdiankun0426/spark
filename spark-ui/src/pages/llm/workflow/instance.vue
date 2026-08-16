<template>
  <div class="app-container">
    <div class="page-header">
      <div class="page-title">
        <el-button text @click="$router.push('/llm/workflow')"><el-icon><ArrowLeft /></el-icon></el-button>
        <span>我的运行记录</span>
      </div>
      <div>
        <el-select v-model="query.status" placeholder="状态筛选" clearable style="width:140px" @change="loadList">
          <el-option label="运行中" :value="1" />
          <el-option label="成功" :value="2" />
          <el-option label="失败" :value="3" />
          <el-option label="超时" :value="4" />
        </el-select>
        <el-button @click="loadList" style="margin-left:8px"><el-icon><Search /></el-icon>查询</el-button>
      </div>
    </div>

    <el-table :data="list" border highlight-current-row height="calc(100vh - 180px)">
      <el-table-column prop="id" label="编号" width="120" align="center" />
      <el-table-column prop="templateName" label="工作流" min-width="160" align="center" />
      <el-table-column prop="revNum" label="版本" width="80" align="center" />
      <el-table-column prop="statusName" label="状态" width="100" align="center" />
      <el-table-column prop="durationMs" label="耗时" width="100" align="center">
        <template #default="{ row }">{{ row.durationMs != null ? row.durationMs + 'ms' : '-' }}</template>
      </el-table-column>
      <el-table-column prop="createdDt" label="运行时间" width="160" align="center" />
      <el-table-column label="操作" width="80" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" text size="small" @click="handleOpenDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top:12px">
      <el-pagination
          :current-page="query.pageNo" :page-size="query.pageSize" :page-sizes="pageSizes"
          :background="true" layout="total, sizes, prev, pager, next, jumper" :total="total"
          @size-change="s => { query.pageSize = s; loadList(); }"
          @current-change="p => { query.pageNo = p; loadList(); }"
      />
    </div>

    <!--运行详情抽屉（复用公共组件）-->
    <instance-detail-drawer v-model="detailVisible" :instance-id="detailInstanceId" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Search } from '@element-plus/icons-vue';
import { pageInstanceHistoryAPI } from '@/api/workflow/instance.js';
import InstanceDetailDrawer from '@/components/WfInstanceDetailDrawer/index.vue';

const query = ref({ pageNo: 1, pageSize: 30, status: undefined });
const pageSizes = [30, 50, 100];
const list = ref([]);
const total = ref(0);

const detailVisible = ref(false);
const detailInstanceId = ref(null);

async function loadList() {
  const res = await pageInstanceHistoryAPI(query.value);
  if (res.code === 200 && res.data) {
    list.value = res.data.rows || [];
    total.value = res.data.total;
  }
}

function handleOpenDetail(row) {
  detailInstanceId.value = row.id;
  detailVisible.value = true;
}

onMounted(() => { loadList(); });
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;
}
.page-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 18px;
  font-weight: 600;
  color: $color-text-primary;
}
</style>

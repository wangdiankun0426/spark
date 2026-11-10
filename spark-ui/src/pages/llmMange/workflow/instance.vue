<template>
  <div class="app-container">
    <div class="instance-header">
      <div class="instance-header-left">
        <el-button text @click="$router.push('/llm/workflow')">
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
      </div>
      <div class="instance-header-right">
        <el-select
            v-model="query.status"
            placeholder="状态筛选"
            clearable
            style="width: 200px"
            @change="loadList"
        >
          <el-option label="运行中" :value="1" />
          <el-option label="成功" :value="2" />
          <el-option label="失败" :value="3" />
          <el-option label="超时" :value="4" />
          <el-option label="已取消" :value="5" />
          <el-option label="已暂停" :value="6" />
          <el-option label="已驳回" :value="7" />
        </el-select>
        <el-button type="info" @click="loadList">
          <el-icon><Search /></el-icon>查询
        </el-button>
      </div>
    </div>

    <el-table
        :data="list"
        border
        highlight-current-row
        height="calc(100vh - 207px)"
        @row-click="handleOpenDetail"
    >
      <el-table-column prop="templateName" label="工作流" min-width="160" align="center" show-overflow-tooltip />
      <el-table-column prop="revNum" label="版本" width="80" align="center" />
      <el-table-column prop="statusName" label="状态" width="100" align="center" />
      <el-table-column prop="durationMs" label="耗时" width="100" align="center">
        <template #default="{ row }">{{ row.durationMs != null ? row.durationMs + 'ms' : '-' }}</template>
      </el-table-column>
      <el-table-column prop="createdByName" label="触发人" align="center" width="120"/>
      <el-table-column prop="createdDt" label="触发时间" width="160" align="center" />
    </el-table>

    <el-pagination
        :current-page="query.pageNo"
        :page-size="query.pageSize"
        :page-sizes="pageSizes"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="s => { query.pageSize = s; loadList(); }"
        @current-change="p => { query.pageNo = p; loadList(); }"
    />

    <!--运行详情弹窗-->
    <instance-detail-dialog
        v-model="detailVisible"
        :instance-id="detailInstanceId"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { Search } from '@element-plus/icons-vue';
import { pageInstanceHistoryAPI } from '@/api/workflow/instance.js';
import InstanceDetailDialog from '@/components/WfInstanceDetailDialog/index.vue';

const route = useRoute();
const query = ref({
  pageNo: 1, 
  pageSize: 30,
  status: undefined,
  templateId: route.query.templateId || undefined
});
const pageSizes = [30, 50, 100];
const list = ref([]);
const total = ref(0);

const detailVisible = ref(false);
const detailInstanceId = ref(null);

async function loadList() {
  query.value.templateId = route.query.templateId || undefined;
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
:deep(.el-table__row) {
  cursor: pointer;
}

.instance-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;
  padding: $spacing-md $spacing-lg;
  background-color: $bg-card;
  border: 1px solid $border-color-light;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.instance-header-left {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.instance-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

</style>

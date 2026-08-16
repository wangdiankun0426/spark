<template>
  <div class="app-container">
    <!--顶部头部-->
    <div class="wf-header">
      <div class="wf-header-left">
        <div class="wf-header-title">
          <el-icon class="wf-header-icon"><MagicStick /></el-icon>
          WORKFLOW
        </div>
        <div class="wf-header-subtitle">浏览已开启的AI工作流，快速运行</div>
      </div>
      <div class="wf-header-right">
        <el-input v-model="keyword" placeholder="搜索工作流名称" clearable :prefix-icon="Search" style="width:240px" />
        <el-button @click="$router.push('/llm/workflow/instance')"><el-icon><Clock /></el-icon>我的运行</el-button>
      </div>
    </div>

    <!--卡片网格-->
    <div class="wf-grid" v-if="workflowList.length">

      <info-card
          v-for="item in workflowList"
          :key="item.id"
          :theme="getTheme(item)"
          :icon="MagicStick"
          :title="item.name"
          :id-text="'#' + item.id"
          :description="item.description || '暂无描述'"
          :height="300"
          :disabled="item.status !== 1"
          @click="handleOpenRun(item)"
      >
        <!--版本徽章-->
        <template #badge>
          <span v-if="item.status !== 1" class="badge badge-muted">已关闭</span>
        </template>

        <!--标签-->
        <template #tags>
          <el-tag size="small" effect="light" round>{{ item.revNum || '-' }}</el-tag>
          <el-tag v-if="item.createdByName" size="small" effect="light" round>{{ item.createdByName }}</el-tag>
        </template>

        <!--底部元信息-->
        <template #meta>
          <span class="meta-item" v-if="item.createdDt">
            <el-icon><Clock /></el-icon>
            <span>{{ item.createdDt }}</span>
          </span>
        </template>

        <!--底部操作-->
        <template #action>
          <span v-if="item.status !== 1" class="action-item action-muted">已关闭</span>
          <span v-else class="action-item">
            运行
            <el-icon><ArrowRight /></el-icon>
          </span>
        </template>
      </info-card>

    </div>
    <el-empty v-else :description="keyword ? '未找到匹配的工作流' : '暂无已开启的工作流'" :image-size="120" />

    <!--快速运行抽屉-->
    <run-instance-drawer v-model="runVisible" :workflow="currentWorkflow" @success="handleRunSuccess" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount } from 'vue';
import { pageWorkflowListAPI } from '@/api/workflow/template.js';
import { MagicStick, Search, ArrowRight, Clock } from '@element-plus/icons-vue';
import RunInstanceDrawer from '@/components/WfRunInstanceDrawer/index.vue';
import InfoCard from '@/components/InfoCard/index.vue';

const keyword = ref('');
const workflowList = ref([]);
const runVisible = ref(false);
const currentWorkflow = ref(null);
const themes = ['blue', 'green', 'purple', 'orange', 'cyan', 'pink', 'indigo'];
let searchTimer = null;

onMounted(() => { loadList(); });
onBeforeUnmount(() => { if (searchTimer) clearTimeout(searchTimer); });

watch(keyword, () => {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(() => { loadList(); }, 300);
});

function getTheme(item) { return themes[(item.id || 0) % themes.length]; }

function handleOpenRun(item) {
  currentWorkflow.value = item;
  runVisible.value = true;
}

function handleRunSuccess() { runVisible.value = false; }

async function loadList() {
  const query = { status: 1, name: keyword.value || undefined, pageSize: 50 };
  const res = await pageWorkflowListAPI(query);
  if (res.code === 200 && res.data) { workflowList.value = res.data.rows || []; }
}
</script>

<style scoped lang="scss">
.wf-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  padding: $spacing-lg $spacing-xl;
  background-color: $bg-card;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-card;
}
.wf-header-left { display: flex; flex-direction: column; gap: $spacing-xs; }
.wf-header-title { display: flex; align-items: center; gap: $spacing-sm; font-size: 20px; font-weight: 700; color: $color-text-primary; }
.wf-header-icon { font-size: 24px; color: $color-primary; }
.wf-header-subtitle { font-size: 13px; color: $color-text-secondary; }
.wf-header-right { display: flex; align-items: center; gap: $spacing-md; }

.wf-grid {
  height: calc(100vh - #{$nav-height} - 180px);
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: $spacing-lg;
  align-content: start;
}
</style>

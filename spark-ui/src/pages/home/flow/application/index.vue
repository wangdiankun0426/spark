<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="flow-header">
      <div class="flow-header-left">
        <div class="flow-header-title">
          流程申请
        </div>
        <div class="flow-header-subtitle">浏览流程模板并发起新流程</div>
      </div>
      <div class="flow-header-right">
        <el-input
            v-model="keyword"
            placeholder="搜索流程模板名称"
            clearable
            :prefix-icon="Search"
            style="width: 300px"
        />
      </div>
    </div>

    <!-- 流程模板卡片网格 -->
    <div
        class="flow-grid"
        v-if="templateList.length"
    >
      <info-card
          v-for="item in templateList"
          :key="item.id"
          :icon="FlowApplication"
          :title="item.name"
          :description="item.remark || '暂无备注'"
          :disabled="item.status !== 1"
          :actions="cardActions(item)"
      />
    </div>
    <!-- 空状态 -->
    <el-empty
        class="empty-grid"
        v-else
        :description="keyword ? '未找到匹配的流程模板' : '暂无流程模板'"
        :image-size="120"
    />

    <!-- 分页 -->
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

    <!-- 流程发起抽屉 -->
    <flow-detail-drawer
        ref="detailDrawerRef"
        :visible="flowFormVisible"
        v-model:name="flowForm.name"
        v-model:description="flowForm.description"
        v-model:level="flowForm.level"
        :created-by-name="userInfo.name"
        :dept-name="userInfo.deptName"
        title="发起流程"
        :formJson="formJson"
        :bpmJson="bpmJson"
        :type="1"
        @close="handleCloseFlowForm"
    >
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="handleSubmitFlow">发起流程</el-button>
          <el-button @click="handleCloseFlowForm">关闭</el-button>
        </div>
      </template>
    </flow-detail-drawer>
  </div>
</template>

<script setup>
import {ref, watch, onMounted, onBeforeUnmount, computed} from 'vue';
import {
  pageTemplateListAPI,
  showTemplateDetailAPI,
} from '@/api/flow/template';
import { createFlowInstanceAPI } from '@/api/flow/instance.js';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import FlowDetailDrawer from '@/components/FlowDetailDrawer';
import InfoCard from '@/components/InfoCard/index.vue';
import store from "@/store/index.js";
import { useRoute } from 'vue-router';
import FlowApplication from "@/assets/icons/flowApplication.vue";

const route = useRoute();

const userInfo = computed(() => store.getters['user/getUserInfo'])

const templateList = ref([]);
const total = ref(0);
const pageSizes = [10, 30, 50];
const keyword = ref('');
// 分页查询条件
const query = ref({
  pageNo: 1,
  pageSize: 10,
});

// 流程发起抽屉相关
const flowFormVisible = ref(false);
const formJson = ref({});
const bpmJson = ref({});
const detailDrawerRef = ref(null);
const flowForm = ref(createEmptyFlowForm());

let searchTimer = null;

onMounted(() => {
  loadTemplateList();
  const templateId = route.query.templateId;
  if (templateId) {
    handleOpenTemplate(Number(templateId));
  }
});

onBeforeUnmount(() => {
  if (searchTimer) {
    clearTimeout(searchTimer);
  }
});

/**
 * 名称搜索防抖，300ms 后回到第一页并重新查询
 */
watch(keyword, () => {
  if (searchTimer) {
    clearTimeout(searchTimer);
  }
  searchTimer = setTimeout(() => {
    query.value.pageNo = 1;
    loadTemplateList();
  }, 300);
});

/**
 * 构造空流程发起表单
 */
function createEmptyFlowForm() {
  return {
    templateId: undefined,
    templateRevId: undefined,
    formId: undefined,
    formRevId: undefined,
    processId: undefined,
    name: undefined,
    description: undefined,
    level: 1,
  };
}

/**
 * 分页查询流程模板列表，携带名称关键字
 */
function loadTemplateList() {
  const params = {
    pageNo: query.value.pageNo,
    pageSize: query.value.pageSize,
  }
  if (keyword.value) {
    params.name = keyword.value;
  }
  pageTemplateListAPI(params).then(res => {
    if (res.code === 200 && res.data) {
      templateList.value = res.data.rows || [];
      total.value = res.data.total || 0;
    }
  });
}

/**
 * 切换每页条数，回到第一页重新查询
 * @param size
 */
function handleSizeChange(size) {
  query.value.pageSize = size;
  query.value.pageNo = 1;
  loadTemplateList();
}

/**
 * 切换页码重新查询
 * @param pageNo
 */
function handleCurrentChange(pageNo) {
  query.value.pageNo = pageNo;
  loadTemplateList();
}

/**
 * 卡片底部工具栏按钮
 * @param item
 */
function cardActions(item) {
  return [
    { key: 'apply', label: '申请', icon: 'CirclePlus', onClick: () => handleOpenTemplate(item.id) }
  ];
}

/**
 * 打开流程模板发起申请
 * @param id
 */
function handleOpenTemplate(id) {
  showTemplateDetailAPI({ id }).then(res => {
    if (res.code === 200) {
      formJson.value = JSON.parse(res.data.formJson);
      bpmJson.value = JSON.parse(res.data.bpmJson);
      flowForm.value.templateId = res.data.id;
      flowForm.value.templateRevId = res.data.revId;
      flowForm.value.formId = res.data.formId;
      flowForm.value.formRevId = res.data.formRevId;
      flowForm.value.processId = res.data.processId;
      flowForm.value.name = res.data.name;
      flowFormVisible.value = true;
    }
  }).catch(() => {});
}

/**
 * 提交流程
 */
function handleSubmitFlow() {
  if (!detailDrawerRef.value?.validateTitle()) {
    return;
  }
  const list = JSON.parse(JSON.stringify(formJson.value)).widgetList;
  // 必填校验
  const missingWidget = list.find(w => {
    if (!w.config.required) {
      return false;
    }
    const v = w.config.value;
    return v === null || v === undefined || v === '' || (Array.isArray(v) && v.length === 0);
  });
  if (missingWidget) {
    ElMessage.warning(`【${missingWidget.config.label}】为必填项，请填写后再发起`);
    return;
  }
  const values = [];
  list.forEach(widget => {
    const config = widget.config;
    values.push({
      code: config.code,
      type: widget.type,
      value: config.value,
      showValue: config.showValue,
    });
  });
  flowForm.value.values = values;
  createFlowInstanceAPI(flowForm.value).then(res => {
    if (res.code !== 200) {
      return;
    }
    ElMessage.success('流程发起成功');
    handleCloseFlowForm();
  });
}

/**
 * 关闭流程发起表单
 */
function handleCloseFlowForm() {
  formJson.value = {};
  bpmJson.value = {};
  flowForm.value = createEmptyFlowForm();
  flowFormVisible.value = false;
}

</script>

<style scoped lang="scss">
.flow-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  padding: $spacing-md $spacing-xl;
  background-color: $bg-card;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.flow-header-left {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.flow-header-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 20px;
  font-weight: 700;
  color: $color-text-primary;
}

.flow-header-subtitle {
  font-size: 13px;
  color: $color-text-secondary;
}

.flow-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.flow-grid {
  height: calc(100vh - #{$nav-height} - 178px);
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: $spacing-lg;
  align-content: start;
}

.empty-grid {
  height: calc(100vh - #{$nav-height} - 178px);
}

.drawer-footer {
  padding: 0 $spacing-md;
}

</style>

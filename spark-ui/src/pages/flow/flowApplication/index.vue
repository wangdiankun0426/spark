<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="flow-header">
      <div class="flow-header-left">
        <div class="flow-header-title">
          <el-icon class="flow-header-icon">
            <Share />
          </el-icon>
          流程模板
        </div>
        <div class="flow-header-subtitle">浏览流程模板并发起新流程</div>
      </div>
      <div class="flow-header-right">
        <el-input
            v-model="keyword"
            placeholder="搜索流程模板名称"
            clearable
            :prefix-icon="Search"
            style="width: 240px"
        />
      </div>
    </div>

    <!-- 流程模板卡片网格 -->
    <div class="flow-grid" v-if="templateList.length">
      <info-card
          v-for="item in templateList"
          :key="item.id"
          :theme="getTheme(item)"
          :icon="Share"
          :title="item.name"
          :id-text="'编号 #' + item.id"
          :description="'模板ID：' + item.processId"
          :height="300"
          @click="handleOpenTemplate(item.id)"
      >
        <!-- 版本徽章 -->
        <template #badge>
          <span class="badge badge-theme">v{{ item.revNum }}</span>
        </template>

        <!-- 配置标签 -->
        <template #tags>
          <el-tag size="small" effect="light" round>版本 v{{ item.revNum }}</el-tag>
          <el-tag size="small" effect="light" round v-if="item.createdByName">{{ item.createdByName }}</el-tag>
        </template>

        <!-- 底部元信息 -->
        <template #meta>
          <span class="meta-item" v-if="item.createdByName">
            <el-icon><User /></el-icon>
            <span>{{ item.createdByName }}</span>
          </span>
        </template>

        <!-- 底部操作 -->
        <template #action>
          <el-button text type="primary" @click.stop="handleOpenTemplate(item.id)">
            <el-icon><EditPen /></el-icon>申请流程
          </el-button>
        </template>
      </info-card>
    </div>
    <!-- 空状态 -->
    <el-empty v-else :description="keyword ? '未找到匹配的流程模板' : '暂无流程模板'" :image-size="120" />

    <!--流程模板表单-->
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
          <el-button
              type="primary"
              @click="handleSubmitTemplateForm"
          >发起流程</el-button>
          <el-button
              @click="handleCloseFlowForm"
          >关闭</el-button>
        </div>
      </template>
    </flow-detail-drawer>
  </div>
</template>

<script setup>
import {ref, watch, onMounted, onBeforeUnmount, computed} from 'vue';
import { pageTemplateListAPI, showTemplateDetailAPI } from '@/api/flow/template';
import { createFlowInstanceAPI } from '@/api/flow/instance.js';
import { ElMessage } from 'element-plus';
import { Search, Share, User, EditPen } from '@element-plus/icons-vue';
import FlowDetailDrawer from '@/components/FlowDetailDrawer';
import InfoCard from '@/components/InfoCard/index.vue';
import store from "@/store/index.js";

const userInfo = computed(() => store.getters['user/getUserInfo'])

const templateList = ref([]);
const keyword = ref('');

// 主题色循环，配合 variables.scss 中的 agent 主题 token 使用
const themes = ['blue', 'green', 'purple', 'orange', 'cyan', 'pink', 'indigo'];

// 流程发起抽屉相关
const flowFormVisible = ref(false);
const formJson = ref({});
const bpmJson = ref({});
const detailDrawerRef = ref(null);
const flowForm = ref({
  templateId: undefined,
  templateRevId: undefined,
  formId: undefined,
  formRevId: undefined,
  processId: undefined,
  name: undefined,
  description: undefined,
  level: 1,
});

let searchTimer = null;

onMounted(() => {
  loadTemplateList();
});

onBeforeUnmount(() => {
  if (searchTimer) {
    clearTimeout(searchTimer);
  }
});

/**
 * 名称搜索防抖，300ms 后重新查询
 */
watch(keyword, () => {
  if (searchTimer) {
    clearTimeout(searchTimer);
  }
  searchTimer = setTimeout(() => {
    loadTemplateList();
  }, 300);
});

/**
 * 加载流程模板列表，一次性拉取全量已启用模板
 */
function loadTemplateList() {
  const params = { page: false, status: 1 };
  if (keyword.value) {
    params.name = keyword.value;
  }
  pageTemplateListAPI(params).then(res => {
    if (res.code === 200 && res.data) {
      templateList.value = res.data.rows || [];
    }
  });
}

/**
 * 根据 id 计算主题名
 * @param item
 */
function getTheme(item) {
  return themes[item.id % themes.length];
}

/**
 * 打开流程模板
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
 * 提交流程模板表单
 */
function handleSubmitTemplateForm() {
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
    const value = {
      code: config.code,
      type: widget.type,
      value: config.value,
      showValue: config.showValue,
    };
    values.push(value);
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
 * 关闭表单
 */
function handleCloseFlowForm() {
  formJson.value = {};
  bpmJson.value = {};
  flowForm.value = {
    templateId: undefined,
    templateRevId: undefined,
    formId: undefined,
    formRevId: undefined,
    processId: undefined,
    name: undefined,
    description: undefined,
    level: 1,
  };
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
  border-radius: $border-radius-lg;
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

.flow-header-icon {
  font-size: 24px;
  color: $color-primary;
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
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: $spacing-lg;
}

.drawer-footer {
  padding: 0 16px;
}
</style>

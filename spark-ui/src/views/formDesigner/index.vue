<template>
  <el-container class="component-box">
    <!--左侧面板-->
    <el-aside class="left-widget-panel">
      <widgetPanel :designer="designer" />
    </el-aside>
    <!--中间面板-->
    <el-container class="middle-form-widget">
      <!--工具栏：按钮统一 plain 白底，包一层容器靠右摆放-->
      <el-header>
        <div class="toolbar-buttons">
          <el-button
              plain
              @click="handleClearWidget"
          ><el-icon><Delete /></el-icon>清空表单</el-button>
          <el-button
              plain
              @click="handleViewForm"
          ><el-icon><View /></el-icon>预览表单</el-button>
          <el-button
              plain
              @click="handleOpenJson"
          ><el-icon><Tickets /></el-icon>JSON数据</el-button>
          <el-button
              type="primary"
              @click="handleSaveForm"
          ><el-icon><Select /></el-icon>保存模板</el-button>
        </div>
      </el-header>
      <!--表单-->
      <el-main>
        <formWidget :designer="designer" />
      </el-main>
    </el-container>
    <!-- 右侧-->
    <el-aside class="right-setting-panel" v-if="designer.widgetList.length > 0">
      <settingPanel :designer="designer" />
    </el-aside>
  </el-container>
  <!--表单预览组件-->
  <el-dialog
      v-model="viewFormVisible"
      title="表单预览"
      :width="designer.formConfig.width"
  >
    <form-view
        :form="previewForm"
        :key="formKey"
    />
  </el-dialog>
  <!--表单JSON：可复制到其他表单应用，也可粘贴其他表单的JSON后应用到当前表单-->
  <el-drawer
      v-model="jsonVisible"
      title="表单 JSON"
      direction="ltr"
      :size="920"
  >
    <div class="json-box">
      <el-input
          v-model="jsonText"
          type="textarea"
          :rows="24"
          placeholder="打开时填入当前表单JSON，可复制到其他表单应用，也可粘贴其他表单的JSON后点击应用"
      />
    </div>
    <template #footer>
      <el-button plain @click="handleCopyJson"><el-icon><CopyDocument /></el-icon>复制当前</el-button>
      <el-button type="primary" @click="handleApplyJson"><el-icon><Check /></el-icon>应用</el-button>
    </template>
  </el-drawer>
</template>
<script setup>
import {ref} from 'vue';
import widgetPanel from './widgetPanel/index.vue';
import formWidget from './formWidget/index.vue';
import settingPanel from './settingPanel/index.vue';
import FormView from '../../components/FormView/index.vue';
import {queryFormJsonAPI, saveFormJsonAPI} from '@/api/form/form.js';
import { useRoute } from 'vue-router';
import {ElMessage} from "element-plus";
const route = useRoute();
const id = route.params.id;
// 路由带版本号时编辑指定版本，不带版本号时编辑当前最新版本
const revId = route.params.revId;
const designer = ref({
  widgetList: [],
  formConfig: {
    width: 600,
    position: "right",
    size: "default",
  },
  selectedId: null,
});
const viewFormVisible = ref(false);
const formKey = ref(0);
// 预览表单数据：预览时事件动作会改写 config，必须用深拷贝，否则会被保存时落库
const previewForm = ref(null);
const jsonVisible = ref(false);
const jsonText = ref('');

handleGetFormJson();

/**
 * 获取表单json
 * 路由带版本号时查指定版本，不带版本号时后端默认取表单当前版本
 */
function handleGetFormJson() {
  if (!id) {
    return
  }
  const query = {
    id: id,
  }
  if (revId) {
    query.revId = revId
  }
  queryFormJsonAPI(query).then(res => {
    if(res.data) {
      designer.value = JSON.parse(res.data);
      // 打开设计器默认选中第一个组件，右侧设置面板可直接编辑；无组件时不选
      designer.value.selectedId = designer.value.widgetList.length > 0 ? 0 : null;
    }
  });
}

/**
 * 清空组件列表
 */
function handleClearWidget() {
  designer.value = {
    widgetList: [],
    formConfig: {
      width: 600,
      position: "right",
      size: "default",
    },
    selectedId: null
  };
}

/**
 * 保存表单
 */
function handleSaveForm() {
  if (!id) {
    return
  }
  const form = {
    id: id,
    formJson: JSON.stringify(designer.value),
  }
  saveFormJsonAPI(form).then(res => {
    if (res.code !== 200) {
      return;
    }
    ElMessage.success("保存成功");
  });
}

/**
 * 预览表单
 */
function handleViewForm() {
  // 深拷贝一份给预览用，避免预览时的事件动作污染画布数据后被保存落库
  previewForm.value = JSON.parse(JSON.stringify(designer.value));
  formKey.value++;
  viewFormVisible.value = true;
}

/**
 * 当前表单可移植的JSON：组件列表与表单配置，不含选中态
 */
function buildFormJson() {
  return {
    widgetList: designer.value.widgetList,
    formConfig: designer.value.formConfig,
  }
}

function handleOpenJson() {
  jsonText.value = JSON.stringify(buildFormJson(), null, 2);
  jsonVisible.value = true;
}

function handleCopyJson() {
  const text = JSON.stringify(buildFormJson(), null, 2);
  const done = () => ElMessage.success("已复制表单JSON");
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text).then(done).catch(() => fallbackCopy(text, done));
  } else {
    fallbackCopy(text, done);
  }
}

function fallbackCopy(text, done) {
  const ta = document.createElement('textarea');
  ta.value = text;
  ta.style.position = 'fixed';
  ta.style.opacity = '0';
  document.body.appendChild(ta);
  ta.select();
  try {
    document.execCommand('copy');
    done();
  } finally {
    document.body.removeChild(ta);
  }
}

/**
 * 应用JSON替换当前表单：整体覆盖组件列表与表单配置，不保留原有组件
 */
function handleApplyJson() {
  let json;
  try {
    json = JSON.parse(jsonText.value);
  } catch (e) {
    ElMessage.warning("JSON格式有误，请检查后重试");
    return;
  }
  // 兼容只粘贴组件数组的情况
  const sourceList = Array.isArray(json.widgetList) ? json.widgetList : (Array.isArray(json) ? json : []);
  if (sourceList.length === 0) {
    ElMessage.warning("JSON中没有可应用的组件");
    return;
  }
  // 深拷贝后再挂到 designer，避免持有外部引用
  const widgets = sourceList.map(widget => JSON.parse(JSON.stringify(widget)));
  designer.value.widgetList = widgets;
  if (json.formConfig) {
    designer.value.formConfig = { ...json.formConfig };
  }
  designer.value.selectedId = null;
  jsonText.value = JSON.stringify(buildFormJson(), null, 2);
  ElMessage.success("已应用" + widgets.length + "个组件");
}
</script>
<style lang="scss" scoped>
.component-box {
  height: 100vh;
  padding: 0;
  background-color: $bg-card;
}
.left-widget-panel {
  height: 100%;
  padding: $spacing-sm;
  background-color: $bg-card;
  border: 1px solid $border-color-light;
  border-radius: 0 $border-radius-md $border-radius-md 0;
  box-shadow: $shadow-card;
  transition: $transition-normal;
  &:hover {
    box-shadow: $shadow-card-hover;
  }
  width: 15%;
  min-width: 208px;
  max-width: 240px;
}
.middle-form-widget {
  height: 100%;
  padding: 0 10px;
  .el-main {
    background-color: transparent;
  }
  .el-header {
    padding: 5px 10px 0;
    height: 40px;
    border-bottom: 1px solid $border-color-light;
  }
  .toolbar-buttons {
    display: flex;
    justify-content: flex-end;
    align-items: center;
  }
}
.right-setting-panel {
  height: 100%;
  padding: $spacing-sm;
  background-color: $bg-card;
  border: 1px solid $border-color-light;
  border-radius: $border-radius-md 0 0 $border-radius-md;
  box-shadow: $shadow-card;
  transition: $transition-normal;
  &:hover {
    box-shadow: $shadow-card-hover;
  }
  width: 20%;
  min-width: 300px;
  max-width: 340px;
}
.json-box {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.json-box :deep(.el-textarea) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.json-box :deep(.el-textarea__inner) {
  flex: 1;
  min-height: 0;
  height: 100%;
  resize: none;
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
}
</style>

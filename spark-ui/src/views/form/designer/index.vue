<template>
  <el-container class="component-box">
    <!--左侧面板-->
    <el-aside class="left-widget-panel">
      <widgetPanel :designer="designer" />
    </el-aside>
    <!--中间面板-->
    <el-container class="middle-form-widget">
      <!--工具栏-->
      <el-header>
        <el-button
            type="danger"
            @click="handleClearWidget"
        ><el-icon><Delete /></el-icon>清空</el-button>
        <el-button
            type="warning"
            @click="handleViewForm"
        ><el-icon><View /></el-icon>预览表单</el-button>
        <el-button
            type="primary"
            @click="handleSaveForm"
        ><el-icon><Select /></el-icon>保存模板</el-button>
      </el-header>
      <!--表单-->
      <el-main>
        <formWidget :designer="designer" />
      </el-main>
    </el-container>
    <!-- 右侧-->
    <el-aside class="right-setting-panel">
      <settingPanel :designer="designer" v-if="designer.widgetList.length > 0"/>
    </el-aside>
  </el-container>
  <!--表单预览组件-->
  <el-dialog
      v-model="viewFormVisible"
      title="表单预览"
      :width="designer.formConfig.width"
  >
    <form-view
        :form="designer"
        :key="formKey"
    />
  </el-dialog>
</template>
<script setup>
import {ref} from 'vue';
import widgetPanel from './widgetPanel/index.vue';
import formWidget from './formWidget/index.vue';
import settingPanel from './settingPanel/index.vue';
import FormView from '../../../components/FormView/index.vue';
import {queryFormJsonAPI, saveFormJsonAPI} from '@/api/form/form.js';
import { useRoute } from 'vue-router';
import {ElMessage} from "element-plus";
const route = useRoute();
const id = route.params.id;
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

handleGetFormJson();

/**
 * 获取表单json
 */
function handleGetFormJson() {
  if (!id && !revId) {
    return
  }
  const query = {
    id: id,
    revId: revId,
  }
  queryFormJsonAPI(query).then(res => {
    if(res.data) {
      designer.value = JSON.parse(res.data);
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
  formKey.value++;
  viewFormVisible.value = true;
}
</script>
<style lang="scss" scoped>
.component-box {
  background-color: white
}
.left-widget-panel {
  width: 15%;
  border-right: 1px dashed #0052cc;
  height: 100vh;
  padding-right: 10px;
  padding-left: 10px
}
.middle-form-widget {
  padding-right: 10px;
  padding-left: 10px;
  .el-header {
    padding-top: 5px;
    height: 40px;
  }
}
.right-setting-panel {
  width: 20%;
  border-left: 1px dashed #0052cc;
  height: 100vh;
  padding-right: 10px;
  padding-left: 10px
}
</style>

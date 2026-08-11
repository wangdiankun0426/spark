<template>
  <el-tabs v-model="activeName">
      <el-tab-pane name="componentSetting">
        <template #label>
          <span>组件设置</span>
        </template>
        <component
            v-if="selectedId !== null"
            :is="getFieldSettingName(widget)"
            :key="widget.config.code"
            :config="widget.config"
        />
      </el-tab-pane>
      <el-tab-pane name="formSetting">
        <template #label>
          <span>表单设置</span>
        </template>
        <el-form label-width="auto">
          <el-form-item label="宽度" label-position="right">
            <el-input-number
                v-model="formConfig.width"
                :min="600"
                :max="1000"
                controls-position="right"
            />
          </el-form-item>
          <el-form-item label="对齐方式" label-position="right">
            <el-radio-group v-model="formConfig.position">
              <el-radio-button value="left" label="left">靠左</el-radio-button>
              <el-radio-button value="right" label="right">靠右</el-radio-button>
              <el-radio-button value="top" label="top">靠上</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="组件大小" label-position="right">
            <el-radio-group v-model="formConfig.size">
              <el-radio-button value="large" label="large">较大</el-radio-button>
              <el-radio-button value="default" label="default">默认</el-radio-button>
              <el-radio-button value="small" label="small">较小</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
</template>
<script setup>
import {ref, watch} from 'vue';
const props = defineProps({
  designer: Object,
})
const activeName = ref('componentSetting');
const widget = ref({});
const formConfig = ref({});
const selectedId = ref(null);

getSelectedWidget();

/**
 * 获取当前被选中的组件配置
 * */
function getSelectedWidget() {
  selectedId.value = props.designer.selectedId;
  formConfig.value = props.designer.formConfig;
  if (selectedId.value !== null) {
    widget.value = props.designer.widgetList[props.designer.selectedId];
  }
}

// 监听 designer 的变化
watch(props.designer, (newValue, oldValue) => {
  getSelectedWidget();
});

/**
 * 获取自定义组件配置组件名称
 */
function getFieldSettingName(widget) {
  return "custom-"+widget.type+"-setting";
}
</script>

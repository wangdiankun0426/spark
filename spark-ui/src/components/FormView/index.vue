<template>
  <el-form
      class="form-inline"
      :label-position="props.form.formConfig.position"
      :size="props.form.formConfig.size"
      :disabled="disabled"
  >
    <el-row :gutter="24">
      <el-col :span="widget.config.width"
              v-for="(widget, index) in props.form.widgetList"
              :key="index">
        <component
            :is="getFieldName(widget)"
            :widget="widget"
            :key="widget.config.code"
            @widget-event="handleWidgetEvent"
        />
      </el-col>
    </el-row>
  </el-form>
</template>
<script setup>
import { onMounted, onBeforeUnmount } from 'vue';
import { runWidgetActions } from './formAction.js';

const props = defineProps({
  form: Object,
  disabled: { type: Boolean, default: false },
});

// 表单级事件：mounted 渲染完成、destroyed 卸载、widget-event 所有组件事件的汇总出口
// 业务页面按客户需求做表单定制时直接监听，例：
//   <form-view :form="formJson" @widget-event="handleWidgetEvent" @mounted="handleFormMounted" />
//   function handleWidgetEvent(payload) {
//     // payload: { widget, type, code, label, eventName, value, showValue, oldValue, config }
//     if (payload.eventName === 'change' && payload.code === 'fieldXxx') { ... }
//   }
const emit = defineEmits(['widget-event', 'mounted', 'destroyed']);

/**
 * 获取自定义组件名称
 * @param widget
 * @returns {string}
 */
function getFieldName(widget) {
  return "custom-"+widget.type;
}

/**
 * 处理组件事件：先执行设计器配置的声明式动作，再把事件抛给业务页面做自定义逻辑
 * @param payload 标准事件对象（含 widget、eventName、value、oldValue 等）
 */
function handleWidgetEvent(payload) {
  runWidgetActions(payload.widget, payload.eventName, props.form.widgetList, props.disabled);
  emit('widget-event', payload);
}

onMounted(() => {
  emit('mounted', props.form);
});

onBeforeUnmount(() => {
  emit('destroyed', props.form);
});

</script>
<style scoped lang="scss">
.form-inline {
}
</style>

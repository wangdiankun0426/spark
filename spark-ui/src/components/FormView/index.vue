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
        />
      </el-col>
    </el-row>
  </el-form>
</template>
<script setup>
const props = defineProps({
  form: Object,
  disabled: { type: Boolean, default: false },
});

/**
 * 获取自定义组件名称
 * @param widget
 * @returns {string}
 */
function getFieldName(widget) {
  return "custom-"+widget.type;
}

</script>
<style scoped lang="scss">
.form-inline {
  //height: 400px;
  //min-height: 60vh;
}
</style>

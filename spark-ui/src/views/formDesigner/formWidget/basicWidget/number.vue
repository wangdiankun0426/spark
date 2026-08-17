<template>
  <el-form-item
      :label="props.widget.config.label"
      :required="props.widget.config.required"
      v-if="!props.widget.config.hidden"
  >
    <el-input-number
        v-model="props.widget.config.value"
        :min="props.widget.config.min"
        :max="props.widget.config.max"
        :step="props.widget.config.step"
        :disabled="props.widget.config.disabled"
        style="width:100%"
        @change="handleChangeShowValue"
    />
  </el-form-item>
</template>

<script setup>
defineOptions({
  name: "customNumber"
});
const props = defineProps({
  widget: Object,
});

/**
 * 补充showValue
 */
function handleChangeShowValue() {
  props.widget.config.showValue = props.widget.config.value;
}
</script>

<style lang="scss" scoped>
.el-form-item {
  margin-bottom: 10px;
}
</style>

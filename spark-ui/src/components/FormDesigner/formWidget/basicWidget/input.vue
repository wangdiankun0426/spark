<template>
  <el-form-item
      :label="props.widget.config.label"
      v-if="!props.widget.config.hidden"
  >
    <el-input
        v-model="props.widget.config.value"
        :placeholder="props.widget.config.placeholder"
        :disabled="props.widget.config.disabled"
        :readonly="props.widget.config.readonly"
        clearable
        @change="handleChangeShowValue"
    />
  </el-form-item>
</template>

<script setup>
defineOptions({
  name: "customInput"
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

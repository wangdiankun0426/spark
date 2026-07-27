<template>
  <el-form-item
      :label="props.widget.config.label"
      v-if="!props.widget.config.hidden"
  >
    <el-select
        v-model="props.widget.config.value"
        :disabled="props.widget.config.disabled"
        :placeholder="props.widget.config.placeholder"
        clearable
        @change="handleChangeShowValue"
    >
      <el-option
          v-for="option in props.widget.config.options"
          :key="option.value"
          :value="option.value"
          :label="option.label"
      />
    </el-select>
  </el-form-item>
</template>

<script setup>
defineOptions({
  name: "customSelect"
});
const props = defineProps({
  widget: Object,
});

/**
 * 获取type值
 * @param value
 * @returns {string}
 */
function getTypeLabelByKey(value) {
  for (let item of props.widget.config.options) {
    if (item.value === value) {
      return item.label;
    }
  }
  return "未知";
}

/**
 * 补充showValue
 */
function handleChangeShowValue() {
  const value = props.widget.config.value;
  props.widget.config.showValue = getTypeLabelByKey(value);
}
</script>

<style lang="scss" scoped>
.el-form-item {
  margin-bottom: 10px;
}
</style>

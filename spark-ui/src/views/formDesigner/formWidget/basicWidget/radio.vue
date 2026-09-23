<template>
  <el-form-item
      :label="props.widget.config.label"
      :required="props.widget.config.required"
      v-if="!props.widget.config.hidden"
      @focusin="emitEvent('focus')"
      @focusout="emitEvent('blur')"
      @click="emitEvent('click')"
  >
    <el-radio-group
        v-model="props.widget.config.value"
        @change="handleChangeShowValue"
    >
      <el-radio
          v-for="option in props.widget.config.options"
          :key="option.value"
          :label="option.value"
          :disabled="props.widget.config.disabled"
      >
        {{ option.label }}
      </el-radio>
    </el-radio-group>
  </el-form-item>
</template>

<script setup>
import { useWidgetEventBridge } from '@/components/FormView/formEvent.js';

defineOptions({
  name: "customRadio"
});
const props = defineProps({
  widget: Object,
});
const emit = defineEmits(['widget-event']);
const { emitEvent, emitChange } = useWidgetEventBridge(props.widget, emit);

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
 * 补充showValue并抛值改变事件
 */
function handleChangeShowValue() {
  const value = props.widget.config.value;
  props.widget.config.showValue = getTypeLabelByKey(value);
  emitChange();
}
</script>

<style lang="scss" scoped>
.el-form-item {
  margin-bottom: 10px;
}
</style>

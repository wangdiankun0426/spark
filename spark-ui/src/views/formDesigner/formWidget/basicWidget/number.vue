<template>
  <el-form-item
      :label="props.widget.config.label"
      :required="props.widget.config.required"
      v-if="!props.widget.config.hidden"
      @focusin="emitEvent('focus')"
      @focusout="emitEvent('blur')"
      @click="emitEvent('click')"
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
import { useWidgetEventBridge } from '@/components/FormView/formEvent.js';

defineOptions({
  name: "customNumber"
});
const props = defineProps({
  widget: Object,
});
const emit = defineEmits(['widget-event']);
const { emitEvent, emitChange } = useWidgetEventBridge(props.widget, emit);

/**
 * 补充showValue并抛值改变事件
 */
function handleChangeShowValue() {
  props.widget.config.showValue = props.widget.config.value;
  emitChange();
}
</script>

<style lang="scss" scoped>
.el-form-item {
  margin-bottom: 10px;
}
</style>

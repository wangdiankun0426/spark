<template>
  <el-form-item
      :label="props.widget.config.label"
      :required="props.widget.config.required"
      v-if="!props.widget.config.hidden"
      @focusin="emitEvent('focus')"
      @focusout="emitEvent('blur')"
      @click="emitEvent('click')"
  >
    <el-date-picker
        v-model="props.widget.config.value"
        :type="props.widget.config.dateType"
        :format="dateFormat"
        :value-format="dateFormat"
        :placeholder="props.widget.config.placeholder"
        :disabled="props.widget.config.disabled || props.widget.config.readonly"
        style="width:100%"
        @change="handleChangeShowValue"
    />
  </el-form-item>
</template>

<script setup>
import { computed } from 'vue';
import { useWidgetEventBridge } from '@/components/FormView/formEvent.js';

defineOptions({
  name: "customDate"
});
const props = defineProps({
  widget: Object,
});
const emit = defineEmits(['widget-event']);
const { emitEvent, emitChange } = useWidgetEventBridge(props.widget, emit);

/** 日期格式：datetime 带时分秒，date 仅日期 */
const dateFormat = computed(() => props.widget.config.dateType === 'datetime' ? 'YYYY-MM-DD HH:mm:ss' : 'YYYY-MM-DD');

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

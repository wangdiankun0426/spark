<template>
    <!-- 多行文本：保留原始换行 -->
    <text v-if="isTextarea" class="field-value-text field-value-text--area">{{ fieldValueText }}</text>

    <!-- 其他字段：纯文本展示 -->
    <text v-else class="field-value-text">{{ fieldValueText }}</text>
</template>
<script setup>
import {computed} from 'vue';
import {
  isAttachmentField,
  getAttachmentNames,
  getFieldValueText,
  EMPTY_TEXT
} from './formUtil';

// 多行文本字段类型
const FORM_TYPE_TEXTAREA = 'textarea';

const props = defineProps({
  // 表单组件对象（含 type 与 config），与 type/value/showValue 二选一
  widget: {
    type: Object,
    default: null
  },
  // 字段类型（未传 widget 时指定）
  type: {
    type: String,
    default: ''
  },
  // 表单值（未传 widget 时指定）
  value: {
    type: [String, Number],
    default: null
  },
  // 表单展示值（未传 widget 时指定）
  showValue: {
    type: [String, Number],
    default: null
  },
  // 空值占位文本
  emptyText: {
    type: String,
    default: EMPTY_TEXT
  }
});

/** 字段配置：未传组件对象时按外部属性组装，保证取值逻辑统一 */
const fieldConfig = computed(() => {
  const config = (props.widget && props.widget.config) || {};
  return {
    type: (props.widget && props.widget.type) || props.type || '',
    value: config.value !== undefined ? config.value : props.value,
    showValue: config.showValue !== undefined ? config.showValue : props.showValue,
    options: config.options || []
  };
});

/** 是否为上传附件字段 */
const isAttachment = computed(() => isAttachmentField(fieldConfig.value.type));

/** 是否为多行文本字段 */
const isTextarea = computed(() => fieldConfig.value.type === FORM_TYPE_TEXTAREA);

/** 字段展示文本：附件解析 JSON 后取文件名称，其余字段按常规规则取值 */
const fieldValueText = computed(() => {
  const config = fieldConfig.value;
  if (isAttachment.value) {
    const names = getAttachmentNames(config.value, config.showValue);
    return names.length > 0 ? names.join('、') : props.emptyText;
  }
  return getFieldValueText(
      config.value,
      config.showValue,
      config.options,
      props.emptyText
  );
});
</script>
<style scoped lang="scss">
.field-value-text {
  word-break: break-all;
}

// 多行文本保留换行
.field-value-text--area {
  white-space: pre-wrap;
}
</style>

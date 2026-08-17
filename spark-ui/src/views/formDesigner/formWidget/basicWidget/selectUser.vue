<template>
  <el-form-item
      :label="props.widget.config.label"
      :required="props.widget.config.required"
      v-if="!props.widget.config.hidden"
  >
    <select-user
        v-model="userIdArray"
        :multiple="props.widget.config.multiple"
        :disabled="props.widget.config.disabled || props.widget.config.readonly"
        :placeholder="props.widget.config.placeholder"
        @update:showValue="handleShowValueUpdate"
    />
  </el-form-item>
</template>

<script setup>
import { computed } from 'vue'
import SelectUser from '@/components/SelectUser/index.vue'

defineOptions({
  name: "customSelectUser"
})
const props = defineProps({
  widget: Object,
})

// widget.config.value 约定：
//   多选：逗号串 "1,2,3"，转成 ID 数组与 v-model 双向同步
//   单选：单个 ID（Number）
const userIdArray = computed({
  get() {
    const raw = props.widget.config.value
    if (props.widget.config.multiple) {
      if (!raw) return []
      return String(raw)
          .split(',')
          .map(id => Number(id))
          .filter(Boolean)
    }
    return raw == null || raw === '' ? null : Number(raw)
  },
  set(val) {
    if (props.widget.config.multiple) {
      props.widget.config.value = (val || []).join(',')
    } else {
      props.widget.config.value = val == null ? null : val
    }
  }
})

// 同步 showValue：多选 emit 名称数组，单选 emit 单个名称字符串
function handleShowValueUpdate(names) {
  if (props.widget.config.multiple) {
    props.widget.config.showValue = (names || []).join(',')
  } else {
    props.widget.config.showValue = names || ''
  }
}
</script>

<style lang="scss" scoped>
.el-form-item {
  margin-bottom: 10px;
}
</style>

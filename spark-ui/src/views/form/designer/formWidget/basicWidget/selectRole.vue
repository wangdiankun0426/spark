<template>
  <el-form-item
      :label="props.widget.config.label"
      v-if="!props.widget.config.hidden"
  >
    <select-role
        v-model="roleIdArray"
        :multiple="props.widget.config.multiple"
        :disabled="props.widget.config.disabled || props.widget.config.readonly"
        :placeholder="props.widget.config.placeholder"
        @update:showValue="handleShowValueUpdate"
    />
  </el-form-item>
</template>

<script setup>
import { computed } from 'vue'
import SelectRole from '@/components/SelectRole/index.vue'

defineOptions({
  name: "customSelectRole"
})
const props = defineProps({
  widget: Object,
})

const roleIdArray = computed({
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
<template>
  <el-form-item
      :label="props.widget.config.label"
      :required="props.widget.config.required"
      v-if="!props.widget.config.hidden"
  >
    <el-select
        v-model="knowledgeId"
        :multiple="props.widget.config.multiple"
        :disabled="props.widget.config.disabled || props.widget.config.readonly"
        :placeholder="props.widget.config.placeholder"
        clearable
        filterable
        style="width: 100%"
        @change="handleChange"
    >
      <el-option
          v-for="item in knowledgeList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
      />
    </el-select>
  </el-form-item>
</template>

<script setup>
import { computed, ref } from 'vue'
import { pageKnowledgeListAPI } from '@/api/kb/knowledge.js'

defineOptions({
  name: "customSelectKnowledge"
})

const props = defineProps({
  widget: Object,
})

// 知识库选项列表
const knowledgeList = ref([])

/**
 * 加载知识库选项列表
 */
const loadKnowledgeList = () => {
  pageKnowledgeListAPI({ page: false }).then(res => {
    if (res.code === 200 && res.data) {
      knowledgeList.value = res.data.rows || []
    }
  }).catch(() => {})
}
loadKnowledgeList()

// widget.config.value 约定：
//   多选：逗号串 "1,2,3"，转成 ID 数组与 v-model 双向同步
//   单选：单个 ID（Number）
const knowledgeId = computed({
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

/**
 * 选中变化时同步 showValue（选中知识库名称）
 * @param val 选中的知识库id（多选为数组）
 */
function handleChange(val) {
  if (props.widget.config.multiple) {
    props.widget.config.showValue = (val || []).map(id => findKnowledgeName(id)).join(',')
  } else {
    props.widget.config.showValue = val == null ? '' : findKnowledgeName(val)
  }
}

/**
 * 根据知识库id查询名称
 * @param id 知识库id
 * @returns {string} 知识库名称
 */
function findKnowledgeName(id) {
  const item = knowledgeList.value.find(el => el.id === id)
  return item ? item.name : ''
}
</script>

<style lang="scss" scoped>
.el-form-item {
  margin-bottom: 10px;
}
</style>

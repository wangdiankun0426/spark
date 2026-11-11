<template>
  <div class="kb-archive-config">
    <el-divider>知识库归档配置</el-divider>
    <el-form-item label="知识库">
      <el-select
          v-model="form.config.knowledgeId"
          placeholder="选择知识库"
          style="width:100%"
          clearable
          filterable
          @change="emit('update')">
        <el-option
            v-for="k in knowledgeList"
            :key="k.id"
            :label="k.name"
            :value="k.id"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="文档ID">
      <var-field
          clearable
          v-model="form.config.fileCode"
          placeholder="点击选择变量"
          @change="emit('update')"
      />
    </el-form-item>
    <el-divider>输出数据</el-divider>
    <div
        v-for="b in outputVar"
        :key="b.value"
        class="var-row"
    >
      <span class="var-label">{{ b.label }}</span>
      <code class="var-code" :title="b.value" v-text="b.value"></code>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { pageKnowledgeListAPI } from '@/api/kb/knowledge.js'
import VarField from '../var/VarField.vue'
import { getNodeOutputs } from '../nodes/index.js'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])

const knowledgeList = ref([])
onMounted(async () => {
  // 字典接口失败时保持空候选，不阻断面板挂载
  try {
    const res = await pageKnowledgeListAPI({ page: false })
    if (res && res.code === 200 && res.data?.rows) {
      knowledgeList.value = res.data.rows
    }
  } catch (e) {
    knowledgeList.value = []
  }
})

const outputVar = computed(() => getNodeOutputs(props.form))
</script>

<style scoped lang="scss">
.var-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px;
  .var-label {
    flex-shrink: 0;
    font-size: 13px;
    color: $color-text-primary;
  }
  .var-code {
    flex: 1;
    min-width: 0;
    font-size: 12px;
    color: $app-primary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>

<template>
  <div class="rag-retrieve-config">
    <el-divider>知识库检索配置</el-divider>
    <el-form-item label="知识库">
      <el-select
          v-model="form.config.kbIds"
          multiple
          placeholder="选择知识库"
          style="width:100%"
          @change="emit('update')"
          clearable
          size="default"
      >
        <el-option v-for="kb in kbList" :key="kb.id" :label="kb.name" :value="kb.id" />
      </el-select>
    </el-form-item>
    <el-form-item label="检索问题">
      <var-field
          clearable
          v-model="form.config.query"
          textarea
          :rows="4"
          placeholder="输入检索问题，支持变量"
          @change="emit('update')"
      />
    </el-form-item>
    <el-form-item label="返回条数">
      <el-input-number
          v-model="form.config.topK"
          :min="1"
          :max="20"
          :step="1"
          placeholder="条"
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
import { ref, onMounted, computed } from 'vue'
import { pageKnowledgeListAPI } from '@/api/kb/knowledge.js'
import VarField from '../var/VarField.vue'
import { getNodeOutputs } from '../nodes/index.js'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])

const kbList = ref([])
onMounted(async () => {
  // 字典接口失败时保持空候选，不阻断面板挂载
  try {
    const res = await pageKnowledgeListAPI({ page: false })
    if (res && res.code === 200 && res.data?.rows) {
      kbList.value = res.data.rows
    }
  } catch (e) {
    kbList.value = []
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

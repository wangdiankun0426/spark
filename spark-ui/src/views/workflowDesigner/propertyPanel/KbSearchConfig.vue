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
      <el-input
          v-model="form.config.query"
          type="textarea"
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
      <code class="var-code" v-text="b.value"></code>
      <el-button
          type="primary"
          link
          @click="copyVar(b.value)"
      >复制</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { pageKnowledgeListAPI } from '@/api/kb/knowledge.js'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])

const kbList = ref([])
onMounted(async () => {
  const res = await pageKnowledgeListAPI({ page: false })
  if (res.code === 200 && res.data?.rows) {
    kbList.value = res.data.rows
  }
})

/** 节点输出变量 */
const outputVar = computed(() => [
  { label: '检索结果', value: `#{node:${props.form.id}result}#` },
])

/** 复制变量到剪贴板*/
function copyVar(text) {
  const done = () => ElMessage.success('已复制: ' + text)
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text).then(done).catch(() => fallbackCopy(text, done))
  } else {
    fallbackCopy(text, done)
  }
}
function fallbackCopy(text, done) {
  const ta = document.createElement('textarea')
  ta.value = text
  ta.style.position = 'fixed'
  ta.style.opacity = '0'
  document.body.appendChild(ta)
  ta.select()
  try {
    document.execCommand('copy')
    done()
  } finally {
    document.body.removeChild(ta)
  }
}
</script>

<style scoped lang="scss">
.var-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 4px;
  .var-label {
    width: 60px;
    flex-shrink: 0;
    font-size: 13px;
    color: $color-text-primary;
  }
  .var-code {
    flex: 1;
    font-size: 14px;
    color: #409eff;
    word-break: break-all;
  }
}
</style>

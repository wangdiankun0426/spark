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
      <el-input
          v-model="form.config.fileCode"
          placeholder="支持变量，详见变量帮助"
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
import { ref, onMounted } from 'vue'
import { pageKnowledgeListAPI } from '@/api/kb/knowledge.js'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])

const knowledgeList = ref([])
onMounted(async () => {
  const res = await pageKnowledgeListAPI({ page: false })
  if (res.code === 200 && res.data?.rows) {
    knowledgeList.value = res.data.rows
  }
})

/** 节点输出变量 */
const outputVar = [
  { label: '知识库ID', value: `#{node:${props.form.id}kbId}#` },
  { label: '归档后文档ID', value: `#{node:${props.form.id}fileId}#` },
];

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

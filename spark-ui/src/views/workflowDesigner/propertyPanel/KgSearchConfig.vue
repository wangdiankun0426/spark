<template>
  <div class="knowledge-graph-config">
    <el-divider>知识图谱检索配置</el-divider>
    <el-form-item label="知识图谱">
      <el-select
          v-model="form.config.graphId"
          multiple
          placeholder="选择知识图谱"
          style="width:100%"
          @change="emit('update')"
          clearable
          size="default"
      >
        <el-option v-for="g in graphList" :key="g.id" :label="g.name" :value="g.id" />
      </el-select>
    </el-form-item>
    <el-form-item label="查询内容">
      <el-input
          v-model="form.config.query"
          type="textarea"
          :rows="4"
          placeholder="输入查询内容，支持变量"
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
import { ElMessage } from 'element-plus'
import { pageGraphListAPI } from '@/api/kg/graph.js'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])

const graphList = ref([])
onMounted(async () => {
  const res = await pageGraphListAPI({ page: false })
  if (res.code === 200 && res.data?.rows) {
    graphList.value = res.data.rows
  }
})

/** 节点输出变量 */
const outputVar = [
  { label: '查询结果', value: `#{node:${props.form.id}result}#` },
]

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

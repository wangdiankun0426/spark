<template>
  <div class="llm-task-config">
    <el-divider>LLM配置</el-divider>
    <el-form-item label="文档ID">
      <el-input
          v-model="form.config.fileCode"
          placeholder="支持变量，详见变量帮助"
          @change="emit('update')"
      />
    </el-form-item>
    <el-form-item label="模型">
      <el-select
          v-model="form.config.modelId"
          placeholder="选择模型"
          style="width:100%"
          @change="emit('update')"
          clearable
          size="default"
      >
        <el-option v-for="m in modelList" :key="m.id" :label="m.name" :value="m.id" />
      </el-select>
    </el-form-item>
    <el-form-item label="提示词">
      <el-input
          v-model="form.config.prompt"
          type="textarea"
          :rows="12"
          placeholder="输入提示词模板，支持变量，详见下方文档内容说明"
          @change="emit('update')"
      />
      <div class="llm-doc-tips">
        如需模型响应参数，在提示词中规范模型的响应数据即可在下游节点中使用
        <div class="tips-title">提示词变量</div>
        <div class="tips-line">文档内容 <code>#{base:content}#</code></div>
      </div>
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
import { pageModelListAPI } from '@/api/llm/model.js'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])

const modelList = ref([])
onMounted(async () => {
  const res = await pageModelListAPI({ page: false, type: 1 })
  if (res.code === 200 && res.data?.rows) {
    modelList.value = res.data.rows
  }
})

/** 节点输出变量 */
const outputVar = [
  { label: '模型响应', value: `#{node:${props.form.id}text}#` },
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
.llm-doc-tips {
  width: 100%;
  margin-top: 8px;
  padding: 8px 10px;
  border: 1px solid $border-color-light;
  border-radius: 4px;
  background: #fafafa;
  font-size: 12px;
  color: $color-text-secondary;
  line-height: 1.8;

  .tips-title {
    font-weight: 600;
    color: $color-text-primary;
  }

  .tips-line {
    code {
      padding: 0 3px;
      border-radius: 3px;
      background: #f0f2f5;
      color: #0052cc;
    }
  }
}

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

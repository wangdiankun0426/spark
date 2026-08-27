<template>
  <div class="agent-task-config">
    <el-divider>Agent配置</el-divider>
    <el-form-item label="智能体">
      <el-select
          v-model="form.config.agentId"
          placeholder="选择智能体"
          style="width:100%"
          @change="emit('update')"
          clearable
          size="default"
      >
        <el-option v-for="a in agentList" :key="a.id" :label="a.name" :value="a.id" />
      </el-select>
    </el-form-item>
    <el-form-item label="任务描述">
      <el-input
          v-model="form.config.task"
          type="textarea"
          :rows="6"
          placeholder="输入Agent任务描述，支持变量"
          @change="emit('update')"
      />
    </el-form-item>
    <el-form-item label="记忆ID">
      <el-input
          clearable
          v-model="form.config.memoryId"
          placeholder="选填，隔离对话历史的记忆ID，支持变量"
          @change="emit('update')"
      />
      <div class="form-tips">不填时默认以节点ID作为记忆ID</div>
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
import { pageAgentListAPI } from '@/api/llm/agent.js'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])

const agentList = ref([])
onMounted(async () => {
  const res = await pageAgentListAPI({ page: false })
  if (res.code === 200 && res.data?.rows) {
    agentList.value = res.data.rows
  }
})

/** 节点输出变量 */
const outputVar = [
  { label: 'Agent响应', value: `#{node:${props.form.id}text}#` },
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
.form-tips {
  width: 100%;
  font-size: 12px;
  color: $color-text-secondary;
  line-height: 1.5;
  margin-top: 4px;
}

.var-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 4px;
  .var-label {
    width: 80px;
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

<template>
  <div class="human-review-config">
    <el-divider>人工审核配置</el-divider>
    <el-form-item label="审核提示">
      <el-input
          v-model="form.config.prompt"
          type="textarea"
          :rows="4"
          placeholder="输入审核提示信息"
          @change="emit('update')"
      />
    </el-form-item>
    <el-form-item label="审核人">
      <el-select
          v-model="form.config.reviewerIds"
          multiple
          placeholder="选择审核人"
          style="width:100%"
          @change="emit('update')"
          clearable
          size="default"
      >
        <el-option v-for="u in userList" :key="u.id" :label="u.name" :value="u.id" />
      </el-select>
    </el-form-item>
    <el-form-item label="审核类型">
      <el-radio-group v-model="form.config.reviewType" @change="emit('update')">
        <el-radio value="approve">审批通过</el-radio>
        <el-radio value="reject">审批拒绝</el-radio>
        <el-radio value="both">通过或拒绝</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="必须填写意见">
      <el-switch
          v-model="form.config.requireComment"
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
import { pageUserListAPI } from '@/api/sys/user.js'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])

const userList = ref([])
onMounted(async () => {
  const res = await pageUserListAPI({ page: false })
  if (res.code === 200 && res.data?.rows) {
    userList.value = res.data.rows
  }
})

/** 节点输出变量 */
const outputVar = [
  { label: '审核结果', value: `#{node:${props.form.id}approved}#` },
  { label: '审核意见', value: `#{node:${props.form.id}comment}#` },
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

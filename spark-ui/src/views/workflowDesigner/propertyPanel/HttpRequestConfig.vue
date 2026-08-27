<template>
  <div class="http-request-config">
    <el-divider>HTTP请求配置</el-divider>
    <el-form-item label="请求方法">
      <el-select
          v-model="form.config.method"
          placeholder="选择方法"
          style="width:120px"
          @change="emit('update')"
          clearable
          size="default"
      >
        <el-option label="GET" value="GET" />
        <el-option label="POST" value="POST" />
        <el-option label="PUT" value="PUT" />
        <el-option label="DELETE" value="DELETE" />
        <el-option label="PATCH" value="PATCH" />
      </el-select>
    </el-form-item>
    <el-form-item label="请求地址">
      <el-input
          v-model="form.config.url"
          placeholder="输入URL，支持变量"
          @change="emit('update')"
      />
    </el-form-item>
    <el-form-item label="请求头">
      <el-input
          v-model="form.config.headers"
          type="textarea"
          :rows="4"
          placeholder="JSON格式，如：{&quot;Content-Type&quot;: &quot;application/json&quot;}"
          @change="emit('update')"
      />
    </el-form-item>
    <el-form-item label="请求体">
      <el-input
          v-model="form.config.body"
          type="textarea"
          :rows="6"
          placeholder="JSON格式请求体，支持变量"
          @change="emit('update')"
      />
    </el-form-item>
    <el-form-item label="请求超时">
      <el-input-number
          v-model="form.config.httpTimeoutMs"
          :min="1000"
          :max="300000"
          :step="1000"
          :step-strictly="true"
          placeholder="毫秒"
          @change="emit('update')"
      />
      <div class="form-tips">HTTP连接/读取超时时间，默认30000ms</div>
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
import { ElMessage } from 'element-plus'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])

/** 节点输出变量 */
const outputVar = [
  { label: '响应状态', value: `#{node:${props.form.id}statusCode}#` },
  { label: '响应体', value: `#{node:${props.form.id}body}#` },
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

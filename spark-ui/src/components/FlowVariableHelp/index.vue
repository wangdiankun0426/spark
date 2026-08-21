<template>
  <el-dialog
      :model-value="modelValue"
      title="变量帮助"
      width="40%"
      @update:model-value="(v) => emit('update:modelValue', v)"
  >
    <el-tabs v-model="activeTab" style="height: 500px; overflow: auto">
      <el-tab-pane label="表单值" name="form">
        <el-empty
            v-if="formFields.length === 0"
            description="未绑定输入表单"
            :image-size="50"
        />
        <div
            v-for="f in formFields"
            :key="'v-' + f.code"
            class="var-row"
        >
          <span class="var-label">{{ f.label }}</span>
          <code class="var-code" v-text="'#{form:' + f.code + '}#'"></code>
          <el-button
              type="primary"
              link
              @click="copyVar('#{form:' + f.code + '}#')"
          >复制</el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane label="表达显示值" name="formTxt">
        <el-empty
            v-if="formFields.length === 0"
            description="未绑定输入表单"
            :image-size="50"
        />
        <div
            v-for="f in formFields"
            :key="'t-' + f.code"
            class="var-row">
          <span class="var-label">{{ f.label }}</span>
          <code class="var-code" v-text="'#{formTxt:' + f.code + '}#'"></code>
          <el-button
              type="primary"
              link
              @click="copyVar('#{formTxt:' + f.code + '}#')"
          >复制</el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane label="基础变量" name="base">
        <div
            v-for="b in baseVars"
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
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'VariableHelp' })

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  // 表单字段列表 [{ label, code }]，用于表单值/表达显示值页签
  formFields: { type: Array, default: () => [] },
  // 基础变量列表 [{ label, value }]，如 #{base:instanceId}#
  baseVars: { type: Array, default: () => [] }
})
const emit = defineEmits(['update:modelValue'])

const activeTab = ref('form')

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

<style lang="scss" scoped>
.var-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 4px;
  .var-label {
    width: 120px;
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

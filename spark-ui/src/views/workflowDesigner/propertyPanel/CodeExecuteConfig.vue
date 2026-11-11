<template>
  <div class="code-execute-config">
    <el-divider>代码执行配置</el-divider>
    <el-form-item label="Python脚本">
      <div class="code-field">
        <python-code :model-value="form.config.code" readonly>
          <template #actions>
            <el-button type="primary" link @click="openEditor">编辑</el-button>
            <el-button type="primary" link @click="copyCode">复制</el-button>
          </template>
        </python-code>
        <div class="code-hint">脚本需定义 main(params) 函数，return 字典供下游引用</div>
      </div>
    </el-form-item>

    <el-form-item label="输入参数">
      <div class="map-list">
        <el-button type="primary" link size="small" class="map-add" @click="addInput">添加参数</el-button>
        <div v-for="(item, index) in form.config.inputs" :key="index" class="map-row">
          <el-input
              v-model="item.name"
              class="map-name"
              placeholder="参数名"
              clearable
              @change="emit('update')"
          />
          <var-field
              v-model="item.value"
              class="map-value"
              placeholder="点击选择变量"
              clearable
              @change="emit('update')"
          />
          <el-button type="danger" link size="small" @click="removeInput(index)">删除</el-button>
        </div>
      </div>
    </el-form-item>

    <el-form-item label="输出声明">
      <div class="map-list">
        <el-button type="primary" link size="small" class="map-add" @click="addOutput">添加输出</el-button>
        <div v-for="(item, index) in form.config.outputs" :key="index" class="map-row">
          <el-input
              v-model="item.name"
              class="map-name"
              placeholder="参数名"
              clearable
              @change="emit('update')"
          />
          <el-input
              v-model="item.key"
              class="map-value"
              placeholder="参数，对应 main 返回字典的键"
              clearable
              @change="emit('update')"
          />
          <el-button type="danger" link size="small" @click="removeOutput(index)">删除</el-button>
        </div>
      </div>
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

    <el-dialog
        v-model="editorVisible"
        title="编辑 Python 脚本"
        width="780px"
        top="6vh"
        append-to-body
        :close-on-click-modal="false"
        @opened="onEditorOpened"
    >
      <python-code ref="editorRef" v-model="draft">
        <template #actions>
          <el-button type="primary" link @click="resetDefault">重置默认</el-button>
          <el-button type="primary" link @click="clearCode">清空</el-button>
        </template>
      </python-code>
      <template #footer>
        <el-button @click="editorVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCode">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PythonCode from '@/components/PythonCode/index.vue'
import VarField from '../var/VarField.vue'
import { DEFAULT_PYTHON_CODE, getNodeOutputs } from '@/views/workflowDesigner/nodes/index.js'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])

if (!Array.isArray(props.form.config.inputs)) {
  props.form.config.inputs = []
}
if (!Array.isArray(props.form.config.outputs)) {
  props.form.config.outputs = []
}

const editorVisible = ref(false)
const draft = ref('')
const editorRef = ref(null)

/** 输出变量：声明的键拼接节点id生成引用变量 */
const outputVar = computed(() => getNodeOutputs(props.form))

function addInput() {
  props.form.config.inputs.push({ name: '', value: '' })
  emit('update')
}

function removeInput(index) {
  props.form.config.inputs.splice(index, 1)
  emit('update')
}

function addOutput() {
  props.form.config.outputs.push({ name: '', key: '' })
  emit('update')
}

function removeOutput(index) {
  props.form.config.outputs.splice(index, 1)
  emit('update')
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

function openEditor() {
  draft.value = props.form.config.code || ''
  editorVisible.value = true
}

function onEditorOpened() {
  editorRef.value?.focus()
}

function saveCode() {
  props.form.config.code = draft.value
  emit('update')
  editorVisible.value = false
}

function resetDefault() {
  draft.value = DEFAULT_PYTHON_CODE
}

function clearCode() {
  draft.value = ''
}

function copyCode() {
  const done = () => ElMessage.success('脚本已复制')
  const text = props.form.config.code || ''
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text).then(done).catch(() => fallbackCopy(text, done))
  } else {
    fallbackCopy(text, done)
  }
}
</script>

<style scoped lang="scss">
.code-field {
  width: 100%;
}

.code-hint {
  margin-top: 6px;
  font-size: 12px;
  color: $app-text-secondary;
}

.map-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.map-add {
  align-self: flex-end;
}

.map-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.map-name {
  width: 120px;
  flex-shrink: 0;
}

.map-value {
  flex: 1;
}

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

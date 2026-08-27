<template>
  <div class="variable-op-config">
    <el-divider>变量操作配置</el-divider>
    <el-form-item label="操作类型">
      <el-select
          v-model="form.config.operation"
          placeholder="选择操作类型"
          style="width:100%"
          @change="emit('update')"
          clearable
          size="default"
      >
        <el-option label="赋值" value="set" />
        <el-option label="合并" value="merge" />
        <el-option label="拼接" value="concat" />
        <el-option label="格式化" value="format" />
        <el-option label="类型转换" value="convert" />
      </el-select>
    </el-form-item>

    <!-- 赋值：定义多个变量 -->
    <template v-if="form.config.operation === 'set'">
      <div v-for="(v, idx) in variables" :key="idx" class="var-item">
        <div class="var-item-header">
          <span>变量 {{ idx + 1 }}</span>
          <el-button
              v-if="variables.length > 1"
              link
              type="danger"
              size="small"
              @click="removeVariable(idx)"
          >删除</el-button>
        </div>
        <el-form-item label="变量名">
          <el-input
              v-model="v.name"
              placeholder="输入变量名"
              @change="syncVariables"
          />
        </el-form-item>
        <el-form-item label="变量值">
          <el-input
              v-model="v.value"
              placeholder="输入变量值，支持变量"
              @change="syncVariables"
          />
        </el-form-item>
      </div>
      <el-button type="primary" link @click="addVariable">
        <el-icon><Plus /></el-icon>添加变量
      </el-button>
    </template>

    <!-- 合并：多个来源拼接为一个字符串 -->
    <template v-else-if="form.config.operation === 'merge'">
      <el-form-item label="来源变量">
        <el-select
            v-model="form.config.sources"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入来源变量，支持变量"
            style="width:100%"
            @change="emit('update')"
        >
        </el-select>
      </el-form-item>
      <el-form-item label="分隔符">
        <el-input
            clearable
            v-model="form.config.separator"
            placeholder="默认逗号"
            @change="emit('update')"
        />
      </el-form-item>
    </template>

    <!-- 拼接：模板 -->
    <el-form-item v-else-if="form.config.operation === 'concat'" label="模板">
      <el-input
          v-model="form.config.template"
          type="textarea"
          :rows="4"
          placeholder="输入拼接模板，支持变量，如：${n_xxx.result}"
          @change="emit('update')"
      />
    </el-form-item>

    <!-- 格式化 / 类型转换：来源 + 目标 -->
    <template v-else-if="form.config.operation === 'format' || form.config.operation === 'convert'">
      <el-form-item label="来源变量">
        <el-input
            clearable
            v-model="form.config.source"
            placeholder="输入来源变量，支持变量"
            @change="emit('update')"
        />
      </el-form-item>
      <el-form-item v-if="form.config.operation === 'format'" label="格式">
        <el-select
            v-model="form.config.format"
            placeholder="选择格式"
            style="width:100%"
            @change="emit('update')"
            clearable
            size="default"
        >
          <el-option label="转大写" value="uppercase" />
          <el-option label="转小写" value="lowercase" />
          <el-option label="去首尾空格" value="trim" />
          <el-option label="数字" value="number" />
        </el-select>
      </el-form-item>
      <el-form-item v-else label="目标类型">
        <el-select
            v-model="form.config.targetType"
            placeholder="选择目标类型"
            style="width:100%"
            @change="emit('update')"
            clearable
            size="default"
        >
          <el-option label="字符串" value="string" />
          <el-option label="数字" value="number" />
          <el-option label="布尔" value="boolean" />
          <el-option label="JSON" value="json" />
        </el-select>
      </el-form-item>
    </template>

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
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])

/** 赋值操作的变量列表（本地编辑态） */
const variables = ref([{ name: '', value: '' }])

/** 选中节点变化时回填变量列表 */
watch(() => props.form, (newForm) => {
  if (newForm?.config?.variables && Array.isArray(newForm.config.variables)) {
    variables.value = JSON.parse(JSON.stringify(newForm.config.variables))
  } else {
    variables.value = [{ name: '', value: '' }]
  }
}, { immediate: true, deep: true })

/** 添加一行变量 */
function addVariable() {
  variables.value.push({ name: '', value: '' })
}

/** 删除一行变量 */
function removeVariable(idx) {
  if (variables.value.length > 1) {
    variables.value.splice(idx, 1)
    syncVariables()
  }
}

/** 将本地变量列表写回节点配置 */
function syncVariables() {
  if (!props.form.config) {
    props.form.config = {}
  }
  const valid = variables.value.filter(v => v.name)
  if (valid.length > 0) {
    props.form.config.variables = valid
  } else {
    delete props.form.config.variables
  }
  emit('update')
}

/** 节点输出变量 */
const outputVar = [
  { label: '操作结果', value: `#{node:${props.form.id}result}#` },
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
.var-item {
  padding: 8px;
  border: 1px solid $border-color-light;
  border-radius: 4px;
  background: #fafafa;
  width: 100%;
}
.var-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: $color-text-secondary;
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

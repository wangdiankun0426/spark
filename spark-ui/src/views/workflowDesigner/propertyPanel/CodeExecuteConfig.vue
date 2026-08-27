<template>
  <div class="code-execute-config">
    <el-divider>代码执行配置</el-divider>
    <el-form-item label="代码类型">
      <el-select
          v-model="form.config.codeType"
          placeholder="选择代码类型"
          style="width:100%"
          @change="emit('update')"
          clearable
          size="default"
      >
        <el-option label="SpEL表达式" value="spel" />
        <el-option label="JSON操作" value="json" />
        <el-option label="字符串操作" value="string" />
        <el-option label="数学运算" value="math" />
      </el-select>
    </el-form-item>
    <el-form-item label="代码/表达式">
      <el-input
          v-model="form.config.code"
          type="textarea"
          :rows="8"
          placeholder="输入代码或表达式，支持变量"
          @change="emit('update')"
      />
    </el-form-item>

    <!-- JSON操作参数 -->
    <template v-if="form.config.codeType === 'json'">
      <el-form-item label="JSON操作">
        <el-select
            v-model="form.config.jsonOperation"
            placeholder="选择JSON操作"
            style="width:100%"
            @change="emit('update')"
            clearable
            size="default"
        >
          <el-option label="校验" value="parse" />
          <el-option label="提取字段" value="extract" />
          <el-option label="合并" value="merge" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.config.jsonOperation === 'extract'" label="提取路径">
        <el-input
            clearable
            v-model="form.config.jsonPath"
            placeholder="字段路径，如 data.items"
            @change="emit('update')"
        />
      </el-form-item>
      <el-form-item v-if="form.config.jsonOperation === 'merge'" label="第二个JSON">
        <el-input
            v-model="form.config.json2"
            type="textarea"
            :rows="4"
            placeholder='输入要合并的JSON，如 {"key": "value"}'
            @change="emit('update')"
        />
      </el-form-item>
    </template>

    <!-- 字符串操作参数 -->
    <template v-else-if="form.config.codeType === 'string'">
      <el-form-item label="字符串操作">
        <el-select
            v-model="form.config.stringOperation"
            placeholder="选择字符串操作"
            style="width:100%"
            @change="emit('update')"
            clearable
            size="default"
        >
          <el-option label="长度" value="length" />
          <el-option label="截取" value="substring" />
          <el-option label="替换" value="replace" />
          <el-option label="分割" value="split" />
          <el-option label="去首尾空格" value="trim" />
          <el-option label="转大写" value="uppercase" />
          <el-option label="转小写" value="lowercase" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.config.stringOperation === 'substring'" label="起始位置">
        <el-input-number
            v-model="form.config.start"
            :min="0"
            @change="emit('update')"
        />
      </el-form-item>
      <el-form-item v-if="form.config.stringOperation === 'substring'" label="结束位置">
        <el-input-number
            v-model="form.config.end"
            :min="0"
            @change="emit('update')"
        />
      </el-form-item>
      <template v-if="form.config.stringOperation === 'replace'">
        <el-form-item label="查找字符串">
          <el-input
              clearable
              v-model="form.config.oldStr"
              placeholder="被替换的字符串"
              @change="emit('update')"
          />
        </el-form-item>
        <el-form-item label="替换为">
          <el-input
              clearable
              v-model="form.config.newStr"
              placeholder="替换后的字符串"
              @change="emit('update')"
          />
        </el-form-item>
      </template>
      <el-form-item v-if="form.config.stringOperation === 'split'" label="分隔符">
        <el-input
            clearable
            v-model="form.config.separator"
            placeholder="默认逗号"
            @change="emit('update')"
        />
      </el-form-item>
    </template>

    <!-- 数学运算参数 -->
    <template v-else-if="form.config.codeType === 'math'">
      <el-form-item label="数学运算">
        <el-select
            v-model="form.config.mathOperation"
            placeholder="选择数学运算"
            style="width:100%"
            @change="emit('update')"
            clearable
            size="default"
        >
          <el-option label="表达式求值" value="eval" />
          <el-option label="绝对值" value="abs" />
          <el-option label="四舍五入" value="round" />
          <el-option label="向上取整" value="ceil" />
          <el-option label="向下取整" value="floor" />
          <el-option label="最大值" value="max" />
          <el-option label="最小值" value="min" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.config.mathOperation === 'max' || form.config.mathOperation === 'min'" label="第二个值">
        <el-input
            clearable
            v-model="form.config.val2"
            placeholder="输入第二个数值，支持变量"
            @change="emit('update')"
        />
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
import { ElMessage } from 'element-plus'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])

/** 节点输出变量 */
const outputVar = [
  { label: '执行结果', value: `#{node:${props.form.id}result}#` },
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

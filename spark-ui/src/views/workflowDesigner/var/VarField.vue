<template>
  <div class="var-field">
    <el-input
        ref="inputRef"
        :model-value="modelValue"
        :type="textarea ? 'textarea' : 'text'"
        :rows="textarea ? rows : undefined"
        :placeholder="placeholder"
        :clearable="clearable"
        @update:model-value="val => emit('update:modelValue', val)"
        @change="val => emit('change', val)"
        @focus="focusedOnce = true"
    >
      <template v-if="!textarea" #suffix>
        <el-popover
            v-model:visible="visible"
            trigger="click"
            placement="bottom-end"
            :width="320"
            :show-arrow="false"
        >
          <template #reference>
            <span class="var-field__trigger" title="插入变量">
              <span class="var-field__glyph">{x}</span>
            </span>
          </template>
          <var-picker-panel v-if="visible" :groups="groups" @select="insertVar" />
        </el-popover>
      </template>
    </el-input>
    <div v-if="textarea" class="var-field__bar">
      <el-popover
          v-model:visible="visible"
          trigger="click"
          placement="bottom-end"
          :width="320"
          :show-arrow="false"
      >
        <template #reference>
          <span class="var-field__insert">
            <span class="var-field__glyph">{x}</span> 插入变量
          </span>
        </template>
        <var-picker-panel v-if="visible" :groups="groups" @select="insertVar" />
      </el-popover>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, watch, nextTick, onBeforeUnmount } from 'vue'
import VarPickerPanel from './VarPickerPanel.vue'
import { WF_VARS_KEY } from './flowVariables.js'

const props = defineProps({
  modelValue: { type: String, default: '' },
  textarea: { type: Boolean, default: false },
  rows: { type: Number, default: 4 },
  placeholder: { type: String, default: '' },
  clearable: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'change'])

const groups = inject(WF_VARS_KEY, ref([]))
const inputRef = ref(null)
const visible = ref(false)
// 输入框从未聚焦时 selectionStart 恒为 0，此时追加到末尾而非插到最前
const focusedOnce = ref(false)

/** 在光标处插入变量串，插入后光标移到变量之后并保持面板打开 */
function insertVar(item) {
  const el = props.textarea ? inputRef.value?.textarea : inputRef.value?.input
  const text = props.modelValue || ''
  const start = el && focusedOnce.value ? el.selectionStart : text.length
  const end = el && focusedOnce.value ? el.selectionEnd : text.length
  const token = item.value
  const next = text.slice(0, start) + token + text.slice(end)
  // 程序化编辑不触发原生 change，需双 emit 让画布同步
  emit('update:modelValue', next)
  emit('change', next)
  nextTick(() => {
    if (el) {
      const caret = start + token.length
      el.focus()
      el.setSelectionRange(caret, caret)
    }
  })
}

function onEsc(e) {
  if (e.key === 'Escape') visible.value = false
}

watch(visible, (val) => {
  if (val) {
    document.addEventListener('keydown', onEsc, true)
  } else {
    document.removeEventListener('keydown', onEsc, true)
  }
})

onBeforeUnmount(() => document.removeEventListener('keydown', onEsc, true))
</script>

<style scoped lang="scss">
@use '../styles/vars.scss' as *;

.var-field {
  width: 100%;
}

.var-field__trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: $app-text-placeholder;
  transition: $transition-fast;

  &:hover {
    color: $wf-line-active;
  }
}

.var-field__glyph {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  font-weight: 600;
}

.var-field__bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 4px;
}

.var-field__insert {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: $app-text-secondary;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    color: $wf-line-active;
  }
}
</style>
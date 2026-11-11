<template>
  <div class="py-code" :class="{ 'is-edit': !readonly }">
    <div class="py-code__bar">
      <span class="py-code__dot"></span>
      <span class="py-code__lang">Python</span>
      <span class="py-code__file">main.py</span>
      <span class="py-code__bar-right">
        <span v-if="!readonly" class="py-code__stat">行 {{ cursor.ln }}，列 {{ cursor.col }}</span>
        <span class="py-code__stat">{{ lineCount }} 行</span>
        <slot name="actions" />
      </span>
    </div>

    <div v-if="readonly" class="py-code__view">
      <template v-if="modelValue">
        <div class="py-code__gutter">
          <span v-for="n in lineCount" :key="n" class="py-code__ln">{{ n }}</span>
        </div>
        <pre class="py-code__pre"><code v-html="viewHtml"></code></pre>
      </template>
      <div v-else class="py-code__empty">暂无脚本，点击「编辑」编写 main(params) 函数</div>
    </div>

    <div v-else class="py-code__edit">
      <div ref="gutterRef" class="py-code__gutter">
        <span v-for="n in lineCount" :key="n" class="py-code__ln">{{ n }}</span>
      </div>
      <div class="py-code__area">
        <pre ref="preRef" class="py-code__pre" aria-hidden="true"><code v-html="editHtml"></code></pre>
        <textarea
            ref="taRef"
            class="py-code__ta"
            :value="modelValue"
            wrap="off"
            spellcheck="false"
            autocomplete="off"
            autocorrect="off"
            autocapitalize="off"
            @input="onInput"
            @scroll="syncScroll"
            @keydown="onKeydown"
            @keyup="updateCursor"
            @click="updateCursor"
        ></textarea>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import hljs from '@/utils/highlight'

const props = defineProps({
  modelValue: { type: String, default: '' },
  readonly: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

const taRef = ref(null)
const preRef = ref(null)
const gutterRef = ref(null)
const cursor = ref({ ln: 1, col: 1 })

const lineCount = computed(() => (props.modelValue ? props.modelValue.split('\n').length : 1))

const viewHtml = computed(() => hljs.highlight(props.modelValue || '', { language: 'python', ignoreIllegals: true }).value)

// 编辑态高亮层末尾补一行，与 textarea 光标的隐含空行对齐，避免滚动高度差一截
const editHtml = computed(() => {
  const code = props.modelValue || ''
  const source = code.endsWith('\n') ? code + ' ' : code + '\n'
  return hljs.highlight(source, { language: 'python', ignoreIllegals: true }).value
})

function syncScroll() {
  const ta = taRef.value
  if (!ta) return
  if (preRef.value) {
    preRef.value.scrollTop = ta.scrollTop
    preRef.value.scrollLeft = ta.scrollLeft
  }
  if (gutterRef.value) gutterRef.value.scrollTop = ta.scrollTop
}

function updateCursor() {
  const ta = taRef.value
  if (!ta) return
  const before = ta.value.slice(0, ta.selectionStart)
  cursor.value = { ln: before.split('\n').length, col: ta.selectionStart - before.lastIndexOf('\n') }
}

function onInput(e) {
  emit('update:modelValue', e.target.value)
  nextTick(() => {
    syncScroll()
    updateCursor()
  })
}

function applyValue(value, start, end) {
  emit('update:modelValue', value)
  nextTick(() => {
    const ta = taRef.value
    if (ta) {
      ta.selectionStart = start
      ta.selectionEnd = end
    }
    syncScroll()
    updateCursor()
  })
}

function onKeydown(e) {
  const ta = taRef.value
  if (!ta) return
  if (e.key === 'Tab') {
    e.preventDefault()
    handleTab(ta, e.shiftKey)
  } else if (e.key === 'Enter') {
    e.preventDefault()
    handleEnter(ta)
  }
}

// Tab 光标处插四个空格，选区或 Shift+Tab 按整行缩进/反缩进
function handleTab(ta, outdent) {
  const { selectionStart: s, selectionEnd: e, value } = ta
  if (s === e && !outdent) {
    applyValue(value.slice(0, s) + '    ' + value.slice(s), s + 4, s + 4)
    return
  }
  const ls = value.lastIndexOf('\n', s - 1) + 1
  let le = value.indexOf('\n', e)
  if (le === -1) le = value.length
  const block = value.slice(ls, le)
  const lines = block.split('\n')
  let firstDelta = 0
  const newLines = lines.map((line, i) => {
    if (outdent) {
      const n = (line.match(/^ {1,4}/) || [''])[0].length
      if (i === 0) firstDelta = -n
      return line.slice(n)
    }
    if (i === 0) firstDelta = 4
    return '    ' + line
  })
  const newBlock = newLines.join('\n')
  const delta = newBlock.length - block.length
  const ns = Math.max(ls, s + firstDelta)
  applyValue(value.slice(0, ls) + newBlock + value.slice(le), ns, e + delta)
}

// 回车继承当前行缩进，行尾为冒号时再进一级
function handleEnter(ta) {
  const { selectionStart: s, selectionEnd: e, value } = ta
  const ls = value.lastIndexOf('\n', s - 1) + 1
  const line = value.slice(ls, s)
  const indent = (line.match(/^ */) || [''])[0]
  const extra = /:\s*$/.test(line) ? '    ' : ''
  const insert = '\n' + indent + extra
  const pos = s + insert.length
  applyValue(value.slice(0, s) + insert + value.slice(e), pos, pos)
}

function focus() {
  nextTick(() => {
    const ta = taRef.value
    if (ta) {
      ta.focus()
      updateCursor()
      syncScroll()
    }
  })
}

defineExpose({ focus })
</script>

<style scoped lang="scss">
$code-font: ui-monospace, SFMono-Regular, Menlo, Consolas, 'Courier New', monospace;
$code-fs: 13px;
$code-lh: 22px;
$code-pad-y: 12px;
$code-pad-x: 14px;
$code-line: color-mix(in srgb, #{$code-text} 12%, transparent);

.py-code {
  width: 100%;
  border-radius: $border-radius-md;
  overflow: hidden;
  border: 1px solid $code-line;
  background: $code-bg;
}

.py-code__bar {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 38px;
  padding: 0 12px;
  background: color-mix(in srgb, #{$code-text} 7%, #{$code-bg});
  border-bottom: 1px solid $code-line;
}

.py-code__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: $code-key;
  box-shadow: 0 0 8px color-mix(in srgb, #{$code-key} 70%, transparent);
}

.py-code__lang {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.3px;
  color: $code-text;
}

.py-code__file {
  font-size: 12px;
  font-family: $code-font;
  color: $code-punct;
}

.py-code__bar-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.py-code__stat {
  font-size: 11px;
  font-family: $code-font;
  color: $code-punct;
}

.py-code__bar :deep(.el-button.is-link) {
  height: auto;
  padding: 0;
  font-size: 12px;
  color: $code-key;
}

.py-code__bar :deep(.el-button.is-link:hover) {
  color: $code-text;
}

.py-code__gutter {
  flex-shrink: 0;
  padding: $code-pad-y 0;
  text-align: right;
  user-select: none;
  background: $code-bg;
  border-right: 1px solid $code-line;
}

.py-code__ln {
  display: block;
  height: $code-lh;
  line-height: $code-lh;
  padding: 0 10px 0 14px;
  font-family: $code-font;
  font-size: $code-fs;
  color: $code-lineno;
}

.py-code__pre {
  margin: 0;
  padding: $code-pad-y $code-pad-x;
  font-family: $code-font;
  font-size: $code-fs;
  line-height: $code-lh;
  tab-size: 4;
  white-space: pre;
  color: $code-text;
}

.py-code__pre code {
  font-family: inherit;
}

// 只读态：行号 sticky 钉左，随内容纵向滚动，横向滚动时遮住下方代码
.py-code__view {
  display: flex;
  max-height: 320px;
  overflow: auto;
}

.py-code__view .py-code__gutter {
  position: sticky;
  left: 0;
  z-index: 2;
}

.py-code__view .py-code__pre {
  flex-shrink: 0;
  min-width: 100%;
}

.py-code__empty {
  padding: 22px 16px;
  font-size: 12px;
  color: $code-punct;
}

// 编辑态：高亮层与透明 textarea 同度量叠放，textarea 滚动同步给高亮层与行号
.py-code__edit {
  display: flex;
  height: 440px;
}

.py-code__edit .py-code__gutter {
  overflow: hidden;
}

.py-code__area {
  position: relative;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.py-code__area .py-code__pre {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.py-code__ta {
  position: absolute;
  inset: 0;
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  margin: 0;
  border: 0;
  outline: none;
  resize: none;
  padding: $code-pad-y $code-pad-x;
  font-family: $code-font;
  font-size: $code-fs;
  line-height: $code-lh;
  tab-size: 4;
  white-space: pre;
  overflow: auto;
  background: transparent;
  color: transparent;
  -webkit-text-fill-color: transparent;
  caret-color: $code-text;
}

.py-code__ta::selection {
  background: $code-mark;
}

.py-code :deep(.hljs-keyword),
.py-code :deep(.hljs-built_in),
.py-code :deep(.hljs-title.function_) {
  color: $code-key;
}

.py-code :deep(.hljs-string) {
  color: $code-string;
}

.py-code :deep(.hljs-number),
.py-code :deep(.hljs-symbol) {
  color: $code-number;
}

.py-code :deep(.hljs-literal),
.py-code :deep(.hljs-meta) {
  color: $code-bool;
}

.py-code :deep(.hljs-comment) {
  color: $code-punct;
  font-style: italic;
}

.py-code :deep(.hljs-params) {
  color: $code-text;
}

.py-code :deep(.hljs-title) {
  color: $code-text;
  font-weight: 600;
}

.py-code__view::-webkit-scrollbar,
.py-code__ta::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.py-code__view::-webkit-scrollbar-thumb,
.py-code__ta::-webkit-scrollbar-thumb {
  background: color-mix(in srgb, #{$code-text} 22%, transparent);
  border-radius: 6px;
  border: 2px solid $code-bg;
}

.py-code__view::-webkit-scrollbar-thumb:hover,
.py-code__ta::-webkit-scrollbar-thumb:hover {
  background: color-mix(in srgb, #{$code-text} 34%, transparent);
}

.py-code__view::-webkit-scrollbar-track,
.py-code__ta::-webkit-scrollbar-track,
.py-code__view::-webkit-scrollbar-corner,
.py-code__ta::-webkit-scrollbar-corner {
  background: transparent;
}
</style>

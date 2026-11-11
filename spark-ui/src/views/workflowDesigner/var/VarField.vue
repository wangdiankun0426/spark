<template>
  <div class="var-field">
    <el-popover
        v-if="!textarea"
        v-model:visible="visible"
        trigger="click"
        placement="bottom-start"
        :width="320"
        :show-arrow="false"
    >
      <template #reference>
        <div class="var-select" :class="{ 'is-active': visible }">
          <el-tag v-if="singleLabel" class="var-select__tag" size="small" effect="light">{{ singleLabel }}</el-tag>
          <span v-else-if="modelValue" class="var-select__text">{{ modelValue }}</span>
          <span v-else class="var-select__placeholder">{{ placeholder || '请选择变量' }}</span>
          <el-icon v-if="modelValue && clearable" class="var-select__clear" @click.stop="clearValue"><CircleClose /></el-icon>
          <el-icon v-else class="var-select__arrow"><ArrowDown /></el-icon>
        </div>
      </template>
      <var-picker-panel v-if="visible" :groups="groups" :selected="selectedValues" @select="selectSingle" />
    </el-popover>

    <template v-else>
      <div
          ref="editorRef"
          class="var-editor"
          contenteditable="true"
          :data-placeholder="placeholder"
          :style="{ minHeight: editorMinHeight }"
          @input="onEditorInput"
          @keydown="onEditorKeydown"
          @paste="onEditorPaste"
          @compositionstart="composing = true"
          @compositionend="onCompositionEnd"
      ></div>
      <div class="var-field__bar">
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
          <var-picker-panel v-if="visible" :groups="groups" :selected="selectedValues" @select="insertVar" />
        </el-popover>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, inject, watch, onMounted, onBeforeUnmount } from 'vue'
import VarPickerPanel from './VarPickerPanel.vue'
import { WF_VARS_KEY } from './flowVariables.js'

const props = defineProps({
  modelValue: { type: String, default: '' },
  textarea: { type: Boolean, default: false },
  rows: { type: Number, default: 4 },
  placeholder: { type: String, default: '' },
  clearable: { type: Boolean, default: true }
})
const emit = defineEmits(['update:modelValue', 'change'])

const groups = inject(WF_VARS_KEY, ref([]))
const visible = ref(false)
const editorRef = ref(null)
const composing = ref(false)
let lastValue = ''
let savedRange = null

const VAR_PATTERN = /#\{(?:base|form|formTxt|node):[^}]+\}#/g

/** 变量串到展示名的映射，取自下拉分组目录 */
const varLabelMap = computed(() => {
  const map = {}
  const list = groups.value || []
  list.forEach(g => (g.items || []).forEach(item => { map[item.value] = item.label }))
  return map
})

/** 单行框仅承载单个变量，命中目录展示中文名，否则回退原始键 */
const singleLabel = computed(() => {
  const text = props.modelValue || ''
  if (!/^#\{(?:base|form|formTxt|node):[^}]+\}#$/.test(text)) return ''
  return varLabelMap.value[text] || text.slice(2, -2)
})

/** 当前值已引用的变量串，供下拉面板标记已选 */
const selectedValues = computed(() => {
  const text = props.modelValue || ''
  const list = []
  for (const m of text.matchAll(VAR_PATTERN)) list.push(m[0])
  return list
})

const editorMinHeight = computed(() => `${props.rows * 22 + 16}px`)

/** 单行选中即整值替换并收起下拉 */
function selectSingle(item) {
  emit('update:modelValue', item.value)
  emit('change', item.value)
  visible.value = false
}

function clearValue() {
  emit('update:modelValue', '')
  emit('change', '')
}

/** 构造内联变量标签节点，contenteditable=false 使其作为整体编辑 */
function buildChip(token) {
  const span = document.createElement('span')
  span.className = 'var-chip'
  span.dataset.token = token
  span.contentEditable = 'false'
  span.textContent = varLabelMap.value[token] || token.slice(2, -2)
  return span
}

/** 按变量串切分值，文本与标签节点相间渲染，换行靠 pre-wrap 保留 */
function renderEditor(value) {
  const el = editorRef.value
  if (!el) return
  const text = value || ''
  const frag = document.createDocumentFragment()
  let last = 0
  for (const m of text.matchAll(VAR_PATTERN)) {
    if (m.index > last) frag.appendChild(document.createTextNode(text.slice(last, m.index)))
    frag.appendChild(buildChip(m[0]))
    last = m.index + m[0].length
  }
  if (last < text.length) frag.appendChild(document.createTextNode(text.slice(last)))
  el.replaceChildren(frag)
  lastValue = text
  savedRange = null
}

/** 序列化编辑区：标签节点还原为变量串，零宽空格仅作光标占位需剔除 */
function serializeEditor() {
  const el = editorRef.value
  if (!el) return ''
  let out = ''
  el.childNodes.forEach(node => {
    if (node.nodeType === Node.TEXT_NODE) {
      out += node.textContent
    } else if (node.dataset && node.dataset.token) {
      out += node.dataset.token
    } else if (node.tagName === 'BR') {
      out += '\n'
    } else {
      out += node.textContent
    }
  })
  return out.replace(/\u200B/g, '')
}

function emitValue() {
  const value = serializeEditor()
  lastValue = value
  emit('update:modelValue', value)
  emit('change', value)
}

function onEditorInput() {
  if (composing.value) return
  emitValue()
}

function onCompositionEnd() {
  composing.value = false
  emitValue()
}

/** 光标在编辑区内时持续保存选区，焦点移往下拉面板后仍能按原光标位置插入 */
function onSelectionChange() {
  const el = editorRef.value
  const sel = window.getSelection()
  if (el && sel && sel.rangeCount && el.contains(sel.anchorNode)) {
    savedRange = sel.getRangeAt(0).cloneRange()
  }
}

/** 取当前光标范围，未聚焦时落到末尾 */
function caretRange() {
  const el = editorRef.value
  const sel = window.getSelection()
  if (sel && sel.rangeCount && el.contains(sel.anchorNode)) return sel.getRangeAt(0)
  const range = document.createRange()
  range.selectNodeContents(el)
  range.collapse(false)
  return range
}

function moveCaret(node, offset) {
  const sel = window.getSelection()
  const range = document.createRange()
  range.setStart(node, offset)
  range.collapse(true)
  sel.removeAllRanges()
  sel.addRange(range)
  savedRange = range.cloneRange()
}

/** 多行在光标处插入变量标签，零宽空格垫后保证光标可继续输入 */
function insertVar(item) {
  const el = editorRef.value
  if (!el) return
  el.focus()
  let range
  if (savedRange && el.contains(savedRange.startContainer)) {
    range = savedRange.cloneRange()
  } else {
    range = document.createRange()
    range.selectNodeContents(el)
    range.collapse(false)
  }
  range.deleteContents()
  const chip = buildChip(item.value)
  const spacer = document.createTextNode('\u200B')
  range.insertNode(spacer)
  range.insertNode(chip)
  moveCaret(spacer, 1)
  emitValue()
}

/** 回车强制插入换行符，避免浏览器生成 div 破坏序列化 */
function onEditorKeydown(e) {
  if (composing.value) return
  if (e.key === 'Enter') {
    e.preventDefault()
    insertText('\n')
  }
}

/** 粘贴只取纯文本，杜绝富文本标签混入 */
function onEditorPaste(e) {
  e.preventDefault()
  const text = e.clipboardData ? e.clipboardData.getData('text/plain') : ''
  if (text) insertText(text)
}

function insertText(text) {
  const el = editorRef.value
  if (!el) return
  el.focus()
  const range = caretRange()
  range.deleteContents()
  const node = document.createTextNode(text)
  range.insertNode(node)
  moveCaret(node, text.length)
  emitValue()
}

function onEsc(e) {
  if (e.key === 'Escape') visible.value = false
}

watch(visible, (val) => {
  if (val) document.addEventListener('keydown', onEsc, true)
  else document.removeEventListener('keydown', onEsc, true)
})

watch(() => props.modelValue, (val) => {
  if (composing.value || document.activeElement === editorRef.value) return
  if ((val || '') !== lastValue) renderEditor(val)
})

watch(varLabelMap, () => {
  if (composing.value || document.activeElement === editorRef.value) return
  renderEditor(props.modelValue)
})

onMounted(() => {
  if (props.textarea) {
    renderEditor(props.modelValue)
    document.addEventListener('selectionchange', onSelectionChange)
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onEsc, true)
  document.removeEventListener('selectionchange', onSelectionChange)
})
</script>

<style scoped lang="scss">
.var-field {
  width: 100%;
}

// 单行变量选择器：点击整框开下拉，框内以中文名标签呈现
.var-select {
  box-sizing: border-box;
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  min-height: 32px;
  padding: 0 11px;
  border: 1px solid $border-color;
  border-radius: $border-radius-sm;
  background: $app-bg-card;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    border-color: $el-color-primary;
  }

  &.is-active {
    border-color: $el-color-primary;
  }
}

.var-select__tag {
  max-width: calc(100% - 24px);
}

.var-select__text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  font-size: 13px;
  color: $app-text-primary;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.var-select__placeholder {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  color: $app-text-placeholder;
}

.var-select__clear,
.var-select__arrow {
  flex-shrink: 0;
  margin-left: auto;
  font-size: 14px;
  color: $app-text-placeholder;
}

.var-select__clear {
  cursor: pointer;

  &:hover {
    color: $app-text-secondary;
  }
}

// 多行富文本编辑区：变量内联为标签，回车与粘贴强制纯文本以保证序列化稳定
.var-editor {
  box-sizing: border-box;
  width: 100%;
  padding: 8px 11px;
  border: 1px solid $border-color;
  border-radius: $border-radius-sm;
  background: $app-bg-card;
  font-size: 13px;
  line-height: 22px;
  color: $app-text-primary;
  white-space: pre-wrap;
  word-break: break-word;
  overflow-y: auto;
  cursor: text;
  transition: $transition-fast;

  &:focus {
    border-color: $el-color-primary;
    outline: none;
  }

  &:empty::before {
    content: attr(data-placeholder);
    color: $app-text-placeholder;
  }

  :deep(.var-chip) {
    display: inline-flex;
    align-items: center;
    height: 20px;
    padding: 0 6px;
    margin: 0 2px;
    border-radius: $border-radius-sm;
    background: $app-primary-light;
    color: $app-primary;
    font-size: 12px;
    line-height: 20px;
    vertical-align: middle;
    user-select: none;
  }
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
    color: $el-color-primary;
  }
}

.var-field__glyph {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  font-weight: 600;
}
</style>

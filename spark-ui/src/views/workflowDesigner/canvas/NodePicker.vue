<template>
  <teleport to="body">
    <div v-if="visible" class="node-picker" :style="pickerStyle" @mousedown.stop>
      <div class="picker-search">
        <el-input
            ref="searchRef"
            v-model="keyword"
            placeholder="搜索节点"
            size="default"
            clearable
            @keydown.esc.stop="close"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
      <div class="picker-cats">
        <span
            v-for="cat in categories"
            :key="cat.name"
            class="picker-cat"
            :class="{ 'is-active': activeCat === cat.name }"
            @click="activeCat = cat.name"
        >{{ cat.title }} <i>{{ cat.count }}</i></span>
      </div>
      <div class="picker-list">
        <div
            v-for="type in filteredTypes"
            :key="type"
            class="picker-item"
            draggable="true"
            @dragstart="onDragStart($event, type)"
            @click="pick(type)"
        >
          <span class="picker-ico" :style="{ background: metaOf(type).bgColor, color: metaOf(type).color }">
            <el-icon :size="15"><component :is="metaOf(type).icon" /></el-icon>
          </span>
          <span class="picker-text">
            <b>{{ metaOf(type).name }}</b>
            <em>{{ metaOf(type).desc }}</em>
          </span>
        </div>
        <div v-if="!filteredTypes.length" class="picker-empty">没有匹配的节点</div>
      </div>
      <div class="picker-foot">↑↓ 选择 · Enter 插入 · Esc 关闭 · 可拖到画布任意位置</div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick, onBeforeUnmount } from 'vue'
import { NODE_META, NODE_GROUPS, PICKER_TYPES } from '../nodes/index.js'

defineOptions({ name: 'WorkflowNodePicker' })

const props = defineProps({
  visible: { type: Boolean, default: false },
  /** 弹出位置（浏览器视口坐标） */
  position: { type: Object, default: () => ({ x: 0, y: 0 }) }
})

const emit = defineEmits(['pick', 'close'])

const keyword = ref('')
const activeCat = ref('all')
const searchRef = ref(null)

const categories = computed(() => {
  const cats = [{ name: 'all', title: '全部', count: PICKER_TYPES.length }]
  NODE_GROUPS.forEach(g => {
    const count = PICKER_TYPES.filter(t => NODE_META[t].group === g.name).length
    if (count) cats.push({ ...g, count })
  })
  return cats
})

const filteredTypes = computed(() => {
  let types = PICKER_TYPES
  if (activeCat.value !== 'all') {
    types = types.filter(t => NODE_META[t].group === activeCat.value)
  }
  const kw = keyword.value.trim().toLowerCase()
  if (kw) {
    types = types.filter(t => {
      const meta = NODE_META[t]
      return meta.name.toLowerCase().includes(kw) || (meta.desc || '').toLowerCase().includes(kw)
    })
  }
  return types
})

const metaOf = (type) => NODE_META[type] || {}

const PICKER_W = 620
const PICKER_H = 420

const pickerStyle = computed(() => {
  const x = Math.min(props.position.x, window.innerWidth - PICKER_W - 16)
  const y = Math.min(props.position.y, window.innerHeight - PICKER_H - 16)
  return { left: Math.max(16, x) + 'px', top: Math.max(16, y) + 'px' }
})

watch(() => props.visible, (val) => {
  if (val) {
    keyword.value = ''
    activeCat.value = 'all'
    nextTick(() => searchRef.value?.focus())
    document.addEventListener('mousedown', onOutsideMouseDown, true)
    document.addEventListener('keydown', onGlobalKeydown, true)
  } else {
    removeGlobalListeners()
  }
})

onBeforeUnmount(removeGlobalListeners)

function removeGlobalListeners() {
  document.removeEventListener('mousedown', onOutsideMouseDown, true)
  document.removeEventListener('keydown', onGlobalKeydown, true)
}

function onOutsideMouseDown(event) {
  if (!event.target.closest('.node-picker')) {
    close()
  }
}

function onGlobalKeydown(event) {
  if (event.key === 'Escape') {
    close()
  }
}

function pick(type) {
  emit('pick', type)
  close()
}

function close() {
  emit('close')
}

function onDragStart(event, type) {
  event.dataTransfer.setData('nodeType', type)
  event.dataTransfer.effectAllowed = 'copy'
  close()
}
</script>

<style scoped lang="scss">
@use '../styles/vars.scss' as *;
.node-picker {
  position: fixed;
  z-index: 3000;
  width: $wf-picker-width;
  max-height: 420px;
  display: flex;
  flex-direction: column;
  background: $wf-panel-bg;
  border: 1px solid $wf-panel-border;
  border-radius: 14px;
  box-shadow: 0 12px 40px rgba(31, 45, 74, 0.2);
  overflow: hidden;
}

.picker-search {
  padding: 12px 14px 8px;
}

.picker-cats {
  display: flex;
  gap: 6px;
  padding: 0 14px 10px;
  flex-wrap: wrap;
}

.picker-cat {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  color: $app-text-secondary;
  background: $wf-sec-bg;
  cursor: pointer;
  user-select: none;

  i {
    font-style: normal;
    margin-left: 2px;
    font-size: 11px;
    opacity: 0.7;
  }

  &.is-active {
    background: $wf-cat-active-bg;
    color: $wf-cat-active-text;
  }
}

.picker-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 2px 14px 12px;
  align-content: start;
}

.picker-item {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 8px 10px;
  border: 1px solid $wf-panel-border;
  border-radius: 10px;
  cursor: pointer;
  transition: border-color 0.12s, box-shadow 0.12s;

  &:hover {
    border-color: $wf-line-active;
    box-shadow: 0 2px 8px rgba(47, 123, 255, 0.12);
  }
}

.picker-ico {
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.picker-text {
  min-width: 0;
  display: flex;
  flex-direction: column;

  b {
    font-size: 12.5px;
    font-weight: 600;
    color: $app-text-primary;
    line-height: 17px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  em {
    font-style: normal;
    font-size: 10.5px;
    color: $wf-node-summary;
    line-height: 14px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.picker-empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 24px 0;
  font-size: 12px;
  color: $app-text-placeholder;
}

.picker-foot {
  flex-shrink: 0;
  padding: 7px 14px;
  border-top: 1px solid $wf-panel-border;
  font-size: 11px;
  color: $app-text-placeholder;
}
</style>

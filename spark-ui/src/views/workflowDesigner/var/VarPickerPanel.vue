<template>
  <div class="var-picker">
    <div class="var-picker__search">
      <el-input
          ref="searchRef"
          v-model="keyword"
          placeholder="搜索变量"
          size="default"
          clearable
          @keydown.enter="insertFirst"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>
    <div class="var-picker__tabs">
      <span
          v-for="tab in TABS"
          :key="tab.type"
          class="var-tab"
          :class="{ 'is-active': activeType === tab.type }"
          @click="activeType = tab.type"
      >{{ tab.title }}</span>
    </div>
    <div class="var-picker__list">
      <template v-if="filteredGroups.length">
        <div
            v-for="g in filteredGroups"
            :key="g.type + (g.nodeId || '') + g.title"
            class="var-group"
        >
          <div v-if="g.type === 'node'" class="var-group__head">
            <span class="var-group__dot" :style="{ background: g.color }"></span>
            <span class="var-group__title">{{ g.title }}</span>
          </div>
          <div
              v-for="item in g.items"
              :key="item.value"
              class="var-item"
              @click="emit('select', item)"
          >
            <span class="var-item__label">{{ item.label }}</span>
            <code class="var-item__value" v-text="item.value"></code>
          </div>
        </div>
      </template>
      <div v-else class="var-picker__empty">
        {{ hasAnyVar ? '没有匹配的变量' : '暂无可引用变量，添加节点或绑定表单后可引用' }}
      </div>
    </div>
    <div class="var-picker__foot">Enter 插入首项 · Esc 关闭</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'

defineOptions({ name: 'VarPickerPanel' })

const props = defineProps({
  groups: { type: Array, default: () => [] }
})
const emit = defineEmits(['select'])

// 页签固定展示，无变量时不隐藏
const TABS = [
  { type: 'form', title: '表单值' },
  { type: 'formTxt', title: '显示值' },
  { type: 'node', title: '节点数据' }
]

const keyword = ref('')
const activeType = ref('form')
const searchRef = ref(null)

onMounted(() => nextTick(() => searchRef.value?.focus()))

const filteredGroups = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return props.groups
      .filter(g => g.type === activeType.value)
      .map(g => {
        const items = kw
            ? g.items.filter(item =>
                item.label.toLowerCase().includes(kw) ||
                item.value.toLowerCase().includes(kw) ||
                g.title.toLowerCase().includes(kw))
            : g.items
        return { ...g, items }
      })
      .filter(g => g.items.length)
})

const hasAnyVar = computed(() => props.groups.some(g => g.items.length))

const firstMatch = computed(() => {
  for (const g of filteredGroups.value) {
    if (g.items.length) return g.items[0]
  }
  return null
})

function insertFirst() {
  if (firstMatch.value) emit('select', firstMatch.value)
}
</script>

<style scoped lang="scss">
.var-picker {
  display: flex;
  flex-direction: column;
}

.var-picker__search {
  padding-bottom: 8px;
}

.var-picker__tabs {
  display: flex;
  gap: 4px;
  padding-bottom: 8px;
}

.var-tab {
  flex: 1;
  padding: 5px 0;
  border-radius: $border-radius-sm;
  font-size: 12px;
  text-align: center;
  color: $app-text-secondary;
  background: $app-bg-sub;
  cursor: pointer;
  user-select: none;
  transition: $transition-fast;

  &:hover {
    color: $app-primary;
  }

  &.is-active {
    background: $app-primary-light;
    color: $app-primary;
    font-weight: 600;
  }
}

.var-picker__list {
  max-height: 300px;
  overflow-y: auto;
  margin: 0 -4px;
  padding: 0 4px;
}

.var-group {
  margin-bottom: 6px;
}

.var-group__head {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 6px;
}

.var-group__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.var-group__title {
  font-size: 12px;
  font-weight: 600;
  color: $app-text-secondary;
}

.var-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 6px 8px;
  border-radius: $border-radius-sm;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    background: $app-bg-sub;
  }
}

.var-item__label {
  font-size: 13px;
  color: $app-text-primary;
  line-height: 18px;
}

.var-item__value {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 11px;
  color: $app-primary;
  word-break: break-all;
  line-height: 16px;
}

.var-picker__empty {
  text-align: center;
  padding: 24px 8px;
  font-size: 12px;
  color: $app-text-placeholder;
  line-height: 1.6;
}

.var-picker__foot {
  flex-shrink: 0;
  margin-top: 8px;
  padding-top: 7px;
  border-top: 1px solid $border-color-light;
  font-size: 11px;
  color: $app-text-placeholder;
}
</style>

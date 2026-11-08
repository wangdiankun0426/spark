<template>
  <teleport to="body">
    <div
        v-if="visible"
        ref="menuRef"
        class="node-ctx"
        :style="menuStyle"
        @mousedown.stop
        @contextmenu.prevent
    >
      <div class="nc-head">{{ headText }}</div>

      <div
          v-for="item in insertable"
          :key="item.type"
          class="nc-item"
          @click="$emit('pick', item.type)"
      >
        <span class="nc-ico" :style="{ background: item.bgColor, color: item.color }">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
        <span class="nc-label">{{ item.name }}</span>
        <span class="nc-hint">插入</span>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

defineOptions({ name: 'NodeContextMenu' })

const props = defineProps({
  visible: { type: Boolean, default: false },
  /** 鼠标视口坐标 */
  x: { type: Number, default: 0 },
  y: { type: Number, default: 0 },
  /** 当前右键的节点 */
  node: { type: Object, default: null },
  /** 可插入的节点类型 [{ type, name, icon, color, bgColor }] */
  insertable: { type: Array, default: () => [] }
})

const emit = defineEmits(['pick', 'update:visible'])

const menuRef = ref(null)
const offsetX = ref(0)
const offsetY = ref(0)

/** 菜单标题随右键节点变化 */
const headText = computed(() => {
  if (!props.node) return '添加节点'
  if (props.node.type === 'startEvent') return '在「开始」后添加'
  if (props.node.type === 'exclusiveGateway') return '为「条件分支」新增一条分支'
  if (props.node.type === 'endEvent') return '在「结束」前添加'
  return `在「${props.node.name || '节点'}」后添加`
})

/** 视口边界内定位：右侧/下方放不下时整体回退 */
const menuStyle = computed(() => ({
  left: offsetX.value + 'px',
  top: offsetY.value + 'px'
}))

/** 渲染后测量菜单尺寸并修正位置，避免被视口裁切 */
const placeMenu = async () => {
  offsetX.value = props.x
  offsetY.value = props.y
  await nextTick()
  const el = menuRef.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  const maxX = window.innerWidth - rect.width - 8
  const maxY = window.innerHeight - rect.height - 8
  offsetX.value = Math.max(8, Math.min(props.x, maxX))
  offsetY.value = Math.max(8, Math.min(props.y, maxY))
}

watch(() => props.visible, (visible) => {
  if (visible) placeMenu()
}, { immediate: true })

/** 点击菜单外部或滚动时关闭 */
const onDocMouseDown = (event) => {
  if (!props.visible) return
  if (menuRef.value && menuRef.value.contains(event.target)) return
  emit('update:visible', false)
}

onMounted(() => {
  document.addEventListener('mousedown', onDocMouseDown, true)
  window.addEventListener('scroll', () => props.visible && emit('update:visible', false), true)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', onDocMouseDown, true)
})
</script>

<style scoped lang="scss">
.node-ctx {
  position: fixed;
  z-index: 3000;
  width: 208px;
  padding: 6px;
  box-sizing: border-box;
  background: $app-bg-card;
  border: 1px solid $border-color;
  border-radius: 10px;
  box-shadow: 0 12px 32px rgba(15, 34, 66, 0.16);
  user-select: none;
}

.nc-head {
  padding: 4px 8px 8px;
  font-size: 11px;
  color: $app-text-placeholder;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nc-item {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 32px;
  padding: 0 8px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: $app-text-primary;

  &:hover {
    background: $app-bg-sub;
  }
}

.nc-ico {
  width: 20px;
  height: 20px;
  border-radius: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
}

.nc-label {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nc-hint {
  font-size: 11px;
  color: $app-text-placeholder;
  white-space: nowrap;
}
</style>

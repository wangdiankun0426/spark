<template>
  <div
      class="node-card"
      :class="{ 'is-selected': selected, 'is-disabled': node.disabled, 'is-placeholder': isPlaceholder }"
      :style="{ '--nc': meta.color, '--nc-bg': meta.bgColor }"
  >
    <span class="node-ico">
      <el-icon :size="16"><component :is="meta.icon" /></el-icon>
    </span>
    <span class="node-text">
      <b>{{ node.name || meta.name }}</b>
      <em>{{ summary }}</em>
    </span>
    <el-icon v-if="node.disabled" class="node-disabled-mark" :size="13"><VideoPause /></el-icon>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { NODE_META, getNodeSummary } from './index.js'

defineOptions({ name: 'WorkflowNodeCard' })

const props = defineProps({
  node: { type: Object, required: true },
  selected: { type: Boolean, default: false }
})

const meta = computed(() => NODE_META[props.node.type] || {})
const summary = computed(() => getNodeSummary(props.node))
/** 分支占位节点：虚线空卡，点击后选择真实节点类型 */
const isPlaceholder = computed(() => props.node.type === 'placeholder')
</script>

<style scoped lang="scss">
@use '../styles/vars.scss' as *;
.node-card {
  position: relative;
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 12px;
  border: 1.5px solid $wf-node-border;
  border-radius: $wf-node-radius;
  background: $app-bg-card;
  box-shadow: 0 1px 4px rgba(31, 45, 74, 0.06);
  transition: box-shadow 0.15s;

  // 选中：把原有描边加深到节点自身的类型色，并加一圈同色微光（与流程设计器一致）
  &.is-selected {
    border-width: 2.5px;
    border-color: $wf-node-color;
    box-shadow: 0 0 8px $wf-node-glow;
  }

  &.is-disabled {
    border-style: dashed;
    opacity: 0.55;
  }

  /* 分支占位：虚线空卡，提示点击后选择节点 */
  &.is-placeholder {
    border-style: dashed;
    border-color: $wf-line;
    background: $wf-sec-bg;
    box-shadow: none;
    cursor: pointer;

    .node-ico {
      border: 1px dashed $wf-line;
      background: transparent;
      color: $app-text-placeholder;
    }

    .node-text {
      b {
        color: $app-text-secondary;
        font-weight: 500;
      }

      em {
        color: $app-text-placeholder;
      }
    }

    &:hover {
      border-color: $wf-line-active;

      .node-ico {
        border-color: $wf-line-active;
        color: $wf-line-active;
      }
    }
  }
}

.node-ico {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  background: $wf-node-ico-bg;
  color: $wf-node-ico-color;
}

.node-text {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;

  b {
    font-size: 13px;
    font-weight: 600;
    color: $app-text-primary;
    line-height: 18px;
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

.node-disabled-mark {
  position: absolute;
  right: 8px;
  top: 8px;
  color: $app-text-placeholder;
}

html.dark .node-card {
  border-color: $wf-node-border-dark;
}

// 暗色下的选中态同样用类型色，避免被上面的暗色描边覆盖
html.dark .node-card.is-selected {
  border-color: $wf-node-color-dark;
}
</style>

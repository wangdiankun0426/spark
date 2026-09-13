<template>
  <div class="info-card" :class="{ 'is-disabled': disabled }">
    <div class="info-card-head">
      <span class="info-card-icon">
        <el-icon>
          <component :is="icon" />
        </el-icon>
      </span>
      <span class="info-card-title" :title="title">{{ title }}</span>
    </div>
    <div
        class="info-card-desc"
        :title="description"
    >{{ description }}</div>
    <div
        class="info-card-bar"
        v-if="actions.length"
    >
      <template
          v-for="(act, i) in visibleActions"
          :key="act.key || i"
      >
        <span v-if="i > 0" class="info-card-bar-split">|</span>
        <span
            class="info-card-bar-btn"
            :class="{ 'is-disabled': act.disabled || act.loading }"
            @click="handleAction(act)"
        >
          <el-icon
              v-if="act.loading"
              class="info-card-bar-loading">
            <Loading />
          </el-icon>
          <el-icon v-else>
            <component :is="act.icon" />
          </el-icon>
          <span class="info-card-bar-label">{{ act.label }}</span>
        </span>
      </template>

      <!-- 超出一行的按钮折叠进气泡 -->
      <template v-if="hiddenActions.length">
        <span class="info-card-bar-split">|</span>
        <el-popover
            placement="top"
            trigger="hover"
            :show-arrow="false"
            popper-class="info-card-bar-pop"
        >
          <template #reference>
            <span class="info-card-bar-btn info-card-bar-more">···</span>
          </template>

          <div class="info-card-bar-pop-list">
            <template
                v-for="(act, i) in hiddenActions"
                :key="act.key || i"
            >
              <span
                  class="info-card-bar-btn"
                  :class="{ 'is-disabled': act.disabled || act.loading }"
                  @click="handleAction(act)"
              >
                <el-icon v-if="act.loading" class="info-card-bar-loading"><Loading /></el-icon>
                <el-icon v-else><component :is="act.icon" /></el-icon>
                <span class="info-card-bar-label">{{ act.label }}</span>
              </span>
            </template>
          </div>
        </el-popover>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Loading } from '@element-plus/icons-vue'

const MAX_BAR_ACTIONS = 3

const props = defineProps({
  icon: {
    type: [Object, Function],
    required: true
  },
  // 标题
  title: {
    type: String,
    default: ''
  },
  // 备注文案
  description: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  actions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['action'])

// 工具栏直接展示的按钮
const visibleActions = computed(() => props.actions.slice(0, MAX_BAR_ACTIONS))

// 收进「···」气泡的按钮
const hiddenActions = computed(() => props.actions.slice(MAX_BAR_ACTIONS))

/**
 * 触发工具栏按钮
 * @param act 按钮配置项
 */
function handleAction(act) {
  if (act.disabled || act.loading) {
    return
  }
  if (typeof act.onClick === 'function') {
    act.onClick(act)
  }
  emit('action', act)
}
</script>

<style scoped lang="scss">
.info-card {
  display: flex;
  flex-direction: column;
  height: 175px;
  min-height: 175px;
  flex-shrink: 0;
  box-sizing: border-box;
  background-color: $bg-card;
  border: 1px solid $border-color;
  border-radius: $border-radius-md;
  overflow: hidden;
  transition: $transition-normal;
  box-shadow: 0 1px 4px rgba(23, 43, 77, 0.06);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 79, 197, 0.10);
  }
}

.info-card-head {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md $spacing-md 0;
  flex-shrink: 0;
}

.info-card-icon {
  width: 42px;
  height: 42px;
  flex-shrink: 0;
  border-radius: $border-radius-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: $color-primary-light;
  color: $color-primary;
  .el-icon {
    font-size: 30px;
  }
}

.info-card-title {
  flex: 1;
  min-width: 0;
  font-size: 15px;
  font-weight: 600;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.info-card-desc {
  flex: 1;
  min-height: 38px;
  padding: $spacing-sm $spacing-md 0;
  font-size: 12px;
  line-height: 1.6;
  color: $color-text-secondary;
  word-break: break-all;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.info-card-bar {
  display: flex;
  align-items: center;
  margin-top: $spacing-md;
  padding: $spacing-md $spacing-md;
  background-color: #f4f5f7;
  overflow: hidden;
  flex-shrink: 0;
}

.info-card-bar-split {
  flex-shrink: 0;
  margin: 0 $spacing-sm;
  font-size: 12px;
  line-height: 1;
  color: $border-color;
}

.info-card-bar-btn {
  min-width: 0;
  font-size: 12px;
  line-height: 1;
  color: $color-text-secondary;
  cursor: pointer;
  transition: $transition-fast;
  display: inline-flex;
  align-items: center;
  gap: $spacing-xs;

  &:hover {
    color: $color-primary;
  }

  &.is-disabled {
    color: $color-text-placeholder;
    cursor: default;

    &:hover {
      color: $color-text-placeholder;
    }
  }
}

.info-card-bar-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.info-card-bar-more {
  flex-shrink: 0;
  font-weight: 600;
}

.info-card-bar-loading {
  animation: info-card-spin 1s linear infinite;
}

@keyframes info-card-spin {
  to {
    transform: rotate(360deg);
  }
}

:global(.info-card-bar-pop) {
  padding: 0 !important;
  border: 1px solid $border-color;
  box-shadow: 0 4px 16px rgba(0, 79, 197, 0.10);
  border-radius: $border-radius-sm;
}

.info-card-bar-pop-list {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  padding: $spacing-xs 0;
}

.info-card-bar-pop-list .info-card-bar-btn {
  padding: 6px $spacing-sm;
  justify-content: flex-start;
  white-space: nowrap;
  border-radius: 4px;
  gap: 4px;
  &:hover:not(.is-disabled) {
    background-color: $color-primary-light;
    color: $color-primary;
  }
}

:global(.info-card-bar-pop.el-popover) {
  min-width: unset !important;
  padding: 0 !important;
  width: auto !important;
}

.info-card.is-disabled {
  opacity: 0.6;
  cursor: default;

  &:hover {
    transform: none;
    box-shadow: 0 1px 4px rgba(23, 43, 77, 0.06);
  }
}
</style>

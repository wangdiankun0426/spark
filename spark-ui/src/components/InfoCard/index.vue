<template>
  <!--
    通用信息卡片：
    - 通过 slot 暴露 badge / tags / meta / action 四个差异化区域
    - 主题色由 theme prop 控制，复用 variables.scss 中的 agent 主题 token
    - 卡片高度固定（默认 300px），标签区超出时滚动，保证网格整齐
  -->
  <div
      class="info-card"
      :class="[{ 'info-card-disabled': disabled }, 'theme-' + theme]"
      :style="{ height: height + 'px' }"
      @click="handleClick"
  >
    <!-- 顶部：头像 + 名称 + 徽章 -->
    <div class="info-card-top">
      <div class="info-card-avatar">
        <el-icon class="info-card-avatar-icon">
          <component :is="icon" />
        </el-icon>
      </div>
      <div class="info-card-name-row">
        <div class="info-card-name" :title="title">{{ title }}</div>
        <div class="info-card-id">{{ idText }}</div>
      </div>
      <div class="info-card-badge" v-if="$slots.badge">
        <slot name="badge" />
      </div>
    </div>

    <!-- 描述 -->
    <div class="info-card-desc">{{ description }}</div>

    <!-- 标签区：由调用方通过 slot 自定义，超长可滚动 -->
    <div class="info-card-tags" v-if="$slots.tags">
      <slot name="tags" />
    </div>

    <!-- 底部：元信息 + 操作 -->
    <div class="info-card-footer">
      <div class="info-card-meta" v-if="$slots.meta">
        <slot name="meta" />
      </div>
      <div class="info-card-action" v-if="$slots.action">
        <slot name="action" />
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 通用信息卡片
 * 适用于列表式信息展示场景（agent / knowledge / flow 等），统一视觉与结构
 */

const props = defineProps({
  // 主题色：blue / green / purple / orange / cyan / pink / indigo
  theme: {
    type: String,
    default: 'blue'
  },
  // 头像图标组件（Element Plus icon）
  icon: {
    type: [Object, Function],
    required: true
  },
  // 标题
  title: {
    type: String,
    default: ''
  },
  // 编号文案（如 #123）
  idText: {
    type: String,
    default: ''
  },
  // 描述文案
  description: {
    type: String,
    default: ''
  },
  // 是否禁用（已停用 / 已停用等场景）
  disabled: {
    type: Boolean,
    default: false
  },
  // 卡片固定高度（px）
  height: {
    type: Number,
    default: 300
  }
})

const emit = defineEmits(['click'])

function handleClick(e) {
  if (props.disabled) {
    return
  }
  emit('click', e)
}
</script>

<style scoped lang="scss">
.info-card {
  position: relative;
  background-color: $bg-card;
  border-radius: $border-radius-md;
  padding: $spacing-lg;
  cursor: pointer;
  overflow: hidden;
  transition: $transition-normal;
  border: 1px solid $border-color;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  box-shadow: 0 1px 4px rgba(23, 43, 77, 0.06);

  &:hover {
    transform: translateY(-2px);
    border-color: $border-color;
    box-shadow: 0 4px 16px rgba(0, 79, 197, 0.10);
  }
}

.info-card-top {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.info-card-avatar {
  width: 46px;
  height: 46px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: $transition-normal;
  background: var(--info-theme);
}

.info-card-avatar-icon {
  font-size: 24px;
  color: #ffffff;
}

.info-card-name-row {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  min-width: 0;
  flex: 1;
}

.info-card-name {
  font-size: 15px;
  font-weight: 600;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.info-card-id {
  font-size: 12px;
  color: $color-text-placeholder;
  font-weight: 400;
}

.info-card-badge {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.info-card-desc {
  font-size: 13px;
  color: $color-text-secondary;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
}

.info-card-tags {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  gap: $spacing-xs;
  min-height: 24px;
  padding: $spacing-xs 0;
}

.info-card-footer {
  margin-top: auto;
  padding-top: $spacing-md;
  border-top: 1px solid $border-color-light;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  font-size: 12px;
  color: $color-text-secondary;
}

.info-card-meta {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  flex: 1;
  min-width: 0;

  :deep(.el-icon) {
    font-size: 14px;
  }
}

.info-card-action {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  color: var(--info-theme);
}

// ============== slot 内公共样式（穿透 scoped）==============
// 徽章：调用方使用 <span class="badge badge-xxx">
.info-card-badge {
  :deep(.badge) {
    flex-shrink: 0;
    padding: 2px $spacing-sm;
    font-size: 11px;
    font-weight: 600;
    border-radius: $border-radius-sm;
    line-height: 1.4;
    display: inline-flex;
    align-items: center;
  }

  :deep(.badge-primary) {
    color: $color-primary;
    background-color: $color-primary-soft;
  }

  :deep(.badge-muted) {
    color: $color-text-placeholder;
    background-color: $border-color-light;
  }

  :deep(.badge-theme) {
    color: var(--info-theme);
    background-color: var(--info-theme-soft);
  }
}

// 元信息项：调用方使用 <span class="meta-item"><el-icon><.../></el-icon>xxx</span>
.info-card-meta {
  :deep(.meta-item) {
    display: inline-flex;
    align-items: center;
    gap: $spacing-xs;
    min-width: 0;

    span {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

// 操作：调用方使用 <span class="action-item">xxx<el-icon><.../></el-icon></span>
.info-card-action {
  :deep(.action-item) {
    display: inline-flex;
    align-items: center;
    gap: $spacing-xs;
    font-weight: 600;
    transition: $transition-fast;
    cursor: pointer;
  }

  :deep(.action-muted) {
    color: $color-text-placeholder;
    cursor: default;
  }

  // el-button 文本按钮在操作区中跟随主题色
  :deep(.el-button.is-text) {
    color: var(--info-theme);
    padding: 0;

    &:hover {
      color: var(--info-theme);
      background-color: var(--info-theme-soft);
    }
  }
}

// 标签区中的 el-tag 跟随主题色
// 仅覆盖无 type 的默认 tag，保留 success / info / warning / danger 的语义色
.info-card-tags {
  :deep(.el-tag:not(.el-tag--success):not(.el-tag--info):not(.el-tag--warning):not(.el-tag--danger)) {
    --el-tag-bg-color: var(--info-theme-soft);
    --el-tag-text-color: var(--info-theme);
    --el-tag-border-color: transparent;
    border-color: transparent;
    font-weight: 400;
  }

  // 标签分组的小标题（agent 用）
  :deep(.tag-group) {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: $spacing-xs;
    width: 100%;

    .tag-group-label {
      font-size: 12px;
      font-weight: 600;
      color: $color-text-secondary;
      margin-right: $spacing-xs;
    }
  }
}

// 已停用：置灰，禁用 hover 效果
.info-card-disabled {
  cursor: not-allowed;
  opacity: 0.62;

  .info-card-avatar {
    background: $color-text-placeholder;
  }

  &:hover {
    transform: none;
    border-color: $border-color;
    box-shadow: 0 1px 4px rgba(23, 43, 77, 0.06);
  }
}

// ============== 主题色 ==============
// 通过 CSS 变量统一驱动卡片各处主题色（头像 / 标签 / 操作文字）
.theme-blue   { --info-theme: #{$agent-theme-blue};   --info-theme-light: #{lighten($agent-theme-blue, 12%)};   --info-theme-soft: #{$agent-theme-blue-soft}; }
.theme-green  { --info-theme: #{$agent-theme-green};  --info-theme-light: #{lighten($agent-theme-green, 12%)};  --info-theme-soft: #{$agent-theme-green-soft}; }
.theme-purple { --info-theme: #{$agent-theme-purple}; --info-theme-light: #{lighten($agent-theme-purple, 12%)}; --info-theme-soft: #{$agent-theme-purple-soft}; }
.theme-orange { --info-theme: #{$agent-theme-orange}; --info-theme-light: #{lighten($agent-theme-orange, 12%)}; --info-theme-soft: #{$agent-theme-orange-soft}; }
.theme-cyan   { --info-theme: #{$agent-theme-cyan};   --info-theme-light: #{lighten($agent-theme-cyan, 12%)};   --info-theme-soft: #{$agent-theme-cyan-soft}; }
.theme-pink   { --info-theme: #{$agent-theme-pink};   --info-theme-light: #{lighten($agent-theme-pink, 12%)};   --info-theme-soft: #{$agent-theme-pink-soft}; }
.theme-indigo { --info-theme: #{$agent-theme-indigo}; --info-theme-light: #{lighten($agent-theme-indigo, 12%)}; --info-theme-soft: #{$agent-theme-indigo-soft}; }
</style>

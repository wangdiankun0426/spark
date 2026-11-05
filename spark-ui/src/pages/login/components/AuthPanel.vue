<template>
  <!-- 右侧：白色内容区外壳，登录表单与注册表单共用 -->
  <section class="auth-panel">
    <el-tooltip content="返回登录" placement="bottom" :disabled="!showBack">
      <button
          v-if="showBack"
          type="button"
          class="panel-corner panel-corner--left"
          @click="emit('back')"
      >
        <el-icon><ArrowLeft /></el-icon>
      </button>
    </el-tooltip>

    <el-tooltip :content="currentTheme === 'light' ? '深色模式' : '浅色模式'" placement="left">
      <button
          type="button"
          class="panel-corner panel-corner--right"
          :class="{ 'is-desktop': isDesktop }"
          @click="toggleTheme"
      >
        <el-icon><Moon v-if="currentTheme === 'light'" /><Sunny v-else /></el-icon>
      </button>
    </el-tooltip>

    <div class="auth-main">
      <slot/>
    </div>

    <div class="auth-copyright">
      Copyright © 2024-2026 evancloud.top All Rights Reserved.
    </div>
  </section>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import { ArrowLeft, Moon, Sunny } from '@element-plus/icons-vue';
import { getTheme, setTheme } from '@/utils/themeUtil.js';
import { isDesktop } from '@desktop/env.js';

defineProps({
  // 是否展示左上角返回按钮（注册页回到登录页）
  showBack: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['back']);

const currentTheme = ref(getTheme());

/**
 * 切换深浅色主题并持久化，与主界面导航栏共用同一份主题配置
 */
function toggleTheme() {
  currentTheme.value = currentTheme.value === 'light' ? 'dark' : 'light';
  setTheme(currentTheme.value);
}

function syncTheme(e) {
  currentTheme.value = e && e.detail ? e.detail : getTheme();
}

onMounted(() => {
  window.addEventListener('app-theme-change', syncTheme);
  window.addEventListener('storage', syncTheme);
});

onUnmounted(() => {
  window.removeEventListener('app-theme-change', syncTheme);
  window.removeEventListener('storage', syncTheme);
});
</script>

<style lang="scss" scoped>
// 右侧内容区外壳，登录与注册表单共用
.auth-panel {
  position: relative;
  flex: none;
  width: 36.5%;
  min-width: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: $app-bg-card;

  // 与左侧渐变交界处留一条极柔和的过渡带，避免生硬切边
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 88px;
    pointer-events: none;
    background: linear-gradient(90deg, rgba(23, 43, 77, 0.055), transparent);
    -webkit-mask-image: linear-gradient(180deg, transparent, $mask-opaque 22%, $mask-opaque 78%, transparent);
    mask-image: linear-gradient(180deg, transparent, $mask-opaque 22%, $mask-opaque 78%, transparent);
  }
}

// 右上主题切换与左上返回
.panel-corner {
  position: absolute;
  top: clamp(22px, 3.4vh, 34px);
  width: 34px;
  height: 34px;
  padding: 0;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: 1px solid $ds-line;
  background-color: $app-bg-card;
  color: $app-text-secondary;
  font-size: 17px;
  cursor: pointer;
  transition: color 0.18s ease, border-color 0.18s ease, background-color 0.18s ease;

  &:hover {
    color: $ds-brand;
    border-color: $ds-brand;
    background-color: $ds-surface-3;
  }
}

.panel-corner--right {
  right: clamp(24px, 3vw, 40px);
}

// 桌面端右上角有固定窗口控制条，主题按钮下移避让
.panel-corner--right.is-desktop {
  top: calc(#{$nav-height} + 20px);
}

.panel-corner--left {
  left: clamp(24px, 3vw, 40px);
}

.auth-main {
  position: relative;
  z-index: 1;
  width: clamp(348px, 25.6vw, 380px);
  max-width: 90%;
  display: flex;
  flex-direction: column;
}

// 表单通用样式，作用于插槽内容
.auth-main :deep(.auth-title) {
  margin: 0;
  font-size: 25px;
  font-weight: 800;
  letter-spacing: 0.6px;
  color: $color-text-primary;
}

.auth-main :deep(.auth-subtitle) {
  margin: 9px 0 0;
  font-size: 12.5px;
  color: $app-text-secondary;
}

.auth-main :deep(.auth-form) {
  margin-top: 24px;
}

// 填充式输入框，聚焦时卡片底色加品牌描边
.auth-main :deep(.auth-fields) {
  min-height: 186px;
}

.auth-main :deep(.auth-fields .el-form-item) {
  margin-bottom: 18px;
}

.auth-main :deep(.auth-fields .el-input__wrapper) {
  height: 47px;
  padding: 0 14px;
  background-color: $ds-surface-2;
  border-radius: $border-radius-md !important;
  box-shadow: 0 0 0 1px transparent inset !important;
  transition: background-color 0.16s ease, box-shadow 0.16s ease;

  &:hover {
    background-color: $ds-surface-3;
    box-shadow: 0 0 0 1px transparent inset !important;
  }

  &.is-focus {
    background-color: $app-bg-card;
    box-shadow: 0 0 0 1.5px $ds-brand inset !important;
  }
}

.auth-main :deep(.auth-fields .el-input__inner) {
  font-size: 13.5px;
}

.auth-main :deep(.auth-fields .el-input__prefix .el-icon) {
  color: $app-text-placeholder;
  font-size: 17px;
}

// 前置图标与输入文字之间留出呼吸感
.auth-main :deep(.auth-fields .el-input__prefix) {
  margin-right: 9px;
}

// 自动填充时 Chrome 会铺一层浅蓝底，用大范围 inset 阴影覆盖并保持文字主题色
.auth-main :deep(.auth-fields input:-webkit-autofill),
.auth-main :deep(.auth-fields input:-webkit-autofill:hover),
.auth-main :deep(.auth-fields input:-webkit-autofill:focus) {
  -webkit-text-fill-color: $color-text-primary !important;
  caret-color: $color-text-primary;
  -webkit-box-shadow: 0 0 0 1000px $ds-surface-2 inset !important;
  box-shadow: 0 0 0 1000px $ds-surface-2 inset !important;
  transition: background-color 9999s ease-in-out 0s;
}

// 聚焦态输入框底色变卡片色，自动填充覆盖色同步跟随
.auth-main :deep(.auth-fields .el-input__wrapper.is-focus input:-webkit-autofill) {
  -webkit-box-shadow: 0 0 0 1000px $app-bg-card inset !important;
  box-shadow: 0 0 0 1000px $app-bg-card inset !important;
}

.auth-main :deep(.auth-submit-item) {
  width: 100%;
  margin: 22px 0 0;
}

// 渐变底与光晕由皮肤层 --ds-grad / --ds-glow 提供，深浅主题自动切换
.auth-main :deep(.auth-submit-btn) {
  width: 100%;
  height: 47px;
  border: none !important;
  border-radius: $border-radius-md !important;
  font-size: 14.5px;
  font-weight: 700;
  letter-spacing: 4px;
  text-indent: 4px;
  transition: filter 0.16s ease, box-shadow 0.16s ease;

  &:hover,
  &:focus {
    filter: brightness(1.06);
    box-shadow: $ds-glow-strong;
  }
}

// 图形验证码与短信倒计时按钮共用一个右侧槽位
.auth-main :deep(.auth-code) {
  margin-left: 6px;
  width: 32%;
  height: 47px;
}

.auth-main :deep(.auth-code-img) {
  width: 100%;
  height: 100%;
  cursor: pointer;
  vertical-align: middle;
  border-radius: $border-radius-md;
  box-sizing: border-box;
}

.auth-main :deep(.code-send-btn) {
  width: 100%;
  height: 47px !important;
  margin: 0 !important;
  padding: 0;
  border-radius: $border-radius-md !important;
  font-size: 12.5px;
  letter-spacing: 0;
  color: $color-text-white;
}

// 协议勾选
.auth-main :deep(.auth-agreement) {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  margin: 18px 0 0;
  font-size: 12px;
  color: $app-text-secondary;

  .el-checkbox {
    height: auto;
    margin-right: 0;

    .el-checkbox__label {
      padding-left: 6px;
      font-size: 12px;
      color: $app-text-secondary;
    }

    &.is-checked .el-checkbox__label {
      color: $app-text-secondary;
    }
  }

  .el-button {
    height: auto;
    padding: 0;
    font-size: 12px;
  }

  .agreement-and {
    margin: 0 2px;
  }
}

.auth-main :deep(.auth-agreement-error) {
  padding-top: 6px;
  font-size: 12px;
  line-height: 18px;
  text-align: center;
  color: $el-color-danger;
}

// 底部文字入口
.auth-main :deep(.auth-entry) {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.auth-main :deep(.auth-entry-tip) {
  font-size: 12px;
  color: $app-text-secondary;
}

.auth-main :deep(.auth-entry .el-button) {
  height: auto;
  padding: 0;
  font-size: 12px;
  font-weight: 650;
}

// 其他方式登录
.auth-main :deep(.auth-divider) {
  margin: 84px 0 0;

  .el-divider__text {
    padding: 0 12px;
    font-size: 11px;
    background-color: $app-bg-card;
    color: $app-text-secondary;
  }

  .el-divider__text span {
    font-size: 11px;
  }
}

.auth-main :deep(.auth-type-row) {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 20px;
}

.auth-main :deep(.auth-type-btn) {
  width: 40px !important;
  height: 40px !important;
  margin: 0 10px !important;
  padding: 0 !important;
  border-radius: 50% !important;
  border: 1px solid $ds-line !important;
  background-color: $app-bg-card !important;
  color: $app-text-secondary !important;
  transition: color 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;

  .el-icon {
    font-size: 17px;
  }

  &:hover,
  &:focus {
    color: $ds-brand !important;
    border-color: $ds-brand !important;
    box-shadow: $ds-glow;
  }
}

// 版权
.auth-copyright {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
  font-size: 11px;
  color: $app-text-placeholder;
}

// 深色主题
html.dark {
  .auth-panel {
    &::before {
      background: linear-gradient(90deg, rgba(0, 0, 0, 0.28), transparent);
    }
  }

  .auth-main :deep(.auth-agreement) {
    .el-checkbox__label {
      color: $app-text-secondary !important;
    }
  }
}

// 适配
@supports (height: 100dvh) {
  .auth-panel {
    min-height: 100dvh;
  }
}

// 矮屏压缩字段高度与间距，保证 768/800 高度下不溢出
@media (max-height: 860px) {
  .auth-main :deep(.auth-fields) {
    min-height: 170px;

    .el-input__wrapper {
      height: 44px;
    }
  }

  .auth-main :deep(.auth-submit-btn) {
    height: 44px;
  }

  .auth-main :deep(.auth-submit-item) {
    margin-top: 14px;
  }

  .auth-main :deep(.auth-divider) {
    margin-top: 52px;
  }

  .auth-main :deep(.auth-code) {
    height: 44px;
  }
}

@media (max-width: 1120px) {
  .auth-panel {
    min-width: 380px;
  }
}
</style>

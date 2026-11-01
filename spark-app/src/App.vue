<template>
</template>
<script setup>
import {onLaunch, onPageNotFound} from '@dcloudio/uni-app';
import store from '@/store';
import {initNavBarInfo} from '@/utils/navBarUtil';

onLaunch(() => {
  initNavBarInfo();
  // 启动即应用已保存的主题，避免首屏先亮后暗
  store.dispatch('theme/initTheme');
});

// 打开不存在的页面时 兜底跳转 404
onPageNotFound(() => {
  uni.redirectTo({
    url: '/pages/error/notFound',
    fail: () => {
      uni.reLaunch({url: '/pages/error/notFound'});
    }
  });
});
</script>
<style lang="scss">
@import 'node_modules/uview-plus/index.scss';
@import './styles/theme.scss';

.u-tabbar-item__icon {
  width: auto !important;
  min-width: 0;
}

/* 全局字体优化：消除大屏幕失真 */
page,
body,
html {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial,
    sans-serif;
}

/* 隐藏全局滚动条，禁止页面主体滚动（各页面自行管理内部滚动） */
page,
body,
html,
uni-page,
uni-page-body,
uni-page-wrapper,
uni-app {
  -ms-overflow-style: none !important;
  scrollbar-width: none !important;
  overflow: hidden;
}

page::-webkit-scrollbar,
body::-webkit-scrollbar,
html::-webkit-scrollbar,
uni-page::-webkit-scrollbar,
uni-page-body::-webkit-scrollbar,
uni-page-wrapper::-webkit-scrollbar,
uni-app::-webkit-scrollbar {
  display: none !important;
  width: 0 !important;
  height: 0 !important;
}

.safe-area-page {
  box-sizing: border-box;
  min-height: 100vh;
  background-color: var(--app-page-base);
}

app-tabbar {
  display: block;
  flex: none;
  flex-shrink: 0;
}

/* #ifdef H5 */
.uni-input-input:-webkit-autofill,
.uni-input-input:-webkit-autofill:hover,
.uni-input-input:-webkit-autofill:focus,
.uni-input-input:-webkit-autofill:active {
  -webkit-box-shadow: 0 0 0 1000px var(--app-bg-card) inset !important;
  box-shadow: 0 0 0 1000px var(--app-bg-card) inset !important;
  -webkit-text-fill-color: var(--app-text-primary) !important;
  caret-color: var(--app-text-primary);
  transition: background-color 100000s ease-in-out 0s;
}
/* #endif */

/* 全局页面顶部蓝色标题栏 */
.page-header {
  display: flex;
  align-items: center;
  height: 44px;
  padding: 0 16px;
  background-color: $spark-color-header-bg;
  border-bottom: 1rpx solid $spark-color-header-border;
  color: var(--app-brand);
  flex-shrink: 0;

  .page-header-back {
    margin-right: 12px;
    flex-shrink: 0;
  }

  .page-header-title {
    flex: 1;
    font-size: 17px;
    font-style: italic;
    font-weight: 700;
    letter-spacing: 1px;
    /* #ifdef MP-WEIXIN */
    color: var(--app-brand);
    /* #endif */
    /* #ifdef H5 */
    background-image: var(--app-title-gradient);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    /* #endif */
    text-align: center;
  }

  .page-header-extra {
    flex-shrink: 0;
  }
}
</style>

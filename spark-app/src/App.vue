<template>
</template>
<script setup>
</script>
<style lang="scss">
@import 'node_modules/uview-plus/index.scss';

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

/* 全局页面安全区域留白，适配刘海屏/灵动岛/底部手势条 */
/* #ifndef H5 */
.safe-area-page {
  padding-top: 50px !important;
}
/* #endif */

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

/* 全局页面顶部蓝色标题栏 */
.page-header {
  display: flex;
  align-items: center;
  height: 44px;
  padding: 0 16px;
  background-color: #0052cc;
  color: #fff;
  flex-shrink: 0;

  .page-header-back {
    margin-right: 12px;
    flex-shrink: 0;
  }

  .page-header-title {
    flex: 1;
    font-size: 17px;
    font-weight: 600;
    color: #fff;
    text-align: center;
  }

  .page-header-extra {
    flex-shrink: 0;
  }
}
</style>

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
@import './styles/base.scss';
@import './styles/shell.scss';
</style>

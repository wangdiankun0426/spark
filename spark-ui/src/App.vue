<template>
  <!-- 桌面端无边框窗口控制条（浏览器端不渲染） -->
  <desktop-titlebar />
  <!-- 在模板中使用 <router-view> 来显示组件 -->
  <router-view></router-view>
</template>

<script setup>
import {onMounted} from 'vue';
import DesktopTitlebar from '@desktop/titlebar.vue';
import {checkHotUpdate} from '@desktop/update.js';

onMounted(() => {
  // 只有主窗口负责检查更新，子窗口（window-*）与宠物窗口不重复请求、不重复弹窗
  if (window.__TAURI__?.window.getCurrentWindow().label !== 'main') return;
  // 首屏资源加载完再检查，避免和启动时的资源请求抢带宽
  window.setTimeout(() => checkHotUpdate(true), 3000);
});
</script>

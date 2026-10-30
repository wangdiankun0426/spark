<template>
  <!-- 桌面端无边框窗口控制条（浏览器端不渲染） -->
  <desktop-titlebar />
  <!-- 在模板中使用 <router-view> 来显示组件 -->
  <router-view></router-view>
</template>

<script setup>
import DesktopTitlebar from '@/components/DesktopTitlebar/index.vue';
</script>

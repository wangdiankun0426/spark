<!--
  无边框窗口控制条，仅主窗口且未进入客户端外壳时渲染；
  /home 及子路由的标题栏由 shell/topbar.vue 承担，登录、协议、独立预览等页面仍用这条浮层
-->
<template>
  <div v-if="visible" class="dsk-titlebar" data-tauri-drag-region>
    <settings-entry/>
    <span class="dsk-titlebar__divider"></span>
    <window-controls/>
  </div>
</template>

<script setup>
import {computed} from 'vue';
import {useRoute} from 'vue-router';
import {useDesktopWindow} from './useDesktopWindow.js';
import SettingsEntry from './components/settingsEntry.vue';
import WindowControls from './components/windowControls.vue';

const route = useRoute();
const {isMainWindow} = useDesktopWindow();

// /home 及子路由使用客户端独立外壳，不再叠加这条浮层，避免出现两套窗口按钮
const inShell = computed(() => route.path.startsWith('/home'));

const visible = computed(() => isMainWindow.value && !inShell.value);
</script>

<style scoped lang="scss">
.dsk-titlebar {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9999;
  height: 50px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 6px;
  box-sizing: border-box;
}

.dsk-titlebar__divider {
  width: 1px;
  height: 20px;
  margin: 0 4px;
  background: $dsk-line;
  flex: none;
}
</style>

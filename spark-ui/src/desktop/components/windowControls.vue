<!-- 窗口三键仅主窗口渲染，子窗口保留系统标题栏 -->
<template>
  <div v-if="isMainWindow" class="dsk-window-controls">
    <el-tooltip content="最小化" placement="bottom" :show-after="300">
      <div class="dsk-window-controls__btn" @click="minimize">
        <el-icon><Minus/></el-icon>
      </div>
    </el-tooltip>
    <el-tooltip :content="isMaximized ? '向下还原' : '最大化'" placement="bottom" :show-after="300">
      <div class="dsk-window-controls__btn" @click="toggleMaximize">
        <el-icon><CopyDocument v-if="isMaximized"/><FullScreen v-else/></el-icon>
      </div>
    </el-tooltip>
    <el-tooltip content="关闭到托盘" placement="bottom-end" :show-after="300">
      <div class="dsk-window-controls__btn dsk-window-controls__btn--close" @click="hideWindow">
        <el-icon><Close/></el-icon>
      </div>
    </el-tooltip>
  </div>
</template>

<script setup>
import {Minus, Close, FullScreen, CopyDocument} from '@element-plus/icons-vue';
import {useDesktopWindow} from '../useDesktopWindow.js';

const {isMainWindow, isMaximized, minimize, toggleMaximize, hideWindow} = useDesktopWindow();
</script>

<style scoped lang="scss">
.dsk-window-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: none;
}

.dsk-window-controls__btn {
  width: 34px;
  height: 34px;
  border-radius: $border-radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $dsk-text-muted;
  cursor: pointer;
  transition: background-color 0.16s ease, color 0.16s ease;

  .el-icon {
    font-size: 15px;
  }

  &:hover {
    background: $dsk-hover;
    color: $dsk-text;
  }

  &--close:hover {
    background: $dsk-danger-bg;
    color: $dsk-danger;
  }
}
</style>

<template>
  <div v-if="desktopVisible" class="desktop-titlebar is-desktop">
    <div class="desktop-titlebar__drag" data-tauri-drag-region></div>
    <div class="desktop-titlebar__actions">
      <el-popover
          v-model:visible="helpVisible"
          placement="bottom-end"
          :width="118"
          trigger="click"
      >
        <template #reference>
          <div class="window-btn window-btn--tool" title="帮助">
            <el-icon><Question /></el-icon>
          </div>
        </template>
        <div class="setting-panel">
          <div class="setting-panel__item" @click="openAgreement('service')">
            <el-icon><Document /></el-icon>
            <span>用户服务协议</span>
          </div>
          <div class="setting-panel__item" @click="openAgreement('privacy')">
            <el-icon><Lock /></el-icon>
            <span>隐私政策</span>
          </div>
        </div>
      </el-popover>
      <el-popover
          v-model:visible="settingVisible"
          placement="bottom-end"
          :width="118"
          trigger="click"
      >
        <template #reference>
          <div class="window-btn window-btn--tool" title="设置">
            <el-icon><Setting /></el-icon>
          </div>
        </template>
        <div class="setting-panel">
          <div class="setting-panel__item" @click="changeDir">
            <el-icon><FolderOpened /></el-icon>
            <span>修改存储路径</span>
          </div>
          <div class="setting-panel__item" @click="checkUpdate">
            <el-icon><Refresh /></el-icon>
            <span>系统更新</span>
          </div>
        </div>
      </el-popover>
      <div class="window-btn" title="最小化" @click="minimize">
        <el-icon><Minus /></el-icon>
      </div>
      <div class="window-btn" :title="isMaximized ? '还原' : '最大化'" @click="toggleMaximize">
        <el-icon><CopyDocument v-if="isMaximized" /><FullScreen v-else /></el-icon>
      </div>
      <div class="window-btn" :title="isFullscreen ? '退出全屏' : '全屏'" @click="toggleFullscreen">
        <el-icon><Aim /></el-icon>
      </div>
      <div class="window-btn window-btn--close" title="关闭" @click="close">
        <el-icon><Close /></el-icon>
      </div>
    </div>

    <el-dialog v-model="dirDialogVisible" title="修改存储路径" width="520px" append-to-body>
      <el-input v-model="downloadDir" readonly placeholder="点击右侧选择文件夹">
        <template #append>
          <el-button @click="chooseDir">选择文件夹</el-button>
        </template>
      </el-input>
      <div class="dir-tip">下载的文件将自动保存到该目录，同名文件会自动追加序号</div>
      <template #footer>
        <el-button @click="dirDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDir">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {Minus, Close, FullScreen, CopyDocument, Aim, Setting, FolderOpened, Document, Lock, Refresh} from '@element-plus/icons-vue';
import Question from '@/assets/icons/question.vue';
import {ElMessage} from 'element-plus';
import {useDesktopWindow} from '@/utils/desktopWindow';
import {getDownloadDir, pickDownloadDir, saveDownloadDir} from '@/utils/download';

const {desktopVisible, isMaximized, isFullscreen, minimize, toggleMaximize, toggleFullscreen, close} = useDesktopWindow();

const settingVisible = ref(false)
const helpVisible = ref(false)
const dirDialogVisible = ref(false)
const downloadDir = ref('')

function openAgreement(type) {
  helpVisible.value = false
  window.open('/agreement?type=' + type, '_blank')
}

function checkUpdate() {
  settingVisible.value = false
  ElMessage.info('功能开发中')
}

async function changeDir() {
  settingVisible.value = false
  downloadDir.value = await getDownloadDir()
  dirDialogVisible.value = true
}

async function chooseDir() {
  const dir = await pickDownloadDir()
  if (dir) downloadDir.value = dir
}

function confirmDir() {
  if (!downloadDir.value) {
    ElMessage.warning('请先选择文件夹')
    return
  }
  saveDownloadDir(downloadDir.value)
  dirDialogVisible.value = false
  ElMessage.success('存储路径已修改')
}
</script>

<style lang="scss" scoped>
.desktop-titlebar {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9999;
  height: 50px;
  display: flex;
  align-items: stretch;
}

.desktop-titlebar__drag {
  width: 80px;
  height: 100%;
}

.desktop-titlebar__actions {
  display: flex;
  align-items: center;
  padding: 0 6px;
  gap: 2px;
}

.window-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--app-text-secondary);
  cursor: pointer;
  transition: background 0.15s ease;

  .el-icon {
    font-size: 16px;
  }

  &:hover {
    background: var(--app-bg-sub);
    color: var(--app-text-primary);
  }

  &--close:hover {
    background: #fde7e9;
    color: #e81123;
  }

  &--tool {
    margin-right: 6px;
  }

  &--tool .el-icon {
    font-size: 22px;
  }
}

.setting-panel {
  margin: -12px;

  &__item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 6px;
    border-radius: 8px;
    font-size: 13px;
    color: var(--app-text-primary);
    cursor: pointer;

    .el-icon {
      font-size: 16px;
      color: var(--app-text-secondary);
    }

    &:hover {
      background: var(--app-bg-sub);
    }
  }
}

.dir-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--app-text-placeholder);
  line-height: 1.6;
}
</style>

<!-- 帮助/设置入口由浮层控制条与客户端顶栏共用，保证两处行为与弹窗只有一份实现 -->
<template>
  <div class="dsk-settings-entry">
    <el-popover v-model:visible="helpVisible" placement="bottom-end" :width="126" trigger="click">
      <template #reference>
        <!-- popover 绑外层 cell、tooltip 绑内层按钮，避免两个组件争抢同一个元素的 trigger 事件 -->
        <div class="dsk-settings-entry__cell">
          <el-tooltip content="帮助" placement="bottom" :show-after="300">
            <div class="dsk-settings-entry__btn">
              <el-icon><Question/></el-icon>
            </div>
          </el-tooltip>
        </div>
      </template>
      <div class="dsk-settings-entry__panel">
        <div class="dsk-settings-entry__item" @click="openAgreement('service')">
          <el-icon><Document/></el-icon>
          <span>用户服务协议</span>
        </div>
        <div class="dsk-settings-entry__item" @click="openAgreement('privacy')">
          <el-icon><Lock/></el-icon>
          <span>隐私政策</span>
        </div>
      </div>
    </el-popover>

    <el-popover v-model:visible="settingsVisible" placement="bottom-end" :width="132" trigger="click">
      <template #reference>
        <div class="dsk-settings-entry__cell">
          <el-tooltip content="设置" placement="bottom" :show-after="300">
            <div class="dsk-settings-entry__btn">
              <el-icon><Setting/></el-icon>
            </div>
          </el-tooltip>
        </div>
      </template>
      <div class="dsk-settings-entry__panel">
        <div class="dsk-settings-entry__item" @click="changeDir">
          <el-icon><FolderOpened/></el-icon>
          <span>修改存储路径</span>
        </div>
        <div class="dsk-settings-entry__item" @click="checkUpdate">
          <el-icon><Refresh/></el-icon>
          <span>系统更新</span>
        </div>
      </div>
    </el-popover>

    <el-dialog v-model="dirDialogVisible" title="修改存储路径" width="520px" append-to-body>
      <el-input v-model="downloadDir" readonly placeholder="点击右侧选择文件夹">
        <template #append>
          <el-button @click="chooseDir">选择文件夹</el-button>
        </template>
      </el-input>
      <div class="dsk-settings-entry__tip">下载的文件将自动保存到该目录，同名文件会自动追加序号</div>
      <template #footer>
        <el-button @click="dirDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDir">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref} from 'vue';
import {Setting, FolderOpened, Document, Lock, Refresh} from '@element-plus/icons-vue';
import Question from '@/assets/icons/question.vue';
import {ElMessage} from 'element-plus';
import {getDownloadDir, pickDownloadDir, saveDownloadDir} from '../download.js';
import {checkHotUpdate} from '../hotUpdate.js';

const settingsVisible = ref(false);
const helpVisible = ref(false);
const dirDialogVisible = ref(false);
const downloadDir = ref('');

function openAgreement(type) {
  helpVisible.value = false;
  window.open('/agreement?type=' + type, '_blank');
}

function checkUpdate() {
  settingsVisible.value = false;
  checkHotUpdate(false);
}

async function changeDir() {
  settingsVisible.value = false;
  downloadDir.value = await getDownloadDir();
  dirDialogVisible.value = true;
}

async function chooseDir() {
  const dir = await pickDownloadDir();
  if (dir) downloadDir.value = dir;
}

function confirmDir() {
  if (!downloadDir.value) {
    ElMessage.warning('请先选择文件夹');
    return;
  }
  saveDownloadDir(downloadDir.value);
  dirDialogVisible.value = false;
  ElMessage.success('存储路径已修改');
}
</script>

<style scoped lang="scss">
.dsk-settings-entry {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: none;
}

.dsk-settings-entry__cell {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
}

.dsk-settings-entry__btn {
  position: relative;
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
    font-size: 17px;
  }

  &:hover {
    background: $dsk-hover;
    color: $dsk-text;
  }
}

.dsk-settings-entry__panel {
  margin: -12px;
}

.dsk-settings-entry__item {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 9px 7px;
  border-radius: $border-radius-md;
  font-size: 13px;
  color: $dsk-text;
  cursor: pointer;

  .el-icon {
    font-size: 15px;
    color: $dsk-text-muted;
  }

  &:hover {
    background: $dsk-menu-hover;
  }
}

.dsk-settings-entry__tip {
  margin-top: 8px;
  font-size: 12px;
  color: $dsk-text-faint;
  line-height: 1.6;
}
</style>

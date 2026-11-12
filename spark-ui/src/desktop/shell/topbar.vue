<!--
  客户端专属顶栏，工具区顺序与 Web 导航栏一致；
  客户端无模块切换需求故不渲染模块坞，用户信息条下沉到侧栏账号卡，帮助/设置与窗口三键为客户端专属
-->
<template>
  <header class="dsk-topbar" data-tauri-drag-region>
    <div class="dsk-topbar__brand" @click="goHome">
      <img class="dsk-topbar__logo" :src="brandLogo" alt="星火云"/>
      <span class="dsk-topbar__name">星火云<i>AI</i></span>
      <span class="dsk-topbar__ver">v{{ appVersion }}</span>
    </div>

    <div class="dsk-topbar__spacer" data-tauri-drag-region></div>

    <div class="dsk-topbar__tools">
      <el-tooltip content="消息通知" placement="bottom" :show-after="300">
        <div class="dsk-tool" @click="messageVisible = true">
          <el-icon><Bell/></el-icon>
          <span v-if="unreadTotal > 0" class="dsk-tool__badge">{{ unreadTotal > 99 ? '99+' : unreadTotal }}</span>
        </div>
      </el-tooltip>

      <el-tooltip content="切换租户" placement="bottom" :show-after="300">
        <div class="dsk-tool" @click="tenantSwitchVisible = true">
          <el-icon><OfficeBuilding/></el-icon>
        </div>
      </el-tooltip>

      <el-tooltip content="MCP 令牌" placement="bottom" :show-after="300">
        <div class="dsk-tool" @click="mcpTokenVisible = true">
          <el-icon><Key/></el-icon>
        </div>
      </el-tooltip>

      <span class="dsk-topbar__divider"></span>

      <!-- 主题切换直接复用 Web 导航栏的滑动开关，.app-theme-toggle 定义在全局 shell.scss -->
      <div
          class="app-theme-toggle"
          :class="{ 'is-dark': currentTheme === 'dark' }"
          title="切换亮 / 深色主题"
          @click="toggleTheme"
      >
        <span class="m m-sun">
          <el-icon><Sunny/></el-icon>
        </span>
        <span class="m m-moon">
          <el-icon><Moon/></el-icon>
        </span>
        <span class="knob">
          <el-icon><Moon v-if="currentTheme === 'light'"/><Sunny v-else/></el-icon>
        </span>
      </div>

      <el-tooltip content="退出登录" placement="bottom" :show-after="300">
        <div class="dsk-tool dsk-tool--danger" @click="handleLogout">
          <el-icon><SwitchButton/></el-icon>
        </div>
      </el-tooltip>

      <span class="dsk-topbar__divider"></span>

      <settings-entry/>

      <span class="dsk-topbar__divider"></span>

      <window-controls/>
    </div>

    <message-center v-model="messageVisible" @read="loadMessageCount"/>
    <tenant-switch v-model="tenantSwitchVisible" :current-tenant-id="userInfo.currentTenantId"/>
    <mcp-token v-model="mcpTokenVisible"/>
  </header>
</template>

<script setup>
import {ref, computed, onMounted, onBeforeUnmount} from 'vue';
import {useRouter} from 'vue-router';
import {useStore} from 'vuex';
import {Bell, Key, Moon, OfficeBuilding, Sunny, SwitchButton} from '@element-plus/icons-vue';
import {queryMyMessageCountAPI} from '@/api/manage/sys/message.js';
import {getTheme, setTheme} from '@/utils/themeUtil';
import {useLogout} from '@/utils/useLogout.js';
import TenantSwitch from '@/components/TenantSwitch';
import McpToken from '@/components/McpToken';
import MessageCenter from '@/components/MessageCenter';
import brandLogoLight from '@/assets/images/logo-light.png';
import brandLogoDark from '@/assets/images/logo.png';
import SettingsEntry from '../components/settingsEntry.vue';
import WindowControls from '../components/windowControls.vue';

const router = useRouter();
const store = useStore();

const {handleLogout} = useLogout();

const appVersion = __APP_VERSION__;

const userInfo = computed(() => store.getters['user/getUserInfo'] || { id: undefined });

const messageVisible = ref(false);
const tenantSwitchVisible = ref(false);
const mcpTokenVisible = ref(false);

const currentTheme = ref(getTheme());
const brandLogo = computed(() => (currentTheme.value === 'light' ? brandLogoLight : brandLogoDark));

function handleThemeChange(e) {
  currentTheme.value = e.detail;
}

// 与 Web 导航栏同一个开关行为：setTheme 会派发 app-theme-change，这里再同步本地状态
function toggleTheme() {
  currentTheme.value = currentTheme.value === 'light' ? 'dark' : 'light';
  setTheme(currentTheme.value);
}

onMounted(() => {
  window.addEventListener('app-theme-change', handleThemeChange);
  loadMessageCount();
});

onBeforeUnmount(() => {
  window.removeEventListener('app-theme-change', handleThemeChange);
});

const unreadTotal = ref(0);

function loadMessageCount() {
  queryMyMessageCountAPI().then(res => {
    if (res.code === 200 && res.data) {
      unreadTotal.value = res.data.unread || 0;
    }
  }).catch(() => {});
}

function goHome() {
  router.push('/home').catch(() => {});
}
</script>

<style scoped lang="scss">
.dsk-topbar {
  position: relative;
  z-index: 30;
  display: flex;
  align-items: center;
  gap: 10px;
  height: 48px;
  padding: 0 8px 0 14px;
  box-sizing: border-box;
  flex: none;
  background: $dsk-topbar;
  border-bottom: 1px solid $dsk-line;
  color: $dsk-text;
  user-select: none;
}

.dsk-topbar__brand {
  display: flex;
  align-items: center;
  gap: 9px;
  flex: none;
  cursor: pointer;
}

.dsk-topbar__logo {
  width: 26px;
  height: 26px;
  border-radius: 8px;
  object-fit: cover;
  -webkit-user-drag: none;
}

.dsk-topbar__name {
  font-size: 14.5px;
  font-weight: 800;
  letter-spacing: 0.01em;
  color: $dsk-text;
  white-space: nowrap;

  i {
    font-style: normal;
    background: $dsk-brand-grad;
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
  }
}

.dsk-topbar__ver {
  padding: 2px 7px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  line-height: 1.45;
  color: $dsk-text-faint;
  background: $dsk-chip-bg;
  white-space: nowrap;
}

.dsk-topbar__spacer {
  flex: 1 1 auto;
  min-width: 20px;
  align-self: stretch;
}

.dsk-topbar__tools {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: none;
}

.dsk-tool {
  position: relative;
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: $border-radius-md;
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

  &--danger:hover {
    background: $dsk-danger-bg;
    color: $dsk-danger;
  }
}

// 未读角标与 Web 端同款红点，顶栏 48px 下偏移量比 Web 收一档，避免顶到栏边缘
.dsk-tool__badge {
  position: absolute;
  top: -3px;
  right: -6px;
  min-width: 16px;
  height: 15px;
  padding: 0 3.5px;
  border-radius: 8px;
  background: $dsk-badge-grad;
  color: $color-text-white;
  font-size: 9px;
  font-weight: 800;
  line-height: 15px;
  text-align: center;
  box-sizing: border-box;
  z-index: 1;
  box-shadow: 0 0 0 2px $dsk-topbar;
}

.dsk-topbar__divider {
  width: 1px;
  height: 20px;
  margin: 0 4px;
  background: $dsk-line;
  flex: none;
}
</style>

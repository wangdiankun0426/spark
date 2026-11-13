<template>
  <div class="app-nav" data-tauri-drag-region>
    <div class="app-nav__brand" @click="goHome">
      <img class="app-nav__logo" :src="brandLogo" alt="星火云"/>
      <span class="app-nav__name">星火云<i>AI</i></span>
      <span class="app-nav__version">v{{ appVersion }}</span>
    </div>

    <nav v-if="hasModuleMenu" class="app-nav__dock">
      <div
          v-for="mod in dockModules"
          :key="mod.code"
          class="app-tab"
          :class="{ 'is-active': defaultActive === mod.path }"
          @click="switchModule(mod.path)"
      >
        <el-icon><component :is="mod.icon"/></el-icon>
        <span>{{ mod.title }}</span>
      </div>
    </nav>

    <div class="app-nav__spacer"></div>

    <div class="app-nav__actions">
      <el-tooltip content="消息通知" placement="bottom" :show-after="300">
        <div class="app-tool" @click="messageVisible = true">
          <el-icon><Bell/></el-icon>
          <span v-if="unreadTotal > 0" class="app-tool__badge">{{ unreadTotal > 99 ? '99+' : unreadTotal }}</span>
        </div>
      </el-tooltip>
      <el-tooltip content="切换租户" placement="bottom" :show-after="300">
        <div class="app-tool" @click="tenantSwitchVisible = true">
          <el-icon><OfficeBuilding/></el-icon>
        </div>
      </el-tooltip>
      <el-tooltip v-if="checkPermi('llm:mcpKey:query')" content="MCP 令牌" placement="bottom" :show-after="300">
        <div class="app-tool" @click="mcpTokenVisible = true">
          <el-icon><Key/></el-icon>
        </div>
      </el-tooltip>
    </div>

    <span class="app-nav__divider"></span>

    <div
        class="app-theme-toggle"
        :class="{ 'is-dark': currentTheme === 'dark' }"
        title="切换亮 / 深色主题"
        @click="toggleTheme"
    >
      <span class="m m-sun"><el-icon><Sunny/></el-icon></span>
      <span class="m m-moon"><el-icon><Moon/></el-icon></span>
      <span class="knob"><el-icon><Moon v-if="currentTheme === 'light'"/><Sunny v-else/></el-icon></span>
    </div>

    <el-tooltip content="退出登录" placement="bottom" :show-after="300">
      <div class="app-tool app-tool--danger" @click="handleLogout">
        <el-icon><SwitchButton/></el-icon>
      </div>
    </el-tooltip>

    <div class="app-nav__user" @click="userInfoVisible = true">
      <span class="app-nav__avatar-wrap">
        <user-avatar :user-id="userInfo.id" :size="32"/>
        <span class="app-nav__online"></span>
      </span>
      <span class="app-nav__uinfo">
        <span class="app-nav__uname">{{ userInfo.name || '用户' }}</span>
        <span class="app-nav__ulogin">{{ userInfo.loginName || userInfo.account || '' }}</span>
      </span>
    </div>
  </div>

  <user-profile v-model="userInfoVisible" @logout="handleLogout" />
  <tenant-switch v-model="tenantSwitchVisible" :current-tenant-id="userInfo.currentTenantId" />
  <mcp-token v-model="mcpTokenVisible" />
  <!-- 消息中心与桌面客户端共用同一份实现，避免两端各自维护列表与跳转逻辑 -->
  <message-center v-model="messageVisible" @read="loadMessageCount" />
</template>

<script setup>
import {queryMyMessageCountAPI} from "@/api/manage/sys/message.js";
import {ref, computed, onMounted} from "vue";
import {
  Bell,
  Key,
  Moon,
  OfficeBuilding,
  Sunny,
  SwitchButton
} from "@element-plus/icons-vue";
import { useRouter, useRoute } from 'vue-router';
import {useStore} from "vuex";
import {getTheme, applyTheme, setTheme} from '@/utils/themeUtil';
import {resolveIcon} from '@/utils/iconRegistry.js';
import {useLogout} from '@/utils/useLogout.js';
import {checkPermi} from '@/utils/permissionUtil';
import UserAvatar from '@/components/UserAvatar';
import UserProfile from '@/components/UserProfile';
import TenantSwitch from '@/components/TenantSwitch';
import McpToken from '@/components/McpToken';
import MessageCenter from '@/components/MessageCenter';
import brandLogoLight from '@/assets/images/logo-light.png';
import brandLogoDark from '@/assets/images/logo.png';
import { isDesktop } from '@desktop/env.js';
const router = useRouter();
const route = useRoute();
const store = useStore()

const userInfo = computed(() => store.getters['user/getUserInfo'] || { id: undefined })

// 模块坞由后端菜单树驱动，首页之外的模块即坞入口
const dockModules = computed(() => {
  const menuTree = store.getters['permission/menuTree'] || []
  return menuTree
      .filter(node => node.path && node.code !== 'module.home')
      .map(node => ({ code: node.code, path: node.path, title: node.title, icon: resolveIcon(node.icon) }))
})

// 桌面客户端面向普通用户，不提供管理模块入口，整个模块坞不渲染
const hasModuleMenu = computed(() => !isDesktop && dockModules.value.length > 0)

const appVersion = __APP_VERSION__

const currentTheme = ref(getTheme());
applyTheme(currentTheme.value);

// 品牌图标跟随主题色系，与登录页保持一致：浅色用浅底淡色图标，深色用深蓝渐变图标
const brandLogo = computed(() => (currentTheme.value === 'light' ? brandLogoLight : brandLogoDark));

/**
 * 点击品牌区回到默认落地页，站点管理员无首页时落到站点管理
 */
function goHome() {
  const defaultPath = store.getters['permission/defaultPath'];
  if (defaultPath) {
    router.push(defaultPath).catch(() => {});
  }
}

/**
 * 点击按钮在深色/浅色主题间切换并持久化
 */
function toggleTheme() {
  currentTheme.value = currentTheme.value === 'light' ? 'dark' : 'light';
  setTheme(currentTheme.value);
}

const userInfoVisible = ref(false);
const tenantSwitchVisible = ref(false);
const mcpTokenVisible = ref(false);

// 顶部「消息」角标：进入应用时拉一次未读总数
const unreadTotal = ref(0);

/**
 * 拉取我的消息分类计数，用于顶部角标与弹窗页签
 */
function loadMessageCount() {
  queryMyMessageCountAPI().then(res => {
    if (res.code === 200 && res.data) {
      unreadTotal.value = res.data.unread || 0;
    }
  }).catch(() => {});
}

onMounted(loadMessageCount);

/**
 * 切换模块
 * @param path 模块首页路径
 */
function switchModule(path) {
  router.push(path).catch(() => {});
}

const {handleLogout} = useLogout();

const defaultActive = computed(() => {
  const path = route.path.match(/\/([^\/]+)\//);
  if (path !== null) {
    return '/'+path[1];
  }
  return '/home';
})

const messageVisible = ref(false);
</script>

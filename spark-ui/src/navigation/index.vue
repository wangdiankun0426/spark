<template>
  <div>
    <div class="nav-bar" data-tauri-drag-region>
      <div class="logo-box" data-tauri-drag-region="false">
        <img class="logo-lockup" :src="currentTheme === 'light' ? logoLockupDark : logoLockupLight" @click="$router.push('/home')"/>
        <span class="logo-version">v{{ appVersion }}</span>
      </div>

      <div
          class="user-dock"
          :class="{ 'is-open': dockOpen }"
          @mouseenter="dockOpen = true"
          @mouseleave="dockOpen = false"
      >
        <div class="user-dock__trigger" data-tauri-drag-region="false" title="个人中心" @click="openUserInfoForm">
          <user-avatar :user-id="userInfo.id" :size="38" />
        </div>
        <div class="user-dock__items" data-tauri-drag-region="false">
          <div class="dock-group">
            <div
                v-if="hasMenu(MODULE_CODES.LLM)"
                class="dock-btn"
                :class="{ 'is-active': defaultActive === '/llm' }"
                @click="switchModule('/llm')"
            >
              <el-icon><Help /></el-icon>
              <span>AI</span>
            </div>
            <div
                v-if="hasMenu(MODULE_CODES.BUS)"
                class="dock-btn"
                :class="{ 'is-active': defaultActive === '/bus' }"
                @click="switchModule('/bus')"
            >
              <el-icon><SuitcaseLine /></el-icon>
              <span>业务</span>
            </div>
            <div
                v-if="isOrgAdmin()"
                class="dock-btn"
                :class="{ 'is-active': defaultActive === '/org' }"
                @click="switchModule('/org')"
            >
              <el-icon><User /></el-icon>
              <span>组织</span>
            </div>
            <div
                v-if="isSysAdmin()"
                class="dock-btn"
                :class="{ 'is-active': defaultActive === '/site' }"
                @click="switchModule('/site')"
            >
              <el-icon><Setting /></el-icon>
              <span>站点</span>
            </div>
          </div>
          <div class="dock-divider"></div>
          <div class="dock-group">
            <div class="dock-btn" @click="openMessageDialog">
              <el-icon><Bell /></el-icon>
              <span>消息</span>
            </div>
            <div class="dock-btn" @click="toggleTheme">
              <el-icon><Sunny v-if="currentTheme === 'light'" /><Moon v-else /></el-icon>
              <span>主题</span>
            </div>
            <div class="dock-btn" @click="handleLocaleChange">
              <el-icon><Globe /></el-icon>
              <span>语言</span>
            </div>
            <div v-if="!isDesktop" class="dock-btn" @click="toggleFullscreen">
              <el-icon><Aim v-if="isFullscreen" /><FullScreen v-else /></el-icon>
              <span>全屏</span>
            </div>
            <div class="dock-btn" @click="openTenantSwitch">
              <el-icon><OfficeBuilding /></el-icon>
              <span>租户</span>
            </div>
            <div class="dock-btn" @click="openMcpToken">
              <el-icon><Key /></el-icon>
              <span>MCP</span>
            </div>
            <div class="dock-btn dock-btn--danger" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!--个人中心弹窗-->
    <user-profile v-model="userInfoVisible" />

    <!--切换租户抽屉-->
    <tenant-switch v-model="tenantSwitchVisible" :current-tenant-id="userInfo.currentTenantId" />

    <!--MCP token 维护弹窗-->
    <mcp-token v-model="mcpTokenVisible" />

    <!--消息通知弹窗-->
    <el-dialog
        v-model="messageVisible"
        title="消息通知"
        width="760px"
        top="8vh"
        class="nav-message-dialog"
        @open="loadNavMessageData"
    >
      <div class="nav-message-body" ref="messageBodyRef">
        <el-timeline v-if="messageList.length">
          <el-timeline-item
              v-for="(item, i) in messageList"
              :key="i"
              :timestamp="item.createdDt"
              placement="top"
          >
            <div
                class="nav-message-item"
                :class="{ 'is-link': isClickableMessage(item.refId) }"
                @click="handleMessageClick(item)"
            >
              <el-tag type="info" size="small">
                {{ item.title }}
              </el-tag>
              <p class="nav-message-content">{{ item.content }}</p>
              <div v-if="isClickableMessage(item.refId)" class="nav-message-detail">
                <span>详情</span>
                <el-icon><Right /></el-icon>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无消息" :image-size="60"/>
      </div>
      <template #footer>
        <el-pagination
            v-if="messageTotal > 0"
            small
            background
            layout="total, prev, pager, next"
            :current-page="messageQuery.pageNo"
            :page-size="messageQuery.pageSize"
            :total="messageTotal"
            @current-change="handleMessagePageChange"
        />
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {logoutAPI} from "@/api/manage/auth/login.js";
import {pageMyMessageListAPI} from "@/api/manage/sys/message.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {ref, computed, onMounted, onUnmounted} from "vue";
import {Setting, Moon, Sunny, Right, User, Help, SuitcaseLine, OfficeBuilding, SwitchButton, Key, Aim, FullScreen} from "@element-plus/icons-vue";
import {isDesktop} from '@/utils/desktop.js';
import { useRouter, useRoute } from 'vue-router';
import {useStore} from "vuex";
import {getTheme, applyTheme, setTheme} from '@/utils/themeUtil';
import {hasMenu, MODULE_CODES} from '@/utils/menuUtil.js';
import UserAvatar from '@/components/UserAvatar';
import UserProfile from '@/components/UserProfile';
import TenantSwitch from '@/components/TenantSwitch';
import McpToken from '@/components/McpToken';
import Bell from "@/assets/icons/bell.vue";
import Globe from "@/assets/icons/globe.vue";
import logoLockupDark from '@/assets/images/logo-lockup-dark.png';
import logoLockupLight from '@/assets/images/logo-lockup-light.png';
import {isOrgAdmin, isSysAdmin} from "@/utils/permissionUtil.js";
const router = useRouter();
const route = useRoute();
const store = useStore()

const userInfo = computed(() => store.getters['user/getUserInfo'] || { id: undefined })

const appVersion = __APP_VERSION__

const currentTheme = ref(getTheme());
applyTheme(currentTheme.value);

const dockOpen = ref(false);

/**
 * 点击按钮在深色/浅色主题间切换并持久化
 */
function toggleTheme() {
  currentTheme.value = currentTheme.value === 'light' ? 'dark' : 'light';
  setTheme(currentTheme.value);
}

const isFullscreen = ref(false);

/**
 * 切换全屏与退出全屏
 */
function toggleFullscreen() {
  if (document.fullscreenElement) {
    document.exitFullscreen().catch(() => {});
    return;
  }
  document.documentElement.requestFullscreen().catch(() => {
    ElMessage.warning('当前环境不支持全屏');
  });
}

/**
 * 全屏状态变化时同步图标，兼容 Esc、F11 等外部方式进出全屏
 */
function onFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement;
}

onMounted(() => {
  document.addEventListener('fullscreenchange', onFullscreenChange);
});

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', onFullscreenChange);
});

/**
 * 切换语言，语言包尚未接入
 */
function handleLocaleChange() {
  ElMessage.info('功能开发中');
}

const userInfoVisible = ref(false);

const tenantSwitchVisible = ref(false);

const mcpTokenVisible = ref(false);

/**
 * 打开 MCP token 维护弹窗
 */
function openMcpToken() {
  mcpTokenVisible.value = true;
}

/**
 * 打开切换租户抽屉
 */
function openTenantSwitch() {
  tenantSwitchVisible.value = true;
}

/**
 * 打开消息通知弹窗
 */
function openMessageDialog() {
  messageVisible.value = true;
}

/**
 * 切换模块
 * @param path 模块首页路径
 */
function switchModule(path) {
  router.push(path).catch(() => {});
}

/**
 * 打开个人中心弹窗
 */
function openUserInfoForm() {
  userInfoVisible.value = true;
}

/**
 * 登出系统
 */
function handleLogout() {
  ElMessageBox.confirm(
      '是否确定退出系统?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    logoutAPI().then(res => {
      store.dispatch('user/logout');
      router.push({ path: "/login" }).catch(()=>{});
    })
  }).catch(() => {
  })
}

/**
 * 初始化路径
 */
const defaultActive = computed(() => {
  //监听路由，控制菜单选择
  const path = route.path.match(/\/([^\/]+)\//);
  if (path !== null) {
    return '/'+path[1];
  }
  return '/home';
})

const messageVisible = ref(false);
const messageList = ref([]);

const messageQuery = ref({ pageNo: 1, pageSize: 8 });
const messageTotal = ref(0);
const messageBodyRef = ref(null);
let messageRequestMark = 0;

/**
 * 按分页参数加载我的消息
 */
function loadMessageList() {
  const requestMark = ++messageRequestMark;
  pageMyMessageListAPI(messageQuery.value).then(res => {
    // 快速翻页时丢弃过期响应 避免旧数据覆盖新数据
    if (requestMark !== messageRequestMark) {
      return;
    }
    if (res.code !== 200 || !res.data) {
      messageList.value = [];
      messageTotal.value = 0;
      return;
    }
    messageList.value = res.data.rows || [];
    messageTotal.value = res.data.total || 0;
  });
}

/**
 * 翻页
 */
function handleMessagePageChange(pageNo) {
  messageQuery.value.pageNo = pageNo;
  loadMessageList();
  if (messageBodyRef.value) {
    messageBodyRef.value.scrollTop = 0;
  }
}

/**
 * 弹窗展开时加载消息
 */
function loadNavMessageData() {
  messageQuery.value.pageNo = 1;
  loadMessageList();
}

/**
 * 消息是否可点击跳转
 * @param refId
 * @returns {boolean}
 */
function isClickableMessage(refId) {
  return Number(refId) % 100 === 7 || Number(refId) % 100 === 9 || Number(refId) % 100 === 12;
}

/**
 * 消息点击跳转
 * 文档/附件跳转到文档预览页
 * 流程消息跳转到对应流程列表并打开详情
 * @param item
 */
function handleMessageClick(item) {
  const refId = item.refId;
  if (refId === undefined || refId === null) {
    return;
  }
  const refType = Number(refId) % 100;
  // 文档/附件 -> 文档预览页
  if (refType === 7 || refType === 9) {
    const { href } = router.resolve({ path: '/document/preview', query: { id: refId } })
    window.open(href, '_blank')
    messageVisible.value = false;
    return;
  }
  // 流程消息 -> 我的待办 / 我的申请
  if (refType === 12) {
    if (item.type === 2 || item.type === 5) {
      // 待办/催办通知 -> 我的待办
      router.push({ path: '/home/flow/myTodo', query: { id: refId } }).catch(() => {});
      messageVisible.value = false;
    } else if (item.type === 3 || item.type === 4) {
      // 完结/驳回通知 -> 我的申请
      router.push({ path: '/home/flow/myApplication', query: { id: refId } }).catch(() => {});
      messageVisible.value = false;
    }
  }
}

</script>

<style lang="scss" scoped>
.nav-bar {
  position: relative;
  display: flex;
  align-items: center;
  height: $nav-height;
  padding: 0px;
  background: var(--nav-bg);
}
.logo-box {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding-left: 5px;
}
.logo-lockup {
  display: block;
  height: 32px;
  width: auto;
}
.logo-version {
  align-self: flex-end;
  margin-left: 8px;
  margin-bottom: 9px;
  font-size: 13px;
  line-height: 1;
  color: var(--nav-text);
  opacity: 0.7;
}
.user-dock {
  position: relative;
  flex: none;
  width: 50px;
  height: 100%;
  margin-left: 14px;
  margin-top: 4px;

  &__trigger {
    position: relative;
    z-index: 1;
    width: 50px;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;

    :deep(.el-avatar) {
      position: relative;
      border: 1px dashed var(--nav-avatar-border);
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
    }

    :deep(.el-avatar::after) {
      content: '';
      position: absolute;
      inset: 0;
      border-radius: 50%;
      padding: 2px;
      background: conic-gradient(from 0deg, transparent 0deg 250deg, var(--nav-avatar-ring) 340deg, transparent 360deg);
      -webkit-mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
      -webkit-mask-composite: xor;
      mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
      mask-composite: exclude;
      opacity: 0;
      pointer-events: none;
      transition: opacity 0.2s ease;
    }

    &:hover :deep(.el-avatar::after) {
      opacity: 1;
      animation: nav-avatar-ring-spin 1.4s linear infinite;
    }
  }

  &__items {
    position: absolute;
    left: 50px;
    top: 0;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    gap: 4px;
    height: 50px;
    padding: 0 12px 0 2px;
    background: var(--nav-bg);
    border-radius: 0 0 10px 10px;
    white-space: nowrap;
    overflow: hidden;
    max-width: 0;
    opacity: 0;
    pointer-events: none;
    transition: max-width 0.22s ease, opacity 0.18s ease;
  }

  &.is-open &__items {
    max-width: 620px;
    opacity: 1;
    pointer-events: auto;
    box-shadow: var(--app-shadow-nav);
  }
}

@keyframes nav-avatar-ring-spin {
  to {
    transform: rotate(360deg);
  }
}

.dock-group {
  display: flex;
  align-items: center;
  gap: 4px;
}

.dock-divider {
  width: 1px;
  height: 22px;
  margin: 0 8px;
  background: var(--app-border);
}

.dock-btn {
  width: 44px;
  height: 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  border-radius: 8px;
  color: var(--sidebar-text);
  cursor: pointer;
  transition: all 0.18s ease;

  .el-icon {
    font-size: 21px;
  }

  span {
    font-size: 11px;
    line-height: 1;
  }

  &:hover {
    background: var(--app-primary-light);
    color: var(--app-primary);
  }

  &.is-active {
    background: var(--app-primary-light);
    color: var(--app-primary);
  }

  &--danger:hover {
    color: var(--el-color-danger);
  }
}

.nav-message-dialog {
  .nav-message-body {
    height: 480px;
    overflow-y: auto;
  }
  .el-dialog__footer {
    .el-pagination {
      display: flex;
      justify-content: flex-end;
      padding: 0;
      margin-bottom: $spacing-sm;
    }
  }
  .nav-message-content {
    margin: 6px 0 0 0;
    font-size: 13px;
    color: $color-text-secondary;
    line-height: 18px;
  }
  .nav-message-item {
    padding: 4px;
    border-radius: $border-radius-sm;
    cursor: pointer;
    transition: $transition-fast;
    &.is-link:hover {
      background-color: $color-primary-soft;
    }
  }
  .nav-message-detail {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 2px;
    margin-top: 8px;
    padding-top: 8px;
    border-top: 1px dashed $border-color-light;
    color: $color-primary;
    font-size: 12px;
    .el-icon {
      font-size: 12px;
    }
  }
  .nav-message-footer {
    margin-top: 8px;
    padding-top: 8px;
    border-top: 1px solid $border-color-light;
    display: flex;
    justify-content: center;
    .el-pagination {
      float: none;
      padding: 0;
    }
  }
}
</style>

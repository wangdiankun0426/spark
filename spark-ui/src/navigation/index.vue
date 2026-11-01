<template>
  <div class="nav-bar" data-tauri-drag-region>
    <div class="nav-left" data-tauri-drag-region="false">
      <img class="logo-lockup" :src="currentTheme === 'light' ? logoLockupDark : logoLockupLight" @click="$router.push('/home')"/>
      <span class="logo-version">v{{ appVersion }}</span>
      <div
          class="user-avatar-trigger"
          data-tauri-drag-region="false"
          @click="userInfoVisible = true"
      >
        <user-avatar :user-id="userInfo.id" :size="36" />
      </div>
      <div v-if="hasModuleMenu" class="dock-group">
        <div
            v-if="hasMenu(MODULE_CODES.LLM)"
            class="dock-btn"
            :class="{ 'is-active': defaultActive === '/llm' }"
            @click="switchModule('/llm')"
        >
          <NavAi class="dock-nav-icon"/>
          <span>AI</span>
        </div>
        <div
            v-if="hasMenu(MODULE_CODES.BUS)"
            class="dock-btn"
            :class="{ 'is-active': defaultActive === '/bus' }"
            @click="switchModule('/bus')"
        >
          <NavBus class="dock-nav-icon"/>
          <span>业务</span>
        </div>
        <div
            v-if="isOrgAdmin()"
            class="dock-btn"
            :class="{ 'is-active': defaultActive === '/org' }"
            @click="switchModule('/org')"
        >
          <NavOrg class="dock-nav-icon"/>
          <span>组织</span>
        </div>
        <div
            v-if="isSysAdmin()"
            class="dock-btn"
            :class="{ 'is-active': defaultActive === '/site' }"
            @click="switchModule('/site')"
        >
          <NavSite class="dock-nav-icon"/>
          <span>站点</span>
        </div>
      </div>
      <div v-if="hasModuleMenu" class="dock-divider"></div>
      <div class="dock-group">
        <div class="dock-btn" @click="messageVisible = true">
          <el-icon><Bell /></el-icon>
          <span>消息</span>
        </div>
        <div class="dock-btn" @click="tenantSwitchVisible = true">
          <el-icon><OfficeBuilding /></el-icon>
          <span>租户</span>
        </div>
        <div class="dock-btn" @click="mcpTokenVisible = true">
          <el-icon><Key /></el-icon>
          <span>MCP</span>
        </div>
        <div class="dock-btn" @click="toggleTheme">
          <el-icon><Moon v-if="currentTheme === 'light'" /><Sunny v-else /></el-icon>
          <span>主题</span>
        </div>
        <div class="dock-btn" @click="handleLocaleChange">
          <el-icon><Globe /></el-icon>
          <span>语言</span>
        </div>
        <div class="dock-btn dock-btn--danger" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出</span>
        </div>
      </div>
    </div>
  </div>

  <user-profile v-model="userInfoVisible" />
  <tenant-switch v-model="tenantSwitchVisible" :current-tenant-id="userInfo.currentTenantId" />
  <mcp-token v-model="mcpTokenVisible" />

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
</template>

<script setup>
import {logoutAPI} from "@/api/manage/auth/login.js";
import {pageMyMessageListAPI} from "@/api/manage/sys/message.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {ref, computed} from "vue";
import {Moon, Sunny, Right, OfficeBuilding, SwitchButton, Key} from "@element-plus/icons-vue";
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
import NavAi from "@/assets/icons/navAi.vue";
import NavBus from "@/assets/icons/navBus.vue";
import NavOrg from "@/assets/icons/navOrg.vue";
import NavSite from "@/assets/icons/navSite.vue";
import logoLockupDark from '@/assets/images/logo-lockup-dark.png';
import logoLockupLight from '@/assets/images/logo-lockup-light.png';
import {isOrgAdmin, isSysAdmin} from "@/utils/permissionUtil.js";
const router = useRouter();
const route = useRoute();
const store = useStore()

const userInfo = computed(() => store.getters['user/getUserInfo'] || { id: undefined })

const hasModuleMenu = computed(() =>
    hasMenu(MODULE_CODES.LLM) || hasMenu(MODULE_CODES.BUS) || isOrgAdmin() || isSysAdmin()
)

const appVersion = __APP_VERSION__

const currentTheme = ref(getTheme());
applyTheme(currentTheme.value);

/**
 * 点击按钮在深色/浅色主题间切换并持久化
 */
function toggleTheme() {
  currentTheme.value = currentTheme.value === 'light' ? 'dark' : 'light';
  setTheme(currentTheme.value);
}

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
 * 切换模块
 * @param path 模块首页路径
 */
function switchModule(path) {
  router.push(path).catch(() => {});
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
    logoutAPI().then(() => {
      store.dispatch('user/logout');
      router.push({ path: "/login" }).catch(()=>{});
    })
  }).catch(() => {})
}

const defaultActive = computed(() => {
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
 * @param refId 消息关联标识
 * @returns {boolean}
 */
function isClickableMessage(refId) {
  return Number(refId) % 100 === 7 || Number(refId) % 100 === 9 || Number(refId) % 100 === 12;
}

/**
 * 消息点击跳转
 * @param item 消息
 */
function handleMessageClick(item) {
  const refId = item.refId;
  if (refId === undefined || refId === null) {
    return;
  }
  const refType = Number(refId) % 100;
  if (refType === 7 || refType === 9) {
    const { href } = router.resolve({ path: '/document/preview', query: { id: refId } })
    window.open(href, '_blank')
    messageVisible.value = false;
    return;
  }
  if (refType === 12) {
    if (item.type === 2 || item.type === 5) {
      router.push({ path: '/home/flow/myTodo', query: { id: refId } }).catch(() => {});
      messageVisible.value = false;
    } else if (item.type === 3 || item.type === 4) {
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
  background: var(--nav-bg);
}
.nav-left {
  display: flex;
  align-items: center;
  gap: 4px;
  padding-left: 5px;
}
.logo-lockup {
  display: block;
  height: 32px;
  width: auto;
  cursor: pointer;
}
.logo-version {
  align-self: flex-end;
  margin-left: 8px;
  margin-bottom: 15px;
  font-size: 13px;
  line-height: 1;
  color: var(--nav-text);
  opacity: 0.7;
}
.user-avatar-trigger {
  flex: none;
  width: 44px;
  height: 44px;
  margin-left: 14px;
  margin-right: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  :deep(.el-avatar) {
    border: 1px dashed var(--nav-avatar-border);
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    transition: transform 0.18s ease;
  }

  &:hover :deep(.el-avatar) {
    transform: scale(1.12);
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

  .dock-nav-icon {
    width: 22px;
    height: 22px;
  }

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
}
</style>

<template>
  <div>
    <el-menu :default-active="defaultActive" router mode="horizontal">
      <div class="logo-box">
        <img src="../assets/images/logo.png" width="46" height="40" @click="$router.push('/home')"/>
      </div>
      <el-menu-item v-if="hasMenu(MODULE_CODES.LLM)" index="/llm">
        <el-icon><HelpFilled /></el-icon>
        <span>AI管理</span>
      </el-menu-item>
      <el-menu-item v-if="hasMenu(MODULE_CODES.BUS)" index="/bus">
        <el-icon><Menu /></el-icon>
        <span>业务管理</span>
      </el-menu-item>
      <el-menu-item index="/org" v-if="isOrgAdmin()">
        <el-icon><UserFilled /></el-icon>
        <span>组织管理</span>
      </el-menu-item>
      <el-menu-item index="/site" v-if="isSysAdmin()">
        <el-icon><Setting /></el-icon>
        <span>站点管理</span>
      </el-menu-item>

      <div class="tool-box">
        <!-- 主题切换 -->
        <div class="tool-item"
             :title="currentTheme === 'light' ? '切换为深色主题' : '切换为浅色主题'"
             @click="toggleTheme"
        >
          <el-icon style="font-size: 20px">
            <Sunny v-if="currentTheme === 'light'" />
            <Moon v-else />
          </el-icon>
        </div>
        <!-- 国际化 -->
        <el-dropdown trigger="click" @command="handleLocaleChange">
          <div class="tool-item" title="切换语言">
            <span class="tool-item-text">中</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="zh-CN">简体中文</el-dropdown-item>
              <el-dropdown-item command="en-US">English</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <!-- 全屏 -->
        <div class="tool-item"
             :title="isFullscreen ? '退出全屏' : '全屏'"
             @click="toggleFullscreen"
        >
          <el-icon style="font-size: 20px">
            <Aim v-if="isFullscreen" />
            <FullScreen v-else />
          </el-icon>
        </div>
        <!-- 消息 -->
        <el-popover
            trigger="click"
            placement="bottom"
            :width="360"
            popper-class="nav-message-popover"
            @show="loadNavMessageData"
        >
          <template #reference>
            <div class="tool-item" title="消息">
              <el-icon style="font-size: 20px"><Bell /></el-icon>
            </div>
          </template>
          <div class="nav-message-body" @scroll="handleMessageScroll">
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
            <!-- 分页加载提示 -->
            <div v-if="messageList.length && messageTipText" class="nav-message-tip">
              {{ messageTipText }}
            </div>
          </div>
        </el-popover>
      </div>

      <div class="user-box">
        <el-dropdown>
          <div class="user-profile-trigger">
            <user-avatar :user-id="userInfo.id" :size="36" @click="openUserInfoForm" />
            <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <!--当前登录用户信息-->
              <div class="user-dropdown-header">
                <div class="user-dropdown-name">{{ userInfo.name }}</div>
              </div>
              <el-dropdown-item
                  @click="openUserInfoForm"
              >
                <el-icon class="user-menu-icon"><User /></el-icon>个人中心
              </el-dropdown-item>
              <el-dropdown-item
                  @click="openTenantSwitch"
              >
                <el-icon class="user-menu-icon"><OfficeBuilding /></el-icon>切换租户
              </el-dropdown-item>
              <el-dropdown-item
                  @click="handleLogout"
              >
                <el-icon class="user-menu-icon"><SwitchButton /></el-icon>退出系统
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-menu>

    <!--个人中心弹窗-->
    <user-profile v-model="userInfoVisible" />

    <!--切换租户抽屉-->
    <tenant-switch v-model="tenantSwitchVisible" :current-tenant-id="userInfo.currentTenantId" />
  </div>
</template>

<script setup>
import {logoutAPI} from "@/api/manage/auth/login.js";
import {pageMyMessageListAPI} from "@/api/manage/sys/message.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {ref, computed, onMounted, onUnmounted} from "vue";
import {ArrowDown, Setting, Moon, Sunny, Right, FullScreen, Aim, User, OfficeBuilding, SwitchButton} from "@element-plus/icons-vue";
import { useRouter, useRoute } from 'vue-router';
import {useStore} from "vuex";
import {getTheme, applyTheme, setTheme} from '@/utils/themeUtil';
import {hasMenu, MODULE_CODES} from '@/utils/menuUtil.js';
import UserAvatar from '@/components/UserAvatar';
import UserProfile from '@/components/UserProfile';
import TenantSwitch from '@/components/TenantSwitch';
import Bell from "@/assets/icons/bell.vue";
import {isOrgAdmin, isSysAdmin} from "@/utils/utils.js";
const router = useRouter();
const route = useRoute();
const store = useStore()

const userInfo = computed(() => store.getters['user/getUserInfo'] || { id: undefined })

const currentTheme = ref(getTheme());
applyTheme(currentTheme.value);

/**
 * 点击按钮在深色/浅色主题间切换并持久化
 */
function toggleTheme() {
  currentTheme.value = currentTheme.value === 'light' ? 'dark' : 'light';
  setTheme(currentTheme.value);
}

// 是否处于全屏状态
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

// 个人中心弹窗显隐
const userInfoVisible = ref(false);

// 切换租户抽屉显隐
const tenantSwitchVisible = ref(false);

/**
 * 打开切换租户抽屉
 */
function openTenantSwitch() {
  tenantSwitchVisible.value = true;
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

// 消息弹窗相关
const messageList = ref([]);

// 我的消息分页参数与状态
const messageQuery = ref({ pageNo: 1, pageSize: 10 });
const messageTotal = ref(0);
const messageLoadedCount = ref(0);
const messageLoading = ref(false);
const messageFinished = ref(false);

// 底部加载提示
const messageTipText = computed(() => {
  if (messageLoading.value) {
    return '加载中...';
  }
  return messageFinished.value ? '没有更多了' : '';
});

/**
 * 重置分页并加载我的消息（每次弹窗展开时重新拉取）
 */
function loadMessageList() {
  messageQuery.value.pageNo = 1;
  messageList.value = [];
  messageTotal.value = 0;
  messageLoadedCount.value = 0;
  messageFinished.value = false;
  loadMoreMessages();
}

/**
 * 加载更多我的消息
 */
function loadMoreMessages() {
  if (messageLoading.value || messageFinished.value) {
    return;
  }
  messageLoading.value = true;
  pageMyMessageListAPI(messageQuery.value).then(res => {
    if (res.code !== 200 || !res.data) {
      messageFinished.value = true;
      return;
    }
    const rows = res.data.rows || [];
    messageList.value = messageList.value.concat(rows);
    messageTotal.value = res.data.total || 0;
    messageLoadedCount.value += rows.length;
    messageFinished.value = messageLoadedCount.value >= messageTotal.value;
    messageQuery.value.pageNo++;
  }).finally(() => {
    messageLoading.value = false;
  });
}

/**
 * 消息列表滚动到底部时加载下一页
 * @param e 滚动事件对象
 */
function handleMessageScroll(e) {
  const el = e.target;
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 20) {
    loadMoreMessages();
  }
}

/**
 * 弹窗展开时加载消息
 */
function loadNavMessageData() {
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
    return;
  }
  // 流程消息 -> 我的待办 / 我的申请
  if (refType === 12) {
    if (item.type === 2 || item.type === 5) {
      // 待办/催办通知 -> 我的待办
      router.push({ path: '/home/flow/myTodo', query: { id: refId } }).catch(() => {});
    } else if (item.type === 3 || item.type === 4) {
      // 完结/驳回通知 -> 我的申请
      router.push({ path: '/home/flow/myApplication', query: { id: refId } }).catch(() => {});
    }
  }
}

</script>

<style lang="scss" scoped>
// 导航栏整体 - 跟随主题变量
.el-menu {
  background: var(--nav-bg) !important;
  box-shadow: var(--nav-shadow);
}
// 左侧 logo 区域
.logo-box {
  position: absolute;
  left: 12px;
  top: 0;
  display: flex;
  align-items: center;
  height: 100%;
  width: 60px;
  overflow: hidden;
  cursor: pointer;
}
// 一级菜单项基础样式
.el-menu-item {
  color: var(--nav-text) !important;
  font-size: 15px !important;
  font-weight: 400 !important;
  transition: $transition-fast;
  letter-spacing: 2px;
}
// 悬停一级菜单
.el-menu-item:hover {
  background-color: var(--nav-hover-bg) !important;
  font-weight: 600;
  color: var(--nav-hover-text) !important;
  border-bottom: 0;
}
// 激活状态一级菜单
.el-menu-item.is-active {
  background-color: var(--nav-active-bg) !important;
  border-bottom: 3px solid var(--nav-active-border) !important;
  color: var(--nav-active-text) !important;
}
.el-menu--horizontal.el-menu {
  --el-menu-horizontal-height: #{$nav-height} !important;
  border: 0;
  height: $nav-height !important;
  display: flex;
  align-items: center;
  justify-content: center;
}
// 工具栏区域
.tool-box {
  height: $nav-height;
  position: absolute;
  right: 110px;
  top: 0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
}

.tool-item {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  cursor: pointer;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: $transition-fast;
  color: var(--nav-tool-color);
  margin-right: 2px;
  margin-left: 2px;
  &:hover,
  &.is-active {
    background-color: var(--nav-tool-hover-bg);
    color: var(--nav-tool-hover-color);
    box-shadow: 0 0 8px rgba(96, 165, 250, 0.4);
  }
}

.tool-item-text {
  font-size: 14px;
  font-weight: 600;
  line-height: 1;
}

.user-box {
  position: absolute;
  right: 30px;
  border: 0;
  cursor: pointer;
  height: $nav-height;
  display: flex;
  align-items: center;

  .user-profile-trigger {
    display: flex;
    align-items: center;
    gap: 8px;
    outline: none;

    :deep(.el-avatar) {
      border: 0.5px solid var(--nav-avatar-border);
      transition: $transition-fast;
      border-radius: 50% !important;
      background: rgba(255, 255, 255, 0.1);
    }

    .dropdown-icon {
      color: var(--nav-dropdown-color);
      font-size: 12px;
      transition: $transition-fast;
      filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
    }

    &:hover {
      .dropdown-icon {
        color: var(--nav-tool-hover-color);
      }
      :deep(.el-avatar) {
        border-color: var(--nav-active-border);
      }
    }
  }
}

.user-dropdown-header {
  padding: 8px 16px;
  border-bottom: 1px solid $border-color-light;
}

.user-dropdown-name {
  max-width: 0;
  min-width: 100%;
  font-size: 13px;
  font-weight: 600;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-menu-icon {
  margin-right: 6px;
}

:deep(.el-tooltip__trigger:focus-visible) {
  outline: none !important;
}
:deep(.el-dropdown-link:focus) {
  outline: none !important;
}

.nav-message-popover {
  .nav-message-body {
    height: 400px;
    overflow-y: auto;
  }
  .nav-message-tip {
    padding: 8px 0;
    text-align: center;
    font-size: 12px;
    color: $color-text-secondary;
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

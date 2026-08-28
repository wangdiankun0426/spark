<template>
  <div>
    <el-menu :default-active="defaultActive" router mode="horizontal">
      <div class="logo-box">
        <img src="../assets/images/logo.png" width="50" height="40"/>
      </div>
      <el-menu-item index="/home">
        <el-icon><DataAnalysis /></el-icon>
        <span>首页</span>
      </el-menu-item>
      <el-menu-item index="/llm">
        <el-icon><Ai /></el-icon>
        <span>AI应用</span>
      </el-menu-item>
      <el-menu-item index="/kb">
        <el-icon><FolderOpened /></el-icon>
        <span>知识库</span>
      </el-menu-item>
      <el-menu-item index="/kg">
        <el-icon><GraphV2 /></el-icon>
        <span>知识图谱</span>
      </el-menu-item>
      <el-menu-item index="/flow">
        <el-icon><Connection /></el-icon>
        <span>流程中心</span>
      </el-menu-item>
      <el-menu-item index="/manage" v-if="userInfo.id === 101 && !isDesktop">
        <el-icon><Setting /></el-icon>
        <span>管理后台</span>
      </el-menu-item>

      <div class="tool-box">
        <!-- 主题切换 -->
        <div class="tool-item" :title="currentTheme === 'light' ? '切换为深色主题' : '切换为浅色主题'" @click="toggleTheme">
          <el-icon style="font-size: 20px">
            <Sunny v-if="currentTheme === 'light'" />
            <Moon v-else />
          </el-icon>
        </div>
        <!-- 搜索快捷功能 -->
        <el-popover
            v-model:visible="searchVisible"
            trigger="click"
            placement="bottom"
            :width="320"
            popper-class="nav-search-popover"
        >
          <template #reference>
            <div class="tool-item" :class="{ 'is-active': searchVisible }" title="快捷功能">
              <el-icon style="font-size: 20px"><SearchV2 /></el-icon>
            </div>
          </template>
          <div class="nav-search-panel">
            <el-input
                v-model="searchKeyword"
                placeholder="搜索快捷功能"
                clearable
                :prefix-icon="Search"
                ref="searchInputRef"
            />
            <div class="nav-search-list">
              <div
                  v-for="item in filteredShortcuts"
                  :key="item.path"
                  class="nav-search-item"
                  @click="handleSelectShortcut(item)"
              >
                <el-icon class="nav-search-icon"><component :is="item.icon" /></el-icon>
                <span class="nav-search-name">{{ item.name }}</span>
              </div>
              <el-empty v-if="filteredShortcuts.length === 0" description="无匹配项" :image-size="50"/>
            </div>
          </div>
        </el-popover>
        <!-- 通讯录 -->
        <div class="tool-item" @click="openContactDrawer" title="通讯录">
          <el-icon style="font-size: 20px"><ContactList /></el-icon>
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
          <el-tabs v-model="navMessageTab" class="nav-message-tabs">
            <el-tab-pane label="消息" name="message">
              <div class="nav-message-body">
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
                    </div>
                  </el-timeline-item>
                </el-timeline>
                <el-empty v-else description="暂无消息" :image-size="60"/>
              </div>
            </el-tab-pane>
            <el-tab-pane label="公告" name="notice">
              <div class="nav-message-body">
                <div v-if="noticeList.length">
                  <div
                      v-for="(item, i) in noticeList"
                      :key="i"
                      class="nav-notice-item"
                      @click="handleViewNotice(item)"
                  >
                    <div class="nav-notice-title">{{ item.title }}</div>
                    <div class="nav-notice-meta">
                      <el-tag size="small" type="info">{{ item.typeName }}</el-tag>
                      <span class="nav-notice-time">{{ item.createdDt }}</span>
                    </div>
                  </div>
                </div>
                <el-empty v-else description="暂无公告" :image-size="60"/>
              </div>
            </el-tab-pane>
          </el-tabs>
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
              <el-dropdown-item @click="openUserInfoForm">个人中心</el-dropdown-item>
              <el-dropdown-item @click="handleLogout">退出系统</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-menu>

    <!--通讯录抽屉-->
    <contacts v-model="contactVisible" />

    <!--个人中心弹窗-->
    <user-profile v-model="userInfoVisible" />
  </div>
</template>

<script setup>
import {userDetailAPI} from "@/api/system/user.js";
import {logoutAPI} from "@/api/auth/login.js";
import {queryMyMessageListAPI} from "@/api/system/message.js";
import {noticeListAPI} from "@/api/system/notice.js";
import {ElMessageBox} from "element-plus";
import {ref, computed, nextTick, watch} from "vue";
import {ArrowDown, DataAnalysis, FolderOpened, Connection, Search, Setting, Moon, Sunny} from "@element-plus/icons-vue";
import { useRouter, useRoute } from 'vue-router';
import {useStore} from "vuex";
import {getTheme, applyTheme, setTheme} from '@/utils/themeUtil';
import UserAvatar from '@/components/UserAvatar';
import UserProfile from '@/components/UserProfile';
import Contacts from '@/components/Chat/contacts.vue';
import ContactList from "@/assets/icons/contactList.vue";
import GraphV2 from "@/assets/icons/graphV2.vue";
import Ai from "@/assets/icons/ai.vue";
import FlowListIcon from '@/assets/icons/flowList.vue';
import MyPendingListIcon from '@/assets/icons/myPendingList.vue';
import KnowledgeIcon from '@/assets/icons/knowledge.vue';
import GraphIcon from '@/assets/icons/graph.vue';
import AgentIcon from '@/assets/icons/agent.vue';
import ModelMarketIcon from '@/assets/icons/modelMarket.vue';
import Bell from "@/assets/icons/bell.vue";
import SearchV2 from "@/assets/icons/searchV2.vue";
import {isDesktop} from "@/utils/desktop.js";
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

// 个人中心弹窗显隐
const userInfoVisible = ref(false);

// 通讯录抽屉显隐
const contactVisible = ref(false);

/**
 * 打开通讯录抽屉
 */
function openContactDrawer() {
  contactVisible.value = true;
}

// 搜索快捷功能相关
const searchVisible = ref(false);
const searchKeyword = ref('');
const searchInputRef = ref(null);

// 快捷功能列表
const shortcutList = ref([
  { name: '流程申请', path: '/flow/application', icon: FlowListIcon },
  { name: '我的待办', path: '/flow/myTodo', icon: MyPendingListIcon },
  { name: '知识库', path: '/kb/knowledge', icon: KnowledgeIcon },
  { name: '知识图谱', path: '/kg/graph', icon: GraphIcon },
  { name: 'Agent', path: '/llm/agent', icon: AgentIcon },
  { name: '模型市场', path: '/llm/modelMarket', icon: ModelMarketIcon }
]);

// 按关键字过滤快捷功能
const filteredShortcuts = computed(() => {
  const kw = searchKeyword.value.trim();
  if (!kw) {
    return shortcutList.value;
  }
  return shortcutList.value.filter(item => item.name.includes(kw));
});

/**
 * 选中快捷功能后跳转
 * @param item
 */
function handleSelectShortcut(item) {
  router.push(item.path).catch(() => {});
  searchVisible.value = false;
  searchKeyword.value = '';
}

/**
 * 搜索框展开时自动聚焦输入框
 */
watch(searchVisible, (val) => {
  if (val) {
    nextTick(() => {
      searchInputRef.value?.focus();
    });
  }
});

/**
 * 拉取当前登录用户详情并同步至 store（登录后首次进入时触发）
 */
function getUserDetail() {
  userDetailAPI().then(res => {
    store.dispatch('user/setUserInfo', { userInfo: res.data })
  })
}

getUserDetail()

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
const noticeList = ref([]);
const navMessageTab = ref('message');

/**
 * 加载消息列表（每次弹窗时重新拉取）
 */
function loadMessageList() {
  queryMyMessageListAPI().then(res => {
    messageList.value = res.data || [];
  });
}

/**
 * 加载公告列表（每次弹窗时重新拉取）
 */
function loadNoticeList() {
  noticeListAPI({}).then(res => {
    if (res.code !== 200) {
      return;
    }
    noticeList.value = res.data || [];
  });
}

/**
 * 弹窗展开时加载消息与公告
 */
function loadNavMessageData() {
  loadMessageList();
  loadNoticeList();
}

/**
 * 消息是否可点击跳转（附件/文档/流程）
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
      router.push({ path: '/flow/myTodo', query: { id: refId } }).catch(() => {});
    } else if (item.type === 3 || item.type === 4) {
      // 完结/驳回通知 -> 我的申请
      router.push({ path: '/flow/myApplication', query: { id: refId } }).catch(() => {});
    }
  }
}

/**
 * 打开公告预览
 * @param row
 */
function handleViewNotice(row) {
  window.open("/notice/view/" + row.id);
}

</script>

<style lang="scss" scoped>
// 导航栏整体 - 跟随主题变量（深色蓝渐变 / 浅色白）
.el-menu {
  background: var(--nav-bg) !important;
  box-shadow: var(--nav-shadow);
}

// 左侧 logo 区域
.logo-box {
  display: flex;
  align-items: center;
  height: 100%;
  width: 60px;
  padding: 0 0 0 10px;
  margin-left: 20px;
  margin-right: 10px;
  overflow: hidden;
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
}

// 工具栏区域
.tool-box {
  height: $nav-height;
  width: 400px;
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
  margin-right: 8px;

  &:hover,
  &.is-active {
    background-color: var(--nav-tool-hover-bg);
    color: var(--nav-tool-hover-color);
    box-shadow: 0 0 8px rgba(96, 165, 250, 0.4);
  }
}

// 搜索面板
.nav-search-panel {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding: $spacing-xs 0;
}

.nav-search-list {
  max-height: 280px;
  overflow-y: auto;
}

.nav-search-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-sm;
  border-radius: $border-radius-sm;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    background-color: $color-primary-light;

    .nav-search-icon,
    .nav-search-name {
      color: $color-primary;
    }
  }
}

.nav-search-icon {
  font-size: 18px;
  color: $color-text-secondary;
  transition: $transition-fast;
}

.nav-search-name {
  font-size: 14px;
  color: $color-text-primary;
  transition: $transition-fast;
}

// 右侧用户头像区域
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

/* 去掉 element-plus dropdown 默认聚焦边框 */
:deep(.el-tooltip__trigger:focus-visible) {
  outline: none !important;
}
:deep(.el-dropdown-link:focus) {
  outline: none !important;
}

// 导航栏消息悬浮框
.nav-message-popover {
  .nav-message-tabs {
    .el-tabs__header {
      margin-bottom: 8px;
    }
    .el-tabs__nav-wrap::after {
      height: 1px;
    }
  }
  .nav-message-body {
    max-height: 320px;
    overflow-y: auto;
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
  .nav-notice-item {
    padding: 8px 4px;
    border-bottom: 1px solid $border-color-light;
    cursor: pointer;
    transition: $transition-fast;
    &:last-child {
      border-bottom: 0;
    }
    &:hover {
      background-color: $color-primary-soft;
      border-radius: $border-radius-sm;
    }
  }
  .nav-notice-title {
    font-size: 13px;
    font-weight: 400;
    color: $color-text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .nav-notice-meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 4px;
  }
  .nav-notice-time {
    font-size: 12px;
    color: $color-text-secondary;
  }
}
</style>

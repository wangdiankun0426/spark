<template>
  <div class="app-nav" data-tauri-drag-region>
    <div class="app-nav__brand" @click="goHome">
      <img class="app-nav__logo" :src="brandLogo" alt="星火云"/>
      <span class="app-nav__name">星火云<i>AI</i></span>
      <span class="app-nav__version">v{{ appVersion }}</span>
    </div>

    <nav v-if="hasModuleMenu" class="app-nav__dock">
      <div
          v-if="hasMenu(MODULE_CODES.LLM)"
          class="app-tab"
          :class="{ 'is-active': defaultActive === '/llm' }"
          @click="switchModule('/llm')"
      >
        <el-icon><NavAi/></el-icon>
        <span>AI</span>
      </div>
      <div
          v-if="hasMenu(MODULE_CODES.BUS)"
          class="app-tab"
          :class="{ 'is-active': defaultActive === '/bus' }"
          @click="switchModule('/bus')"
      >
        <el-icon><NavBus/></el-icon>
        <span>业务</span>
      </div>
      <div
          v-if="isOrgAdmin()"
          class="app-tab"
          :class="{ 'is-active': defaultActive === '/org' }"
          @click="switchModule('/org')"
      >
        <el-icon><NavOrg/></el-icon>
        <span>组织</span>
      </div>
      <div
          v-if="isSysAdmin()"
          class="app-tab"
          :class="{ 'is-active': defaultActive === '/site' }"
          @click="switchModule('/site')"
      >
        <el-icon><NavSite/></el-icon>
        <span>站点</span>
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
      <el-tooltip content="MCP 令牌" placement="bottom" :show-after="300">
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

  <el-dialog
      v-model="messageVisible"
      title="消息通知"
      width="760px"
      top="8vh"
      class="nav-message-dialog"
      @open="loadNavMessageData"
  >
    <div class="nav-message-panel">
      <div class="nav-message-tabs">
        <button
            v-for="tab in messageTabs"
            :key="tab.key"
            type="button"
            class="nav-message-tab"
            :class="{ 'is-active': messageTab === tab.key, 'is-read': tab.key === 'read' }"
            @click="switchMessageTab(tab.key)"
        >
          <span>{{ tab.label }}</span>
          <span v-if="tab.count > 0" class="nav-message-tab__count">{{ tab.count }}</span>
        </button>
      </div>
      <div class="nav-message-body" ref="messageBodyRef">
        <el-timeline v-if="messageList.length">
          <el-timeline-item
              v-for="(item, i) in messageList"
              :key="item.id || i"
              :timestamp="item.createdDt"
              placement="top"
          >
            <div
                class="nav-message-item"
                :class="{ 'is-link': isClickableMessage(item.refId), 'is-unread': item.readFlag === 0 }"
                @click="handleMessageClick(item)"
            >
              <div class="nav-message-head">
                <span v-if="item.readFlag === 0" class="nav-message-dot"></span>
                <el-tag type="info" size="small">
                  {{ item.title }}
                </el-tag>
                <el-tooltip v-if="item.readFlag === 0" content="标为已读" placement="top" :show-after="200">
                  <button type="button" class="nav-message-read-btn" aria-label="标为已读" @click.stop="handleReadMessage(item)">
                    <el-icon v-if="readingId === item.id" class="is-loading"><Loading/></el-icon>
                    <el-icon v-else><Select/></el-icon>
                  </button>
                </el-tooltip>
              </div>
              <p class="nav-message-content">{{ item.content }}</p>
              <div v-if="isClickableMessage(item.refId)" class="nav-message-detail">
                <span>详情</span>
                <el-icon><Right /></el-icon>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else :description="emptyMessageText" :image-size="60"/>
      </div>
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
import {pageMyMessageListAPI, queryMyMessageCountAPI, readMessageAPI} from "@/api/manage/sys/message.js";
import {ElMessageBox} from "element-plus";
import {ref, computed, onMounted} from "vue";
import {
  Bell,
  Key,
  Loading,
  Moon,
  OfficeBuilding,
  Right,
  Select,
  Sunny,
  SwitchButton
} from "@element-plus/icons-vue";
import NavAi from "@/assets/icons/navAi.vue";
import NavBus from "@/assets/icons/navBus.vue";
import NavOrg from "@/assets/icons/navOrg.vue";
import NavSite from "@/assets/icons/navSite.vue";
import { useRouter, useRoute } from 'vue-router';
import {useStore} from "vuex";
import {getTheme, applyTheme, setTheme} from '@/utils/themeUtil';
import {hasMenu, MODULE_CODES} from '@/utils/menuUtil.js';
import UserAvatar from '@/components/UserAvatar';
import UserProfile from '@/components/UserProfile';
import TenantSwitch from '@/components/TenantSwitch';
import McpToken from '@/components/McpToken';
import brandLogoLight from '@/assets/images/logo-light.png';
import brandLogoDark from '@/assets/images/logo.png';
import {isOrgAdmin, isSysAdmin} from "@/utils/permissionUtil.js";
import { isDesktop } from '@desktop/env.js';
const router = useRouter();
const route = useRoute();
const store = useStore()

const userInfo = computed(() => store.getters['user/getUserInfo'] || { id: undefined })

// 桌面客户端面向普通用户，不提供 AI 业务组织站点等管理模块入口，整个模块坞不渲染
const hasModuleMenu = computed(() =>
    !isDesktop && (hasMenu(MODULE_CODES.LLM) || hasMenu(MODULE_CODES.BUS) || isOrgAdmin() || isSysAdmin())
)

const appVersion = __APP_VERSION__

const currentTheme = ref(getTheme());
applyTheme(currentTheme.value);

// 品牌图标跟随主题色系，与登录页保持一致：浅色用浅底淡色图标，深色用深蓝渐变图标
const brandLogo = computed(() => (currentTheme.value === 'light' ? brandLogoLight : brandLogoDark));

/**
 * 点击品牌区回到工作台
 */
function goHome() {
  router.push('/home').catch(() => {});
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
      messageCount.value = { unread: res.data.unread || 0, readCount: res.data.readCount || 0 };
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
// 弹窗默认展示未读，按服务端 filterType 过滤
const messageTab = ref('unread');
const messageCount = ref({ unread: 0, readCount: 0 });
const messageQuery = ref({ pageNo: 1, pageSize: 8, filterType: 'unread' });
const messageTotal = ref(0);
const messageBodyRef = ref(null);
// 正在标记已读的消息 id，防止重复提交
const readingId = ref(null);
let messageRequestMark = 0;

const messageTabs = computed(() => [
  { key: 'unread', label: '未读', count: messageCount.value.unread || 0 },
  { key: 'read', label: '已读', count: messageCount.value.readCount || 0 }
]);

const emptyMessageText = computed(() => (messageTab.value === 'unread' ? '暂无未读消息' : '暂无已读消息'));

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
 * 切换消息页签，未读与已读分别向服务端按状态分页查询
 * @param key 页签标识
 */
function switchMessageTab(key) {
  if (messageTab.value === key) {
    return;
  }
  messageTab.value = key;
  messageQuery.value.pageNo = 1;
  messageQuery.value.filterType = key;
  loadMessageList();
  if (messageBodyRef.value) {
    messageBodyRef.value.scrollTop = 0;
  }
}

/**
 * 弹窗展开时加载消息，默认回到未读页签
 */
function loadNavMessageData() {
  messageTab.value = 'unread';
  messageQuery.value.pageNo = 1;
  messageQuery.value.filterType = 'unread';
  loadMessageCount();
  loadMessageList();
}

/**
 * 从当前未读列表移除已读消息，并同步分页总数
 * @param item 消息
 */
function removeUnreadRow(item) {
  const index = messageList.value.findIndex(row => row.id === item.id);
  if (index === -1) {
    return;
  }
  messageList.value.splice(index, 1);
  messageTotal.value = Math.max(messageTotal.value - 1, 0);
  // 当前页被移空但仍有余量时回补，避免停留在空列表
  if (!messageList.value.length && messageTotal.value > 0) {
    messageQuery.value.pageNo = Math.max(messageQuery.value.pageNo - 1, 1);
    loadMessageList();
  }
}

/**
 * 标记单条消息已读，成功后同步列表与未读计数
 * @param item 消息
 * @returns {Promise}
 */
function markMessageRead(item) {
  if (item.readFlag !== 0 || !item.id) {
    return Promise.resolve();
  }
  return readMessageAPI({id: item.id}).then(res => {
    if (res.code !== 200) {
      return;
    }
    item.readFlag = 1;
    loadMessageCount();
    if (messageTab.value === 'unread') {
      removeUnreadRow(item);
    }
  });
}

/**
 * 点击已读按钮标记消息已读
 * @param item 消息
 */
function handleReadMessage(item) {
  if (item.readFlag !== 0 || readingId.value !== null) {
    return;
  }
  readingId.value = item.id;
  markMessageRead(item).finally(() => {
    readingId.value = null;
  });
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
  markMessageRead(item);
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
// 导航栏与菜单栏的通用样式统一维护在 shell.scss
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
  .nav-message-tabs {
    display: inline-flex;
    align-self: flex-start;
    gap: 2px;
    padding: 3px;
    margin-bottom: $spacing-sm;
    border-radius: 999px;
    background-color: $app-bg-sub;
  }
  .nav-message-tab {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    height: 28px;
    padding: 0 14px;
    border: none;
    border-radius: 999px;
    background-color: transparent;
    font-size: 12.5px;
    font-weight: 600;
    color: $color-text-secondary;
    cursor: pointer;
    transition: $transition-fast;
    &.is-active {
      background-color: $bg-card;
      color: $color-primary;
      box-shadow: $shadow-card;
    }
  }
  .nav-message-tab__count {
    min-width: 18px;
    padding: 0 5px;
    border-radius: 9px;
    background-color: $ds-err-bg;
    color: $ds-err-tx;
    font-size: 11px;
    line-height: 18px;
    text-align: center;
  }
  .nav-message-tab.is-read .nav-message-tab__count {
    background-color: $app-bg-sub;
    color: $color-text-placeholder;
  }
  .nav-message-head {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }
  .nav-message-dot {
    flex: none;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background-color: $color-primary;
    box-shadow: 0 0 0 3px $app-primary-soft;
  }
  .nav-message-content {
    margin: 6px 0 0 0;
    font-size: 13px;
    color: $color-text-secondary;
    line-height: 18px;
  }
  .nav-message-read-btn {
    flex: none;
    margin-left: auto;
    width: 24px;
    height: 24px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    border: none;
    border-radius: 50%;
    background-color: $app-primary-soft;
    color: $color-primary;
    cursor: pointer;
    opacity: 0;
    transform: translateX(4px);
    transition: $transition-fast;
    &:hover {
      background-color: $color-primary;
      color: $color-text-white;
    }
  }
  .nav-message-item:hover .nav-message-read-btn,
  .nav-message-read-btn:focus-visible {
    opacity: 1;
    transform: none;
  }
  .nav-message-item {
    padding: 4px;
    border-radius: $border-radius-sm;
    cursor: pointer;
    transition: $transition-fast;
    &.is-link:hover {
      background-color: $color-primary-soft;
    }
    &.is-unread .nav-message-content {
      color: $color-text-primary;
      font-weight: 600;
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

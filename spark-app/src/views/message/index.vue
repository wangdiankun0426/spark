<template>
  <view class="message-container safe-area-page" :class="themeClass">
    <!-- 顶部标题栏：带返回按钮的三段式 -->
    <view class="message-top" :style="navHeaderStyle()">
      <view class="message-header">
        <view class="header-back" @click="handleBack">
          <up-icon name="arrow-left" size="20" :color="backIconColor"/>
        </view>
        <text class="header-title">消息</text>
        <view class="header-slot"></view>
      </view>
    </view>

    <!-- 筛选标签：只看已读/未读，数量与列表都由接口按同一条 SQL 条件返回 -->
    <view class="msg-filter">
      <view
          v-for="chip in filterChips"
          :key="chip.key"
          class="filter-chip"
          :class="{ 'is-on': chip.key === filterType }"
          @click="handleFilterChange(chip.key)"
      >
        <text class="filter-chip__text">{{ chip.label }}</text>
        <text v-if="chip.count > 0" class="filter-chip__count">{{ chip.count }}</text>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view
        class="message-list"
        scroll-y
        @scrolltolower="loadMoreMessages"
    >
      <!-- 加载中 -->
      <view v-if="loading && messageList.length === 0" class="loading-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <!-- 空状态 -->
      <view v-else-if="messageList.length === 0" class="empty-box">
        <up-empty :text="emptyText" icon="chat-fill"/>
      </view>

      <!-- 消息卡片 -->
      <template v-else>
        <view
            class="message-card"
            :class="{'is-unread': item.readFlag === 0}"
            v-for="item in messageList"
            :key="item.id"
            @click="handleMsgClick(item)"
        >
          <view class="card-top">
            <view class="type-pill" :class="'pill-' + pillKey(item.type)">
              <view class="pill-dot"></view>
              <text class="pill-text">{{ typeText(item.type) }}</text>
            </view>
            <text class="card-title">{{ item.title }}</text>
            <text class="card-time">{{ shortTime(item.createdDt) }}</text>
          </view>
          <text class="card-content">{{ item.content }}</text>
          <view v-if="isFlowMessage(item)" class="card-foot">
            <text class="link-text">详情</text>
            <up-icon name="arrow-right" size="11" :color="linkColor"/>
          </view>
        </view>
        <up-loadmore :status="loadStatus" class="list-loadmore"/>
      </template>
    </scroll-view>
  </view>
</template>
<script setup>
import {ref, computed} from "vue";
import {onShow} from "@dcloudio/uni-app";
import {navHeaderStyle} from "@/utils/navBarUtil";
import {useTheme} from "@/utils/useTheme";
import {getInfoColor, getHeaderIconColor} from "@/utils/themeUtil";
import {
  pageMyMessageListAPI,
  queryMyMessageCountAPI,
  readMessageAPI
} from "@/api/sys/message";

const {isDark, themeClass} = useTheme();

const backIconColor = computed(() => getHeaderIconColor(isDark.value));
const linkColor = computed(() => getInfoColor(isDark.value));

const messageList = ref([]);
const loading = ref(true);
const query = ref({
  pageNo: 1,
  pageSize: 15
});
const total = ref(0);
const loadedCount = ref(0);
const loadStatus = ref('loadmore');

const countInfo = ref({total: 0, unread: 0, readCount: 0});
const filterType = ref('all');

const filterChips = computed(() => [
  {key: 'all', label: '全部', count: countInfo.value.total || 0},
  {key: 'unread', label: '未读', count: countInfo.value.unread || 0},
  {key: 'read', label: '已读', count: countInfo.value.readCount || 0}
]);

const emptyText = computed(() => {
  if (filterType.value === 'unread') {
    return '没有未读消息';
  }
  if (filterType.value === 'read') {
    return '没有已读消息';
  }
  return '暂无消息';
});

const flowMsgTypes = [2, 3, 4, 5];
const flowDetailTypeMap = {2: 3, 3: 1, 4: 1, 5: 3};
const msgTypeText = {1: '登录', 2: '待办', 3: '完结', 4: '驳回', 5: '催办', 6: '系统'};
const msgTypePill = {1: 'info', 2: 'todo', 3: 'done', 4: 'reject', 5: 'urge', 6: 'system'};

onShow(() => {
  getMyMessageList();
  getMessageCount();
});

/**
 * 返回上一页
 */
function handleBack() {
  uni.navigateBack();
}

/**
 * 拉取分类计数
 */
function getMessageCount() {
  queryMyMessageCountAPI().then(res => {
    if (res.code === 200 && res.data) {
      countInfo.value = res.data;
    }
  });
}

/**
 * 获取用户消息列表，筛选条件交给接口处理
 */
function getMyMessageList() {
  loading.value = true;
  loadStatus.value = 'loading';
  query.value.pageNo = 1;
  query.value.filterType = filterType.value;
  pageMyMessageListAPI(query.value).then(res => {
    if (res.code !== 200 || !res.data) {
      loadStatus.value = 'nomore';
      return;
    }
    const rows = res.data.rows || [];
    messageList.value = rows;
    total.value = res.data.total || 0;
    loadedCount.value = rows.length;
    loadStatus.value = loadedCount.value >= total.value ? 'nomore' : 'loadmore';
  }).finally(() => {
    loading.value = false;
  });
}

/**
 * 触底加载更多消息
 */
function loadMoreMessages() {
  if (loading.value || loadStatus.value === 'nomore') {
    return;
  }
  query.value.pageNo++;
  loading.value = true;
  loadStatus.value = 'loading';
  pageMyMessageListAPI(query.value).then(res => {
    if (res.code === 200 && res.data) {
      const rows = res.data.rows || [];
      messageList.value = messageList.value.concat(rows);
      loadedCount.value += rows.length;
      loadStatus.value = loadedCount.value >= total.value ? 'nomore' : 'loadmore';
    } else {
      loadStatus.value = 'nomore';
    }
  }).finally(() => {
    loading.value = false;
  });
}

/**
 * 切换筛选标签：重置分页后重新请求，未读数量同步刷新
 * @param key 筛选标识
 */
function handleFilterChange(key) {
  if (key === filterType.value) {
    return;
  }
  filterType.value = key;
  getMyMessageList();
  getMessageCount();
}

/**
 * 是否为流程类消息
 * @param item 消息行数据
 */
function isFlowMessage(item) {
  return flowMsgTypes.indexOf(item.type) !== -1 && item.refId !== undefined && item.refId !== null;
}

/**
 * 消息类型名称
 * @param type 类型
 */
function typeText(type) {
  return msgTypeText[type] || '消息';
}

/**
 * 状态胶囊配色
 * @param type 类型
 */
function pillKey(type) {
  return msgTypePill[type] || 'system';
}

/**
 * 列表里的时间：当天只显示时分，昨天显示「昨天」，更早显示月日
 * @param value 服务端时间
 */
function shortTime(value) {
  if (!value) {
    return '';
  }
  const date = new Date(String(value).replace(/-/g, '/'));
  if (isNaN(date.getTime())) {
    return String(value).slice(0, 10);
  }
  const now = new Date();
  const pad = n => (n < 10 ? '0' + n : '' + n);
  if (date.toDateString() === now.toDateString()) {
    return pad(date.getHours()) + ':' + pad(date.getMinutes());
  }
  const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000);
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天';
  }
  return pad(date.getMonth() + 1) + '-' + pad(date.getDate());
}

/**
 * 点击消息：先落库已读，再按需跳转流程详情
 * @param item 消息行数据
 */
function handleMsgClick(item) {
  if (item.readFlag === 0) {
    item.readFlag = 1;
    readMessageAPI({id: item.id}).then(() => getMessageCount());
  }
  if (!isFlowMessage(item)) {
    return;
  }
  uni.navigateTo({
    url: '/pages/flow/instanceDetail?id=' + item.refId + '&type=' + flowDetailTypeMap[item.type]
  });
}
</script>
<style scoped lang="scss">
.message-container {
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background-color: var(--app-bg-page);
}

.message-top {
  flex-shrink: 0;
  background-color: var(--app-bg-card);
  border-bottom: 1px solid var(--app-border-light);
}

.message-header {
  position: relative;
  display: flex;
  align-items: center;
  height: 48px;
  padding: 0 14px;
}

.header-back,
.header-slot {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-back:active {
  opacity: 0.6;
}

.header-title {
  flex: 1;
  text-align: center;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.4px;
  color: var(--app-text-primary);
}

.msg-filter {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 10px 14px 4px;
  background-color: var(--app-bg-page);
}

.filter-chip {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 30px;
  padding: 0 12px;
  border-radius: 10px;
  background-color: var(--app-bg-card);
  border: 1px solid var(--app-border-light);
  transition: background 0.2s ease, box-shadow 0.2s ease;

  .filter-chip__text {
    font-size: 12.5px;
    color: var(--app-text-secondary);
    font-weight: 500;
  }

  .filter-chip__count {
    font-size: 11px;
    color: var(--app-text-tertiary);
    font-weight: 600;
  }
}

.filter-chip.is-on {
  background-image: var(--app-gradient-fill);
  border-color: transparent;
  box-shadow: var(--app-shadow-brand);

  .filter-chip__text,
  .filter-chip__count {
    color: var(--app-fill-tx);
    font-weight: 700;
  }
}

.message-list {
  width: 100%;
  padding: 2px 14px 0;
  flex: 1;
  min-height: 0;
  box-sizing: border-box;
  overflow-y: auto;
}

.list-loadmore {
  padding-bottom: 30px;
}

.loading-box, .empty-box {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 120px;
}

.message-card {
  position: relative;
  background-color: var(--app-bg-card);
  border: 1px solid var(--app-border-light);
  border-radius: 14px;
  padding: 13px 14px 11px;
  margin-bottom: 10px;
  box-shadow: var(--app-shadow-card);

  &:active {
    background-color: var(--app-bg-hover);
  }
}

.message-card.is-unread::before {
  content: '';
  position: absolute;
  left: 0;
  top: 16px;
  bottom: 16px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background-image: var(--app-gradient-fill);
}

.card-top {
  display: flex;
  align-items: center;
  gap: 7px;
}

.type-pill {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 3px;
  height: 20px;
  padding: 0 7px;
  border-radius: 6px;
}

.pill-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background-color: currentColor;
}

.pill-text {
  font-size: 10.5px;
  font-weight: 600;
  line-height: 1;
}

.pill-todo {
  color: var(--app-text-info);
  background-color: var(--app-bg-info);
}

.pill-urge {
  color: var(--app-text-warning);
  background-color: var(--app-bg-warning);
}

.pill-reject {
  color: var(--app-text-error);
  background-color: var(--app-bg-error);
}

.pill-done {
  color: var(--app-text-success);
  background-color: var(--app-bg-success);
}

.pill-info {
  color: var(--app-text-info);
  background-color: var(--app-bg-info);
}

.pill-system {
  color: var(--app-text-purple);
  background-color: var(--app-bg-purple);
}

.card-title {
  flex: 1;
  min-width: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text-primary);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.card-time {
  flex-shrink: 0;
  font-size: 11.5px;
  color: var(--app-text-tertiary);
}

.card-content {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.62;
  color: var(--app-text-secondary);
  word-break: break-all;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-foot {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 2px;
  margin-top: 6px;

  .link-text {
    font-size: 12px;
    font-weight: 600;
    color: var(--app-text-info);
  }
}
</style>

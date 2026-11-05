<template>
  <view class="contact-container safe-area-page" :class="themeClass">
    <view class="contact-header" :style="navHeaderStyle()">
      <text class="header-title">通讯录</text>
      <view class="header-sub">
        <text v-if="isEmpty" class="sub-text">还没有联系人</text>
        <template v-else-if="loaded && !keyword.trim()">
          <text class="sub-text">共</text>
          <text class="sub-num">{{ totalUser }}</text>
          <text class="sub-text">个联系人</text>
          <template v-if="unreadPersonCount > 0">
            <view class="sub-dot"></view>
            <text class="sub-text sub-unread">{{ unreadPersonCount }} 人未读</text>
          </template>
        </template>
      </view>
    </view>

    <view class="contact-search">
      <view class="search-field" :class="{ 'is-active': searchFocused }">
        <up-icon name="search" size="16" :color="searchIconColor"/>
        <input
            v-model="keyword"
            class="search-input"
            placeholder="搜索姓名"
            :placeholder-style="'color:' + placeholderColor"
            confirm-type="search"
            @focus="searchFocused = true"
            @blur="searchFocused = false"
        />
        <view v-if="keyword" class="search-clear" @click="clearKeyword">
          <up-icon name="close-circle-fill" size="16" :color="clearIconColor"></up-icon>
        </view>
      </view>
    </view>

    <scroll-view
        scroll-y="true"
        class="contact-list"
        @scrolltolower="loadUserList"
    >
      <view v-if="!loaded || (loading && userList.length === 0)" class="loading-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <view v-else-if="userList.length === 0" class="empty-box">
        <up-empty :text="keyword ? '没有匹配的联系人' : '还没有联系人'" icon="man-add-fill"/>
      </view>

      <template v-else>
        <view v-if="recentList.length > 0" class="section-block">
          <view class="section-head">
            <SectionTitle>最近联系</SectionTitle>
            <view class="section-more">
              <text class="section-more__text">{{ recentList.length }} 人</text>
            </view>
          </view>

          <view class="recent-row">
            <view
                class="recent-item"
                v-for="item in recentList"
                :key="'recent-' + item.id"
                @click="handleUserClick(item)"
            >
              <view class="recent-avatar">
                <user-avatar
                    type="user"
                    shape="rounded"
                    :userId="item.id"
                    :name="item.name"
                    :size="48"
                />
                <view v-if="item.noReadCount > 0" class="avatar-badge badge-page">
                  <text class="badge-text">{{ item.noReadCount > 99 ? '99+' : item.noReadCount }}</text>
                </view>
              </view>
              <text class="recent-name">{{ item.name }}</text>
            </view>
          </view>
        </view>

        <view class="section-block">
          <view class="section-head">
            <SectionTitle>全部联系人</SectionTitle>
            <view class="section-more">
              <text class="section-more__text">{{ totalUser }} 人</text>
            </view>
          </view>

          <view class="list-card">
            <template v-for="(item, index) in userList" :key="item.id">
              <view class="user-row" @click="handleUserClick(item)">
                <view class="row-avatar">
                  <user-avatar
                      type="user"
                      shape="rounded"
                      :userId="item.id"
                      :name="item.name"
                      :size="44"
                  />
                  <view v-if="item.noReadCount > 0" class="avatar-badge badge-card">
                    <text class="badge-text">{{ item.noReadCount > 99 ? '99+' : item.noReadCount }}</text>
                  </view>
                </view>
                <view class="user-main">
                  <text class="user-name">{{ item.name }}</text>
                  <text v-if="item.deptPath || item.deptName" class="user-dept">
                    {{ item.deptPath || item.deptName }}
                  </text>
                </view>
                <up-icon name="arrow-right" size="13" :color="chevronColor"></up-icon>
              </view>
              <view v-if="index < userList.length - 1" class="row-divider"></view>
            </template>
          </view>
        </view>

        <view class="list-tip">{{ loading ? '正在加载...' : (hasMore ? '上拉加载更多' : '没有更多联系人了') }}</view>
      </template>
    </scroll-view>

    <AppTabbar :value="active" @change="handleOnTabChange"/>
  </view>
</template>

<script setup>
import {ref, watch, computed} from "vue";
import {onShow} from "@dcloudio/uni-app";
import {pageUserMyListAPI} from "@/api/chat/user";
import {useDebounceFn} from "@/utils/debounce";
import {navHeaderStyle} from "@/utils/navBarUtil";
import {useTheme} from "@/utils/useTheme";
import {getSearchIconColor, getChevronColor, getPlaceholderColor} from "@/utils/themeUtil";
import UserAvatar from '@/components/UserAvatar/index.vue'
import AppTabbar from "@/components/AppTabbar/index.vue";
import SectionTitle from "@/components/SectionTitle/index.vue";

const {isDark, themeClass} = useTheme();

const active = ref("contact");
const searchIconColor = computed(() => getSearchIconColor(isDark.value));
const clearIconColor = computed(() => getChevronColor(isDark.value));
const chevronColor = computed(() => getChevronColor(isDark.value));
const placeholderColor = computed(() => getPlaceholderColor(isDark.value));

const pageSizeConst = 20;
const recentSizeConst = 6;

const userList = ref([]);
const recentList = ref([]);
const keyword = ref("");
const loaded = ref(false);
const loading = ref(false);
const hasMore = ref(true);
const searchFocused = ref(false);
const totalUser = ref(0);
let pageNo = 0;
let requestSeq = 0;

const searchUsers = useDebounceFn(() => loadUserList(true), 500);

watch(keyword, () => {
  searchUsers();
});

onShow(() => {
  loadUserList(true);
});

const unreadPersonCount = computed(() => {
  return userList.value.filter(u => u.noReadCount > 0).length;
});

// 真空态判定：顶部提示与列表空态共用，需区别于搜索无结果
// 加 !loading 是为了页面重新进入时先清空列表再拉数据，避免闪一下「还没有联系人」
const isEmpty = computed(() => {
  return loaded.value && !loading.value && userList.value.length === 0 && !keyword.value.trim();
});

function pickRecentList(rows) {
  const chatted = rows.filter(u => u.chatSpaceId || u.noReadCount > 0);
  chatted.sort((a, b) => (b.noReadCount || 0) - (a.noReadCount || 0));
  return chatted.slice(0, recentSizeConst);
}

function loadUserList(reset) {
  if (reset === true) {
    userList.value = [];
    recentList.value = [];
    totalUser.value = 0;
    pageNo = 0;
    hasMore.value = true;
  } else if (loading.value) {
    return;
  }
  if (!hasMore.value) {
    return;
  }
  loading.value = true;
  const nextPage = pageNo + 1;
  const seq = ++requestSeq;
  pageUserMyListAPI({
    pageNo: nextPage,
    pageSize: pageSizeConst,
    name: keyword.value.trim() || undefined
  }).then(res => {
    if (seq !== requestSeq || res.code !== 200) {
      return;
    }
    const data = res.data || {};
    const rows = data.rows || [];
    pageNo = nextPage;
    hasMore.value = rows.length >= pageSizeConst;
    userList.value = userList.value.concat(rows);
    if (typeof data.total === 'number') {
      totalUser.value = data.total;
    } else {
      totalUser.value = userList.value.length;
    }
    if (nextPage === 1) {
      recentList.value = pickRecentList(rows);
    }
  }).finally(() => {
    if (seq === requestSeq) {
      loading.value = false;
    }
    loaded.value = true;
  });
}

function clearKeyword() {
  keyword.value = '';
}

function handleUserClick(item) {
  const index = userList.value.findIndex(u => u.id === item.id)
  uni.navigateTo({
    url: '/views/chat/index',
    events: {
      'space-created': function(data) {
        if (data.targetId === item.id && index !== -1) {
          userList.value[index].chatSpaceId = data.spaceId
        }
      }
    },
    success: function(res) {
      res.eventChannel.emit('setTarget', {
        target: item,
        targetType: 'user'
      })
    }
  })
}

function handleOnTabChange(index) {
  uni.reLaunch({
    url: '/pages/' + index + '/index'
  })
}
</script>

<style scoped lang="scss">
.contact-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background-color: $app-bg-page;
}

.contact-header {
  padding: 14px 16px 10px;
  background-color: $app-header-bg;

  .header-title {
    display: block;
    font-size: 24px;
    font-weight: 700;
    letter-spacing: 0.5px;
    color: $app-text-primary;
  }
}

.header-sub {
  display: flex;
  align-items: center;
  margin-top: 5px;
  padding-left: 1px;
}

.sub-text {
  font-size: 11.5px;
  color: $app-text-tertiary;
}

.sub-num {
  margin: 0 3px;
  font-size: 11.5px;
  font-weight: 600;
  color: $app-brand;
}

.sub-unread {
  margin-left: 2px;
  font-weight: 600;
  color: $app-brand;
}

.sub-dot {
  width: 3px;
  height: 3px;
  margin: 0 7px 0 8px;
  border-radius: 50%;
  background-color: $app-text-placeholder;
  flex-shrink: 0;
}

.contact-search {
  flex-shrink: 0;
  padding: 10px 14px 4px;
  background-color: $app-bg-page;
}

.search-field {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 13px;
  border-radius: 14px;
  background-color: $app-bg-card;
  border: 1px solid transparent;
  box-shadow: $app-shadow-card;
  transition: border-color 0.18s ease, background-color 0.18s ease;
}

.search-field.is-active {
  border-color: $app-brand;
}

.search-input {
  flex: 1;
  min-width: 0;
  height: 100%;
  font-size: 13.5px;
  color: $app-text-primary;
  background-color: transparent;
}

.search-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
}

.contact-list {
  flex: 1;
  height: 0;
  min-height: 0;
  padding: 8px 14px 0;
  box-sizing: border-box;
}

.loading-box, .empty-box {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 110px;
}

.list-tip {
  padding: 14px 0 28px;
  text-align: center;
  font-size: 11.5px;
  color: $app-text-tertiary;
}

.section-block {
  margin-bottom: 18px;

  &:first-child {
    margin-top: 6px;
  }
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 22px;
  margin: 0 2px 9px;
}

.section-more {
  display: flex;
  align-items: center;
  gap: 1px;

  .section-more__text {
    font-size: 11px;
    color: $app-text-tertiary;
  }
}

.recent-row {
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  gap: 11px;
  padding: 3px 2px 0;
}

.recent-item {
  flex: none;
  width: 50px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;

  &:active {
    opacity: 0.7;
  }
}

.recent-avatar {
  position: relative;
  flex-shrink: 0;
}

.recent-name {
  max-width: 100%;
  font-size: 11px;
  color: $app-text-secondary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.avatar-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 999px;
  background-color: $app-error-solid;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;

  .badge-text {
    font-size: 9.5px;
    line-height: 1;
    color: $app-fill-tx;
    font-weight: 600;
  }
}

.badge-page {
  border: 1.5px solid $app-bg-page;
}

.badge-card {
  border: 1.5px solid $app-bg-card;
}

.list-card {
  background-color: $app-bg-card;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: $app-shadow-card;
}

.user-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 14px;

  &:active {
    background-color: $app-bg-hover;
  }
}

.row-avatar {
  position: relative;
  flex-shrink: 0;
}

.user-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.2px;
  color: $app-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-dept {
  font-size: 11.5px;
  line-height: 16px;
  color: $app-text-tertiary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row-divider {
  height: 1px;
  margin-left: 68px;
  background-color: $app-border-light;
}
</style>

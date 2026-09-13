<template>
  <view class="contact-container safe-area-page">
    <!-- 顶部标题栏 -->
    <view class="contact-header">
      <view class="header-left" @click="goBack">
        <up-icon name="arrow-left" size="20" color="#fff"></up-icon>
        <text class="header-title">通讯录</text>
      </view>
    </view>

    <!-- 姓名检索 -->
    <view class="contact-search">
      <up-search
          v-model="keyword"
          placeholder="搜索姓名"
          :showAction="false"
          :height="4"
          bgColor="#f5f7fa"
      />
    </view>

    <!-- 用户列表 -->
    <scroll-view
        scroll-y="true"
        class="contact-list"
        @scrolltolower="loadUserList"
    >
      <!-- 加载中 -->
      <view v-if="!loaded || (loading && userList.length === 0)" class="loading-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <!-- 空状态 -->
      <view v-else-if="userList.length === 0" class="empty-box">
        <up-empty text="暂无用户" icon="man-add-fill"/>
      </view>

      <!-- 用户卡片 -->
      <template v-else>
        <view class="user-card" v-for="item in userList" :key="item.id" @click="handleUserClick(item)">
          <view class="card-header">
            <view class="card-title-row">
              <user-avatar type="user" :userId="item.id" :name="item.name" :size="48"/>
              <view class="user-info">
                <view class="user-name-row">
                  <text class="user-name">{{ item.name }}</text>
                  <view class="badge-noread" v-if="item.noReadCount > 0">
                    <text class="badge-text">{{ item.noReadCount > 99 ? '99+' : item.noReadCount }}</text>
                  </view>
                </view>
                <text v-if="item.deptName" class="user-dept">{{ item.deptName }}</text>
              </view>
            </view>
            <up-icon name="arrow-right" size="16" color="#b3b3b3"></up-icon>
          </view>
        </view>
        <view class="list-tip">{{ loading ? '正在加载...' : (hasMore ? '上拉加载更多' : '没有更多联系人了') }}</view>
      </template>
    </scroll-view>
  </view>
</template>
<script setup>
import {ref, watch} from "vue";
import {pageUserMyListAPI} from "@/api/chat/user";
import {useDebounceFn} from "@/utils/debounce";
import UserAvatar from '@/components/UserAvatar/index.vue'

// 每页加载的联系人数量
const PAGE_SIZE = 20

const userList = ref([]);
// 姓名检索关键字
const keyword = ref("");
// 首屏是否加载完成
const loaded = ref(false);
// 是否正在加载
const loading = ref(false);
// 是否还有下一页
const hasMore = ref(true);
// 已加载到的页码
let pageNo = 0;
// 请求序号，用于丢弃过期响应（检索期间连续输入时）
let requestSeq = 0;

// 关键字变化后防抖检索
const searchUsers = useDebounceFn(() => loadUserList(true), 500);

watch(keyword, () => {
  searchUsers();
});

loadUserList();

/**
 * 加载联系人，检索条件变化时从头加载
 * @param reset 是否重置为第一页，仅显式传 true 才重置（避免事件对象被当成参数）
 */
function loadUserList(reset) {
  if (reset === true) {
    userList.value = [];
    pageNo = 0;
    hasMore.value = true;
  } else if (loading.value) {
    // 上拉加载去重，检索重置可直接打断进行中的请求
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
    pageSize: PAGE_SIZE,
    name: keyword.value.trim() || undefined
  }).then(res => {
    if (seq !== requestSeq || res.code !== 200) {
      return;
    }
    const rows = (res.data && res.data.rows) || [];
    pageNo = nextPage;
    hasMore.value = rows.length >= PAGE_SIZE;
    userList.value = userList.value.concat(rows);
  }).finally(() => {
    // 过期请求不解除加载态，避免打断有新请求时的加载提示
    if (seq === requestSeq) {
      loading.value = false;
    }
    loaded.value = true;
  });
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

/**
 * 返回上一页
 */
function goBack() {
  uni.navigateBack()
}
</script>
<style scoped lang="scss">
.contact-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background-color: $uni-bg-color-dashboard;
}

.contact-header {
  flex-shrink: 0;
  padding: 16px 16px;
  background-color: #0052cc;
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #ffffff;
  }
}

.contact-search {
  flex-shrink: 0;
  width: 94%;
  margin: 12px auto 0;
}

.contact-list {
  flex: 1;
  min-height: 0;
  width: 94%;
  margin: 0 auto;
  padding-top: 12px;
  box-sizing: border-box;
}

.loading-box, .empty-box {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 120px;
}

.list-tip {
  padding: 12px 0 30px;
  text-align: center;
  font-size: 12px;
  color: #999;
}

.user-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title-row {
  display: flex;
  align-items: center;
  flex: 1;
  gap: 12px;
}


.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: #333333;
}

/* 直属部门名称 */
.user-dept {
  font-size: 12px;
  color: #999999;
}

.badge-noread {
  background-color: #e74c3c;
  border-radius: 10px;
  padding: 0 6px;
  min-width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;

  .badge-text {
    font-size: 11px;
    color: #fff;
    font-weight: 500;
  }
}
</style>

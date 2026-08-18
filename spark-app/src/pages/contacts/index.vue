<template>
  <view class="contacts-container safe-area-page">
    <!-- 顶部标题栏 -->
    <view class="contacts-header">
      <text class="header-title">通讯录</text>
    </view>

    <!-- 用户列表 -->
    <view class="contacts-list">
      <!-- 加载中 -->
      <view v-if="loading" class="loading-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <!-- 空状态 -->
      <view v-else-if="userList.length === 0" class="empty-box">
        <up-empty text="暂无用户" icon="man-add-fill"/>
      </view>

      <!-- 用户卡片 -->
      <view v-else class="user-card" v-for="item in userList" :key="item.id" @click="handleUserClick(item)">
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
            </view>
          </view>
          <up-icon name="arrow-right" size="16" color="#b3b3b3"></up-icon>
        </view>
      </view>
    </view>

    <up-tabbar :value="active" @change="handleOnTabChange" activeColor="#0052cc">
      <up-tabbar-item name="home" icon="home-fill" text="首页"/>
      <up-tabbar-item name="todo" icon="order" text="待办"/>
      <up-tabbar-item name="contacts" icon="man-add-fill" text="通讯录"/>
      <up-tabbar-item name="agent" icon="grid-fill" text="智能体"/>
      <up-tabbar-item name="message" icon="chat-fill" text="通知"/>
      <up-tabbar-item name="my" icon="account" text="我的"/>
    </up-tabbar>
  </view>
</template>
<script setup>
import {ref} from "vue";
import {userMyListAPI} from "@/api/chat/user";
import UserAvatar from '@/components/UserAvatar/index.vue'

const active = ref("contacts");
const userList = ref([]);
const loading = ref(true);
getUserList();

/**
 * 获取用户列表
 */
function getUserList() {
  loading.value = true;
  userMyListAPI({page: false}).then(res => {
    if (res.code !== 200) {
      return;
    }
    if (res.data === undefined || res.data === null) {
      return;
    }
    userList.value = res.data || [];
  }).finally(() => {
    loading.value = false;
  });
}

function handleUserClick(item) {
  const index = userList.value.findIndex(u => u.id === item.id)
  uni.navigateTo({
    url: '/pages/chat/index',
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
 * 底部导航栏
 * @param index
 */
function handleOnTabChange(index) {
  uni.reLaunch({
    url: '/pages/'+index+'/index'
  })
}
</script>
<style scoped lang="scss">
.contacts-container {
  height: 100vh;
  overflow: hidden;
  background-color: $uni-bg-color-dashboard;
}

.contacts-header {
  padding: 10px 16px;
  background-color: #0052cc;

  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #ffffff;
  }
}

.contacts-list {
  width: 94%;
  margin: 0 auto;
  padding-top: 12px;
  /* #ifdef H5 */
  height: calc(100vh - 120px);
  /* #endif */
  /* #ifdef MP-WEIXIN */
  height: calc(100vh - 200px);
  /* #endif */
  overflow-y: auto;
}

.loading-box, .empty-box {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 120px;
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


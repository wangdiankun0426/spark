<template>
  <view class="message-container safe-area-page">
    <!-- 顶部标题栏 -->
    <view class="message-header">
      <text class="header-title">系统通知</text>
    </view>

    <!-- 消息列表 -->
    <view class="message-list">
      <!-- 加载中 -->
      <view v-if="loading" class="loading-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <!-- 空状态 -->
      <view v-else-if="messageList.length === 0" class="empty-box">
        <up-empty text="暂无消息" icon="chat-fill"/>
      </view>

      <!-- 消息卡片 -->
      <view v-else class="message-card" v-for="item in messageList" :key="item.id">
        <view class="card-header">
          <view class="card-title-row">
            <text class="card-title">{{ item.title }}</text>
          </view>
          <text class="card-time">{{ item.createdDt }}</text>
        </view>
        <view class="card-divider"></view>
        <view class="card-content">
          <text class="content-text">{{ item.content }}</text>
        </view>
      </view>
    </view>

    <up-tabbar :value="active" @change="handleOnTabChange" activeColor="#0052cc">
      <up-tabbar-item name="home" icon="home-fill" text="首页"/>
      <up-tabbar-item name="contacts" icon="man-add-fill" text="通讯录"/>
      <up-tabbar-item name="agent" icon="grid-fill" text="智能体"/>
      <up-tabbar-item name="message" icon="chat-fill" text="通知"/>
      <up-tabbar-item name="my" icon="account" text="我的"/>
    </up-tabbar>
  </view>
</template>
<script setup>
import {ref} from "vue";
import {myMessageListAPI} from "@/api/system/message";

const active = ref("message");
const messageList = ref([]);
const loading = ref(true);

getMyMessageList();

/**
 * 获取用户消息列表
 */
function getMyMessageList() {
  loading.value = true;
  myMessageListAPI().then(res => {
    if (res.code !== 200) {
      return;
    }
    if (res.data === undefined || res.data === null) {
      return;
    }
    messageList.value = res.data;
  }).finally(() => {
    loading.value = false;
  });
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
.message-container {
  height: 100vh;
  overflow: hidden;
  background-color: $uni-bg-color-dashboard;
}

.message-header {
  padding: 10px 16px;
  background-color: #0052cc;

  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #ffffff;
  }
}

.message-list {
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

.message-card {
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
  padding-bottom: 12px;
}

.card-title-row {
  display: flex;
  align-items: center;
  flex: 1;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333333;
}

.card-time {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
  margin-left: 12px;
}

.card-divider {
  height: 1px;
  background-color: #f0f2f5;
  margin-bottom: 12px;
}

.card-content {
  .content-text {
    font-size: 14px;
    color: #5a5a5a;
    line-height: 1.7;
    word-break: break-all;
  }
}
</style>


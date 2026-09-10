<template>
  <view class="message-container safe-area-page">
    <!-- 顶部标题栏 -->
    <view class="message-header">
      <text class="header-title">消息</text>
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
      <view
          v-else
          class="message-card"
          :class="{'message-card-link': isFlowMessage(item)}"
          v-for="item in messageList"
          :key="item.id"
          @click="handleMsgClick(item)"
      >
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
        <!-- 流程类消息点击跳转流程详情 -->
        <view v-if="isFlowMessage(item)" class="card-link">
          <text class="link-text">详情</text>
          <up-icon name="arrow-right" size="12" color="#0052cc"/>
        </view>
      </view>
    </view>

    <up-tabbar :value="active" @change="handleOnTabChange" activeColor="#0052cc">
      <up-tabbar-item
          v-for="tab in tabBarItems"
          :key="tab.name"
          :name="tab.name"
          :icon="tab.icon"
          :text="tab.text"
      />
    </up-tabbar>
  </view>
</template>
<script setup>
import {ref, computed} from "vue";
import {visibleTabs} from "@/utils/menuUtil";
import {myMessageListAPI} from "@/api/sys/message";

const active = ref("message");

// 按菜单权限过滤后的底部导航项（随用户菜单权限响应式更新）
const tabBarItems = computed(() => visibleTabs());

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

// 流程类消息类型：2-待办 3-完结 4-驳回 5-催办
const FLOW_MSG_TYPES = [2, 3, 4, 5];
// 流程类消息跳转详情页类型：待办/催办通知给审批人（我的待办），完结/驳回通知给申请人（我的申请）
const FLOW_DETAIL_TYPE_MAP = {2: 3, 3: 1, 4: 1, 5: 3};

/**
 * 是否为可跳转的流程类消息
 * @param item 消息行数据
 */
function isFlowMessage(item) {
  return FLOW_MSG_TYPES.indexOf(item.type) !== -1 && item.refId !== undefined && item.refId !== null;
}

/**
 * 点击消息卡片跳转流程详情
 * @param item 消息行数据
 */
function handleMsgClick(item) {
  if (!isFlowMessage(item)) {
    return;
  }
  uni.navigateTo({
    url: '/pages/flow/instanceDetail?id=' + item.refId + '&type=' + FLOW_DETAIL_TYPE_MAP[item.type]
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
  box-shadow: 0 2px 8px rgba(23, 43, 77, 0.08);

  &:active {
    background-color: #f5f7fa;
  }
}

.message-card-link {
  cursor: pointer;
}

.card-link {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 2px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #ebeef5;

  .link-text {
    font-size: 12px;
    color: #0052cc;
  }
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


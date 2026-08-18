<template>
  <view class="agent-container safe-area-page">
    <!-- 顶部标题栏 -->
    <view class="agent-header">
      <text class="header-title">智能体</text>
    </view>

    <!-- 智能体列表 -->
    <view class="agent-list">
      <!-- 加载中 -->
      <view v-if="loading" class="loading-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <!-- 空状态 -->
      <view v-else-if="agentList.length === 0" class="empty-box">
        <up-empty text="暂无智能体" icon="grid-fill"/>
      </view>

      <!-- 智能体卡片 -->
      <view v-else class="agent-card" v-for="item in agentList" :key="item.id" @click="handleAgentClick(item)">
        <view class="card-header">
          <view class="card-title-row">
            <user-avatar type="agent" :name="item.name" :size="48" shape="rounded"/>
            <view class="agent-info">
              <text class="agent-name">{{ item.name }}</text>
              <view class="agent-status">
                <view class="status-dot" :class="item.status === 1 ? 'status-on' : 'status-off'"/>
                <text class="status-text">{{ item.statusName }}</text>
              </view>
            </view>
          </view>
          <up-icon name="arrow-right" size="16" color="#b3b3b3"></up-icon>
        </view>
        <view class="card-divider"></view>
        <view class="card-content">
          <view class="info-row">
            <text class="info-label">模型：</text>
            <text class="info-value">{{ item.chatModelName }}</text>
          </view>
          <view class="info-row" v-if="item.kbNames && item.kbNames !== '无'">
            <text class="info-label">知识库：</text>
            <text class="info-value">{{ item.kbNames }}</text>
          </view>
          <view class="info-row" v-if="item.graphNames && item.graphNames !== '无'">
            <text class="info-label">知识图谱：</text>
            <text class="info-value">{{ item.graphNames }}</text>
          </view>
          <view class="info-row" v-if="item.tools">
            <text class="info-label">工具：</text>
            <text class="info-value">{{ item.toolNames || item.tools }}</text>
          </view>
        </view>
        <view class="card-footer">
          <up-icon name="clock" size="12" color="#b3b3b3"></up-icon>
          <text class="create-time">{{ item.createdDt }}</text>
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
import {ref, getCurrentInstance} from "vue";
import {agentPageListAPI} from "@/api/llm/agent";
import UserAvatar from '@/components/UserAvatar/index.vue'

const active = ref("agent");
const agentList = ref([]);
const loading = ref(true);
const { proxy } = getCurrentInstance()

getAgentList();

/**
 * 获取智能体列表
 */
function getAgentList() {
  loading.value = true;
  agentPageListAPI({page: false}).then(res => {
    if (res.code !== 200) {
      return;
    }
    if (res.data === undefined || res.data === null) {
      return;
    }
    agentList.value = res.data.rows || [];
  }).finally(() => {
    loading.value = false;
  });
}

function handleAgentClick(item) {
  const index = agentList.value.findIndex(a => a.id === item.id)
  uni.navigateTo({
    url: '/pages/chat/index',
    events: {
      'space-created': function(data) {
        if (data.targetId === item.id && index !== -1) {
          agentList.value[index].chatSpaceId = data.spaceId
        }
      }
    },
    success: function(res) {
      res.eventChannel.emit('setTarget', {
        target: item,
        targetType: 'agent'
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
.agent-container {
  height: 100vh;
  overflow: hidden;
  background-color: $uni-bg-color-dashboard;
}

.agent-header {
  padding: 10px 16px;
  background-color: #0052cc;

  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #ffffff;
  }
}

.agent-list {
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

.agent-card {
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
  gap: 12px;
}


.agent-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.agent-name {
  font-size: 16px;
  font-weight: 600;
  color: #333333;
}

.agent-status {
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;

  &.status-on {
    background-color: #52c41a;
  }
  &.status-off {
    background-color: #d9d9d9;
  }
}

.status-text {
  font-size: 12px;
  color: #999;
}

.card-divider {
  height: 1px;
  background-color: #f0f2f5;
  margin-bottom: 12px;
}

.card-content {
  padding-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: center;
}

.info-label {
  font-size: 13px;
  color: #999;
  flex-shrink: 0;
}

.info-value {
  font-size: 13px;
  color: #5a5a5a;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 4px;

  .create-time {
    font-size: 12px;
    color: #b3b3b3;
  }
}
</style>


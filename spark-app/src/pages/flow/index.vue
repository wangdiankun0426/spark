<template>
  <view class="flow-container safe-area-page">
    <!-- 顶部标题栏 -->
    <view class="flow-header">
      <text class="header-title">流程</text>
    </view>

    <!-- 分类页签 -->
    <view class="flow-tabs">
      <up-tabs
          :list="tabList"
          :current="tabIndex"
          lineColor="#0052cc"
          :scrollable="false"
          @change="handleTabChange"/>
    </view>

    <!-- 流程实例列表 -->
    <scroll-view
        class="flow-list"
        scroll-y
        @scrolltolower="handleLoadMore"
    >
      <!-- 加载中 -->
      <view v-if="loading && instanceList.length === 0" class="loading-box">
        <up-loading-icon text="加载中..."/>
      </view>

      <!-- 空状态 -->
      <view v-else-if="instanceList.length === 0" class="empty-box">
        <up-empty text="暂无流程" icon="list"/>
      </view>

      <!-- 实例卡片 -->
      <template v-else>
        <view
            class="instance-card"
            v-for="item in instanceList"
            :key="item.id"
            @click="handleInstanceClick(item)"
        >
          <view class="card-title-row">
            <text class="instance-name">{{ item.instanceName || item.name }}</text>
            <text class="instance-status" :class="'status-' + item.status">{{ item.statusName }}</text>
          </view>
          <view class="card-info-row">
            <text class="info-label">紧急程度</text>
            <text class="info-value">{{ levelText(item.level) }}</text>
          </view>
          <view class="card-info-row">
            <text class="info-label">{{ tabIndex === 3 ? '抄送人' : '申请人' }}</text>
            <text class="info-value">{{ item.createdByName }}</text>
          </view>
          <view class="card-info-row">
            <text class="info-label">{{ tabIndex === 3 ? '抄送时间' : '申请时间' }}</text>
            <text class="info-value">{{ item.createdDt }}</text>
          </view>
        </view>
        <up-loadmore :status="loadStatus"/>
      </template>
    </scroll-view>

    <up-tabbar :value="active" @change="handleOnTabChange" activeColor="#0052cc">
      <up-tabbar-item name="home" icon="home-fill" text="首页"/>
      <up-tabbar-item name="flow" icon="order" text="流程"/>
      <up-tabbar-item name="llm" icon="grid-fill" text="AI+"/>
      <up-tabbar-item name="message" icon="chat-fill" text="消息"/>
      <up-tabbar-item name="my" icon="account" text="我的"/>
    </up-tabbar>
  </view>
</template>
<script setup>
import {ref} from "vue";
import {onShow} from "@dcloudio/uni-app";
import {
  pageMyApplicationListAPI,
  pageMyTodoListAPI,
  pageMyDoneListAPI,
  pageCopyMyListAPI
} from "@/api/flow/instance";

const active = ref("flow");
const tabList = [
  {name: '我的申请'},
  {name: '我的待办'},
  {name: '我的已办'},
  {name: '抄送给我'}
];
const tabIndex = ref(0);
const instanceList = ref([]);
const loading = ref(false);
const loadStatus = ref('loadmore');
const query = ref({pageNo: 1, pageSize: 15});
const total = ref(0);
// 页签对应的列表接口：1 我的申请 3 我的待办 4 我的已办 5 抄送给我
const tabApiList = [pageMyApplicationListAPI, pageMyTodoListAPI, pageMyDoneListAPI, pageCopyMyListAPI];
// 页签对应的详情类型（详情页按类型展示操作按钮，5 抄送只读）
const tabTypeList = [1, 3, 4, 5];

/**
 * 紧急程度文案
 * @param level 紧急程度
 */
function levelText(level) {
  const levelMap = {1: '一般', 2: '重要', 3: '紧急'};
  return levelMap[level] || '一般';
}

/**
 * 查询当前页签列表
 */
function handleGetInstanceList() {
  const api = tabApiList[tabIndex.value];
  if (!api) {
    return;
  }
  loading.value = true;
  loadStatus.value = 'loading';
  api(query.value).then(res => {
    if (res.code === 200 && res.data) {
      const rows = res.data.rows || [];
      instanceList.value = query.value.pageNo === 1 ? rows : instanceList.value.concat(rows);
      total.value = res.data.total;
      loadStatus.value = instanceList.value.length >= total.value ? 'nomore' : 'loadmore';
    } else {
      instanceList.value = query.value.pageNo === 1 ? [] : instanceList.value;
      loadStatus.value = 'nomore';
    }
  }).finally(() => {
    loading.value = false;
  });
}

/**
 * 切换页签
 * @param tab uview-plus tabs change 事件参数（{...页签项, index}，index 为下标）
 */
function handleTabChange(tab) {
  const index = tab.index;
  tabIndex.value = index;
  query.value.pageNo = 1;
  instanceList.value = [];
  handleGetInstanceList();
}

/**
 * 触底加载更多
 */
function handleLoadMore() {
  if (loading.value || loadStatus.value === 'nomore') {
    return;
  }
  query.value.pageNo = query.value.pageNo + 1;
  handleGetInstanceList();
}

/**
 * 从详情页返回时刷新当前页签（审批后状态变化）
 */
onShow(() => {
  query.value.pageNo = 1;
  instanceList.value = [];
  handleGetInstanceList();
});

/**
 * 点击流程实例进入详情
 * @param item 行数据
 */
function handleInstanceClick(item) {
  uni.navigateTo({
    url: '/pages/flow/instanceDetail?id=' + (item.instanceId || item.id) + '&type=' + tabTypeList[tabIndex.value]
  });
}

/**
 * 底部导航栏
 * @param index
 */
function handleOnTabChange(index) {
  uni.reLaunch({
    url: '/pages/' + index + '/index'
  });
}
</script>
<style scoped lang="scss">
.flow-container {
  height: 100vh;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: $uni-bg-color-dashboard;
}

.flow-container :deep(.u-tabbar) {
  flex: none;
}

.flow-header {
  padding: 10px 16px;
  background-color: #0052cc;

  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #ffffff;
  }
}

.flow-tabs {
  background-color: #ffffff;
}

.flow-list {
  width: 94%;
  margin: 0 auto;
  padding-top: 12px;
  padding-bottom: 12px;
  /* #ifdef H5 */
  height: calc(100vh - 120px);
  /* #endif */
  /* #ifdef MP-WEIXIN */
  height: calc(100vh - 200px);
  /* #endif */
  overflow-y: auto;
}

.loading-box,
.empty-box {
  display: flex;
  justify-content: center;
  padding-top: 60px;
}

.instance-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;

  .card-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .instance-name {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .instance-status {
      flex-shrink: 0;
      margin-left: 8px;
      font-size: 12px;
    }
  }

  .card-info-row {
    display: flex;
    align-items: center;
    font-size: 13px;

    .info-label {
      color: #909399;
      width: 70px;
      flex-shrink: 0;
    }

    .info-value {
      color: #606266;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

// 状态着色：审批中蓝、通过绿、驳回红、撤回橙、转办/加签灰
.status-1,
.status-6,
.status-7,
.status-8 {
  color: #909399;
}

.status-2 {
  color: #0052cc;
}

.status-3,
.status-5 {
  color: #52c41a;
}

.status-4 {
  color: #f56c6c;
}

.status-9 {
  color: #e6a23c;
}
</style>

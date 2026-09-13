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
        <up-empty :text="activeTab ? '暂无流程' : '暂无可用功能'" icon="list"/>
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
            <text class="info-label">流程类型</text>
            <text class="info-value">{{ item.typeName }}</text>
          </view>
          <view class="card-info-row">
            <text class="info-label">紧急程度</text>
            <text class="info-value">{{ item.levelName }}</text>
          </view>
          <view class="card-info-row">
            <text class="info-label">{{ isCopyTab ? '抄送人' : '申请人' }}</text>
            <text class="info-value">{{ item.createdByName }}</text>
          </view>
          <view class="card-info-row">
            <text class="info-label">{{ isCopyTab ? '抄送时间' : '申请时间' }}</text>
            <text class="info-value">{{ item.createdDt }}</text>
          </view>
        </view>
        <up-loadmore :status="loadStatus" class="list-loadmore"/>
      </template>
    </scroll-view>

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
import {onLoad} from "@dcloudio/uni-app";
import {visibleTabs} from "@/utils/menuUtil";
import {
  pageMyApplicationListAPI,
  pageMyTodoListAPI,
  pageMyDoneListAPI,
  pageCopyMyListAPI
} from "@/api/flow/instance";

const active = ref("flow");

// 按菜单权限过滤后的底部导航项
const tabBarItems = computed(() => visibleTabs());
// 流程中心页签
const visibleTabList = [
  {name: '我的申请', api: pageMyApplicationListAPI, type: 1},
  {name: '我的待办', api: pageMyTodoListAPI, type: 3},
  {name: '我的已办', api: pageMyDoneListAPI, type: 4},
  {name: '抄送给我', api: pageCopyMyListAPI, type: 5}
];
const tabList = computed(() => visibleTabList.map(cfg => ({name: cfg.name})));
const tabIndex = ref(0);
// 当前激活页签配置
const activeTab = computed(() => visibleTabList[tabIndex.value]);
const isCopyTab = computed(() => (activeTab.value ? activeTab.value.type === 5 : false));
const instanceList = ref([]);
const loading = ref(false);
const loadStatus = ref('loadmore');
const query = ref({pageNo: 1, pageSize: 15});
const total = ref(0);

/**
 * 页面加载：支持从首页带 tab 参数直达指定页签（下标 0 申请 / 1 待办 / 2 已办 / 3 抄送），
 * 同时补上首屏列表加载（原先只有切换页签才请求，首次进入列表是空的）
 * @param options 页面参数
 */
onLoad(options => {
  const index = Number(options && options.tab);
  if (!Number.isNaN(index) && index >= 0 && index < visibleTabList.length) {
    tabIndex.value = index;
  }
  handleGetInstanceList();
});

/**
 * 查询当前页签列表
 */
function handleGetInstanceList() {
  const tab = activeTab.value;
  if (!tab) {
    instanceList.value = [];
    loadStatus.value = 'nomore';
    return;
  }
  loading.value = true;
  loadStatus.value = 'loading';
  tab.api(query.value).then(res => {
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
 * 点击流程实例进入详情
 * @param item 行数据
 */
function handleInstanceClick(item) {
  uni.navigateTo({
    url: '/pages/flow/instanceDetail?id=' + (item.instanceId || item.id) + '&type=' + (activeTab.value ? activeTab.value.type : 1)
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
  padding: 16px 16px;
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
  /* #ifdef H5 */
  height: calc(100vh - 120px);
  /* #endif */
  /* #ifdef MP-WEIXIN */
  height: calc(100vh - 200px);
  /* #endif */
  overflow-y: auto;
}

.list-loadmore {
  padding-bottom: 30px;
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
  box-shadow: 0 2px 8px rgba(23, 43, 77, 0.08);
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

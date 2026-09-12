<template>
  <up-popup
      class="session-picker"
      :show="show"
      mode="bottom"
      round="12"
      @close="handleClose"
  >
    <view class="picker-body">
      <!-- 标题 -->
      <view class="picker-header">
        <text class="picker-title">{{ title }}</text>
        <view class="picker-close" @click="handleClose">
          <up-icon name="close" size="18" color="#999"></up-icon>
        </view>
      </view>

      <!-- 会话检索 -->
      <view class="picker-search">
        <up-search
            v-model="keyword"
            placeholder="搜索会话名称"
            :showAction="false"
            :height="46"
            bgColor="#f5f7fa"
        />
      </view>

      <!-- 开启新对话 -->
      <view class="picker-new" @click="handleNew">
        <up-icon name="plus" size="16" color="#0052cc"></up-icon>
        <text class="picker-new-text">开启新对话</text>
      </view>

      <!-- 会话列表：滚动到底部加载下一页 -->
      <scroll-view scroll-y="true" class="picker-list" @scrolltolower="loadMore">
        <view v-if="loading && sessionList.length === 0" class="picker-tip">
          <up-loading-icon text="加载中..."/>
        </view>

        <view v-else-if="sessionList.length === 0" class="picker-tip">
          <up-empty :text="keyword ? '未找到匹配的会话' : '暂无会话记录'" icon="chat"/>
        </view>

        <template v-else>
          <view
              class="picker-item"
              v-for="item in sessionList"
              :key="item.spaceId"
              @click="handleSelect(item)"
          >
            <view class="picker-item-main">
              <view class="picker-item-title">{{ item.title || '新对话' }}</view>
              <view class="picker-item-sub">
                <text v-if="item.modelName">{{ item.modelName }}</text>
                <text v-if="formatTime(item.createdDt)">{{ formatTime(item.createdDt) }}</text>
              </view>
            </view>
            <up-icon name="arrow-right" size="14" color="#c8c8c8"></up-icon>
          </view>
          <view class="picker-more">{{ loading ? '正在加载...' : (hasMore ? '上拉加载更多' : '没有更多会话了') }}</view>
        </template>
      </scroll-view>
    </view>
  </up-popup>
</template>

<script setup>
import {ref, watch} from "vue";
import {pageMyChatSpaceListAPI} from "@/api/chat/space";
import {useDebounceFn} from "@/utils/debounce";

// 会话类型，对应后端 ChatSpaceTypeEnum 的取值
const SPACE_TYPE = {agent: 3, model: 2}
// 每页加载的会话数量
const PAGE_SIZE = 20

const props = defineProps({
  show: {type: Boolean, default: false},
  // 会话类型：agent=智能体，model=模型
  targetType: {type: String, default: 'agent'},
  // 对话对象id，只查与该智能体/模型的会话
  receiverId: {type: [String, Number], default: null},
  // 弹层标题
  title: {type: String, default: '会话记录'}
});
const emit = defineEmits(['update:show', 'select', 'new']);

// 会话名称检索关键字
const keyword = ref("");
const sessionList = ref([]);
const loading = ref(false);
// 是否还有下一页
const hasMore = ref(true);
// 已加载到的页码
let pageNo = 0;
// 请求序号，用于丢弃过期响应（检索期间连续输入时）
let requestSeq = 0;

// 关键字变化后防抖检索（走后端）
const searchSessions = useDebounceFn(() => loadSessions(true), 500);

watch(keyword, () => {
  searchSessions();
});

// 弹层打开时重置检索条件并加载会话记录
watch(() => props.show, (value) => {
  if (!value) {
    return;
  }
  keyword.value = "";
  loadSessions(true);
});

/**
 * 加载会话记录，检索条件变化时从头加载
 * @param reset 是否重置为第一页，仅显式传 true 才重置（避免事件对象被当成参数）
 */
function loadSessions(reset) {
  if (reset === true) {
    sessionList.value = [];
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
  pageMyChatSpaceListAPI({
    spaceType: SPACE_TYPE[props.targetType] || SPACE_TYPE.agent,
    receiverId: props.receiverId,
    pageNo: nextPage,
    pageSize: PAGE_SIZE,
    title: keyword.value.trim() || undefined
  }).then(res => {
    if (seq !== requestSeq || res.code !== 200) {
      return;
    }
    const rows = (res.data && res.data.rows) || [];
    pageNo = nextPage;
    hasMore.value = rows.length >= PAGE_SIZE;
    sessionList.value = sessionList.value.concat(rows);
  }).finally(() => {
    // 过期请求不解除加载态，避免打断有新请求时的加载提示
    if (seq === requestSeq) {
      loading.value = false;
    }
  });
}

/**
 * 滚动到底部加载下一页
 */
function loadMore() {
  loadSessions();
}

/**
 * 格式化会话创建时间，兼容时间戳与字符串
 * @param value 时间
 * @returns 展示文案，形如 09-13 10:20
 */
function formatTime(value) {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  if (isNaN(date.getTime())) {
    return "";
  }
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');
  return month + '-' + day + ' ' + hour + ':' + minute;
}

function handleClose() {
  emit('update:show', false);
}

/**
 * 选择已有会话
 * @param item 会话
 */
function handleSelect(item) {
  emit('update:show', false);
  emit('select', item);
}

/**
 * 开启新对话
 */
function handleNew() {
  emit('update:show', false);
  emit('new');
}
</script>

<style scoped lang="scss">
/* uview-plus 弹窗组件根节点自带 flex:1，防止在纵向 flex 布局页面中抢占高度 */
.session-picker {
  flex: none;
}

.picker-body {
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.picker-title {
  font-size: 16px;
  font-weight: 600;
  color: #333333;
}

.picker-close {
  padding: 4px;
}

.picker-search {
  background-color: #ffffff;
}

/* 开启新对话 */
.picker-new {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 40px;
  border-radius: 8px;
  background-color: #f0f6ff;
}

.picker-new-text {
  font-size: 14px;
  font-weight: 500;
  color: #0052cc;
}

.picker-list {
  height: 360px;
}

.picker-tip {
  display: flex;
  justify-content: center;
  padding-top: 60px;
}

/* 分页加载提示 */
.picker-more {
  padding: 12px 0 4px;
  text-align: center;
  font-size: 12px;
  color: #999999;
}

.picker-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px;
  border-radius: 6px;

  &:active {
    background-color: #f5f7fa;
  }
}

.picker-item-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.picker-item-title {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.picker-item-sub {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #999999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

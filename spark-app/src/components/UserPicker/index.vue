<template>
  <up-popup
      class="user-picker"
      :show="show"
      mode="bottom"
      round="12"
      @close="handleClose"
  >
    <view class="picker-body">
      <!-- 姓名搜索 -->
      <view class="picker-search">
        <up-search
            v-model="keyword"
            placeholder="搜索姓名"
            :showAction="false"
            bgColor="#f5f7fa"
        />
      </view>

      <!-- 用户列表 -->
      <scroll-view class="picker-list" scroll-y @scrolltolower="handleLoadMore">
        <!-- 加载中 -->
        <view v-if="userLoading && userList.length === 0" class="picker-tip">
          <up-loading-icon text="加载中..."/>
        </view>

        <!-- 空状态 -->
        <view v-else-if="userList.length === 0" class="picker-tip">
          <up-empty text="暂无用户" icon="man-add-fill"/>
        </view>

        <!-- 用户行 -->
        <template v-else>
          <view
              class="picker-item"
              v-for="user in userList"
              :key="user.id"
              :class="{ 'picker-item--active': selectedIds.includes(user.id) }"
              @click="handleToggleUser(user)"
          >
            <view class="picker-user">
              <user-avatar type="user" :userId="user.id" :name="user.name" :size="36"/>
              <text class="picker-name">{{ user.name }}</text>
            </view>
            <up-icon v-if="selectedIds.includes(user.id)" name="checkmark" size="16" color="#0052cc"/>
          </view>
          <up-loadmore :status="loadStatus"/>
        </template>
      </scroll-view>

      <up-button type="primary" text="确定" :loading="loading" @click="handleConfirm"/>
    </view>
  </up-popup>
</template>
<script setup>
import {computed, ref, watch} from "vue";
import {pageUserListAPI} from "@/api/sys/user";
import {toast} from "uview-plus";
import UserAvatar from '@/components/UserAvatar/index.vue'

// 每页条数
const PAGE_SIZE = 20;
// 检索防抖时长（毫秒）
const SEARCH_DELAY = 300;

const props = defineProps({
  // 是否展示弹层
  show: {
    type: Boolean,
    default: false
  },
  // 弹层标题
  title: {
    type: String,
    default: '选择用户'
  },
  // 是否多选
  multiple: {
    type: Boolean,
    default: false
  },
  // 确定按钮加载状态
  loading: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:show', 'confirm']);

const keyword = ref('');
const userList = ref([]);
const userLoading = ref(false);
const loadStatus = ref('loadmore');
const pageNo = ref(1);
const total = ref(0);
const selectedIds = ref([]);
// 已选用户对象缓存（id -> user），跨页/跨检索保留已选中项
const selectedUserMap = ref({});
// 检索防抖定时器
let searchTimer = null;

// 弹层打开时重置选择与检索条件并加载首页
watch(() => props.show, (value) => {
  if (!value) {
    return;
  }
  keyword.value = '';
  selectedIds.value = [];
  selectedUserMap.value = {};
  resetAndLoad();
});

// 姓名检索防抖走接口
watch(keyword, () => {
  if (!props.show) {
    return;
  }
  clearTimeout(searchTimer);
  searchTimer = setTimeout(resetAndLoad, SEARCH_DELAY);
});

/**
 * 重置分页并加载第一页
 */
function resetAndLoad() {
  pageNo.value = 1;
  userList.value = [];
  loadStatus.value = 'loadmore';
  handleLoadUserList();
}

/**
 * 分页加载用户列表（系统用户，按姓名检索）
 */
function handleLoadUserList() {
  userLoading.value = true;
  loadStatus.value = 'loading';
  pageUserListAPI({
    page: true,
    pageNo: pageNo.value,
    pageSize: PAGE_SIZE,
    name: keyword.value.trim() || undefined
  }).then(res => {
    if (res.code === 200 && res.data) {
      const rows = res.data.rows || [];
      userList.value = pageNo.value === 1 ? rows : userList.value.concat(rows);
      total.value = res.data.total;
      loadStatus.value = userList.value.length >= total.value ? 'nomore' : 'loadmore';
    } else {
      userList.value = pageNo.value === 1 ? [] : userList.value;
      loadStatus.value = 'nomore';
    }
  }).finally(() => {
    userLoading.value = false;
  });
}

/**
 * 触底加载更多
 */
function handleLoadMore() {
  if (userLoading.value || loadStatus.value === 'nomore') {
    return;
  }
  pageNo.value = pageNo.value + 1;
  handleLoadUserList();
}

/**
 * 已选中的用户对象列表
 */
const selectedUsers = computed(() => {
  return selectedIds.value.map(id => selectedUserMap.value[id]).filter(Boolean);
});

/**
 * 切换选中用户（单选覆盖、多选增删）
 * @param user 用户对象
 */
function handleToggleUser(user) {
  if (!props.multiple) {
    selectedIds.value = [user.id];
    selectedUserMap.value = {[user.id]: user};
    return;
  }
  const index = selectedIds.value.indexOf(user.id);
  if (index === -1) {
    selectedIds.value.push(user.id);
    selectedUserMap.value[user.id] = user;
  } else {
    selectedIds.value.splice(index, 1);
    delete selectedUserMap.value[user.id];
  }
}

/**
 * 确认选择，向外抛出选中的用户对象
 */
function handleConfirm() {
  if (selectedIds.value.length === 0) {
    toast(props.multiple ? '请选择用户' : '请选择' + props.title.replace('选择', ''));
    return;
  }
  emit('confirm', selectedUsers.value);
}

/**
 * 关闭弹层
 */
function handleClose() {
  emit('update:show', false);
}
</script>
<style scoped lang="scss">
/* uview-plus 弹窗组件根节点自带 flex:1，防止在纵向 flex 布局页面中抢占高度 */
.user-picker {
  flex: none;
}

.picker-body {
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;

  .picker-search {
    background-color: #ffffff;
  }

  .picker-list {
    height: 320px;
  }

  .picker-tip {
    display: flex;
    justify-content: center;
    padding-top: 60px;
  }

  .picker-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px;
    border-radius: 6px;

    .picker-user {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .picker-name {
      font-size: 14px;
      color: #303133;
    }

    &.picker-item--active {
      background-color: #f0f6ff;
    }
  }
}
</style>

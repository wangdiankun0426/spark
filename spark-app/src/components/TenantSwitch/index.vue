<template>
  <up-popup
      class="tenant-switch-wrap"
      :show="show"
      mode="bottom"
      round="12"
      @close="handleClose"
  >
    <view class="tenant-switch-popup">
      <view class="tenant-popup-header">
        <text class="tenant-popup-title">切换租户</text>
        <up-icon name="close" size="18" color="#909399" @click="handleClose"/>
      </view>

      <!-- 加载中 -->
      <view v-if="tenantLoading && tenantList.length === 0" class="tenant-popup-tip">
        <up-loading-icon text="加载中..."/>
      </view>

      <!-- 空状态 -->
      <view v-else-if="tenantList.length === 0" class="tenant-popup-tip">
        <up-empty text="暂无可切换的租户"/>
      </view>

      <!-- 租户列表 -->
      <scroll-view v-else class="tenant-popup-list" scroll-y>
        <view
            v-for="item in tenantList"
            :key="item.id"
            class="tenant-item"
            :class="{ 'is-current': isCurrentTenant(item.id) }"
            @click="handleTenantClick(item)"
        >
          <view class="tenant-info">
            <text class="tenant-name">{{ item.name }}</text>
            <text class="tenant-meta">截止时间：{{ item.deadline || '不限' }}</text>
          </view>
          <view v-if="isCurrentTenant(item.id)" class="tenant-state">
            <up-tag type="primary" size="mini" plain>当前租户</up-tag>
          </view>
          <view v-else class="tenant-state">
            <up-loading-icon v-if="switchingTenantId === item.id" size="16" color="#0052cc"/>
            <up-icon v-else name="arrow-right" size="16" color="#0052cc"/>
          </view>
        </view>
      </scroll-view>
    </view>
  </up-popup>
</template>
<script setup>
import {ref, watch} from "vue";
import {useStore} from "vuex";
import {getSessionAPI} from "@/api/auth/login";
import {queryMyTenantListAPI, switchTenantAPI} from "@/api/sys/tenant";
import {toast} from "uview-plus"

const props = defineProps({
  // 弹层显隐
  show: {type: Boolean, default: false},
  // 当前默认租户id
  currentTenantId: {type: [Number, String], default: undefined}
});

const emit = defineEmits(['update:show']);

const store = useStore();
// 当前用户已加入且可用的租户列表
const tenantList = ref([]);
// 租户列表加载状态
const tenantLoading = ref(false);
// 正在切换中的租户id，用于行内loading
const switchingTenantId = ref(null);

// 弹层展开时加载租户列表
watch(() => props.show, (value) => {
  if (!value) {
    return;
  }
  loadTenantList();
});

/**
 * 加载当前用户已加入且可用的租户列表
 */
function loadTenantList() {
  tenantLoading.value = true;
  queryMyTenantListAPI().then(res => {
    if (res.code !== 200) {
      return;
    }
    tenantList.value = res.data || [];
  }).finally(() => {
    tenantLoading.value = false;
  });
}

/**
 * 是否为当前默认租户
 * @param id 租户id
 * @returns {boolean}
 */
function isCurrentTenant(id) {
  return Number(id) === Number(props.currentTenantId);
}

/**
 * 点击非当前租户行即切换默认租户，成功后刷新会话并回到工作台
 * @param item 租户数据
 */
function handleTenantClick(item) {
  if (isCurrentTenant(item.id) || switchingTenantId.value !== null) {
    return;
  }
  switchingTenantId.value = item.id;
  switchTenantAPI({id: item.id}).then(res => {
    if (res.code !== 200) {
      return;
    }
    handleClose();
    toast('已切换至「' + item.name + '」');
    // 延时等待提示展示后刷新会话，使新租户下的菜单与数据范围全局生效
    setTimeout(refreshSessionAndHome, 800);
  }).finally(() => {
    switchingTenantId.value = null;
  });
}

/**
 * 重新拉取会话信息更新用户状态，并返回工作台首页
 */
function refreshSessionAndHome() {
  getSessionAPI().then(res => {
    if (res.code === 200) {
      store.dispatch('user/setUserInfo', {userInfo: res.data});
    }
  }).finally(() => {
    uni.reLaunch({
      url: '/pages/home/index'
    });
  });
}

/**
 * 关闭弹层
 */
function handleClose() {
  emit('update:show', false);
}
</script>
<style scoped lang="scss">
// uview-plus 弹窗根节点自带 flex:1，防止在纵向 flex 布局页面中抢占高度
.tenant-switch-wrap {
  flex: none;
}

.tenant-switch-popup {
  padding: 16px 16px 24px;

  .tenant-popup-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;

    .tenant-popup-title {
      font-size: 17px;
      font-weight: 600;
      color: #303133;
    }
  }

  .tenant-popup-tip {
    display: flex;
    justify-content: center;
    padding: 40px 0;
  }

  .tenant-popup-list {
    max-height: 360px;

    .tenant-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 6px;
      border-bottom: 1px solid #f5f7fa;

      &:last-child {
        border-bottom: 0;
      }

      &:active {
        background-color: #f5f7fa;
      }

      &.is-current {
        .tenant-name {
          color: #0052cc;
        }
      }

      .tenant-info {
        display: flex;
        flex-direction: column;
        gap: 4px;
        min-width: 0;

        .tenant-name {
          font-size: 15px;
          color: #303133;
        }

        .tenant-meta {
          font-size: 12px;
          color: #909399;
        }
      }

      .tenant-state {
        flex-shrink: 0;
        margin-left: 12px;
      }
    }
  }
}
</style>

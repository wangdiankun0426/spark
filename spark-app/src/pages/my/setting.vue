<template>
  <div class="setting-container safe-area-page">
    <!-- 顶部标题栏 -->
    <view class="setting-header" :style="navHeaderStyle()">
      <view class="header-left" @click="onClickLeft">
        <up-icon name="arrow-left" size="20" color="#2f7bff"></up-icon>
        <text class="header-title">设置</text>
      </view>
    </view>

    <view class="setting-content">
      <view class="group-title">基础</view>
      <up-cell-group>
        <up-cell title="切换租户" isLink @click="openTenantPopup"/>
      </up-cell-group>
      <view class="group-title group-title--spaced">系统</view>
      <up-cell-group>
        <up-cell title="用户服务协议" isLink @click="openAgreement('service')"/>
        <up-cell title="隐私政策" isLink @click="openAgreement('privacy')"/>
      </up-cell-group>
    </view>

    <!-- 登出按钮 -->
    <view class="setting-logout">
      <up-button
          type="primary"
          color="linear-gradient(90deg, #004fc5 0%, #0072e0 100%)"
          :custom-style="{ height: '88rpx', borderRadius: '12rpx', boxShadow: '0 12rpx 28rpx rgba(0, 79, 197, 0.28)' }"
          @click="modalVisible = true"
      >
        <text class="logout-text">退 出</text>
      </up-button>
    </view>

    <up-modal
        :show="modalVisible"
        title="提示"
        content="是否确定退出系统?"
        @confirm="handleLogout"
        showCancelButton
        @cancel="modalVisible = false"
    />

    <!-- 切换租户弹层 -->
    <tenant-switch
        :show="tenantPopupVisible"
        :current-tenant-id="userInfo.currentTenantId"
        @update:show="tenantPopupVisible = $event"
    />
  </div>
</template>
<script setup>
import {computed, ref} from 'vue';
import {useStore} from 'vuex';
import {logoutAPI} from "@/api/auth/login";
import {navHeaderStyle} from "@/utils/navBarUtil";
import TenantSwitch from "@/components/TenantSwitch/index.vue"

const store = useStore();
const modalVisible = ref(false);

// 切换租户弹层显隐
const tenantPopupVisible = ref(false);

const userInfo = computed(() => store.getters["user/getUserInfo"] || {
  id: undefined,
  name: undefined,
  currentTenantId: undefined
});

/**
 * 返回
 */
function onClickLeft() {
  uni.navigateBack();
}

/**
 * 打开切换租户弹层
 */
function openTenantPopup() {
  tenantPopupVisible.value = true;
}

/**
 * 打开协议文本页
 */
function openAgreement(type) {
  uni.navigateTo({url: `/pages/agreement/index?type=${type}`});
}

/**
 * 登出系统
 */
function handleLogout() {
  modalVisible.value = false;
  logoutAPI().then(() => {
    store.dispatch('user/logout');
    uni.reLaunch({
      url: '/pages/login'
    });
  })
}
</script>
<style scoped lang="scss">
.setting-container {
  height: 100vh;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: $spark-color-header-bg;
}

.setting-header {
  flex-shrink: 0;
  padding: 16px 16px;
  background-color: $spark-color-header-bg;
  border-bottom: 1rpx solid $spark-color-header-border;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .header-title {
    font-size: 17px;
    font-style: italic;
    font-weight: 700;
    letter-spacing: 1px;
    /* #ifdef MP-WEIXIN */
    color: #2f7bff;
    /* #endif */
    /* #ifdef H5 */
    background-image: linear-gradient(92deg, #2563eb 0%, #3d8bff 100%);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    /* #endif */
  }
}

.setting-content {
  flex: 1;
  overflow-y: auto;
  padding-top: 8px;
  padding-bottom: 90px;
}

.group-title {
  padding: 12px 16px 6px;
  font-size: 15px;
  color: $spark-color-text-tertiary;
}

.group-title--spaced {
  margin-top: 12px;
}

.setting-logout {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10;
  padding: 16px 20px;
  padding-bottom: calc(16px + constant(safe-area-inset-bottom));
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
  background-color: $spark-color-header-bg;
}

.logout-text {
  font-size: 32rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
  color: #ffffff;
}
</style>

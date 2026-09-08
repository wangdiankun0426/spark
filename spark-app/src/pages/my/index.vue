<template>
  <div class="my-box safe-area-page">
    <div class="my-header">
      <div class="my-avatar-row">
        <div class="my-avatar-left">
          <UserAvatar type="user" :userId="userInfo.id" :name="userInfo.name" :size="100"/>
        </div>

        <div class="my-info-right">
          <text class="my-name-text">{{userInfo.name}}（{{userInfo.loginName}}）<br>
            <text class="my-dept-text">{{userInfo.deptPath}}</text>
          </text>
        </div>
      </div>
    </div>

    <div class="my-card">
      <view class="quick-grid">
        <view v-if="hasMenu(MENU_IDS.CONTACTS)" class="quick-item" @click="goContacts">
          <view class="quick-icon-wrap" style="background-color: #e6f7ff;">
            <up-icon name="man-add-fill" size="32" color="#1890ff"></up-icon>
          </view>
          <text class="quick-label">通讯录</text>
        </view>
        <view class="quick-item" @click="openTenantPopup">
          <view class="quick-icon-wrap" style="background-color: #e6fffb;">
            <up-icon name="share-square" size="32" color="#13c2c2"></up-icon>
          </view>
          <text class="quick-label">切换租户</text>
        </view>
        <view class="quick-item">
          <view class="quick-icon-wrap" style="background-color: #fff7e6;">
            <up-icon name="server-fill" size="32" color="#faad14"></up-icon>
          </view>
          <text class="quick-label">开发中</text>
        </view>
        <view class="quick-item">
          <view class="quick-icon-wrap" style="background-color: #f9f0ff;">
            <up-icon name="star-fill" size="32" color="#722ed1"></up-icon>
          </view>
          <text class="quick-label">开发中</text>
        </view>
      </view>
    </div>

    <div class="my-content">
      <up-cell-group class="my-cell-group">
        <up-cell title="个人信息" url="/pages/my/infomation" isLink />
        <up-cell title="修改密码" url="/pages/my/updatePwd" isLink />
        <up-cell title="修改头像" url="/pages/my/updateAvatar" isLink />
      </up-cell-group>
    </div>

    <!-- 登出按钮 -->
    <div class="my-logout-wrapper">
      <up-button
          type="primary"
          class="logout-button"
          @click="modalVisible = true"
      >
        <text class="logout-text">退 出</text>
      </up-button>
    </div>

    <up-modal
        :show="modalVisible"
        title="标题"
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

    <up-tabbar :value="active" @change="handleOnTabChange" activeColor="#0052cc">
      <up-tabbar-item
          v-for="tab in tabBarItems"
          :key="tab.name"
          :name="tab.name"
          :icon="tab.icon"
          :text="tab.text"
      />
    </up-tabbar>
  </div>
</template>
<script setup>
import {logoutAPI} from "@/api/auth/login";
import {ref, computed} from "vue";
import {useStore} from "vuex";
import {MENU_IDS, hasMenu, visibleTabs, checkMenuAccess} from "@/utils/menuUtil";
import UserAvatar from "@/components/UserAvatar/index.vue"
import TenantSwitch from "@/components/TenantSwitch/index.vue"

const store = useStore();
const active = ref("my");

// 按菜单权限过滤后的底部导航项
const tabBarItems = computed(() => visibleTabs());

const userInfo = computed(() => store.getters["user/getUserInfo"] || {
  id: undefined,
  name: undefined,
  loginName: undefined,
  deptPath: undefined,
  currentTenantId: undefined
});
const modalVisible = ref(false);

// 切换租户弹层显隐
const tenantPopupVisible = ref(false);

/**
 * 打开切换租户弹层
 */
function openTenantPopup() {
  tenantPopupVisible.value = true;
}

/**
 * 跳转通讯录
 */
function goContacts() {
  uni.navigateTo({
    url: '/views/contacts/index'
  })
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
.my-box {
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.my-box :deep(.u-tabbar) {
  flex: none;
}

.my-header {
  padding-top: 30px;
  height: 150px;
  width: 100%;
  background-color: #0052cc;
}

.my-avatar-row {
  width: 90%;
  height: 100px;
  margin: auto;
  overflow: hidden;
  position: relative;
  top: 10px;
}

.my-avatar-left {
  float: left;
  width: 30%;
  overflow: hidden;
}

.my-info-right {
  width: 70%;
  height: 80px;
  float: right;
  display: flex;
  align-items: center;
}

.my-name-text {
  font-size: 20px;
  color: #ffffff;
  font-weight: bolder;
}

.my-dept-text {
  font-size: 16px;
  color: #d7d7d7;
}

.my-card {
  width: 90%;
  margin: auto;
  background-color: #fff;
  border-radius: 10px;
  overflow: hidden;
  padding: 16px 8px;
  position: relative;
  top: -30px;
  z-index: 1;
}

.my-content {
  flex: 1;
  overflow-y: auto;
  position: relative;
  top: -30px;
}

.quick-grid {
  display: flex;
  justify-content: space-around;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.quick-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-label {
  font-size: 16px;
  color: #333;
}

.my-cell-group {
  margin-top: 20px;
  padding-bottom: 80px;
}

.my-logout-wrapper {
  position: fixed;
  bottom: 70px;
  left: 0;
  right: 0;
  padding: 40px 10px;
  z-index: 10;
}

.logout-button {
  width: 100%;
  background-color: #0052cc !important;
  height: 50px;
}

.logout-text {
  font-size: 20px;
  font-weight: bolder;
  color: #ffffff;
}
</style>

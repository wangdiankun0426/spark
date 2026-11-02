<template>
  <view class="info-page safe-area-page" :class="themeClass">
    <!-- 顶部标题栏 -->
    <view class="page-header" :style="navHeaderStyle()">
      <view class="page-header-back" @click="onClickLeft">
        <up-icon name="arrow-left" size="19" :color="backIconColor"></up-icon>
      </view>
      <text class="page-header-title">个人信息</text>
      <view class="page-header-extra"></view>
    </view>

    <scroll-view class="info-body" scroll-y>
      <!-- 身份卡：头像 + 姓名 + 部门，落在页面最上方 -->
      <view class="profile-card">
        <user-avatar
            type="user"
            shape="rounded"
            :userId="userInfo.id"
            :name="userInfo.name"
            :size="58"
        />
        <view class="profile-main">
          <text class="profile-name">{{ userInfo.name || '未填写' }}</text>
          <text class="profile-dept">{{ userInfo.deptPath || '暂未分配部门' }}</text>
        </view>
      </view>

      <!-- 基础信息：标签在左、值在右，长文本自动换行 -->
      <view class="section-card">
        <view class="section-head">
          <view class="section-bar"></view>
          <text class="section-title">基础信息</text>
        </view>
        <view class="kv-row">
          <text class="kv-key">登录账号</text>
          <text class="kv-value">{{ userInfo.loginName || '未填写' }}</text>
        </view>
        <view class="kv-row">
          <text class="kv-key">手机号码</text>
          <text class="kv-value">{{ userInfo.phone || '未填写' }}</text>
        </view>
        <view class="kv-row">
          <text class="kv-key">邮箱</text>
          <text class="kv-value">{{ userInfo.email || '未填写' }}</text>
        </view>
        <view class="kv-row">
          <text class="kv-key">性别</text>
          <text class="kv-value">{{ userInfo.sexName || '未填写' }}</text>
        </view>
      </view>

      <!-- 组织信息 -->
      <view class="section-card">
        <view class="section-head">
          <view class="section-bar"></view>
          <text class="section-title">组织信息</text>
        </view>
        <view class="kv-row">
          <text class="kv-key">所属租户</text>
          <text class="kv-value">{{ userInfo.tenantName || '未分配' }}</text>
        </view>
        <view class="kv-row">
          <text class="kv-key">所属角色</text>
          <text class="kv-value">{{ userInfo.roleNames || '未分配' }}</text>
        </view>
        <view class="kv-row">
          <text class="kv-key">所属部门</text>
          <text class="kv-value">{{ userInfo.deptPath || '未分配' }}</text>
        </view>
      </view>

      <view class="page-foot-tip">如需修改资料，请联系管理员</view>
    </scroll-view>
  </view>
</template>
<script setup>
import {computed} from "vue";
import {useStore} from "vuex";
import UserAvatar from "@/components/UserAvatar/index.vue"
import {navHeaderStyle} from "@/utils/navBarUtil";
import {useTheme} from "@/utils/useTheme";
import {getHeaderIconColor} from "@/utils/themeUtil";

const {isDark, themeClass} = useTheme();
const store = useStore();
const userInfo = computed(() => store.getters["user/getUserInfo"] || {
  id: undefined,
  name: undefined,
  loginName: undefined,
  deptPath: undefined,
  sexName: undefined
});

const backIconColor = computed(() => getHeaderIconColor(isDark.value));

/*
  * 返回
  */
function onClickLeft() {
  uni.navigateBack();
 }
</script>
<style scoped lang="scss">
.info-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  box-sizing: border-box;
  background-color: var(--app-bg-page);
}

.info-body {
  flex: 1;
  min-height: 0;
  padding: 12px 14px calc(20px + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 16px 15px;
  margin-bottom: 10px;
  border-radius: 16px;
  background-image: var(--app-gradient-hero);
  border: 1px solid var(--app-hero-border);
  box-shadow: var(--app-shadow-card);
}

.profile-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.profile-name {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.3px;
  color: var(--app-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-dept {
  font-size: 11.5px;
  line-height: 16px;
  color: var(--app-text-secondary);
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  word-break: break-all;
}

.section-card {
  padding: 13px 14px 4px;
  margin-bottom: 10px;
  border-radius: 16px;
  background-color: var(--app-bg-card);
  box-shadow: var(--app-shadow-card);
}

.section-head {
  display: flex;
  align-items: center;
  gap: 7px;
  padding-bottom: 8px;
}

.section-bar {
  width: 3px;
  height: 12px;
  border-radius: 2px;
  background-image: var(--app-gradient-fill);
}

.section-title {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--app-text-primary);
}

.kv-row {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 8px 0;
  border-top: 1px solid var(--app-divide);
}

.kv-key {
  flex-shrink: 0;
  width: 72px;
  font-size: 12.5px;
  line-height: 18px;
  color: var(--app-text-tertiary);
}

.kv-value {
  flex: 1;
  min-width: 0;
  text-align: right;
  font-size: 12.5px;
  line-height: 18px;
  color: var(--app-text-primary);
  word-break: break-all;
}

.page-foot-tip {
  padding: 10px 0 4px;
  text-align: center;
  font-size: 11px;
  color: var(--app-text-tertiary);
}
</style>

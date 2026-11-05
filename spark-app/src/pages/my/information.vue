<template>
  <view class="info-page safe-area-page" :class="themeClass">
    <PageHeader title="个人信息" @back="onClickLeft"/>

    <scroll-view class="info-body" scroll-y>
      <!-- 基础信息：标签在左、值在右，长文本自动换行 -->
      <view class="section-card">
        <view class="section-head">
          <SectionTitle>基础信息</SectionTitle>
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
          <SectionTitle>组织信息</SectionTitle>
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
import PageHeader from "@/components/PageHeader/index.vue";
import SectionTitle from "@/components/SectionTitle/index.vue";
import {useTheme} from "@/utils/useTheme";

const {themeClass} = useTheme();
const store = useStore();
const userInfo = computed(() => store.getters["user/getUserInfo"] || {
  id: undefined,
  name: undefined,
  loginName: undefined,
  deptPath: undefined,
  sexName: undefined
});

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
  background-color: $app-bg-page;
}

.info-body {
  flex: 1;
  min-height: 0;
  padding: 12px 14px calc(20px + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.section-card {
  padding: 13px 14px 4px;
  margin-bottom: 10px;
  border-radius: 16px;
  background-color: $app-bg-card;
  box-shadow: $app-shadow-card;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 7px;
  padding-bottom: 8px;
}

.kv-row {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 8px 0;
  border-top: 1px solid $app-divide;
}

.kv-key {
  flex-shrink: 0;
  width: 72px;
  font-size: 12.5px;
  line-height: 18px;
  color: $app-text-tertiary;
}

.kv-value {
  flex: 1;
  min-width: 0;
  text-align: right;
  font-size: 12.5px;
  line-height: 18px;
  color: $app-text-primary;
  word-break: break-all;
}

.page-foot-tip {
  padding: 10px 0 4px;
  text-align: center;
  font-size: 11px;
  color: $app-text-tertiary;
}
</style>

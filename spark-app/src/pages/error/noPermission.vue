<template>
  <view class="status-page safe-area-page" :class="themeClass">
    <view class="status-body">
      <view class="status-num">403</view>
      <text class="status-title">暂无访问权限</text>
      <text class="status-desc">您暂未被授予该功能的菜单权限，如需使用请联系管理员开通</text>
    </view>
  </view>
</template>

<script setup>
import {useTheme} from '@/utils/useTheme';

const {themeClass} = useTheme();
</script>

<style scoped lang="scss">
.status-page {
  height: 100vh;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--app-bg-page);
}

.status-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0 40px;
}

.status-num {
  font-size: 88px;
  font-weight: 700;
  color: var(--app-text-info);
  line-height: 1;
}

.status-title {
  margin-top: 24px;
  font-size: 22px;
  font-weight: 600;
  color: var(--app-text-primary);
}

.status-desc {
  margin-top: 12px;
  font-size: 14px;
  color: var(--app-text-muted);
  text-align: center;
  line-height: 22px;
}

</style>

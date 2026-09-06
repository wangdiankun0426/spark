<template>
  <view class="status-page safe-area-page">
    <view class="page-header">
      <view class="page-header-back">
        <up-icon name="arrow-left" color="#ffffff" size="18"/>
      </view>
      <text class="page-header-title">无权限</text>
    </view>
    <view class="status-body">
      <view class="status-num">403</view>
      <text class="status-title">暂无访问权限</text>
      <text class="status-desc">您暂未被授予该功能的菜单权限，如需使用请联系管理员开通</text>
    </view>
  </view>
</template>

<script setup>

</script>

<style scoped lang="scss">
.status-page {
  height: 100vh;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #f5f7fa;
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
  color: #0052cc;
  line-height: 1;
}

.status-title {
  margin-top: 24px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.status-desc {
  margin-top: 12px;
  font-size: 14px;
  color: #909399;
  text-align: center;
  line-height: 22px;
}

</style>

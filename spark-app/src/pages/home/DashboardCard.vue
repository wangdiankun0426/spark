<template>
  <view class="dashboard-card" :style="customStyle">
    <!-- 头部：标题和可选图标/操作区域 -->
    <view class="card-header" v-if="title || $slots.header">
      <view class="card-title-box">
        <up-icon v-if="icon" :name="icon" :color="iconColor" size="20" class="card-icon"></up-icon>
        <text class="card-title">{{ title }}</text>
        <up-icon v-if="tooltip" name="question-circle" color="#999" size="14" class="card-tooltip"></up-icon>
      </view>
      <view class="card-extra">
        <slot name="extra"></slot>
      </view>
    </view>
    <view class="card-divider" v-if="showDivider && (title || $slots.header)"></view>
    <!-- 内容区域 -->
    <view class="card-body">
      <slot></slot>
    </view>
  </view>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: ''
  },
  iconColor: {
    type: String,
    default: '#1890ff'
  },
  tooltip: {
    type: Boolean,
    default: false
  },
  showDivider: {
    type: Boolean,
    default: true
  },
  customStyle: {
    type: Object,
    default: () => ({})
  }
})
</script>

<style scoped lang="scss">
.dashboard-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 10px;
  margin: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 12px;

    .card-title-box {
      display: flex;
      align-items: center;

      .card-icon {
        margin-right: 6px;
      }

      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #333333;
      }

      .card-tooltip {
        margin-left: 6px;
      }
    }
  }

  .card-divider {
    height: 1px;
    background-color: #f0f2f5;
    margin-bottom: 12px;
  }
}
</style>

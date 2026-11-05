<template>
  <!-- 列表空状态：hero 渐变卡 + 柔色图标，按钮可选，点击在组件边界止冒泡 -->
  <view class="empty-card">
    <view class="empty-glow"></view>
    <view class="empty-ring"></view>
    <view class="empty-icon">
      <up-icon :name="icon" size="22" :color="iconColor"/>
    </view>
    <text class="empty-title">{{ title }}</text>
    <text v-if="desc" class="empty-sub">{{ desc }}</text>
    <view v-if="btnText" class="empty-btn" @click.stop="emit('btnClick')">
      <up-icon v-if="btnIcon" :name="btnIcon" size="11" :color="iconColor"/>
      <text class="empty-btn__text">{{ btnText }}</text>
    </view>
  </view>
</template>

<script setup>
import {computed} from 'vue'
import {useTheme} from '@/utils/useTheme'
import {getHeaderIconColor} from '@/utils/themeUtil'

const props = defineProps({
  icon: {type: String, required: true},
  title: {type: String, required: true},
  desc: {type: String, default: ''},
  btnText: {type: String, default: ''},
  btnIcon: {type: String, default: 'reload'}
})

const emit = defineEmits(['btnClick'])

const {isDark} = useTheme()
const iconColor = computed(() => getHeaderIconColor(isDark.value))
</script>

<style scoped lang="scss">
.empty-card {
  position: relative;
  overflow: hidden;
  width: 100%;
  box-sizing: border-box;
  padding: 22px 18px 20px;
  border-radius: $radius-2xl;
  background-image: $app-gradient-hero;
  border: 1px solid $app-hero-border;
  box-shadow: $app-shadow-card;
}

.empty-glow {
  position: absolute;
  right: -46px;
  top: -52px;
  width: 168px;
  height: 168px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(47, 123, 255, 0.14) 0%, rgba(47, 123, 255, 0) 66%);
  pointer-events: none;
}

.empty-ring {
  position: absolute;
  right: -14px;
  top: -20px;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  border: 1px solid $app-hero-border;
  pointer-events: none;
}

.empty-icon {
  position: relative;
  width: 46px;
  height: 46px;
  border-radius: 15px;
  background-color: $app-primary-soft;
  border: 1px solid $app-brand-border;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-title {
  position: relative;
  display: block;
  margin-top: 13px;
  font-size: $font-size-base;
  font-weight: 600;
  color: $app-text-primary;
}

.empty-sub {
  position: relative;
  display: block;
  margin-top: 6px;
  font-size: $font-size-xs;
  line-height: 1.6;
  color: $app-text-tertiary;
}

.empty-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  margin-top: 14px;
  height: 30px;
  padding: 0 13px;
  border-radius: $radius-md;
  background-color: $app-primary-soft;
  border: 1px solid $app-brand-border;

  &:active {
    opacity: 0.85;
  }
}

.empty-btn__text {
  font-size: $font-size-xs;
  font-weight: 600;
  color: $app-brand;
}
</style>

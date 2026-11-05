<template>
  <!-- 二级页统一头部：返回 + 居中标题 + 右侧操作槽，导航适配与图标色内部收口 -->
  <view class="page-header" :style="below ? navHeaderBelowStyle(belowGap) : navHeaderStyle()">
    <view class="page-header-side" @click="emit('back')">
      <up-icon name="arrow-left" size="20" :color="iconColor"></up-icon>
    </view>
    <text class="page-header-title" :class="{'is-tip': tip}">{{ title }}</text>
    <view class="page-header-side">
      <slot name="extra"></slot>
    </view>
  </view>
</template>

<script setup>
import {computed} from 'vue'
import {navHeaderStyle, navHeaderBelowStyle} from '@/utils/navBarUtil'
import {useTheme} from '@/utils/useTheme'
import {getHeaderIconColor} from '@/utils/themeUtil'

const props = defineProps({
  title: {type: String, required: true},
  // 右侧自带操作按钮与微信胶囊挤位时，整行下移到胶囊底边下方
  below: {type: Boolean, default: false},
  // below 模式内容行与胶囊底边的间距
  belowGap: {type: Number, default: 8},
  // 标题降级为灰色提示文案，用于无选中对象的兜底场景
  tip: {type: Boolean, default: false}
})

const emit = defineEmits(['back'])

const {isDark} = useTheme()
const iconColor = computed(() => getHeaderIconColor(isDark.value))
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: center;
  height: 48px;
  padding: 0 14px;
  background-color: $app-bg-card;
  border-bottom: 1px solid $app-border-light;
  flex-shrink: 0;
}

.page-header-side {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.page-header-title {
  flex: 1;
  min-width: 0;
  text-align: center;
  font-size: $font-size-title;
  font-weight: 700;
  letter-spacing: 0.4px;
  color: $app-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.page-header-title.is-tip {
  font-size: $font-size-md;
  color: $app-text-tertiary;
}
</style>

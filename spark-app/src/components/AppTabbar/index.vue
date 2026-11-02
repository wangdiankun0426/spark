<template>
  <!-- 底部导航：自绘组件，替代 up-tabbar。
       uview 的 tabbar 把背景色写死在组件样式里，小程序样式隔离下外部改不动，
       暗色皮肤底部会一直露白，故这里用页面级 CSS 变量自行实现 -->
  <view class="app-tabbar">
    <view
        class="app-tabbar__item"
        :class="{'is-active': tab.name === value, 'is-center': tab.center}"
        v-for="tab in tabBarItems"
        :key="tab.name"
        @click="handleChange(tab.name)"
    >
      <!-- 中间的 AI 应用：凸起圆钮，底栏唯一的视觉重心 -->
      <view v-if="tab.center" class="center-btn">
        <up-icon :name="tab.icon" size="24" :color="tab.name === value ? centerActiveColor : inactiveColor"/>
      </view>
      <template v-else>
        <up-icon
            :name="tab.icon"
            size="22"
            :color="tab.name === value ? activeColor : inactiveColor"
        />
      </template>
      <text
          class="app-tabbar__text"
          :class="{'is-center-text': tab.center}"
          :style="{color: tab.name === value ? (tab.center ? centerTextColor : activeColor) : inactiveColor}"
      >{{ tab.text }}</text>
    </view>
  </view>
</template>
<script setup>
import {computed} from 'vue';
import {visibleTabs} from '@/utils/menuUtil';
import {useTheme} from '@/utils/useTheme';
import {getOnFillColor, getPrimaryColor, getTertiaryColor, getHeaderIconColor} from '@/utils/themeUtil';

defineProps({
  // 当前选中的导航项 name
  value: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['change']);

const {isDark} = useTheme();

// 顶部/底部导航选中与未选中配色，暗色下整体提亮一档
const activeColor = computed(() => getPrimaryColor(isDark.value));
const inactiveColor = computed(() => getTertiaryColor(isDark.value));
// 凸起圆钮下方的文字色，跟着品牌色走
const centerTextColor = computed(() => getHeaderIconColor(isDark.value));
const centerActiveColor = computed(() => getOnFillColor(isDark.value));

// 按菜单权限过滤后的底部导航项
const tabBarItems = computed(() => visibleTabs());

/**
 * 切换导航
 * @param name 目标导航项
 */
function handleChange(name) {
  if (name === undefined) {
    return;
  }
  emit('change', name);
}
</script>
<style scoped lang="scss">
.app-tabbar {
  display: flex;
  align-items: stretch;
  flex: none;
  height: 54px;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  background-color: var(--app-bg-tabbar);
  border-top: 1rpx solid var(--app-border-light);
  box-sizing: content-box;
}

.app-tabbar__item {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.app-tabbar__item.is-active:not(.is-center)::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 36rpx;
  height: 6rpx;
  border-radius: 0 0 6rpx 6rpx;
  background-image: var(--app-gradient-brand);
}

.app-tabbar__item.is-center {
  justify-content: flex-end;
  padding-bottom: 3px;
}

.center-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  margin-top: -24px;
  margin-bottom: 3px;
  border-radius: 50%;
  background-color: var(--app-bg-sub);
  border: 1px solid var(--app-border-light);
  box-shadow: 0 0 0 5px var(--app-bg-tabbar);
  transition: background-color 0.18s ease, box-shadow 0.18s ease, width 0.18s ease, height 0.18s ease;
}

.app-tabbar__item.is-active .center-btn {
  width: 48px;
  height: 48px;
  margin-top: -26px;
  background-color: transparent;
  background-image: var(--app-gradient-fill);
  border-color: transparent;
  box-shadow: var(--app-shadow-center);
}

.tab-badge {
  position: absolute;
  top: 7px;
  right: 50%;
  margin-right: -16px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background-color: var(--app-error-solid);
  box-shadow: 0 0 0 2px var(--app-bg-tabbar);
}

.app-tabbar__text {
  font-size: 11px;
  line-height: 14px;
  font-weight: 500;
}

.app-tabbar__text.is-center-text {
  font-weight: 700;
}

.app-tabbar__item.is-active .app-tabbar__text {
  font-weight: 700;
}
</style>

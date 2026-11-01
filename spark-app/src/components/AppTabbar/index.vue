<template>
  <!-- 底部导航：自绘组件，替代 up-tabbar。
       uview 的 tabbar 把背景色写死在组件样式里，小程序样式隔离下外部改不动，
       暗色皮肤底部会一直露白，故这里用页面级 CSS 变量自行实现 -->
  <view class="app-tabbar">
    <view
        class="app-tabbar__item"
        v-for="tab in tabBarItems"
        :key="tab.name"
        @click="handleChange(tab.name)"
    >
      <up-icon
          :name="tab.icon"
          size="22"
          :color="tab.name === value ? activeColor : inactiveColor"
      />
      <text
          class="app-tabbar__text"
          :style="{color: tab.name === value ? activeColor : inactiveColor}"
      >{{ tab.text }}</text>
    </view>
  </view>
</template>
<script setup>
import {computed} from 'vue';
import {visibleTabs} from '@/utils/menuUtil';
import {useTheme} from '@/utils/useTheme';

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
const activeColor = computed(() => (isDark.value ? '#4d8dff' : '#004fc5'));
const inactiveColor = computed(() => (isDark.value ? '#8b9cb5' : '#909399'));

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
  align-items: center;
  flex: none;
  height: 50px;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  background-color: var(--app-bg-tabbar);
  border-top: 1rpx solid var(--app-border-light);
  box-sizing: content-box;
}

.app-tabbar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.app-tabbar__text {
  font-size: 11px;
  line-height: 14px;
}
</style>

<template>
  <div class="my-box safe-area-page" :class="themeClass">
    <div class="my-header" :style="headerStyle">
      <view class="my-setting-entry" :style="settingEntryStyle" @click="goSetting">
        <up-icon name="setting" size="22" :color="settingIconColor"/>
      </view>

      <div class="my-avatar-row">
        <div class="my-avatar-left">
          <UserAvatar
              type="user"
              :userId="userInfo.id"
              :name="userInfo.name"
              :size="100"
          />
        </div>

        <div class="my-info-right">
          <text class="my-name-text">{{userInfo.name}}</text>
          <text class="my-dept-text">部门：{{userInfo.deptPath}}</text>
        </div>
      </div>
    </div>

    <div class="my-content">
      <view class="func-card" v-for="section in funcSections" :key="section.key">
        <text class="func-card-title">{{ section.title }}</text>
        <view class="func-grid">
          <view
              class="func-item"
              v-for="item in section.list"
              :key="item.url || item.key"
              @click="goFunc(item)"
          >
            <view class="func-icon-wrap" :style="{backgroundColor: item.bgColor}">
              <ThemeGlyph
                  v-if="item.glyph"
                  :dark="isDark"
                  size="normal"
                  :style="{color: item.color}"
              />
              <up-icon v-else :name="item.icon" size="32" :color="item.color"></up-icon>
            </view>
            <text class="func-label">{{ item.label }}</text>
          </view>
        </view>
      </view>
    </div>

    <AppTabbar :value="active" @change="handleOnTabChange"/>
  </div>
</template>
<script setup>
import {ref, computed} from "vue";
import {useStore} from "vuex";
import {navBarInfo} from "@/utils/navBarUtil";
import {useTheme} from "@/utils/useTheme";
import UserAvatar from "@/components/UserAvatar/index.vue"
import ThemeGlyph from "@/components/ThemeGlyph/index.vue"
import AppTabbar from "@/components/AppTabbar/index.vue"

const store = useStore();
const active = ref("my");
const {isDark, themeClass, toggleTheme} = useTheme();

// 主题相关的图标配色
const settingIconColor = computed(() => (isDark.value ? '#e5edf7' : '#172b4d'));

// 小程序端渐变头延伸到状态栏，H5 保持原间距
const headerStyle = computed(() => {
  // #ifdef MP-WEIXIN
  return {paddingTop: navBarInfo.statusBarHeight + 20 + 'px'}
  // #endif
  // #ifdef H5
  return {paddingTop: '42px'}
  // #endif
})

// 小程序端设置入口下移到微信胶囊按钮下方
const settingEntryStyle = computed(() => {
  // #ifdef MP-WEIXIN
  return {top: navBarInfo.menuBottom + 10 + 'px'}
  // #endif
  // #ifdef H5
  return {top: '22px'}
  // #endif
})

// 按菜单权限过滤后的底部导航项
const userInfo = computed(() => store.getters["user/getUserInfo"] || {
  id: undefined,
  name: undefined,
  deptPath: undefined
});

// 功能入口按热门与基础分组；主题入口排在「修改头像」右侧，同一套图标格样式，点击即切换皮肤
const funcSections = computed(() => [
  {
    key: 'hot',
    title: '热门功能',
    list: [
      {label: '通讯录', icon: 'man-add-fill', color: '#1890ff', bgColor: isDark.value ? 'rgba(24, 144, 255, 0.16)' : '#e6f7ff', url: '/views/contact/index'}
    ]
  },
  {
    key: 'basic',
    title: '基础功能',
    list: [
      {label: '个人信息', icon: 'account-fill', color: '#13c2c2', bgColor: isDark.value ? 'rgba(19, 194, 194, 0.16)' : '#e6fffb', url: '/pages/my/information'},
      {label: '修改密码', icon: 'lock-fill', color: '#52c41a', bgColor: isDark.value ? 'rgba(82, 196, 26, 0.16)' : '#f6ffed', url: '/pages/my/updatePwd'},
      {label: '修改头像', icon: 'photo-fill', color: '#fa8c16', bgColor: isDark.value ? 'rgba(250, 140, 22, 0.16)' : '#fff7e6', url: '/pages/my/updateAvatar'},
      {
        key: 'theme',
        action: 'theme',
        label: isDark.value ? '浅色模式' : '深色模式',
        glyph: true,
        color: isDark.value ? '#f5b544' : '#7c5cff',
        bgColor: isDark.value ? 'rgba(245, 181, 68, 0.16)' : '#f3f0ff'
      }
    ]
  }
]);

/**
 * 跳转常用功能
 * @param item 功能项，action 为内置行为时走内置逻辑，否则按 url 跳转
 */
function goFunc(item) {
  if (item.action === 'theme') {
    toggleTheme();
    return;
  }
  uni.navigateTo({
    url: item.url
  })
}

/**
 * 跳转设置页
 */
function goSetting() {
  uni.navigateTo({
    url: '/pages/my/setting'
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
  background-color: var(--app-bg-page);
}

.my-header {
  height: 170px;
  width: 100%;
  position: relative;
  background-image: var(--app-gradient-profile);
}

.my-setting-entry {
  position: absolute;
  right: 12px;
  z-index: 2;
  padding: 6px;
}

.my-avatar-row {
  width: 90%;
  min-height: 100px;
  margin: auto;
  overflow: hidden;
  position: relative;
  top: 16px;
}

.my-avatar-left {
  float: left;
  width: 30%;
  overflow: hidden;
}

.my-info-right {
  width: 70%;
  min-height: 80px;
  float: right;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding-right: 34px;
  box-sizing: border-box;
}

.my-name-text {
  font-size: 20px;
  line-height: 1.4;
  letter-spacing: 0.5px;
  color: $spark-color-text;
  font-weight: 700;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
}

.my-dept-text {
  font-size: 14px;
  line-height: 1.4;
  letter-spacing: 0.5px;
  font-weight: 400;
  color: var(--app-text-secondary);
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
}

.my-content {
  flex: 1;
  overflow-y: auto;
  margin-top: -12px;
  position: relative;
  z-index: 1;
}

.func-card {
  width: 90%;
  margin: 4px auto 0;
  background-color: var(--app-bg-card);
  border: 1px solid var(--app-border-light);
  border-radius: 10px;
  padding: 16px 8px;
  box-shadow: var(--app-shadow-card);
}

.func-card + .func-card {
  margin-top: 16px;
}

.func-card-title {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text-primary);
  padding-left: 8px;
}

.func-grid {
  display: flex;
  flex-wrap: wrap;
  margin-top: 16px;
}

.func-item {
  width: 25%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.func-icon-wrap {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.func-label {
  font-size: 14px;
  color: var(--app-text-secondary);
}
</style>

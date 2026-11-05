<template>
  <div class="my-box safe-area-page" :class="themeClass">
    <!-- 头部：渐变个人卡 -->
    <div class="my-header" :style="headerStyle">
      <div class="my-avatar-row">
        <view class="my-avatar-ring">
          <UserAvatar
              type="user"
              :userId="userInfo.id"
              :name="userInfo.name"
              :size="62"
          />
        </view>

        <div class="my-info-right">
          <text class="my-name-text">{{userInfo.name}}</text>
          <text class="my-dept-text">{{userInfo.deptPath}}</text>
          <view class="my-tenant-chip">
            <up-icon name="home-fill" size="10" :color="getOnFillColor(isDark)"/>
            <text class="my-tenant-text">{{userInfo.tenantName || '未分配租户'}}</text>
          </view>
        </div>
      </div>
    </div>

    <!-- 中部：分组列表卡，小程序普通 view 不支持滚动容器，必须用 scroll-view -->
    <scroll-view class="my-content" scroll-y>
      <view class="section-card" v-for="section in funcSections" :key="section.key">
        <view class="section-head">
          <SectionTitle>{{ section.title }}</SectionTitle>
        </view>

        <view
            class="setting-row"
            v-for="item in section.list"
            :key="item.url || item.key"
            @click="goFunc(item)"
        >
          <view class="row-icon" :style="{backgroundColor: item.bgColor}">
            <ThemeGlyph
                v-if="item.glyph"
                :dark="isDark"
                size="small"
                :style="{color: item.color}"
            />
            <up-icon v-else :name="item.icon" size="17" :color="item.color"></up-icon>
          </view>
          <text class="row-title">{{ item.label }}</text>

          <!-- 当前租户在标题右侧显示租户名 -->
          <text v-if="item.action === 'tenant'" class="row-value">{{ userInfo.tenantName || '未分配' }}</text>

          <!-- 主题入口用开关表达当前状态，点击整行同样生效 -->
          <up-switch
              v-if="item.action === 'theme'"
              :model-value="isDark"
              :size="20"
              :active-color="switchColor"
              :inactive-color="switchOffColor"
          />
          <up-icon v-else name="arrow-right" size="12" :color="chevronColor"/>
        </view>
      </view>
    </scroll-view>

    <!-- 底部：退出登录 + 版本号，内边距避让上探约 26px 的 AI 圆钮 -->
    <div class="my-footer">
      <view class="logout-btn" @click="modalVisible = true">
        <text class="logout-text">退出登录</text>
      </view>
      <text class="version-text">v{{ appVersion }}</text>
    </div>

    <!-- 弹层宿主：小程序端 modal/switch 内部 popup 的插槽内容会参与 flex 分配顶高底栏 -->
    <view class="sheet-host">
      <up-modal
          :show="modalVisible"
          title="提示"
          content="是否确定退出系统?"
          showCancelButton
          @confirm="handleLogout"
          @cancel="modalVisible = false"
      />

      <!-- 切换租户弹层 -->
      <tenant-switch
          :show="tenantPopupVisible"
          :current-tenant-id="userInfo.currentTenantId"
          @update:show="tenantPopupVisible = $event"
      />
    </view>

    <AppTabbar :value="active" @change="handleOnTabChange"/>
  </div>
</template>
<script setup>
import {ref, computed} from "vue";
import {onShow} from "@dcloudio/uni-app";
import {useStore} from "vuex";
import {navBarInfo} from "@/utils/navBarUtil";
import {useTheme} from "@/utils/useTheme";
import {getPrimaryColor, getChevronColor, getThemeToken, getOnFillColor} from "@/utils/themeUtil";
import {getSessionAPI, logoutAPI} from "@/api/auth/login";
import UserAvatar from "@/components/UserAvatar/index.vue"
import ThemeGlyph from "@/components/ThemeGlyph/index.vue"
import TenantSwitch from "@/components/TenantSwitch/index.vue"
import AppTabbar from "@/components/AppTabbar/index.vue"
import SectionTitle from "@/components/SectionTitle/index.vue"

const store = useStore();
const active = ref("my");
const {isDark, themeClass, toggleTheme} = useTheme();
const modalVisible = ref(false);
// 切换租户弹层显隐
const tenantPopupVisible = ref(false);

// 版本号由 vite.config.js 的 define 在编译期从 package.json 注入，与设置页同一来源
const appVersion = __APP_VERSION__;

// 开关选中色、行尾箭头，组件只能传实色
const switchColor = computed(() => getPrimaryColor(isDark.value));
// 关闭态轨道：u-switch 只吃实色，用比分割线稍重一档的灰，否则亮色下整条轨道几乎看不见
const switchOffColor = computed(() => isDark.value ? '#3a4a63' : '#d5dbe6');
const chevronColor = computed(() => getChevronColor(isDark.value));

// 每次进入都刷一次会话信息：部门路径、租户名等字段由后端补齐，避免沿用旧缓存
onShow(() => {
  getSessionAPI().then(res => {
    if (res.code === 200 && res.data) {
      store.dispatch("user/setUserInfo", {userInfo: res.data});
    }
  });
});

// 小程序端渐变头延伸到状态栏，H5 保持原间距
const headerStyle = computed(() => {
  // #ifdef MP-WEIXIN
  return {paddingTop: navBarInfo.statusBarHeight + 16 + 'px'}
  // #endif
  // #ifdef H5
  return {paddingTop: '34px'}
  // #endif
})

const userInfo = computed(() => store.getters["user/getUserInfo"] || {
  id: undefined,
  name: undefined,
  deptPath: undefined,
  tenantName: undefined
});

// 功能入口分三组：账号与安全、通用、关于；租户与主题为内置行为，其余按 url 跳转
const funcSections = computed(() => [
  {
    key: 'account',
    title: '账号与安全',
    list: [
      {label: '个人信息', icon: 'account-fill', color: getThemeToken('tileCyan', isDark.value), bgColor: getThemeToken('tileCyanBg', isDark.value), url: '/pages/my/information'},
      {label: '修改密码', icon: 'lock-fill', color: getThemeToken('tileGreen', isDark.value), bgColor: getThemeToken('tileGreenBg', isDark.value), url: '/pages/my/updatePwd'},
      {label: '修改头像', icon: 'photo-fill', color: getThemeToken('tileOrange', isDark.value), bgColor: getThemeToken('tileOrangeBg', isDark.value), url: '/pages/my/updateAvatar'},
      {key: 'tenant', action: 'tenant', label: '当前租户', icon: 'home-fill', color: getThemeToken('tileBlue', isDark.value), bgColor: getThemeToken('tileBlueBg', isDark.value)}
    ]
  },
  {
    key: 'general',
    title: '通用',
    list: [
      {
        key: 'theme',
        action: 'theme',
        label: '深色模式',
        glyph: true,
        color: getThemeToken('tileTheme', isDark.value),
        bgColor: getThemeToken('tileThemeBg', isDark.value)
      }
    ]
  },
  {
    key: 'about',
    title: '关于',
    list: [
      {key: 'service', action: 'agreement', type: 'service', label: '用户服务协议', icon: 'file-text-fill', color: getThemeToken('tileBlue', isDark.value), bgColor: getThemeToken('tileBlueBg', isDark.value)},
      {key: 'privacy', action: 'agreement', type: 'privacy', label: '隐私政策', icon: 'info-circle-fill', color: getThemeToken('tilePurple', isDark.value), bgColor: getThemeToken('tilePurpleBg', isDark.value)}
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
  if (item.action === 'tenant') {
    tenantPopupVisible.value = true;
    return;
  }
  if (item.action === 'agreement') {
    uni.navigateTo({url: `/pages/agreement/index?type=${item.type}`});
    return;
  }
  uni.navigateTo({
    url: item.url
  })
}

/**
 * 退出登录：清本地会话后回登录页
 */
function handleLogout() {
  modalVisible.value = false;
  logoutAPI().then(() => {
    store.dispatch('user/logout');
    uni.reLaunch({
      url: '/pages/login'
    });
  });
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
  background-color: $app-bg-page;
}

.my-header {
  position: relative;
  flex: none;
  width: 100%;
  padding-bottom: 18px;
  overflow: hidden;
  border-radius: 0 0 22px 22px;
  box-shadow: $app-shadow-hero;
  background-image: $app-gradient-profile;
  background-color: $app-bg-mid;
}

.my-header::after {
  content: '';
  position: absolute;
  right: -78px;
  top: -96px;
  width: 214px;
  height: 214px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.26);
  box-shadow: 0 0 0 26px rgba(255, 255, 255, 0.06) inset;
  pointer-events: none;
  z-index: 0;
}

.my-header::before {
  content: '';
  position: absolute;
  left: -40px;
  top: -60px;
  width: 220px;
  height: 200px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.22), rgba(255, 255, 255, 0) 68%);
  pointer-events: none;
  z-index: 0;
}

.my-avatar-row {
  position: relative;
  z-index: 1;
  width: 100%;
  padding: 10px 16px 0;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  gap: 13px;
}

.my-avatar-ring {
  flex: none;
  padding: 2px;
  border-radius: 50%;
  background-image: linear-gradient(150deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.28));
  box-shadow: 0 10px 20px -12px rgba(4, 20, 40, 0.7);
  line-height: 0;
}

.my-info-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  box-sizing: border-box;
}

.my-name-text {
  font-size: 19px;
  line-height: 1.3;
  letter-spacing: 0.4px;
  color: $app-fill-tx;
  font-weight: 700;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
}

.my-dept-text {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.5;
  letter-spacing: 0.2px;
  font-weight: 400;
  color: $app-fill-tx;
  opacity: 0.82;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
}

.my-tenant-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  max-width: 100%;
  margin-top: 7px;
  padding: 2px 8px;
  border-radius: 999px;
  background-color: rgba(255, 255, 255, 0.18);
  box-sizing: border-box;
}

.my-tenant-text {
  font-size: 11px;
  line-height: 1.5;
  letter-spacing: 0.2px;
  color: $app-fill-tx;
  opacity: 0.92;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.my-content {
  flex: 1;
  height: 0;
  min-height: 0;
  padding: 14px 14px 12px;
  box-sizing: border-box;
  position: relative;
  z-index: 1;
}

.section-card {
  padding: 13px 14px 2px;
  border-radius: 16px;
  background-color: $app-bg-card;
  border: 1px solid $app-border-light;
  box-shadow: $app-shadow-card;
}

.section-card + .section-card {
  margin-top: 12px;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 7px;
  padding-bottom: 2px;
}

.setting-row {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 10px 0;
  border-top: 1px solid $app-divide;

  &:active {
    opacity: 0.72;
  }
}

.row-icon {
  flex: none;
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.row-title {
  flex: 1;
  min-width: 0;
  font-size: 13.5px;
  font-weight: 500;
  color: $app-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row-value {
  flex: none;
  max-width: 45%;
  font-size: 12px;
  color: $app-text-tertiary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.my-footer {
  flex: none;
  padding: 6px 16px 38px;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 52px;
  border-radius: 15px;
  background-color: $app-bg-card;
  border: 1px solid $app-border-light;
  box-shadow: $app-shadow-card;

  &:active {
    background-color: $app-bg-hover;
  }
}

.logout-text {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
  color: $app-text-error;
}

.version-text {
  display: block;
  margin-top: 9px;
  text-align: center;
  font-size: 11px;
  letter-spacing: 0.4px;
  color: $app-text-tertiary;
}
</style>

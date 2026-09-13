<template>
  <div class="my-box safe-area-page">
    <div class="my-header">
      <view class="my-setting-entry" @click="goSetting">
        <up-icon name="setting" size="22" color="#ffffff"/>
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
          <text class="my-dept-text">{{userInfo.deptPath}}</text>
        </div>
      </div>
    </div>

    <div class="my-content">
      <view class="func-card">
        <text class="func-card-title">常用功能</text>
        <view class="func-grid">
          <view
              class="func-item"
              v-for="item in funcList"
              :key="item.url"
              @click="goFunc(item.url)"
          >
            <view class="func-icon-wrap" :style="{backgroundColor: item.bgColor}">
              <up-icon :name="item.icon" size="32" :color="item.color"></up-icon>
            </view>
            <text class="func-label">{{ item.label }}</text>
          </view>
        </view>
      </view>
    </div>

    <up-tabbar :value="active" @change="handleOnTabChange" activeColor="#0052cc">
      <up-tabbar-item
          v-for="tab in tabBarItems"
          :key="tab.name"
          :name="tab.name"
          :icon="tab.icon"
          :text="tab.text"
      />
    </up-tabbar>
  </div>
</template>
<script setup>
import {ref, computed} from "vue";
import {useStore} from "vuex";
import {visibleTabs} from "@/utils/menuUtil";
import UserAvatar from "@/components/UserAvatar/index.vue"

const store = useStore();
const active = ref("my");

// 按菜单权限过滤后的底部导航项
const tabBarItems = computed(() => visibleTabs());

const userInfo = computed(() => store.getters["user/getUserInfo"] || {
  id: undefined,
  name: undefined,
  deptPath: undefined
});

// 常用功能入口
const funcList = [
  {label: '通讯录', icon: 'man-add-fill', color: '#1890ff', bgColor: '#e6f7ff', url: '/views/contact/index'},
  {label: '个人信息', icon: 'account-fill', color: '#13c2c2', bgColor: '#e6fffb', url: '/pages/my/information'},
  {label: '修改密码', icon: 'lock-fill', color: '#52c41a', bgColor: '#f6ffed', url: '/pages/my/updatePwd'},
  {label: '修改头像', icon: 'photo-fill', color: '#fa8c16', bgColor: '#fff7e6', url: '/pages/my/updateAvatar'}
];

/**
 * 跳转设置页
 */
function goSetting() {
  uni.navigateTo({
    url: '/pages/my/setting'
  })
}

/**
 * 跳转常用功能
 * @param url 目标页面路径
 */
function goFunc(url) {
  uni.navigateTo({
    url: url
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
}

.my-box :deep(.u-tabbar) {
  flex: none;
}

.my-header {
  padding-top: 30px;
  height: 190px;
  width: 100%;
  position: relative;
  background-image: linear-gradient(180deg,
          #0067ff 0%,
          #3f8fff 60px,
          #7db3ff 120px,
          #ffffff 190px);
}

/* 右上角设置入口 */
.my-setting-entry {
  position: absolute;
  right: 12px;
  top: 22px;
  z-index: 2;
  padding: 6px;
}

.my-avatar-row {
  width: 90%;
  /* 用最小高度而非固定高度，名称/部门多行时不被裁切 */
  min-height: 100px;
  margin: auto;
  overflow: hidden;
  position: relative;
  top: 10px;
}

.my-avatar-left {
  float: left;
  width: 30%;
  overflow: hidden;
}

.my-info-right {
  width: 70%;
  /* 用最小高度而非固定高度：内容不足时保持与头像居中对齐，多行时随内容撑高，不裁切 */
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
  letter-spacing: 1.2px;
  color: #ffffff;
  font-weight: bolder;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
}

.my-dept-text {
  font-size: 16px;
  line-height: 1.4;
  letter-spacipcng: 1.2px;
  color: #ebebeb;
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
}

.func-card {
  width: 90%;
  margin: 20px auto 0;
  background-color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.85);
  border-radius: 10px;
  padding: 16px 8px;
  box-shadow: 0 6px 20px rgba(0, 52, 120, 0.12);
}

.func-card-title {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
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
  color: #9e9e9e;
}
</style>

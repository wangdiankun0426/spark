<template>
  <div class="my-box safe-area-page">
    <div class="my-header">
      <div class="my-avatar-row">
        <div class="my-avatar-left">
          <UserAvatar type="user" :userId="userInfo.id" :name="userInfo.name" :size="100"/>
        </div>

        <div class="my-info-right">
          <text class="my-name-text">{{userInfo.name}}（{{userInfo.loginName}}）<br>
            <text class="my-dept-text">{{userInfo.deptPath}}</text>
          </text>
        </div>
      </div>
    </div>

    <div class="my-content">
      <div class="my-card">
      </div>

      <up-cell-group class="my-cell-group">
        <up-cell title="个人信息" url="/pages/my/infomation" isLink />
        <up-cell title="修改密码" url="/pages/my/updatePwd" isLink />
        <up-cell title="修改头像" url="/pages/my/updateAvatar" isLink />
      </up-cell-group>
    </div>

    <div>
      <div class="my-logout-wrapper">
        <up-button
            type="primary"
            class="logout-button"
            @click="modalVisible = true"
        >
          <text class="logout-text">退出登录</text>
        </up-button>
      </div>
      <up-modal
          title="标题"
          content="是否确定退出系统?"
          :show="modalVisible"
          @confirm="handleLogout"
          showCancelButton
          @cancel="modalVisible = false"
          :asyncClose="true"
      />
    </div>

    <up-tabbar :value="active" @change="handleOnTabChange" activeColor="#0052cc">
      <up-tabbar-item name="home" icon="home-fill" text="首页"/>
      <up-tabbar-item name="contacts" icon="man-add-fill" text="通讯录"/>
      <up-tabbar-item name="agent" icon="grid-fill" text="智能体"/>
      <up-tabbar-item name="message" icon="chat-fill" text="通知"/>
      <up-tabbar-item name="my" icon="account" text="我的"/>
    </up-tabbar>
  </div>
</template>
<script setup>
import {logoutAPI} from "@/api/auth/login";
import {ref, computed} from "vue";
import {useStore} from "vuex";
import UserAvatar from "@/components/UserAvatar/index.vue"

const store = useStore();
const active = ref("my");
const userInfo = computed(() => store.getters["user/getUserInfo"] || {
  id: undefined,
  name: undefined,
  loginName: undefined,
  deptPath: undefined
});
const modalVisible = ref(false);
/**
 * 登出系统
 */
function handleLogout() {
  logoutAPI().then(() => {
    store.dispatch('user/logout');
    uni.reLaunch({
      url: '/pages/login'
    });
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
}

.my-header {
  padding-top: 30px;
  height: 150px;
  width: 100%;
  background-color: #0052cc;
}

.my-avatar-row {
  width: 90%;
  height: 100px;
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
  height: 80px;
  float: right;
  display: flex;
  align-items: center;
}

.my-name-text {
  font-size: 18px;
  color: #ffffff;
  font-weight: bolder;
}

.my-dept-text {
  font-size: 18px;
  color: #d7d7d7;
}

.my-content {
  position: relative;
  top: -30px;
}

.my-card {
  width: 90%;
  margin: auto;
  height: 80px;
  background-color: #fff;
  border-radius: 10px;
  overflow: hidden;
}

.my-cell-group {
  margin-top: 20px;
}

.my-logout-wrapper {
  margin: auto;
  margin-top: 340px;
  width: 90%;
}

.logout-button {
  width: 100%;
  background-color: #0052cc !important;
  height: 50px;
}

.logout-text {
  font-size: 20px;
  font-weight: bolder;
  color: #ffffff;
}
</style>

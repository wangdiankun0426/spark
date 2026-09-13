<template>
  <div class="safe-area-page">
    <!-- 顶部标题栏 -->
    <view class="info-header">
      <view class="header-left" @click="onClickLeft">
        <up-icon name="arrow-left" size="20" color="#fff"></up-icon>
        <text class="header-title">个人信息</text>
      </view>
    </view>

    <div class="info-form">
      <up-form>
        <up-form-item
            label-width="100px"
            label="头像"
        >
          <user-avatar type="user" :userId="userInfo.id" :name="userInfo.name" :size="80"/>
        </up-form-item>
        <up-form-item
            label-width="100px"
            label="用户名称"
        >
          <up-input v-model="userInfo.name" placeholder="未填写" readonly border="none"/>
        </up-form-item>
        <up-form-item
            label-width="100px"
            label="登录名"
        >
          <up-input v-model="userInfo.loginName" placeholder="未填写" readonly border="none"/>
        </up-form-item>
        <up-form-item
            label-width="100px"
            label="所属部门"
        >
          <up-input v-model="userInfo.deptPath" placeholder="未填写" readonly border="none"/>
        </up-form-item>
        <up-form-item
            label-width="100px"
            label="所属角色"
        >
          <up-input v-model="userInfo.roleNames" placeholder="未填写" readonly border="none"/>
        </up-form-item>
        <up-form-item
            label-width="100px"
            label="手机号"
        >
          <up-input v-model="userInfo.phone" placeholder="未填写" readonly border="none"/>
        </up-form-item>
        <up-form-item
            label-width="100px"
            label="邮箱"
        >
          <up-input v-model="userInfo.email" placeholder="未填写" readonly border="none"/>
        </up-form-item>
        <up-form-item
            label-width="100px"
            label="性别"
        >
          <up-input v-model="userInfo.sexName" placeholder="未填写" readonly border="none"/>
        </up-form-item>
      </up-form>
    </div>

  </div>
</template>
<script setup>
import {computed} from "vue";
import {useStore} from "vuex";
import UserAvatar from "@/components/UserAvatar/index.vue"

const store = useStore();
const userInfo = computed(() => store.getters["user/getUserInfo"] || {
  id: undefined,
  name: undefined,
  loginName: undefined,
  deptPath: undefined,
  sexName: undefined
});

/*
  * 返回
  */
function onClickLeft() {
  uni.navigateBack();
 }
</script>
<style scoped lang="scss">
.info-header {
  flex-shrink: 0;
  padding: 16px 16px;
  background-color: #0052cc;
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #ffffff;
  }
}
.info-form {
  padding-top: 20px;
  width: 90%;
  margin: auto;
}
</style>

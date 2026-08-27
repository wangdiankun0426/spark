<template>
  <div class="login-box safe-area-page">
    <div class="login-header">
      <text class="login-title">登录</text><br>
      <text class="login-subtitle">(欢迎使用星火云应用平台)</text>
    </div>
    <div class="login-middle">
      <div class="login-middle-form">
        <up-form labelWidth="60">
          <!--账号登录-->
          <div v-if = "loginForm.loginType === 1">
            <up-form-item
                label="用户名"
                prop="loginForm.username"
            >
              <up-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名"
                  border="bottom"
                  clearable
              >
              </up-input>
            </up-form-item>
            <up-form-item
                label="密码"
                prop="loginForm.password"
            >
              <up-input
                  v-model="loginForm.password"
                  :placeholder="'请输入密码'"
                  :type="showPassword ? 'text' : 'password'"
                  border="bottom"
                  clearable
              >
                <template #suffix>
                  <up-icon
                      :name="showPassword ? 'eye-fill' : 'eye-off'"
                      size="22"
                      color="#909399"
                      @click="showPassword = !showPassword"
                  ></up-icon>
                </template>
              </up-input>
            </up-form-item>
            <up-form-item
                label="验证码"
                prop="loginForm.validateValue"
            >
              <!-- #ifdef H5 -->
              <up-input
                  v-model="loginForm.validateValue"
                  placeholder="请输入验证码"
                  border="bottom"
                  clearable
              >
                <template #suffix>
                  <view @click="getValidateImg">
                    <img :src="validateImg" width="120" height="50"/>
                  </view>
                </template>
              </up-input>
              <!-- #endif -->
              <!-- #ifdef MP-WEIXIN -->
              <view class="code-input-wrap">
                <up-input
                    v-model="loginForm.validateValue"
                    placeholder="请输入验证码"
                    border="bottom"
                    clearable
                    class="code-input-flex"
                />
                <image v-if="validateImg" :src="validateImg" class="validate-img" mode="scaleToFill" @click="getValidateImg" @error="onValidateImgError" @load="onValidateImgLoad" />
              </view>
              <!-- #endif -->
            </up-form-item>
          </div>
          <!--手机号登录-->
          <div v-if = "loginForm.loginType === 2">
            <up-form-item
                label="手机号"
                prop="loginForm.phone"
            >
              <up-input
                  v-model="loginForm.phone"
                  placeholder="请输入手机号"
                  border="bottom"
                  clearable
              >
              </up-input>
            </up-form-item>
            <up-form-item
                label="验证码"
                prop="loginForm.validateValue"
            >
              <up-input
                  v-model="loginForm.validateValue"
                  placeholder="请输入验证码"
                  border="bottom"
                  clearable
              >
                <template #suffix>
                  <up-button class="sms-code-btn" plain size="mini" type="primary" @click="getMessageCode" :disabled="count != 0">
                    <text v-if="count == 0">获取验证码</text>
                    <text v-else>等待({{count}}秒)</text>
                  </up-button>
                </template>
              </up-input>
            </up-form-item>
          </div>
          <!--邮箱登录-->
          <div v-if = "loginForm.loginType === 3">
            <up-form-item
                label="邮箱"
                prop="loginForm.email"
            >
              <up-input
                  v-model="loginForm.email"
                  placeholder="请输入邮箱"
                  border="bottom"
                  clearable
              >
              </up-input>
            </up-form-item>
            <up-form-item
                label="验证码"
                prop="loginForm.validateValue"
            >
              <up-input
                  v-model="loginForm.validateValue"
                  placeholder="请输入验证码"
                  border="bottom"
                  clearable
              >
                <template #suffix>
                  <up-button class="sms-code-btn" plain size="mini" type="primary" @click="getEmailCode" :disabled="count !== 0">
                    <text v-if="count == 0">获取验证码</text>
                    <text v-else>等待({{count}}秒)</text>
                  </up-button>
                </template>
              </up-input>
            </up-form-item>
          </div>
        </up-form>
      </div>
      <div class="login-middle-btn">
        <up-button
            class="login-submit-btn"
            @click="submitLoginForm"
            type="primary"
            :loading="loading"
            loading-text="登 录 中...">
          <text class="login-text">登 录</text>
        </up-button>
      </div>
    </div>

    <up-divider text="其他方式登录"  textColor="#0052cc"
                lineColor="#0052cc"/>

    <div class="login-type-icons">
      <view class="login-type-icon-item" v-if="loginForm.loginType !== 1" @click="switchLoginType(1)">
        <view class="icon-circle">
          <up-icon name="lock" size="24" color="#0052cc"></up-icon>
        </view>
        <text class="icon-label">密码登录</text>
      </view>
      <view class="login-type-icon-item" v-if="loginForm.loginType !== 2" @click="switchLoginType(2)">
        <view class="icon-circle">
          <up-icon name="phone" size="24" color="#0052cc"></up-icon>
        </view>
        <text class="icon-label">手机号登录</text>
      </view>
      <view class="login-type-icon-item" v-if="loginForm.loginType !== 3" @click="switchLoginType(3)">
        <view class="icon-circle">
          <up-icon name="email" size="24" color="#0052cc"></up-icon>
        </view>
        <text class="icon-label">邮箱登录</text>
      </view>
    </div>
  </div>
</template>
<script setup>
import {loginAPI, getValidateCodeAPI, getMessageCodeAPI, getEmailCodeAPI} from '@/api/auth/login.js';
import {des} from '@/utils/encryptUtil';
import {ref} from 'vue';
import {useStore} from 'vuex';

const store = useStore();
const loading = ref(false);
const showPassword = ref(false);
const loginForm = ref({
  username: undefined,
  password: undefined,
  phone: undefined,
  email: undefined,
  validateId: undefined,
  validateValue:undefined,
  loginType: 1
});
const validateImg = ref(undefined);
const count = ref(0);
const timer = ref(null);

getValidateImg();

/**
 * 获取邮箱验证码
 */
function getEmailCode() {
  const param = {
    email: loginForm.value.email
  }
  getEmailCodeAPI(param).then(res => {
    if (res.code !== 200) {
      return ;
    }
    loginForm.value.validateId = res.data.uuid
    count.value = 60;
    clearInterval(timer.value); // 清除之前的计时器
    timer.value = setInterval(() => {
      if (count.value > 0) {
        count.value -= 1;
      } else {
        clearInterval(timer.value); // 计时结束，清除计时器
      }
    }, 1000);
  })
}
/**
 * 获取短信验证码
 */
function getMessageCode() {
  const param = {
    phone: loginForm.value.phone
  }
  getMessageCodeAPI(param).then(res => {
    if (res.code !== 200) {
      return ;
    }
    loginForm.value.validateId = res.data.uuid
    this.count = 60;
    clearInterval(this.timer); // 清除之前的计时器
    this.timer = setInterval(() => {
      if (this.count > 0) {
        this.count -= 1;
      } else {
        clearInterval(this.timer); // 计时结束，清除计时器
      }
    }, 1000);
  })
}
/**
 * 切换登录类型
 */
function switchLoginType(type) {
  loginForm.value.loginType = type;
}
/**
 * 获取验证码
 */
function onValidateImgError(e) {
  console.error('验证码图片加载失败:', JSON.stringify(e.detail));
}
function onValidateImgLoad(e) {
  console.log('验证码图片加载成功');
}
function getValidateImg() {
  getValidateCodeAPI().then(res => {
    if (res.code !== 200) {
      return ;
    }
    // 去除 base64 中可能存在的换行符等空白字符
    const cleanBase64 = res.data.img.replace(/[\s\r\n]+/g, '');
    validateImg.value = `data:image/gif;base64,${cleanBase64}`;
    loginForm.value.validateId = res.data.uuid;
  });
}
/**
 * 提交登录表单
 */
function submitLoginForm() {
  loading.value = true;
  const data = {};
  if (loginForm.value.loginType === 1) {
    data.loginName =  loginForm.value.username;
    data.password = des(loginForm.value.password);
    data.validateId = loginForm.value.validateId;
    data.validateValue = loginForm.value.validateValue;
    data.loginType = loginForm.value.loginType;
    data.loginPlatform = 2;
  } else if (loginForm.value.loginType === 2) {
    data.phone = loginForm.value.phone;
    data.validateId = loginForm.value.validateId;
    data.validateValue = loginForm.value.validateValue;
    data.loginType = loginForm.value.loginType;
    data.loginPlatform = 2;
  } else if (loginForm.value.loginType === 3) {
    data.email = loginForm.value.email;
    data.validateId = loginForm.value.validateId;
    data.validateValue = loginForm.value.validateValue;
    data.loginType = loginForm.value.loginType;
    data.loginPlatform = 2;
  }
  store.dispatch('user/login', {loginForm: data}).then(res => {
    if (res.code === 200) {
      // 关闭所有页面，打开应用内的某个页面
      uni.reLaunch({
        url: '/pages/home/index'
      });
    } else {
      if (res.code === 604) {
        getValidateImg();
      }
      loading.value = false;
    }
  }).catch(() => {
    loading.value = false;
  });
}
</script>
<style scoped lang="scss">
.login-box {
  padding: 0;
  height: 100vh;
}
.login-header {
  padding-top: 200px;
  padding-bottom:10px;
  width: 90%;
  margin: auto;
}
.login-middle {
  width: 90%;
  margin: auto;
  overflow: hidden;
}
.login-middle-form {
  padding: 5px 10px 5px 10px;
  border-radius: 10px;
}
.login-type-btn {
  float: left;
  margin-left: 10rpx;
}

.login-title {
  font-size: 36px;
  font-weight: bolder;
  color: #0052cc;
}

.login-subtitle {
  color: #3f78cd;
  font-size: 18px;
  font-weight: bolder;
}

.code-input-wrap {
  display: flex;
  align-items: center;
  width: 100%;
}

.code-input-flex {
  flex: 1;
}

.validate-img {
  width: 100px;
  height: 40px;
  margin-left: 8px;
}

.sms-code-btn {
  height: 38px;
  font-size: 18px;
}

.login-type-icons {
  display: flex;
}

.login-type-icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-left: 30px;
}

.icon-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #0052cc;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
}

.icon-label {
  font-size: 12px;
  color: #606266;
}

.login-middle-btn {
  margin-top: 20px;
  overflow: hidden;
}

.login-submit-btn {
  background-color: #0052cc;
  height: 50px;
  font-weight: bolder;
  font-size: 20px;
}

.logout-text {
  font-size: 20px;
  font-weight: bolder;
  color: #ffffff;
}
</style>

<template>
  <div class="login-box safe-area-page">
    <!-- 科技风背景装饰层：纯 CSS 实现，规避小程序不支持的 SVG / blur / mask -->
    <view class="login-bg">
      <view class="bg-grid"></view>
      <view class="bg-blob bg-blob-1"></view>
      <view class="bg-blob bg-blob-2"></view>
      <view class="bg-orbit bg-orbit-1"></view>
      <view class="bg-orbit bg-orbit-2"></view>
      <view class="bg-dot bg-dot-1"></view>
      <view class="bg-dot bg-dot-2"></view>
      <view class="bg-dot bg-dot-3"></view>
    </view>

    <!-- 品牌区 -->
    <view class="login-brand">
      <text class="login-title">星火云应用平台</text>
      <view class="brand-bar"></view>
      <text class="login-subtitle">欢迎使用 — 星火云应用平台</text>
    </view>

    <!-- 登录表单区 -->
    <view class="login-card">
      <view class="login-card-head">
        <view class="login-card-bar"></view>
        <text class="login-card-title">{{ loginForm.loginType === 1 ? '密码登录' : (loginForm.loginType === 2 ? '手机号登录' : '邮箱登录') }}</text>
      </view>
      <!-- 表单区固定高度：密码登录 3 行、手机号/邮箱登录 2 行，
           固定高度可避免切换登录方式时下方按钮与整页布局跳动 -->
      <view class="login-form-body">
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
      </view>
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
    </view>

    <!-- 其他方式登录 -->
    <view class="login-other">
      <up-divider text="其他方式登录" textColor="#9296a5" lineColor="#d9e2f2"/>

      <div class="login-type-icons">
        <view class="login-type-icon-item" v-if="loginForm.loginType !== 1" @click="switchLoginType(1)">
          <view class="icon-circle">
            <up-icon name="lock" size="24" color="#004fc5"></up-icon>
          </view>
        </view>
        <view class="login-type-icon-item" v-if="loginForm.loginType !== 2" @click="switchLoginType(2)">
          <view class="icon-circle">
            <up-icon name="phone" size="24" color="#004fc5"></up-icon>
          </view>
        </view>
        <view class="login-type-icon-item" v-if="loginForm.loginType !== 3" @click="switchLoginType(3)">
          <view class="icon-circle">
            <up-icon name="email" size="24" color="#004fc5"></up-icon>
          </view>
        </view>
        <!-- #ifdef MP-WEIXIN -->
        <view class="login-type-icon-item" @click="wxQuickLogin">
          <view class="icon-circle">
            <up-icon name="weixin-fill" size="24" color="#07c160"></up-icon>
          </view>
        </view>
        <!-- #endif -->
      </div>
    </view>
    <!-- 版权说明 -->
    <view class="login-copyright">
      <text class="copyright-text">Copyright © 2024-2026 evancloud.top All Rights Reserved.</text>
    </view>
  </div>
</template>
<script setup>
import {loginAPI, getValidateCodeAPI, getMessageCodeAPI, getEmailCodeAPI, getEncryptKeyAPI} from '@/api/auth/login.js';
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

// #ifdef MP-WEIXIN
/**
 * 微信静默免密登录
 */
function wxQuickLogin() {
  if (loading.value) {
    return;
  }
  loading.value = true;
  uni.login({
    provider: 'weixin',
    success: (loginRes) => {
      const data = {
        loginType: 5,
        wxCode: loginRes.code,
        loginPlatform: 6
      };
      store.dispatch('user/login', {loginForm: data}).then(res => {
        loading.value = false;
        if (res.code === 200) {
          // 关闭所有页面，打开应用内的某个页面
          uni.reLaunch({
            url: '/pages/home/index'
          });
        }
      }).catch(() => {
        loading.value = false;
      });
    },
    fail: () => {
      loading.value = false;
    }
  });
}

// 进入登录页时未手动登出则自动静默登录
if (!store.state.user.wxSkipAutoLogin) {
  wxQuickLogin();
}
// #endif

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
async function submitLoginForm() {
  loading.value = true;
  const data = {};
  if (loginForm.value.loginType === 1) {
    // 申请一次性加密密钥
    const keyResult = await getEncryptKeyAPI();
    if (keyResult.code !== 200) {
      loading.value = false;
      return;
    }
    data.loginName =  loginForm.value.username;
    data.password = des(loginForm.value.password, keyResult.data.key);
    data.keyId = keyResult.data.keyId;
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
  position: relative;
  box-sizing: border-box;
  padding: 0 48rpx;
  height: 100vh;
  overflow-y: auto;
  background-image: linear-gradient(160deg, $spark-color-bg-start 0%, $spark-color-bg-mid 55%, $spark-color-bg-end 100%);
}

.login-bg {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
}

.bg-grid {
  position: absolute;
  left: -10%;
  top: -10%;
  width: 120%;
  height: 120%;
  opacity: 0.7;
  background-image:
      linear-gradient(rgba(0, 79, 197, 0.06) 1rpx, transparent 1rpx),
      linear-gradient(90deg, rgba(0, 79, 197, 0.06) 1rpx, transparent 1rpx);
  background-size: 80rpx 80rpx;
}

.bg-blob {
  position: absolute;
  border-radius: 50%;
  animation: blobFloat 18s ease-in-out infinite alternate;
}

.bg-blob-1 {
  width: 520rpx;
  height: 520rpx;
  left: -160rpx;
  top: -140rpx;
  background-image: radial-gradient(circle at 30% 30%, rgba(147, 197, 253, 0.55) 0%, rgba(191, 219, 254, 0.25) 45%, rgba(191, 219, 254, 0) 72%);
}

.bg-blob-2 {
  width: 480rpx;
  height: 480rpx;
  right: -150rpx;
  top: 700rpx;
  background-image: radial-gradient(circle at 60% 40%, rgba(165, 243, 252, 0.5) 0%, rgba(205, 226, 252, 0.22) 45%, rgba(205, 226, 252, 0) 72%);
  animation-delay: -6s;
}

@keyframes blobFloat {
  0% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30rpx, -24rpx) scale(1.08);
  }
  66% {
    transform: translate(-24rpx, 20rpx) scale(0.95);
  }
  100% {
    transform: translate(16rpx, -12rpx) scale(1.04);
  }
}

.bg-orbit {
  position: absolute;
  border: 1rpx dashed rgba(0, 79, 197, 0.22);
  border-radius: 50%;
  animation: orbitSpin 30s linear infinite;
}

.bg-orbit-1 {
  width: 360rpx;
  height: 360rpx;
  right: -120rpx;
  top: -80rpx;
}

.bg-orbit-2 {
  width: 240rpx;
  height: 240rpx;
  left: -80rpx;
  bottom: 120rpx;
  border-color: rgba(0, 150, 255, 0.18);
  animation-direction: reverse;
  animation-duration: 24s;
}

@keyframes orbitSpin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.bg-dot {
  position: absolute;
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background-color: rgba(0, 120, 255, 0.45);
  box-shadow: 0 0 16rpx rgba(0, 120, 255, 0.4);
  animation: dotFloat 8s ease-in-out infinite alternate;
}

.bg-dot-1 {
  left: 18%;
  top: 20%;
}

.bg-dot-2 {
  right: 16%;
  top: 30%;
  animation-delay: -3s;
}

.bg-dot-3 {
  left: 30%;
  bottom: 22%;
  animation-delay: -6s;
}

@keyframes dotFloat {
  0% {
    transform: translateY(0);
    opacity: 0.55;
  }
  100% {
    transform: translateY(-28rpx);
    opacity: 1;
  }
}

.login-brand {
  position: relative;
  z-index: 1;
  padding-top: 300rpx;
  padding-bottom: 56rpx;
}

.login-title {
  display: block;
  font-size: 52rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
  // 小程序不支持 background-clip: text，故以品牌色兜底
  color: $spark-color-primary;
  /* #ifdef H5 */
  background-image: linear-gradient(90deg, #004fc5 0%, #0072e0 45%, #00b8d9 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  /* #endif */
}

.brand-bar {
  width: 96rpx;
  height: 8rpx;
  margin: 20rpx 0;
  border-radius: 8rpx;
  background-image: linear-gradient(90deg, #004fc5 0%, #00b8d9 100%);
}

.login-subtitle {
  display: block;
  font-size: 26rpx;
  color: $spark-color-text-secondary;
  letter-spacing: 1rpx;
}

/* 登录表单区：直接落在背景上，不用白色卡片包裹 */
.login-card {
  position: relative;
  z-index: 1;
}

.login-card-head {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.login-form-body {
  min-height: 200px;
}

.login-card-bar {
  width: 8rpx;
  height: 32rpx;
  margin-right: 14rpx;
  border-radius: 8rpx;
  background-image: linear-gradient(180deg, #004fc5, #00b8d9);
}

.login-card-title {
  font-size: 34rpx;
  font-weight: 600;
  color: $spark-color-text;
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

.login-other {
  position: relative;
  z-index: 1;
  margin-top: 40rpx;
}

.login-type-icons {
  display: flex;
  justify-content: center;
  margin-top: 16rpx;
}

.login-type-icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  // 用 margin 代替 flex gap，兼容低版本小程序基础库
  margin: 0 20rpx;
}

.icon-circle {
  width: 84rpx;
  height: 84rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: #ffffff;
  border: 1rpx solid rgba(0, 79, 197, 0.12);
  box-shadow: 0 6rpx 18rpx rgba(26, 111, 232, 0.1);
}

.login-middle-btn {
  margin-top: 32rpx;
  overflow: hidden;
}

.login-submit-btn {
  height: 88rpx;
  font-size: 32rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
  color: #ffffff;
  border-radius: 12rpx;
  background-color: $spark-color-primary;
  background-image: linear-gradient(90deg, #004fc5 0%, #0072e0 100%);
  box-shadow: 0 12rpx 28rpx rgba(0, 79, 197, 0.28);
}

.logout-text {
  font-size: 20px;
  font-weight: bolder;
  color: #ffffff;
}

.login-copyright {
  position: relative;
  z-index: 1;
  margin-top: 40rpx;
  padding-bottom: 40rpx;
  text-align: center;
}

.copyright-text {
  font-size: 22rpx;
  color: #909399;
}
</style>

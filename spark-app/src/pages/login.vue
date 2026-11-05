<template>
  <div class="login-box safe-area-page" :class="themeClass">
    <!-- 背景装饰：顶部品牌蓝光晕 + 右上细圆环，纯 CSS 规避小程序不支持的 SVG / blur / mask -->
    <view class="login-bg">
      <view class="bg-glow"></view>
      <view class="bg-ring bg-ring-1"></view>
      <view class="bg-ring bg-ring-2"></view>
    </view>

    <!-- 主题切换：登录前也能选好皮肤，只放图标不放文案 -->
    <view class="login-theme-btn" :style="themeToggleStyle" @click="toggleTheme">
      <ThemeGlyph :dark="isDark" size="normal" :style="{color: themeGlyphColor}"/>
    </view>

    <!-- 品牌区：logo 磁贴 + 平台名，下面是问候语，两级层次 -->
    <view class="login-brand">
      <view class="brand-row">
        <view class="brand-logo">
          <image class="brand-logo__img" :src="brandLogo" mode="aspectFit"/>
        </view>
        <view class="brand-name">
          <text class="brand-name__cn">星火云AI</text>
          <text class="brand-name__en">SPARK CLOUD</text>
        </view>
      </view>
      <text class="login-title">欢迎回来</text>
      <text class="login-tip">登录后开启你的 AI 辅助工作之旅</text>
    </view>

    <!-- 表单区：三个白色胶囊输入框直接落在浅蓝背景上，无外层卡片 -->
    <view class="login-form">
      <!--账号登录-->
      <block v-if="loginForm.loginType === 1">
        <view class="field" :class="fieldClass('username')">
          <view class="field-icon">
            <up-icon name="account" size="19" :color="fieldIconColor"/>
          </view>
          <up-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              border="none"
              clearable
              :color="inputTextColor"
              :placeholder-style="inputPlaceholderStyle"
              :custom-style="inputStyle"
              @focus="onFieldFocus('username')"
              @blur="onFieldBlur('username')"
          />
        </view>
        <view v-if="fieldErrors.username" class="field-err">{{ fieldErrors.username }}</view>

        <view class="field" :class="fieldClass('password')">
          <view class="field-icon">
            <up-icon name="lock" size="19" :color="fieldIconColor"/>
          </view>
          <up-input
              v-model="loginForm.password"
              :placeholder="'请输入密码'"
              :type="showPassword ? 'text' : 'password'"
              border="none"
              :color="inputTextColor"
              :placeholder-style="inputPlaceholderStyle"
              :custom-style="inputStyle"
              @focus="onFieldFocus('password')"
              @blur="onFieldBlur('password')"
          >
            <template #suffix>
              <up-icon
                  :name="showPassword ? 'eye-fill' : 'eye-off'"
                  size="20"
                  :color="eyeIconColor"
                  @click="showPassword = !showPassword"
              />
            </template>
          </up-input>
        </view>
        <view v-if="fieldErrors.password" class="field-err">{{ fieldErrors.password }}</view>

        <view class="field" :class="fieldClass('validateValue')">
          <view class="field-icon">
            <up-icon name="integral" size="19" :color="fieldIconColor"/>
          </view>
          <!-- #ifdef H5 -->
          <up-input
              v-model="loginForm.validateValue"
              placeholder="请输入验证码"
              border="none"
              clearable
              :color="inputTextColor"
              :placeholder-style="inputPlaceholderStyle"
              :custom-style="inputStyle"
              @focus="onFieldFocus('validateValue')"
              @blur="onFieldBlur('validateValue')"
          >
            <template #suffix>
              <view @click="getValidateImg">
                <img :src="validateImg" width="96" height="40" class="validate-img-h5"/>
              </view>
            </template>
          </up-input>
          <!-- #endif -->
          <!-- #ifdef MP-WEIXIN -->
          <view class="code-input-wrap">
            <up-input
                v-model="loginForm.validateValue"
                placeholder="请输入验证码"
                border="none"
                clearable
                class="code-input-flex"
                :color="inputTextColor"
                :placeholder-style="inputPlaceholderStyle"
                :custom-style="inputStyle"
                @focus="onFieldFocus('validateValue')"
                @blur="onFieldBlur('validateValue')"
            />
            <image v-if="validateImg" :src="validateImg" class="validate-img" mode="scaleToFill" @click="getValidateImg" @error="onValidateImgError" @load="onValidateImgLoad"/>
          </view>
          <!-- #endif -->
        </view>
        <view v-if="fieldErrors.validateValue && loginForm.loginType === 1" class="field-err">{{ fieldErrors.validateValue }}</view>
      </block>

      <!-- 手机号登录 -->
      <block v-if="loginForm.loginType === 2">
        <view class="field" :class="fieldClass('phone')">
          <view class="field-icon">
            <up-icon name="phone" size="19" :color="fieldIconColor"/>
          </view>
          <up-input
              v-model="loginForm.phone"
              type="number"
              maxlength="11"
              placeholder="请输入手机号"
              border="none"
              clearable
              :color="inputTextColor"
              :placeholder-style="inputPlaceholderStyle"
              :custom-style="inputStyle"
              @focus="onFieldFocus('phone')"
              @blur="onFieldBlur('phone')"
          />
        </view>
        <view v-if="fieldErrors.phone" class="field-err">{{ fieldErrors.phone }}</view>

        <view class="field" :class="fieldClass('validateValue')">
          <view class="field-icon">
            <up-icon name="integral" size="19" :color="fieldIconColor"/>
          </view>
          <up-input
              v-model="loginForm.validateValue"
              placeholder="请输入短信验证码"
              border="none"
              clearable
              :color="inputTextColor"
              :placeholder-style="inputPlaceholderStyle"
              :custom-style="inputStyle"
              @focus="onFieldFocus('validateValue')"
              @blur="onFieldBlur('validateValue')"
          >
            <template #suffix>
              <view class="sms-btn" :class="count > 0 ? 'is-wait' : ''" @click="getSmsCode">
                <text v-if="count === 0">获取验证码</text>
                <text v-else>{{ count }}s 后重发</text>
              </view>
            </template>
          </up-input>
        </view>
        <view v-if="fieldErrors.validateValue && loginForm.loginType === 2" class="field-err">{{ fieldErrors.validateValue }}</view>
      </block>

      <!--邮箱登录-->
      <block v-if="loginForm.loginType === 3">
        <view class="field" :class="fieldClass('email')">
          <view class="field-icon">
            <up-icon name="email" size="19" :color="fieldIconColor"/>
          </view>
          <up-input
              v-model="loginForm.email"
              placeholder="请输入邮箱"
              border="none"
              clearable
              :color="inputTextColor"
              :placeholder-style="inputPlaceholderStyle"
              :custom-style="inputStyle"
              @focus="onFieldFocus('email')"
              @blur="onFieldBlur('email')"
          />
        </view>
        <view v-if="fieldErrors.email" class="field-err">{{ fieldErrors.email }}</view>

        <view class="field" :class="fieldClass('validateValue')">
          <view class="field-icon">
            <up-icon name="integral" size="19" :color="fieldIconColor"/>
          </view>
          <up-input
              v-model="loginForm.validateValue"
              placeholder="请输入邮箱验证码"
              border="none"
              clearable
              :color="inputTextColor"
              :placeholder-style="inputPlaceholderStyle"
              :custom-style="inputStyle"
              @focus="onFieldFocus('validateValue')"
              @blur="onFieldBlur('validateValue')"
          >
            <template #suffix>
              <view class="sms-btn" :class="count > 0 ? 'is-wait' : ''" @click="getEmailCode">
                <text v-if="count === 0">获取验证码</text>
                <text v-else>{{ count }}s 后重发</text>
              </view>
            </template>
          </up-input>
        </view>
        <view v-if="fieldErrors.validateValue && loginForm.loginType === 3" class="field-err">{{ fieldErrors.validateValue }}</view>
      </block>

      <up-button
          class="login-btn"
          @click="submitLoginForm"
          type="primary"
          :color="loginBtnColor"
          :custom-style="loginBtnStyle"
          :loading="loading"
          loading-text="登 录 中...">
        <text class="login-text">登 录</text>
      </up-button>
    </view>

    <!-- 其他方式登录 -->
    <view class="login-other">
      <up-divider text="其他方式登录" :textColor="dividerTextColor" :lineColor="dividerLineColor"/>

      <div class="login-type-icons">
        <view class="login-type-icon-item" v-if="loginForm.loginType !== 1" @click="switchLoginType(1)">
          <view class="icon-circle">
            <up-icon name="lock" size="22" :color="primaryColor"></up-icon>
          </view>
        </view>
        <view class="login-type-icon-item" v-if="loginForm.loginType !== 2" @click="switchLoginType(2)">
          <view class="icon-circle">
            <up-icon name="phone" size="22" :color="primaryColor"></up-icon>
          </view>
        </view>
        <view class="login-type-icon-item" v-if="loginForm.loginType !== 3" @click="switchLoginType(3)">
          <view class="icon-circle">
            <up-icon name="email" size="22" :color="primaryColor"></up-icon>
          </view>
        </view>
        <!-- #ifdef MP-WEIXIN -->
        <view class="login-type-icon-item" @click="wxQuickLogin">
          <view class="icon-circle">
            <up-icon name="weixin-fill" size="22" color="#07c160"></up-icon>
          </view>
        </view>
        <!-- #endif -->
      </div>

      <view class="login-secure">
        <up-icon name="lock" size="12" :color="mutedColor"/>
        <text class="secure-text">安全登录 · 数据全程加密传输</text>
      </view>
    </view>

    <!-- 协议与版权：同意声明放到页脚，勾选凭证逻辑保持不变 -->
    <view class="login-foot">
      <view class="login-agreement" @click="toggleAgree">
        <up-icon
            class="agree-icon"
            :name="agreed ? 'checkmark-circle-fill' : 'checkmark-circle'"
            size="16"
            :color="agreed ? primaryColor : mutedColor"
        ></up-icon>
        <text class="agreement-text">登录即代表同意</text>
        <text class="agreement-link" @click.stop="openAgreement('service')">用户服务协议</text>
        <text class="agreement-text">与</text>
        <text class="agreement-link" @click.stop="openAgreement('privacy')">隐私政策</text>
      </view>
      <view class="login-copyright">
        <text class="copyright-text">Copyright © 2024-2026 evancloud.top All Rights Reserved.</text>
      </view>
    </view>
  </div>
</template>
<script setup>
import {getValidateCodeAPI, getSmsCodeAPI, getEmailCodeAPI, getEncryptKeyAPI} from '@/api/auth/login.js';
import {des} from '@/utils/encryptUtil';
import {AGREEMENT_VERSION} from '@/utils/agreementDoc';
import {useTheme} from '@/utils/useTheme';
import {
  getPrimaryColor,
  getTertiaryColor,
  getTextPrimaryColor,
  getPlaceholderColor,
  getBorderColor,
  getGradientFill
} from '@/utils/themeUtil';
import ThemeGlyph from '@/components/ThemeGlyph/index.vue';
import {navBarInfo} from '@/utils/navBarUtil';
import {ref, computed} from 'vue';
import {useStore} from 'vuex';

const store = useStore();
const {isDark, themeClass, toggleTheme} = useTheme();
const loading = ref(false);
const showPassword = ref(false);

// 主题相关的图标与分隔线配色，避免深色皮肤下底色发灰、文字发暗
const primaryColor = computed(() => getPrimaryColor(isDark.value));
const mutedColor = computed(() => getTertiaryColor(isDark.value));
const dividerTextColor = computed(() => getTertiaryColor(isDark.value));
const dividerLineColor = computed(() => getBorderColor(isDark.value));

// 主题按钮与密码眼睛的图标色，按皮肤给实色
const themeGlyphColor = computed(() => (isDark.value ? '#f5b544' : '#4b6584'));
const eyeIconColor = computed(() => getTertiaryColor(isDark.value));

// 输入框内左侧图标色，跟随占位文字一档
const fieldIconColor = computed(() => getTertiaryColor(isDark.value));

// uview 把输入框文字色写死成 JS 默认值内联下发，小程序对 CSS 变量内联支持不稳，这里按主题给实色
const inputTextColor = computed(() => getTextPrimaryColor(isDark.value));
const inputPlaceholderStyle = computed(() => 'color:' + getPlaceholderColor(isDark.value));

// 输入框透明，胶囊外壳的底色描边由 .field 承担
const inputStyle = computed(() => ({
  backgroundColor: 'transparent',
  height: '100%',
  fontSize: '27rpx'
}));

// 登录按钮走品牌渐变填充，暗色为降亮度的深蓝渐变
const loginBtnColor = computed(() => getGradientFill(isDark.value));

// 品牌 logo 跟随主题色系：浅色用白底淡色磁贴，深色用深蓝渐变磁贴
const brandLogo = computed(() => (isDark.value ? '/static/icons/logo.png' : '/static/icons/logo-light.png'));

const loginBtnStyle = computed(() => ({
  height: '104rpx',
  borderRadius: '28rpx',
  boxShadow: '$app-shadow-brand',
  fontSize: '31rpx',
  fontWeight: '700',
  letterSpacing: '6rpx',
  border: 'none'
}));

// 切换按钮贴状态栏下方，小程序端避开微信胶囊按钮
const themeToggleStyle = computed(() => {
  // #ifdef MP-WEIXIN
  return {top: navBarInfo.menuBottom + 10 + 'px'}
  // #endif
  // #ifdef H5
  return {top: '16px'}
  // #endif
});

// 输入框聚焦态与前端必填校验
const focusField = ref('');
const fieldErrors = ref({});

function fieldClass(key) {
  return {
    'is-focus': focusField.value === key,
    'is-error': !!fieldErrors.value[key]
  };
}

function onFieldFocus(key) {
  focusField.value = key;
  if (fieldErrors.value[key]) {
    fieldErrors.value = {...fieldErrors.value, [key]: ''};
  }
}

function onFieldBlur() {
  focusField.value = '';
}

/**
 * 前端必填校验：不通过时把错误文案挂到对应输入框下方
 */
function validateFields() {
  const f = loginForm.value;
  const t = f.loginType;
  const errs = {};
  if (t === 1) {
    if (!f.username || !f.username.trim()) {
      errs.username = '请输入用户名';
    }
    if (!f.password) {
      errs.password = '请输入密码';
    }
    if (!f.validateValue) {
      errs.validateValue = '请输入验证码';
    }
  } else if (t === 2) {
    if (!/^1\d{10}$/.test(f.phone || '')) {
      errs.phone = '请输入正确的手机号';
    }
    if (!f.validateValue) {
      errs.validateValue = '请输入短信验证码';
    }
  } else if (t === 3) {
    if (!/^[\w.-]+@[\w-]+(\.[\w-]+)+$/.test(f.email || '')) {
      errs.email = '请输入正确的邮箱地址';
    }
    if (!f.validateValue) {
      errs.validateValue = '请输入邮箱验证码';
    }
  }
  fieldErrors.value = errs;
  return Object.keys(errs).length === 0;
}

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

const AGREEMENT_KEY = 'sparkAgreementAgree';

/**
 * 读取本地协议同意凭证，版本不一致视为未同意
 */
function getStoredAgreed() {
  const record = uni.getStorageSync(AGREEMENT_KEY);
  return !!(record && record.version === AGREEMENT_VERSION);
}

const agreed = ref(getStoredAgreed());

/**
 * 切换协议勾选状态并持久化
 */
function toggleAgree() {
  agreed.value = !agreed.value;
  if (agreed.value) {
    uni.setStorageSync(AGREEMENT_KEY, {version: AGREEMENT_VERSION, time: Date.now()});
  } else {
    uni.removeStorageSync(AGREEMENT_KEY);
  }
}

/**
 * 打开协议文本页
 */
function openAgreement(type) {
  uni.navigateTo({url: `/pages/agreement/index?type=${type}`});
}

// #ifdef MP-WEIXIN
/**
 * 微信静默免密登录
 */
function wxQuickLogin() {
  if (!agreed.value) {
    uni.showToast({title: '请先阅读并同意用户服务协议和隐私政策', icon: 'none'});
    return;
  }
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

// 进入登录页时未手动登出且已同意协议才自动静默登录，避免同意前收集 openid
if (!store.state.user.wxSkipAutoLogin && agreed.value) {
  wxQuickLogin();
}
// #endif

getValidateImg();

/**
 * 获取邮箱验证码
 */
function getEmailCode() {
  if (count.value !== 0) {
    return;
  }
  const param = {
    email: loginForm.value.email
  }
  getEmailCodeAPI(param).then(res => {
    if (res.code !== 200) {
      return ;
    }
    loginForm.value.validateId = res.data.uuid
    count.value = 60;
    clearInterval(timer.value);
    timer.value = setInterval(() => {
      if (count.value > 0) {
        count.value -= 1;
      } else {
        clearInterval(timer.value);
      }
    }, 1000);
  })
}
/**
 * 获取短信验证码
 */
function getSmsCode() {
  if (count.value !== 0) {
    return;
  }
  const param = {
    phone: loginForm.value.phone
  }
  getSmsCodeAPI(param).then(res => {
    if (res.code !== 200) {
      return ;
    }
    loginForm.value.validateId = res.data.uuid
    count.value = 60;
    clearInterval(timer.value);
    timer.value = setInterval(() => {
      if (count.value > 0) {
        count.value -= 1;
      } else {
        clearInterval(timer.value);
      }
    }, 1000);
  })
}
/**
 * 切换登录类型
 */
function switchLoginType(type) {
  loginForm.value.loginType = type;
  fieldErrors.value = {};
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
  if (!agreed.value) {
    uni.showToast({title: '请先阅读并同意用户服务协议和隐私政策', icon: 'none'});
    return;
  }
  if (!validateFields()) {
    return;
  }
  loading.value = true;
  // #ifdef MP-WEIXIN
  const loginPlatform = 6;
  // #endif
  // #ifndef MP-WEIXIN
  const loginPlatform = 2;
  // #endif
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
    data.loginPlatform = loginPlatform;
  } else if (loginForm.value.loginType === 2) {
    data.phone = loginForm.value.phone;
    data.validateId = loginForm.value.validateId;
    data.validateValue = loginForm.value.validateValue;
    data.loginType = loginForm.value.loginType;
    data.loginPlatform = loginPlatform;
  } else if (loginForm.value.loginType === 3) {
    data.email = loginForm.value.email;
    data.validateId = loginForm.value.validateId;
    data.validateValue = loginForm.value.validateValue;
    data.loginType = loginForm.value.loginType;
    data.loginPlatform = loginPlatform;
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
// 登录页验证码按钮私有色，全站无同值 token，不提升全局
// stylelint-disable color-no-hex
$login-light-sms-color: #1b5fe0;
$login-light-wait-bg: #eef2f8;
$login-dark-sms-color: #9dc0ff;
$login-dark-wait-bg: #2b3a52;
// stylelint-enable color-no-hex

.login-box {
  position: relative;
  box-sizing: border-box;
  padding: 0 44rpx;
  width: 100%;
  height: 100vh;
  overflow-y: auto;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  background-image: $app-gradient-login;
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

.bg-glow {
  position: absolute;
  left: 50%;
  top: -400rpx;
  transform: translateX(-50%);
  width: 1240rpx;
  height: 920rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(47, 123, 255, 0.14) 0%, rgba(47, 123, 255, 0) 66%);
}

.bg-ring {
  position: absolute;
  border-radius: 50%;
  border: 2rpx solid rgba(47, 123, 255, 0.08);
}

.bg-ring-1 {
  width: 920rpx;
  height: 920rpx;
  right: -380rpx;
  top: 120rpx;
}

.bg-ring-2 {
  width: 600rpx;
  height: 600rpx;
  right: -220rpx;
  top: 300rpx;
  border-color: rgba(47, 123, 255, 0.11);
}

.login-brand {
  position: relative;
  z-index: 1;
  padding-top: 250rpx;
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.brand-logo {
  flex: none;
  width: 96rpx;
  height: 96rpx;
  border-radius: 30rpx;
  box-shadow: $app-shadow-logo;
}

.brand-logo__img {
  width: 96rpx;
  height: 96rpx;
}

.brand-name {
  display: flex;
  flex-direction: column;
  gap: 2rpx;
}

.brand-name__cn {
  font-size: 31rpx;
  font-weight: 700;
  letter-spacing: 0.4rpx;
  color: $app-text-primary;
}

.brand-name__en {
  font-size: 19rpx;
  font-weight: 600;
  letter-spacing: 3.2rpx;
  color: $app-text-secondary;
  opacity: 0.62;
}

.login-title {
  display: block;
  margin-top: 60rpx;
  font-size: 54rpx;
  font-weight: 800;
  letter-spacing: -0.8rpx;
  color: $app-text-primary;
}

.login-tip {
  display: block;
  margin-top: 18rpx;
  font-size: 25rpx;
  line-height: 1.6;
  color: $app-text-tertiary;
  letter-spacing: 0.4rpx;
}

.login-form {
  position: relative;
  z-index: 1;
  margin-top: 56rpx;
}

.field {
  display: flex;
  align-items: center;
  gap: 18rpx;
  height: 100rpx;
  padding: 0 26rpx;
  border-radius: 26rpx;
  border: 3rpx solid $app-field-border;
  background-color: $app-field-bg;
  box-shadow: 0 12rpx 32rpx -20rpx rgba(23, 43, 77, 0.2);
  margin-bottom: 26rpx;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.field.is-focus {
  border-color: $app-brand;
}

.field.is-error {
  border-color: $app-text-error;
}

.field-icon {
  flex: none;
  display: flex;
}

.field :deep(.u-input) {
  flex: 1;
  min-width: 0;
}

.field-err {
  margin: -14rpx 10rpx 20rpx;
  font-size: 23rpx;
  color: $app-text-error;
}

.code-input-wrap {
  display: flex;
  align-items: center;
  width: 100%;
  min-width: 0;
}

.code-input-flex {
  flex: 1;
  min-width: 0;
}

.validate-img {
  width: 96px;
  height: 40px;
  margin-left: 8px;
  border-radius: 18rpx;
  flex-shrink: 0;
}

.validate-img-h5 {
  width: 96px;
  height: 40px;
  border-radius: 18rpx;
  flex-shrink: 0;
}

.sms-btn {
  flex: none;
  height: 68rpx;
  padding: 0 24rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  font-size: 24rpx;
  font-weight: 600;
  color: $login-light-sms-color;
  background-color: rgba(47, 123, 255, 0.1);

  &.is-wait {
    color: $app-text-tertiary;
    background-color: $login-light-wait-bg;
  }
}

.login-btn {
  margin-top: 40rpx;
}

/* #ifdef H5 */
.login-form :deep(.uni-input-input) {
  &:-webkit-autofill,
  &:-webkit-autofill:hover,
  &:-webkit-autofill:focus,
  &:-webkit-autofill:active {
    -webkit-box-shadow: 0 0 0 1000px $app-autofill-bg inset !important;
    box-shadow: 0 0 0 1000px $app-autofill-bg inset !important;
  }
}
/* #endif */

.login-other {
  position: relative;
  z-index: 1;
  margin-top: 52rpx;
}

.login-type-icons {
  display: flex;
  justify-content: center;
  margin-top: 24rpx;
}

.login-type-icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 20rpx;
}

.icon-circle {
  width: 84rpx;
  height: 84rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: $app-bg-card;
  border: 1rpx solid $app-border;
  box-shadow: $app-shadow-card;
}

.login-secure {
  margin-top: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
}

.secure-text {
  font-size: 22rpx;
  letter-spacing: 0.6rpx;
  color: $app-text-tertiary;
}

.login-foot {
  position: relative;
  z-index: 1;
  margin-top: auto;
  padding-top: 40rpx;
  padding-bottom: calc(40rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
}

.login-agreement {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;

  .agree-icon {
    margin-right: 6rpx;
  }

  .agreement-text {
    font-size: 22rpx;
    color: $app-text-tertiary;
  }

  .agreement-link {
    font-size: 22rpx;
    color: $app-brand;
    font-weight: 600;
  }
}

.login-copyright {
  margin-top: 12rpx;
  text-align: center;
}

.copyright-text {
  font-size: 20rpx;
  color: $app-text-muted;
}

.login-theme-btn {
  position: absolute;
  right: 36rpx;
  z-index: 3;
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20rpx;
  background-color: rgba(255, 255, 255, 0.72);
  border: 1rpx solid $app-border-light;
  box-shadow: $app-shadow-card;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.login-theme-btn:active {
  transform: scale(0.94);
}

.login-box.theme-dark {
  .login-theme-btn {
    background-color: rgba(255, 255, 255, 0.07);
    border-color: rgba(255, 255, 255, 0.1);
    box-shadow: $app-shadow-card;
  }

  .bg-glow {
    background: radial-gradient(circle, rgba(77, 141, 255, 0.3) 0%, rgba(77, 141, 255, 0) 66%);
  }

  .bg-ring {
    border-color: rgba(140, 180, 255, 0.12);
  }

  .bg-ring-2 {
    border-color: rgba(140, 180, 255, 0.16);
  }

  .field {
    box-shadow: 0 12rpx 32rpx -20rpx rgba(0, 0, 0, 0.6);
  }

  .field.is-focus {
    border-color: $app-brand;
  }

  .field.is-error {
    border-color: $app-text-error;
  }

  .icon-circle {
    box-shadow: $app-shadow-card;
  }

  .sms-btn {
    color: $login-dark-sms-color;
    background-color: rgba(77, 141, 255, 0.16);

    &.is-wait {
      color: $app-text-placeholder;
      background-color: $login-dark-wait-bg;
    }
  }

  .validate-img,
  .validate-img-h5 {
    opacity: 0.86;
  }
}
</style>

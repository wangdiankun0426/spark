<template>
  <div class="auth-page">
    <!-- 左侧：品牌与能力介绍（组件化，登录/注册共用） -->
    <AuthBrandPanel/>

    <!-- 右侧：白色登录区（组件化外壳，表单内容由页面提供） -->
    <AuthPanel>
      <h2 class="auth-title">欢迎回来</h2>
      <p class="auth-subtitle">登录之后开启您的AI辅助工作之旅吧</p>

      <el-form ref="loginRef" :model="loginForm" :rules="loginFormRules" class="auth-form">
        <div class="auth-fields">
          <template v-if="loginForm.loginType === 1">
            <el-form-item prop="username">
              <el-input
                  v-model="loginForm.username"
                  type="text"
                  placeholder="账号"
                  @keyup.enter="submitLoginForm"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="密码"
                  show-password
                  @keyup.enter="submitLoginForm"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="validateValue">
              <el-input
                  v-model="loginForm.validateValue"
                  auto-complete="off"
                  placeholder="验证码"
                  style="width: 65%"
                  @keyup.enter="submitLoginForm"
              >
                <template #prefix>
                  <el-icon><Validate /></el-icon>
                </template>
              </el-input>
              <div class="auth-code">
                <img :src="validateImg" class="auth-code-img" @click="getValidateImg" title="点击刷新验证码" alt=""/>
              </div>
            </el-form-item>
          </template>

          <template v-if="loginForm.loginType === 2">
            <el-form-item prop="phone">
              <el-input
                  v-model="loginForm.phone"
                  type="text"
                  placeholder="手机号"
                  @keyup.enter="submitLoginForm"
              >
                <template #prefix>
                  <el-icon><Iphone /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="validateValue">
              <el-input
                  v-model="loginForm.validateValue"
                  auto-complete="off"
                  placeholder="验证码"
                  style="width: 65%"
                  @keyup.enter="submitLoginForm"
              >
                <template #prefix>
                  <el-icon><Validate /></el-icon>
                </template>
              </el-input>
              <div class="auth-code">
                <el-button type="primary" @click="getSmsCode" :disabled="count !== 0" class="code-send-btn">
                  <span v-if="count === 0">获取验证码</span>
                  <span v-else>等待({{ count }}秒)</span>
                </el-button>
              </div>
            </el-form-item>
          </template>

          <template v-if="loginForm.loginType === 3">
            <el-form-item prop="email">
              <el-input
                  v-model="loginForm.email"
                  type="text"
                  placeholder="邮箱"
                  @keyup.enter="submitLoginForm"
              >
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="validateValue">
              <el-input
                  v-model="loginForm.validateValue"
                  auto-complete="off"
                  placeholder="验证码"
                  style="width: 65%"
                  @keyup.enter="submitLoginForm"
              >
                <template #prefix>
                  <el-icon><Validate /></el-icon>
                </template>
              </el-input>
              <div class="auth-code">
                <el-button type="primary" @click="getEmailCode" :disabled="count !== 0" class="code-send-btn">
                  <span v-if="count === 0">获取验证码</span>
                  <span v-else>等待({{ count }}秒)</span>
                </el-button>
              </div>
            </el-form-item>
          </template>
        </div>

        <el-form-item class="auth-submit-item">
          <el-button
              :loading="loading"
              type="primary"
              class="auth-submit-btn"
              @click="submitLoginForm"
          >登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-agreement">
        <el-checkbox v-model="agreed" @change="onAgreeChange">我已阅读并同意</el-checkbox>
        <el-button type="primary" link @click="openAgreement('service')">用户服务协议</el-button>
        <span class="agreement-and">和</span>
        <el-button type="primary" link @click="openAgreement('privacy')">隐私政策</el-button>
      </div>
      <div v-if="agreementError" class="auth-agreement-error">请先阅读并同意用户服务协议和隐私政策</div>

      <div class="auth-entry">
        <span class="auth-entry-tip">还没有账号？</span>
        <el-button type="primary" link @click="goRegister">立即注册</el-button>
      </div>

      <el-divider class="auth-divider">
        <span>其他方式登录</span>
      </el-divider>

      <div class="auth-type-row">
        <el-tooltip content="密码登录" placement="top" v-if="loginForm.loginType !== 1">
          <el-button circle class="auth-type-btn" @click="switchLoginType(1)">
            <el-icon><Lock /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="手机号登录" placement="top" v-if="loginForm.loginType !== 2">
          <el-button circle class="auth-type-btn" @click="switchLoginType(2)">
            <el-icon><Iphone /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="邮箱登录" placement="top" v-if="loginForm.loginType !== 3">
          <el-button circle class="auth-type-btn" @click="switchLoginType(3)">
            <el-icon><Message /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </AuthPanel>
  </div>
</template>

<script setup>
import {getValidateCodeAPI, getSmsCodeAPI, getEmailCodeAPI, getSessionAPI, getEncryptKeyAPI} from '@/api/manage/auth/login.js';
import { des } from '@/utils/encryptUtil.js';
import { AGREEMENT_VERSION } from '@/utils/agreementDoc.js';
import { localStorageKey } from '@/utils/keyUtils.js';
import Validate from '@/assets/icons/validate.vue';
import { ref, getCurrentInstance } from "vue";
import { useRouter } from 'vue-router';
import { useStore } from "vuex";
import { isDesktop } from '@desktop/env.js';
import AuthBrandPanel from './components/AuthBrandPanel.vue';
import AuthPanel from './components/AuthPanel.vue';

const router = useRouter();
const { proxy } = getCurrentInstance();

const loginForm = ref({
  username: undefined,
  password: undefined,
  phone: undefined,
  email: undefined,
  validateId: undefined,
  validateValue: undefined,
  loginType: 1
});

const loginFormRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  validateValue: [
    { required: true, trigger: "blur", message: "请输入验证码" },
    { min: 4, max: 4, trigger: "blur", message: "验证码为4位字符" }
  ],
};
const loading = ref(false);
const validateImg = ref(undefined);
const count = ref(0);
const timer = ref(null);

/**
 * 读取本地协议同意凭证，版本不一致视为未同意
 */
function getStoredAgreed() {
  const record = JSON.parse(localStorage.getItem(localStorageKey.AGREEMENT_KEY) || 'null');
  return !!(record && record.version === AGREEMENT_VERSION);
}

const agreed = ref(getStoredAgreed());

// 未勾选协议提交时，在勾选区内联红字提示，不弹 toast
const agreementError = ref(false);

/**
 * 勾选时持久化同意凭证，取消时移除
 */
function onAgreeChange(value) {
  if (value) {
    localStorage.setItem(localStorageKey.AGREEMENT_KEY, JSON.stringify({version: AGREEMENT_VERSION, time: Date.now()}));
    agreementError.value = false;
  } else {
    localStorage.removeItem(localStorageKey.AGREEMENT_KEY);
  }
}

/**
 * 新标签打开协议文本页
 * @param type service 用户服务协议 / privacy 隐私政策
 */
function openAgreement(type) {
  const { href } = router.resolve({ path: '/agreement', query: { type } });
  window.open(href, '_blank');
}

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
      return;
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
function getSmsCode() {
  const param = {
    phone: loginForm.value.phone
  }
  getSmsCodeAPI(param).then(res => {
    if (res.code !== 200) {
      return;
    }
    loginForm.value.validateId = res.data.uuid
    count.value = 60;
    // 清除之前的计时器
    clearInterval(timer.value);
    timer.value = setInterval(() => {
      if (count.value > 0) {
        count.value -= 1;
      } else {
        // 计时结束，清除计时器
        clearInterval(timer.value);
      }
    }, 1000);
  })
}

/**
 * 切换登录类型
 * @param type
 */
function switchLoginType(type) {
  loginForm.value.loginType = type;
}

/**
 * 获取验证码
 */
function getValidateImg() {
  getValidateCodeAPI().then(res => {
    validateImg.value = "data:image/gif;base64," + res.data.img;
    loginForm.value.validateId = res.data.uuid;
  });
}

const store = useStore();

/**
 * 提交登录表单
 */
function submitLoginForm() {
  proxy.$refs.loginRef.validate(async valid => {
    if (valid) {
      if (!agreed.value) {
        agreementError.value = true;
        return;
      }
      loading.value = true;
      const data = {};
      if (loginForm.value.loginType === 1) {
        // 申请一次性加密密钥
        const keyResult = await getEncryptKeyAPI();
        if (keyResult.code !== 200) {
          loading.value = false;
          return;
        }
        data.loginName = loginForm.value.username;
        data.password = des(loginForm.value.password, keyResult.data.key);
        data.keyId = keyResult.data.keyId;
        data.validateId = loginForm.value.validateId;
        data.validateValue = loginForm.value.validateValue;
        data.loginType = loginForm.value.loginType;
        data.loginPlatform = isDesktop ? 3 : 1;
      } else if (loginForm.value.loginType === 2) {
        data.phone = loginForm.value.phone;
        data.validateId = loginForm.value.validateId;
        data.validateValue = loginForm.value.validateValue;
        data.loginType = loginForm.value.loginType;
        data.loginPlatform = isDesktop ? 3 : 1;
      } else if (loginForm.value.loginType === 3) {
        data.email = loginForm.value.email;
        data.validateId = loginForm.value.validateId;
        data.validateValue = loginForm.value.validateValue;
        data.loginType = loginForm.value.loginType;
        data.loginPlatform = isDesktop ? 3 : 1;
      }
      store.dispatch('user/login', {loginForm: data}).then(res => {
        if (res.code === 200) {
          getSessionAPI().then(res => {
            store.dispatch('user/setUserInfo', { userInfo: res.data })
            router.push({ path: '/' })
          })
        } else {
          if (res.code === 604) {
            getValidateImg();
          }
          loading.value = false;
        }
      }).finally(() => {
        loading.value = false;
      })
    }
  });
}

/**
 * 跳转注册页
 */
function goRegister() {
  router.push('/register');
}
</script>

<style lang="scss" scoped>
// 左侧品牌区与右侧面板由组件提供，页面只负责左右分栏
.auth-page {
  height: 100vh;
  display: flex;
  align-items: stretch;
  overflow: hidden;
  background-color: $app-bg-card;
}

@supports (height: 100dvh) {
  .auth-page {
    height: 100dvh;
  }
}
</style>

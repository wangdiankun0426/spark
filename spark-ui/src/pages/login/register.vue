<template>
  <div class="auth-page">
    <!-- 左侧：品牌与能力介绍（与登录页共用同一组件） -->
    <AuthBrandPanel/>

    <!-- 右侧：白色注册区 -->
    <AuthPanel show-back @back="goLogin">
      <h2 class="auth-title">创建账号</h2>
      <p class="auth-subtitle">注册加入星火云AI，开启智能工作之旅</p>

      <el-form
          ref="registerRef"
          :model="registerForm"
          :rules="registerFormRules"
          class="auth-form"
      >
        <div class="auth-fields">
          <el-form-item prop="loginName">
            <el-input
                v-model="registerForm.loginName"
                type="text"
                placeholder="登录名"
                @keyup.enter="submitRegisterForm"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码"
                show-password
                @keyup.enter="submitRegisterForm"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                show-password
                @keyup.enter="submitRegisterForm"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="sex">
            <div class="gender-field">
              <el-icon class="gender-icon"><User /></el-icon>
              <div class="gender-seg">
                <span :class="{ on: registerForm.sex === 1 }" @click="registerForm.sex = 1">男</span>
                <span :class="{ on: registerForm.sex === 2 }" @click="registerForm.sex = 2">女</span>
              </div>
            </div>
          </el-form-item>

          <el-form-item prop="validateValue">
            <el-input
                v-model="registerForm.validateValue"
                auto-complete="off"
                placeholder="验证码"
                class="auth-code-input"
                @keyup.enter="submitRegisterForm"
            >
              <template #prefix>
                <el-icon><Validate /></el-icon>
              </template>
            </el-input>
            <div class="auth-code">
              <img :src="validateImg" class="auth-code-img" @click="getValidateImg" title="点击刷新验证码" alt=""/>
            </div>
          </el-form-item>
        </div>

        <div class="auth-agreement">
          <el-checkbox v-model="agreed" @change="onAgreeChange">我已阅读并同意</el-checkbox>
          <el-button type="primary" link @click="openAgreement('service')">用户服务协议</el-button>
          <span class="agreement-and">和</span>
          <el-button type="primary" link @click="openAgreement('privacy')">隐私政策</el-button>
        </div>
        <div v-if="agreementError" class="auth-agreement-error">请先阅读并同意用户服务协议和隐私政策</div>

        <el-form-item class="auth-submit-item">
          <el-button
              :loading="loading"
              type="primary"
              class="auth-submit-btn"
              @click="submitRegisterForm"
          >注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-entry">
        <span class="auth-entry-tip">已有账号？</span>
        <el-button type="primary" link @click="goLogin">立即登录</el-button>
      </div>
    </AuthPanel>
  </div>
</template>

<script setup>
import { getValidateCodeAPI, getEncryptKeyAPI } from '@/api/manage/auth/login.js';
import { registerAPI } from '@/api/manage/auth/register.js';
import { des } from '@/utils/encryptUtil.js';
import { AGREEMENT_VERSION } from '@/utils/agreementDoc.js';
import { localStorageKey } from '@/utils/keyUtils.js';
import Validate from '@/assets/icons/validate.vue';
import { ref, getCurrentInstance } from "vue";
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import AuthBrandPanel from './components/AuthBrandPanel.vue';
import AuthPanel from './components/AuthPanel.vue';

const router = useRouter();
const { proxy } = getCurrentInstance();

const registerForm = ref({
  loginName: undefined,
  password: undefined,
  confirmPassword: undefined,
  sex: 1,
  validateId: undefined,
  validateValue: undefined
});

const validatePassword = (rule, value, callback) => {
  if (value === undefined || value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== registerForm.value.password) {
    callback(new Error('两次输入密码不一致'));
  } else {
    callback();
  }
};

// 注册无租户会话，密码长度规则与 TenantConfigEnum 默认值保持一致
const registerFormRules = {
  loginName: [
    { required: true, trigger: "blur", message: "请输入登录名" },
    { min: 2, max: 20, trigger: "blur", message: "登录名长度为2-20个字符" }
  ],
  password: [
    { required: true, trigger: "blur", message: "请输入密码" },
    { min: 6, max: 14, trigger: "blur", message: "密码长度为6-14个字符" }
  ],
  confirmPassword: [
    { required: true, trigger: "blur", validator: validatePassword }
  ],
  validateValue: [{ required: true, trigger: "blur", message: "请输入验证码" }]
};

const loading = ref(false);
const validateImg = ref(undefined);

/**
 * 读取本地协议同意凭证，与登录页共用同一份记录
 */
function getStoredAgreed() {
  const record = JSON.parse(localStorage.getItem(localStorageKey.AGREEMENT_KEY) || 'null');
  return !!(record && record.version === AGREEMENT_VERSION);
}

const agreed = ref(getStoredAgreed());
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

/**
 * 获取验证码
 */
function getValidateImg() {
  getValidateCodeAPI().then(res => {
    registerForm.value.validateId = res.data.uuid;
    validateImg.value = 'data:image/png;base64,' + res.data.img;
  });
}

getValidateImg();

/**
 * 提交注册表单
 */
function submitRegisterForm() {
  proxy.$refs.registerRef.validate(async valid => {
    if (!valid) return;
    if (!agreed.value) {
      agreementError.value = true;
      return;
    }
    loading.value = true;
    const keyResult = await getEncryptKeyAPI();
    if (keyResult.code !== 200) {
      loading.value = false;
      return;
    }
    const data = {
      loginName: registerForm.value.loginName,
      password: des(registerForm.value.password, keyResult.data.key),
      keyId: keyResult.data.keyId,
      sex: registerForm.value.sex,
      validateId: registerForm.value.validateId,
      validateValue: registerForm.value.validateValue
    };
    registerAPI(data).then(res => {
      loading.value = false;
      if (res.code !== 200) {
        getValidateImg();
        return;
      }
      ElMessage.success('注册成功，请登录');
      router.push('/login');
    }).catch(() => {
      loading.value = false;
      getValidateImg();
    });
  });
}

/**
 * 跳转登录页
 */
function goLogin() {
  router.push('/login');
}
</script>

<style lang="scss" scoped>
// 左右分栏由页面负责，两侧内容区来自公共组件
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

// 与输入框同高的性别分段双选
.gender-field {
  width: 100%;
  height: 47px;
  padding: 0 $spacing-sm 0 14px;
  display: flex;
  align-items: center;
  border-radius: $border-radius-md;
  background: $ds-surface-2;
}

.gender-icon {
  flex: none;
  margin-right: 9px;
  font-size: 17px;
  color: $app-text-placeholder;
}

.gender-seg {
  flex: 1;
  min-width: 0;
  display: flex;
  gap: $spacing-sm;

  span {
    flex: 1;
    height: 33px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    border: 1px solid $ds-line;
    background-color: $app-bg-card;
    font-size: 12.5px;
    color: $app-text-secondary;
    cursor: pointer;
    user-select: none;
    transition: color $transition-fast, border-color $transition-fast, background-color $transition-fast;

    &:hover {
      border-color: $ds-brand;
    }

    &.on {
      color: $ds-brand;
      font-weight: 650;
      border-color: $ds-brand;
      background-color: $app-primary-light;
    }
  }
}

html.dark .gender-seg {
  span {
    background: $app-bg-sub;

    &.on {
      color: $ds-brand;
      background: $app-primary-light;
    }
  }
}

// 验证码输入框与右侧验证码图并排，固定占比
.auth-code-input {
  width: 65%;
}

// 矮屏下与输入框一起压缩
@media (max-height: 860px) {
  .gender-field {
    height: 44px;
  }

  .gender-seg span {
    height: 30px;
  }
}
</style>

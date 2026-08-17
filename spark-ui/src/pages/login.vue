<template>
  <div class="login">
    <!-- 左侧 -->
    <div class="el-login-sidebar">
      <!-- 蓝色动态背景 -->
      <div class="anim-bg">
        <div class="anim-blob anim-blob-1"></div>
        <div class="anim-blob anim-blob-2"></div>
        <div class="anim-blob anim-blob-3"></div>
        <div class="anim-ring anim-ring-1"></div>
        <div class="anim-ring anim-ring-2"></div>
        <div class="anim-ring anim-ring-3"></div>
        <div class="anim-dot anim-dot-1"></div>
        <div class="anim-dot anim-dot-2"></div>
        <div class="anim-dot anim-dot-3"></div>
      </div>
      <div class="sidebar-content">
        <h1 style="font-size: 32px;">星火云AI应用平台</h1>
        <p style="font-size: 16px;">欢迎使用 — 星火云AI应用平台</p>
        <p style="font-size: 14px;">
          星火云AI应用平台，我们的目标不仅是一个企业AI协作工具系统，更是以帮助企业实现高效运营、创新驱动和可持续发展的核心支撑为长远目标。
        </p>
        <div class="sidebar-features">
          <div class="feature-item">
            <div class="feature-icon"><el-icon><ChatDotRound /></el-icon></div>
            <div class="feature-text">
              <div class="feature-name">即时通信</div>
              <div class="feature-desc">企业级即时消息与协作</div>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon"><el-icon><Connection /></el-icon></div>
            <div class="feature-text">
              <div class="feature-name">自定义流程</div>
              <div class="feature-desc">可视化编排业务流程</div>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon"><el-icon><EditPen /></el-icon></div>
            <div class="feature-text">
              <div class="feature-name">自定义表单</div>
              <div class="feature-desc">拖拽式表单设计器</div>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon"><el-icon><MagicStick /></el-icon></div>
            <div class="feature-text">
              <div class="feature-name">AI应用</div>
              <div class="feature-desc">智能对话与工作流编排</div>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon"><el-icon><SetUp /></el-icon></div>
            <div class="feature-text">
              <div class="feature-name">AI工作流</div>
              <div class="feature-desc">可视化构建智能工作流</div>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon"><el-icon><User /></el-icon></div>
            <div class="feature-text">
              <div class="feature-name">Agent</div>
              <div class="feature-desc">专属智能体助手</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧 -->
    <div class="el-login-main">
      <div>
        <h2 style="color: #304156; font-size: 26px;">
          <strong>{{ loginForm.loginType === 1 ? '密码登录' : (loginForm.loginType === 2 ? '手机号登录' : '邮箱登录') }}</strong>
        </h2>
      </div>
      <div>
        <el-form ref="loginRef" :model="loginForm" :rules="loginFormRules">
          <div style="height: 210px; margin-top: 20px">
            <div v-if="loginForm.loginType === 1">
            <el-form-item prop="username">
              <el-input
                  v-model="loginForm.username"
                  type="text"
                  placeholder="账号"
                  @keyup.enter="submitLoginForm"
              >
                <template #prefix>
                  <el-icon style="font-size: 20px !important;"><Avatar /></el-icon>
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
                  <el-icon style="font-size: 20px !important;"><Lock /></el-icon>
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
                  <el-icon style="font-size: 20px !important;"><Validate /></el-icon>
                </template>
              </el-input>
              <div class="login-code">
                <img :src="validateImg" class="login-code-img" @click="getValidateImg" title="点击刷新二维码" alt=""/>
              </div>
            </el-form-item>
          </div>

          <div v-if="loginForm.loginType === 2">
            <el-form-item prop="phone">
              <el-input
                  v-model="loginForm.phone"
                  type="text"
                  placeholder="手机号"
                  @keyup.enter="submitLoginForm"
              >
                <template #prefix>
                  <el-icon style="font-size: 20px !important;"><Iphone /></el-icon>
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
                  <el-icon style="font-size: 20px !important;"><Validate /></el-icon>
                </template>
              </el-input>
              <div class="login-code">
                <el-button type="primary" @click="getMessageCode" :disabled="count !== 0" style="width: 100%; height: 44px; margin: 0; border-radius: 4px; color: white;">
                  <span v-if="count === 0">获取验证码</span>
                  <span v-else>等待({{count}}秒)</span>
                </el-button>
              </div>
            </el-form-item>
          </div>

          <div v-if="loginForm.loginType === 3">
            <el-form-item prop="email">
              <el-input
                  v-model="loginForm.email"
                  type="text"
                  placeholder="邮箱"
                  @keyup.enter="submitLoginForm"
              >
                <template #prefix>
                  <el-icon style="font-size: 20px !important;"><Message /></el-icon>
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
                  <el-icon style="font-size: 20px !important;"><Validate /></el-icon>
                </template>
              </el-input>
              <div class="login-code">
                <el-button type="primary" @click="getEmailCode" :disabled="count !== 0" style="width: 100%; height: 44px; margin: 0; border-radius: 4px; color: white;">
                  <span v-if="count === 0">获取验证码</span>
                  <span v-else>等待({{count}}秒)</span>
                </el-button>
              </div>
            </el-form-item>
          </div>
          </div>
          <el-form-item style="width: 100%;">
            <el-button
                :loading="loading"
                size="large"
                type="primary"
                style="width: 100%; background-color: #0052cc;height: 40px; font-weight: bold; font-size: 18px"
                @click="submitLoginForm"
            >登 录
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-divider>
        <span style="font-size: 12px">其他方式登录</span>
      </el-divider>

      <div style="margin: 20px">
        <el-tooltip content="密码登录" placement="bottom" v-if="loginForm.loginType !== 1">
          <el-button circle @click="switchLoginType(1)">
            <el-icon style="font-size: 18px"><Lock /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="手机号登录" placement="bottom"   v-if="loginForm.loginType !== 2">
          <el-button circle @click="switchLoginType(2)">
            <el-icon style="font-size: 18px"><Iphone /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="邮箱登录" placement="bottom"   v-if="loginForm.loginType !== 3">
          <el-button circle @click="switchLoginType(3)">
            <el-icon style="font-size: 18px"><Message /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getValidateCodeAPI, getMessageCodeAPI, getEmailCodeAPI } from '@/api/auth/login.js';
import { des } from '@/utils/encryptUtil';
import Validate from '@/assets/icons/validate';
import { ref, getCurrentInstance } from "vue";
import { useRouter } from 'vue-router';
import { useStore } from "vuex";

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
  validateValue: [{ required: true, trigger: "blur", message: "请输入验证码" }],
};
const loading = ref(false);
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
function getMessageCode() {
  const param = {
    phone: loginForm.value.phone
  }
  getMessageCodeAPI(param).then(res => {
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
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true;
      const data = {};
      if (loginForm.value.loginType === 1) {
        data.loginName = loginForm.value.username;
        data.password = des(loginForm.value.password);
        data.validateId = loginForm.value.validateId;
        data.validateValue = loginForm.value.validateValue;
        data.loginType = loginForm.value.loginType;
        data.loginPlatform = 1;
      } else if (loginForm.value.loginType === 2) {
        data.phone = loginForm.value.phone;
        data.validateId = loginForm.value.validateId;
        data.validateValue = loginForm.value.validateValue;
        data.loginType = loginForm.value.loginType;
        data.loginPlatform = 1;
      } else if (loginForm.value.loginType === 3) {
        data.email = loginForm.value.email;
        data.validateId = loginForm.value.validateId;
        data.validateValue = loginForm.value.validateValue;
        data.loginType = loginForm.value.loginType;
        data.loginPlatform = 1;
      }
      store.dispatch('user/login', {loginForm: data}).then(res => {
        if (res.code === 200) {
          router.push({ path: "/" }).catch(() => { });
        } else {
          if (res.code === 604) {
            getValidateImg();
          }
          loading.value = false;
        }
      })
    }
  });
}
</script>

<style lang="scss" scoped>
.login {
  height: 100vh;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
  margin: 0;
  background-color: $bg-page;
}

// 左侧 - 蓝色动态背景
.el-login-sidebar {
  width: 60%;
  height: 100vh;
  background: linear-gradient(160deg, #081c45 0%, #0c2f6b 50%, #123f87 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
  color: #ffffff;
}

// 蓝色动态背景装饰层
.anim-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

// 极光渐变光斑
.anim-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.4;
  animation: blobFloat 18s ease-in-out infinite alternate;
}

.anim-blob-1 {
  width: 780px;
  height: 780px;
  left: -200px;
  top: -200px;
  background: radial-gradient(circle at 30% 30%, #3b82f6 0%, #6ea8ff 35%, transparent 70%);
}

.anim-blob-2 {
  width: 700px;
  height: 700px;
  right: -180px;
  bottom: -160px;
  background: radial-gradient(circle at 60% 40%, #1e6fd9 0%, #4f9bff 35%, transparent 70%);
  animation-delay: -6s;
}

.anim-blob-3 {
  width: 580px;
  height: 580px;
  left: 32%;
  top: 28%;
  background: radial-gradient(circle at 50% 50%, #2a7bff 0%, transparent 65%);
  opacity: 0.32;
  animation-delay: -12s;
}

@keyframes blobFloat {
  0% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(40px, -30px) scale(1.08);
  }
  66% {
    transform: translate(-30px, 25px) scale(0.95);
  }
  100% {
    transform: translate(20px, -15px) scale(1.04);
  }
}

// 简约几何圆环
.anim-ring {
  position: absolute;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 50%;
  animation: ringFloat 26s ease-in-out infinite alternate;
}

.anim-ring-1 {
  width: 220px;
  height: 220px;
  right: 18%;
  top: 16%;
}

.anim-ring-2 {
  width: 120px;
  height: 120px;
  left: 12%;
  bottom: 22%;
  border-color: rgba(255, 255, 255, 0.14);
  animation-delay: -8s;
}

.anim-ring-3 {
  width: 70px;
  height: 70px;
  right: 24%;
  bottom: 34%;
  border-color: rgba(255, 255, 255, 0.12);
  animation-delay: -14s;
}

@keyframes ringFloat {
  0% {
    transform: translateY(0) rotate(0deg);
  }
  100% {
    transform: translateY(-24px) rotate(20deg);
  }
}

// 微光粒子
.anim-dot {
  position: absolute;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.75);
  box-shadow: 0 0 12px rgba(140, 180, 255, 0.9);
  animation: dotFloat 8s ease-in-out infinite alternate;
}

.anim-dot-1 { left: 20%; top: 42%; }
.anim-dot-2 { left: 70%; top: 28%; animation-delay: -3s; }
.anim-dot-3 { left: 46%; top: 76%; animation-delay: -6s; }

@keyframes dotFloat {
  0% {
    transform: translateY(0);
    opacity: 0.55;
  }
  100% {
    transform: translateY(-28px);
    opacity: 1;
  }
}

// 简约网格
.sidebar-content {
  margin: 70px 80px 20px 80px;
  text-align: left;
  position: relative;
  z-index: 1;
}

// 核心功能卡片
.sidebar-features {
  margin-top: 32px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 20px;
  max-width: 560px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.22);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
    transform: translateY(-2px);
  }
}

.feature-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  background: linear-gradient(135deg, #0052cc, #00b8d9);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.feature-text {
  min-width: 0;
}

.feature-name {
  font-size: 15px;
  font-weight: 600;
  color: #ffffff;
}

.feature-desc {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
  margin-top: 2px;
  white-space: nowrap;
}

// 右侧 - 登录表单区（宽度居中）
.el-login-main {
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 400px;
  max-width: 90%;
  margin: auto;
  background: transparent;
}

.el-login-main h2 {
  text-align: center;
}

.el-input {
  height: 44px;
}

.login-code {
  margin-left: 5px;
  width: 32%;
  height: 44px;
  float: right;

  img {
    width: 100%;
    height: 100%;
    cursor: pointer;
    vertical-align: middle;
    border-radius: $border-radius-sm;
    border: 1px solid $border-color;
  }
}

::v-deep(.el-divider__text) {
  background-color: $bg-page !important;
  color: $color-text-placeholder;
}
</style>


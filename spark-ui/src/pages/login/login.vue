<template>
  <div class="login">
    <!-- 浅色科技风动态背景（全屏铺满） -->
    <div class="anim-bg">
      <div class="anim-grid"></div>
      <div class="anim-blob anim-blob-1"></div>
      <div class="anim-blob anim-blob-2"></div>
      <div class="anim-orbit anim-orbit-1"></div>
      <div class="anim-orbit anim-orbit-2"></div>
      <svg class="anim-net" viewBox="0 0 1600 900" preserveAspectRatio="xMidYMid slice">
        <polyline class="net-line" points="80,620 220,480 380,560 520,360 680,460 860,280"/>
        <polyline class="net-line net-line--2" points="120,220 260,330 420,180 560,320 720,150 880,240"/>
        <polyline class="net-line net-line--3" points="180,720 340,650 480,720 640,580 800,660"/>
        <polyline class="net-line net-line--4" points="960,520 1120,420 1260,500 1400,340 1520,420"/>
        <circle class="net-node" cx="80" cy="620" r="4"/>
        <circle class="net-node net-node--2" cx="220" cy="480" r="3"/>
        <circle class="net-node net-node--3" cx="520" cy="360" r="4"/>
        <circle class="net-node net-node--4" cx="860" cy="280" r="3"/>
        <circle class="net-node net-node--5" cx="420" cy="180" r="3"/>
        <circle class="net-node net-node--6" cx="720" cy="150" r="4"/>
        <circle class="net-node net-node--7" cx="640" cy="580" r="3"/>
        <circle class="net-node net-node--8" cx="1120" cy="420" r="4"/>
        <circle class="net-node net-node--9" cx="1400" cy="340" r="3"/>
      </svg>
      <div class="anim-dot anim-dot-1"></div>
      <div class="anim-dot anim-dot-2"></div>
      <div class="anim-dot anim-dot-3"></div>
      <div class="anim-dot anim-dot-4"></div>
    </div>
    <!-- 左侧 -->
    <div class="el-login-sidebar">
      <div class="sidebar-content">
        <h1 class="sidebar-title">星火云应用平台</h1>
        <p style="font-size: 16px;">欢迎使用 — 星火云应用平台</p>
        <p style="font-size: 14px;">
          星火云应用平台，我们的目标不仅是一个企业AI协作工具系统，更是以帮助企业实现高效运营、创新驱动和可持续发展的核心支撑为长远目标。
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
      <!-- 资源包下载入口-->
      <div v-if="!isDesktop" class="sidebar-downloads">
        <div class="download-items">
          <el-tooltip content="点击下载 Win10+ 安装包" placement="top">
            <div class="download-item" @click="downloadWin">
              <el-icon><Monitor /></el-icon>
              <span>Win10+ 安装包</span>
            </div>
          </el-tooltip>
          <el-popover placement="top" :width="180" trigger="hover">
            <template #reference>
              <div class="download-item">
                <el-icon><Iphone /></el-icon>
                <span>移动端 H5</span>
              </div>
            </template>
            <div style="text-align: center;">
              <img v-if="h5QrImg" :src="h5QrImg" alt="移动端 H5 二维码"
                   style="width: 140px; height: 140px; border: 1px solid #ebeef5; border-radius: 8px;"/>
              <div style="font-size: 12px; color: #909399; margin-top: 6px;">扫码访问移动端 H5</div>
            </div>
          </el-popover>

          <el-popover placement="top" :width="180" trigger="hover">
            <template #reference>
              <div class="download-item">
                <el-icon><ChatDotRound /></el-icon>
                <span>微信小程序</span>
              </div>
            </template>
            <div style="text-align: center;">
              <img v-if="wechatImg" :src="wechatImg" alt="微信小程序 二维码"
                   style="width: 140px; height: 140px; border: 1px solid #ebeef5; border-radius: 8px;"/>
            </div>
          </el-popover>
          <el-tooltip content="开发中，敬请期待" placement="top">
            <div class="download-item download-item--soon" @click="comingSoon('Mac')">
              <el-icon><Platform /></el-icon>
              <span>Mac</span>
            </div>
          </el-tooltip>
          <el-tooltip content="开发中，敬请期待" placement="top">
            <div class="download-item download-item--soon" @click="comingSoon('Android')">
              <el-icon><Cellphone /></el-icon>
              <span>Android</span>
            </div>
          </el-tooltip>
          <el-tooltip content="开发中，敬请期待" placement="top">
            <div class="download-item download-item--soon" @click="comingSoon('iOS')">
              <el-icon><Cellphone /></el-icon>
              <span>iOS</span>
            </div>
          </el-tooltip>
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
                type="primary"
                style="width: 100%; background-color: #004fc5;height: 40px; font-weight: bold; font-size: 18px"
                @click="submitLoginForm"
            >登 录
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div style="margin-top: 10px; margin-bottom: 10px;">
        <span style="font-size: 14px; color: #606266;">还没有账号？</span>
        <el-button type="primary" link @click="goRegister">立即注册</el-button>
      </div>

      <el-divider>
        <span style="font-size: 12px">其他方式登录</span>
      </el-divider>

      <div style="margin: 20px">
        <el-tooltip
            content="密码登录"
            placement="bottom"
            v-if="loginForm.loginType !== 1"
        >
          <el-button circle @click="switchLoginType(1)">
            <el-icon style="font-size: 18px"><Lock /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip
            content="手机号登录"
            placement="bottom"
            v-if="loginForm.loginType !== 2"
        >
          <el-button circle @click="switchLoginType(2)">
            <el-icon style="font-size: 18px"><Iphone /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip
            content="邮箱登录"
            placement="bottom"
            v-if="loginForm.loginType !== 3"
        >
          <el-button circle @click="switchLoginType(3)">
            <el-icon style="font-size: 18px"><Message /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>
    <!-- 版权说明 -->
    <div class="login-copyright">
      Copyright © 2024-2026 evancloud.top All Rights Reserved.
    </div>
  </div>
</template>

<script setup>
import { getValidateCodeAPI, getMessageCodeAPI, getEmailCodeAPI } from '@/api/manage/auth/login.js';
import { des } from '@/utils/encryptUtil.js';
import Validate from '@/assets/icons/validate.vue';
import { ref, getCurrentInstance } from "vue";
import { useRouter } from 'vue-router';
import { useStore } from "vuex";
import { ElMessage } from 'element-plus';
import QRCode from 'qrcode';
import { isDesktop } from '@/utils/desktop.js';

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

// Windows安装包下载地址
const WIN_SETUP_URL = 'https://spark.evancloud.top:7101/bundle/星火云_0.0.1_x64-setup.exe';
// 移动端 H5 访问地址
const H5_URL = 'https://spark.evancloud.top:7102/';
// 移动端 H5 二维码图片
const h5QrImg = ref(undefined);
// 微信小程序图片
const wechatImg = ref('https://spark.evancloud.top:7101/bundle/wechatUrl.png');
// 生成移动端 H5 访问二维码
QRCode.toDataURL(H5_URL, { width: 160, margin: 1 }).then(dataUrl => {
  h5QrImg.value = dataUrl;
});

/**
 * 下载 Windows 安装包
 */
function downloadWin() {
  window.open(WIN_SETUP_URL);
}

/**
 * 未上线客户端提示
 * @param name 客户端名称
 */
function comingSoon(name) {
  ElMessage.info(name + ' 客户端开发中，敬请期待');
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
.login {
  height: 100vh;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
  margin: 0;
  position: relative;
  // 全屏浅色科技风渐变背景
  background: linear-gradient(160deg, #f7faff 0%, #eaf2fe 55%, #e3edfd 100%);
}

// 左侧 - 透明容器（背景由全屏 anim-bg 提供）
.el-login-sidebar {
  width: 60%;
  height: 100vh;
  padding: 20px;
  position: relative;
  overflow: hidden;
  color: $color-text-primary;
}

// 科技风动态背景装饰层（全屏铺满）
.anim-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

// 科技网格底纹（双层网格 + 边缘羽化）
.anim-grid {
  position: absolute;
  inset: 0;
  background-image:
      linear-gradient(rgba(0, 79, 197, 0.06) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 79, 197, 0.06) 1px, transparent 1px),
      linear-gradient(rgba(0, 79, 197, 0.04) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 79, 197, 0.04) 1px, transparent 1px);
  background-size: 160px 160px, 160px 160px, 40px 40px, 40px 40px;
  -webkit-mask-image: radial-gradient(ellipse at 50% 45%, #000 40%, transparent 88%);
  mask-image: radial-gradient(ellipse at 50% 45%, #000 40%, transparent 88%);
}

// 柔和光晕（蓝青色系，低透明度）
.anim-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.32;
  animation: blobFloat 18s ease-in-out infinite alternate;
}

.anim-blob-1 {
  width: 720px;
  height: 720px;
  left: -180px;
  top: -200px;
  background: radial-gradient(circle at 30% 30%, #93c5fd 0%, #bfdbfe 40%, transparent 70%);
}

.anim-blob-2 {
  width: 640px;
  height: 640px;
  right: -160px;
  bottom: -180px;
  background: radial-gradient(circle at 60% 40%, #a5f3fc 0%, #cde2fc 40%, transparent 70%);
  animation-delay: -6s;
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

// 旋转轨道环（虚线 + 缓慢自转，边缘小光点）
.anim-orbit {
  position: absolute;
  border: 1px dashed rgba(0, 79, 197, 0.22);
  border-radius: 50%;
  animation: orbitSpin 30s linear infinite;
}

.anim-orbit-1 {
  width: 420px;
  height: 420px;
  right: -100px;
  top: -120px;
}

.anim-orbit-2 {
  width: 260px;
  height: 260px;
  left: 6%;
  bottom: -60px;
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

// 数据流网络线条与节点
.anim-net {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.net-line {
  fill: none;
  stroke: rgba(0, 79, 197, 0.22);
  stroke-width: 1;
  stroke-dasharray: 8 12;
  animation: netFlow 6s linear infinite;
}

.net-line--2 {
  stroke: rgba(0, 150, 216, 0.18);
  animation-duration: 8s;
  animation-direction: reverse;
}

.net-line--3 {
  stroke: rgba(0, 79, 197, 0.14);
  animation-duration: 10s;
}

@keyframes netFlow {
  to {
    stroke-dashoffset: -200;
  }
}

.net-node {
  fill: #004fc5;
  filter: drop-shadow(0 0 4px rgba(0, 120, 255, 0.6));
  animation: nodePulse 3s ease-in-out infinite;
}

.net-node--2,
.net-node--4,
.net-node--7,
.net-node--9 {
  fill: #00b8d9;
  animation-delay: -1s;
}

.net-node--3,
.net-node--6,
.net-node--8 {
  animation-delay: -2s;
}

@keyframes nodePulse {
  0%,
  100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}

// 微光粒子
.anim-dot {
  position: absolute;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(0, 120, 255, 0.45);
  box-shadow: 0 0 12px rgba(0, 120, 255, 0.4);
  animation: dotFloat 8s ease-in-out infinite alternate;
}

.anim-dot-1 { left: 20%; top: 42%; }
.anim-dot-2 { left: 70%; top: 28%; animation-delay: -3s; }
.anim-dot-3 { left: 46%; top: 76%; animation-delay: -6s; }
.anim-dot-4 { left: 86%; top: 62%; animation-delay: -4s; }

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
  margin: 140px 80px 20px 80px;
  text-align: left;
  position: relative;
  z-index: 1;
}

// 平台标题 - 蓝青渐变科技字效
.sidebar-title {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 2px;
  background: linear-gradient(90deg, #004fc5 0%, #0072e0 45%, #00b8d9 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
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
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(6px);
  border: 1px solid rgba(0, 79, 197, 0.1);
  box-shadow: 0 2px 8px rgba(26, 111, 232, 0.06);
  position: relative;
  overflow: hidden;
  transition: all 0.2s;

  // 左侧科技感渐变光条，悬停时点亮
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 3px;
    background: linear-gradient(180deg, #004fc5, #00b8d9);
    opacity: 0;
    transition: all 0.2s;
  }

  &:hover {
    background: #ffffff;
    border-color: rgba(0, 79, 197, 0.2);
    box-shadow: 0 6px 16px rgba(26, 111, 232, 0.14);
    transform: translateY(-2px);

    &::before {
      opacity: 1;
    }
  }
}

.feature-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  background: linear-gradient(135deg, #004fc5, #00b8d9);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  box-shadow: 0 2px 10px rgba(0, 110, 220, 0.35);
}

.feature-text {
  min-width: 0;
}

.feature-name {
  font-size: 15px;
  font-weight: 600;
  color: $color-text-primary;
}

.feature-desc {
  font-size: 12px;
  color: $color-text-secondary;
  margin-top: 2px;
  white-space: nowrap;
}

// 客户端下载入口（固定在左侧栏左下角，一字排开）
.sidebar-downloads {
  position: absolute;
  left: 40px;
  bottom: 28px;
  z-index: 1;
}

.download-items {
  display: flex;
  flex-wrap: nowrap;
  gap: 10px;
}

.download-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: 20px;
  font-size: 13px;
  color: $color-text-primary;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(6px);
  border: 1px solid rgba(0, 79, 197, 0.12);
  box-shadow: 0 2px 8px rgba(26, 111, 232, 0.06);
  cursor: pointer;
  transition: all 0.2s;

  .el-icon {
    font-size: 16px;
    color: #004fc5;
  }

  &:hover {
    background: #ffffff;
    border-color: rgba(0, 79, 197, 0.28);
    box-shadow: 0 4px 12px rgba(26, 111, 232, 0.14);
    transform: translateY(-1px);
  }
}

// 未上线客户端置灰虚线样式
.download-item--soon {
  color: $color-text-secondary;
  border-style: dashed;
  border-color: rgba(0, 79, 197, 0.18);

  .el-icon {
    color: $color-text-secondary;
  }
}

// 右侧 - 登录表单区
.el-login-main {
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 400px;
  max-width: 90%;
  margin: auto;
  margin-right: 8%;
  padding: 40px 36px;
  background: #ffffff;
  border: 1px solid rgba(0, 79, 197, 0.08);
  border-radius: $border-radius-lg;
  box-shadow: 0 8px 32px rgba(26, 111, 232, 0.12);
  position: relative;
  z-index: 1;
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

// 分隔线：文字底色与白色卡片一致，横线在文字两侧正常断开
::v-deep(.el-divider__text) {
  background-color: #ffffff !important;
  color: $color-text-placeholder;
}

// 版权说明
.login-copyright {
  position: fixed;
  bottom: 20px;
  right: 8%;
  text-align: center;
  font-size: 12px;
  color: #909399;
  z-index: 1;
}
</style>


<template>
  <div class="login">
    <!-- 左侧 -->
    <div class="el-login-sidebar">
      <!-- 科技风动态背景，随左栏裁切在60%区域内 -->
      <div class="anim-bg">
        <div class="anim-grid"></div>
        <div class="anim-blob anim-blob-1"></div>
        <div class="anim-blob anim-blob-2"></div>
        <div class="anim-orbit anim-orbit-1"></div>
        <div class="anim-orbit anim-orbit-2"></div>
        <div class="anim-dot anim-dot-1"></div>
        <div class="anim-dot anim-dot-2"></div>
        <div class="anim-dot anim-dot-3"></div>
        <div class="anim-dot anim-dot-4"></div>
      </div>
      <div ref="contentRef" class="sidebar-content">
        <p class="sidebar-kicker">SPARK AI PLATFORM</p>
        <h1 class="sidebar-title">星火云应用平台</h1>
        <p class="sidebar-desc">
          欢迎使用星火云应用平台，我们的目标不仅是一个企业AI协作工具系统，更是以帮助企业实现高效运营、创新驱动和可持续发展的核心支撑为长远目标。
        </p>
        <div ref="featuresRef" class="sidebar-features" :style="{ height: featuresHeight }">
          <div class="feature-item">
            <span class="feature-index">01</span>
            <div class="feature-icon"><img :src="loginIcon1" alt=""/></div>
            <div class="feature-line"></div>
            <div class="feature-text">
              <div class="feature-name">即时通信</div>
              <div class="feature-desc">即时消息与群组协作，文件互传、消息提醒实时同步</div>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-index">02</span>
            <div class="feature-icon"><img :src="loginIcon2" alt=""/></div>
            <div class="feature-line"></div>
            <div class="feature-text">
              <div class="feature-name">自定义流程</div>
              <div class="feature-desc">可视化拖拽编排审批流程，支持条件分支与自动流转</div>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-index">03</span>
            <div class="feature-icon"><img :src="loginIcon3" alt=""/></div>
            <div class="feature-line"></div>
            <div class="feature-text">
              <div class="feature-name">自定义表单</div>
              <div class="feature-desc">拖拽设计各类业务表单，丰富控件开箱即用、数据自动归集</div>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-index">04</span>
            <div class="feature-icon"><img :src="loginIcon4" alt=""/></div>
            <div class="feature-line"></div>
            <div class="feature-text">
              <div class="feature-name">AI管理</div>
              <div class="feature-desc">模型/技能库/MCP统一接入，挂载知识库与知识图谱</div>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-index">05</span>
            <div class="feature-icon"><img :src="loginIcon5" alt=""/></div>
            <div class="feature-line"></div>
            <div class="feature-text">
              <div class="feature-name">AI工作流</div>
              <div class="feature-desc">AI节点融入业务流程，检索推理自动执行、释放自动化潜力</div>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-index">06</span>
            <div class="feature-icon"><img :src="loginIcon6" alt=""/></div>
            <div class="feature-line"></div>
            <div class="feature-text">
              <div class="feature-name">Agent</div>
              <div class="feature-desc">专属智能体挂载知识库与工具，全天候自主完成任务</div>
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
    <div class="el-login-aside">
    <div ref="loginMainRef" class="el-login-main">
      <div class="login-title-row">
        <div>
          <h2 class="login-title">欢迎回来</h2>
          <p class="login-subtitle">登录之后开启您的AI辅助工作之旅吧</p>
        </div>
        <div class="login-header-tools">
          <el-tooltip :content="currentTheme === 'light' ? '深色模式' : '浅色模式'" placement="top">
            <el-button circle @click="toggleTheme">
              <el-icon><Moon v-if="currentTheme === 'light'" /><Sunny v-else /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="语言" placement="top">
            <el-button circle @click="handleLocaleChange">
              <el-icon><Globe /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>
      <div>
        <el-form ref="loginRef" :model="loginForm" :rules="loginFormRules">
          <div class="login-fields">
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
                <el-button type="primary" @click="getSmsCode" :disabled="count !== 0" style="width: 100%; height: 44px; margin: 0; border-radius: 4px; color: white;">
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
          <el-form-item style="width: 100%; margin-top: 5px;">
            <el-button
                :loading="loading"
                type="primary"
                class="auth-submit-btn"
                @click="submitLoginForm"
            >登 录
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="login-agreement">
        <el-checkbox v-model="agreed" @change="onAgreeChange">我已阅读并同意</el-checkbox>
        <el-button type="primary" link @click="openAgreement('service')">《用户服务协议》</el-button>
        <span class="agreement-and">和</span>
        <el-button type="primary" link @click="openAgreement('privacy')">《隐私政策》</el-button>
      </div>
      <div v-if="agreementError" class="agreement-error">请先阅读并同意用户服务协议和隐私政策</div>

      <div class="register-entry">
        <span style="font-size: 14px; color: #606266;">还没有账号？</span>
        <el-button type="primary" link @click="goRegister">立即注册</el-button>
      </div>

      <el-divider>
        <span style="font-size: 12px">其他方式登录</span>
      </el-divider>

      <div class="login-type-row">
        <el-tooltip
            content="密码登录"
            placement="bottom"
            v-if="loginForm.loginType !== 1"
        >
          <el-button circle class="login-type-btn" @click="switchLoginType(1)">
            <el-icon style="font-size: 20px"><Lock /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip
            content="手机号登录"
            placement="bottom"
            v-if="loginForm.loginType !== 2"
        >
          <el-button circle class="login-type-btn" @click="switchLoginType(2)">
            <el-icon style="font-size: 20px"><Iphone /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip
            content="邮箱登录"
            placement="bottom"
            v-if="loginForm.loginType !== 3"
        >
          <el-button circle class="login-type-btn" @click="switchLoginType(3)">
            <el-icon style="font-size: 20px"><Message /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>
      <!-- 版权说明 -->
      <div class="login-copyright">
        Copyright © 2024-2026 evancloud.top All Rights Reserved.
      </div>
    </div>
  </div>
</template>

<script setup>
import {getValidateCodeAPI, getSmsCodeAPI, getEmailCodeAPI, getSessionAPI, getEncryptKeyAPI} from '@/api/manage/auth/login.js';
import { des } from '@/utils/encryptUtil.js';
import { AGREEMENT_VERSION } from '@/utils/agreementDoc.js';
import { localStorageKey } from '@/utils/keyUtils.js';
import Validate from '@/assets/icons/validate.vue';
import { ref, getCurrentInstance, onMounted, onUnmounted, nextTick } from "vue";
import { useRouter } from 'vue-router';
import { useStore } from "vuex";
import { ElMessage } from 'element-plus';
import { Sunny, Moon } from '@element-plus/icons-vue';
import Globe from '@/assets/icons/globe.vue';
import QRCode from 'qrcode';
import { isDesktop } from '@desktop/env.js';
import { getTheme, setTheme } from '@/utils/themeUtil.js';
import loginIcon1 from '@/assets/images/login-1.png';
import loginIcon2 from '@/assets/images/login-2.png';
import loginIcon3 from '@/assets/images/login-3.png';
import loginIcon4 from '@/assets/images/login-4.png';
import loginIcon5 from '@/assets/images/login-5.png';
import loginIcon6 from '@/assets/images/login-6.png';

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

const currentTheme = ref(getTheme());

/**
 * 切换深浅色主题并持久化，与主界面导航栏共用同一份主题配置
 */
function toggleTheme() {
  currentTheme.value = currentTheme.value === 'light' ? 'dark' : 'light';
  setTheme(currentTheme.value);
}

/**
 * 切换语言，语言包尚未接入
 */
function handleLocaleChange() {
  ElMessage.info('功能开发中');
}

// 未勾选协议提交时，在勾选区内联红框提示，不弹 toast
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

// Windows安装包下载地址，文件名与 rename-installer.cjs 产物一致
const WIN_SETUP_URL = `https://spark.evancloud.top:7101/bundle/sparkcloud_${__APP_VERSION__}_x64-setup.exe`;
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

// 功能竖条区补足到登录卡片同高，两栏各自垂直居中后顶底自然对齐
const loginMainRef = ref();
const contentRef = ref();
const featuresRef = ref();
const featuresHeight = ref(undefined);

/**
 * 按登录卡片高度计算功能竖条区高度，使左侧内容块与卡片等高等中
 */
function syncFeaturesHeight() {
  if (!loginMainRef.value || !contentRef.value || !featuresRef.value) {
    return;
  }
  const cardHeight = loginMainRef.value.getBoundingClientRect().height;
  const headingHeight = featuresRef.value.getBoundingClientRect().top - contentRef.value.getBoundingClientRect().top;
  featuresHeight.value = Math.max(200, cardHeight - headingHeight) + 'px';
}

onMounted(() => {
  nextTick(syncFeaturesHeight);
  window.addEventListener('resize', syncFeaturesHeight);
});

onUnmounted(() => {
  window.removeEventListener('resize', syncFeaturesHeight);
});

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
.login {
  height: 100vh;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
  margin: 0;
  position: relative;
  background: linear-gradient(160deg, #f7faff 0%, #eaf2fe 55%, #e3edfd 100%);
}

.el-login-sidebar {
  width: 60%;
  height: 100vh;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
  color: $color-text-primary;
}

.anim-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

.anim-grid {
  position: absolute;
  inset: 0;
  background-image:
      linear-gradient(rgba(0, 79, 197, 0.06) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 79, 197, 0.06) 1px, transparent 1px),
      linear-gradient(rgba(0, 79, 197, 0.04) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 79, 197, 0.04) 1px, transparent 1px);
  background-size: 160px 160px, 160px 160px, 40px 40px, 40px 40px;
  mask-image: radial-gradient(ellipse at 50% 45%, #000 40%, transparent 88%);
}

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

.sidebar-content {
  width: 80%;
  margin: 0 auto;
  transform: translateY(clamp(-20px, -7vh, -120px));
  text-align: left;
  position: relative;
  z-index: 1;
}

.sidebar-kicker {
  margin: 0 0 10px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 4px;
  color: #6366f1;
  opacity: 0.75;
}

.sidebar-title {
  margin: 0;
  font-size: clamp(24px, 1.7vw, 32px);
  font-weight: 600;
  letter-spacing: 3px;
  background: linear-gradient(90deg, #004fc5 0%, #0072e0 45%, #00b8d9 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.sidebar-desc {
  max-width: 90%;
  margin: 18px 0 0;
  padding-left: 14px;
  border-left: 2px solid rgba(99, 102, 241, 0.35);
  font-size: clamp(13px, 0.95vw, 15px);
  line-height: 1.9;
  color: $color-text-secondary;
}

.sidebar-features {
  margin-top: clamp(28px, 2.4vw, 48px);
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: clamp(12px, 0.9vw, 20px);
}

.feature-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: clamp(22px, 1.8vw, 32px) 8px 16px;
  box-sizing: border-box;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.82), rgba(245, 247, 255, 0.6));
  backdrop-filter: blur(6px);
  border: 1px solid rgba(99, 102, 241, 0.18);
  box-shadow: 0 2px 10px rgba(79, 70, 229, 0.08), inset 0 0 24px rgba(139, 92, 246, 0.04);
  overflow: hidden;
  transition: all 0.25s ease;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 20%;
    right: 20%;
    height: 2px;
    border-radius: 0 0 2px 2px;
    background: linear-gradient(90deg, transparent, #0072e0, #6366f1, transparent);
    opacity: 0;
    transition: opacity 0.25s ease;
  }

  &:hover {
    background: linear-gradient(180deg, #ffffff, rgba(240, 245, 255, 0.7));
    border-color: rgba(0, 79, 197, 0.5);
    box-shadow: 0 0 0 1px rgba(0, 79, 197, 0.15), 0 10px 28px rgba(0, 79, 197, 0.25);
    transform: translateY(-3px);

    &::before {
      opacity: 1;
    }

  }

}

.feature-icon {
  flex-shrink: 0;
  line-height: 1;
  animation: iconPulse 2.4s ease-in-out infinite;

  img {
    display: block;
    width: clamp(76px, 5.8vw, 104px);
    height: clamp(76px, 5.8vw, 104px);
    object-fit: contain;
  }
}

.feature-item:nth-child(2) .feature-icon { animation-delay: 0.4s; }
.feature-item:nth-child(3) .feature-icon { animation-delay: 0.8s; }
.feature-item:nth-child(4) .feature-icon { animation-delay: 1.2s; }
.feature-item:nth-child(5) .feature-icon { animation-delay: 1.6s; }
.feature-item:nth-child(6) .feature-icon { animation-delay: 2s; }

@keyframes iconPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.14);
  }
}

.feature-index {
  position: absolute;
  top: 10px;
  right: 12px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 1px;
  color: rgba(0, 79, 197, 0.28);
}

.feature-line {
  width: 22px;
  height: 2px;
  margin-top: 12px;
  border-radius: 1px;
  background: linear-gradient(90deg, #6366f1, #a855f7);
  opacity: 0.7;
}

.feature-text {
  width: 100%;
  min-width: 0;
  margin-top: 10px;
  text-align: center;
}

.feature-name {
  font-size: clamp(13px, 0.95vw, 16px);
  font-weight: 600;
  letter-spacing: 1px;
  color: $color-text-primary;
  white-space: nowrap;
}

.feature-desc {
  display: -webkit-box;
  margin-top: 5px;
  font-size: clamp(11px, 0.75vw, 13px);
  line-height: 1.5;
  color: $color-text-secondary;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

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

.download-item--soon {
  color: $color-text-secondary;
  border-style: dashed;
  border-color: rgba(0, 79, 197, 0.18);

  .el-icon {
    color: $color-text-secondary;
  }
}

.el-login-aside {
  position: relative;
  width: 40%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--app-bg-card);
}

// 暗色左侧品牌区整体灰蓝调，卡片与文字颜色保持浅色主题原样
html.dark {
  .login {
    background: linear-gradient(160deg, #1b2740 0%, #16203a 55%, #111a30 100%);
  }

  .sidebar-kicker {
    color: #a5b4fc;
    opacity: 0.95;
  }

  .sidebar-title {
    background: linear-gradient(90deg, #93c5fd 0%, #7dd3fc 45%, #67e8f9 100%);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  .sidebar-desc {
    color: #b8c5dc;
  }

  .anim-grid {
    background-image:
        linear-gradient(rgba(147, 197, 253, 0.09) 1px, transparent 1px),
        linear-gradient(90deg, rgba(147, 197, 253, 0.09) 1px, transparent 1px),
        linear-gradient(rgba(147, 197, 253, 0.05) 1px, transparent 1px),
        linear-gradient(90deg, rgba(147, 197, 253, 0.05) 1px, transparent 1px);
  }

  .anim-blob-1 {
    opacity: 0.4;
    background: radial-gradient(circle at 30% 30%, rgba(96, 165, 250, 0.5) 0%, rgba(59, 130, 246, 0.22) 40%, transparent 70%);
  }

  .anim-blob-2 {
    opacity: 0.36;
    background: radial-gradient(circle at 60% 40%, rgba(56, 189, 248, 0.42) 0%, rgba(14, 116, 144, 0.18) 40%, transparent 70%);
  }

  .anim-orbit {
    border-color: rgba(147, 197, 253, 0.3);
  }

  .anim-dot {
    background: rgba(147, 197, 253, 0.6);
    box-shadow: 0 0 12px rgba(96, 165, 250, 0.55);
  }

  .feature-item {
    background: linear-gradient(180deg, rgba(51, 65, 92, 0.72), rgba(41, 54, 80, 0.6));
    border-color: rgba(147, 197, 253, 0.25);
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3), inset 0 0 24px rgba(96, 165, 250, 0.06);

    &:hover {
      background: linear-gradient(180deg, rgba(62, 78, 110, 0.85), rgba(51, 65, 92, 0.75));
      border-color: rgba(147, 197, 253, 0.5);
    }
  }

  .feature-name {
    color: #f1f5f9;
  }

  .feature-desc {
    color: #b6c4da;
  }

  .feature-index {
    color: rgba(191, 219, 254, 0.6);
  }

  .download-item {
    color: #dbe6f4;
    background: rgba(41, 54, 80, 0.7);
    border-color: rgba(147, 197, 253, 0.22);

    &:hover {
      color: #ffffff;
      background: rgba(58, 73, 102, 0.9);
    }
  }

  .login-type-btn {
    background-color: var(--app-bg-sub);

    &:hover,
    &:focus {
      background-color: var(--app-bg-sub);
    }
  }

  .el-divider__text {
    background-color: var(--app-bg-card) !important;
  }
}

.el-login-main {
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 400px;
  max-width: 90%;
  position: relative;
  z-index: 1;
}

.el-input {
  height: 44px;
}

.auth-submit-btn {
  width: 100%;
  height: 40px;
  font-weight: bold;
  font-size: 18px;
  background-color: #004fc5 !important;
  border-color: #004fc5 !important;

  &:hover,
  &:focus {
    background-color: #2669cd !important;
    border-color: #2669cd !important;
  }
}

:deep(.el-input__inner) {
  &:-webkit-autofill,
  &:-webkit-autofill:hover,
  &:-webkit-autofill:focus {
    -webkit-text-fill-color: var(--app-text-primary);
    caret-color: var(--app-text-primary);
    -webkit-box-shadow: 0 0 0 1000px var(--el-fill-color-blank) inset;
    transition: background-color 9999s ease-in-out 0s;
  }
}

.login-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-top: -16px;
}

.login-header-tools {
  display: flex;
  /* 按钮本身是 34px 无边框圆形、图标仅 20px，留白已足够，间距收紧到 2px 视觉上才不散 */
  gap: 2px;

  /* 主题 / 语言：去掉描边和底色，只在悬停时浮出淡色底，图标放大到 20px */
  :deep(.el-button.is-circle) {
    width: 34px;
    height: 34px;
    padding: 0;
    border: none !important;
    /* 全局 .el-button 用 !important 把圆角压成了 5px，这里拉回正圆 */
    border-radius: 50% !important;
    background-color: transparent !important;
    color: var(--app-text-secondary);
    font-size: 20px;
    transition: background-color 0.18s ease, color 0.18s ease;

    &:hover,
    &:focus-visible,
    &:active {
      background-color: var(--app-primary-light) !important;
      color: var(--app-primary) !important;
      border: none !important;
    }

    .el-icon {
      font-size: 20px;
    }
  }
}

.login-title {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
  color: $color-text-primary;
}

.login-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: $color-text-secondary;
}

.login-fields {
  height: 174px;
  margin-top: 24px;
}

.login-type-row {
  display: flex;
  align-items: center;
  margin-top: 20px;
}

.login-type-row .login-type-btn {
  width: 46px;
  height: 46px;
  padding: 0;
  margin: 0 16px 0 0;
  color: $color-primary;
  border: 1px solid rgba(0, 79, 197, 0.12);
  border-radius: 50% !important;
  background-color: var(--app-bg-card);
  box-shadow: 0 2px 8px rgba(26, 111, 232, 0.1);
  transition: all 0.2s;

  &:hover,
  &:focus {
    color: $color-primary;
    border-color: rgba(0, 79, 197, 0.32);
    background-color: var(--app-bg-sub);
    box-shadow: 0 6px 16px rgba(26, 111, 232, 0.18);
    transform: translateY(-1px);
  }
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

.login-agreement {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  margin: 12px 0 6px;
  font-size: 13px;
  color: $color-text-secondary;

  :deep(.el-checkbox) {
    height: auto;
    margin-right: 0;
  }

  :deep(.el-button) {
    height: auto;
    padding: 0;
    font-size: 13px;
  }

  .agreement-and {
    margin: 0 2px;
  }
}

.agreement-error {
  padding-top: 4px;
  font-size: 12px;
  line-height: 18px;
  text-align: center;
  color: var(--el-color-danger);
}

.register-entry {
  margin-top: 10px;
  margin-bottom: 10px;
  text-align: right;
}

::v-deep(.el-divider__text) {
  background-color: var(--app-bg-card);
  color: $color-text-placeholder;
}

// 版权说明
.login-copyright {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
  font-size: 12px;
  color: #909399;
}
</style>


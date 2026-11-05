<template>
  <!-- 左侧：品牌与能力介绍，登录页与注册页共用 -->
  <section class="brand-stage">
    <div class="stage-glow stage-glow-a"></div>
    <div class="stage-glow stage-glow-b"></div>

    <div class="stage-content">
      <div class="stage-brand">
        <img :src="brandLogo" alt="星火云"/>
        <span>星火云<i>AI</i></span>
      </div>

      <h1 class="stage-title">让业务流程真正跑在 AI 上</h1>
      <p class="stage-desc">
        以<b>高效运营、创新驱动</b>为核心的一站式智能工作底座，把知识、流程、表单与智能体串成一条完整链路。
      </p>

      <div class="cap-grid">
        <div v-for="group in capGroups" :key="group.title" class="cap-col">
          <div class="cap-hd">{{ group.title }}</div>
          <div class="cap-cards">
            <article v-for="item in group.items" :key="item.name" class="cap-card">
              <span class="cap-icon" :style="{ '--z': item.scale }">
                <img :src="item.icon" alt=""/>
              </span>
              <div class="cap-text">
                <div class="cap-name">{{ item.name }}</div>
                <div class="cap-desc">{{ item.desc }}</div>
              </div>
            </article>
          </div>
        </div>
      </div>

      <div class="stage-metrics">
        <div v-for="item in metrics" :key="item.label">
          <b>{{ item.value }}</b><span>{{ item.label }}</span>
        </div>
      </div>
    </div>

    <!-- 客户端下载入口 -->
    <div v-if="!isDesktop" class="stage-downloads">
      <span class="download-label">客户端下载</span>
      <div class="download-items">
        <el-tooltip content="点击下载 Win10+ 安装包" placement="top">
          <div class="download-item" @click="downloadWin">
            <el-icon><Monitor /></el-icon>
            <span>Win10+</span>
          </div>
        </el-tooltip>
        <el-popover placement="top" :width="180" trigger="hover">
          <template #reference>
            <div class="download-item">
              <el-icon><Iphone /></el-icon>
              <span>移动端 H5</span>
            </div>
          </template>
          <div class="qr-popover">
            <img v-if="h5QrImg" :src="h5QrImg" alt="移动端 H5 二维码" class="qr-popover__img"/>
            <div class="qr-popover__tip">扫码访问移动端 H5</div>
          </div>
        </el-popover>

        <el-popover placement="top" :width="180" trigger="hover">
          <template #reference>
            <div class="download-item">
              <el-icon><ChatDotRound /></el-icon>
              <span>微信小程序</span>
            </div>
          </template>
          <div class="qr-popover">
            <img v-if="wechatImg" :src="wechatImg" alt="微信小程序 二维码" class="qr-popover__img"/>
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
  </section>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { ChatDotRound, Cellphone, Iphone, Monitor, Platform } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import QRCode from 'qrcode';
import { isDesktop } from '@desktop/env.js';
import { getTheme } from '@/utils/themeUtil.js';
import loginIcon1 from '@/assets/images/login-1.png';
import loginIcon2 from '@/assets/images/login-2.png';
import loginIcon3 from '@/assets/images/login-3.png';
import loginIcon4 from '@/assets/images/login-4.png';
import loginIcon5 from '@/assets/images/login-5.png';
import loginIcon6 from '@/assets/images/login-6.png';
import brandLogoLight from '@/assets/images/logo-light.png';
import brandLogoDark from '@/assets/images/logo.png';

/**
 * 六大能力：分两组呈现，icon 用 3D 素材、按各自缩放保留"忽大忽小"的错落感
 */
const capGroups = [
  {
    title: '协作与流程',
    items: [
      { name: '即时通信', desc: '群组协作，消息实时触达', icon: loginIcon1, scale: 0.92 },
      { name: '自定义流程', desc: '拖拽编排，条件分支', icon: loginIcon2, scale: 1.34 },
      { name: '自定义表单', desc: '组件开箱即用', icon: loginIcon3, scale: 1.03 }
    ]
  },
  {
    title: '智能与自动化',
    items: [
      { name: 'AI 管理', desc: '模型技能 MCP 统一接入', icon: loginIcon4, scale: 1.13 },
      { name: 'AI 工作流', desc: 'AI 节点融入业务流程', icon: loginIcon5, scale: 0.82 },
      { name: 'Agent 智能体', desc: '挂载知识库，全天候自主', icon: loginIcon6, scale: 1.29 }
    ]
  }
];

const metrics = [
  { value: '6 大', label: '核心能力模块' },
  { value: '100+', label: '预置业务模板' },
  { value: '40+', label: '大模型与技能' },
  { value: '24h', label: '智能体自主运行' },
  { value: '99.9%', label: '服务可用性' }
];

const currentTheme = ref(getTheme());

// 品牌图标跟随主题色系：浅色主题用浅底淡色图标，深色主题用深蓝渐变图标
const brandLogo = computed(() => (currentTheme.value === 'light' ? brandLogoLight : brandLogoDark));

/**
 * 主题切换由右侧面板发起，此处监听全局事件保持品牌图标同步
 */
function syncTheme(e) {
  currentTheme.value = e && e.detail ? e.detail : getTheme();
}

onMounted(() => {
  window.addEventListener('app-theme-change', syncTheme);
  window.addEventListener('storage', syncTheme);
});

onUnmounted(() => {
  window.removeEventListener('app-theme-change', syncTheme);
  window.removeEventListener('storage', syncTheme);
});

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
</script>

<style lang="scss" scoped>
// 品牌区渐变 token 由 variables 统一注入，此处仅保留组件私有的 rgba 光效变量
$auth-glow-a: radial-gradient(circle, rgba(47, 123, 255, 0.16), transparent 68%);
$auth-glow-b: radial-gradient(circle, rgba(34, 200, 216, 0.14), transparent 68%);
$auth-card-grad: linear-gradient(148deg, rgba(255, 255, 255, 0.95) 0%, rgba(234, 242, 255, 0.78) 58%, rgba(255, 255, 255, 0.9) 100%);
$auth-card-grad-hover: linear-gradient(148deg, rgba(255, 255, 255, 0.98) 0%, rgba(226, 238, 255, 0.85) 58%, rgba(255, 255, 255, 0.94) 100%);
$auth-divider: rgba(23, 43, 77, 0.08);

// 左侧品牌区
.brand-stage {
  position: relative;
  flex: 1 1 auto;
  width: 63.5%;
  padding: clamp(40px, 5.6vh, 58px) clamp(40px, 5vw, 72px) 0 clamp(44px, 6.1vw, 88px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: $color-text-primary;
  background: $auth-stage-bg;
}

.stage-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  pointer-events: none;
  z-index: 0;
}

.stage-glow-a {
  width: 520px;
  height: 520px;
  right: -140px;
  top: -60px;
  background: $auth-glow-a;
}

.stage-glow-b {
  width: 460px;
  height: 460px;
  left: -120px;
  bottom: -160px;
  background: $auth-glow-b;
}

.stage-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

// 品牌标识
.stage-brand {
  display: flex;
  align-items: center;
  gap: 15px;
  user-select: none;

  img {
    width: clamp(44px, 3.9vw, 56px);
    height: clamp(44px, 3.9vw, 56px);
    border-radius: clamp(12px, 1.05vw, 15px);
    box-shadow: 0 12px 26px -10px rgba(26, 111, 232, 0.5);
    -webkit-user-drag: none;

    // 深色下品牌蓝投影会晕成一圈亮框，换成中性深色阴影
    html.dark & {
      box-shadow: 0 12px 26px -10px rgba(0, 0, 0, 0.6);
    }
  }

  span {
    font-size: clamp(19px, 1.6vw, 23px);
    font-weight: 800;
    letter-spacing: 0.8px;
    color: $color-text-primary;

    i {
      font-style: normal;
      margin-left: 2px;
      background: $auth-ai-grad;
      -webkit-background-clip: text;
      background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }
}

// 主标题与副文案
.stage-title {
  margin: clamp(38px, 7vh, 78px) 0 0;
  max-width: 620px;
  font-size: clamp(22px, 2.05vw, 29px);
  font-weight: 800;
  letter-spacing: 0.6px;
  line-height: 1.4;
  background: $auth-title-grad;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stage-desc {
  margin: clamp(12px, 1.8vh, 18px) 0 0;
  max-width: 520px;
  font-size: clamp(12.5px, 0.94vw, 13.5px);
  line-height: 1.95;
  color: $app-text-secondary;

  b {
    font-weight: 650;
    color: $ds-brand;
  }
}

// 能力分组双列布局，每组三张卡
.cap-grid {
  margin-top: clamp(26px, 4.6vh, 46px);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 24px;
  width: 100%;
  max-width: 596px;
}

.cap-hd {
  display: flex;
  align-items: center;
  gap: 9px;
  margin-bottom: 12px;
  font-size: 10px;
  font-weight: 650;
  letter-spacing: 2.4px;
  color: $app-text-placeholder;

  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: $auth-divider;
  }
}

.cap-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

// 卡片无描边，白到淡蓝磨砂渐变加柔和投影
.cap-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  height: 96px;
  padding: 0 $spacing-md;
  border-radius: 12px;
  background: $auth-card-grad;
  box-shadow: 0 10px 26px -22px rgba(20, 45, 90, 0.55), 0 1px 2px rgba(23, 43, 77, 0.035);
  backdrop-filter: blur(10px) saturate(1.06);
  transition: box-shadow 0.22s ease, background 0.22s ease;

  &:hover {
    background: $auth-card-grad-hover;
    box-shadow: 0 18px 34px -24px rgba(20, 45, 90, 0.6), 0 1px 2px rgba(23, 43, 77, 0.04);
  }
}

// 图标槽位放大，但素材按 contain 等比放入不做尺寸归一，各图标自带留白不同，视觉上保留错落感
.cap-icon {
  flex: none;
  width: 68px;
  height: 68px;
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: contain;
    // stylelint-disable-next-line function-disallowed-list
    transform: scale(var(--z, 1));
    -webkit-user-drag: none;
  }
}

.cap-text {
  min-width: 0;
}

.cap-name {
  font-size: 13px;
  font-weight: 650;
  letter-spacing: 0.2px;
  color: $app-text-primary;
  white-space: nowrap;
}

.cap-desc {
  margin-top: 3px;
  font-size: 11px;
  line-height: 1.5;
  color: $app-text-secondary;
  white-space: nowrap;
}

// 数据指标
.stage-metrics {
  margin-top: clamp(24px, 4.4vh, 40px);
  display: flex;
  align-items: stretch;
  max-width: 700px;

  > div {
    padding-right: clamp(14px, 1.4vw, 20px);
    margin-right: clamp(14px, 1.4vw, 20px);
    border-right: 1px solid $auth-divider;

    &:last-child {
      padding-right: 0;
      margin-right: 0;
      border-right: 0;
    }
  }

  b {
    display: block;
    font-size: clamp(17px, 1.32vw, 19px);
    font-weight: 700;
    letter-spacing: 0.3px;
    color: $app-text-primary;
  }

  span {
    display: block;
    margin-top: 4px;
    font-size: 10.5px;
    color: $app-text-placeholder;
    white-space: nowrap;
  }
}

// 底部客户端下载轻文字入口
.stage-downloads {
  position: absolute;
  left: clamp(44px, 6.1vw, 88px);
  bottom: clamp(24px, 3.4vh, 44px);
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 18px;
}

.download-label {
  font-size: 10.5px;
  letter-spacing: 1.5px;
  color: $app-text-placeholder;
}

.download-items {
  display: flex;
  align-items: center;
  gap: 18px;
}

.download-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 3px 0;
  font-size: 11.5px;
  color: $app-text-secondary;
  cursor: pointer;
  border-bottom: 1px dashed transparent;
  transition: color 0.18s ease, border-color 0.18s ease;

  .el-icon {
    font-size: 14px;
    color: $app-text-placeholder;
    transition: color 0.18s ease;
  }

  &:hover {
    color: $ds-brand;
    border-bottom-color: $ds-brand;

    .el-icon {
      color: $ds-brand;
    }
  }
}

.download-item--soon {
  color: $app-text-placeholder;

  .el-icon {
    color: $app-text-placeholder;
  }
}

// 下载二维码弹层
.qr-popover {
  text-align: center;

  &__img {
    width: 140px;
    height: 140px;
    border: 1px solid $ds-line;
    border-radius: 8px;
  }

  &__tip {
    margin-top: 6px;
    font-size: 12px;
    color: $app-text-placeholder;
  }
}

// 深色主题
html.dark {
  $auth-glow-a: radial-gradient(circle, rgba(47, 123, 255, 0.22), transparent 68%);
  $auth-glow-b: radial-gradient(circle, rgba(34, 200, 216, 0.16), transparent 68%);
  $auth-card-grad: linear-gradient(148deg, rgba(51, 65, 92, 0.78) 0%, rgba(41, 54, 80, 0.66) 58%, rgba(45, 58, 86, 0.75) 100%);
  $auth-card-grad-hover: linear-gradient(148deg, rgba(62, 78, 110, 0.88) 0%, rgba(51, 65, 92, 0.78) 58%, rgba(56, 70, 100, 0.85) 100%);
  $auth-divider: rgba(147, 197, 253, 0.16);

  .brand-stage {
    background: $auth-stage-bg;
  }

  .stage-glow-a {
    background: $auth-glow-a;
  }

  .stage-glow-b {
    background: $auth-glow-b;
  }

  .stage-title {
    background: $auth-title-grad;
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  .cap-hd {
    color: rgba(147, 197, 253, 0.72);

    &::after {
      background: $auth-divider;
    }
  }

  .cap-card {
    background: $auth-card-grad;
    box-shadow: 0 12px 28px -22px rgba(0, 0, 0, 0.8), 0 1px 2px rgba(0, 0, 0, 0.25);

    &:hover {
      background: $auth-card-grad-hover;
      box-shadow: 0 18px 34px -24px rgba(0, 0, 0, 0.85);
    }
  }

  .stage-metrics {
    > div {
      border-right-color: $auth-divider;
    }
  }
}

// 适配
// 矮屏压缩标题与卡片间距，保证 768/800 高度下不溢出
@media (max-height: 860px) {
  .stage-title {
    margin-top: 34px;
  }

  .cap-grid {
    margin-top: 24px;
  }

  .cap-card {
    height: 84px;
    padding: 0 14px;
    gap: 13px;
  }

  .cap-icon {
    width: 58px;
    height: 58px;
  }

  .stage-metrics {
    margin-top: 22px;
  }
}

// 窄屏收起能力清单与指标，保留标题
@media (max-width: 1120px) {
  .cap-grid,
  .stage-metrics {
    display: none;
  }
}
</style>

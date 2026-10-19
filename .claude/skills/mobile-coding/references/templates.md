# spark-app 移动端代码模板（生成新文件前必读）

配套规范：`.claude/skills/mobile-coding/SKILL.md`（禁止项/红线）。

## 1. 目录结构

```
src/
├── api/                    # API 接口（按业务分文件）
│   ├── auth/login.js       # 登录相关 API
│   ├── system/user.js      # 用户 API
│   └── request.js          # axios 请求封装（UniAdapter，全局使用）
├── components/             # 组件（home 首页组件 / icons 图标组件）
├── pages/                  # 页面
│   ├── login.vue           # 登录页（账号/手机号/邮箱三种方式）
│   ├── home/index.vue      # 首页
│   ├── business/index.vue  # 工作台
│   ├── message/index.vue   # 消息/通知
│   ├── agent/index.vue     # AI 智能体
│   ├── chat/index.vue      # 聊天
│   ├── contacts/index.vue  # 通讯录
│   └── my/                 # 个人中心（index、infomation、updatePwd、updateAvatar）
├── utils/encryptUtil.js    # DES 加密工具
├── assets/images/          # 静态资源
├── App.vue                 # 应用入口
├── main.js                 # createSSRApp 入口
├── pages.json              # 页面路由配置
└── manifest.json           # 应用配置
```

## 2. 页面路由（pages.json）

所有页面统一 `"navigationStyle": "custom"`。新增页面需在 `pages.json` 的 `pages` 数组注册：

```json
{
  "path": "pages/xxx/index",
  "style": {
    "navigationStyle": "custom"
  }
}
```

## 3. 页面模板（参考 `pages/login.vue`）

```vue
<template>
  <view class="page-box safe-area-page">
    <up-form labelWidth="60">
      <up-form-item label="名称" prop="name">
        <up-input v-model="form.name" placeholder="请输入名称" border="bottom" clearable />
      </up-form-item>
    </up-form>
    <up-button class="submit-btn" type="primary" @click="submitForm" :loading="loading">
      提交
    </up-button>
  </view>
</template>

<script setup>
import {ref} from 'vue';
import {useStore} from 'vuex';
import {xxxAPI} from '@/api/xxx/xxx.js';

const store = useStore();
const loading = ref(false);
const form = ref({ name: undefined });

/**
 * 提交表单
 */
function submitForm() {
  loading.value = true;
  xxxAPI(form.value).then(res => {
    loading.value = false;
    if (res.code !== 200) return;
    uni.showToast({ title: '提交成功', icon: 'success' });
  })
}
</script>

<style scoped lang="scss">
.page-box {
  padding: 20rpx;
}
</style>
```

**页面规范**：
- 结构：`<template>` → `<script setup>` → `<style scoped lang="scss">`
- 组件用 uViewPlus（`up-` 前缀），easycom 自动引入，无需手动 import
- 状态用 `ref` / `reactive`；全局状态用 `useStore()`（Vuex）
- 函数加中文 JSDoc 注释；注释在代码上方，只写一眼看不出来的核心信息，CSS 不写注释
- 样式 `scoped`，单位用 `rpx`；页面容器可加 `safe-area-page` 类适配安全区

### 3.1 自定义导航栏适配（微信小程序）

所有页面 `navigationStyle: custom`，顶部适配统一走 `@/utils/navBarUtil.js`，由 `App.vue` 在 `onLaunch` 调 `initNavBarInfo()` 初始化（状态栏高度 + 胶囊按钮布局），**禁止写死顶部间距**：

- 普通头部页：头部节点绑定 `:style="navHeaderStyle()"`（小程序 `paddingTop = 状态栏高度 + 16px`，内容行与微信胶囊同高；H5 返回空对象样式不变）。头部右侧自带操作按钮时（先例：聊天页 ... 更多），同一行会与胶囊挤位，改用 `:style="navHeaderBelowStyle()"` 让整个内容行下移到胶囊底边下方（paddingTop = menuBottom + 8px），右侧按钮保持正常贴右、不加 margin 避让。
- 导航栏头部背景统一纯白 `background-color: $spark-color-header-bg;`（#ffffff，变量定义在 uni.scss；暖白 #faf7f2 被否），底部用 1rpx 极淡蓝紫细描边 `$spark-color-header-border` 与内容区分隔；蓝色头部历次被否（#0052cc 压抑、低饱和渐变发灰、高明度渐变发白、#5b94f5 纯色也被否），导航栏禁止用蓝色背景。例外：工作台/我的页欢迎区保留蓝色融页渐变（欢迎卡片不是导航栏），渐变中下段偏浅，白字会发虚，其上文字必须用深色加粗：标题 `$spark-color-text` + font-weight 800，副文案 `rgba(23,43,77,.7)` + 500，图标同样用深色。工作台首页容器用 flex 列布局（welcome-card `flex-shrink:0`、滚动区 `flex:1; min-height:0`），欢迎卡高度由 padding 撑开、不要再写死 height/calc 高度；渐变色标压缩到 140px 内收尾于 #eaf2ff 并设同名 background-color 兜底。**up-tabbar 组件根节点自带 `flex:1`，放进 flex column 容器会被纵向拉伸形成透明遮挡层，必须加 `:deep(.u-tabbar){flex:none}` 覆盖**（message/flow/llm/my/home 五个 tab 页统一此写法），不要用 `:placeholder="false"` + 手动 padding-bottom 的绕路方案。聊天气泡、状态点等非头部元素不受此限。
- 导航栏标题 `.header-title` 必须与工作台板块标题完全同款：`font-style: italic; font-weight: 700; letter-spacing: 1px;`，小程序端纯 `color: #2f7bff;`（**禁止加 text-shadow 辉光**：小程序渲染引擎会把发光糊成描边导致发虚，H5 端才允许），H5 端蓝色渐变文字（`background-image: linear-gradient(92deg, #2563eb 0%, #3d8bff 100%); background-clip: text; -webkit-text-fill-color: transparent;`，两端用条件编译区分，直接抄 pages/home/index.vue 的 .section-title）；以蓝为主，不要用偏紫色值（#6a5cff、#8b5cf6 均被否）。头部内返回箭头、右侧操作图标统一 `color="#2f7bff"`。字号分级：三个底部 tab 主页（消息/流程/AI应用）20px，其余二级页面 17px。
- 状态栏文字色按头部深浅逐页配置：`pages.json` 全局 `navigationBarTextStyle: "black"`（纯白导航栏页），深色/蓝色头部页在该页 style 里单独覆盖为 `"white"`（当前为 login、home/index、my/index 三页）；改头部深浅时必须同步检查此项。
- 底部 tabbar：导航项统一在 `utils/menuUtil.js#visibleTabs()` 配置，图标必须用 uview-plus 的实心（`-fill`）图标；选中色 `activeColor="#004fc5"`（与登录/登出按钮深蓝主色一致，当前五项：home-fill / file-text-fill / grid-fill / chat-fill / account-fill）。
- 列表卡片图标统一蓝白素色，禁止青/紫/绿/橙/粉多色渐变底色池（用户评「花里胡哨」）：容器 `background-color: rgba(47,123,255,.1); border: 1rpx solid rgba(47,123,255,.18); border-radius`，图标 `color="#2f7bff"`（两处统一规格：46px 容器 + 12px 圆角 + 34px 图标；先例 pages/llm/index.vue 的 .card-icon、pages/home/index.vue 的 .item-icon，后者卡片固定高 72px）；up-tabs 统一：`lineColor="#2f7bff"`、`:activeStyle="{color: '#2f7bff', fontWeight: 700}"`、下划线为页签宽度的六成、组件自动居中（`:lineWidth="Math.floor(uni.getSystemInfoSync().windowWidth / tabList.length * 0.6)"`，仅 `:scrollable="false"` 平分屏宽时成立，铺满版被否，先例 llm/flow 页）。
- 渐变/全屏头部页（参考 `pages/home/index.vue`、`pages/my/index.vue`）：头部自身绑动态 `paddingTop`；需要落到胶囊下方的定位元素用 `navBarInfo.menuBottom` 计算，平台差异用 `// #ifdef MP-WEIXIN` / `// #ifdef H5` 条件编译分别返回。

## 4. API 请求

### 导入

```js
import request from '@/api/request'
```

`request` 基于 axios + `UniAdapter`（内部调用 `uni.request`），已全局可用，无需重复封装。

### 认证

- H5：从 `localStorage.getItem('token')` 读取；微信小程序：`wx.getStorageSync('token')`
- 由 `request.js` 自动注入 `Authorization` header，禁止手工拼接
- `params` 中的 `undefined`/`null` 会被自动过滤

### 响应处理（request.js 已统一）

- `code === 200`：成功，返回 `response.data`
- `code === 601`：未授权，自动 `uni.reLaunch` 到 `/pages/login`
- 其他：`toast(response.data.message)` 统一提示，业务代码无需重复弹提示

### 新增 API（参考 `api/auth/login.js`）

```js
import request from '@/api/request'

/**
 * 登录
 * @param data 登录参数
 * @returns {Promise}
 */
export function loginAPI(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    params: data
  })
}
```

- 命名 `功能+API`；GET/POST 都用 `params`（文件上传等明确 body 场景除外）
- 每个函数加中文 JSDoc

## 5. 平台适配（条件编译）

H5 与微信小程序差异用条件编译处理：

```js
// #ifdef H5
// H5 专用代码
// #endif

// #ifdef MP-WEIXIN
// 微信小程序专用代码
// #endif
```

示例（参考 `login.vue` 验证码区）：H5 用 `<img>` + `#suffix` 插槽，小程序用 `<image>` 组件。

## 6. 加密工具

```js
import { des } from '@/utils/encryptUtil'

const encrypted = des(password) // DES 加密，ECB 模式 + Pkcs7 填充
```

- 密码、手机号等敏感字段提交前用 `des()` 加密
- 密钥从 `import.meta.env.VITE_ENCRYPT_PRIVATE_KEY` 读取，禁止硬编码

## 7. Vuex（登录态）

- 登录走 `store.dispatch('user/login', { loginForm: data })`，成功后 `uni.reLaunch` 到 `/pages/home/index`
- token 由 `user` 模块管理，`store.getters['user/getToken']` 供 request.js 取用
- 新增模块必须 `namespaced: true` 并在 `store/index.js` 注册

## 8. uViewPlus 组件

easycom 自动引入，直接使用（无需 import）：

```vue
<up-button>按钮</up-button>
<up-input v-model="value" />
<up-form>...</up-form>
<up-form-item label="名称" prop="name" />
<up-divider text="分隔线" />
```

前缀映射：`u-xxx` / `u--xxx` / `up-xxx` → `uview-plus/components/u-xxx/u-xxx.vue`。统一使用 `up-` 前缀。

## 9. 环境变量

| 变量 | 说明 |
|------|------|
| `VITE_USER_NODE_ENV` | 环境标识：`development` / `production` |
| `VITE_HTTP_BASE_API` | API 基础地址（开发环境需配置，生产环境留空） |
| `VITE_ENCRYPT_PRIVATE_KEY` | DES 加密密钥 |

- 开发环境：`.env.dev`；生产环境：`.env.prod`

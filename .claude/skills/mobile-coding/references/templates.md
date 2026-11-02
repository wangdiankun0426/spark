# spark-app 移动端代码模板（生成新文件前必读）

配套规范：`.claude/skills/mobile-coding/SKILL.md`（禁止项/红线）。

## 1. 目录结构

```
src/
├── api/                    # API 接口（按业务分文件）
│   ├── auth/login.js       # 登录相关 API
│   ├── system/user.js      # 用户 API
│   └── request.js          # axios 请求封装（UniAdapter，全局使用）
├── components/             # 通用组件
│   ├── AppTabbar/          # 底部导航（自绘，替代 up-tabbar）
│   ├── ThemeGlyph/         # 亮/暗主题图标（纯 CSS 画月亮/太阳）
│   ├── UserAvatar/ UserPicker/ AiSessionPicker/ TenantSwitch/ FormFieldValue/
│   └── icons/              # 业务图标组件
├── pages/                  # 页面（必须与 pages.json 一致）
│   ├── login.vue           # 登录页（账号/手机号/邮箱三种方式）
│   ├── home/index.vue      # 工作台
│   ├── flow/               # 流程（index、instanceDetail）
│   ├── llm/                # AI 应用（index、wfInstanceDetail）
│   ├── message/index.vue   # 消息/通知
│   ├── my/                 # 个人中心（index、setting、information、updatePwd、updateAvatar）
│   ├── agreement/          # 协议页
│   ├── oauthLogin.vue      # 单点登录回跳
│   └── error/              # notFound、noPermission
├── views/                  # 复用视图（chat 聊天、contact 通讯录）
├── styles/theme.scss       # 亮/暗两套 CSS 变量 + 全局主题底座
├── store/                  # Vuex（modules/user.js、modules/theme.js）
├── utils/                  # themeUtil / useTheme / navBarUtil / menuUtil / encryptUtil / keyUtils / markdownUtil / debounce
├── assets/images/          # 静态资源
├── App.vue                 # 应用入口（引入 theme.scss、初始化导航与主题）
├── main.js                 # createSSRApp 入口（uview setConfig）
├── pages.json              # 页面路由配置
├── uni.scss                # 全局 SCSS 变量（已映射主题 CSS 变量）
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
  <view class="page-box safe-area-page" :class="themeClass">
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
import {useTheme} from '@/utils/useTheme';

const store = useStore();
const {themeClass} = useTheme();
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
  padding: 10px;
}
</style>
```

**页面规范**：
- 结构：`<template>` → `<script setup>` → `<style scoped lang="scss">`
- 根节点必须同时挂 `safe-area-page` 与 `:class="themeClass"`（`const {themeClass} = useTheme()`），主题细节见 3.2
- 组件用 uViewPlus（`up-` 前缀），easycom 自动引入，无需手动 import
- 状态用 `ref` / `reactive`；全局状态用 `useStore()`（Vuex）
- 函数加中文 JSDoc 注释；注释在代码上方，只写一眼看不出来的核心信息，CSS 不写注释
- 样式 `scoped`，单位以 `px` 为主（全站先例统一 px；`rpx` 仅用于 tabbar 指示条等极少数随屏宽等比缩放的装饰）；颜色一律用 `var(--app-*)`，禁止硬编码

### 3.1 自定义导航栏适配（微信小程序）

所有页面 `navigationStyle: custom`，顶部适配统一走 `@/utils/navBarUtil.js`，由 `App.vue` 在 `onLaunch` 调 `initNavBarInfo()` 初始化（状态栏高度 + 胶囊按钮布局），**禁止写死顶部间距**：

- 普通头部页：头部节点绑定 `:style="navHeaderStyle()"`（小程序 `paddingTop = 状态栏高度 + 16px`，内容行与微信胶囊同高；H5 返回空对象样式不变）。头部右侧自带操作按钮时分两种处理：二级页（先例：聊天页 ... 更多）同一行会与胶囊挤位，改用 `:style="navHeaderBelowStyle()"` 让整个内容行下移到胶囊底边下方（paddingTop = menuBottom + 8px），右侧按钮保持正常贴右、不加 margin 避让；tab 主页（24px 大标题行）必须保持 `navHeaderStyle()` 与其他 tab 主页标题齐平、不得整行下移；头部带副标题时（先例：pages/flow/index.vue），右侧操作按钮放进副标题 flex 行末尾、`margin-left:auto` 贴右并与副标题文字垂直居中——副标题行本就落在胶囊下方，无需任何胶囊避让；无副标题、按钮只能放标题行的场景才用 navBarInfo 水平避让。
- **两级头部**（2026-11 UI 升级定版，取代旧的单一 `header-bg` + italic 品牌色标题）：
  - **tab 主页**（工作台/流程/AI应用/消息，底部挂 `AppTabbar` 的页面）：头部底色 `background-color: $spark-color-header-bg;`（`uni.scss` 映射到 `var(--app-header-bg)`，亮 #ffffff、暗 #131d2e），**不加下分隔线**；标题左对齐、`24px/700/letter-spacing:.5px/color:var(--app-text-primary)`，非斜体。
  - **二级页**（`navigateTo` 进入、带返回箭头的页面，含聊天窗口 `views/chat/index`、聊天设置 `chatDetail`、聊天记录 `chatMsgRecord`）：头部高 `48px`、`padding:0 14px`、`background-color: var(--app-bg-card)`（亮 #ffffff、暗 #1a2537）、底部 `1px solid var(--app-border-light)`，节点绑 `navHeaderStyle()`；左右各留 `30px` 等宽槽位放返回与操作图标，标题 `flex:1` 居中、`18px/700/letter-spacing:.4px/var(--app-text-primary)`，非斜体。
  - 两级标题都**已废弃**旧的 italic + `letter-spacing:1px` + 品牌色 `var(--app-brand)` + H5 渐变文字 `--app-title-gradient` + `text-shadow` 辉光方案，不得再抄旧代码里的 `.section-title` 渐变文字写法。
  - 头部返回/操作图标统一 19~20px，颜色走 `@/utils/themeUtil` 的 `getHeaderIconColor(isDark)`（亮 #2f7bff、暗 #8fb6ff 提亮一档），**不要写死 #2f7bff，也不要每页重写一份 `backIconColor` computed**；右侧自带操作按钮（如聊天页「...」更多）与微信胶囊挤位时整行改用 `navHeaderBelowStyle()`，按钮贴右不加 margin 避让。
  - 蓝色纯色导航底历次被否（#0052cc 压抑、低饱和渐变发灰、高明度渐变发白、#5b94f5 也被否），导航栏禁止蓝底；蓝色只用在欢迎卡渐变与品牌元素，禁止写死 `#fff` 头部。聊天气泡、状态点等非头部元素不受头部规格约束。
- **欢迎卡**（工作台 `.welcome-card` / 我的 `.my-header`）：整卡品牌蓝渐变，`background-image: var(--app-gradient-welcome|--app-gradient-profile)`（亮色 172deg 蓝渐变收尾 #8fb8ff，暗色另为 radial 蓝光叠加深色渐变），并必须同规则写 `background-color: var(--app-bg-mid)` 兜底；底部圆角 `20px`，高度由 padding 撑开、**不写死 height/calc**；工作台容器用 flex 列布局（welcome-card `flex-shrink:0`、滚动区 `flex:1;min-height:0`）。**卡上所有文字/图标一律 `var(--app-fill-tx)`**（亮 #ffffff、暗 #e9f1ff），靠 `opacity` 分级而非写 rgba 白：主标题 21px/700 不透明、副文案约 12.5px/opacity:.82、辅助文字 11.5px/opacity:.72~.92；渐变填充按钮内的图标/文字用 `getOnFillColor(isDark)`。禁止写死 `#fff` 或 `rgba(255,255,255,*)` 作文字色；纯装饰性圆环、光斑的 rgba 白可保留。旧的「140px 内收尾 #eaf2ff + 深色加粗文字」方案已随整卡蓝渐变废弃。
- 状态栏图标色由主题运行时统一设置，不再逐页配置：`utils/themeUtil.js#applyStatusBarTheme()` 调 `uni.setNavigationBarColor`（亮色黑图标、暗色白图标）并按皮肤切 `uni.setBackgroundColor` 改窗口与下拉区底色；切皮肤时由 store 触发，每页 `useTheme()` 初始化时再校正一次。`pages.json` 的 `navigationBarTextStyle: "black"` 只作首屏兜底，新增页面不必单独覆盖。
- 底部 tabbar：导航项统一在 `utils/menuUtil.js#visibleTabs()` 配置（按菜单权限过滤），图标用 uview-plus 实心（`-fill`）图标，当前五项 home-fill / file-text-fill / **star-fill（中间「AI应用」，带 `center:true` 凸起圆钮）** / chat-fill / account-fill；AI 是平台主入口，圆钮是底栏唯一视觉重心。渲染走自绘 `@/components/AppTabbar`（显式 `import`，`<AppTabbar :value="active" @change="..."/>`，自定义组件不走 easycom）：组件高 `54px` + 安全区内边距自兜，底色与上描边走 `--app-bg-tabbar` / `--app-border-light`；普通项选中/未选中色由组件内 computed 按皮肤给出（亮 #004fc5 / #909399，暗 #4d8dff / #8b9cb5），选中项顶部有 36rpx 品牌渐变指示条；中间凸起圆钮常态 46px（`--app-bg-sub` 底 + `--app-border-light` 描边 + 上浮 -24px），选中放大到 48px、底切 `--app-gradient-fill`、阴影整条用 `--app-shadow-center`（亮蓝辉光/暗黑影，两套取值，**禁止再写 `.theme-dark .xxx` 外部类覆盖阴影**——组件样式隔离小程序不生效）、钮上图标用 `getOnFillColor(isDark)`；未读红点用 `--app-error-solid`。页面不传 `activeColor`、不留 padding-bottom、**不写 `:deep(.u-tabbar){flex:none}` 补丁**、不用 `:placeholder="false"` 绕路；`App.vue` 已对宿主节点写 `app-tabbar{display:block;flex:none}`。
- 列表卡片图标统一蓝白素色、柔色底无描边（2026-11 升级定版），禁止青/紫/绿/橙/粉多色渐变底色池（用户评「花里胡哨」）：容器 `background-color: var(--app-primary-soft)`、不加 border；图标用品牌色实色（`up-icon :color` 走 isDark computed 或 `getHeaderIconColor`）。两种现行尺寸：工作台 `pages/home/index.vue` 的 `.item-icon` 为 38px 容器 + 11px 圆角 + 18px 图标；AI 应用 `pages/llm/index.vue` 的 `.card-icon` 为 42px 容器 + 12px 圆角 + 34px 图标；卡片圆角 14、不再固定 72px 高。旧的「46px 容器 + 12px 圆角 + rgba(.1) 底 + rgba(.18) 描边」规格已废弃。**此条只管列表卡片**，「我的」页基础功能/热门功能是功能宫格，按功能用不同色（蓝/青/绿/橙/紫）区分是用户确认保留的既定设计，不算违规。
- 页签统一用自绘分段控件 `.seg-tab`（2026-11 升级定版，先例 `pages/flow/index.vue`、`pages/llm/index.vue`），不再新增 `up-tabs`：高 30px、圆角 9px；均分版各段 `flex:1`（flow，gap 4），胶囊版各段 `flex:none` + 页面左右 padding 14px（llm，gap 6）；选中段底切 `var(--app-gradient-fill)`、辉光 `box-shadow: var(--app-shadow-brand)`、文字 `var(--app-fill-tx)` + 700，未选段文字 `var(--app-text-secondary)`（13~13.5px）；选中切换用自绘滑块/类名，无组件隔离问题。旧页面已有的 `up-tabs` 可保留，其旧配置（`lineColor="#2f7bff"`、`:activeStyle="{color:'#2f7bff',fontWeight:700}"`、下划线六成宽、`scrollable=false` 时 `:lineWidth` 公式）不再用于新页。
- 渐变/全屏头部页（参考 `pages/home/index.vue`、`pages/my/index.vue`）：头部自身绑动态 `paddingTop`；需要落到胶囊下方的定位元素用 `navBarInfo.menuBottom` 计算，平台差异用 `// #ifdef MP-WEIXIN` / `// #ifdef H5` 条件编译分别返回。

### 3.2 主题（亮色 / 暗色）

与 Web 端一致，只区分亮色（light，默认）与暗色（dark），页面结构不变。小程序无法给 `page` 节点动态挂类名，所以用「页面根节点绑类名 + CSS 变量逐层继承」实现，**不要另造第二套方案**：

- 每个页面根节点必须 `:class="themeClass"`（`const {themeClass} = useTheme()`，来自 `@/utils/useTheme`）；子组件不单独绑，由父级作用域覆盖。短内容页底色由 `App.vue` 全局 `.safe-area-page { background-color: var(--app-page-base) }` 兜住，暗色下不会露白。
- 变量只定义一处：`styles/theme.scss` 的两个 mixin（`theme-light-vars` / `theme-dark-vars`），分别挂在 `page, html, .theme-light` 与 `.theme-dark` 上，`App.vue` 全局引入。**新增变量必须亮/暗两套同时加且同名**，不允许只在一套里定义。
- 样式里只用 `var(--app-*)`：文字 `--app-text-primary|regular|secondary|tertiary|placeholder|muted`，底色 `--app-bg-page|card|sub|overlay|hover`、`--app-page-base`、`--app-header-bg`、`--app-bg-tabbar`，描边 `--app-border|border-light|divide`，另有 `--app-primary*`、`--app-brand*`、`--app-brand-border`、`--app-error-border`、`--app-error-solid`（未读徽标实心红）、`--app-shadow-card`、`--app-shadow-brand`（品牌蓝辉光）、`--app-shadow-center`（凸起圆钮选中态整段阴影）、状态色 `--app-bg-info|success|warning|error|purple` 与其 `--app-text-*`；品牌渐变填充面相关 `--app-gradient-fill` + 其上文字 `--app-fill-tx`（亮 #fff/暗 #e9f1ff）、hero 卡片 `--app-gradient-hero`/`--app-hero-border`、页面纹理 `--app-bg-texture`、头像底 `--app-avatar-bg`。`uni.scss` 的 `$spark-color-*` 已全部映射到这些变量，老页面沿用旧变量名即自动跟随皮肤，改造时不必逐行替换。
- **渐变必须整段收进一个变量**（`--app-gradient-login|brand|fill|card|welcome|profile|hero`、`--app-title-gradient`），页面只写 `background-image: var(--app-gradient-xxx)`；禁止 `linear-gradient(160deg, var(--app-bg-start), ...)` 这种在函数里嵌 `var()` 的写法，部分小程序渲染层解析不了。`--app-title-gradient` 与旧 italic 标题已随 2026-11 升级废弃，仅存量引用，新页不要用。
- 需要具体色值传 prop 的地方（uview 的 `color` / `activeColor` / `placeholderStyle`、`up-parse` 的 `tag-style`、`up-input/up-textarea` 的 `customStyle` 底色）用 computed 按 `isDark` 给实色，**不传内联 `var()`**（小程序原生输入组件存疑）；同一色值跨文件重复时收进 `utils/themeUtil.js`，现成函数：`getSearchBgColor`（搜索/选人弹层底）、`getHeaderIconColor`（头部返回/操作图标，亮 #2f7bff/暗 #8fb6ff）、`getOnFillColor`（品牌渐变填充面上的文字/图标，亮 #fff/暗 #e9f1ff）；单文件内的一次性色值就地写在 computed 里即可。
- 切换入口固定两处，都是纯图标、不带文字：登录页右上角圆角方块按钮（`login-theme-btn` + `ThemeGlyph`，64rpx 方块、`border-radius:20rpx`，小程序用 `navBarInfo.menuBottom + 10` 避开胶囊），「我的」页基础功能第 4 个图标格（排在「修改头像」右侧，`ThemeGlyph` 走 `item.glyph` 分支）。文案统一「深色模式 / 浅色模式」（与 PC 端一致），不写「暗色 / 亮色」；内部主题值与类名仍用 `light` / `dark`。
- 图标语义统一为「**点一下会变成什么**」：浅色时画月亮（点了变深色）、深色时画太阳。`ThemeGlyph` 组件的 `dark` prop 就是这个语义，PC 端登录页与顶栏同规则，两端不要各写一套。
- 切换持久化：主题存 `utils/keyUtils.js#localStorageKey.THEME`，读写走 `utils/themeUtil.js`，状态在 `store/modules/theme.js`（`SET_THEME` 同时写缓存与调用 `applyDocumentTheme`），`App.vue` 的 `onLaunch` 派发 `theme/initTheme` 避免首屏先亮后暗。

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

样式里的条件编译用 `/* #ifdef H5 */` 包裹，否则 WXSS 会留下不认识的伪类。已知两个必须处理的差异：

- **浏览器自动填充底色**：`App.vue` 已有全局 `.uni-input-input:-webkit-autofill` 规则，用 `box-shadow: 0 0 0 1000px var(--app-bg-card) inset !important` 盖掉蓝色底并配 `-webkit-text-fill-color: var(--app-text-primary)`。**必须用 `.uni-input-input` 类名，不能写 `input` 标签** —— 编译器会把标签选择器改写成 `uni-input` 自定义元素，永远匹配不到原生输入框。输入框压在渐变上时（登录页）局部覆盖为 `--app-autofill-bg`，且 scoped 里要写 `:deep(.uni-input-input)`。
- **状态栏与窗口底色**：小程序端 `page` 节点背景由 wxss 固定，只能靠 `uni.setBackgroundColor` 改下拉区，见 `utils/themeUtil.js#applyStatusBarTheme()`。

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

**主题适配要点（改组件配色前必须知道）**：

- **组件样式是组件级隔离的**：编译后选择器带 `data-v-xxx`，组件 json 也没有 `styleIsolation`，所以 `.theme-dark .u-xxx { background-color: ... }` 这类外部覆盖在小程序里**完全不生效**，不要再用它改组件底色。
- **颜色有两套来源，都要顾到**：
  - SCSS 变量（`$u-main-color`、`$u-content-color`、`$u-border-color`、`$u-bg-color`、`$u-disabled-color` 等）：在 `uni.scss` 里 `@import 'node_modules/uview-plus/theme.scss'` **之后**重定向到 `var(--app-*)`，后赋值的同名变量才生效；`$u-popup-content-background-color`、`$u-button-plain-background-color`、`$u-upload-*` 在组件内是 `!default`，在此处先赋值即可。
  - JS props 默认值：`u-input` / `u-search` / `u-textarea` / `u-code-input` 的文字色与占位符色写死在 props 里（`#303133`、`#c0c4cc` 等），渲染时以内联 style 落到节点，SCSS 变量管不到，必须在 `main.js` 用 `setConfig({ props: { input: {...} } })` 改为引用 CSS 变量。**注意内联 `var()` 在 H5 稳定、小程序原生 input 存疑**，所以页面上的 `up-input` 直接传 `:color` / `:placeholder-style`（computed 按 `isDark` 给实色），不要只依赖 setConfig。
- **已知例外**：`u-tabbar` 底色写死且隔离，已由自绘 `AppTabbar` 替代（见 3.1）。
- **up-popup / up-modal 在 flex 列布局页面里的占位坑**：H5 下未显示时根节点 0 高；微信小程序 virtualHost 模式下其插槽内容会提前渲染并作为 flex item 参与高度分配，吃掉列表区半屏空间、把底部固定栏/操作按钮顶上。页面根是 `display:flex;flex-direction:column` 时，弹层（含 up-modal——其内部就是 u-popup，以及内含 popup 的 UserPicker/AiSessionPicker/TenantSwitch）必须包一层脱离文档流的宿主 `<view class="sheet-host">`；该类已在 `App.vue` 全局定义（`position:absolute;top:0;left:0;width:0;height:0;overflow:visible`），页面直接用类名、禁止再各写一份。内部遮罩/面板均为 fixed 定位不受影响。先例：`pages/flow/index.vue`、`pages/flow/instanceDetail.vue`、`pages/llm/index.vue`、`pages/my/index.vue`。

排查组件"暗色下还是白底/黑字"时，按这个顺序看：组件 `.vue` 里有没有 `!default` 变量 → props 默认值里有没有写死的色 → 是不是 `styleIsolation` 造成的，不要先怀疑 CSS 变量没生效。

## 9. 环境变量

| 变量 | 说明 |
|------|------|
| `VITE_USER_NODE_ENV` | 环境标识：`development` / `production` |
| `VITE_HTTP_BASE_API` | API 基础地址（开发环境需配置，生产环境留空） |
| `VITE_ENCRYPT_PRIVATE_KEY` | DES 加密密钥 |

- 开发环境：`.env.dev`；生产环境：`.env.prod`
- 版本号用 `__APP_VERSION__`（`vite.config.js` 的 `define` 读 `package.json` 注入），页面直接取，不要再包 `typeof` 兜底；与 PC 端 `spark-ui` 同一套做法，但两端 `package.json` 各自独立。

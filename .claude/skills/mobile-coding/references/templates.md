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
  padding: 20rpx;
}
</style>
```

**页面规范**：
- 结构：`<template>` → `<script setup>` → `<style scoped lang="scss">`
- 根节点必须同时挂 `safe-area-page` 与 `:class="themeClass"`（`const {themeClass} = useTheme()`），主题细节见 3.2
- 组件用 uViewPlus（`up-` 前缀），easycom 自动引入，无需手动 import
- 状态用 `ref` / `reactive`；全局状态用 `useStore()`（Vuex）
- 函数加中文 JSDoc 注释；注释在代码上方，只写一眼看不出来的核心信息，CSS 不写注释
- 样式 `scoped`，单位用 `rpx`；颜色一律用 `var(--app-*)`，禁止硬编码

### 3.1 自定义导航栏适配（微信小程序）

所有页面 `navigationStyle: custom`，顶部适配统一走 `@/utils/navBarUtil.js`，由 `App.vue` 在 `onLaunch` 调 `initNavBarInfo()` 初始化（状态栏高度 + 胶囊按钮布局），**禁止写死顶部间距**：

- 普通头部页：头部节点绑定 `:style="navHeaderStyle()"`（小程序 `paddingTop = 状态栏高度 + 16px`，内容行与微信胶囊同高；H5 返回空对象样式不变）。头部右侧自带操作按钮时（先例：聊天页 ... 更多），同一行会与胶囊挤位，改用 `:style="navHeaderBelowStyle()"` 让整个内容行下移到胶囊底边下方（paddingTop = menuBottom + 8px），右侧按钮保持正常贴右、不加 margin 避让。
- 导航栏头部背景统一 `background-color: $spark-color-header-bg;`（`uni.scss` 已把它映射到 `var(--app-header-bg)`：亮色 #ffffff、暗色 #131d2e；暖白 #faf7f2 被否，**禁止写死 `#fff`**），底部用 1rpx 极淡蓝紫细描边 `$spark-color-header-border` 与内容区分隔；蓝色头部历次被否（#0052cc 压抑、低饱和渐变发灰、高明度渐变发白、#5b94f5 纯色也被否），导航栏禁止用蓝色背景。例外：工作台/我的页欢迎区保留蓝色融页渐变（欢迎卡片不是导航栏），渐变中下段偏浅，白字会发虚，其上文字必须用深色加粗：标题 `$spark-color-text` + font-weight 800，副文案 `var(--app-text-secondary)` + 500，图标同样用深色；暗色皮肤下这几个变量自动转浅色，不要单独写死色值。工作台首页容器用 flex 列布局（welcome-card `flex-shrink:0`、滚动区 `flex:1; min-height:0`），欢迎卡高度由 padding 撑开、不要再写死 height/calc 高度；渐变色标压缩到 140px 内收尾于 #eaf2ff 并设同名 background-color 兜底；渐变已收进 `--app-gradient-welcome` / `--app-gradient-profile`（暗色另有一套取值），页面写 `background-image: var(--app-gradient-welcome)` 并用 `background-color: var(--app-bg-mid)` 兜底。**底部导航统一用自绘组件 `@/components/AppTabbar`，禁止再用 `up-tabbar`**：uview 的 tabbar 把底色写死成 `#fff`，且组件样式隔离（编译后选择器带 `data-v-xxx`、组件 json 无 `styleIsolation`），小程序里外部类名改不动，暗色下底部必然露白。`App.vue` 已对宿主节点写 `app-tabbar { display:block; flex:none }`，放进 flex column 容器不会被压缩，页面只写 `<AppTabbar :value="active" @change="handleOnTabChange"/>`（自定义组件不走 easycom，需显式 `import AppTabbar from '@/components/AppTabbar/index.vue'`），**不要再加 `:deep(.u-tabbar){flex:none}` 之类的补丁**，也不要用 `:placeholder="false"` + 手动 padding-bottom 的绕路方案。聊天气泡、状态点等非头部元素不受此限。
- 导航栏标题 `.header-title` 必须与工作台板块标题完全同款：`font-style: italic; font-weight: 700; letter-spacing: 1px;`，小程序端纯 `color: var(--app-brand);`（亮色 #2f7bff、暗色 #4d8dff；**禁止加 text-shadow 辉光**：小程序渲染引擎会把发光糊成描边导致发虚，H5 端才允许），H5 端蓝色渐变文字（`background-image: var(--app-title-gradient); background-clip: text; -webkit-text-fill-color: transparent;`，两端用条件编译区分，直接抄 `pages/home/index.vue` 的 `.section-title` 或 `App.vue` 的 `.page-header-title`）；以蓝为主，不要用偏紫色值（#6a5cff、#8b5cf6 均被否）。头部内返回箭头、右侧操作图标统一 `color="#2f7bff"`。字号分级：三个底部 tab 主页（消息/流程/AI应用）20px，其余二级页面 17px。
- 状态栏图标色由主题运行时统一设置，不再逐页配置：`utils/themeUtil.js#applyStatusBarTheme()` 调 `uni.setNavigationBarColor`（亮色黑图标、暗色白图标）并按皮肤切 `uni.setBackgroundColor` 改窗口与下拉区底色；切皮肤时由 store 触发，每页 `useTheme()` 初始化时再校正一次。`pages.json` 的 `navigationBarTextStyle: "black"` 只作首屏兜底，新增页面不必单独覆盖。
- 底部 tabbar：导航项统一在 `utils/menuUtil.js#visibleTabs()` 配置（按菜单权限过滤），图标必须用 uview-plus 的实心（`-fill`）图标，当前五项 home-fill / file-text-fill / grid-fill / chat-fill / account-fill；渲染走 `AppTabbar`，选中/未选中色由组件内 computed 按皮肤给出（亮 #004fc5 / #909399，暗 #4d8dff / #8b9cb5），页面不再传 `activeColor`。底色与上描边走 `--app-bg-tabbar` / `--app-border-light`，高度 50px + 安全区内边距由组件自己兜住，页面容器不要再给 tabbar 留 padding-bottom。
- 列表卡片图标统一蓝白素色，禁止青/紫/绿/橙/粉多色渐变底色池（用户评「花里胡哨」）：容器 `background-color: rgba(47,123,255,.1); border: 1rpx solid rgba(47,123,255,.18); border-radius`，图标 `color="#2f7bff"`（两处统一规格：46px 容器 + 12px 圆角 + 34px 图标；先例 pages/llm/index.vue 的 .card-icon、pages/home/index.vue 的 .item-icon，后者卡片固定高 72px）；**此条只管列表卡片**，「我的」页的基础功能/热门功能是功能宫格，按功能用不同色（蓝/青/绿/橙/紫）区分是用户确认保留的既定设计，不算违规；up-tabs 统一：`lineColor="#2f7bff"`、`:activeStyle="{color: '#2f7bff', fontWeight: 700}"`、下划线为页签宽度的六成、组件自动居中（`:lineWidth="Math.floor(uni.getSystemInfoSync().windowWidth / tabList.length * 0.6)"`，仅 `:scrollable="false"` 平分屏宽时成立，铺满版被否，先例 llm/flow 页）。
- 渐变/全屏头部页（参考 `pages/home/index.vue`、`pages/my/index.vue`）：头部自身绑动态 `paddingTop`；需要落到胶囊下方的定位元素用 `navBarInfo.menuBottom` 计算，平台差异用 `// #ifdef MP-WEIXIN` / `// #ifdef H5` 条件编译分别返回。

### 3.2 主题（亮色 / 暗色）

与 Web 端一致，只区分亮色（light，默认）与暗色（dark），页面结构不变。小程序无法给 `page` 节点动态挂类名，所以用「页面根节点绑类名 + CSS 变量逐层继承」实现，**不要另造第二套方案**：

- 每个页面根节点必须 `:class="themeClass"`（`const {themeClass} = useTheme()`，来自 `@/utils/useTheme`）；子组件不单独绑，由父级作用域覆盖。短内容页底色由 `App.vue` 全局 `.safe-area-page { background-color: var(--app-page-base) }` 兜住，暗色下不会露白。
- 变量只定义一处：`styles/theme.scss` 的两个 mixin（`theme-light-vars` / `theme-dark-vars`），分别挂在 `page, html, .theme-light` 与 `.theme-dark` 上，`App.vue` 全局引入。**新增变量必须亮/暗两套同时加且同名**，不允许只在一套里定义。
- 样式里只用 `var(--app-*)`：文字 `--app-text-primary|regular|secondary|tertiary|placeholder|muted`，底色 `--app-bg-page|card|sub|overlay|hover`、`--app-page-base`、`--app-header-bg`、`--app-bg-tabbar`，描边 `--app-border|border-light|divide`，另有 `--app-primary*`、`--app-brand*`、`--app-shadow-card`、状态色 `--app-bg-info|success|warning|error|purple` 与其 `--app-text-*`。`uni.scss` 的 `$spark-color-*` 已全部映射到这些变量，老页面沿用旧变量名即自动跟随皮肤，改造时不必逐行替换。
- **渐变必须整段收进一个变量**（`--app-gradient-login|brand|card|welcome|profile`、`--app-title-gradient`），页面只写 `background-image: var(--app-gradient-xxx)`；禁止 `linear-gradient(160deg, var(--app-bg-start), ...)` 这种在函数里嵌 `var()` 的写法，部分小程序渲染层解析不了。
- 需要具体色值传 prop 的地方（uview 的 `color` / `activeColor` / `placeholderStyle`、`up-parse` 的 `tag-style`）用 computed 按 `isDark` 给实色，不传 `var()`；同一色值在多个文件重复出现时收进 `utils/themeUtil.js`（如 `getSearchBgColor`），单文件内的一次性色值就地写在 computed 里即可。
- 切换入口固定两处，都是纯图标、不带文字：登录页右上角圆形按钮（`login-theme-btn` + `ThemeGlyph`，小程序用 `navBarInfo.menuBottom + 10` 避开胶囊），「我的」页基础功能第 4 个图标格（排在「修改头像」右侧，`ThemeGlyph` 走 `item.glyph` 分支）。文案统一「深色模式 / 浅色模式」（与 PC 端一致），不写「暗色 / 亮色」；内部主题值与类名仍用 `light` / `dark`。
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

排查组件"暗色下还是白底/黑字"时，按这个顺序看：组件 `.vue` 里有没有 `!default` 变量 → props 默认值里有没有写死的色 → 是不是 `styleIsolation` 造成的，不要先怀疑 CSS 变量没生效。

## 9. 环境变量

| 变量 | 说明 |
|------|------|
| `VITE_USER_NODE_ENV` | 环境标识：`development` / `production` |
| `VITE_HTTP_BASE_API` | API 基础地址（开发环境需配置，生产环境留空） |
| `VITE_ENCRYPT_PRIVATE_KEY` | DES 加密密钥 |

- 开发环境：`.env.dev`；生产环境：`.env.prod`
- 版本号用 `__APP_VERSION__`（`vite.config.js` 的 `define` 读 `package.json` 注入），页面直接取，不要再包 `typeof` 兜底；与 PC 端 `spark-ui` 同一套做法，但两端 `package.json` 各自独立。

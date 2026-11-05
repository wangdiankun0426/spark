---
name: mobile-coding
description: Spark 项目前端移动端（spark-app，uni-app H5 / 微信小程序）编码规范。新增或修改 spark-app 的 src/**、pages.json、manifest.json、新增页面、对接后端接口、调整配色与亮/暗主题、改 uview 组件样式或底部导航时触发。提供项目背景、API 约定、禁止项与红线；完整页面/API 模板与平台适配细则在 references/ 下，生成代码前必须先 Read 加载，禁止凭摘要编造模板。
---

# spark-app 移动端（uni-app）编码规范

## 1. 触发边界

**适用**：`spark-app/` 目录下 `src/**`、`pages.json`、`manifest.json` 的创建、修改。

**不适用**：纯文档、git 操作、`dist/`、`node_modules/`。

**强制加载规则（最高优先级）**：本文件只含规则摘要，**代码模板不在本文件内**。命中第 7 节导航表的任何场景时，必须先 Read `references/templates.md`，再动手写代码；禁止凭本文件摘要或记忆编造页面/API 模板，违者按红线处理——返工。

## 2. 项目背景

- **类型**：基于 uni-app 的跨平台移动端应用，支持 H5 与微信小程序。
- **技术栈**：Vue 3 + uni-app + Vite + **uViewPlus**（easycom 自动引入，`up-` 前缀，无需手动 import）。
- **SSR 模式**：`main.js` 使用 `createSSRApp` 创建应用。
- **响应约定**：后端返回 `{ code, message, data }`；`code===200` 成功，`code===601` 自动 `uni.reLaunch` 到 `/pages/login`。
- **登录平台标识**：移动端登录请求带 `loginPlatform = 2`。
- **主题**：内置亮/暗两套皮肤（与 Web 端一致），由 `styles/theme.scss` 的 `--app-*` CSS 变量驱动，页面根节点绑 `themeClass` 跟随；细则见 `references/templates.md` 3.2。
- **样式架构（对标 PC 三层变量）**：`styles/theme.scss` 是运行时主题色唯一定义处（亮/暗两套，只放定义）；`uni.scss` 是 SCSS 侧唯一变量入口（uni-app 插件自动注入每个编译单元，只准放变量），承载全部 `$app-*` 同名映射（右值为 `var(--app-*)`）、静态刻度（`$spacing-*`/`$radius-*`/`$font-size-*`/`$transition-*`/`$z-*`）与 `$u-*` 覆盖；`styles/base.scss` 放字体/滚动条/页面底色/`.safe-area-page`/autofill 底座，`styles/shell.scss` 放 `.sheet-host`/tabbar 宿主等全局结构类，二者由 `App.vue` 全局引入。消费层只准写 `$app-*`，禁止裸 `var()`。

## 3. API 约定（要点）

- 统一 `import request from '@/api/request'`（axios + UniAdapter，自动注入 `Authorization`，空值自动过滤）
- 命名 `功能+API`；GET/POST 都用 `params`（文件上传等明确 body 场景除外）；每个函数加中文 JSDoc
- `code===200` 成功取数据；`601` 自动跳登录；其他 code 由 request.js 统一 toast，业务代码不重复提示
- 密码等敏感字段提交前用 `@/utils/encryptUtil` 的 `des()` 加密，密钥来自 `import.meta.env.VITE_ENCRYPT_PRIVATE_KEY`

## 4. 页面与平台适配（要点）

- 页面结构 `<template>` → `<script setup>` → `<style scoped lang="scss">`，样式单位以 `px` 为主（全站先例统一用 px，`rpx` 仅限指示条等极少数需随屏宽等比缩放的装饰）
- 字号、圆角、间距、过渡、浮层层级一律用 `uni.scss` 的静态刻度变量（`$font-size-*`/`$radius-*`/`$spacing-*`/`$transition-*`/`$z-*`），禁止硬编码数值；刻度无完全匹配值时就近归档，不为孤值新增档位；页面内相邻叠放的 `z-index: 0/1` 属局部 stacking 不入档
- 每个页面根节点必须同时挂 `safe-area-page` 与 `:class="themeClass"`（`const {themeClass} = useTheme()`），漏一个该页切主题就不跟随
- 颜色一律走 `uni.scss` 的 `$app-*` 变量，禁止硬编码、禁止裸写 `var(--app-*)`；渐变整段用 `$app-gradient-*`，不在 `linear-gradient()` 里嵌变量
- 底部导航统一用自绘 `@/components/AppTabbar`，禁止 `up-tabbar`（uview 的 tabbar 底色写死 `#fff` 且组件样式隔离，小程序暗色下必然露白）；中间「AI应用」为凸起圆钮（`star-fill` + `center:true`），配置在 `menuUtil.js#visibleTabs()`
- 流程/AI 应用首页的页签统一用自绘分段控件（`.seg-tab`），不再新增 `up-tabs`；两级头部（tab 主页 / 二级页）与标题字号、欢迎卡白字体系以 templates.md 3.1 为准
- 新增页面必须注册到 `pages.json` 的 `pages` 数组，统一 `"navigationStyle": "custom"`
- H5 与微信小程序差异用条件编译 `#ifdef H5` / `#ifdef MP-WEIXIN` 处理
- 注释在代码上方，只写一眼看不出来的核心信息，禁止用括号补充细化说明；禁止 `// ===== 标题 =====` 装饰性分隔线，分组标题直接写 `// 标题`；`<style>` / `.scss` 注释只写在选择器（类）顶部一行核心信息（为什么/适配意图），`{}` 规则块内部、声明行之间及行尾一律不写
- 代码标识符统一首字母小写驼峰（lowerCamelCase）：变量、函数、事件处理函数、请求参数名等；组件名用 PascalCase；禁止 `snake_case`。日志/报错消息首词同样用驼峰，如 `console.log('receiverChange error', e)`
- 完整模板与细则见 `references/templates.md`

## 5. 禁止项

1. 未经用户同意引入新的 UI 库、HTTP 库、状态管理库
2. 使用 Options API / mixin / 普通 `<script>` —— 业务页面用 `<script setup>`
3. 在 `.vue` 中写非 `scoped` 的 `<style>`（全局文件除外）
4. 硬编码颜色/字号/圆角/间距/层级，或在 `.vue`/`base.scss`/`shell.scss` 裸写 `var(--app-*)` —— 颜色用 `uni.scss` 的 `$app-*` 同名变量，静态样式走刻度变量；`var()` 只允许出现在 theme.scss 定义层、uni.scss 映射右值与 JS 通道（main.js setConfig、themeUtil 注释）；必须以实色传组件 prop 时用 computed 按 `isDark` 出值，同一色值跨文件重复的收进 `utils/themeUtil.js`（如 `getSearchBgColor`）
5. 直接手工拼接 `Authorization` / 硬编码 token
6. 将 `VITE_ENCRYPT_PRIVATE_KEY` 写入源码/日志
7. 在 console 输出 token、密码、明文、密钥等敏感信息
8. POST 用 `data` 传参（与后端约定相悖，文件上传等场景除外）
9. 在 H5/小程序间复制粘贴平台相关代码而不加条件编译
10. 生成 README、CHANGELOG、设计文档等，除非用户明确要求
11. 在代码中使用 emoji
12. 在选择器规则块 `{}` 内部写注释（声明行之间、行尾），或逐条给样式属性加注释 —— CSS 注释只允许写在选择器上方，且只写核心信息（为什么/适配意图）
13. 注释复述代码本身已经表达的信息（"一眼就看明白的"），只为凑注释而注释
14. 使用 `// ===== 标题 =====`、`// ==== 标题 ====` 等装饰性分隔线注释 —— 分组标题直接写普通 `// 标题`
15. 变量/函数/参数等标识符使用 `snake_case` 或首字母大写 —— 统一首字母小写驼峰（组件名 PascalCase 除外）
16. 新增页面根节点不绑 `:class="themeClass"` —— 小程序无法给 `page` 动态挂类名，漏绑该页切暗色不生效
17. 用 `.theme-dark .u-xxx` 这类外部类名覆盖 uview 组件样式 —— 组件样式隔离，小程序里完全不生效，改色走 8 节的两套来源
18. 使用 `up-tabbar` 或 `:deep(.u-tabbar){flex:none}` 补丁 —— 底部导航统一 `AppTabbar`
19. 提交前不跑 `npm run lint:style` —— stylelint 三红线静态拦截（禁裸 `var()`、禁 hex、自定义属性右值裸 `$`），退出码非 0 必须修复。永久豁免：`src/styles/**` 定义层、`uni.scss` 映射右值、chat 页 hljs 语法色板、UserAvatar 头像分类色板；页面私有色用 `// stylelint-disable color-no-hex` 块级豁免并注明原因（先例 login.vue 验证码按钮私有色），不得新增文件级豁免

## 6. 红线（违反必须返工）

- 引入新依赖未经用户同意
- 修改 `request.js` 的 601 跳转或 Authorization 注入逻辑
- 修改 `VITE_ENCRYPT_PRIVATE_KEY`
- 将密钥、token、明文密码写入代码或日志
- 中文项目出现英文界面文案或英文注释（专有名词除外）
- 绕过 request.js 直接调用 axios/uni.request
- 新增页面未注册到 `pages.json`

## 7. 参考文件导航（命中场景必须先 Read，再动手）

| 场景 | 动手前必须先 Read |
|------|----------|
| 新增页面/组件/API（即几乎一切移动端编码任务） | `references/templates.md` |
| 处理 H5 与微信小程序平台差异、加密、Vuex、环境变量 | `references/templates.md` |
| 配色、亮/暗主题、uview 组件改色、底部导航、输入框文字色 | `references/templates.md`（3.2 主题 + 第 8 节 uViewPlus） |

判断依据：拿不准是否命中时，一律按命中处理——先 Read 再写。

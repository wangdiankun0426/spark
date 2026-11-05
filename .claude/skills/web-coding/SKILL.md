---
name: web-coding
description: Spark 项目前端 Web（spark-ui，Vue3 PC 端）编码规范。新增或修改 spark-ui 的 src/**、vite.mcp.js、.env.*、index.html、新增页面、写 Vue 组件或对接后端接口时触发。提供目录约定、禁止项、必须项、已有能力复用与红线；完整代码模板与视觉风格细则在 references/ 下，生成代码前必须先 Read 加载，禁止凭摘要编造模板。
---

# spark-ui 前端（Vue3 PC 端）编码规范

## 1. 触发边界

**适用**：`spark-ui/` 目录下 `src/**`、`vite.config.js`、`.env.*`、`index.html` 的创建、修改。

**不适用**：纯文档、git 操作、`dist/`、`node_modules/`。

**强制加载规则（最高优先级）**：本文件只含规则摘要，**代码模板与视觉风格细则不在本文件内**。命中第 10 节导航表的任何场景时，必须先 Read 对应 references 文件，再动手写代码；禁止凭本文件摘要或记忆编造页面/组件模板或样式写法，违者按红线处理——返工。

## 2. 项目背景

- **类型**：Vue 3 PC 端星火云应用平台前端（含首页、AI 应用、知识库、知识图谱、流程中心、表单/流程设计器、后台管理）。
- **技术栈**：Vue `^3.3.4` + Vite `^4.4.5` + Element Plus `^2.3.12` + Vuex `^4.1.0` + Vue Router `^4.2.5`。
- **其他依赖**：axios `^1.6.7`、echarts `^5.5.0`、@kangc/v-md-editor、@vue-office/*、pdfjs-dist、@fullcalendar/*。
- **后端代理**：vite dev server 将 `/api` 代理到 `http://127.0.0.1:8000` 并去掉 `/api` 前缀。
- **响应约定**：后端返回 `{ code, message, data }`；`code===200` 成功，`code===601` 跳转 `/login`。
- **运行时变量**：后端基址 `process.env.BASE_HTTP_API`（由 Vite 注入 `.env` 的 `VITE_HTTP_BASE_API`）；加密密钥 `import.meta.env.VITE_ENCRYPT_PRIVATE_KEY`。
- env 文件只在 Vite 启动时读取一次，改动后必须重启 dev 服务；`vite.config.js` 的 `envBannerPlugin` 会在启动横幅打印实际 mode、接口基址与监听端口，dev 模式基址指向远程时输出黄色警告，排查接口环境问题先看该横幅。
- **管理员判定**：`utils/utils.js#isAdmin()` 检查 `userInfo.id === 101`，仅前端体验校验。

## 3. 目录与文件约定

| 类别 | 路径 | 命名 |
|---|---|---|
| 业务页面 | `src/pages/<模块>/[子模块/]<name>.vue` | camelCase.vue |
| 通用组件 | `src/components/<分组>/<name>.vue` | camelCase.vue |
| 图标组件 | `src/assets/icons/<name>.vue` | camelCase.vue |
| API 接口 | `src/api/<模块>/<name>.js` | camelCase.js |
| 路由模块 | `src/router/modules/<模块>.routes.js` | kebab-case.routes.js |
| Vuex 模块 | `src/store/modules/<name>.js` | camelCase.js |
| 工具函数 | `src/utils/<name>.js` | camelCase.js |
| 样式 | `src/styles/<name>.scss` | camelCase.scss |

业务模块：`home` / `chat` / `kb` / `kg` / `llm` / `flow` / `manage`（`manage` 下含 `system` / `log` / `form` / `kb` / `kg` / `llm` / `workflow` / `chat` 等后台管理子页）。

## 4. 禁止项（硬性）

1. 未经用户同意引入新的 UI 库、HTTP 库、状态管理库
2. 使用 Options API、`Vue.extend`、mixin、普通 `<script>` —— 业务组件必须 `<script setup>`
3. 在 `.vue` 中写非 `scoped` 的 `<style>`（`src/styles/*.scss` 全局文件除外）
4. 业务样式中硬编码颜色/字号/间距，或直接写 `var(--xxx)` 消费 CSS 变量 —— 三层架构：主题色只在 `theme.scss` / `element.scss` 的 `:root` 定义，`variables.scss` 做同名 `$` 映射，`.vue` / `.scss` 消费层一律引用 `variables.scss` 的 `$` 变量（如 `$app-text-secondary`、`$ds-brand`、`$shell-nav-text`），禁止直接写 `var(--app-text-secondary)`；不随主题变化的字面值也禁止写死 `#fff`，用 `$color-text-white` 等 token。豁免：组件私有动态 CSS 变量（如 `:style` 绑定的 `--z`，非主题色）、JS/echarts 画布场景（SCSS 不可达，用模块内色板常量）
    - **自定义属性右值必须插值**：给 CSS 自定义属性（`--xxx`）赋 SCSS 变量时写 `--el-tag-bg-color: #{$ds-info-bg};`，禁止裸写 `--el-tag-bg-color: $ds-info-bg;`——SCSS 对自定义属性右值按原样字符串处理、不求值，裸写会把 `$ds-info-bg` 字面输出到 CSS 导致失效。普通声明直接写 `color: $ds-brand;` 即可，仅自定义属性右值与 `calc()` 内需要 `#{}`
    - **stylelint 强制**：以上三条红线由 `spark-ui/.stylelintrc.cjs` 静态拦截，提交前跑 `npm run lint:style`，退出码非 0 必须修复。存量 hex 豁免台账已于 2026-11-05 清零，消费层违规一律走 token 化治理，不得新增文件级豁免；`src/styles/**` 定义层与 `src/assets/icons/**` SVG 资源为永久豁免。组件私有动态 CSS 变量（如 `--z`）用 `// stylelint-disable-next-line function-disallowed-list` 行级豁免并注明原因；mask 遮罩的不透明色用 `$mask-opaque` 而非行级豁免
5. 直接调用 `axios` —— 必须从 `@/api/request` 引入
6. 硬编码 localStorage key 字符串 —— 用 `utils/keyUtils.js#localStorageKey`
7. 修改 `VITE_ENCRYPT_PRIVATE_KEY`，或将密钥写入源码/日志
8. 在 console 输出 token、密码、明文、密钥等敏感信息
9. POST 接口用 `data` 传参（与后端约定相悖；例外：文件上传、大文本保存——拼 URL 后可能超 8KB 触发 431，如技能全文/表单 JSON 保存，必须 `data` + 后端 `@RequestBody`，先例 `api/form/form.js#saveFormJsonAPI`、`api/llm/skill.js`）
10. 为兼容旧逻辑新增 fallback、过渡层、shim、"如果不存在则"判断 —— 直接修改原代码
11. 为未发生场景预写 try/catch、错误边界、空值兜底 —— 仅在系统边界（用户输入、外部 API）做必要校验
12. 预先添加"为将来扩展预留"的抽象层、配置项、参数
13. 生成 README、CHANGELOG、设计文档等，除非用户明确要求
14. 在代码中使用 emoji
15. 改动 `request.js` 中 `code===601` 跳转 `/login` 的行为，除非用户明确要求
16. 业务 CRUD 的新增/修改编辑表单用 `el-dialog` —— 统一用左侧抽屉 `el-drawer direction="ltr"`（详情查看等非编辑场景除外）
17. 在选择器规则块 `{}` 内部写注释（声明行之间、行尾），或逐条给样式属性加注释 —— CSS 注释只允许写在选择器上方，且只写核心信息（为什么/适配意图），详则 5.1
18. 注释复述代码本身已经表达的信息（"一眼就看明白的"），只为凑注释而注释
19. 变量/函数/参数等标识符使用 `snake_case` 或首字母大写 —— 统一首字母小写驼峰（组件名 PascalCase 除外）
20. 使用 `// ===== 标题 =====`、`// ==== 标题 ====` 等装饰性分隔线注释 —— 分组标题直接写普通 `// 标题`

## 5. 必须项

1. 用户可见文案、注释、错误提示用中文（专有名词、品牌词除外）
2. 注释写在代码上方；公共函数加中文 JSDoc；`<style>` 内注释只写在选择器上方，规则块 `{}` 内不写 —— 详则 5.1
3. 组件结构 `<template>` → `<script setup>` → `<style scoped lang="scss">`
4. API 命名 `功能+API`，从 `@/api/request` 引入
5. GET/POST 都用 `params`（大文本保存等明确 body 场景除外，见禁止项 9）
6. 新增模块路由后在 `router/index.js` 注入 `layoutRoutes[0].children`；页面路由设 `meta.title`
7. 新增 Vuex 模块必须 `namespaced: true` 并在 `store/index.js` 注册
8. 内容页面顶层容器 `class="app-container"`
9. 敏感字段加密用 `utils/encryptUtil.js#des()`
10. 日期格式化用 `utils/dateUtil.js`
11. 路径用 `@/...` 别名（同目录工具类除外）
12. Element Plus 图标模板中直接用 `<el-icon><IconName /></el-icon>`；自定义图标组件从 `assets/icons/` 导入
13. 生成的 Vue/HTML 模板标签必须完整闭合（开始/结束一一对应，自闭合用 `/>`），避免漏 `>`、`</xxx>` 或嵌套错误
14. 代码标识符统一首字母小写驼峰（lowerCamelCase）：变量、函数、props、emits、事件处理函数、请求参数名等；组件名用 PascalCase；禁止 `snake_case`。日志/报错消息首词同样用驼峰，如 `console.log('receiverChange error', e)`

### 5.1 注释规范

**CSS 注释只写选择器顶部**：`<style>` / `.scss` 内注释只允许写在选择器（类）上方，用 `//` 写一行核心信息——为什么有这条规则、特殊适配意图（如覆盖三方组件的原因、平台兼容）。禁止在 `{}` 规则块内部、声明行之间或行尾写注释，禁止逐条给属性加注释；选择器与变量名写成自解释的（`.form-widget-box--dragover`、`$shadow-card`），普通样式块不写注释。

- 要写：`.el-popper.is-dark` 上方写 `// 深色气泡固定深底浅字，深色主题下 EP 默认文字跟随主题会导致不可读`
- 不写：规则块内的 `// 圆角 6px`、声明行尾注释、`// 卡片样式` 这类复述选择器的注释

**JS 与模板只写核心注释**：只写"一眼看不出来"的信息 —— 为什么这么做、隐含约束、踩坑规避。写"为什么"，不写"是什么"。**注释只写核心，禁止用括号补充细化说明**。

**禁止装饰性分隔线**：不写 `// ==================== 标题 ====================`、`// ====== 标题 ======` 这类等号横线，分组标题直接写 `// 标题`。

- 要写：`使用 .stop 修饰符来阻止事件冒泡 不然会出现触发前一个组件的handleSelectWidget 导致异常`、`这里对widget进行深拷贝，不然会出现推入新对象时却发现数组中新增的对象变成了之前修改过的对象的样子`、`dragCounter 归零后取消高亮`
- 不写：`// 拖拽进入画布`、`// 记录源下标`、`// 获取列表`、`// 关闭弹窗` —— 函数名已说明，删掉

**JSDoc 保留但精简**：公共函数按项目惯例保留 `/** ... */` + `@param` / `@returns`（API 文件必须），但描述行不能只把函数名翻译成中文；无参数无返回的简单函数可整块省略。

## 6. 已有能力（复用，禁止重复实现）

| 能力 | 路径 | 用途 |
|---|---|---|
| axios 实例 | `@/api/request` | 统一请求、Authorization、空值过滤、超时、错误提示、601 跳转 |
| DES 加密 | `@/utils/encryptUtil.js#des(str)` | 敏感字段加密 |
| 日期格式化 | `@/utils/dateUtil.js` | `getCurrentDate`、`formatDate` |
| 存储 key | `@/utils/keyUtils.js#localStorageKey` | `SPARK_USER_TOKEN`、`SPARK_USER_INFO` |
| 文档类型 | `@/utils/documentUtil.js` | `getDocumentCategory`、文件后缀映射 |
| 管理员判断 | `@/utils/utils.js#isAdmin()` | `userInfo.id === 101` |
| 设计 token | `@/styles/variables.scss` | SCSS 侧唯一变量入口：编译期参数（颜色字面值、圆角、阴影、间距、过渡、侧栏宽度）+ 全部运行时主题变量的同名 `$` 映射（`$app-*` / `$ds-*` / `$shell-*` / `$el-*`），消费层只准引用本文件 |
| 主题变量 | `@/styles/theme.scss` | `--app-*` / `--shell-*` / `--nav-*` / `--sidebar-*` 的唯一定义处，亮/暗两套与公共主题底座（`html.dark` / `html.theme-light`），只放定义不放消费 |
| Element 覆盖 | `@/styles/element.scss` | `--el-*` / `--ds-*` 的唯一定义处与 EP 组件选择器覆盖（亮暗两套），el-* 覆盖只准放这里；选择器内消费一律用 `$` 变量 |
| 应用外壳 | `@/styles/shell.scss` | 顶部导航栏与左侧菜单栏（`.app-nav` / `.app-sidebar`）全局结构样式，由 `--shell-*` token 驱动 |
| 全局基础样式 | `@/styles/main.scss` | reset/排版、滚动条、`app-container`、第三方组件（v-md-editor 等）的 `html.dark` 深色覆盖 |

> 独立 `.scss` 文件若被 `.vue` 以 `@use` 引入（如 `monitor.scss`、`chatMarkdown.scss`），文件首行必须显式 `@use "@/styles/variables" as *;`——vite 的 variables 全局注入只作用于样式入口（.vue 的 style 块、main.js 直接 import 的 scss），@use 子模块由 Sass 编译器直接读取、拿不到注入，漏写会报 Undefined variable。
| Markdown 编辑/预览 | `@kangc/v-md-editor` | 已在 `main.js` 全局注册 |
| 图表 | `echarts` | 首页/统计图表 |

## 7. 响应处理与安全

`request.js` 已统一处理：非 200 弹 `ElMessage.warning(message)`；`601` 跳 `/login`；`blob` 响应（文件下载）直接返回；`params` 空值自动过滤。业务代码判断 `res.code === 200` 取 `res.data` 即可，失败无需重复提示。

- 加密密钥来自 `import.meta.env.VITE_ENCRYPT_PRIVATE_KEY`，禁止硬编码到源码
- token 由 `request.js` 自动注入 `Authorization`，禁止手工拼接
- 用户输入在表单校验层处理；尽量避免 `v-html` 渲染用户内容
- 后台管理路由必须挂 `/manage` 下，由前置守卫统一校验 `isAdmin()`

## 8. 视觉风格基调

**纯白底、细描边淡投影、贴边铺满、无多余留白**；卡片复用 `element.scss` 的 `.el-card` 配方不另造；颜色/间距/圆角/阴影全走 `variables.scss` 的 `$` 变量。上述"纯白底"是浅色主题的基准，深色主题由 `theme.scss` / `element.scss` 定义、经 `variables.scss` 同名映射自动接管，消费层只写 `$` 变量即自动跟随，禁止直接写 `var(--xxx)` 或写死色值。涉及 UI 样式与布局时必读 `references/visual-style.md`。

## 9. 红线（违反必须返工）

- 引入新依赖未经用户同意
- 改动 `request.js` 拦截器逻辑（除非用户明确要求）
- 修改 `VITE_ENCRYPT_PRIVATE_KEY`
- 将密钥、token、明文密码写入代码或日志
- 中文项目出现英文界面文案或英文注释（专有名词除外）
- POST 接口用 `data` 传参（文件上传、大文本保存等明确 body 场景除外，见禁止项 9）
- 修改 token 存储位置或 key 名称
- 给"未来扩展"添加抽象层或配置项

## 10. 参考文件导航（命中场景必须先 Read，再动手）

| 场景 | 动手前必须先 Read |
|------|----------|
| 新增页面/组件/API/路由/Vuex 模块（即几乎一切前端编码任务） | `references/templates.md` |
| 涉及页面样式、布局、卡片、配色、留白、亮/暗主题 | `references/visual-style.md` |
| 交付前自查视觉质量 | `references/visual-style.md`（交付前自查清单） |

判断依据：拿不准是否命中时，一律按命中处理——先 Read 再写。

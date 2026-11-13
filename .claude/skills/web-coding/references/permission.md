# 前端鉴权与动态路由（spark-ui）

配套规范：`../SKILL.md`。本文件是 PC 端鉴权与动态路由逻辑的唯一事实来源，涉及身份判定、动态路由、按钮权限、登录态的编码必须先 Read 本文件。

## 1. 三身份与权限来源

- 后端 `/auth/session` 返回 `{ userInfo, routers, permissions }`：routers 是菜单树，permissions 是操作权限串集合。
- 三身份前端判定走 `@/utils/permissionUtil.js`，**仅体验层校验，后端强制**：
  - `isSysAdmin()`：`userInfo.accountType === 2`。
  - `isOrgAdmin()`：`(userInfo.roleType & 2) === 2`。
  - `checkPermi(perm)`：`permissions.includes('*:*:*') || permissions.includes(perm)`。
- permissions 含 `*:*:*` 即 sysAdmin 通配。
- 身份判定统一走 permissionUtil，**禁止用 `userInfo.id === 101` 等硬编码判定管理员**。

## 2. permission store

`@/store/modules/permission.js`，`namespaced: true`。

- state：`permissions` / `menuTree` / `routes` / `routesGenerated`。
- `generateRoutes({ routers, permissions })`：commit 权限串与菜单树，`buildRoutes` 生成路由，置 `routesGenerated=true`。
- `componentMap`：`import.meta.glob('@/pages/**/*.vue')` + `('@/views/**/*.vue')`，键归一化为相对 src 无后缀路径，与后端菜单 `component` 串一致。
- `buildRoutes`：模块转路由，`collectLeafMenus` 把分组目录拍平为平铺子路由，`resolveComponent` 未命中告警跳过，模块组件缺失跳过防白屏；桌面端 `module.home` 用 `@desktop/pages/homeContainer.vue`。
- getters：`defaultPath`（首个含 redirect 的模块重定向目标）、`hasPermi(perm)`、`permissions`、`menuTree`、`routes`、`routesGenerated`。
- `reset`：清空全部 state，登出与切换身份时调用。

## 3. 路由守卫 router/index.js

- 桌面端 Hash 模式，Web 端 History 模式。
- 免登录白名单 `['/login', '/oauthLogin', '/register', '/agreement']` **先行放行**，登录页由此挂载清残留凭证，破解 601 跳转后的循环。
- 无 token → `/login`。
- `!routesGenerated` 时：`getSessionAPI` → 失败 `logout` + `/login` → `setUserInfo` → `generateRoutes` → `clearDynamicRoutes` → `addRoute('index', route)` → `next({ path, query, hash, replace: true })`。
  - `clearDynamicRoutes` 按 `meta.code` `removeRoute`，清上一身份残留动态路由，防切换租户或重登后旧授权路由直达。
  - 按路径重解析而非 spread to，避免刷新时 catchAll 的 noFound 名导致复匹到 404。
- `to.path === '/'` → `defaultPath || '/noPermission'`，落地页菜单驱动，sysAdmin 无首页模块时自然落到站点管理。
- `to.name === 'noFound'` → `/noPermission`，已登录但目标不在授权路由内直达无权页。
- **业务页面路由由后端菜单种子驱动**，不手工注入 `layoutRoutes[0].children`；静态 `common.routes` / `layout.routes` 保留。新增业务页面须建菜单种子，`component` 串与 componentMap 键一致。

## 4. 操作权限 v-hasPermi

- 指令 `@/directive/hasPermi.js`：`mounted` 期 `perms.some(perm => checkPermi(perm))`，不命中且存在父节点则 `removeChild`；`installPermissionDirectives(app)` 注册。
- 用法 `v-hasPermi="['workflow:def:create']"`，命中任一权限串即保留元素。
- `InfoCard` 的 `cardActions` 按 `perms` 渲染期过滤。
- **按钮权限范围**：仅 platform0 三模块（AI 管理、业务管理、组织管理）挂按钮权限；公共模块（platform2）与站点模块（platform1）**不限制**。
- v-hasPermi 是体验层隐藏，后端 `@RequiresPermi` 才是强制拦截，二者权限串须一致。

```vue
<el-button v-hasPermi="['workflow:def:create']" type="primary" @click="openCreate">新增</el-button>
```

## 5. 登录态与请求

- `@/store/modules/user.js`：token 即后端 sessionId，`login` 成功 `SET_TOKEN(res.data.sessionId)`；getters `getUserInfo` / `getToken`；`logout` 清 TOKEN + USERINFO。
- `request.js` 自动注入 `Authorization` = token；`code === 601` 跳 `/login`（**红线禁改**）；非 200 弹 `ElMessage.warning(message)`。
- 登录页 `onMounted` 调 `user/logout` + `permission/reset` 清残留凭证（`login.vue` / `oauthLogin.vue`），防 601 跳登录后被守卫误判已登录而循环。
- 密码 des 一次性密钥：`getEncryptKeyAPI` 返回 keyId + key，`des(password, key)` 加密。
- loginPlatform：PC 浏览器=1，PC 客户端=3（`isDesktop`），企微 PC=4。

## 6. 编码规则与禁止项

- 身份与权限判定走 permissionUtil + store getters，禁手写 `id === 101`。
- 新增按钮挂 `v-hasPermi`，权限串与后端按钮种子逐字一致。
- 不改 `request.js` 的 601 跳转；不改 `VITE_ENCRYPT_PRIVATE_KEY`；token、密钥禁入 console。
- 业务路由由菜单种子驱动，`component` 串与 componentMap 键一致；切换身份须 `clearDynamicRoutes` + `permission/reset`。
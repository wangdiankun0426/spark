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

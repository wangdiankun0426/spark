---
name: mobile-coding
description: Spark 项目前端移动端（spark-app，uni-app H5 / 微信小程序）编码规范。新增或修改 spark-app 的 src/**、pages.json、manifest.json、新增页面、对接后端接口时触发。提供项目背景、API 约定、禁止项与红线；完整页面/API 模板与平台适配细则在 references/ 下，生成代码前必须先 Read 加载，禁止凭摘要编造模板。
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

## 3. API 约定（要点）

- 统一 `import request from '@/api/request'`（axios + UniAdapter，自动注入 `Authorization`，空值自动过滤）
- 命名 `功能+API`；GET/POST 都用 `params`（文件上传等明确 body 场景除外）；每个函数加中文 JSDoc
- `code===200` 成功取数据；`601` 自动跳登录；其他 code 由 request.js 统一 toast，业务代码不重复提示
- 密码等敏感字段提交前用 `@/utils/encryptUtil` 的 `des()` 加密，密钥来自 `import.meta.env.VITE_ENCRYPT_PRIVATE_KEY`

## 4. 页面与平台适配（要点）

- 页面结构 `<template>` → `<script setup>` → `<style scoped lang="scss">`，样式单位用 `rpx`
- 新增页面必须注册到 `pages.json` 的 `pages` 数组，统一 `"navigationStyle": "custom"`
- H5 与微信小程序差异用条件编译 `#ifdef H5` / `#ifdef MP-WEIXIN` 处理
- 注释在代码上方，只写一眼看不出来的核心信息，禁止用括号补充细化说明；`<style>` / `.scss` 内不写任何注释
- 代码标识符统一首字母小写驼峰（lowerCamelCase）：变量、函数、事件处理函数、请求参数名等；组件名用 PascalCase；禁止 `snake_case`。日志/报错消息首词同样用驼峰，如 `console.log('receiverChange error', e)`
- 完整模板与细则见 `references/templates.md`

## 5. 禁止项

1. 未经用户同意引入新的 UI 库、HTTP 库、状态管理库
2. 使用 Options API / mixin / 普通 `<script>` —— 业务页面用 `<script setup>`
3. 在 `.vue` 中写非 `scoped` 的 `<style>`（全局文件除外）
4. 硬编码颜色/字号/间距 —— 用全局样式变量或主题
5. 直接手工拼接 `Authorization` / 硬编码 token
6. 将 `VITE_ENCRYPT_PRIVATE_KEY` 写入源码/日志
7. 在 console 输出 token、密码、明文、密钥等敏感信息
8. POST 用 `data` 传参（与后端约定相悖，文件上传等场景除外）
9. 在 H5/小程序间复制粘贴平台相关代码而不加条件编译
10. 生成 README、CHANGELOG、设计文档等，除非用户明确要求
11. 在代码中使用 emoji
12. 在 `<style>` / `.scss` 内写任何注释 —— `//`、`/* */` 一律不写，CSS 不注释
13. 注释复述代码本身已经表达的信息（"一眼就看明白的"），只为凑注释而注释
14. 变量/函数/参数等标识符使用 `snake_case` 或首字母大写 —— 统一首字母小写驼峰（组件名 PascalCase 除外）

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

判断依据：拿不准是否命中时，一律按命中处理——先 Read 再写。

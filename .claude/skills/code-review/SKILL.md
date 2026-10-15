---
name: code-review
description: Spark 项目代码复审（严格格式审计）。当用户要求复审、审查、检查生成的或已有的代码是否符合项目严格格式规范时触发。覆盖后端 Java/MyBatis、前端 Web spark-ui、移动端 spark-app 三类代码；复审前必须先 Read 各编码规范 skill 的规范文件（单一事实来源），逐条上报违规并分级（BLOCKER/MAJOR/MINOR），输出 file:line 定位与修复建议。
---

# Spark 项目代码复审（格式审计）

## 1. 目的

严格按项目编码规范复审代码。**格式优先**：即使代码能编译/能运行，只要违反格式规范也必须上报。按项目约定，复审过程**不执行编译/构建**。

## 2. 检查依据（单一事实来源，强制前置加载）

本 skill 不内联检查清单。复审某类代码前，**必须先 Read 对应规范文件**，以其为唯一检查依据；未读规范文件直接凭记忆复审的，视为无效复审，必须重来：

| 分类 | 代码路径 | 复审前必读规范文件 |
|------|----------|--------------------|
| 后端 Java / MyBatis XML | 仓库根各 `spark-*` 模块 `src/**` | `.claude/skills/backend-coding/SKILL.md` 及其 `references/` 全部 |
| 前端 Web | `spark-ui/src/**`、`vite.config.js`、`.env.*`、`index.html` | `.claude/skills/web-coding/SKILL.md` 及其 `references/` 全部 |
| 移动端 | `spark-app/src/**` | `.claude/skills/mobile-coding/SKILL.md` 及其 `references/` 全部 |

## 3. 复审流程

1. 确定范围：用户改动的文件或用户指定的文件
2. 按第 2 节表格 Read 对应分类的规范文件（SKILL.md + references/），以其为检查清单
3. 逐个读取被审文件，逐项核对
4. 每条发现按 `文件:行号 — 违反规则 — 问题描述 — 修复建议` 上报
5. 分级：
   - **BLOCKER**：红线违规（安全、禁用模式、各规范 skill 的红线章节）—— 必须修复后才能交付
   - **MAJOR**：强制规范未遵守（命名、结构、注释语言、返回格式、禁止项）
   - **MINOR**：样式细节（空行、空格、标点）
6. 结尾输出汇总表（各级数量）与按优先级排序的修复清单
7. **只审不改**：默认不修改代码，用户要求时才应用修复

## 4. 输出格式

逐文件上报，格式：

```text
📄 <文件路径>
- [BLOCKER] <行号>: <违反规则> — <问题描述> — <修复建议>
- [MAJOR] <行号>: <违反规则> — <问题描述> — <修复建议>
- [MINOR] <行号>: <违反规则> — <问题描述> — <修复建议>
```

结尾汇总表：

| 严重级别 | 数量 |
|---|---|
| BLOCKER | n |
| MAJOR | n |
| MINOR | n |

随后列出按优先级排列的修复清单。复审默认不改代码，用户要求时才应用修复。

### 输出示例

```text
📄 spark-web/.../controller/xxx/XxxController.java
- [MAJOR] 36: Controller 方法应为 private — 当前为 public — 改为 private

📄 spark-ui/src/pages/xxx/index.vue
- [BLOCKER] 24: POST 接口用 data 传参 — 与后端 params 约定相悖 — 改为 params
- [MINOR] 88: 注释写在行尾 — 应独立成行在代码上方 — 移动注释
```

| 严重级别 | 数量 |
|---|---|
| BLOCKER | 1 |
| MAJOR | 1 |
| MINOR | 1 |

优先修复顺序：BLOCKER（密钥/601/POST data 等）→ MAJOR（命名/注释语言/结构）→ MINOR（样式）。

## 5. 红线（恒为 BLOCKER）

- 密钥/token/明文密码写入代码或日志
- 未经用户同意引入新依赖
- 中文项目出现英文注释或英文界面文案（专有名词除外）
- 前端 POST 用 `data` 传参（文件上传、大文本保存等明确 body 场景除外，见 web-coding 禁止项 9）
- 直接 axios / 手工拼接 token / 硬编码 localStorage key
- 后端跨层或反向调用；业务层直接 `insert/updateById/deleteById`
- 修改 `VITE_ENCRYPT_PRIVATE_KEY` 或 `request.js` 的 601 跳转
- 改动当前功能之外的无关代码

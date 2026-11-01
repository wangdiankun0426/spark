# 星火云应用平台

> 企业级 AI 应用平台 · 低代码平台

星火云应用平台是一套**企业级 AI 协同办公平台**，以「即时通信、自定义表单、自定义流程、AI Agent、AI 工作流（Workflow）」五大能力为核心，融合 RAG 知识库、知识图谱、大模型接入等 AI 基础设施，帮助企业**低代码、可视化**地快速构建和落地各类业务系统。

项目采用 Monorepo 架构，包含后端服务、PC 前端（浏览器端 + Tauri 2 桌面端）和移动端（H5 + 微信小程序），一套代码多端运行，开箱即用。

---

## 系统体验

| 端           | 地址                                                              |
|-------------|-----------------------------------------------------------------|
| PC 端        | https://spark.evancloud.top:7101                                |
| Windows 桌面端 | https://spark.evancloud.top:7101/bundle/sparkcloud_0.0.11_x64-setup.exe |
| H5 移动端      | https://spark.evancloud.top:7102                                |
| 微信小程序       | <img src="https://spark.evancloud.top:7101/bundle/wechatUrl.png" alt="微信小程序" width="160" height="160"> |

**测试账号**

| 账号       | 密码 |
|----------|------|
| zhangsan | 123456 |
| wangwu   | 123456 |

---

## 系统预览

### PC 浏览器端

**登录页**

<img src="https://spark.evancloud.top:7101/bundle/sysImg/pc-login.png" alt="PC 登录页" width="100%">

| 工作台 | AI 对话 |
|---|---|
| <img src="https://spark.evancloud.top:7101/bundle/sysImg/pc-workbench.png" alt="工作台" width="720"> | <img src="https://spark.evancloud.top:7101/bundle/sysImg/pc-aichat.png" alt="AI 对话" width="720"> |

| 知识库 | 知识图谱 |
|---|---|
| <img src="https://spark.evancloud.top:7101/bundle/sysImg/pc-knowledge.png" alt="知识库" width="720"> | <img src="https://spark.evancloud.top:7101/bundle/sysImg/pc-graph.png" alt="知识图谱" width="720"> |

| AI 工作流设计器 | 自定义流程设计器 |
|---|---|
| <img src="https://spark.evancloud.top:7101/bundle/sysImg/pc-workflow.png" alt="AI 工作流设计器" width="720"> | <img src="https://spark.evancloud.top:7101/bundle/sysImg/pc-flow.png" alt="流程设计器" width="720"> |

| 站点监控 · 服务器 | 站点监控 · 接口请求 |
|---|---|
| <img src="https://spark.evancloud.top:7101/bundle/sysImg/pc-monitor-server.png" alt="服务器监控" width="720"> | <img src="https://spark.evancloud.top:7101/bundle/sysImg/pc-monitor-api.png" alt="接口请求监控" width="720"> |

**深色主题**

<img src="https://spark.evancloud.top:7101/bundle/sysImg/pc-dark.png" alt="深色主题" width="100%">

### Windows 桌面端（Tauri 2）

<p float="left">
  <img src="https://spark.evancloud.top:7101/bundle/sysImg/desktop-home.png" alt="桌面端首页" width="48%">
</p>

### H5 移动端

<p float="left">
  <img src="https://spark.evancloud.top:7101/bundle/sysImg/h5-login.png" alt="H5 登录页" width="30%">
  <img src="https://spark.evancloud.top:7101/bundle/sysImg/h5-workbench.png" alt="H5 工作台" width="30%">
  <img src="https://spark.evancloud.top:7101/bundle/sysImg/h5-chat.png" alt="H5 即时通信" width="30%">
</p>

### 微信小程序

<p float="left">
  <img src="https://spark.evancloud.top:7101/bundle/sysImg/mp-workbench.png" alt="小程序工作台" width="24%">
  <img src="https://spark.evancloud.top:7101/bundle/sysImg/mp-chat.png" alt="小程序消息" width="24%">
</p>

---

## 1. 平台定位

**双平台定位：**

- **企业级 AI 应用平台**：内置 AI 对话、RAG 智能问答、AI Agent、知识图谱、AI 工作流编排等能力，让 AI 能力融入企业日常业务。
- **低代码平台**：通过可视化表单设计器、流程设计器、工作流设计器，业务人员无需编写代码即可搭建业务应用，快速实现信息化。

---

## 2. 核心能力

| 能力 | 说明 |
|------|------|
| 💬 **即时通信** | 基于 WebSocket 的实时沟通：一对一/群组会话、消息收发、在线状态、会话空间管理，保障沟通安全与私密 |
| 📝 **自定义表单** | 可视化**表单设计器**：拖拽字段、布局排版、版本管理，表单即配置、即发布、即收集数据 |
| 🔄 **自定义流程** | 可视化**流程设计器** + Flowable 流程引擎：节点编排、审批流转（会签/或签/驳回/转办/加签/抄送）、条件分支、流程监控与历史追溯 |
| 🤖 **AI Agent** | 可视化配置与管理系统级 AI 智能体，接入大模型、技能库、知识库、MCP 工具，实现自主规划与执行 |
| 🧩 **AI 工作流（Workflow）** | 可视化 **DAG 工作流设计器**：拖拽 LLM、知识库/图谱检索、HTTP、代码执行、人工审核等节点编排 AI 能力链，可运行、可追踪 |
| 📚 **RAG 知识库** | 企业文档管理中心：上传/预览/全文检索，基于 RAG 实现文档分块、向量化、多路召回与智能问答 |
| 🕸️ **知识图谱** | 基于 Neo4j 构建企业知识图谱，实体关系抽取与可视化展示，洞察知识关联 |
| 🏢 **多租户** | 一套平台服务多个组织：租户开通与有效期、租户用户管理、默认租户一键切换，数据权限整体隔离 |
| 📊 **站点监控** | 自研轻量内嵌监控（无需 Prometheus）：服务器/JVM、接口请求、Redis、RabbitMQ 分钟级采集与可视化，含慢 SQL Top 排查 |
| 🎨 **双主题界面** | 全站浅色/深色双主题，品牌深蓝灰配色，图表、Markdown、弹层全量适配，支持浏览器与 Windows 桌面端 |

---

## 3. 项目结构

```
spark/
├── spark-common/      # 公共模块：基类、枚举、工具类、常量、Bean
├── spark-dao/         # 数据访问层：DAO 接口 + MyBatis Mapper XML
├── spark-config/      # 配置模块：Redis、MQ、ES、AOP、短信等
├── spark-manage/      # 系统管理：用户/部门/角色/日志/登录/通知、MQ 消费者
├── spark-web/         # Web 层：Controller、过滤器、拦截器、异常处理、启动入口
├── spark-chat/        # 即时通讯（WebSocket）
├── spark-flow/        # 流程引擎（Flowable）
├── spark-form/        # 表单引擎
├── spark-kb/          # RAG 知识库
├── spark-llm/         # 大模型接入、Agent、MCP、向量/排序模型
├── spark-kg/          # 知识图谱
├── spark-workflow/    # AI 工作流（DAG 编排、执行引擎）
├── spark-home/        # 用户工作台：首页看板聚合（待办、推荐、可申请模板、模型用量趋势）
├── spark-task/        # 定时/异步任务（任务实例框架 ITaskTypeHandler）
├── spark-monitor/     # 站点监控（主机/JVM/接口/Redis/MQ 指标采集、快照清理）
├── spark-dms/         # 文档管理（上传、预览、在线编辑、分块）
├── spark-ui/          # PC 前端（Vue 3 浏览器端 + Tauri 2 桌面端）
├── spark-app/         # 移动端（uniapp：H5 + 微信小程序）
├── sql/               # 数据库初始化脚本
└── pom.xml            # Maven 父 POM
```

---

## 4. 技术栈

### 后端

| 技术             | 版本      | 说明              |
|----------------|---------|-----------------|
| JDK            | 17      | 运行环境            |
| SpringBoot     | 3.4.0   | 后端框架            |
| Maven          | 3.9.9   | 构建工具            |
| MySQL          | 8.0.31  | 关系型数据库          |
| Redis          | 7.2.0   | 缓存              |
| RabbitMQ       | 3.11    | 消息队列            |
| Elasticsearch  | 8.1.3   | 全文搜索引擎 / 向量检索  |
| LangChain4j    | 1.4     | 大模型开发框架         |
| MCP SDK        | 0.14.1  | MCP 工具服务接入       |
| Flowable       | 7.2.0   | 流程引擎            |
| Neo4j          | 5.27.0  | 图数据库（知识图谱）      |
| Druid          | 1.2.20  | 数据库连接池 / SQL 监控 |
| OSHI           | 6.6.5   | 主机与 JVM 硬件指标采集  |
| WebSocket      | Spring  | 即时通信长连接         |

### PC 前端（spark-ui）

| 技术              | 版本      | 说明              |
|-----------------|---------|-----------------|
| Vue             | 3.3.4   | 前端框架            |
| Vite            | 4.4.5   | 构建工具            |
| Element Plus    | 2.3.12  | UI 组件库          |
| Vue Router      | 4.2.5   | 路由              |
| Vuex            | 4.1.0   | 状态管理            |
| ECharts         | 5.5.0   | 数据可视化           |
| Tauri           | 2       | 桌面端框架（仅 Windows） |
| Node.js         | 22.20.0 | 运行环境            |

### 移动端（spark-app）

| 技术           | 版本      | 说明              |
|--------------|---------|-----------------|
| uniapp        | 3.0.0   | 跨平台框架           |
| Vue           | 3.4.23  | 前端框架            |
| Vite          | 4.3.5   | 构建工具            |
| uViewPlus     | 3.3.21  | UI 组件库          |
| Vuex          | 4.1.0   | 状态管理            |

---

## 5. 功能模块

### 5.1 即时通信（spark-chat）
基于 WebSocket 的实时通讯能力：会话空间管理、一对一/群组消息收发、消息状态、用户关系维护。沟通数据安全可控，适用于企业内部协同场景。

### 5.2 自定义表单（spark-form）
提供可视化**表单设计器**：拖拽表单字段、配置校验与布局、版本管理；表单模板即建即用，提交的数据统一沉淀为结构化对象与取值，可被流程/工作流引用。

### 5.3 自定义流程（spark-flow）
提供可视化**流程设计器**，基于 Flowable 流程引擎：节点编排、审批人配置（发起人/指定用户/部门/角色/表单字段）、分支条件、通知提醒、流程实例跟踪与历史监控，覆盖请假、报销等典型审批场景。

### 5.4 AI Agent 与大模型生态（spark-llm）
- **模型管理 / 模型市场**：多厂商大模型统一接入与配置
- **智能体（Agent）**：绑定大模型、挂载知识库/知识图谱与 MCP 工具，设定系统提示词，可被对话、问答场景复用
- **技能库（Skill）**：技能文件包（LLM Skill）管理，版本化沉淀可复用提示能力
- **MCP 服务**：MCP Server 配置与个人令牌维护，为 Agent/工作流提供外部工具
- **模型用量**：按模型、业务场景、日期统计调用次数与 Token 消耗（输入/输出），趋势图表支撑成本观测

### 5.5 AI 工作流 Workflow（spark-workflow）
可视化 **DAG 工作流设计器**：拖拽节点、连线编排 AI 能力链，支持版本管理、开启/关闭、运行实例与节点级执行记录。节点类型覆盖：LLM 对话、知识库检索、图谱检索、文档解析、HTTP 请求、代码执行、变量操作、人工审核、Agent 任务、子流程等；节点编辑画布保持类 IDE 的浅色工作区，属性面板随主题适配。

### 5.6 知识库（spark-kb）· RAG
企业级文档管理中心：文件上传/下载、在线预览（Word/Excel/PPT/PDF）、全文检索；基于 RAG 实现文档分块、向量化存储、多链路召回，为 AI 助手与 Agent 提供企业知识支撑。

### 5.7 知识图谱（spark-kg）
基于 Neo4j 构建企业知识图谱：实体关系抽取、图谱可视化展示，帮助发现知识间的关联，支持语义化智能检索。

### 5.8 站点监控（spark-monitor）
自研轻量内嵌监控，分钟级定时采集、快照自动过期清理，无需引入 Prometheus/Grafana，仅系统管理员可见：

- **服务器监控**：多节点在线状态、CPU、物理内存/JVM 堆与非堆、线程（阻塞/死锁）、已加载类、磁盘分区、网络吞吐、JVM 运行时长，主机/Druid 慢 SQL 快照
- **接口请求**：QPS、平均/最大响应耗时、2xx~5xx 状态码分布、错误率；URI 维度全量聚合（分页、按调用次数倒排）；节点慢 SQL Top20
- **缓存监控**：Redis 内存与 RSS、命中率趋势、命令统计
- **消息队列**：RabbitMQ 队列消息积压量、消费者数、无消费者/堆积异常告警

### 5.9 多租户（spark-manage）
- 租户开通、账号配额、有效期（截止时间）管理
- 租户下用户加入/移除、设置/取消租户管理员
- 用户可加入多个租户，左下角一键切换默认租户，切换后菜单、数据权限整体生效

### 5.10 数据看板（spark-home）
工作台聚合展示：模型调用量与 Token 趋势、流程待办/已办/申请分布、通知公告、员工工作日历、待办事项、快捷入口及知识库/知识图谱统计卡片，图表随主题自适应。

### 5.11 后台管理
按职能划分四个管理域：

- **工作台**：待办/已办/我的申请/抄送、AI 对话、智能体、AI 工作流、通讯录、流程发起
- **业务管理**：流程模板、流程实例、表单/任务模板
- **组织管理**：用户、部门、角色、登录日志、操作日志、消息通知
- **AI 管理**：模型/模型市场、技能库、MCP 服务、知识库、知识图谱、AI 工作流、模型用量
- **站点管理**：租户管理、租户配置、服务器监控、缓存监控、消息队列、接口请求

### 5.12 界面与交互体验
- **全局双主题**：浅色与品牌深蓝灰深色主题一键切换，覆盖页面、卡片、表格、表单、抽屉、弹窗、下拉、分页、滚动条；ECharts 图表与 v-md-editor 编辑器/预览同步适配；登录/注册/错误等独立页保持浅色
- **品牌化设计**：品牌 Lockup 标识、登录页六大能力展示、圆角胶囊菜单选中态、统一卡片描边与投影体系
- **全局弹窗拖拽**：el-dialog 与 ElMessageBox 标题栏按住可拖动、双击复位
- **桌面端适配**：Tauri 2 打包 Windows 安装包，Hash 路由兼容应用内协议，窗口内打开设计器/文档页面；支持前端资源热更新，界面与逻辑改动无需重装客户端

---

## 6. 快速开始

### 6.1 后端

```bash
# 1. 初始化数据库（执行 sql/ 目录下的脚本）

# 2. 修改配置（数据库、Redis、RabbitMQ、Elasticsearch 等）
#    编辑 spark-web/src/main/resources/application.yml

# 3. 编译运行
mvn clean install -DskipTests
cd spark-web
mvn spring-boot:run
```

### 6.2 PC 前端

```bash
cd spark-ui

# 安装依赖
npm install

# 浏览器端开发（默认监听 127.0.0.1:80）
npm run dev:web

# 浏览器端构建
npm run build:web

# Tauri 桌面端开发（需安装 Rust 工具链，自动启动 Vite 开发服务器）
npm run dev:win

# Tauri 桌面端打包（仅支持 Windows，产出 NSIS 安装包）
npm run pack:win
```

### 6.3 桌面端热更新

前端资源支持在线热更新，改 `src/**` 或 `src-tauri/desktop/**` 无需重装客户端；`desktopPet/`、Rust 代码与 `tauri.conf.json` 仍需重新打包。

```bash
cd spark-ui

# 提升版本号（package.json 为唯一版本源）
npm pkg set version=0.0.12

# 构建前端，产出 release/desktop/{dist-0.0.12.zip, latest.json}
npm run build:web
npm run pack:update -- "本次更新说明"

# 上传到服务器静态目录，客户端启动时静默检查，也可在「设置 → 系统更新」手动触发
# scp release/desktop/dist-0.0.12.zip release/desktop/latest.json <server>:/opt/spark/bundle/desktop/
```

清单地址由 `.env.prod` 的 `VITE_HOT_UPDATE_URL` 指定，服务器需直接返回 `latest.json` 本体（不可回落到 `index.html`）。升级到首个热更版本后会掉一次登录态（窗口源由 `tauri://localhost` 变为 `http://app.localhost`）。

### 6.4 移动端

```bash
cd spark-app

# 安装依赖
npm install

# H5 开发
npm run dev:h5

# H5 构建
npm run build:h5

# 微信小程序开发
npm run dev:mp-weixin

# 微信小程序构建
npm run build:mp-weixin
```

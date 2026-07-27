# 星火云AI应用平台

---

## 1. 简介

星火云AI应用平台是一套企业级 AI 协同办公管理系统，集成了 RAG 知识库、流程引擎、AI Agent、知识图谱、即时通讯等核心能力。
项目采用 Monorepo 架构，同时包含后端服务、PC 前端（浏览器端 + 桌面端）和移动端（H5 + 微信小程序），旨在实现企业级管理系统的快速开发和部署。

### 项目结构

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
├── spark-llm/         # 大模型接入、MCP、向量/排序模型
├── spark-kg/          # 知识图谱
├── spark-task/        # 定时/异步任务
├── spark-ui/          # PC 前端（Vue 3 浏览器端 + Electron 桌面端）
├── spark-app/         # 移动端（uniapp：H5 + 微信小程序）
├── sql/               # 数据库初始化脚本
└── pom.xml            # Maven 父 POM
```

---

## 2. 技术栈

### 后端

| 技术             | 版本      | 说明              |
|----------------|---------|-----------------|
| JDK            | 17      | 运行环境            |
| SpringBoot     | 3.4.0   | 后端框架            |
| Maven          | 3.9.9   | 构建工具            |
| MySQL          | 8.0.31  | 关系型数据库          |
| Redis          | 7.2.0   | 缓存              |
| RabbitMQ       | 3.11    | 消息队列            |
| Elasticsearch  | 8.1.3   | 全文搜索引擎          |
| LangChain4j    | 1.4     | 大模型开发框架         |
| Flowable       | 7.2.0   | 流程引擎            |
| Neo4j          | 5.27.0  | 图数据库（知识图谱）      |

### PC 前端（spark-ui）

| 技术              | 版本      | 说明              |
|-----------------|---------|-----------------|
| Vue             | 3.3.4   | 前端框架            |
| Vite            | 4.4.5   | 构建工具            |
| Element Plus    | 2.3.12  | UI 组件库          |
| Vue Router      | 4.2.5   | 路由              |
| Vuex            | 4.1.0   | 状态管理            |
| ECharts         | 5.5.0   | 数据可视化           |
| Electron        | 43.2.0  | 桌面端框架           |
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

## 3. 功能模块

### 3.1 数据看板
首页展示企业通知公告、员工工作日历、待办事项、快捷入口、知识库与知识图谱统计数据。

### 3.2 AI 应用
- **智能对话**：员工间一对一即时通讯（WebSocket），保障沟通安全与私密性
- **AI 助手**：基于 RAG 技术的智能问答，可检索系统知识库，实现上下文感知的对话交互
- **Agent 管理**：支持配置和管理 AI Agent

### 3.3 知识库
企业级文档管理中心，支持文件上传/下载、文档在线预览（Word/Excel/PPT/PDF）、全文检索。基于 RAG 技术实现文档分词、向量化存储、多链路召回与智能检索。

### 3.4 知识图谱
基于 Neo4j 图数据库构建企业知识图谱，支持实体关系抽取、图谱可视化展示，帮助发现知识间的关联关系。

### 3.5 流程中心
基于 Flowable 流程引擎，支持流程发起、审批、跟踪、历史查询与流程监控。集成表单设计器，可自定义流程表单。

### 3.6 后台管理
- **基础管理**：用户管理、部门管理、角色管理、公告管理
- **系统配置**：模型管理、Agent 管理、工具管理、对话管理
- **运维管理**：日志管理、流程管理、表单模板管理

---

## 4. 快速开始

### 4.1 后端

```bash
# 1. 初始化数据库（执行 sql/ 目录下的脚本）

# 2. 修改配置（数据库、Redis、RabbitMQ、Elasticsearch 等）
#    编辑 spark-web/src/main/resources/application.yml

# 3. 编译运行
mvn clean install -DskipTests
cd spark-web
mvn spring-boot:run
```

### 4.2 PC 前端

```bash
cd spark-ui

# 安装依赖
npm install

# 浏览器端开发（默认监听 127.0.0.1:80）
npm run dev:web

# 浏览器端构建
npm run build:web

# Electron 桌面端开发
npm run dev:electron

# Electron 桌面端打包
npm run pack:electron:win    # Windows
npm run pack:electron:mac    # macOS
npm run pack:electron:linux  # Linux
```

### 4.3 移动端

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

---

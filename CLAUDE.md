# Spark - Enterprise AI Collaboration Platform

星火云应用平台，集成 RAG 知识库、工作流引擎（Flowable）、AI Agent、知识图谱（Neo4j）、即时通讯，并配套 PC 端（Vue3 + Element Plus + Tauri 2）与移动端（uni-app）前端。

## Key Conventions
- **Minimal changes**: Only modify code related to the current feature. No unrelated refactoring.
- **Layer architecture (strict)**: `Controller → Service → DAO → Database` — cross-layer or reverse calls are forbidden. 无 `spark-service` 模块；`BaseService` 位于 `spark-manage`。
- **Return format**: All Service methods return `ResultData<T>`, success code `ResultData.OK` (200), error via `result.setErrorCode(ErrorCodeEnum.XXX)`.
- **Audit fields**: Automatically populated via `BaseDao.insertDB/updateDBById/deleteDBById` (created_by/dept_id/updated_by/timestamp); 业务层禁止直接调 `insert/updateById/deleteById`。
- **Comments & messages**: All comments and user-facing messages must be in Chinese; `logger.info/warn/error/debug` messages must be in English.
- **详细编码规范见 skills**：`.claude/skills/` 下的 `backend-coding`（Java/MyBatis）、`web-coding`（spark-ui）、`mobile-coding`（spark-app）在生成/修改对应代码时自动加载，含完整模板、禁止项与红线，以其为唯一事实来源。

## No Auto-Compile After Code Generation
- 生成/修改代码后 **不要自动执行编译或构建验证**（如 `mvn compile` / `mvn install` / `mvn package` 等）。
- 代码写完后直接交付，由用户或 CI 统一编译验证，除非用户明确要求编译检查。
- 同理，不要自动执行前端构建（`npm run build` 等）或启动服务。

## Tech Stack
- JDK 17 / SpringBoot 3.4.0 / Maven 3.9.9
- MySQL 8.0.31 / Redis / RabbitMQ / Elasticsearch (8.x)
- MyBatis 3.0.4 + Druid 1.2.20 / Fastjson2 / Hutool 5.8.40
- LangChain4j 1.4.0 / Flowable 7.2.0 / Neo4j Java Driver 5.27.0
- POI 5.4.0 / iTextPDF 9.4.0 / kaptcha / javax.mail / 阿里云短信 (dysmsapi)
- 前端: spark-ui (Vue3 + Element Plus + Vite + Tauri 2) / spark-app (uni-app 微信小程序/H5)

## Module Responsibilities (Backend Maven Modules)
| Module | Responsibility |
|--------|---------------|
| `spark-common` | 基础类、枚举、常量、工具类。`bean/base` 下定义 `BaseEntity/BaseQuery/BaseResult/BaseVO/BaseTree/BaseAssert/BaseContext/BaseException/PageResult/ResultData/SessionHolder`；各业务域 bean（entity/query/result/vo）按 `bean/{module}/` 组织 |
| `spark-dao` | DAO 接口 + MyBatis Mapper XML。`BaseDao<T extends BaseEntity>` 提供 `insertDB/updateDBById/deleteDBById` 默认方法（自动填充审计字段）；各域 DAO 按 `dao/{module}/` 组织，Mapper XML 在 `resources/mapper/{module}/` |
| `spark-config` | 全局配置：`datasource`(Druid)、`redis`、`rabbitmq`、`aspecj`(AOP/操作日志切面)、`async`(异步)、`schedule`(定时)、`captcha`(验证码)、`email`(邮件)、`shortMsg`(短信)、`wecom`(企业微信) |
| `spark-manage` | 核心基础服务层：`BaseService`（分页/数据权限/创建人名补全等通用能力）、系统管理（`system`：用户/部门/角色/消息/通知；`log`：登录日志/操作日志；`auth`：登录认证）、`si` 企业微信组织架构同步、MQ 消费者 `MqConsumer` |
| `spark-web` | 接入层：`controller/{module}/` 各域 Controller、`filter`、`interceptor`、`exception` 全局异常处理、提示资源（prompts）、启动入口 `SparkApplication` |
| `spark-chat` | 即时通讯（WebSocket）：会话空间/消息/用户关系 |
| `spark-flow` | 工作流引擎（Flowable）：流程模板/版本/节点/实例/审批人/讨论/消息，`BaseFlowService` + `FlowableService` 封装流程启停、审批、消息推送 |
| `spark-form` | 表单引擎：表单模板/字段/版本/对象/值 |
| `spark-kb` | RAG 知识库：知识库管理 `IKnowledgeService`、文档管理 `IDocumentService`（上传/分片）、文档分片 `IDocumentChunkService`、文档事件 `IDocumentEventService`（异步处理驱动） |
| `spark-llm` | LLM 集成：模型管理 `IModelService`、供应商 `IProviderService`、智能体 `IAgentService`、MCP `IMcpService`；底层 `ModelFactory`/`AgentFactory`、ES 向量检索 `ESVectorStore`/`ESRetrieve`、Neo4j 图谱存储 `Neo4jGraphStore`/`Neo4jRetrieve`、`RedisChatMemoryStore`、`PromptTemplateLoader`、Agent 工具 `SearchKnowledge/SearchKnowledgeGraph`、`ChunkUtil` 分片工具 |
| `spark-kg` | 知识图谱：实体 `IKgEntityService`、关系 `IKgRelationService`、图谱 `IKgGraphService`（基于 Neo4j，异步抽取复用文档事件链路） |
| `spark-task` | 定时/异步任务：文档处理任务 `DocumentTask`（内容解析、分片、向量化、ES/Neo4j 索引） |

## Frontend Modules
| Module | Responsibility |
|--------|---------------|
| `spark-ui` | PC 端（星火云应用平台）：Vue3 + Element Plus + Vite；`dev:web` / `build:web`；Tauri 2 桌面端（`dev:tauri` / `pack:tauri:win`） |
| `spark-app` | 移动端：uni-app（H5 / 微信小程序），uview-plus |

## 新增业务功能
生成新业务功能（Entity → DAO → Service → Controller 全链路，含 SQL 变更）时，按 `backend-coding` skill 的生成清单执行（`.claude/skills/backend-coding/references/checklist.md`），该 skill 会在编码时自动加载。要点：类头猫脸注释（`@author wangdiankun` + `@since`）；中文注释；写方法是否加 `@LogOperate` 与手动 ID 生成须先询问用户；SQL 变更写入 `sql/alert.sql`（最新在底部）并同步 `sql/table.sql`；生成完成后无需编译验证。

---

# Frontend 前端

前端详细规范（目录结构、组件规范、禁止项、红线、复用能力等）见 `web-coding` 与 `mobile-coding` skill，以下仅列高频要点。

## Frontend: spark-ui (PC 端)

- **技术栈**：Vue 3.3.4 + Vite 4.4.5 + Element Plus 2.3.12 + Vuex 4.1.0 + Vue Router 4.2.5；开发后端 `http://127.0.0.1:8000`（`/api` 由 vite proxy 转发并去掉前缀）。
- **命令**：`npm run dev`（监听 127.0.0.1:80）/ `npm run build`。
- **别名**：`@` → `src`（可省后缀 `.js/.vue/.json/.scss`）；后端基址 `process.env.BASE_HTTP_API`（来自 `.env` 的 `VITE_HTTP_BASE_API`）；加密密钥 `import.meta.env.VITE_ENCRYPT_PRIVATE_KEY`。
- **核心约定**：响应 `{code,message,data}`，`code===200` 成功、`code===601` 跳 `/login`；统一用 `@/api/request`，POST 也用 `params`（文件上传除外）；API 命名 `功能+API`（`getUserListAPI`）；敏感字段 `utils/encryptUtil.js#des()` 加密；`/manage/*` 守卫 `utils/utils.js#isAdmin()`（`userInfo.id===101`）。
- **红线**：不改 `VITE_ENCRYPT_PRIVATE_KEY`、不改 `request.js` 的 601 跳转、不向 console 输出 token/密码/密钥。

## Frontend: spark-app (移动端)

- **技术栈**：Vue 3 + uniapp + Vite + uViewPlus（easycom 自动引入，无需手动 import）。
- **命令**：`npm run dev:h5` / `build:h5` / `dev:mp-weixin` / `build:mp-weixin`。
- **核心约定**：`createSSRApp`；条件编译 `#ifdef H5` / `#ifdef MP-WEIXIN`；API 统一 `@/api/request`（自动带 Authorization，601 自动跳登录）；密码等敏感字段 `des()` 加密（ECB + Pkcs7）。

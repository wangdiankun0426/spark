---
name: backend-coding
description: Spark 项目后端（Java）编码规范。创建/修改 Java 类（Controller/Service/DAO/Entity/VO/Query/Result）、MyBatis XML、生成接口、新增 CRUD/增删改查功能或涉及分层架构时触发。提供核心原则、命名规范、继承体系、红线与文件位置规则；完整代码模板与生成清单在 references/ 下，生成代码前必须先 Read 加载，禁止凭摘要编造模板。
---

# Spark 后端（Java）编码规范

## 1. 触发边界

**适用**：仓库根各 `spark-*` Maven 模块（spark-common / spark-dao / spark-config / spark-manage / spark-web / spark-chat / spark-flow / spark-form / spark-kb / spark-llm / spark-kg / spark-task）中 `src/**` 的 Java 类与 MyBatis XML 的创建、修改。

**不适用**：纯文档、git 操作、前端目录、`sql/` 脚本本身（但字段/表变更需按 SQL 变更规则同步 SQL）。

**强制加载规则（最高优先级）**：本文件只含规则摘要，**代码模板不在本文件内**。命中第 11 节导航表的任何场景时，必须先 Read 对应 references 文件，再动手生成代码；禁止凭本文件摘要或记忆编造模板代码（如 Controller/Service/Mapper XML 的结构、注解、字段），违者按红线处理——返工。

## 2. 项目背景

- **版本**：JDK 17 / SpringBoot 3.4.0 / Maven 3.9.9；MyBatis 3.0.4 + Druid 1.2.20；Fastjson2 / Hutool 5.8.40。
- **无 `spark-service` 模块**；`BaseService` 位于 `spark-manage`。业务 Service 分布在领域模块（chat/flow/form/kb/kg/llm/manage/task），依赖 `spark-manage` 继承 `BaseService`。
- **分层严格**：`Controller → Service → DAO → Database`，禁止跨层或反向调用。
- **返回格式**：所有 Service 方法返回 `ResultData<T>`；成功 `ResultData.OK`（200），失败 `result.setErrorCode(ErrorCodeEnum.XXX)`。

## 3. 核心原则

1. **最小改动**：只改当前功能相关代码，禁止无关重构。
2. **对象映射**：`BeanUtil.copyProperties(vo, entity)`。
3. **ID 生成（仅实体对象级别）**：只有"实体对象级别"的类（即 `ObjectTypeEnum` 中有对应枚举的业务对象，如用户/部门/角色/文档/附件）才自行构造 ID：
   - Service 中 `entity.setId(super.genObjectId(ObjectTypeEnum.XXX))`，禁止手写 ID；
   - 同时实现 `@Override protected Long queryMaxId() { return xxxDao.queryXxxMaxId(); }`（`genObjectId` 依赖它取当前序列基准）；
   - Mapper XML 的 `insert` 必须手动插入 `id` 列（**不用** `useGeneratedKeys`）。
   非实体对象级别的普通记录表（如 `sys_notice`、`sys_message` 等无 ObjectTypeEnum 枚举的）用数据库自增：`insert` 用 `useGeneratedKeys="true" keyProperty="id"`，无需 `setId`/`queryMaxId`。
4. **审计字段**：由 `BaseDao.insertDB/updateDBById/deleteDBById` 自动填充（created_by/dept_id/updated_by/timestamp），禁止手工赋值。
5. **ResultData.code 为 int 原生类型（强制）**：`ResultData.code` 是 `int`（默认 500），**禁止判空**（`getCode() == null` 是不可达的死代码）；判断成功直接 `result.getCode() != ResultData.OK` 比较。注意 `ResultData.OK` 同为 `int`，禁止 `.equals()` 调用。
6. **操作日志与 objId**：写方法加 `@LogOperate(operateType = OperateTypeEnum.XXX_INSERT)`；带 `@LogOperate` 的方法成功返回前必须 `result.setObjId(...)` 设置操作对象 id，否则操作日志保存不到操作对象。
7. **注释与消息**：注释、用户可见消息用中文；`logger.info/warn/error/debug` 消息必须**纯英文、首字母小写的驼峰规则、输出值用 `key={}` 占位**。细则：消息文本首字母小写、不大写开头；多词标识符/方法名/key 保持 camelCase（如 `agentId`、`readSkill`、`executeContentTask`）；单词与缩写全小写（如 `mcp`、`neo4j`、`dag`、`mq`、`es`、`ws`）。禁止中文、禁止 `: {}`/`：{}`/裸 `{}` 占位。示例：

```java
// 禁止：中文、大写开头、冒号分隔、标识符全小写
logger.info("Agent构建完成并缓存, agentId: {}, 名称：{}", agentId, name);
logger.info("agent built and cached, agentid={}", agentId);

// 正确：纯英文、首字母小写驼峰、key={} 占位
logger.info("agent built and cached, agentId={}, name={}", agentId, name);
logger.info("readSkill name={}, skillIds={}", name, skillIds);
```
8. **不自动编译**：生成/修改代码后不执行 `mvn compile/install/package` 等构建验证。
9. **`@LogOperate` 与手动 ID 生成需用户确认（强制询问）**：为写方法生成 `@LogOperate(operateType = OperateTypeEnum.XXX_*)`，或为业务对象手动生成 ID（`genObjectId` + 新增 `ObjectTypeEnum` 枚举 + `queryMaxId`）之前，**必须先询问用户是否需要**；得到用户肯定回答后再生成，**默认不生成**。用户确认不需要时：省略 `@LogOperate` 及对应的 `result.setObjId(...)`；省略 `genObjectId`/`queryMaxId` 与 `ObjectTypeEnum` 枚举，改用数据库自增（`useGeneratedKeys`）。
10. **守护式编程（强制，所有方法必须遵守）**：每个方法入口对入参做防御性校验，非法参数快速失败（guard clause 提前 return），禁止依赖 try-catch 兜底业务异常。详则见 `references/guard-clauses.md`。
11. **JSON 统一走 JsonUtil（强制）**：JSON 解析/序列化一律用 `spark-common` 的 `JsonUtil`（`toObject/toList/toMap/toMapList/toArray/toString/isValidJson/getMap/getMapList/getArray`），业务代码只持有 `Map<String, Object>` / `List` 等 JDK 类型；Map 的类型化取值用 `MapUtil`（`getStringVal/getIntegerVal/getLongVal/getBooleanVal/getDoubleVal`）。业务类禁止 import 第三方 JSON 库（`com.alibaba.fastjson2` / `com.fasterxml.jackson` / `cn.hutool.json`），便于后续维护与 jar 升级；`JsonUtil` 与 `MapUtil` 是唯一的库封装点。

## 4. 基类字段（继承体系）

所有基类在 `spark-common/src/main/java/com/spark/bean/base/`。继承关系（强制）：
- Query → `BaseQuery`；Result → `BaseResult`；VO → `BaseVO`；Entity → `BaseEntity`；Tree → `BaseTree`
- Service 实现 → `BaseService<Q, R>`；DAO → `BaseDao<T>`

| 基类 | 关键字段 |
|------|----------|
| `BaseEntity` | `id`、`createdBy`、`createdDt`、`updatedBy`、`updatedDt`、`deptId` |
| `BaseQuery` | `id`、`ids`、`deleteFlag=1`、`page=true`、`pageNo=1`、`pageSize=30`、`startRow=(pageNo-1)*pageSize`、`dataScopeSQL`、`createdStartTime/createdEndTime`、`createdBy`、`deptId`、`sorts(Map)` |
| `BaseResult` | `id`、`createdBy`、`createdByName`、`createdDt`、`updatedBy`、`updatedByName`、`updatedDt`、`deleteFlag` |
| `BaseVO` | `id` |
| `ResultData<T>` | `code`、`message`、`data`、`objId`；方法 `setCode/setData/setErrorCode(ErrorCodeEnum)/setObjId` |
| `PageResult<T>` | 分页结果：`rows`（列表）、`total`（总数）等 |

`BaseQuery.startRow` 由 getter 自动计算（`(pageNo-1)*pageSize`），Mapper XML 中 `limit #{startRow}, #{pageSize}`。

## 5. 类头注释（强制）

每个 Java 类必须以如下注释块开头：

```java
/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since yyyy-MM-dd HH:mm:ss
 * 类描述（中文）
 */
```

`@since` 格式为 `yyyy-MM-dd HH:mm:ss`（或兼容 `yyyy/M/d HH:mm`，参考现有类）。

## 6. 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类 | PascalCase | `DepartmentService` |
| 方法/变量 | camelCase | `queryList`、`userName` |
| 常量 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 包 | 小写 | `com.spark.service.system` |

- Controller 方法：`createXxx` / `updateXxx` / `deleteXxx` / `pageXxxList` / `queryXxxDetail` / `queryXxxTree`
- DAO 数据方法：`insertDB` / `updateDBById` / `deleteDBById`（BaseDao 提供，自动填充审计字段）；查询 `queryXxxCount` / `queryXxxList` / `queryXxx` / `queryXxxMaxId` / `queryXxxTree`

## 7. 代码风格

- K&R 大括号；关键字后空格；左括号前无空格
- 注释独立成行（代码上方），禁止行尾注释
- **注释核心明了**：注释只说明做什么，禁止在注释中用括号补充细致说明

```java
// 禁止：括号内补充细致说明
// 同任务组产出数据（按任务组id归集，供同组后续任务构造参数时使用，同编码多条以逗号合并）

// 正确：核心明了
// 同任务组产出数据
```
- 最多一个连续空行
- 禁止 emoji
- 禁止无关注释/英文注释/英文响应消息（专有名词除外）
- **禁止方法调用嵌套传参**：方法调用的返回值禁止直接作为另一个方法调用的实参（含 setter），必须先赋值给局部变量再使用；仅 Controller 单层委托 `return xxxService.xxx(vo);` 例外

```java
// 禁止：方法调用嵌套在 setter 实参中
taskInstanceResult.setDataList(taskInstanceDataDao.queryTaskInstanceDataList(dataQuery));

// 正确：先赋值给局部变量，再传入使用
List<TaskInstanceDataResult> dataList = taskInstanceDataDao.queryTaskInstanceDataList(dataQuery);
taskInstanceResult.setDataList(dataList);
```

## 8. 文件位置

| 类型 | 路径 |
|------|------|
| Controller | `spark-web/src/main/java/com/spark/web/controller/{module}/` |
| Service 接口 | `spark-{module}/src/main/java/com/spark/{module}/service/IXxxService.java` |
| Service 实现 | `spark-{module}/src/main/java/com/spark/{module}/service/impl/XxxServiceImpl.java` |
| DAO | `spark-dao/src/main/java/com/spark/dao/{module}/XxxDao.java` |
| Mapper XML | `spark-dao/src/main/resources/mapper/{module}/XxxDao.xml` |
| Entity | `spark-common/src/main/java/com/spark/bean/{module}/entity/Xxx.java` |
| VO | `spark-common/src/main/java/com/spark/bean/{module}/vo/XxxVO.java` |
| Query | `spark-common/src/main/java/com/spark/bean/{module}/query/XxxQuery.java` |
| Result | `spark-common/src/main/java/com/spark/bean/{module}/result/XxxResult.java` |
| Tree | `spark-common/src/main/java/com/spark/bean/{module}/tree/XxxTree.java` |
| Enum | `spark-common/src/main/java/com/spark/enums/` |

业务模块：`chat` / `flow` / `form` / `kb` / `kg` / `llm` / `manage` / `system` / `log` / `auth` / `task` / `workflow`。

## 9. SQL 变更规则

- 新增字段/表/索引：写入 `sql/alert.sql`（顺时间序，最新在底部）
- 表结构变更：同步更新 `sql/table.sql` 对应 CREATE TABLE 定义
- 所有字段/表必须有 COMMENT
- 示例：加列时 `ALTER TABLE xxx ADD COLUMN ...` 追加到 alert.sql 底部，并在 table.sql 同步列定义

## 10. 红线（违反必须返工）

- 跨层或反向调用（如 Controller 直接调 DAO、Service 依赖上层）
- 直接调用 `insert` / `updateById` / `deleteById`（绕过 BaseDao 审计填充）
- 手写 ID、手工赋值审计字段（created_by/updated_by/timestamp/dept_id）
- 注释或用户消息用英文、logger 消息用中文/非首字母小写驼峰/未用 `key={}` 占位
- 修改无关代码、引入无关依赖、通配符导入
- 业务类直接 import 第三方 JSON 库（fastjson2 / jackson / hutool-json），绕过 JsonUtil
- 在代码中输出敏感信息（token/密码/密钥）到日志
- 未经用户同意添加 `@Transactional` 或抽象层
- 方法调用嵌套传参（方法返回值直接作为另一方法的实参，必须先赋值给局部变量；Controller 单层委托 return 除外）
- 方法入口缺少守护式参数校验（入参可能为 null / 越界却不提前拦截，直接进入业务逻辑导致 NPE 或脏数据入库）
- 对 `ResultData.getCode()` 判空或用 `.equals()` 比较（code 与 OK 均为 int 原生类型，直接 `!=`/`==` 比较）

## 11. 参考文件导航（命中场景必须先 Read，再动手）

| 场景 | 动手前必须先 Read |
|------|----------|
| 生成/修改 Controller、Service、DAO、Mapper XML（即几乎一切后端编码任务） | `references/templates.md` |
| 新增完整业务功能（Entity→Controller 全链路） | `references/templates.md` + `references/checklist.md` |
| 编写方法参数校验、判断 try-catch 使用边界 | `references/guard-clauses.md` |

判断依据：拿不准是否命中时，一律按命中处理——先 Read 再写。

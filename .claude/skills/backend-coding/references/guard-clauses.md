# 守护式编程规范（强制详则）

配套规范：`.claude/skills/backend-coding/SKILL.md`（核心原则第 9 条）。

所有 Service/Controller/工具方法在**方法体第一行起**对入参做防御性校验，非法参数快速失败（guard clause），禁止依赖 try-catch 拦截非法参数。

## 1. 校验原则

1. **先校验后执行**：方法第一行开始就是校验代码，业务逻辑在全部校验通过后才执行
2. **快速失败**：校验不通过立即 `return`，不进入后续逻辑
3. **覆盖范围**：null 对象、必填字段为空、数值越界（如状态、评分 1-5）、集合为空等一切可能导致 NPE 或脏数据的场景
4. **统一工具**：`StringUtil.isBlank` / `CollectionUtil.isEmpty` / `Objects.isNull`，不手写 `!= null` 长链
5. **查询兜底**：`query` 等可选入参为 null 时 `query = new XxxQuery();` 兜底（利用 `BaseQuery` 默认值），而非直接使用

## 2. 返回 ResultData 的方法

校验不通过返回错误码，必须放在方法最前：

```java
@Override
public ResultData<Void> feedback(RetrieveLogVO retrieveLogVO) {
    ResultData<Void> result = new ResultData<>();
    // 守护式校验：先校验，后执行
    if (retrieveLogVO == null || retrieveLogVO.getId() == null) {
        result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
        return result;
    }
    Integer feedbackScore = retrieveLogVO.getFeedbackScore();
    if (feedbackScore == null || feedbackScore < 1 || feedbackScore > 5) {
        result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
        return result;
    }
    // 业务逻辑...
}
```

- 一个方法多处校验时，逐个 `if (...) { return; }` 拆开，禁止把多个校验塞进一个 if 里用 `&&` 叠加
- 校验代码之间不夹业务逻辑

## 3. 无返回值（void）方法

校验不通过记录 warn 日志后 return，不进入业务逻辑：

```java
@Override
public void saveRetrieveLog(Long knowledgeId, String query, RetrieveDetailResult detailResult, long costTime) {
    if (knowledgeId == null || StringUtil.isBlank(query) || detailResult == null) {
        logger.warn("saveRetrieveLog skip, invalid param, knowledgeId={}, query={}", knowledgeId, query);
        return;
    }
    // 业务逻辑...
}
```

## 4. 与 try-catch 的关系

- **业务异常不靠 try-catch**：Controller 方法抛出的异常由 `spark-web` 的 `GlobalExceptionHandler` 统一转 `SYSTEM_ERROR`，Service 层不写 try-catch 拦截
- **尽力而为的辅助逻辑可吞异常**：如保存日志这类失败不影响主流程的 void 操作，内部可用 try-catch 包裹吞掉异常（`logger.error(...)`），避免污染主流程——但这是唯一允许 try-catch 的场景
- 守卫校验负责拦截**可预期的非法输入**；try-catch 只兜底**不可预期的系统异常**，两者职责不同，禁止用 try-catch 代替守卫校验

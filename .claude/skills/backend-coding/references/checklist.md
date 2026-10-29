# 新增业务功能生成清单

配套规范：`.claude/skills/backend-coding/SKILL.md`；代码模板见 `templates.md`。

新增业务功能时按序逐项验证：

1. [ ] `spark-common` 创建 Entity / VO / Query / Result，分别继承对应基类
2. [ ] `spark-dao` 创建 DAO 接口（继承 `BaseDao`）与 Mapper XML（含 `insert` / `updateById` / `deleteById`）
3. [ ] 领域模块创建 `IXxxService` 与 `impl/XxxServiceImpl`（继承 `BaseService<Q,R>`）
4. [ ] `spark-web` 创建 Controller（`@RestController` + `@RequestMapping("module/entity")`，方法 `public`）
5. [ ] 类头猫脸注释 + `@since`；中文注释；写方法 `@LogOperate` + 成功时 `result.setObjId(...)`（**须先询问用户确认**，默认不生成，见 SKILL.md 核心原则第 8 条）；业务层 `insertDB/updateDBById/deleteDBById`
6. [ ] 分页方法 `@DataScope` + 实现 `queryCount/queryList`；手动 ID 生成（`genObjectId` + `ObjectTypeEnum` + `queryMaxId`）**须先询问用户确认**，默认不生成
7. [ ] 每个方法入口守护式参数校验（返回 `ResultData` 校验失败置 `ErrorCodeEnum` 后 return；void 方法记 warn 后 return；详则见 `guard-clauses.md`）
8. [ ] SQL 变更写入 `sql/alert.sql`（最新在底部）并同步 `sql/table.sql`
9. [ ] 生成完成后不做编译验证

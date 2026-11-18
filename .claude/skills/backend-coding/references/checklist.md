# 新增业务功能生成清单

配套规范：`.claude/skills/backend-coding/SKILL.md`；代码模板见 `templates.md`，鉴权逻辑见 `permission.md`。

新增业务功能时按序逐项验证：

1. [ ] `spark-common` 创建 Entity / VO / Query / Result，分别继承对应基类
2. [ ] `spark-dao` 创建 DAO 接口（继承 `BaseDao`）与 Mapper XML（含 `insert` / `updateById` / `deleteById`）
3. [ ] 领域模块创建 `IXxxService` 与 `impl/XxxServiceImpl`（继承 `BaseService<Q,R>`）
4. [ ] `spark-web` 创建 Controller（`@RestController` + `@RequestMapping("module/entity")`，方法 `public`）
5. [ ] 类头猫脸注释 + `@since`；中文注释；DAO 接口方法带中文 Javadoc，Mapper XML 无任何 `<!-- -->` 注释；写方法 `@LogOperate` + 成功时 `result.setObjId(...)`（**须先询问用户确认**，默认不生成，见 SKILL.md 核心原则第 9 条）；业务层 `insertDB/updateDBById/deleteDBById`
6. [ ] 分页方法 `@DataScope` + 实现 `queryCount/queryList`；手动 ID 生成（`genObjectId` + `ObjectTypeEnum` + `queryMaxId`）**须先询问用户确认**，默认不生成
7. [ ] 鉴权与租户隔离（详则见 `permission.md`）：Controller public 方法按需标 `@RequiresPermi`，权限串与 `sys_menu` 按钮种子逐字一致；非分页查询/详情/写操作前置 `scopeTenant` 或 `assertTenant`，create 非 sysAdmin 强制归属当前租户；无 tenant_id/dept_id 子表经父对象收口
8. [ ] 每个方法入口守护式参数校验（返回 `ResultData` 校验失败置 `ErrorCodeEnum` 后 return；void 方法记 warn 后 return；详则见 `guard-clauses.md`）
9. [ ] SQL 变更写入 `sql/alert.sql`（最新在底部）并同步 `sql/table.sql`
10. [ ] 测试代码遵守正常后端编码与中文注释规范，相关单元测试已实际执行并全部通过；不单独执行 compile/install/package，不启动或重启服务

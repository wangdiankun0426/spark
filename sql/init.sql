insert into `sys_user`(id, login_name, password, name, status, account_type, delete_flag, created_by, created_dt, updated_by, updated_dt)
values (101, 'sysadmin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 1, 2, 1, 101, now(), 101, now());

insert into `sys_user_profile`(id, delete_flag, created_by, created_dt, updated_by, updated_dt)
values (101, 1, 101, now(), 101, now());

INSERT INTO `sys_tenant` (`id`, `name`, `status`, `deadline`, `account_count`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (103, '演示租户环境', 1, null, 10, 1, 101, now(), 101, now());

INSERT INTO `sys_tenant_config` (`tenant_id`, `name`, `key`, `value`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (103, '默认密码', 'default_password', '123456', 1, 101, now(), 101, now());
INSERT INTO `sys_tenant_config` (`tenant_id`, `name`, `key`, `value`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (103, '企微企业ID', 'wecom_corp_id', '', 1, 101, now(), 101, now());
INSERT INTO `sys_tenant_config` (`tenant_id`, `name`, `key`, `value`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (103, '企微应用ID', 'wecom_agent_id', '', 1, 101, now(), 101, now());
INSERT INTO `sys_tenant_config` (`tenant_id`, `name`, `key`, `value`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (103, '企微应用密钥', 'wecom_corp_secret', '', 1, 101, now(), 101, now());
INSERT INTO `sys_tenant_config` (`tenant_id`, `name`, `key`, `value`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (103, '企微消息Token', 'wecom_msg_token', '', 1, 101, now(), 101, now());
INSERT INTO `sys_tenant_config` (`tenant_id`, `name`, `key`, `value`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (103, '企微消息密钥', 'wecom_msg_encoding_aes_key', '', 1, 101, now(), 101, now());
INSERT INTO `sys_tenant_config` (`tenant_id`, `name`, `key`, `value`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (103, '密码最小长度', 'pwd.min.length', '6', 1, 101, now(), 101, now());
INSERT INTO `sys_tenant_config` (`tenant_id`, `name`, `key`, `value`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (103, '密码最大长度', 'pwd.max.length', '14', 1, 101, now(), 101, now());

INSERT INTO `sys_user` (`id`, `login_name`, `password`, `name`, `status`, `account_type`, `current_tenant_id`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (201, 'orgadmin', 'e10adc3949ba59abbe56e057f20f883e', '组织管理员', 1, 1, 103, 1, 101, now(), 101, now());
INSERT INTO `sys_user_profile` (`id`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (201, 1, 101, now(), 101, now());
INSERT INTO `sys_tenant_user` (`tenant_id`, `user_id`, `role_type`, `dept_id`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (103, 201, 2, 0, 1, 101, now(), 101, now());

INSERT INTO `task_template` (`id`,`tenant_id`,`name`, `task_type`, `remark`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (1, 0, '知识库文档归档任务', 2, '', 1, 101, now(), 101, now());
INSERT INTO `task_template_param` (`id`,`template_id`, `name`, `code`, `type`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (1, 1, '待归档文档ID', 'attId', 2, 1, 101, now(), 101, now());
INSERT INTO `task_template_param` (`id`,`template_id`, `name`, `code`, `type`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (2,1, '所属知识库ID', 'kbId', 2, 1, 101, now(), 101, now());

INSERT INTO `sys_menu` (`name`, `parent_code`, `type`, `code`, `delete_flag`, `created_by`, `created_dt`) VALUES
( 'AI管理模块', '', 1, 'module.llm', 1, 101, now()),
('业务管理模块', '', 1, 'module.bus', 1, 101, now()),
( 'Agent菜单', 'module.llm', 2, 'menu.llm.agent', 1, 101, now()),
( 'WorkFlow菜单', 'module.llm', 2, 'menu.llm.workflow', 1, 101, now()),
( '模型市场菜单', 'module.llm', 2, 'menu.llm.modelMarket', 1, 101, now()),
('技能库菜单', 'module.llm', 2, 'menu.llm.skill', 1, 101, now()),
('MCP服务菜单', 'module.llm', 2, 'menu.llm.mcp', 1, 101, now()),
('知识库菜单', 'module.llm', 2, 'menu.llm.knowledge', 1, 101, now()),
('知识图谱菜单', 'module.llm', 2, 'menu.llm.graph', 1, 101, now()),
( '流程模板菜单', 'module.bus', 2, 'menu.bus.flow', 1, 101, now());

INSERT INTO `llm_skill` (`id`, `tenant_id`, `name`, `description`, `content`, `status`, `dept_id`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (1, 103, 'sql编写规范', '在写sql时候必须使用此规范', '---\nname: sql-writing-guide\ndescription: 当用户要求编写、优化或审查 MySQL 8 查询语句时命中。强制统一的命名规范、分页写法、JOIN 与索引要求，输出可直接执行的 SQL 并附设计说明。\n---\n\n## 执行流程\n\n1. 明确用户的查询目标：涉及的表、过滤条件、返回字段、是否需要分页或排序。\n2. 信息不足时先向用户确认（表结构、数据量级、是否允许全表扫描），不要凭空假设表名与字段名。\n3. 按以下规则编写 SQL，具体细则按需读取对应子文件：\n   - 表名、字段名、别名、索引名的命名要求 → 读取 `references/naming-convention.md`\n   - 分页、JOIN、子查询、函数使用等编写规则与禁止项 → 读取 `references/query-rules.md`\n   - 常见场景（分页列表、树形查询、批量操作等）的可复用模板 → 读取 `scripts/query-templates.sql`\n   - 评估 SQL 是否走索引或给出加索引建议时 → 读取 `references/index-guide.md`\n4. 输出 SQL 后必须附「设计说明」，包含：\n   - 关键写法的选择理由（如为什么用该 JOIN 方式）\n   - 预计命中的索引；如无法命中索引，必须显式提示全表扫描风险\n5. 审查用户提供的既有 SQL 时，按上述 references 逐条核对，输出「违规项清单 + 修正后的 SQL」。\n\n## 硬性要求\n\n- 所有 SQL 以 MySQL 8.0 语法为准，禁止使用 MySQL 8 已移除的特性。\n- 禁止 `SELECT *`，必须列出明确字段。\n- 分页必须包含 `ORDER BY`，且排序字段需有索引或说明无法走索引的原因。\n- 涉及多表时必须为每张表指定别名，别名规则见 `references/naming-convention.md`。\n- 不杜撰表名与字段名；用户未提供表结构时，先要求提供或用注释占位并明确标注。\n\n## 输出格式\n\n1. SQL 代码块（带必要的中文注释）\n2. 设计说明（要点列表）\n3. 如有风险或待确认项，单独列出「注意事项」\n', 1, 102, 1, 101, '2026-09-05 18:38:42', 201, '2026-10-15 11:07:14');
INSERT INTO `llm_skill_file` (`id`, `tenant_id`, `skill_id`, `file_path`, `description`, `content`, `sort`, `dept_id`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (1, 103, 1, 'references/index-guide.md', '索引设计与命中判断', '# 索引设计与命中判断\n\n## 判断方法\n\n- 用 `EXPLAIN` 思路评估：type 至少达到 `range`，`ALL`（全表扫描）必须显式提示。\n- 联合索引遵循最左前缀：索引 `(a, b, c)` 可命中 `a`、`a,b`、`a,b,c` 前缀条件。\n- `ORDER BY` 字段与 `WHERE` 条件共用联合索引时可避免 filesort，例如 `WHERE a = ? ORDER BY b` 适用索引 `(a, b)`。\n\n## 建索引建议的输出格式\n\n给出加索引建议时，直接输出可执行的 DDL：\n\n```sql\nALTER TABLE order_info ADD INDEX idx_user_id_created_dt (user_id, created_dt);\n```\n\n并说明：该索引服务哪些查询、是否会造成已有查询的索引选择变化、写入开销提示。\n\n## 常见失效场景\n\n- 字段上套函数或做隐式类型转换（字符串字段传数字）。\n- `LIKE` 前缀通配、`OR` 两侧有一侧无索引、联合索引跳过最左列。\n- 数据区分度过低的字段（如 status 仅 2~3 个值）单独建索引意义不大，应做联合索引首列需高区分度。\n', 1, 902, 1, 201, '2026-10-15 11:07:14', 201, '2026-10-15 11:07:14');
INSERT INTO `llm_skill_file` (`id`, `tenant_id`, `skill_id`, `file_path`, `description`, `content`, `sort`, `dept_id`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (2, 103, 1, 'references/naming-convention.md', '命名规范', '# 命名规范\n\n## 表名与字段名\n\n- 一律小写蛇形命名：`user_info`、`created_dt`。\n- 表名为业务域名词，禁止拼音、禁止复数后缀。\n- 主键统一为 `id`；外键引用写 `xxx_id`。\n- 时间字段：日期时间用 `_dt` 后缀（如 `created_dt`），纯日期用 `_date` 后缀。\n- 布尔/标识字段用 `is_` 或 `_flag` 后缀（如 `delete_flag`），值为 `1` / `-1` 时须与平台约定一致。\n- 金额、数量等数值字段须注明单位倾向（如 `_amount` 分、`_count` 次数）。\n\n## 别名规则\n\n- 多表查询必须为每张表取别名：主表用业务缩写（如 `user_info` → `ui`），关联表按语义缩写（如 `dept` → `d`）。\n- 别名长度不超过 4 个字母，同一个 SQL 中不得重复。\n- 输出字段若与其他表字段同名，必须用别名表前缀引用（`ui.name`），避免歧义列。\n\n## 索引名\n\n- 普通索引：`idx_字段名`，如 `idx_created_dt`。\n- 联合索引：`idx_首字段_次字段`，如 `idx_user_id_status`。\n- 唯一索引：`uk_字段名`，如 `uk_username`。\n\n## SQL 书写风格\n\n- 关键字大写（`SELECT`、`FROM`、`WHERE`），表名字段名小写。\n- 每行一个字段 / 一个条件，子句换行对齐：\n\n```sql\nSELECT ui.id,\n       ui.name,\n       d.dept_name\nFROM user_info ui\nLEFT JOIN dept d ON d.id = ui.dept_id\nWHERE ui.status = 1\nORDER BY ui.id DESC\nLIMIT 0, 20;\n```\n', 2, 902, 1, 201, '2026-10-15 11:07:14', 201, '2026-10-15 11:07:14');
INSERT INTO `llm_skill_file` (`id`, `tenant_id`, `skill_id`, `file_path`, `description`, `content`, `sort`, `dept_id`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (3, 103, 1, 'references/query-rules.md', '查询编写规则', '# 查询编写规则\n\n## 分页\n\n- 统一使用 `LIMIT offset, size`，且必须带 `ORDER BY`；无稳定排序字段时补 `ORDER BY id`。\n- 深度分页（offset > 10000）须改用「游标分页」：`WHERE id < #{lastId} ORDER BY id DESC LIMIT size`，并向用户说明改动原因。\n- 需要总数时单独写 `COUNT` 查询，不用 `SQL_CALC_FOUND_ROWS`（MySQL 8 已废弃）。\n\n## JOIN\n\n- 多表关联不超过 3 张；超过时建议拆分查询或改冗余字段，并向用户说明取舍。\n- 关联条件字段必须有索引；无索引时显式提示风险。\n- 只取「是否存在」语义用 `EXISTS`，不用 `LEFT JOIN ... IS NULL` 或 `IN` 大列表。\n- `IN` 列表元素不超过 1000 个，超过建议分批或临时表。\n\n## 子查询与函数\n\n- WHERE 条件中的字段禁止套函数（如 `DATE(created_dt) = \'2026-01-01\'`），改为范围条件（`created_dt >= \'2026-01-01\' AND created_dt < \'2026-01-02\'`）以保证走索引。\n- 禁止 `SELECT *`；禁止 `ORDER BY RAND()`；禁止 `NOT IN`（可能含 NULL 导致结果异常），用 `NOT EXISTS` 替代。\n- 模糊查询避免前缀 `%`（`LIKE \'%abc\'` 无法走索引），如业务必须前缀模糊须提示全表扫描风险。\n\n## 写入与事务\n\n- 更新/删除必须带 `WHERE` 条件，且条件字段优先走唯一索引；无条件全表改一律拒绝输出并提醒。\n- 批量插入用多值 `INSERT INTO ... VALUES (...), (...)`，单批建议不超过 500 行。\n- 涉及多步写入时，提示应用层需要事务包裹，技能本身不生成存储过程。\n\n## 审查清单\n\n审查既有 SQL 时按顺序核对：禁止项 → 索引命中 → 分页写法 → 命名规范 → 书写风格，逐条输出违规项并给出修正 SQL。\n', 3, 902, 1, 201, '2026-10-15 11:07:14', 201, '2026-10-15 11:07:14');
INSERT INTO `llm_skill_file` (`id`, `tenant_id`, `skill_id`, `file_path`, `description`, `content`, `sort`, `dept_id`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (4, 103, 1, 'scripts/query-templates.sql', '本文件为参考模板，不会在服务端执行；使用时替换表名、字段名与条件。', '-- 注意：本文件为参考模板，不会在服务端执行；使用时替换表名、字段名与条件。\n\n-- 1. 普通分页列表\nSELECT ui.id,\n       ui.name,\n       ui.status,\n       ui.created_dt\nFROM user_info ui\nWHERE ui.status = 1\n  AND ui.delete_flag = 1\nORDER BY ui.id DESC\nLIMIT 0, 20;\n\n-- 2. 游标分页\nSELECT ui.id,\n       ui.name\nFROM user_info ui\nWHERE ui.id < #{lastId}\n  AND ui.status = 1\nORDER BY ui.id DESC\nLIMIT 20;\n\n-- 3. 存在性判断\nSELECT CASE WHEN EXISTS (\n    SELECT 1\n    FROM user_info ui\n    WHERE ui.username = #{username}\n      AND ui.delete_flag = 1\n) THEN 1 ELSE 0 END AS exist_flag;\n\n-- 4. 时间范围统计\nSELECT DATE_FORMAT(o.created_dt, \'%Y-%m-%d\') AS stat_date,\n       COUNT(1) AS order_count,\n       SUM(o.total_amount) AS total_amount\nFROM order_info o\nWHERE o.created_dt >= #{startDt}\n  AND o.created_dt < #{endDt}\nGROUP BY DATE_FORMAT(o.created_dt, \'%Y-%m-%d\')\nORDER BY stat_date;\n\n-- 5. 树形结构递归查询\nWITH RECURSIVE dept_tree AS (\n    SELECT d.id, d.dept_name, d.parent_id, 1 AS level_no\n    FROM dept d\n    WHERE d.id = #{rootId}\n    UNION ALL\n    SELECT d.id, d.dept_name, d.parent_id, t.level_no + 1\n    FROM dept d\n    INNER JOIN dept_tree t ON t.id = d.parent_id\n)\nSELECT t.id, t.dept_name, t.parent_id, t.level_no\nFROM dept_tree t\nORDER BY t.level_no, t.id;\n\n-- 6. 批量插入\nINSERT INTO user_info (name, status, created_by)\nVALUES (\'张三\', 1, 101),\n       (\'李四\', 1, 101);\n\n-- 7. 安全更新\nUPDATE user_info\nSET status = -1,\n    updated_by = 101\nWHERE id = #{id};\n', 4, 902, 1, 201, '2026-10-15 11:07:14', 201, '2026-10-15 11:07:14');

insert into `sys_user`(id, login_name, password, name, status, account_type, delete_flag, created_by, created_dt, updated_by, updated_dt)
values (101, 'sysadmin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 1, 2, 1, 101, now(), 101, now());

INSERT INTO `sys_tenant` (`id`, `name`, `status`, `deadline`, `account_count`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (103, '演示租户环境', 1, null, 10, 1, 101, now(), 101, now());

INSERT INTO `task_template` (`id`,`tenant_id`,`name`, `task_type`, `remark`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (1, 0, '知识库文档归档任务', 2, '', 1, 101, now(), 101, now());
INSERT INTO `task_template_param` (`id`, `tenant_id`,`template_id`, `name`, `code`, `type`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (1,0, 1, '待归档文档ID', 'attId', 2, 1, 101, now(), 101, now());
INSERT INTO `task_template_param` (`id`, `tenant_id`,`template_id`, `name`, `code`, `type`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (2, 0,1, '所属知识库ID', 'kbId', 2, 1, 101, now(), 101, now());

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
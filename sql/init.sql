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
INSERT INTO `sys_tenant_config` (`tenant_id`, `name`, `key`, `value`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (103, '模型最大并发', 'llm.max.concurrent', '0', 1, 101, now(), 101, now());
INSERT INTO `sys_tenant_config` (`tenant_id`, `name`, `key`, `value`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (103, '模型每分钟请求数', 'llm.rpm', '0', 1, 101, now(), 101, now());
INSERT INTO `sys_tenant_config` (`tenant_id`, `name`, `key`, `value`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (103, '模型每分钟token数', 'llm.tpm', '0', 1, 101, now(), 101, now());

INSERT INTO `sys_user` (`id`, `login_name`, `password`, `name`, `status`, `account_type`, `current_tenant_id`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (201, 'orgadmin', 'e10adc3949ba59abbe56e057f20f883e', '组织管理员', 1, 1, 103, 1, 101, now(), 101, now());
INSERT INTO `sys_user_profile` (`id`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (201, 1, 101, now(), 101, now());
INSERT INTO `sys_tenant_user` (`tenant_id`, `user_id`, `role_type`, `dept_id`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (103, 201, 2, 0, 1, 101, now(), 101, now());

INSERT INTO `task_template` (`id`,`tenant_id`,`name`, `task_type`, `remark`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (1, 0, '知识库文档归档任务', 2, '', 1, 101, now(), 101, now());
INSERT INTO `task_template_param` (`id`,`template_id`, `name`, `code`, `type`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (1, 1, '待归档文档ID', 'attId', 2, 1, 101, now(), 101, now());
INSERT INTO `task_template_param` (`id`,`template_id`, `name`, `code`, `type`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (2,1, '所属知识库ID', 'kbId', 2, 1, 101, now(), 101, now());

-- 菜单全量种子
-- 5 模块 + 12 分组 + 42 菜单 + 201 按钮

INSERT INTO `sys_menu` (`name`, `parent_code`, `code`, `type`, `perms`, `path`, `component`, `icon`, `order_num`, `visible`, `status`, `platform`, `delete_flag`, `created_by`, `created_dt`)
VALUES
-- 模块
('工作台', '', 'module.home', 1, NULL, '/home', 'pages/home/index', 'home', 1, 1, 1, 2, 1, 101, now()),
('AI管理', '', 'module.llm', 1, NULL, '/llm', 'pages/llmMange/index', 'navAi', 2, 1, 1, 0, 1, 101, now()),
('业务管理', '', 'module.bus', 1, NULL, '/bus', 'pages/busManage/index', 'navBus', 3, 1, 1, 0, 1, 101, now()),
('组织管理', '', 'module.org', 1, NULL, '/org', 'pages/orgMange/index', 'navOrg', 4, 1, 1, 3, 1, 101, now()),
('站点管理', '', 'module.site', 1, NULL, '/site', 'pages/siteManage/index', 'navSite', 5, 1, 1, 1, 1, 101, now()),
-- 首页分组目录，无组件，路由生成时拍平、侧栏保留分组
('通用', 'module.home', 'group.home.common', 2, NULL, NULL, NULL, NULL, 1, 1, 1, 2, 1, 101, now()),
('AI', 'module.home', 'group.home.ai', 2, NULL, NULL, NULL, NULL, 2, 1, 1, 2, 1, 101, now()),
('流程', 'module.home', 'group.home.flow', 2, NULL, NULL, NULL, NULL, 3, 1, 1, 2, 1, 101, now()),
-- AI管理分组目录
('基础', 'module.llm', 'group.llm.basic', 2, NULL, NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
('应用', 'module.llm', 'group.llm.app', 2, NULL, NULL, NULL, NULL, 2, 1, 1, 0, 1, 101, now()),
('增强', 'module.llm', 'group.llm.enhance', 2, NULL, NULL, NULL, NULL, 3, 1, 1, 0, 1, 101, now()),
-- 业务管理分组目录
('流程', 'module.bus', 'group.bus.flow', 2, NULL, NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
-- 组织管理分组目录
('数据', 'module.org', 'group.org.data', 2, NULL, NULL, NULL, NULL, 1, 1, 1, 3, 1, 101, now()),
('监控', 'module.org', 'group.org.monitor', 2, NULL, NULL, NULL, NULL, 2, 1, 1, 3, 1, 101, now()),
('配置', 'module.org', 'group.org.config', 2, NULL, NULL, NULL, NULL, 3, 1, 1, 3, 1, 101, now()),
-- 站点管理分组目录
('数据', 'module.site', 'group.site.data', 2, NULL, NULL, NULL, NULL, 1, 1, 1, 1, 1, 101, now()),
('监控', 'module.site', 'group.site.monitor', 2, NULL, NULL, NULL, NULL, 2, 1, 1, 1, 1, 101, now()),
-- AI管理菜单
('模型用量', 'group.llm.basic', 'menu.llm.modelUsage', 3, NULL, '/llm/modelUsage', 'pages/llmMange/modelUsage/index', 'modelUsage', 1, 1, 1, 0, 1, 101, now()),
('模型市场', 'group.llm.basic', 'menu.llm.modelMarket', 3, NULL, '/llm/modelMarket', 'pages/llmMange/modelMarket/index', 'modelMarket', 2, 1, 1, 0, 1, 101, now()),
('技能库', 'group.llm.basic', 'menu.llm.skill', 3, NULL, '/llm/skill', 'pages/llmMange/skill/index', 'skill', 3, 1, 1, 0, 1, 101, now()),
('MCP服务', 'group.llm.basic', 'menu.llm.mcp', 3, NULL, '/llm/mcp', 'pages/llmMange/mcp/index', 'mcp', 4, 1, 1, 0, 1, 101, now()),
('智能体', 'group.llm.app', 'menu.llm.agent', 3, NULL, '/llm/agent', 'pages/llmMange/agent/index', 'agent', 1, 1, 1, 0, 1, 101, now()),
('工作流', 'group.llm.app', 'menu.llm.workflow', 3, NULL, '/llm/workflow', 'pages/llmMange/workflow/index', 'workflow', 2, 1, 1, 0, 1, 101, now()),
('知识库', 'group.llm.enhance', 'menu.llm.knowledge', 3, NULL, '/llm/knowledge', 'pages/llmMange/knowledge/index', 'knowledge', 1, 1, 1, 0, 1, 101, now()),
('知识图谱', 'group.llm.enhance', 'menu.llm.graph', 3, NULL, '/llm/graph', 'pages/llmMange/graph/index', 'graph', 2, 1, 1, 0, 1, 101, now()),
('运行记录', 'menu.llm.workflow', 'menu.llm.workflow.instance', 3, NULL, '/llm/workflow/instance', 'pages/llmMange/workflow/instance', NULL, 9, 0, 1, 0, 1, 101, now()),
('知识库文档', 'menu.llm.knowledge', 'menu.llm.knowledge.document', 3, NULL, '/llm/knowledge/document', 'views/dms/document/index', NULL, 10, 0, 1, 0, 1, 101, now()),
('图谱详情', 'menu.llm.graph', 'menu.llm.graph.detail', 3, NULL, '/llm/graph/detail', 'pages/llmMange/graph/graphDetail/index', NULL, 11, 0, 1, 0, 1, 101, now()),
('图谱文档', 'menu.llm.graph', 'menu.llm.graph.document', 3, NULL, '/llm/graph/document', 'views/dms/document/index', NULL, 12, 0, 1, 0, 1, 101, now()),
('图谱实体', 'menu.llm.graph', 'menu.llm.graph.entity', 3, NULL, '/llm/graph/entity', 'pages/llmMange/graph/entity/index', NULL, 13, 0, 1, 0, 1, 101, now()),
('图谱关系', 'menu.llm.graph', 'menu.llm.graph.relation', 3, NULL, '/llm/graph/relation', 'pages/llmMange/graph/relation/index', NULL, 14, 0, 1, 0, 1, 101, now()),
-- 业务管理菜单
('流程模板', 'group.bus.flow', 'menu.bus.flow', 3, NULL, '/bus/flowTemplate', 'pages/busManage/flowTemplate/index', 'flowApplication', 1, 1, 1, 0, 1, 101, now()),
('流程实例', 'menu.bus.flow', 'menu.bus.flow.instance', 3, NULL, '/bus/flowInstance', 'pages/busManage/flowTemplate/instance', NULL, 2, 0, 1, 0, 1, 101, now()),
-- 组织管理菜单
('用户管理', 'group.org.data', 'menu.org.sys.user', 3, NULL, '/org/sys/user', 'pages/orgMange/sys/user', 'User', 1, 1, 1, 3, 1, 101, now()),
('部门管理', 'group.org.data', 'menu.org.sys.dept', 3, NULL, '/org/sys/dept', 'pages/orgMange/sys/dept', 'OfficeBuilding', 2, 1, 1, 3, 1, 101, now()),
('角色管理', 'group.org.data', 'menu.org.sys.role', 3, NULL, '/org/sys/role', 'pages/orgMange/sys/role', 'role', 3, 1, 1, 3, 1, 101, now()),
('消息管理', 'group.org.data', 'menu.org.sys.message', 3, NULL, '/org/sys/message', 'pages/orgMange/sys/message', 'Bell', 4, 1, 1, 3, 1, 101, now()),
('登录日志', 'group.org.monitor', 'menu.org.log.login', 3, NULL, '/org/log/login', 'pages/orgMange/log/login', 'Key', 1, 1, 1, 3, 1, 101, now()),
('操作日志', 'group.org.monitor', 'menu.org.log.operate', 3, NULL, '/org/log/operate', 'pages/orgMange/log/operate', 'Document', 2, 1, 1, 3, 1, 101, now()),
('系统配置', 'group.org.config', 'menu.org.sys.config', 3, NULL, '/org/sys/config', 'pages/orgMange/sys/config', 'SetUp', 1, 1, 1, 3, 1, 101, now()),
-- 站点管理菜单
('租户管理', 'group.site.data', 'menu.site.sys.tenant', 3, NULL, '/site/sys/tenant', 'pages/siteManage/sys/tenant', 'User', 1, 1, 1, 1, 1, 101, now()),
('菜单管理', 'group.site.data', 'menu.site.sys.menu', 3, NULL, '/site/sys/menu', 'pages/siteManage/sys/menu', 'Menu', 2, 1, 1, 1, 1, 101, now()),
('租户配置', 'menu.site.sys.tenant', 'menu.site.sys.tenantConfig', 3, NULL, '/site/sys/tenant/config', 'pages/siteManage/sys/tenantConfig', NULL, 2, 0, 1, 1, 1, 101, now()),
('服务器监控', 'group.site.monitor', 'menu.site.monitor.server', 3, NULL, '/site/monitor/server', 'pages/siteManage/monitor/server', 'Monitor', 1, 1, 1, 1, 1, 101, now()),
('缓存监控', 'group.site.monitor', 'menu.site.monitor.cache', 3, NULL, '/site/monitor/cache', 'pages/siteManage/monitor/cache', 'Coin', 2, 1, 1, 1, 1, 101, now()),
('消息队列', 'group.site.monitor', 'menu.site.monitor.mq', 3, NULL, '/site/monitor/mq', 'pages/siteManage/monitor/mq', 'Connection', 3, 1, 1, 1, 1, 101, now()),
('接口请求', 'group.site.monitor', 'menu.site.monitor.api', 3, NULL, '/site/monitor/api', 'pages/siteManage/monitor/api', 'DataLine', 4, 1, 1, 1, 1, 101, now()),
-- 首页菜单，全员公共
('工作台', 'group.home.common', 'menu.home.dashboard', 3, NULL, '/home/dashboard', 'pages/home/dashboard/index', 'dashboard', 1, 1, 1, 2, 1, 101, now()),
('通讯录', 'group.home.common', 'menu.home.contact', 3, NULL, '/home/contact', 'pages/home/contact/index', 'chat', 2, 1, 1, 2, 1, 101, now()),
('AI对话', 'group.home.ai', 'menu.home.aichat', 3, NULL, '/home/aichat', 'pages/home/aichat/index', 'aichat', 1, 1, 1, 2, 1, 101, now()),
('智能体', 'group.home.ai', 'menu.home.agent', 3, NULL, '/home/agent', 'pages/home/agent/index', 'agent', 2, 1, 1, 2, 1, 101, now()),
('工作流', 'group.home.ai', 'menu.home.workflow', 3, NULL, '/home/workflow', 'pages/home/workflow/index', 'workflow', 3, 1, 1, 2, 1, 101, now()),
('待审核', 'group.home.ai', 'menu.home.workflow.review', 3, NULL, '/home/workflow/review', 'pages/home/workflow/review', 'myTodo', 4, 1, 1, 2, 1, 101, now()),
('运行实例', 'menu.home.workflow', 'menu.home.workflow.instance', 3, NULL, '/home/workflow/instance', 'pages/home/workflow/instance', NULL, 5, 0, 1, 2, 1, 101, now()),
('流程申请', 'group.home.flow', 'menu.home.flow.application', 3, NULL, '/home/flow/application', 'pages/home/flow/application/index', 'flowApplication', 1, 1, 1, 2, 1, 101, now()),
('我的申请', 'group.home.flow', 'menu.home.flow.myApplication', 3, NULL, '/home/flow/myApplication', 'pages/home/flow/myApplication/index', 'myApplication', 2, 1, 1, 2, 1, 101, now()),
('我的待办', 'group.home.flow', 'menu.home.flow.myTodo', 3, NULL, '/home/flow/myTodo', 'pages/home/flow/myTodo/index', 'myTodo', 3, 1, 1, 2, 1, 101, now()),
('我的已办', 'group.home.flow', 'menu.home.flow.myDone', 3, NULL, '/home/flow/myDone', 'pages/home/flow/myDone/index', 'myDone', 4, 1, 1, 2, 1, 101, now()),
('抄送给我', 'group.home.flow', 'menu.home.flow.copyMe', 3, NULL, '/home/flow/copyMe', 'pages/home/flow/copyMe/index', 'copyMe', 5, 1, 1, 2, 1, 101, now()),
-- 系统管理按钮
('用户查询', 'menu.org.sys.user', 'menu.org.sys.user.f.user.query', 4, 'sys:user:query', NULL, NULL, NULL, 1, 1, 1, 3, 1, 101, now()),
('用户详情', 'menu.org.sys.user', 'menu.org.sys.user.f.user.detail', 4, 'sys:user:detail', NULL, NULL, NULL, 2, 1, 1, 3, 1, 101, now()),
('用户新增', 'menu.org.sys.user', 'menu.org.sys.user.f.user.create', 4, 'sys:user:create', NULL, NULL, NULL, 3, 1, 1, 3, 1, 101, now()),
('用户修改', 'menu.org.sys.user', 'menu.org.sys.user.f.user.update', 4, 'sys:user:update', NULL, NULL, NULL, 4, 1, 1, 3, 1, 101, now()),
('成员移除', 'menu.org.sys.user', 'menu.org.sys.user.f.tenantUser.remove', 4, 'sys:tenantUser:remove', NULL, NULL, NULL, 5, 1, 1, 3, 1, 101, now()),
('部门查询', 'menu.org.sys.dept', 'menu.org.sys.dept.f.dept.query', 4, 'sys:dept:query', NULL, NULL, NULL, 1, 1, 1, 3, 1, 101, now()),
('部门新增', 'menu.org.sys.dept', 'menu.org.sys.dept.f.dept.create', 4, 'sys:dept:create', NULL, NULL, NULL, 2, 1, 1, 3, 1, 101, now()),
('部门修改', 'menu.org.sys.dept', 'menu.org.sys.dept.f.dept.update', 4, 'sys:dept:update', NULL, NULL, NULL, 3, 1, 1, 3, 1, 101, now()),
('部门删除', 'menu.org.sys.dept', 'menu.org.sys.dept.f.dept.delete', 4, 'sys:dept:delete', NULL, NULL, NULL, 4, 1, 1, 3, 1, 101, now()),
('角色查询', 'menu.org.sys.role', 'menu.org.sys.role.f.role.query', 4, 'sys:role:query', NULL, NULL, NULL, 1, 1, 1, 3, 1, 101, now()),
('角色新增', 'menu.org.sys.role', 'menu.org.sys.role.f.role.create', 4, 'sys:role:create', NULL, NULL, NULL, 2, 1, 1, 3, 1, 101, now()),
('角色修改', 'menu.org.sys.role', 'menu.org.sys.role.f.role.update', 4, 'sys:role:update', NULL, NULL, NULL, 3, 1, 1, 3, 1, 101, now()),
('角色删除', 'menu.org.sys.role', 'menu.org.sys.role.f.role.delete', 4, 'sys:role:delete', NULL, NULL, NULL, 4, 1, 1, 3, 1, 101, now()),
('角色用户查询', 'menu.org.sys.role', 'menu.org.sys.role.f.roleUser.query', 4, 'sys:roleUser:query', NULL, NULL, NULL, 5, 1, 1, 3, 1, 101, now()),
('角色用户新增', 'menu.org.sys.role', 'menu.org.sys.role.f.roleUser.add', 4, 'sys:roleUser:add', NULL, NULL, NULL, 6, 1, 1, 3, 1, 101, now()),
('角色用户移除', 'menu.org.sys.role', 'menu.org.sys.role.f.roleUser.remove', 4, 'sys:roleUser:remove', NULL, NULL, NULL, 7, 1, 1, 3, 1, 101, now()),
('消息查询', 'menu.org.sys.message', 'menu.org.sys.message.f.message.query', 4, 'sys:message:query', NULL, NULL, NULL, 1, 1, 1, 3, 1, 101, now()),
('消息发送', 'menu.org.sys.message', 'menu.org.sys.message.f.message.send', 4, 'sys:message:send', NULL, NULL, NULL, 2, 1, 1, 3, 1, 101, now()),
('消息删除', 'menu.org.sys.message', 'menu.org.sys.message.f.message.delete', 4, 'sys:message:delete', NULL, NULL, NULL, 3, 1, 1, 3, 1, 101, now()),
('租户查询', 'menu.site.sys.tenant', 'menu.site.sys.tenant.f.tenant.query', 4, 'sys:tenant:query', NULL, NULL, NULL, 1, 1, 1, 1, 1, 101, now()),
('租户新增', 'menu.site.sys.tenant', 'menu.site.sys.tenant.f.tenant.create', 4, 'sys:tenant:create', NULL, NULL, NULL, 2, 1, 1, 1, 1, 101, now()),
('租户修改', 'menu.site.sys.tenant', 'menu.site.sys.tenant.f.tenant.update', 4, 'sys:tenant:update', NULL, NULL, NULL, 3, 1, 1, 1, 1, 101, now()),
('租户删除', 'menu.site.sys.tenant', 'menu.site.sys.tenant.f.tenant.delete', 4, 'sys:tenant:delete', NULL, NULL, NULL, 4, 1, 1, 1, 1, 101, now()),
('租户成员查询', 'menu.site.sys.tenant', 'menu.site.sys.tenant.f.tenantUser.query', 4, 'sys:tenantUser:query', NULL, NULL, NULL, 5, 1, 1, 1, 1, 101, now()),
('租户成员新增', 'menu.site.sys.tenant', 'menu.site.sys.tenant.f.tenantUser.add', 4, 'sys:tenantUser:add', NULL, NULL, NULL, 6, 1, 1, 1, 1, 101, now()),
('租户成员移除', 'menu.site.sys.tenant', 'menu.site.sys.tenant.f.tenantUser.remove', 4, 'sys:tenantUser:remove', NULL, NULL, NULL, 7, 1, 1, 1, 1, 101, now()),
('租户成员修改', 'menu.site.sys.tenant', 'menu.site.sys.tenant.f.tenantUser.update', 4, 'sys:tenantUser:update', NULL, NULL, NULL, 8, 1, 1, 1, 1, 101, now()),
('租户配置查询', 'menu.site.sys.tenantConfig', 'menu.site.sys.tenantConfig.f.tenantConfig.query', 4, 'sys:tenantConfig:query', NULL, NULL, NULL, 1, 1, 1, 1, 1, 101, now()),
('租户配置新增', 'menu.site.sys.tenantConfig', 'menu.site.sys.tenantConfig.f.tenantConfig.create', 4, 'sys:tenantConfig:create', NULL, NULL, NULL, 2, 1, 1, 1, 1, 101, now()),
('租户配置修改', 'menu.site.sys.tenantConfig', 'menu.site.sys.tenantConfig.f.tenantConfig.update', 4, 'sys:tenantConfig:update', NULL, NULL, NULL, 3, 1, 1, 1, 1, 101, now()),
('租户配置删除', 'menu.site.sys.tenantConfig', 'menu.site.sys.tenantConfig.f.tenantConfig.delete', 4, 'sys:tenantConfig:delete', NULL, NULL, NULL, 4, 1, 1, 1, 1, 101, now()),
('菜单查询', 'menu.site.sys.menu', 'menu.site.sys.menu.f.menu.query', 4, 'sys:menu:query', NULL, NULL, NULL, 1, 1, 1, 1, 1, 101, now()),
('菜单新增', 'menu.site.sys.menu', 'menu.site.sys.menu.f.menu.create', 4, 'sys:menu:create', NULL, NULL, NULL, 2, 1, 1, 1, 1, 101, now()),
('菜单修改', 'menu.site.sys.menu', 'menu.site.sys.menu.f.menu.update', 4, 'sys:menu:update', NULL, NULL, NULL, 3, 1, 1, 1, 1, 101, now()),
('菜单删除', 'menu.site.sys.menu', 'menu.site.sys.menu.f.menu.delete', 4, 'sys:menu:delete', NULL, NULL, NULL, 4, 1, 1, 1, 1, 101, now()),
('登录日志查询', 'menu.org.log.login', 'menu.org.log.login.f.login.query', 4, 'log:login:query', NULL, NULL, NULL, 1, 1, 1, 3, 1, 101, now()),
('强制退出', 'menu.org.log.login', 'menu.org.log.login.f.login.forceLogout', 4, 'log:login:forceLogout', NULL, NULL, NULL, 2, 1, 1, 3, 1, 101, now()),
('操作日志查询', 'menu.org.log.operate', 'menu.org.log.operate.f.operate.query', 4, 'log:operate:query', NULL, NULL, NULL, 1, 1, 1, 3, 1, 101, now()),
-- AI管理按钮
('智能体查询', 'menu.llm.agent', 'menu.llm.agent.f.agent.query', 4, 'llm:agent:query', NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
('智能体详情', 'menu.llm.agent', 'menu.llm.agent.f.agent.detail', 4, 'llm:agent:detail', NULL, NULL, NULL, 2, 1, 1, 0, 1, 101, now()),
('智能体新增', 'menu.llm.agent', 'menu.llm.agent.f.agent.create', 4, 'llm:agent:create', NULL, NULL, NULL, 3, 1, 1, 0, 1, 101, now()),
('智能体修改', 'menu.llm.agent', 'menu.llm.agent.f.agent.update', 4, 'llm:agent:update', NULL, NULL, NULL, 4, 1, 1, 0, 1, 101, now()),
('智能体删除', 'menu.llm.agent', 'menu.llm.agent.f.agent.delete', 4, 'llm:agent:delete', NULL, NULL, NULL, 5, 1, 1, 0, 1, 101, now()),
('模型查询', 'menu.llm.modelMarket', 'menu.llm.modelMarket.f.model.query', 4, 'llm:model:query', NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
('模型详情', 'menu.llm.modelMarket', 'menu.llm.modelMarket.f.model.detail', 4, 'llm:model:detail', NULL, NULL, NULL, 2, 1, 1, 0, 1, 101, now()),
('模型新增', 'menu.llm.modelMarket', 'menu.llm.modelMarket.f.model.create', 4, 'llm:model:create', NULL, NULL, NULL, 3, 1, 1, 0, 1, 101, now()),
('模型修改', 'menu.llm.modelMarket', 'menu.llm.modelMarket.f.model.update', 4, 'llm:model:update', NULL, NULL, NULL, 4, 1, 1, 0, 1, 101, now()),
('模型删除', 'menu.llm.modelMarket', 'menu.llm.modelMarket.f.model.delete', 4, 'llm:model:delete', NULL, NULL, NULL, 5, 1, 1, 0, 1, 101, now()),
('厂商查询', 'menu.llm.modelMarket', 'menu.llm.modelMarket.f.provider.query', 4, 'llm:provider:query', NULL, NULL, NULL, 6, 1, 1, 0, 1, 101, now()),
('厂商详情', 'menu.llm.modelMarket', 'menu.llm.modelMarket.f.provider.detail', 4, 'llm:provider:detail', NULL, NULL, NULL, 7, 1, 1, 0, 1, 101, now()),
('厂商新增', 'menu.llm.modelMarket', 'menu.llm.modelMarket.f.provider.create', 4, 'llm:provider:create', NULL, NULL, NULL, 8, 1, 1, 0, 1, 101, now()),
('厂商修改', 'menu.llm.modelMarket', 'menu.llm.modelMarket.f.provider.update', 4, 'llm:provider:update', NULL, NULL, NULL, 9, 1, 1, 0, 1, 101, now()),
('厂商删除', 'menu.llm.modelMarket', 'menu.llm.modelMarket.f.provider.delete', 4, 'llm:provider:delete', NULL, NULL, NULL, 10, 1, 1, 0, 1, 101, now()),
('密钥查询', 'menu.llm.modelMarket', 'menu.llm.modelMarket.f.providerKey.query', 4, 'llm:providerKey:query', NULL, NULL, NULL, 11, 1, 1, 0, 1, 101, now()),
('密钥详情', 'menu.llm.modelMarket', 'menu.llm.modelMarket.f.providerKey.detail', 4, 'llm:providerKey:detail', NULL, NULL, NULL, 12, 1, 1, 0, 1, 101, now()),
('密钥新增', 'menu.llm.modelMarket', 'menu.llm.modelMarket.f.providerKey.create', 4, 'llm:providerKey:create', NULL, NULL, NULL, 13, 1, 1, 0, 1, 101, now()),
('密钥修改', 'menu.llm.modelMarket', 'menu.llm.modelMarket.f.providerKey.update', 4, 'llm:providerKey:update', NULL, NULL, NULL, 14, 1, 1, 0, 1, 101, now()),
('密钥删除', 'menu.llm.modelMarket', 'menu.llm.modelMarket.f.providerKey.delete', 4, 'llm:providerKey:delete', NULL, NULL, NULL, 15, 1, 1, 0, 1, 101, now()),
('技能查询', 'menu.llm.skill', 'menu.llm.skill.f.skill.query', 4, 'llm:skill:query', NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
('技能详情', 'menu.llm.skill', 'menu.llm.skill.f.skill.detail', 4, 'llm:skill:detail', NULL, NULL, NULL, 2, 1, 1, 0, 1, 101, now()),
('技能新增', 'menu.llm.skill', 'menu.llm.skill.f.skill.create', 4, 'llm:skill:create', NULL, NULL, NULL, 3, 1, 1, 0, 1, 101, now()),
('技能修改', 'menu.llm.skill', 'menu.llm.skill.f.skill.update', 4, 'llm:skill:update', NULL, NULL, NULL, 4, 1, 1, 0, 1, 101, now()),
('技能删除', 'menu.llm.skill', 'menu.llm.skill.f.skill.delete', 4, 'llm:skill:delete', NULL, NULL, NULL, 5, 1, 1, 0, 1, 101, now()),
('MCP查询', 'menu.llm.mcp', 'menu.llm.mcp.f.mcp.query', 4, 'llm:mcp:query', NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
('MCP详情', 'menu.llm.mcp', 'menu.llm.mcp.f.mcp.detail', 4, 'llm:mcp:detail', NULL, NULL, NULL, 2, 1, 1, 0, 1, 101, now()),
('MCP新增', 'menu.llm.mcp', 'menu.llm.mcp.f.mcp.create', 4, 'llm:mcp:create', NULL, NULL, NULL, 3, 1, 1, 0, 1, 101, now()),
('MCP修改', 'menu.llm.mcp', 'menu.llm.mcp.f.mcp.update', 4, 'llm:mcp:update', NULL, NULL, NULL, 4, 1, 1, 0, 1, 101, now()),
('MCP删除', 'menu.llm.mcp', 'menu.llm.mcp.f.mcp.delete', 4, 'llm:mcp:delete', NULL, NULL, NULL, 5, 1, 1, 0, 1, 101, now()),
('MCP测试', 'menu.llm.mcp', 'menu.llm.mcp.f.mcp.test', 4, 'llm:mcp:test', NULL, NULL, NULL, 6, 1, 1, 0, 1, 101, now()),
('MCP工具列表', 'menu.llm.mcp', 'menu.llm.mcp.f.mcp.listTools', 4, 'llm:mcp:listTools', NULL, NULL, NULL, 7, 1, 1, 0, 1, 101, now()),
('MCP令牌新增', 'menu.llm.mcp', 'menu.llm.mcp.f.mcpKey.create', 4, 'llm:mcpKey:create', NULL, NULL, NULL, 8, 1, 1, 0, 1, 101, now()),
('MCP令牌删除', 'menu.llm.mcp', 'menu.llm.mcp.f.mcpKey.delete', 4, 'llm:mcpKey:delete', NULL, NULL, NULL, 9, 1, 1, 0, 1, 101, now()),
('MCP令牌查询', 'menu.llm.mcp', 'menu.llm.mcp.f.mcpKey.query', 4, 'llm:mcpKey:query', NULL, NULL, NULL, 10, 1, 1, 0, 1, 101, now()),
('模型用量查询', 'menu.llm.modelUsage', 'menu.llm.modelUsage.f.modelUsage.query', 4, 'llm:modelUsage:query', NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
-- 知识库按钮
('知识库查询', 'menu.llm.knowledge', 'menu.llm.knowledge.f.knowledge.query', 4, 'kb:knowledge:query', NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
('知识库详情', 'menu.llm.knowledge', 'menu.llm.knowledge.f.knowledge.detail', 4, 'kb:knowledge:detail', NULL, NULL, NULL, 2, 1, 1, 0, 1, 101, now()),
('知识库新增', 'menu.llm.knowledge', 'menu.llm.knowledge.f.knowledge.create', 4, 'kb:knowledge:create', NULL, NULL, NULL, 3, 1, 1, 0, 1, 101, now()),
('知识库修改', 'menu.llm.knowledge', 'menu.llm.knowledge.f.knowledge.update', 4, 'kb:knowledge:update', NULL, NULL, NULL, 4, 1, 1, 0, 1, 101, now()),
('知识库删除', 'menu.llm.knowledge', 'menu.llm.knowledge.f.knowledge.delete', 4, 'kb:knowledge:delete', NULL, NULL, NULL, 5, 1, 1, 0, 1, 101, now()),
-- 知识图谱按钮
('图谱查询', 'menu.llm.graph', 'menu.llm.graph.f.graph.query', 4, 'kg:graph:query', NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
('图谱详情', 'menu.llm.graph', 'menu.llm.graph.f.graph.detail', 4, 'kg:graph:detail', NULL, NULL, NULL, 2, 1, 1, 0, 1, 101, now()),
('图谱新增', 'menu.llm.graph', 'menu.llm.graph.f.graph.create', 4, 'kg:graph:create', NULL, NULL, NULL, 3, 1, 1, 0, 1, 101, now()),
('图谱修改', 'menu.llm.graph', 'menu.llm.graph.f.graph.update', 4, 'kg:graph:update', NULL, NULL, NULL, 4, 1, 1, 0, 1, 101, now()),
('图谱删除', 'menu.llm.graph', 'menu.llm.graph.f.graph.delete', 4, 'kg:graph:delete', NULL, NULL, NULL, 5, 1, 1, 0, 1, 101, now()),
('图谱节点展开', 'menu.llm.graph', 'menu.llm.graph.f.graph.expand', 4, 'kg:graph:expand', NULL, NULL, NULL, 6, 1, 1, 0, 1, 101, now()),
('图谱统计', 'menu.llm.graph', 'menu.llm.graph.f.graph.stats', 4, 'kg:graph:stats', NULL, NULL, NULL, 7, 1, 1, 0, 1, 101, now()),
('图谱推理', 'menu.llm.graph', 'menu.llm.graph.f.search.reason', 4, 'kg:search:reason', NULL, NULL, NULL, 8, 1, 1, 0, 1, 101, now()),
('最短路径', 'menu.llm.graph', 'menu.llm.graph.f.search.path', 4, 'kg:search:path', NULL, NULL, NULL, 9, 1, 1, 0, 1, 101, now()),
('实体查询', 'menu.llm.graph.entity', 'menu.llm.graph.entity.f.entity.query', 4, 'kg:entity:query', NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
('实体详情', 'menu.llm.graph.entity', 'menu.llm.graph.entity.f.entity.detail', 4, 'kg:entity:detail', NULL, NULL, NULL, 2, 1, 1, 0, 1, 101, now()),
('实体新增', 'menu.llm.graph.entity', 'menu.llm.graph.entity.f.entity.create', 4, 'kg:entity:create', NULL, NULL, NULL, 3, 1, 1, 0, 1, 101, now()),
('实体修改', 'menu.llm.graph.entity', 'menu.llm.graph.entity.f.entity.update', 4, 'kg:entity:update', NULL, NULL, NULL, 4, 1, 1, 0, 1, 101, now()),
('实体删除', 'menu.llm.graph.entity', 'menu.llm.graph.entity.f.entity.delete', 4, 'kg:entity:delete', NULL, NULL, NULL, 5, 1, 1, 0, 1, 101, now()),
('实体合并', 'menu.llm.graph.entity', 'menu.llm.graph.entity.f.entity.merge', 4, 'kg:entity:merge', NULL, NULL, NULL, 6, 1, 1, 0, 1, 101, now()),
('关系查询', 'menu.llm.graph.relation', 'menu.llm.graph.relation.f.relation.query', 4, 'kg:relation:query', NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
('关系详情', 'menu.llm.graph.relation', 'menu.llm.graph.relation.f.relation.detail', 4, 'kg:relation:detail', NULL, NULL, NULL, 2, 1, 1, 0, 1, 101, now()),
('关系新增', 'menu.llm.graph.relation', 'menu.llm.graph.relation.f.relation.create', 4, 'kg:relation:create', NULL, NULL, NULL, 3, 1, 1, 0, 1, 101, now()),
('关系修改', 'menu.llm.graph.relation', 'menu.llm.graph.relation.f.relation.update', 4, 'kg:relation:update', NULL, NULL, NULL, 4, 1, 1, 0, 1, 101, now()),
('关系删除', 'menu.llm.graph.relation', 'menu.llm.graph.relation.f.relation.delete', 4, 'kg:relation:delete', NULL, NULL, NULL, 5, 1, 1, 0, 1, 101, now()),
-- 知识库文档按钮，文档组件知识库与图谱共用，两处各授一份同 perms
('文档查询', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.document.query', 4, 'dms:document:query', NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
('文档详情', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.document.detail', 4, 'dms:document:detail', NULL, NULL, NULL, 2, 1, 1, 0, 1, 101, now()),
('文档修改', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.document.update', 4, 'dms:document:update', NULL, NULL, NULL, 3, 1, 1, 0, 1, 101, now()),
('文档删除', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.document.delete', 4, 'dms:document:delete', NULL, NULL, NULL, 4, 1, 1, 0, 1, 101, now()),
('文档下载', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.document.download', 4, 'dms:document:download', NULL, NULL, NULL, 5, 1, 1, 0, 1, 101, now()),
('文档重置事件', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.document.resetEvent', 4, 'dms:document:resetEvent', NULL, NULL, NULL, 6, 1, 1, 0, 1, 101, now()),
('分片查询', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.chunk.query', 4, 'dms:chunk:query', NULL, NULL, NULL, 7, 1, 1, 0, 1, 101, now()),
('分片统计', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.chunk.stats', 4, 'dms:chunk:stats', NULL, NULL, NULL, 8, 1, 1, 0, 1, 101, now()),
('分片重切', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.chunk.rechunk', 4, 'dms:chunk:rechunk', NULL, NULL, NULL, 9, 1, 1, 0, 1, 101, now()),
('分片修改', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.chunk.update', 4, 'dms:chunk:update', NULL, NULL, NULL, 10, 1, 1, 0, 1, 101, now()),
('版本查询', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.version.query', 4, 'dms:version:query', NULL, NULL, NULL, 11, 1, 1, 0, 1, 101, now()),
('版本下载', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.version.download', 4, 'dms:version:download', NULL, NULL, NULL, 12, 1, 1, 0, 1, 101, now()),
('版本回滚', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.version.rollback', 4, 'dms:version:rollback', NULL, NULL, NULL, 13, 1, 1, 0, 1, 101, now()),
('版本删除', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.version.delete', 4, 'dms:version:delete', NULL, NULL, NULL, 14, 1, 1, 0, 1, 101, now()),
('附件详情', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.attachment.detail', 4, 'dms:attachment:detail', NULL, NULL, NULL, 15, 1, 1, 0, 1, 101, now()),
('附件下载', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.attachment.download', 4, 'dms:attachment:download', NULL, NULL, NULL, 16, 1, 1, 0, 1, 101, now()),
('文件上传', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.upload.create', 4, 'dms:upload:create', NULL, NULL, NULL, 17, 1, 1, 0, 1, 101, now()),
('上传进度查询', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.upload.query', 4, 'dms:upload:query', NULL, NULL, NULL, 18, 1, 1, 0, 1, 101, now()),
('文档事件详情', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.event.detail', 4, 'dms:event:detail', NULL, NULL, NULL, 19, 1, 1, 0, 1, 101, now()),
('文档事件修改', 'menu.llm.knowledge.document', 'menu.llm.knowledge.document.f.event.update', 4, 'dms:event:update', NULL, NULL, NULL, 20, 1, 1, 0, 1, 101, now()),
-- 图谱文档按钮，与知识库文档同 perms
('文档查询', 'menu.llm.graph.document', 'menu.llm.graph.document.f.document.query', 4, 'dms:document:query', NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
('文档详情', 'menu.llm.graph.document', 'menu.llm.graph.document.f.document.detail', 4, 'dms:document:detail', NULL, NULL, NULL, 2, 1, 1, 0, 1, 101, now()),
('文档修改', 'menu.llm.graph.document', 'menu.llm.graph.document.f.document.update', 4, 'dms:document:update', NULL, NULL, NULL, 3, 1, 1, 0, 1, 101, now()),
('文档删除', 'menu.llm.graph.document', 'menu.llm.graph.document.f.document.delete', 4, 'dms:document:delete', NULL, NULL, NULL, 4, 1, 1, 0, 1, 101, now()),
('文档下载', 'menu.llm.graph.document', 'menu.llm.graph.document.f.document.download', 4, 'dms:document:download', NULL, NULL, NULL, 5, 1, 1, 0, 1, 101, now()),
('文档重置事件', 'menu.llm.graph.document', 'menu.llm.graph.document.f.document.resetEvent', 4, 'dms:document:resetEvent', NULL, NULL, NULL, 6, 1, 1, 0, 1, 101, now()),
('分片查询', 'menu.llm.graph.document', 'menu.llm.graph.document.f.chunk.query', 4, 'dms:chunk:query', NULL, NULL, NULL, 7, 1, 1, 0, 1, 101, now()),
('分片统计', 'menu.llm.graph.document', 'menu.llm.graph.document.f.chunk.stats', 4, 'dms:chunk:stats', NULL, NULL, NULL, 8, 1, 1, 0, 1, 101, now()),
('分片重切', 'menu.llm.graph.document', 'menu.llm.graph.document.f.chunk.rechunk', 4, 'dms:chunk:rechunk', NULL, NULL, NULL, 9, 1, 1, 0, 1, 101, now()),
('分片修改', 'menu.llm.graph.document', 'menu.llm.graph.document.f.chunk.update', 4, 'dms:chunk:update', NULL, NULL, NULL, 10, 1, 1, 0, 1, 101, now()),
('版本查询', 'menu.llm.graph.document', 'menu.llm.graph.document.f.version.query', 4, 'dms:version:query', NULL, NULL, NULL, 11, 1, 1, 0, 1, 101, now()),
('版本下载', 'menu.llm.graph.document', 'menu.llm.graph.document.f.version.download', 4, 'dms:version:download', NULL, NULL, NULL, 12, 1, 1, 0, 1, 101, now()),
('版本回滚', 'menu.llm.graph.document', 'menu.llm.graph.document.f.version.rollback', 4, 'dms:version:rollback', NULL, NULL, NULL, 13, 1, 1, 0, 1, 101, now()),
('版本删除', 'menu.llm.graph.document', 'menu.llm.graph.document.f.version.delete', 4, 'dms:version:delete', NULL, NULL, NULL, 14, 1, 1, 0, 1, 101, now()),
('附件详情', 'menu.llm.graph.document', 'menu.llm.graph.document.f.attachment.detail', 4, 'dms:attachment:detail', NULL, NULL, NULL, 15, 1, 1, 0, 1, 101, now()),
('附件下载', 'menu.llm.graph.document', 'menu.llm.graph.document.f.attachment.download', 4, 'dms:attachment:download', NULL, NULL, NULL, 16, 1, 1, 0, 1, 101, now()),
('文件上传', 'menu.llm.graph.document', 'menu.llm.graph.document.f.upload.create', 4, 'dms:upload:create', NULL, NULL, NULL, 17, 1, 1, 0, 1, 101, now()),
('上传进度查询', 'menu.llm.graph.document', 'menu.llm.graph.document.f.upload.query', 4, 'dms:upload:query', NULL, NULL, NULL, 18, 1, 1, 0, 1, 101, now()),
('文档事件详情', 'menu.llm.graph.document', 'menu.llm.graph.document.f.event.detail', 4, 'dms:event:detail', NULL, NULL, NULL, 19, 1, 1, 0, 1, 101, now()),
('文档事件修改', 'menu.llm.graph.document', 'menu.llm.graph.document.f.event.update', 4, 'dms:event:update', NULL, NULL, NULL, 20, 1, 1, 0, 1, 101, now()),
-- 流程模板按钮，管理挂业务菜单、浏览挂全员流程申请
('流程模板新增', 'menu.bus.flow', 'menu.bus.flow.f.template.create', 4, 'flow:template:create', NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
('流程模板修改', 'menu.bus.flow', 'menu.bus.flow.f.template.update', 4, 'flow:template:update', NULL, NULL, NULL, 2, 1, 1, 0, 1, 101, now()),
('流程模板删除', 'menu.bus.flow', 'menu.bus.flow.f.template.delete', 4, 'flow:template:delete', NULL, NULL, NULL, 3, 1, 1, 0, 1, 101, now()),
('模板版本新增', 'menu.bus.flow', 'menu.bus.flow.f.templateVersion.create', 4, 'flow:templateVersion:create', NULL, NULL, NULL, 4, 1, 1, 0, 1, 101, now()),
('模板版本详情', 'menu.bus.flow', 'menu.bus.flow.f.templateVersion.detail', 4, 'flow:templateVersion:detail', NULL, NULL, NULL, 5, 1, 1, 0, 1, 101, now()),
('流程模板查询', 'menu.home.flow.application', 'menu.home.flow.application.f.template.query', 4, 'flow:template:query', NULL, NULL, NULL, 1, 1, 1, 2, 1, 101, now()),
('流程模板详情', 'menu.home.flow.application', 'menu.home.flow.application.f.template.detail', 4, 'flow:template:detail', NULL, NULL, NULL, 2, 1, 1, 2, 1, 101, now()),
-- 流程实例按钮，管理挂业务隐藏页、个人操作挂全员首页流程菜单
('流程实例查询', 'menu.bus.flow.instance', 'menu.bus.flow.instance.f.instance.query', 4, 'flow:instance:query', NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
('流程实例管理员审批', 'menu.bus.flow.instance', 'menu.bus.flow.instance.f.instance.adminApproval', 4, 'flow:instance:adminApproval', NULL, NULL, NULL, 2, 1, 1, 0, 1, 101, now()),
('流程实例替换审批人', 'menu.bus.flow.instance', 'menu.bus.flow.instance.f.instance.replaceAssignee', 4, 'flow:instance:replaceAssignee', NULL, NULL, NULL, 3, 1, 1, 0, 1, 101, now()),
('流程申请创建', 'menu.home.flow.application', 'menu.home.flow.application.f.instance.create', 4, 'flow:instance:create', NULL, NULL, NULL, 3, 1, 1, 2, 1, 101, now()),
('流程实例详情', 'menu.home.flow.application', 'menu.home.flow.application.f.instance.detail', 4, 'flow:instance:detail', NULL, NULL, NULL, 4, 1, 1, 2, 1, 101, now()),
('流程审批', 'menu.home.flow.myTodo', 'menu.home.flow.myTodo.f.instance.approve', 4, 'flow:instance:approve', NULL, NULL, NULL, 1, 1, 1, 2, 1, 101, now()),
('流程转办', 'menu.home.flow.myTodo', 'menu.home.flow.myTodo.f.instance.transfer', 4, 'flow:instance:transfer', NULL, NULL, NULL, 2, 1, 1, 2, 1, 101, now()),
('流程加签', 'menu.home.flow.myTodo', 'menu.home.flow.myTodo.f.instance.addSign', 4, 'flow:instance:addSign', NULL, NULL, NULL, 3, 1, 1, 2, 1, 101, now()),
('流程催办', 'menu.home.flow.myApplication', 'menu.home.flow.myApplication.f.instance.urge', 4, 'flow:instance:urge', NULL, NULL, NULL, 1, 1, 1, 2, 1, 101, now()),
('流程撤回', 'menu.home.flow.myApplication', 'menu.home.flow.myApplication.f.instance.recall', 4, 'flow:instance:recall', NULL, NULL, NULL, 2, 1, 1, 2, 1, 101, now()),
('流程抄送', 'menu.home.flow.myApplication', 'menu.home.flow.myApplication.f.instance.copy', 4, 'flow:instance:copy', NULL, NULL, NULL, 3, 1, 1, 2, 1, 101, now()),
-- 工作流按钮，管理挂AI工作流菜单、浏览运行挂全员首页工作流
('工作流新增', 'menu.llm.workflow', 'menu.llm.workflow.f.def.create', 4, 'workflow:def:create', NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
('工作流修改', 'menu.llm.workflow', 'menu.llm.workflow.f.def.update', 4, 'workflow:def:update', NULL, NULL, NULL, 2, 1, 1, 0, 1, 101, now()),
('工作流删除', 'menu.llm.workflow', 'menu.llm.workflow.f.def.delete', 4, 'workflow:def:delete', NULL, NULL, NULL, 3, 1, 1, 0, 1, 101, now()),
('工作流版本保存', 'menu.llm.workflow', 'menu.llm.workflow.f.version.save', 4, 'workflow:version:save', NULL, NULL, NULL, 4, 1, 1, 0, 1, 101, now()),
('工作流版本详情', 'menu.llm.workflow', 'menu.llm.workflow.f.version.detail', 4, 'workflow:version:detail', NULL, NULL, NULL, 5, 1, 1, 0, 1, 101, now()),
('工作流查询', 'menu.home.workflow', 'menu.home.workflow.f.def.query', 4, 'workflow:def:query', NULL, NULL, NULL, 1, 1, 1, 2, 1, 101, now()),
('工作流详情', 'menu.home.workflow', 'menu.home.workflow.f.def.detail', 4, 'workflow:def:detail', NULL, NULL, NULL, 2, 1, 1, 2, 1, 101, now()),
('工作流实例详情', 'menu.home.workflow', 'menu.home.workflow.f.instance.detail', 4, 'workflow:instance:detail', NULL, NULL, NULL, 3, 1, 1, 2, 1, 101, now()),
('工作流运行', 'menu.home.workflow', 'menu.home.workflow.f.instance.run', 4, 'workflow:instance:run', NULL, NULL, NULL, 4, 1, 1, 2, 1, 101, now()),
('工作流实例查询', 'menu.llm.workflow.instance', 'menu.llm.workflow.instance.f.instance.query', 4, 'workflow:instance:query', NULL, NULL, NULL, 1, 1, 1, 0, 1, 101, now()),
('工作流审核', 'menu.home.workflow.review', 'menu.home.workflow.review.f.instance.review', 4, 'workflow:instance:review', NULL, NULL, NULL, 1, 1, 1, 2, 1, 101, now()),
-- 表单按钮，管理挂业务流程模板、渲染与数据挂全员流程申请
('表单新增', 'menu.bus.flow', 'menu.bus.flow.f.form.create', 4, 'form:form:create', NULL, NULL, NULL, 6, 1, 1, 0, 1, 101, now()),
('表单修改', 'menu.bus.flow', 'menu.bus.flow.f.form.update', 4, 'form:form:update', NULL, NULL, NULL, 7, 1, 1, 0, 1, 101, now()),
('表单删除', 'menu.bus.flow', 'menu.bus.flow.f.form.delete', 4, 'form:form:delete', NULL, NULL, NULL, 8, 1, 1, 0, 1, 101, now()),
('表单设计保存', 'menu.bus.flow', 'menu.bus.flow.f.form.saveJson', 4, 'form:form:saveJson', NULL, NULL, NULL, 9, 1, 1, 0, 1, 101, now()),
('表单版本查询', 'menu.bus.flow', 'menu.bus.flow.f.version.query', 4, 'form:version:query', NULL, NULL, NULL, 10, 1, 1, 0, 1, 101, now()),
('表单查询', 'menu.home.flow.application', 'menu.home.flow.application.f.form.query', 4, 'form:form:query', NULL, NULL, NULL, 5, 1, 1, 2, 1, 101, now()),
('表单字段查询', 'menu.home.flow.application', 'menu.home.flow.application.f.field.query', 4, 'form:field:query', NULL, NULL, NULL, 6, 1, 1, 2, 1, 101, now()),
('表单数据保存', 'menu.home.flow.application', 'menu.home.flow.application.f.value.save', 4, 'form:value:save', NULL, NULL, NULL, 7, 1, 1, 2, 1, 101, now()),
('表单数据详情', 'menu.home.flow.application', 'menu.home.flow.application.f.value.detail', 4, 'form:value:detail', NULL, NULL, NULL, 8, 1, 1, 2, 1, 101, now()),
-- 即时通讯按钮，全员公共
('聊天消息查询', 'menu.home.aichat', 'menu.home.aichat.f.msg.query', 4, 'chat:msg:query', NULL, NULL, NULL, 1, 1, 1, 2, 1, 101, now()),
('会话创建', 'menu.home.aichat', 'menu.home.aichat.f.space.create', 4, 'chat:space:create', NULL, NULL, NULL, 2, 1, 1, 2, 1, 101, now()),
('AI会话创建', 'menu.home.aichat', 'menu.home.aichat.f.space.aiCreate', 4, 'chat:space:aiCreate', NULL, NULL, NULL, 3, 1, 1, 2, 1, 101, now()),
('会话重命名', 'menu.home.aichat', 'menu.home.aichat.f.space.update', 4, 'chat:space:update', NULL, NULL, NULL, 4, 1, 1, 2, 1, 101, now()),
('会话删除', 'menu.home.aichat', 'menu.home.aichat.f.space.delete', 4, 'chat:space:delete', NULL, NULL, NULL, 5, 1, 1, 2, 1, 101, now()),
-- 通讯录与智能体首页按钮，全员公共
('聊天消息查询', 'menu.home.contact', 'menu.home.contact.f.msg.query', 4, 'chat:msg:query', NULL, NULL, NULL, 1, 1, 1, 2, 1, 101, now()),
('会话创建', 'menu.home.contact', 'menu.home.contact.f.space.create', 4, 'chat:space:create', NULL, NULL, NULL, 2, 1, 1, 2, 1, 101, now()),
('智能体查询', 'menu.home.agent', 'menu.home.agent.f.agent.query', 4, 'llm:agent:query', NULL, NULL, NULL, 1, 1, 1, 2, 1, 101, now()),
('聊天消息查询', 'menu.home.agent', 'menu.home.agent.f.msg.query', 4, 'chat:msg:query', NULL, NULL, NULL, 2, 1, 1, 2, 1, 101, now()),
('AI会话创建', 'menu.home.agent', 'menu.home.agent.f.space.aiCreate', 4, 'chat:space:aiCreate', NULL, NULL, NULL, 3, 1, 1, 2, 1, 101, now()),
('会话重命名', 'menu.home.agent', 'menu.home.agent.f.space.update', 4, 'chat:space:update', NULL, NULL, NULL, 4, 1, 1, 2, 1, 101, now()),
('会话删除', 'menu.home.agent', 'menu.home.agent.f.space.delete', 4, 'chat:space:delete', NULL, NULL, NULL, 5, 1, 1, 2, 1, 101, now()),
-- 定时任务按钮，归租户业务菜单按角色授权
('任务实例查询', 'menu.bus.flow.instance', 'menu.bus.flow.instance.f.taskInstance.query', 4, 'task:instance:query', NULL, NULL, NULL, 4, 1, 1, 0, 1, 101, now()),
('任务实例详情', 'menu.bus.flow.instance', 'menu.bus.flow.instance.f.taskInstance.detail', 4, 'task:instance:detail', NULL, NULL, NULL, 5, 1, 1, 0, 1, 101, now()),
('任务模板查询', 'menu.bus.flow', 'menu.bus.flow.f.taskTemplate.query', 4, 'task:template:query', NULL, NULL, NULL, 11, 1, 1, 0, 1, 101, now()),
('任务模板详情', 'menu.bus.flow', 'menu.bus.flow.f.taskTemplate.detail', 4, 'task:template:detail', NULL, NULL, NULL, 12, 1, 1, 0, 1, 101, now()),
('任务模板新增', 'menu.bus.flow', 'menu.bus.flow.f.taskTemplate.create', 4, 'task:template:create', NULL, NULL, NULL, 13, 1, 1, 0, 1, 101, now()),
('任务模板修改', 'menu.bus.flow', 'menu.bus.flow.f.taskTemplate.update', 4, 'task:template:update', NULL, NULL, NULL, 14, 1, 1, 0, 1, 101, now()),
('任务模板删除', 'menu.bus.flow', 'menu.bus.flow.f.taskTemplate.delete', 4, 'task:template:delete', NULL, NULL, NULL, 15, 1, 1, 0, 1, 101, now()),
('任务参数查询', 'menu.bus.flow', 'menu.bus.flow.f.taskTemplateParam.query', 4, 'task:templateParam:query', NULL, NULL, NULL, 16, 1, 1, 0, 1, 101, now()),
('任务参数新增', 'menu.bus.flow', 'menu.bus.flow.f.taskTemplateParam.create', 4, 'task:templateParam:create', NULL, NULL, NULL, 17, 1, 1, 0, 1, 101, now()),
('任务参数修改', 'menu.bus.flow', 'menu.bus.flow.f.taskTemplateParam.update', 4, 'task:templateParam:update', NULL, NULL, NULL, 18, 1, 1, 0, 1, 101, now()),
('任务参数删除', 'menu.bus.flow', 'menu.bus.flow.f.taskTemplateParam.delete', 4, 'task:templateParam:delete', NULL, NULL, NULL, 19, 1, 1, 0, 1, 101, now()),
-- 站点监控按钮，仅站点管理员
('接口监控查询', 'menu.site.monitor.api', 'menu.site.monitor.api.f.api.query', 4, 'monitor:api:query', NULL, NULL, NULL, 12, 1, 1, 1, 1, 101, now()),
('消息队列监控查询', 'menu.site.monitor.mq', 'menu.site.monitor.mq.f.mq.query', 4, 'monitor:mq:query', NULL, NULL, NULL, 1, 1, 1, 1, 1, 101, now()),
('节点查询', 'menu.site.monitor.server', 'menu.site.monitor.server.f.node.query', 4, 'monitor:node:query', NULL, NULL, NULL, 1, 1, 1, 1, 1, 101, now()),
('缓存监控查询', 'menu.site.monitor.cache', 'menu.site.monitor.cache.f.redis.query', 4, 'monitor:redis:query', NULL, NULL, NULL, 1, 1, 1, 1, 1, 101, now()),
('服务器监控查询', 'menu.site.monitor.server', 'menu.site.monitor.server.f.server.query', 4, 'monitor:server:query', NULL, NULL, NULL, 2, 1, 1, 1, 1, 101, now()),
-- 工作台按钮，全员公共
('工作台概览', 'menu.home.dashboard', 'menu.home.dashboard.f.dashboard.query', 4, 'home:dashboard:query', NULL, NULL, NULL, 1, 1, 1, 2, 1, 101, now());

-- 演示租户全量授租户/组织菜单开箱可用，站点管理员后续按需收窄
INSERT INTO sys_tenant_menu (tenant_id, menu_id, delete_flag, created_by, created_dt)
SELECT 103, m.id, 1, 101, now()
FROM sys_menu m
WHERE m.platform IN (0, 3) AND m.delete_flag = 1 AND m.status = 1;

INSERT INTO `llm_skill` (`id`, `tenant_id`, `name`, `description`, `content`, `status`, `dept_id`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (1, 103, 'sql编写规范', '在写sql时候必须使用此规范', '---\nname: sql-writing-guide\ndescription: 当用户要求编写、优化或审查 MySQL 8 查询语句时命中。强制统一的命名规范、分页写法、JOIN 与索引要求，输出可直接执行的 SQL 并附设计说明。\n---\n\n## 执行流程\n\n1. 明确用户的查询目标：涉及的表、过滤条件、返回字段、是否需要分页或排序。\n2. 信息不足时先向用户确认（表结构、数据量级、是否允许全表扫描），不要凭空假设表名与字段名。\n3. 按以下规则编写 SQL，具体细则按需读取对应子文件：\n   - 表名、字段名、别名、索引名的命名要求 → 读取 `references/naming-convention.md`\n   - 分页、JOIN、子查询、函数使用等编写规则与禁止项 → 读取 `references/query-rules.md`\n   - 常见场景（分页列表、树形查询、批量操作等）的可复用模板 → 读取 `scripts/query-templates.sql`\n   - 评估 SQL 是否走索引或给出加索引建议时 → 读取 `references/index-guide.md`\n4. 输出 SQL 后必须附「设计说明」，包含：\n   - 关键写法的选择理由（如为什么用该 JOIN 方式）\n   - 预计命中的索引；如无法命中索引，必须显式提示全表扫描风险\n5. 审查用户提供的既有 SQL 时，按上述 references 逐条核对，输出「违规项清单 + 修正后的 SQL」。\n\n## 硬性要求\n\n- 所有 SQL 以 MySQL 8.0 语法为准，禁止使用 MySQL 8 已移除的特性。\n- 禁止 `SELECT *`，必须列出明确字段。\n- 分页必须包含 `ORDER BY`，且排序字段需有索引或说明无法走索引的原因。\n- 涉及多表时必须为每张表指定别名，别名规则见 `references/naming-convention.md`。\n- 不杜撰表名与字段名；用户未提供表结构时，先要求提供或用注释占位并明确标注。\n\n## 输出格式\n\n1. SQL 代码块（带必要的中文注释）\n2. 设计说明（要点列表）\n3. 如有风险或待确认项，单独列出「注意事项」\n', 1, 102, 1, 101, '2026-09-05 18:38:42', 201, '2026-10-15 11:07:14');
INSERT INTO `llm_skill_file` (`id`, `tenant_id`, `skill_id`, `file_path`, `description`, `content`, `sort`, `dept_id`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (1, 103, 1, 'references/index-guide.md', '索引设计与命中判断', '# 索引设计与命中判断\n\n## 判断方法\n\n- 用 `EXPLAIN` 思路评估：type 至少达到 `range`，`ALL`（全表扫描）必须显式提示。\n- 联合索引遵循最左前缀：索引 `(a, b, c)` 可命中 `a`、`a,b`、`a,b,c` 前缀条件。\n- `ORDER BY` 字段与 `WHERE` 条件共用联合索引时可避免 filesort，例如 `WHERE a = ? ORDER BY b` 适用索引 `(a, b)`。\n\n## 建索引建议的输出格式\n\n给出加索引建议时，直接输出可执行的 DDL：\n\n```sql\nALTER TABLE order_info ADD INDEX idx_user_id_created_dt (user_id, created_dt);\n```\n\n并说明：该索引服务哪些查询、是否会造成已有查询的索引选择变化、写入开销提示。\n\n## 常见失效场景\n\n- 字段上套函数或做隐式类型转换（字符串字段传数字）。\n- `LIKE` 前缀通配、`OR` 两侧有一侧无索引、联合索引跳过最左列。\n- 数据区分度过低的字段（如 status 仅 2~3 个值）单独建索引意义不大，应做联合索引首列需高区分度。\n', 1, 902, 1, 201, '2026-10-15 11:07:14', 201, '2026-10-15 11:07:14');
INSERT INTO `llm_skill_file` (`id`, `tenant_id`, `skill_id`, `file_path`, `description`, `content`, `sort`, `dept_id`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (2, 103, 1, 'references/naming-convention.md', '命名规范', '# 命名规范\n\n## 表名与字段名\n\n- 一律小写蛇形命名：`user_info`、`created_dt`。\n- 表名为业务域名词，禁止拼音、禁止复数后缀。\n- 主键统一为 `id`；外键引用写 `xxx_id`。\n- 时间字段：日期时间用 `_dt` 后缀（如 `created_dt`），纯日期用 `_date` 后缀。\n- 布尔/标识字段用 `is_` 或 `_flag` 后缀（如 `delete_flag`），值为 `1` / `-1` 时须与平台约定一致。\n- 金额、数量等数值字段须注明单位倾向（如 `_amount` 分、`_count` 次数）。\n\n## 别名规则\n\n- 多表查询必须为每张表取别名：主表用业务缩写（如 `user_info` → `ui`），关联表按语义缩写（如 `dept` → `d`）。\n- 别名长度不超过 4 个字母，同一个 SQL 中不得重复。\n- 输出字段若与其他表字段同名，必须用别名表前缀引用（`ui.name`），避免歧义列。\n\n## 索引名\n\n- 普通索引：`idx_字段名`，如 `idx_created_dt`。\n- 联合索引：`idx_首字段_次字段`，如 `idx_user_id_status`。\n- 唯一索引：`uk_字段名`，如 `uk_username`。\n\n## SQL 书写风格\n\n- 关键字大写（`SELECT`、`FROM`、`WHERE`），表名字段名小写。\n- 每行一个字段 / 一个条件，子句换行对齐：\n\n```sql\nSELECT ui.id,\n       ui.name,\n       d.dept_name\nFROM user_info ui\nLEFT JOIN dept d ON d.id = ui.dept_id\nWHERE ui.status = 1\nORDER BY ui.id DESC\nLIMIT 0, 20;\n```\n', 2, 902, 1, 201, '2026-10-15 11:07:14', 201, '2026-10-15 11:07:14');
INSERT INTO `llm_skill_file` (`id`, `tenant_id`, `skill_id`, `file_path`, `description`, `content`, `sort`, `dept_id`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (3, 103, 1, 'references/query-rules.md', '查询编写规则', '# 查询编写规则\n\n## 分页\n\n- 统一使用 `LIMIT offset, size`，且必须带 `ORDER BY`；无稳定排序字段时补 `ORDER BY id`。\n- 深度分页（offset > 10000）须改用「游标分页」：`WHERE id < #{lastId} ORDER BY id DESC LIMIT size`，并向用户说明改动原因。\n- 需要总数时单独写 `COUNT` 查询，不用 `SQL_CALC_FOUND_ROWS`（MySQL 8 已废弃）。\n\n## JOIN\n\n- 多表关联不超过 3 张；超过时建议拆分查询或改冗余字段，并向用户说明取舍。\n- 关联条件字段必须有索引；无索引时显式提示风险。\n- 只取「是否存在」语义用 `EXISTS`，不用 `LEFT JOIN ... IS NULL` 或 `IN` 大列表。\n- `IN` 列表元素不超过 1000 个，超过建议分批或临时表。\n\n## 子查询与函数\n\n- WHERE 条件中的字段禁止套函数（如 `DATE(created_dt) = \'2026-01-01\'`），改为范围条件（`created_dt >= \'2026-01-01\' AND created_dt < \'2026-01-02\'`）以保证走索引。\n- 禁止 `SELECT *`；禁止 `ORDER BY RAND()`；禁止 `NOT IN`（可能含 NULL 导致结果异常），用 `NOT EXISTS` 替代。\n- 模糊查询避免前缀 `%`（`LIKE \'%abc\'` 无法走索引），如业务必须前缀模糊须提示全表扫描风险。\n\n## 写入与事务\n\n- 更新/删除必须带 `WHERE` 条件，且条件字段优先走唯一索引；无条件全表改一律拒绝输出并提醒。\n- 批量插入用多值 `INSERT INTO ... VALUES (...), (...)`，单批建议不超过 500 行。\n- 涉及多步写入时，提示应用层需要事务包裹，技能本身不生成存储过程。\n\n## 审查清单\n\n审查既有 SQL 时按顺序核对：禁止项 → 索引命中 → 分页写法 → 命名规范 → 书写风格，逐条输出违规项并给出修正 SQL。\n', 3, 902, 1, 201, '2026-10-15 11:07:14', 201, '2026-10-15 11:07:14');
INSERT INTO `llm_skill_file` (`id`, `tenant_id`, `skill_id`, `file_path`, `description`, `content`, `sort`, `dept_id`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (4, 103, 1, 'scripts/query-templates.sql', '本文件为参考模板，不会在服务端执行；使用时替换表名、字段名与条件。', '-- 注意：本文件为参考模板，不会在服务端执行；使用时替换表名、字段名与条件。\n\n-- 1. 普通分页列表\nSELECT ui.id,\n       ui.name,\n       ui.status,\n       ui.created_dt\nFROM user_info ui\nWHERE ui.status = 1\n  AND ui.delete_flag = 1\nORDER BY ui.id DESC\nLIMIT 0, 20;\n\n-- 2. 游标分页\nSELECT ui.id,\n       ui.name\nFROM user_info ui\nWHERE ui.id < #{lastId}\n  AND ui.status = 1\nORDER BY ui.id DESC\nLIMIT 20;\n\n-- 3. 存在性判断\nSELECT CASE WHEN EXISTS (\n    SELECT 1\n    FROM user_info ui\n    WHERE ui.username = #{username}\n      AND ui.delete_flag = 1\n) THEN 1 ELSE 0 END AS exist_flag;\n\n-- 4. 时间范围统计\nSELECT DATE_FORMAT(o.created_dt, \'%Y-%m-%d\') AS stat_date,\n       COUNT(1) AS order_count,\n       SUM(o.total_amount) AS total_amount\nFROM order_info o\nWHERE o.created_dt >= #{startDt}\n  AND o.created_dt < #{endDt}\nGROUP BY DATE_FORMAT(o.created_dt, \'%Y-%m-%d\')\nORDER BY stat_date;\n\n-- 5. 树形结构递归查询\nWITH RECURSIVE dept_tree AS (\n    SELECT d.id, d.dept_name, d.parent_id, 1 AS level_no\n    FROM dept d\n    WHERE d.id = #{rootId}\n    UNION ALL\n    SELECT d.id, d.dept_name, d.parent_id, t.level_no + 1\n    FROM dept d\n    INNER JOIN dept_tree t ON t.id = d.parent_id\n)\nSELECT t.id, t.dept_name, t.parent_id, t.level_no\nFROM dept_tree t\nORDER BY t.level_no, t.id;\n\n-- 6. 批量插入\nINSERT INTO user_info (name, status, created_by)\nVALUES (\'张三\', 1, 101),\n       (\'李四\', 1, 101);\n\n-- 7. 安全更新\nUPDATE user_info\nSET status = -1,\n    updated_by = 101\nWHERE id = #{id};\n', 4, 902, 1, 201, '2026-10-15 11:07:14', 201, '2026-10-15 11:07:14');

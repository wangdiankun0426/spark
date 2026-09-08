insert into `sys_user`(id, login_name, password, name, status, account_type, delete_flag, created_by, created_dt, updated_by, updated_dt)
values (101, 'sysadmin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 1, 2, 1, 101, now(), 101, now());
INSERT INTO `sys_tenant` (`id`, `name`, `status`, `deadline`, `account_count`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`)
VALUES (103, '演示租户环境', 1, null, 10, 1, 101, now(), 101, now());
INSERT INTO `task_template` (`id`,`tenant_id`,`name`, `task_type`, `remark`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (1, 0, '知识库文档归档任务', 2, '', 1, 101, now(), 101, now());
INSERT INTO `task_template_param` (`id`, `tenant_id`,`template_id`, `name`, `code`, `type`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (1,0, 1, '待归档文档ID', 'attId', 2, 1, 101, now(), 101, now());
INSERT INTO `task_template_param` (`id`, `tenant_id`,`template_id`, `name`, `code`, `type`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (2, 0,1, '所属知识库ID', 'kbId', 2, 1, 101, now(), 101, now());

INSERT INTO `sys_menu` (`id`, `name`, `parent_id`, `delete_flag`, `created_by`, `created_dt`) VALUES
  (10, '首页', 0, 1, 101, now()),
  (101, '切换主题', 10, 1, 101, now()),
  (102, '快捷方式', 10, 1, 101, now()),
  (103, '通讯录', 10, 1, 101, now()),
  (104, '消息', 10, 1, 101, now()),
  (20, 'AI应用', 0, 1, 101, now()),
  (201, 'Agent', 20, 1, 101, now()),
  (202, 'WorkFlow', 20, 1, 101, now()),
  (2021, '运行记录', 202, 1, 101, now()),
  (2022, '流程设计', 202, 1, 101, now()),
  (2023, '表单模板', 202, 1, 101, now()),
  (203, '模型市场', 20, 1, 101, now()),
  (204, '技能库', 20, 1, 101, now()),
  (205, 'MCP服务', 20, 1, 101, now()),
  (30, '知识库', 0, 1, 101, now()),
  (301, '知识库', 30, 1, 101, now()),
  (3011, '知识库文档', 301, 1, 101, now()),
  (302, '知识检索', 30, 1, 101, now()),
  (303, '检索测试', 30, 1, 101, now()),
  (304, '检索统计', 30, 1, 101, now()),
  (40, '知识图谱', 0, 1, 101, now()),
  (401, '知识图谱', 40, 1, 101, now()),
  (4011, '图谱详情', 401, 1, 101, now()),
  (4012, '图谱文档', 401, 1, 101, now()),
  (4013, '图谱实体', 401, 1, 101, now()),
  (4014, '图谱关系', 401, 1, 101, now()),
  (402, '知识检索', 40, 1, 101, now()),
  (403, '实体审核', 40, 1, 101, now()),
  (404, '图谱分析', 40, 1, 101, now()),
  (50, '流程中心', 0, 1, 101, now()),
  (501, '流程申请', 50, 1, 101, now()),
  (5011, '流程实例', 501, 1, 101, now()),
  (5012, '设计流程', 501, 1, 101, now()),
  (5013, '表单模板', 501, 1, 101, now()),
  (5014, '任务模板', 501, 1, 101, now()),
  (502, '我的申请', 50, 1, 101, now()),
  (503, '我的待办', 50, 1, 101, now()),
  (504, '我的已办', 50, 1, 101, now()),
  (505, '抄送给我', 50, 1, 101, now());
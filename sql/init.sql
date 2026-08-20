
insert into `sys_user`(id, login_name, password, name, dept_id, status, delete_flag, created_by, created_dt, updated_by, updated_dt)
values (101, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 102, 1, 1, 101, now(), 101, now());

insert into `sys_department`(id, name, prt_id, code, header_id, delete_flag, created_by, created_dt, updated_by, updated_dt)
values (102, '星火科技', 0, '001', null , 1, 101, now(), 101, now());

INSERT INTO `task_template` (`id`, `name`, `task_type`, `remark`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (1, '知识库文档归档任务', 2, '', 1, 101, now(), 101, now());
INSERT INTO `task_template_param` (`id`, `template_id`, `name`, `code`, `type`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (1, 1, '待归档文档ID', 'attId', 2, 1, 101, now(), 101, now());
INSERT INTO `task_template_param` (`id`, `template_id`, `name`, `code`, `type`, `delete_flag`, `created_by`, `created_dt`, `updated_by`, `updated_dt`) VALUES (2, 1, '所属知识库ID', 'kbId', 2, 1, 101, now(), 101, now());

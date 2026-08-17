DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` bigint(12) NOT NULL COMMENT '主键,后两位固定01',
    `login_name` varchar(200) NOT NULL COMMENT '登录名',
    `password` varchar(72) NOT NULL COMMENT '密码',
    `name` varchar(200) NOT NULL COMMENT '用户名',
    `dept_id` bigint(12) NOT NULL COMMENT '所属部门id',
    `phone` varchar(20) NULL COMMENT '手机号' unique ,
    `email` varchar(20) NULL COMMENT '邮箱' unique ,
    `sex` int(1) NULL COMMENT '性别',
    `avatar` varchar(32) NULL COMMENT '头像',
    `status` int(1) NOT NULL DEFAULT 1 COMMENT '状态',
    `wecom_id` varchar(64) NULL COMMENT '企业微信用户ID',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户信息表';

DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department` (
    `id` bigint(12) NOT NULL COMMENT '主键,后两位固定02',
    `name` varchar(128) NOT NULL COMMENT '名称',
    `prt_id` bigint(12) NOT NULL DEFAULT '0' COMMENT '父部门id,根节点的prtId=0',
    `code` varchar(36)  NOT NULL COMMENT '层级节点码',
    `header_id` bigint(12) NULL COMMENT '部门领导id',
    `dept_num` varchar(36) NULL COMMENT '部门编号',
    `status` int(1) NOT NULL DEFAULT 1 COMMENT '状态',
    `order_num` int(2) NOT NULL DEFAULT 99 COMMENT '排序号',
    `wecom_id` bigint(12) NULL COMMENT '企业微信部门ID',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='部门信息表';

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` bigint(12) NOT NULL COMMENT '主键,后两位固定04',
    `name` varchar(128) NOT NULL COMMENT '名称',
    `data_scope` int(4) NOT NULL DEFAULT 1 COMMENT '数据权限',
    `status` int(1) NOT NULL DEFAULT 1 COMMENT '状态',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='角色信息表';

DROP TABLE IF EXISTS `sys_role_user`;
CREATE TABLE `sys_role_user` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id` bigint(12) NOT NULL COMMENT '角色id',
    `user_id` bigint(12) NOT NULL COMMENT '用户id',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户角色关联表';

DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title` varchar(64) NOT NULL COMMENT '公告标题',
    `type` int(1) NOT NULL COMMENT '公告类型',
    `status` int(1) NOT NULL COMMENT '公告状态',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='公告信息表';

DROP TABLE IF EXISTS `sys_notice_obj`;
CREATE TABLE `sys_notice_obj` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `notice_id` bigint(12) NOT NULL COMMENT '公告id',
    `obj_id` bigint(12) NOT NULL COMMENT '用户/角色/部门id',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='公告关联对象表';

DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message` (
   `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
   `type` int(2) NOT NULL COMMENT '消息类型',
   `title` varchar(64) NOT NULL COMMENT '消息标题',
   `content` varchar(512) NOT NULL COMMENT '消息内容',
   `ref_id` bigint(12) NOT NULL COMMENT '所属对象ID',

   `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
   `created_by` bigint(12) NOT NULL COMMENT '创建人id',
   `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
   `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消息表';

DROP TABLE IF EXISTS `sys_message_user`;
CREATE TABLE `sys_message_user` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `msg_id` bigint(2) NOT NULL COMMENT '消息id',
    `user_id` bigint(2) NOT NULL COMMENT '用户id',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消息用户表';

DROP TABLE IF EXISTS `log_login`;
CREATE TABLE `log_login` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `ipaddress` varchar(36) NOT NULL default '未知' COMMENT 'ip地址',
    `login_type` int(2) NOT NULL default 1 COMMENT '登录类型',
    `login_platform` int(2) NOT NULL default 1 COMMENT '登录平台',
    `session_id` varchar(36) NOT NULL default '' COMMENT 'sessionID',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='登录日志';

DROP TABLE IF EXISTS `log_operate`;
CREATE TABLE `log_operate` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `type` int(5) NOT NULL COMMENT '操作类型',
    `obj_id` bigint(12) NOT NULL COMMENT '操作对象id',
    `code` int(5) NULL COMMENT '操作状态码',
    `consume` int(5) NULL COMMENT '耗时',
    `remark` varchar(36) NULL COMMENT '备注',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志';

DROP TABLE IF EXISTS `llm_provider`;
CREATE TABLE `llm_provider` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(50) NOT NULL COMMENT '厂商名称',
    `icon` varchar(128) NOT NULL COMMENT '厂商图标',
    `api_url` varchar(128) NOT NULL COMMENT 'API地址',
    `secret_key` varchar(128) NOT NULL COMMENT '厂商密钥',
    `order_num` int(3) NOT NULL COMMENT '排序',
    `description` varchar(256)  NULL COMMENT '描述',
    `remark` varchar(256)  NULL COMMENT '备注',

    `dept_id` bigint(12) NOT NULL COMMENT '所属部门',
    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='模型厂商表';

DROP TABLE IF EXISTS `llm_model`;
CREATE TABLE `llm_model` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `provider_id` bigint(12) NOT NULL COMMENT '供应商id',
    `type` int(5) NOT NULL COMMENT '模型类型',
    `name` varchar(50) NOT NULL COMMENT '模型名称',
    `enable_thinking` tinyint(3) NULL COMMENT '是否开启思考模式：1:开启，-1:不开启',
    `temperature` decimal(3,2) NULL COMMENT '温度参数：控制生成内容的随机性，范围 0.0-2.0',
    `status` tinyint(3) NOT NULL DEFAULT 1 COMMENT '状态：1:已开启，-1:已关闭',
    `description` varchar(256)  NULL COMMENT '描述',
    `remark` varchar(256)  NULL COMMENT '备注',

    `dept_id` bigint(12) NOT NULL COMMENT '所属部门',
    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='模型表';

DROP TABLE IF EXISTS `llm_mcp`;
CREATE TABLE `llm_mcp` (
      `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
      `name` varchar(64) NOT NULL COMMENT '服务器名称（唯一标识）',
      `transport` tinyint(3) NOT NULL COMMENT '传输类型：1:STDIO，2:SSE',
      `provider_id` bigint(12) NULL COMMENT '厂商id',
      `command` varchar(255) NULL COMMENT 'STDIO命令，如 node/python/java',
      `args` varchar(512) NULL COMMENT 'STDIO命令参数，JSON数组',
      `url` varchar(255) NULL COMMENT 'SSE模式URL，如 http://localhost:8080/mcp',
      `env` varchar(512) NULL COMMENT '环境变量，JSON对象',
      `timeout` bigint(12) NULL COMMENT '连接超时毫秒',
      `status` int(1) NOT NULL DEFAULT 1 COMMENT '状态：1:启用，-1:禁用',
      `description` varchar(255) NULL COMMENT '描述',

      `dept_id` bigint(12) NOT NULL COMMENT '所属部门',
      `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
      `created_by` bigint(12) NOT NULL COMMENT '创建人id',
      `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
      `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP服务表';

DROP TABLE IF EXISTS `llm_agent`;
CREATE TABLE `llm_agent` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(50) NOT NULL COMMENT '名称',
    `chat_model_id` bigint(12) NULL COMMENT '语言模型ID',
    `system_prompt` varchar(2048) NOT NULL COMMENT '系统提示词',
    `max_messages` int(5) NOT NULL DEFAULT 20 COMMENT '对话记忆大小',
    `tools` varchar(128) NULL COMMENT '工具ID列表',
    `kb_ids` varchar(256) NULL COMMENT '知识库ID列表',
    `graph_ids` varchar(256) NULL COMMENT '知识图谱ID列表',
    `mcp_ids` varchar(256) NULL COMMENT 'MCP服务器ID列表',
    `status` int(1) NOT NULL DEFAULT 1 COMMENT '状态',
    `description` varchar(256)  NULL COMMENT '描述',

    `dept_id` bigint(12) NOT NULL COMMENT '所属部门',
    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体表';

DROP TABLE IF EXISTS `chat_space`;
CREATE TABLE `chat_space` (
  `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `space_id` bigint(12) NOT NULL COMMENT '空间主键，后两位固定05',
  `sender_id` bigint(12) NOT NULL COMMENT '发送人id',
  `receiver_id` bigint(12) NOT NULL COMMENT '接收人id',

  `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
  `created_by` bigint(12) NOT NULL COMMENT '创建人id',
  `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
  `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='聊天空间表';

DROP TABLE IF EXISTS `chat_msg`;
CREATE TABLE `chat_msg` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `space_id` bigint(12) NOT NULL COMMENT '空间主键，后两位固定05',
    `sender_id` bigint(12) NOT NULL COMMENT '发送人id',
    `receiver_id` bigint(12) NOT NULL COMMENT '接收人id',
    `message` text NOT NULL COMMENT '消息',
    `sign_status` int(1) NOT NULL DEFAULT -1 COMMENT '签收状态',
    `read_status` int(1) NOT NULL DEFAULT -1 COMMENT '阅读状态',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息记录表';

DROP TABLE IF EXISTS `chat_msg_att`;
CREATE TABLE `chat_msg_att` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `msg_id` bigint(12) NOT NULL COMMENT '聊天消息id',
    `msg_att_type` int(2) NOT NULL COMMENT '消息附件类型 0 未知类型 1 rag引用文件',
    `doc_id` bigint(12) NOT NULL COMMENT '文档id',
    `doc_name` varchar(256) NOT NULL COMMENT '文档名称',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息附件表';

DROP TABLE IF EXISTS `kb_knowledge`;
CREATE TABLE `kb_knowledge` (
    `id` bigint(12) NOT NULL COMMENT '主键,后两位固定14',
    `name` varchar(128) NOT NULL COMMENT '知识库名称',
    `description` varchar(512) NULL COMMENT '描述',
    `parent_chunk_size` int(8) NOT NULL DEFAULT 800 COMMENT '父块大小',
    `parent_overlap` int(8) NOT NULL DEFAULT 100 COMMENT '父块重叠',
    `child_chunk_size` int(8) NOT NULL DEFAULT 200 COMMENT '子块大小',
    `child_overlap` int(8) NOT NULL DEFAULT 20 COMMENT '子块重叠',
    `enable_qa` int(1) NOT NULL DEFAULT 1 COMMENT '是否生成QA：1:是,0:否',
    `retrieve_top_k` int(8) NOT NULL DEFAULT 15 COMMENT '向量召回topK',
    `min_similarity` decimal(4,2) NOT NULL DEFAULT 0.40 COMMENT '最小相似度',
    `vector_model_id` bigint(12) NOT NULL COMMENT '向量模型id',
    `rerank_model_id` bigint(12) NOT NULL COMMENT '排序模型id',
    `status` int(1) NOT NULL DEFAULT 1 COMMENT '状态',

    `dept_id` bigint(12) NOT NULL COMMENT '所属部门',
    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

DROP TABLE IF EXISTS `kb_document`;
CREATE TABLE `kb_document` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `prt_id` bigint(12) NOT NULL COMMENT '父id',
    `document_type` tinyint(3) NOT NULL COMMENT '文档归属类型',
    `name` varchar(256) NOT NULL COMMENT '名称',
    `size` bigint(12) NOT NULL COMMENT '大小',
    `path` varchar(128) NOT NULL COMMENT '存储路径',
    `ext` varchar(64) NOT NULL COMMENT '拓展名',
    `owner_id` bigint(12) NOT NULL COMMENT '所有者id',

    `dept_id` bigint(12) NOT NULL COMMENT '所属部门',
    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='知识库文档表';

DROP TABLE IF EXISTS `kb_document_event`;
CREATE TABLE `kb_document_event` (
      `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
      `doc_id` bigint(12) NOT NULL COMMENT '文档id',
      `content_status` int(5) NOT NULL DEFAULT 1 COMMENT '提取文件内容状态',
      `content_remark` varchar(128)  NULL COMMENT '提取文件内容备注',
      `chunk_status` int(5) NOT NULL DEFAULT 1 COMMENT '分块状态',
      `chunk_remark` varchar(128) NULL COMMENT '分块备注',
      `index_status` int(5) NOT NULL DEFAULT 1 COMMENT '创建索引状态',
      `index_remark` varchar(128)  NULL COMMENT '创建索引备注',
      `vector_status` int(5) NOT NULL DEFAULT 1 COMMENT '向量化状态',
      `vector_remark` varchar(128)  NULL COMMENT '向量化备注',
      `graph_status` int(5) NOT NULL DEFAULT 1 COMMENT '构建知识图谱状态',
      `graph_remark` varchar(128)  NULL COMMENT '构建知识图谱备注',

      `dept_id` bigint(12) NOT NULL COMMENT '所属部门',
      `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
      `created_by` bigint(12) NOT NULL COMMENT '创建人id',
      `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
      `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='知识库文档事件表';

DROP TABLE IF EXISTS `flow_template`;
CREATE TABLE `flow_template` (
     `id` bigint(12) NOT NULL COMMENT '主键',
     `name` varchar(128) NOT NULL COMMENT '名称',
     `process_id` varchar(128) NOT NULL COMMENT '模板id',
     `form_id` bigint(12) NULL COMMENT '表单id',
     `rev_id` bigint(12) NULL COMMENT '版本id',
     `rev_num` varchar(12) NOT NULL COMMENT '当前版本号',
     `status` int(1) NOT NULL DEFAULT 0 COMMENT '状态',

     `dept_id` bigint(12) NOT NULL COMMENT '所属部门',
     `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
     `created_by` bigint(12) NOT NULL COMMENT '创建人id',
     `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
     `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程模板表';

DROP TABLE IF EXISTS `flow_template_version`;
CREATE TABLE `flow_template_version` (
     `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
     `template_id` bigint(12) NOT NULL COMMENT '流程模板id',
     `rev_code` int(5) NOT NULL COMMENT '当前版本值',
     `rev_num` varchar(12) NOT NULL COMMENT '当前版本号',
     `process_id` varchar(128) NOT NULL COMMENT '模板id',
     `bpm_path` varchar(128) NULL COMMENT 'bpm json文件保存路径',

     `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
     `created_by` bigint(12) NOT NULL COMMENT '创建人id',
     `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
     `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程模板表';

DROP TABLE IF EXISTS `flow_template_node`;
CREATE TABLE `flow_template_node` (
      `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
      `template_id` bigint(12) NOT NULL COMMENT '流程模板id',
      `rev_id` bigint(12) NOT NULL COMMENT '流程版本id',
      `node_id` varchar(36) NOT NULL COMMENT '节点id',
      `name` varchar(36) NOT NULL COMMENT '节点名称',
      `type` varchar(36) NOT NULL COMMENT '节点类型',
      `assignee_type` int(3) NULL COMMENT '审批人类型',
      `assignee` varchar(256) NULL COMMENT '审批人',
      `permission` int(3) NULL COMMENT '节点权限',
      `approve_type` int(3) NULL DEFAULT 1 COMMENT '审批类型',
      `urge_enabled` tinyint(1) NULL DEFAULT 0 COMMENT '是否启用定时催办：1启用/0关闭',
      `urge_interval` int(5) NULL COMMENT '催办间隔',

      `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
      `created_by` bigint(12) NOT NULL COMMENT '创建人id',
      `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
      `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程模板节点表';

DROP TABLE IF EXISTS `flow_template_sequence`;
CREATE TABLE `flow_template_sequence` (
      `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
      `template_id` bigint(12) NOT NULL COMMENT '流程模板id',
      `rev_id` bigint(12) NOT NULL COMMENT '流程版本id',
      `sequence_id` varchar(36) NOT NULL COMMENT '连线id',
      `source_ref` varchar(128) NOT NULL COMMENT '连线起点',
      `target_ref` varchar(128) NOT NULL COMMENT '连线终点',
      `condition` varchar(128) NULL COMMENT '条件表达式',

      `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
      `created_by` bigint(12) NOT NULL COMMENT '创建人id',
      `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
      `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程模板连线表';

DROP TABLE IF EXISTS `flow_template_msg`;
CREATE TABLE `flow_template_msg` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `template_id` bigint(12) NOT NULL COMMENT '流程模板id',
    `rev_id` bigint(12) NOT NULL COMMENT '流程版本id',
    `type` int(5) NOT NULL COMMENT '通知类型',
    `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
    `content` varchar(512) NOT NULL DEFAULT '' COMMENT '通知内容模板',
    `recipient` varchar(256) NOT NULL DEFAULT '' COMMENT '通知人',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程模板消息通知表';

DROP TABLE IF EXISTS `flow_instance`;
CREATE TABLE `flow_instance` (
     `id` bigint(12) NOT NULL COMMENT '主键',
     `template_id` bigint(12) NOT NULL COMMENT '流程模板id',
     `template_rev_id` bigint(12) NOT NULL COMMENT '流程模板版本id',
     `form_id` bigint(12) NOT NULL COMMENT '表单id',
     `form_rev_id` bigint(12) NOT NULL COMMENT '表单版本id',
     `process_id` varchar(128) NOT NULL COMMENT '模板id',
     `flowable_instance_id` varchar(128) NOT NULL COMMENT 'flowable 流程实例id' unique ,
     `status` int(5) NOT NULL COMMENT '流程实例状态',
     `name` varchar(128) NOT NULL COMMENT '流程实例名称',
     `description` varchar(256) NULL COMMENT '流程实例描述',
     `level` int(1) NOT NULL DEFAULT 1 COMMENT '紧急程度',

     `dept_id` bigint(12) NOT NULL COMMENT '所属部门',
     `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
     `created_by` bigint(12) NOT NULL COMMENT '创建人id',
     `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
     `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程实例表';

DROP TABLE IF EXISTS `flow_instance_node`;
CREATE TABLE `flow_instance_node` (
      `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
      `instance_id` bigint(12) NOT NULL COMMENT '流程实例id',
      `node_id` varchar(36) NOT NULL COMMENT '节点id',
      `name` varchar(36) NOT NULL COMMENT '节点名称',
      `type` varchar(36) NOT NULL COMMENT '节点类型',
      `assignee_set_id` varchar(36) NULL COMMENT '审批人集合id',
      `status` int(5) NOT NULL COMMENT '节点状态',

      `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
      `created_by` bigint(12) NOT NULL COMMENT '创建人id',
      `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
      `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程实例节点表';

DROP TABLE IF EXISTS `flow_instance_discuss`;
CREATE TABLE `flow_instance_discuss` (
     `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
     `instance_id` bigint(12) NOT NULL COMMENT '流程实例id',
     `instance_node_id` bigint(12) NOT NULL COMMENT '流程实例节点id',
     `assignee_id` bigint(12) NOT NULL COMMENT '审批人id',
     `status` int(5) NOT NULL COMMENT '审批状态',
     `discuss` varchar(128) NULL COMMENT '审批意见',

     `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
     `created_by` bigint(12) NOT NULL COMMENT '创建人id',
     `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
     `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程实例讨论表';

DROP TABLE IF EXISTS `flow_instance_assignee`;
CREATE TABLE `flow_instance_assignee` (
      `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
      `instance_id` bigint(12) NOT NULL COMMENT '流程实例id',
      `instance_node_id` bigint(12) NOT NULL COMMENT '流程实例节点id',
      `assignee_set_id` varchar(36) NOT NULL COMMENT '审批人集合id',
      `assignee_id` bigint(12) NOT NULL COMMENT '审批人id',
      `status` int(5) NOT NULL COMMENT '审批状态',
      `sort` int(5) NULL COMMENT '审批顺序',

      `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
      `created_by` bigint(12) NOT NULL COMMENT '创建人id',
      `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
      `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程实例审批用户表';

DROP TABLE IF EXISTS `form`;
CREATE TABLE `form` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `type` int(1) NOT NULL COMMENT '表单类型',
    `name`varchar(64) NOT NULL COMMENT '名称',
    `rev_id` bigint(12) NOT NULL COMMENT '当前版本id',
    `rev_num` varchar(12) NOT NULL COMMENT '当前版本号',
    `order_num` int(5) NOT NULL COMMENT '排序号',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='表单表';

DROP TABLE IF EXISTS `form_version`;
CREATE TABLE `form_version` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `form_id` bigint(12) NOT NULL COMMENT '表单id',
    `rev_code` int(5) NOT NULL COMMENT '当前版本值',
    `rev_num` varchar(12) NOT NULL COMMENT '当前版本号',
    `file_path`varchar(128) NULL COMMENT '文件路径',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='表单版本表';

DROP TABLE IF EXISTS `form_field`;
CREATE TABLE `form_field` (
      `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
      `form_id` bigint(12) NOT NULL COMMENT '表单id',
      `rev_id` bigint(12) NOT NULL COMMENT '版本id',
      `label` varchar(36) NOT NULL COMMENT '字段标题',
      `code` varchar(36) NOT NULL COMMENT '字段名',
      `type` varchar(20) NOT NULL COMMENT '字段类型',

      `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
      `created_by` bigint(12) NOT NULL COMMENT '创建人id',
      `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
      `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='表单字段表';

DROP TABLE IF EXISTS `form_obj`;
CREATE TABLE `form_obj` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `obj_id` bigint(12) NOT NULL COMMENT '对象id',
    `form_id` bigint(12) NOT NULL COMMENT '表单id',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='表单关联对象表';

DROP TABLE IF EXISTS `form_obj_value`;
CREATE TABLE `form_obj_value` (
      `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
      `obj_id` bigint(12) NOT NULL COMMENT '对象id',
      `form_id` bigint(12) NOT NULL COMMENT '表单id',
      `code` varchar(36) NOT NULL COMMENT '字段名',
      `type` varchar(20) NOT NULL COMMENT '字段类型',
      `value` varchar(64) NULL COMMENT '字段值',
      `show_value` varchar(512) NULL COMMENT '字段值',

      `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
      `created_by` bigint(12) NOT NULL COMMENT '创建人id',
      `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
      `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='表单对象字段值表';

DROP TABLE IF EXISTS `kg_graph`;
CREATE TABLE kg_graph (
      id BIGINT(20) NOT NULL COMMENT '主键 id',
      name VARCHAR(128) NOT NULL COMMENT '图谱名称',
      description VARCHAR(512) DEFAULT NULL COMMENT '图谱描述',
      entity_types TEXT DEFAULT NULL COMMENT '实体类型 schema（JSON 数组）',
      relation_types TEXT DEFAULT NULL COMMENT '关系类型 schema（JSON 数组）',
      extract_model_id BIGINT(20) DEFAULT NULL COMMENT '抽取模型 id',
      status TINYINT(1) DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',

      dept_id BIGINT(20) DEFAULT NULL COMMENT '部门 id',
      delete_flag TINYINT(1) DEFAULT 1 COMMENT '删除标记位（1-有效 -1-删除）',
      created_by BIGINT(20) DEFAULT NULL COMMENT '创建人',
      created_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      updated_by BIGINT(20) DEFAULT NULL COMMENT '修改人',
      updated_dt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
      PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识图谱配置表';

DROP TABLE IF EXISTS `kg_entity`;
CREATE TABLE kg_entity (
       id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
       graph_id BIGINT(20) NOT NULL COMMENT '图谱 id',
       name VARCHAR(256) NOT NULL COMMENT '实体名称',
       type VARCHAR(64) DEFAULT NULL COMMENT '实体类型',
       source_id BIGINT(20) NOT NULL COMMENT '来源 id' ,
       description VARCHAR(1024) DEFAULT NULL COMMENT '实体描述',
       status TINYINT(1) DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',

       dept_id BIGINT(20) DEFAULT NULL COMMENT '部门 id',
       delete_flag TINYINT(1) DEFAULT 1 COMMENT '删除标记位（1-有效 -1-删除）',
       created_by BIGINT(20) DEFAULT NULL COMMENT '创建人',
       created_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
       updated_by BIGINT(20) DEFAULT NULL COMMENT '修改人',
       updated_dt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
       PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识图谱实体表';

DROP TABLE IF EXISTS `kg_relation`;
CREATE TABLE kg_relation (
     id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
     graph_id BIGINT(20) NOT NULL COMMENT '图谱 id',
     head_entity_id BIGINT(20) NOT NULL COMMENT '头实体 id',
     tail_entity_id BIGINT(20) NOT NULL COMMENT '尾实体 id',
     relation_type VARCHAR(64) NOT NULL COMMENT '关系类型',
     weight DOUBLE DEFAULT 1.0 COMMENT '关系权重',
     source_id BIGINT(20) NOT NULL COMMENT '来源 id' ,
     status TINYINT(1) DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',

     dept_id BIGINT(20) DEFAULT NULL COMMENT '部门 id',
     delete_flag TINYINT(1) DEFAULT 1 COMMENT '删除标记位（1-有效 -1-删除）',
     created_by BIGINT(20) DEFAULT NULL COMMENT '创建人',
     created_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     updated_by BIGINT(20) DEFAULT NULL COMMENT '修改人',
     updated_dt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
     PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识图谱关系表';

DROP TABLE IF EXISTS `wf_template`;
CREATE TABLE `wf_template` (
    `id` bigint(12) NOT NULL COMMENT '主键',
    `name` varchar(128) NOT NULL COMMENT '工作流名称',
    `description` varchar(512) NULL COMMENT '描述',
    `status` int(2) NOT NULL DEFAULT -1 COMMENT '状态：-1关闭/1开启',
    `rev_id` bigint(12) NULL COMMENT '当前生效版本ID',
    `rev_num` varchar(12) NULL COMMENT '当前生效版本号',

    `dept_id` bigint(12) NOT NULL COMMENT '所属部门',
    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1有效/-1无效',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_dept_id` (`dept_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流模板表';

DROP TABLE IF EXISTS `wf_template_version`;
CREATE TABLE `wf_template_version` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `template_id` bigint(12) NOT NULL COMMENT '工作流模板ID',
    `rev_code` int(5) NOT NULL COMMENT '版本序号（1开始递增）',
    `rev_num` varchar(12) NOT NULL COMMENT '版本号（0.1/0.2/...）',
    `dag_json` mediumtext NULL COMMENT 'DAG图定义（节点+边，JSON）',
    `global_vars` text NULL COMMENT '全局变量定义（JSON）',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流模板版本表';

DROP TABLE IF EXISTS `wf_template_endpoint`;
CREATE TABLE `wf_template_endpoint` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `template_id` bigint(12) NOT NULL COMMENT '工作流模板ID',
    `rev_id` bigint(12) NOT NULL COMMENT '生效版本ID',
    `path` varchar(128) NOT NULL COMMENT '端点路径',
    `auth_type` int(2) NOT NULL DEFAULT 0 COMMENT '鉴权',
    `api_key` varchar(64) NULL COMMENT 'API Key',
    `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_path` (`path`, `delete_flag`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='工作流模板节点配置表';

DROP TABLE IF EXISTS `wf_instance`;
CREATE TABLE `wf_instance` (
    `id` bigint(12) NOT NULL COMMENT '主键',
    `template_id` bigint(12) NOT NULL COMMENT '工作流模板ID',
    `rev_id` bigint(12) NOT NULL COMMENT '执行版本ID',
    `rev_num` varchar(12) NOT NULL COMMENT '执行版本号',
    `status` int(2) NOT NULL DEFAULT 1 COMMENT '运行状态',
    `input_json` mediumtext NULL COMMENT '输入参数JSON',
    `output_json` mediumtext NULL COMMENT '输出结果JSON',
    `error_msg` varchar(1024) NULL COMMENT '错误信息',
    `started_dt` timestamp NULL COMMENT '开始时间',
    `finished_dt` timestamp NULL COMMENT '结束时间',
    `duration_ms` bigint(12) NULL COMMENT '总耗时',
    `trigger_type` int(2) NOT NULL DEFAULT 1 COMMENT '触发方式',

    `dept_id` bigint(12) NOT NULL COMMENT '所属部门',
    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识',
    `created_by` bigint(12) NOT NULL COMMENT '触发人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_by` (`created_by`),
    KEY `idx_created_dt` (`created_dt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='工作流实例表';

DROP TABLE IF EXISTS `wf_instance_node`;
CREATE TABLE `wf_instance_node` (
    `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `instance_id` bigint(12) NOT NULL COMMENT '运行实例ID',
    `node_id` varchar(36) NOT NULL COMMENT 'DAG节点ID',
    `node_name` varchar(128) NOT NULL COMMENT '节点名称',
    `node_type` varchar(36) NOT NULL COMMENT '节点类型',
    `status` int(2) NOT NULL DEFAULT 1 COMMENT '状态',
    `input_json` mediumtext NULL COMMENT '节点输入JSON快照',
    `output_json` mediumtext NULL COMMENT '节点输出JSON快照',
    `error_msg` varchar(1024) NULL COMMENT '错误信息',
    `started_dt` timestamp NULL COMMENT '开始时间',
    `finished_dt` timestamp NULL COMMENT '结束时间',
    `duration_ms` bigint(12) NULL COMMENT '耗时',
    `retry_count` int(3) NOT NULL DEFAULT 0 COMMENT '重试次数',

    `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识',
    `created_by` bigint(12) NOT NULL COMMENT '创建人id',
    `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
    `updated_dt` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_instance_id` (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='工作流实例节点表';

DROP TABLE IF EXISTS `task_instance`;
CREATE TABLE `task_instance` (
     `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
     `task_type` int(3) NOT NULL COMMENT '任务类型',
     `obj_id` bigint(12) NOT NULL COMMENT '业务对象id',
     `obj_type` int(3) NOT NULL COMMENT '业务对象类型',
     `task_time` timestamp NOT NULL COMMENT '下次执行时间',
     `interval_hours` int(5) NULL COMMENT '重复间隔',
     `status` tinyint(3) NOT NULL DEFAULT 1 COMMENT '任务状态',
     `remark` varchar(255) NULL COMMENT '备注',

     `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
     `created_by` bigint(12) DEFAULT NULL COMMENT '创建人id',
     `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
     `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务实例表';

DROP TABLE IF EXISTS `task_instance_param`;
CREATE TABLE `task_instance_param` (
   `id` bigint(12) NOT NULL AUTO_INCREMENT COMMENT '主键',
   `task_id` bigint(12) NOT NULL COMMENT '定时任务id',
   `code` varchar(64) NOT NULL COMMENT '参数编码',
   `value` varchar(256) NULL COMMENT '参数值',

   `delete_flag` tinyint(3) NOT NULL DEFAULT '1' COMMENT '删除标识：1:有效，-1：无效',
   `created_by` bigint(12) DEFAULT NULL COMMENT '创建人id',
   `created_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `updated_by` bigint(12) DEFAULT NULL COMMENT '修改人id',
   `updated_dt` timestamp NULL DEFAULT NULL COMMENT '修改时间',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务参数表';

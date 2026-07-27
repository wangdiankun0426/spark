-- ============================================================
-- 知识图谱模块 DDL 脚本
-- 创建时间: 2026-07-22
-- 说明: 知识图谱(Knowledge Graph)相关表结构
-- ============================================================

-- ------------------------------------------------------------
-- 1. 知识图谱配置表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `kg_graph`;
CREATE TABLE `kg_graph` (
    `id` BIGINT(20) NOT NULL COMMENT '主键 id',
    `name` VARCHAR(128) NOT NULL COMMENT '图谱名称',
    `description` VARCHAR(512) DEFAULT NULL COMMENT '图谱描述',
    `entity_types` TEXT DEFAULT NULL COMMENT '实体类型 schema（JSON 数组）',
    `relation_types` TEXT DEFAULT NULL COMMENT '关系类型 schema（JSON 数组）',
    `extract_model_id` BIGINT(20) DEFAULT NULL COMMENT '抽取模型 id',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    `dept_id` BIGINT(20) DEFAULT NULL COMMENT '部门 id',
    `delete_flag` TINYINT(1) DEFAULT 1 COMMENT '删除标记位（1-有效 -1-删除）',
    `created_by` BIGINT(20) DEFAULT NULL COMMENT '创建人',
    `created_dt` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT(20) DEFAULT NULL COMMENT '修改人',
    `updated_dt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识图谱配置表';

-- ------------------------------------------------------------
-- 2. 知识图谱实体表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `kg_entity`;
CREATE TABLE `kg_entity` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `graph_id` BIGINT(20) NOT NULL COMMENT '图谱 id',
    `name` VARCHAR(256) NOT NULL COMMENT '实体名称',
    `type` VARCHAR(64) DEFAULT NULL COMMENT '实体类型',
    `description` VARCHAR(1024) DEFAULT NULL COMMENT '实体描述',
    `source_id` BIGINT(20) NOT NULL COMMENT '来源 id：接口创建存用户 id，文档解析存文档 id',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态（0-无效 1-有效）',
    `dept_id` BIGINT(20) DEFAULT NULL COMMENT '部门 id',
    `delete_flag` TINYINT(1) DEFAULT 1 COMMENT '删除标记位（1-有效 -1-删除）',
    `created_by` BIGINT(20) DEFAULT NULL COMMENT '创建人',
    `created_dt` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT(20) DEFAULT NULL COMMENT '修改人',
    `updated_dt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_graph_id` (`graph_id`),
    KEY `idx_name_type` (`name`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识图谱实体表';

-- ------------------------------------------------------------
-- 3. 知识图谱关系表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `kg_relation`;
CREATE TABLE `kg_relation` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `graph_id` BIGINT(20) NOT NULL COMMENT '图谱 id',
    `head_entity_id` BIGINT(20) NOT NULL COMMENT '头实体 id',
    `tail_entity_id` BIGINT(20) NOT NULL COMMENT '尾实体 id',
    `relation_type` VARCHAR(64) NOT NULL COMMENT '关系类型',
    `weight` DOUBLE DEFAULT 1.0 COMMENT '关系权重',
    `source_id` BIGINT(20) NOT NULL COMMENT '来源 id：接口创建存用户 id，文档解析存文档 id',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态（0-无效 1-有效）',
    `dept_id` BIGINT(20) DEFAULT NULL COMMENT '部门 id',
    `delete_flag` TINYINT(1) DEFAULT 1 COMMENT '删除标记位（1-有效 -1-删除）',
    `created_by` BIGINT(20) DEFAULT NULL COMMENT '创建人',
    `created_dt` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT(20) DEFAULT NULL COMMENT '修改人',
    `updated_dt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_graph_id` (`graph_id`),
    KEY `idx_head_entity_id` (`head_entity_id`),
    KEY `idx_tail_entity_id` (`tail_entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识图谱关系表';

/**
  spark系统数据库
 */
CREATE DATABASE IF NOT EXISTS `spark` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

/*
 * Flowable 工作流引擎独立数据库
 * 该数据库用于存放 Flowable 引擎自动创建的 ACT_* 表
 */
CREATE DATABASE IF NOT EXISTS `spark_bpm` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
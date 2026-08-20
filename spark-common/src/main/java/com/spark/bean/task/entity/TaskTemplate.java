package com.spark.bean.task.entity;

import com.spark.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 10:00:00
 * 通用定时任务模板表
 */
@Data
public class TaskTemplate extends BaseEntity {

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务类型 TaskTypeEnum
     */
    private Integer taskType;

    /**
     * 备注
     */
    private String remark;
}

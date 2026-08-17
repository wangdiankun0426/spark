package com.spark.bean.task.entity;

import com.spark.bean.base.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-17 16:30:00
 * 通用定时任务实例表
 */
@Data
public class TaskInstance extends BaseEntity {

    /**
     * 任务类型
     * TaskTypeEnum
     */
    private Integer taskType;

    /**
     * 业务对象id
     */
    private Long objId;

    /**
     * 业务对象类型 ObjectTypeEnum
     */
    private Integer objType;

    /**
     * 下次执行时间
     */
    private Date taskTime;

    /**
     * 重复间隔
     */
    private Integer intervalHours;

    /**
     * 任务状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}

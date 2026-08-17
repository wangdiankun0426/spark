package com.spark.bean.task.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

import java.util.Date;
import java.util.List;

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
public class TaskInstanceResult extends BaseResult {

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
     * 任务状态 TaskStatusEnum
     */
    private Integer status;

    /**
     * 任务参数列表
     */
    private List<TaskInstanceParamResult> params;

}

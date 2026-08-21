package com.spark.bean.task.result;

import com.fasterxml.jackson.annotation.JsonFormat;
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
     * 业务对象类型名称
     */
    private String objTypeName;

    /**
     * 任务组id
     */
    private String setId;

    /**
     * 执行顺序
     */
    private Integer sort;

    /**
     * 下次执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
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
     * 任务类型名称
     */
    private String taskTypeName;

    /**
     * 任务状态名称
     */
    private String statusName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 任务参数列表
     */
    private List<TaskInstanceParamResult> params;

    /**
     * 任务产出数据列表
     */
    private List<TaskInstanceDataResult> dataList;

}

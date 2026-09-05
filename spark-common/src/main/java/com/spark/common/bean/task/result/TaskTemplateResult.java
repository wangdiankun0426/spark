package com.spark.common.bean.task.result;

import com.spark.common.bean.base.BaseResult;
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
public class TaskTemplateResult extends BaseResult {

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务类型 TaskTypeEnum
     */
    private Integer taskType;

    /**
     * 任务类型名称
     */
    private String taskTypeName;

    /**
     * 备注
     */
    private String remark;
}

package com.spark.common.bean.task.vo;

import com.spark.common.bean.base.BaseVO;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 10:00:00
 * 通用定时任务模板参数
 */
@Data
public class TaskTemplateVO extends BaseVO {

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

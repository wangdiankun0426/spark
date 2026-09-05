package com.spark.common.bean.task.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 10:00:00
 * 通用定时任务模板查询参数
 */
@Data
public class TaskTemplateQuery extends BaseQuery {

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务类型 TaskTypeEnum
     */
    private Integer taskType;
}

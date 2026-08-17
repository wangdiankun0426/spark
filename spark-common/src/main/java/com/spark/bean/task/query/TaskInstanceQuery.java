package com.spark.bean.task.query;

import com.spark.bean.base.BaseQuery;
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
 * 通用定时任务实例查询参数
 */
@Data
public class TaskInstanceQuery extends BaseQuery {

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
     * 任务状态 TaskStatusEnum
     */
    private Integer status;

    /**
     * 下次执行时间
     */
    private Date taskTimeEnd;

}

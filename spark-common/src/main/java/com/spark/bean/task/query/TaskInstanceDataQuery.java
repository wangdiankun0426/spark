package com.spark.bean.task.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-21 15:30:00
 * 任务产出数据查询参数
 */
@Data
public class TaskInstanceDataQuery extends BaseQuery {

    /**
     * 任务组id
     */
    private String setId;

    /**
     * 任务实例id
     */
    private Long taskId;

    /**
     * 数据编码
     */
    private String code;
}

package com.spark.bean.task.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-17 17:30:00
 * 通用定时任务参数查询参数
 */
@Data
public class TaskInstanceParamQuery extends BaseQuery {

    /**
     * 定时任务id
     */
    private Long taskId;

    /**
     * 定时任务id列表（批量查询）
     */
    private List<Long> taskIds;

    /**
     * 参数编码
     */
    private String code;
}

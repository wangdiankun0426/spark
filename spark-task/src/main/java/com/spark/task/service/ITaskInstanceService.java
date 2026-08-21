package com.spark.task.service;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.task.query.TaskInstanceQuery;
import com.spark.bean.task.result.TaskInstanceResult;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-17 16:30:00
 * 通用定时任务调度服务
 */
public interface ITaskInstanceService {

    /**
     * 执行任务
     * @return 执行结果
     */
    ResultData<Void> executeTask();

    /**
     * 分页查询任务实例列表
     * @param query
     * @return 任务实例分页结果
     */
    ResultData<PageResult<TaskInstanceResult>> pageTaskInstanceList(TaskInstanceQuery query);

    /**
     * 查询任务实例详情
     * @param query 查询参数
     * @return 任务实例详情
     */
    ResultData<TaskInstanceResult> queryTaskInstanceDetail(TaskInstanceQuery query);
}

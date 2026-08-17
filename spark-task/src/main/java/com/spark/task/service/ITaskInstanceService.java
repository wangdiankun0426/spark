package com.spark.task.service;

import com.spark.bean.base.ResultData;

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
}

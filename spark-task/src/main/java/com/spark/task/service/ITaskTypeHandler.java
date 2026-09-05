package com.spark.task.service;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.task.result.TaskInstanceResult;

import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-17 16:30:00
 * 通用定时任务类型处理器接口，新任务类型实现本接口并注册为Spring组件即可被调度框架分发
 */
public interface ITaskTypeHandler {

    /**
     * 处理的任务类型
     * @return 任务类型 TaskTypeEnum
     */
    Integer getTaskType();

    /**
     * 执行任务
     * @param taskInstance 任务实例
     * @param params 任务参数
     * @return 任务执行结果，成功时data为输出JSON字符串
     */
    ResultData<Map<String, String>> handle(TaskInstanceResult taskInstance, Map<String, String> params);
}

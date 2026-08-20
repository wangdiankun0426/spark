package com.spark.flow.service;

import com.spark.bean.base.ResultData;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/8/6 22:42
 */
public interface FlowEventService {

    /**
     * 用户任务创建
     * @param instanceId 流程实例ID
     * @param nodeId 节点定义Key
     * @param taskId 任务实例ID
     * @return
     */
    ResultData<Void> onCreatedUserTask(String instanceId, String nodeId, String taskId);

    /**
     * 用户任务完成
     * @param instanceId
     * @param nodeId
     * @param status
     * @return
     */
    ResultData<Void> onCompletedUserTask(String instanceId, String nodeId, Integer status);

    /**
     * 节点任务触发
     * @param flowableInstanceId 流程实例ID（Flowable实例id）
     * @param nodeId
     * @param executeType
     * @return 触发结果
     */
    ResultData<Void> onNodeTask(String flowableInstanceId, String nodeId, Integer executeType);
}

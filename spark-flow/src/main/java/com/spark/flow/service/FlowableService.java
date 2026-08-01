package com.spark.flow.service;

import com.spark.bean.base.ResultData;

import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/1 14:45
 */
public interface FlowableService {

    /**
     * 部署流程
     * @param bpmJson 流程定义
     * @return 响应结果
     */
     ResultData<Void> deploy(String processId, String bpmJson);

    /**
     * 检查流程是否已部署
     * @param processId 流程定义id
     * @return 响应结果
     */
    ResultData<Void> checkProcessDeployed(String processId);

    /**
     * 创建流程实例
     * @param processId 流程定义id
     * @param variables 流程变量
     * @return 响应结果
     */
    ResultData<String> createInstance(String processId, Map<String, Object> variables);

    /**
     * 查询待处理流程实例
     * @param assigneeIds 用户id
     * @return 响应结果
     */
    ResultData<List<String>> queryPendingInstanceIds(List<String> assigneeIds);

    /**
     * 完成任务
     *
     * @param flowableInstanceId 流程实例id
     * @param variables 流程变量
     * @return 响应结果
     */
    ResultData<Void> completeTask(String flowableInstanceId, Map<String, Object> variables);

    /**
     * 按任务ID完成任务
     * 适用于任务创建事件内（任务尚未提交数据库）的场景，如系统自动通过
     *
     * @param taskId 任务id
     * @param variables 流程变量
     * @return 响应结果
     */
    ResultData<Void> completeTaskById(String taskId, Map<String, Object> variables);

    /**
     * 挂起流程实例
     *
     * @param flowableInstanceId 流程实例id
     * @return 响应结果
     */
    ResultData<Void> suspendInstance(String flowableInstanceId);

    /**
     * 激活流程实例
     * @param flowableInstanceId 流程实例id
     * @return 响应结果
     */
    ResultData<Void> activateInstance(String flowableInstanceId);

    /**
     * 获取流程变量
     *
     * @param flowableInstanceId 流程实例id
     * @return 响应结果
     */
    ResultData<Map<String, Object>> getVariables(String flowableInstanceId);

    /**
     * 删除流程实例
     *
     * @param flowableInstanceId 流程实例id
     * @return 响应结果
     */
    ResultData<Void> deleteInstance(String flowableInstanceId);

    /**
     * 设置流程实例任务处理人
     * @param flowableTaskId 任务id
     * @param assignee 处理人
     * @return 响应结果
     */
    ResultData<Void> setAssignee(String flowableTaskId, String assignee);

}

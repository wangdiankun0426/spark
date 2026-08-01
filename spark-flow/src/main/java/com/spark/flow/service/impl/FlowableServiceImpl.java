package com.spark.flow.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.spark.bean.base.BaseException;
import com.spark.bean.base.ResultData;
import com.spark.bean.base.SessionHolder;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.FlowTemplateTypeEnum;
import com.spark.flow.service.FlowableService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.flowable.bpmn.model.*;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/1 14:44
 */
@Service
public class FlowableServiceImpl implements FlowableService {
    private final static Logger logger = LoggerFactory.getLogger(FlowableServiceImpl.class);
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    /**
     * 部署流程
     * @param processId 流程ID
     * @param bpmJson 流程定义JSON
     * @return 响应结果
     */
    @Override
    public ResultData<Void> deploy(String processId, String bpmJson) {
        ResultData<Void> result = new ResultData<>();
        if (processId == null || bpmJson == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        BpmnModel model = this.convertJsonToBpmnModel(processId, bpmJson);
        Deployment deployment = repositoryService.createDeployment()
                .addBpmnModel(processId+".bpmn20.xml", model)
                .deploy();
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 检查流程是否已经部署
     * @param processId 流程ID
     * @return 响应结果
     */
    @Override
    public ResultData<Void> checkProcessDeployed(String processId) {
        ResultData<Void> result = new ResultData<>();
        if (StringUtil.isBlank(processId)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processId)
                // 查询处于激活状态的流程定义 部署之后默认是激活的
                .active()
                .list();
        if (CollectionUtil.isEmpty(list)) {
            result.setErrorCode(ErrorCodeEnum.FLOW_TEMPLATE_NOT_DEPLOYED);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 创建流程实例
     * @param processId 流程ID
     * @param variables 流程变量
     * @return 响应结果
     */
    @Override
    public ResultData<String> createInstance(String processId, Map<String, Object> variables) {
        ResultData<String> result = new ResultData<>();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processId, variables);
        result.setData(processInstance.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询待处理流程实例
     * @param assigneeIds 用户ID
     * @return 响应结果
     */
    @Override
    public ResultData<List<String>> queryPendingInstanceIds(List<String> assigneeIds) {
        ResultData<List<String>> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        List<Task> list = taskService.createTaskQuery()
                .taskAssigneeIds(assigneeIds)
//                .taskCandidateGroupIn(assigneeIds)
                // 只查询激活的流程的任务
                .active()
                .list();
        if (CollectionUtil.isEmpty(list)) {
            result.setCode(ResultData.OK);
            return result;
        }
        List<String> instanceIds = list.stream().map(Task::getProcessInstanceId).toList();
        result.setData(instanceIds);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 完成任务
     * @param flowableInstanceId 流程实例ID
     * @param variables 流程变量
     * @return 响应结果
     */
    @Override
    public ResultData<Void> completeTask(String flowableInstanceId, Map<String, Object> variables) {
        ResultData<Void> result = new ResultData<>();
        List<Task> tasks = taskService
                .createTaskQuery()
                .processInstanceId(flowableInstanceId)
                .active()
                .list();
        if(CollectionUtils.isEmpty(tasks)) {
            result.setErrorCode(ErrorCodeEnum.FLOW_TASK_IS_EMPTY);
            return result;
        }
        Task task = tasks.get(0);
        taskService.complete(task.getId(), variables);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 按任务ID完成任务
     * 通过任务ID走实体缓存查询，任务创建事件内（尚未提交数据库）也可完成
     * @param taskId 任务ID
     * @param variables 流程变量
     * @return 响应结果
     */
    @Override
    public ResultData<Void> completeTaskById(String taskId, Map<String, Object> variables) {
        ResultData<Void> result = new ResultData<>();
        taskService.complete(taskId, variables);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 挂起流程实例
     * @param flowableInstanceId 流程实例ID
     * @return 响应结果
     */
    @Override
    public ResultData<Void> suspendInstance(String flowableInstanceId) {
        ResultData<Void> result = new ResultData<>();
        runtimeService.suspendProcessInstanceById(flowableInstanceId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 激活流程实例
     * @param flowableInstanceId 流程实例ID
     * @return 响应结果
     */
    @Override
    public ResultData<Void> activateInstance(String flowableInstanceId) {
        ResultData<Void> result = new ResultData<>();
        runtimeService.activateProcessInstanceById(flowableInstanceId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 获取流程实例变量
     * @param flowableInstanceId 流程实例ID
     * @return 响应结果
     */
    @Override
    public ResultData<Map<String, Object>> getVariables(String flowableInstanceId) {
        ResultData<Map<String, Object>> result = new ResultData<>();
        Map<String, Object> variables = runtimeService.getVariables(flowableInstanceId);
        result.setData(variables);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除流程实例
     * @param flowableInstanceId 流程实例ID
     * @return 响应结果
     */
    @Override
    public ResultData<Void> deleteInstance(String flowableInstanceId) {
        ResultData<Void> result = new ResultData<>();
        runtimeService.deleteProcessInstance(flowableInstanceId, "删除流程实例");
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 设置流程实例任务处理人
     * @param flowableTaskId 任务ID
     * @param assignee 处理人
     * @return 响应结果
     */
    @Override
    public ResultData<Void> setAssignee(String flowableTaskId, String assignee) {
        ResultData<Void> result = new ResultData<>();
        // 这个方法会把传入的值当作一个用户
        taskService.setAssignee(flowableTaskId, assignee);
//        taskService.addCandidateGroup(flowableTaskId, assignee);
//        taskService.addCandidateUser();
//        // 委派任务
//        taskService.delegateTask();
//        // 签收任务
//        taskService.resolveTask();
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 转换json为bpmnModel
     * @param processId 流程ID
     * @param bpmJson 流程json
     * @return bpmnModel
     */
    private BpmnModel convertJsonToBpmnModel(String processId, String bpmJson) {
        BpmnModel bpmnModel = new BpmnModel();
        org.flowable.bpmn.model.Process process = new org.flowable.bpmn.model.Process();
        process.setId(processId);
        bpmnModel.addProcess(process);
        JSONObject bpmObject = JSONObject.parseObject(bpmJson);
        JSONArray nodes = bpmObject.getJSONArray("nodes");
        for (Object node : nodes) {
            JSONObject el = (JSONObject) node;
            String id = el.getString("id");
            String type = el.getString("type");
            String name = el.getString("name");
            FlowElement flowElement = null;
            FlowTemplateTypeEnum templateType = FlowTemplateTypeEnum.findByValue(type);
            if (templateType == null) {
                throw new BaseException(ErrorCodeEnum.FLOW_NODE_TYPE_UNKNOWN);
            }
            flowElement = switch (templateType) {
                case START_EVENT -> new StartEvent();
                case USER_TASK -> new UserTask();
                case EXCLUSIVE_GATEWAY -> new ExclusiveGateway();
                case END_EVENT -> new EndEvent();
            };
            flowElement.setId(id);
            flowElement.setName(name);
            process.addFlowElement(flowElement);
        }
        JSONArray sequences = bpmObject.getJSONArray("sequences");
        for (Object sequence : sequences) {
            JSONObject seq = (JSONObject) sequence;
            String id = seq.getString("id");
            String sourceRef = seq.getString("sourceRef");
            String targetRef = seq.getString("targetRef");
            String condition = seq.getString("conditionExpression");
            SequenceFlow sequenceFlow = new SequenceFlow();
            sequenceFlow.setId(id);
            sequenceFlow.setSourceRef(sourceRef);
            sequenceFlow.setTargetRef(targetRef);
            if (StringUtil.isNotBlank(condition)) {
                sequenceFlow.setConditionExpression(condition);
            }
            process.addFlowElement(sequenceFlow);
        }
        return bpmnModel;
    }
}

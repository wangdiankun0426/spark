package com.spark.flow.listener;

import com.spark.bean.base.BaseContext;
import com.spark.bean.base.ResultData;
import com.spark.bean.base.SessionHolder;
import com.spark.bean.flow.entity.FlowInstance;
import com.spark.bean.flow.entity.FlowInstanceAssignee;
import com.spark.bean.flow.entity.FlowInstanceNode;
import com.spark.bean.flow.query.FlowInstanceNodeQuery;
import com.spark.bean.flow.query.FlowInstanceQuery;
import com.spark.bean.flow.query.FlowTemplateNodeQuery;
import com.spark.bean.flow.result.FlowInstanceNodeResult;
import com.spark.bean.flow.result.FlowInstanceResult;
import com.spark.bean.flow.result.FlowTemplateNodeResult;
import com.spark.dao.flow.FlowInstanceAssigneeDao;
import com.spark.dao.flow.FlowInstanceDao;
import com.spark.dao.flow.FlowInstanceNodeDao;
import com.spark.dao.flow.FlowTemplateNodeDao;
import com.spark.enums.FlowAssigneeTypeEnum;
import com.spark.enums.FlowInstanceStatusEnum;
import com.spark.flow.service.FlowableService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import org.flowable.common.engine.api.delegate.event.*;
import org.flowable.engine.delegate.event.FlowableActivityEvent;
import org.flowable.engine.delegate.event.FlowableCancelledEvent;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/1 14:44
 * 全局事件监听器
 */
@Component
public class GlobalFlowableEventListener implements FlowableEventListener {
    private static final Logger logger = LoggerFactory.getLogger(GlobalFlowableEventListener.class);
    @Autowired
    private FlowInstanceDao instanceDao;
    @Autowired
    private FlowTemplateNodeDao templateNodeDao;
    @Autowired
    private FlowInstanceNodeDao instanceNodeDao;
    @Autowired
    private FlowableService flowableService;
    @Autowired
    private FlowInstanceAssigneeDao instanceAssigneeDao;

    /**
     * 全局事件监听
     * @param event
     */
    @Override
    public void onEvent(FlowableEvent event) {
        FlowableEngineEventType type = (FlowableEngineEventType)event.getType();
        switch (type) {
            case PROCESS_STARTED:
                handleProcessStarted((FlowableEngineEntityEvent) event);
                break;
            case PROCESS_COMPLETED:
                handleProcessCompleted((FlowableEngineEntityEvent) event);
                break;
            case PROCESS_CANCELLED:
                handleProcessCancelled((FlowableCancelledEvent) event);
                break;
            case ACTIVITY_STARTED:
                handleActivityStarted((FlowableActivityEvent) event);
                break;
            case ACTIVITY_COMPLETED:
                handleActivityCompleted((FlowableActivityEvent) event);
                break;
            case TASK_CREATED:
                handleTaskCreated((FlowableEngineEntityEvent) event);
                break;
            case TASK_COMPLETED:
                handleTaskCompleted((FlowableEngineEntityEvent) event);
                break;
            case TASK_ASSIGNED:
                handleTaskAssigned((FlowableEngineEntityEvent) event);
                break;
            case ENTITY_SUSPENDED:
                handleProcessSuspended((FlowableEngineEvent) event);
                break;
            case ENTITY_ACTIVATED:
                handleProcessActivated((FlowableEngineEvent) event);
                break;
            default:
                break;
        }
    }

    /**
     * 处理流程激活
     * @param event
     */
    private void handleProcessActivated(FlowableEngineEvent event) {
        String flowableInstanceId = event.getProcessInstanceId();
        logger.info("handleProcessActivated, flowableInstanceId: {}" , flowableInstanceId);
    }

    /**
     * 处理流程挂起
     * @param event
     */
    private void handleProcessSuspended(FlowableEngineEvent event) {
        String flowableInstanceId = event.getProcessInstanceId();
        logger.info("handleProcessSuspended, flowableInstanceId: {}" , flowableInstanceId);
    }

    /**
     * 处理流程启动
     * PROCESS_CREATED 先于 PROCESS_STARTED
     * PROCESS_CREATED: 适合做创建时的初始化工作
     * PROCESS_STARTED: 适合做流程真正开始后的监控或日志记录
     * 简单来说，PROCESS_CREATED 是 "已创建"状态，PROCESS_STARTED 是"已启动"状态。
     * @param event
     */
    private void handleProcessStarted(FlowableEngineEntityEvent event) {
        String flowableInstanceId = event.getProcessInstanceId();
        logger.info("handleProcessStarted, flowableInstanceId: {}" , flowableInstanceId);
        BaseContext context = SessionHolder.getContext();
        FlowInstance instance = context.getVal(FlowInstance.class);
        instance.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        instance.setFlowableInstanceId(flowableInstanceId);
        int count = instanceDao.insertDB(instance);
        if (count <= 0) {
            logger.error("handleProcessStarted error, insert db fail");
        }
    }

    /**
     * 处理流程完成
     * @param event
     */
    private void handleProcessCompleted(FlowableEngineEntityEvent event) {
        String instanceId = event.getProcessInstanceId();
        logger.info("handleProcessCompleted, instanceId: {}" , instanceId);
        FlowInstance instance = new FlowInstance();
        instance.setFlowableInstanceId(instanceId);
        instance.setStatus(FlowInstanceStatusEnum.COMPLETED.getValue());
        int count = instanceDao.updateByFlowableInstanceId(instance);
        if (count <= 0) {
            logger.error("handleProcessCompleted error, update db fail");
        }
    }

    /**
     * 处理流程取消
     * @param event
     */
    private void handleProcessCancelled(FlowableCancelledEvent event) {
        String instanceId = event.getProcessInstanceId();
        logger.info("handleProcessCancelled, instanceId: {}" , instanceId);
        FlowInstance instance = new FlowInstance();
        instance.setFlowableInstanceId(instanceId);
        instance.setStatus(FlowInstanceStatusEnum.REJECTED.getValue());
        int count = instanceDao.updateByFlowableInstanceId(instance);
        if (count <= 0) {
            logger.error("handleProcessCancelled error, update db fail");
        }
    }

    /**
     * 处理活动开始
     * @param event
     */
    private void handleActivityStarted(FlowableActivityEvent event) {
        String nodeId = event.getActivityId();
        String type = event.getActivityType();
        String name = event.getActivityName();
        String flowableInstanceId = event.getProcessInstanceId();
        logger.info("handleActivityStarted flowableInstanceId : {} , nodeId: {}, type: {}, name: {}", flowableInstanceId, nodeId, type, name);
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setFlowableInstanceId(flowableInstanceId);
        FlowInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            logger.error("handleActivityStarted error, instance not exist");
            return;
        }
        FlowInstanceNode instanceNode = new FlowInstanceNode();
        instanceNode.setInstanceId(instanceResult.getId());
        instanceNode.setNodeId(nodeId);
        instanceNode.setName(name);
        instanceNode.setType(type);
        instanceNode.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        int count = instanceNodeDao.insertDB(instanceNode);
    }

    /**
     * 处理活动完成
     * @param event
     */
    private void handleActivityCompleted(FlowableActivityEvent event) {
        String nodeId = event.getActivityId();
        String type = event.getActivityType();
        String name = event.getActivityName();
        String flowableInstanceId = event.getProcessInstanceId();
        logger.info("handleActivityCompleted flowableInstanceId : {} , nodeId: {}, type: {}, name: {}", flowableInstanceId, nodeId, type, name);
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setFlowableInstanceId(flowableInstanceId);
        FlowInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            logger.error("handleActivityCompleted error, instance not exist");
            return;
        }
        FlowInstanceNodeQuery instanceNodeQuery = new FlowInstanceNodeQuery();
        instanceNodeQuery.setInstanceId(instanceResult.getId());
        instanceNodeQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        instanceNodeQuery.setNodeId(nodeId);
        FlowInstanceNodeResult instanceNodeResult = instanceNodeDao.queryInstanceNode(instanceNodeQuery);
        if (instanceNodeResult == null) {
            return;
        }
        FlowInstanceNode instanceNode = new FlowInstanceNode();
        instanceNode.setId(instanceNodeResult.getId());
        instanceNode.setStatus(FlowInstanceStatusEnum.COMPLETED.getValue());
        int count = instanceNodeDao.updateDBById(instanceNode);
    }

    /**
     * 处理任务创建
     * @param event
     */
    private void handleTaskCreated(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        String flowableInstanceId = task.getProcessInstanceId();
        String nodeId = task.getTaskDefinitionKey();
        logger.info("handleTaskCreated, flowableInstanceId : {}, taskId: {}, nodeId : {}", flowableInstanceId, task.getId(), nodeId);
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setFlowableInstanceId(task.getProcessInstanceId());
        FlowInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            logger.error("handleTaskCreated error, instance not exist");
            return;
        }
        FlowInstanceNodeQuery instanceNodeQuery = new FlowInstanceNodeQuery();
        instanceNodeQuery.setInstanceId(instanceResult.getId());
        instanceNodeQuery.setNodeId(nodeId);
        instanceNodeQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        FlowInstanceNodeResult instanceNodeResult = instanceNodeDao.queryInstanceNode(instanceNodeQuery);
        if (instanceNodeResult == null) {
            logger.error("handleTaskCreated error, instance node not exist");
            return;
        }
        FlowTemplateNodeQuery templateNodeQuery = new FlowTemplateNodeQuery();
        templateNodeQuery.setTemplateId(instanceResult.getTemplateId());
        templateNodeQuery.setRevId(instanceResult.getTemplateRevId());
        templateNodeQuery.setNodeId(nodeId);
        FlowTemplateNodeResult templateNodeResult = templateNodeDao.queryTemplateNode(templateNodeQuery);
        if (templateNodeResult == null) {
            logger.error("handleTaskCreated error, template node not exist");
            return;
        }
        Integer assigneeType = templateNodeResult.getAssigneeType();
        FlowAssigneeTypeEnum assigneeTypeEnum = FlowAssigneeTypeEnum.indexOf(assigneeType);
        if (assigneeTypeEnum == null) {
            logger.error("handleTaskCreated error, template node assignee type error");
            return;
        }
        List<Long> assigneeIds = new ArrayList<>();
        switch (assigneeTypeEnum) {
            case USER:
                if (StringUtil.isNotBlank(templateNodeResult.getAssignee())) {
                    assigneeIds = Arrays.stream(templateNodeResult.getAssignee().split(",")).map(Long::parseLong).toList();
                }
                break;
            case FLOW_APPROVER:
                assigneeIds.add(instanceResult.getCreatedBy());
                break;
            case SYSTEM:
                flowableService.completeTask(flowableInstanceId,  null);
                break;
            default:
                break;
        }
        if (CollectionUtil.isEmpty(assigneeIds)) {
            logger.warn("handleTaskCreated error, template node assigneeIds is null");
            return;
        }
        String setId = UUID.randomUUID().toString().replaceAll("-", "");
        assigneeIds.forEach(assigneeId -> {
            FlowInstanceAssignee instanceAssignee = new FlowInstanceAssignee();
            instanceAssignee.setInstanceId(instanceResult.getId());
            instanceAssignee.setInstanceNodeId(instanceNodeResult.getId());
            instanceAssignee.setAssigneeId(assigneeId);
            instanceAssignee.setAssigneeSetId(setId);
            instanceAssignee.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
            int count = instanceAssigneeDao.insertDB(instanceAssignee);
        });
        flowableService.setAssignee(task.getId(), setId);
        FlowInstanceNode instanceNode = new FlowInstanceNode();
        instanceNode.setId(instanceNodeResult.getId());
        instanceNode.setAssigneeSetId(setId);
        int count = instanceNodeDao.updateDBById(instanceNode);
    }

    /**
     * 处理任务完成
     * @param event
     */
    private void handleTaskCompleted(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        String flowableInstanceId = task.getProcessInstanceId();
        String nodeId = task.getTaskDefinitionKey();
        logger.info("handleTaskCompleted, flowableInstanceId : {}, taskId: {}, nodeId : {}", flowableInstanceId, task.getId(), nodeId);
        ResultData<Map<String, Object>> variablesData = flowableService.getVariables(flowableInstanceId);
        Map<String, Object> variables = variablesData.getData();
        Object status = variables.get("status");
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setFlowableInstanceId(task.getProcessInstanceId());
        FlowInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            logger.error("handleTaskCompleted error, instance not exist");
            return;
        }
        FlowInstanceNodeQuery instanceNodeQuery = new FlowInstanceNodeQuery();
        instanceNodeQuery.setInstanceId(instanceResult.getId());
        instanceNodeQuery.setNodeId(nodeId);
        FlowInstanceNodeResult instanceNodeResult = instanceNodeDao.queryInstanceNode(instanceNodeQuery);
        if (instanceNodeResult == null) {
            logger.error("handleTaskCompleted error, instance node not exist");
            return;
        }
        FlowInstanceNode instanceNode = new FlowInstanceNode();
        instanceNode.setId(instanceNodeResult.getId());
        logger.info("handleTaskCompleted, status : {}", status);
        instanceNode.setStatus(status == null ? FlowInstanceStatusEnum.COMPLETED.getValue() : (Integer) status);
        int count = instanceNodeDao.updateDBById(instanceNode);
        if (count < 1) {
            logger.error("handleTaskCompleted error, update instance node error");
            return;
        }
        if (FlowInstanceStatusEnum.REJECTED.getValue().equals(status)) {
            //任务被驳回 直接将流程删除 会触发PROCESS_CANCELLED事件
            flowableService.deleteInstance(flowableInstanceId);
        }
    }

    /**
     * 任务分配完成
     * @param event
     */
    private void handleTaskAssigned(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        String flowableInstanceId = task.getProcessInstanceId();
        String nodeId = task.getTaskDefinitionKey();
        String assignee = task.getAssignee();
        logger.info("handleTaskAssigned, flowableInstanceId : {}, taskId: {}, nodeId : {} , assignee {}", flowableInstanceId,
                task.getId(), nodeId, assignee);

    }


    @Override
    public boolean isFailOnException() {
        // 如果监听器抛异常，是否让流程失败？一般设为 false
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        // 是否在事务生命周期事件中触发（如 COMMITTED）
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }
}
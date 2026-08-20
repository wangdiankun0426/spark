package com.spark.flow.service.impl;

import com.spark.bean.base.ResultData;
import com.spark.bean.flow.entity.FlowInstanceAssignee;
import com.spark.bean.flow.entity.FlowInstanceNode;
import com.spark.bean.task.entity.TaskInstance;
import com.spark.bean.task.entity.TaskInstanceParam;
import com.spark.bean.flow.query.FlowInstanceNodeQuery;
import com.spark.bean.flow.query.FlowInstanceQuery;
import com.spark.bean.flow.query.FlowTemplateNodeQuery;
import com.spark.bean.flow.query.FlowTemplateNodeTaskQuery;
import com.spark.bean.flow.query.FlowTemplateNodeTaskParamQuery;
import com.spark.bean.flow.result.FlowInstanceNodeResult;
import com.spark.bean.flow.result.FlowInstanceResult;
import com.spark.bean.flow.result.FlowTemplateNodeResult;
import com.spark.bean.flow.result.FlowTemplateNodeTaskResult;
import com.spark.bean.flow.result.FlowTemplateNodeTaskParamResult;
import com.spark.bean.form.query.FormObjValueQuery;
import com.spark.bean.form.result.FormObjValueResult;
import com.spark.bean.system.query.RoleUserQuery;
import com.spark.bean.system.query.UserQuery;
import com.spark.bean.system.result.RoleUserResult;
import com.spark.bean.system.result.UserResult;
import com.spark.bean.task.query.TaskTemplateQuery;
import com.spark.bean.task.result.TaskTemplateResult;
import com.spark.constant.TaskParamCode;
import com.spark.dao.flow.FlowInstanceAssigneeDao;
import com.spark.dao.flow.FlowInstanceDao;
import com.spark.dao.flow.FlowInstanceNodeDao;
import com.spark.dao.flow.FlowTemplateNodeDao;
import com.spark.dao.flow.FlowTemplateNodeTaskDao;
import com.spark.dao.flow.FlowTemplateNodeTaskParamDao;
import com.spark.dao.task.TaskInstanceDao;
import com.spark.dao.task.TaskInstanceParamDao;
import com.spark.dao.task.TaskTemplateDao;
import com.spark.dao.form.FormObjValueDao;
import com.spark.dao.system.RoleUserDao;
import com.spark.dao.system.UserDao;
import com.spark.enums.*;
import com.spark.flow.service.BaseFlowService;
import com.spark.flow.service.FlowEventService;
import com.spark.flow.service.FlowMessageService;
import com.spark.flow.service.FlowableService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.DateUtil;
import com.spark.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/8/6 22:43
 */
@Service
public class FlowEventServiceImpl extends BaseFlowService implements FlowEventService {
    private final static Logger logger = LoggerFactory.getLogger(FlowEventServiceImpl.class);
    @Autowired
    private FlowInstanceDao instanceDao;
    @Autowired
    private FlowInstanceNodeDao instanceNodeDao;
    @Autowired
    private FlowTemplateNodeDao templateNodeDao;
    @Autowired
    private FlowInstanceAssigneeDao instanceAssigneeDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private RoleUserDao roleUserDao;
    @Autowired
    private FlowableService flowableService;
    @Autowired
    private FlowMessageService flowMessageService;
    @Autowired
    private FormObjValueDao objValueDao;
    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private TaskInstanceParamDao taskInstanceParamDao;
    @Autowired
    private FlowTemplateNodeTaskDao templateNodeTaskDao;
    @Autowired
    private FlowTemplateNodeTaskParamDao templateNodeTaskParamDao;
    @Autowired
    private TaskTemplateDao taskTemplateDao;

    /**
     * 用户任务入口
     * @param instanceId 流程实例ID
     * @param nodeId 节点定义Key
     * @param taskId 任务实例ID
     * @return
     */
    @Override
    public ResultData<Void> onCreatedUserTask(String instanceId, String nodeId, String taskId) {
        ResultData<Void> result = new ResultData<>();
        if (StringUtil.isBlank(instanceId) || StringUtil.isBlank(nodeId) || StringUtil.isBlank(taskId)) {
            logger.error("onCreatedUserTask, instanceId={}, nodeId={}, taskId={}", instanceId, nodeId, taskId);
            return result;
        }
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setFlowableInstanceId(instanceId);
        FlowInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            logger.error("handleTaskCreated error, instance not exist");
            return result;
        }
        FlowInstanceNodeQuery instanceNodeQuery = new FlowInstanceNodeQuery();
        instanceNodeQuery.setInstanceId(instanceResult.getId());
        instanceNodeQuery.setNodeId(nodeId);
        instanceNodeQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        FlowInstanceNodeResult instanceNodeResult = instanceNodeDao.queryInstanceNode(instanceNodeQuery);
        if (instanceNodeResult == null) {
            logger.error("handleTaskCreated error, instance node not exist");
            return result;
        }
        FlowTemplateNodeQuery templateNodeQuery = new FlowTemplateNodeQuery();
        templateNodeQuery.setTemplateId(instanceResult.getTemplateId());
        templateNodeQuery.setRevId(instanceResult.getTemplateRevId());
        templateNodeQuery.setNodeId(nodeId);
        FlowTemplateNodeResult templateNodeResult = templateNodeDao.queryTemplateNode(templateNodeQuery);
        if (templateNodeResult == null) {
            logger.error("handleTaskCreated error, template node not exist");
            return result;
        }
        Integer assigneeType = templateNodeResult.getAssigneeType();
        FlowAssigneeTypeEnum assigneeTypeEnum = FlowAssigneeTypeEnum.indexOf(assigneeType);
        if (assigneeTypeEnum == null) {
            logger.error("handleTaskCreated error, template node assignee type error");
            return result;
        }
        // 获取流程审批人
        List<Long> assigneeIds = this.queryFlowAssignee(instanceId, templateNodeResult.getAssignee(),assigneeTypeEnum);
        Integer permission = templateNodeResult.getPermission();
        FlowApproveTypeEnum approveTypeEnum = FlowApproveTypeEnum.indexOf(templateNodeResult.getApproveType());
        logger.info("assigneeIds={},permission={},approveType={}", assigneeIds, permission, approveTypeEnum.getName());
        if (CollectionUtil.isNotEmpty(assigneeIds)) {
            String setId = UUID.randomUUID().toString().replaceAll("-", "");
            for (int i = 0; i < assigneeIds.size(); i++) {
                FlowInstanceAssignee instanceAssignee = new FlowInstanceAssignee();
                instanceAssignee.setInstanceId(instanceResult.getId());
                instanceAssignee.setInstanceNodeId(instanceNodeResult.getId());
                instanceAssignee.setAssigneeId(assigneeIds.get(i));
                instanceAssignee.setAssigneeSetId(setId);
                instanceAssignee.setSort(i + 1);
                if (FlowApproveTypeEnum.SEQUENTIAL.equals(approveTypeEnum) && i > 0) {
                    instanceAssignee.setStatus(FlowInstanceStatusEnum.WAITING.getValue());
                } else {
                    instanceAssignee.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
                }
                instanceAssigneeDao.insertDB(instanceAssignee);
            }
            flowableService.setAssignee(taskId, setId);
            FlowInstanceNode instanceNode = new FlowInstanceNode();
            instanceNode.setId(instanceNodeResult.getId());
            instanceNode.setAssigneeSetId(setId);
            int count = instanceNodeDao.updateDBById(instanceNode);
            // 构造催办任务实例
            this.generateFlowUrgeTaskInstance(templateNodeResult.getUrgeEnabled(), templateNodeResult.getUrgeInterval(), instanceResult.getId(), instanceNodeResult.getId());
            // 发送待办通知
            flowMessageService.sendFlowNotice(instanceId, MessageTypeEnum.FLOW_TODO.getType());
        } else if (FlowAssigneeTypeEnum.SYSTEM.equals(assigneeTypeEnum) || (permission != null && (permission & FlowTemplateNodePermissionEnum.AUTO_APSS.getValue()) == FlowTemplateNodePermissionEnum.AUTO_APSS.getValue())) {
            // 系统自动通过
            // 任务创建事件内任务尚未提交数据库，必须按任务ID完成
            flowableService.completeTaskById(taskId, null);
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 节点任务触发
     * @param flowableInstanceId 流程实例ID（Flowable实例id）
     * @param nodeId
     * @param executeType
     * @return 触发结果
     */
    @Override
    public ResultData<Void> onNodeTask(String flowableInstanceId, String nodeId, Integer executeType) {
        ResultData<Void> result = new ResultData<>();
        if (StringUtil.isBlank(flowableInstanceId) || StringUtil.isBlank(nodeId) || executeType == null) {
            logger.error("onNodeTask instanceId={}, nodeId={}, executeType={}", flowableInstanceId, nodeId, executeType);
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setFlowableInstanceId(flowableInstanceId);
        FlowInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            logger.error("onNodeTask error, instance not exist");
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_EXIST);
            return result;
        }
        FlowTemplateNodeTaskQuery nodeTaskQuery = new FlowTemplateNodeTaskQuery();
        nodeTaskQuery.setTemplateId(instanceResult.getTemplateId());
        nodeTaskQuery.setRevId(instanceResult.getTemplateRevId());
        nodeTaskQuery.setNodeId(nodeId);
        nodeTaskQuery.setExecuteType(executeType);
        List<FlowTemplateNodeTaskResult> nodeTaskList = templateNodeTaskDao.queryTemplateNodeTaskList(nodeTaskQuery);
        if (CollectionUtil.isEmpty(nodeTaskList)) {
            result.setCode(ResultData.OK);
            return result;
        }
        List<Long> nodeTaskIds = nodeTaskList.stream().map(FlowTemplateNodeTaskResult::getId).toList();
        FlowTemplateNodeTaskParamQuery paramQuery = new FlowTemplateNodeTaskParamQuery();
        paramQuery.setNodeTaskIds(nodeTaskIds);
        List<FlowTemplateNodeTaskParamResult> paramList = templateNodeTaskParamDao.queryTemplateNodeTaskParamList(paramQuery);
        Map<Long, List<FlowTemplateNodeTaskParamResult>> paramMap = paramList.stream().collect(Collectors.groupingBy(FlowTemplateNodeTaskParamResult::getNodeTaskId));
        Map<String, String> formValueMap = new HashMap<>();
        Map<String, String> formTxtMap = new HashMap<>();
        Long instanceId = instanceResult.getId();
        FormObjValueQuery objValueQuery = new FormObjValueQuery();
        objValueQuery.setObjId(instanceId);
        List<FormObjValueResult> valueList = objValueDao.queryFormObjValueList(objValueQuery);
        if (CollectionUtil.isNotEmpty(valueList)) {
            formValueMap = valueList.stream().collect(Collectors.toMap(FormObjValueResult::getCode, FormObjValueResult::getValue, (v1, v2) -> v2));
            formTxtMap = valueList.stream().collect(Collectors.toMap(FormObjValueResult::getCode, FormObjValueResult::getShowValue, (v1, v2) -> v2));
        }
        String setId = UUID.randomUUID().toString().replaceAll("-", "");
        for (FlowTemplateNodeTaskResult nodeTaskResult : nodeTaskList) {
            List<FlowTemplateNodeTaskParamResult> params = paramMap.get(nodeTaskResult.getId());
            // 构造流程任务实例
            this.generateFlowTaskInstance(instanceId, nodeTaskResult, params, formValueMap, formTxtMap, setId);
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 用户任务完成
     * @param instanceId
     * @param nodeId
     * @param status
     * @return
     */
    @Override
    public ResultData<Void> onCompletedUserTask(String instanceId, String nodeId, Integer status) {
        ResultData<Void> result = new ResultData<>();
        if (StringUtil.isBlank(instanceId) || StringUtil.isBlank(nodeId) || status == null) {
            logger.error("onCompletedUserTask instanceId={}, nodeId={}, status={}", instanceId, nodeId, status);
            return result;
        }
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setFlowableInstanceId(instanceId);
        FlowInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            logger.error("onCompletedUserTask error, instance not exist");
            return result;
        }
        FlowInstanceNodeQuery instanceNodeQuery = new FlowInstanceNodeQuery();
        instanceNodeQuery.setInstanceId(instanceResult.getId());
        instanceNodeQuery.setNodeId(nodeId);
        FlowInstanceNodeResult instanceNodeResult = instanceNodeDao.queryInstanceNode(instanceNodeQuery);
        if (instanceNodeResult == null) {
            logger.error("onCompletedUserTask error, instance node not exist");
            return result;
        }
        FlowInstanceNode instanceNode = new FlowInstanceNode();
        instanceNode.setId(instanceNodeResult.getId());
        logger.info("onCompletedUserTask status={}", status);
        instanceNode.setStatus(status);
        int count = instanceNodeDao.updateDBById(instanceNode);
        if (count < 1) {
            logger.error("onCompletedUserTask error, update instance node error");
            return result;
        }
        if (FlowInstanceStatusEnum.REJECTED.getValue().equals(status)) {
            //任务被驳回 直接将流程删除 会触发PROCESS_CANCELLED事件
            result = flowableService.deleteInstance(instanceId);
        } else {
            result.setCode(ResultData.OK);
        }
        return result;
    }

    /**
     * 查询流程审批人
     *
     * @param instanceId
     * @param assignee
     * @param assigneeTypeEnum
     * @return
     */
    private List<Long> queryFlowAssignee(String instanceId, String assignee, FlowAssigneeTypeEnum assigneeTypeEnum) {
        List<Long> assigneeIds = new ArrayList<>();
        if (StringUtil.isBlank(instanceId) || assigneeTypeEnum == null) {
            logger.error("instanceId or assigneeTypeEnum is null");
            return null;
        }
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setFlowableInstanceId(instanceId);
        FlowInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            logger.error("handleTaskCreated error, instance not exist");
            return null;
        }
        switch (assigneeTypeEnum) {
            case FLOW_APPROVER:
                assigneeIds.add(instanceResult.getCreatedBy());
                break;
            case USER:
                assigneeIds = this.queryAssigneeByUser(assignee);
                break;
            case DEPT:
                assigneeIds = this.queryAssigneeByDept(assignee);
                break;
            case ROLE:
                assigneeIds = this.queryAssigneeByRole(assignee);
                break;
            case FORM_DATA:
                assigneeIds = this.queryAssigneeByFormData(instanceId, assignee);
                break;
            default:
                break;
        }
       return assigneeIds;
    }

    /**
     * 从用户中获取审批人
     * @param assignee
     * @return
     */
    private List<Long> queryAssigneeByUser(String assignee) {
        if (StringUtil.isBlank(assignee)) {
            logger.error("queryAssigneeByUser, assignee=null");
            return null;
        }
        return Arrays.stream(assignee.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    /**
     * 从部门中获取审批人
     * @param assignee
     * @return
     */
    private List<Long> queryAssigneeByDept(String assignee) {
        if (StringUtil.isBlank(assignee)) {
            logger.error("queryAssigneeByDept, assignee=null");
            return null;
        }
        List<Long> deptIds = Arrays.stream(assignee.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        UserQuery userQuery = new UserQuery();
        userQuery.setPage(false);
        userQuery.setDeptIds(deptIds);
        List<UserResult> userList = userDao.queryUserList(userQuery);
        if (CollectionUtil.isEmpty(userList)) {
            logger.error("queryAssigneeByDept, userList=null");
            return null;
        }
        return userList.stream()
                .map(UserResult::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 从角色中获取审批人
     * @param assignee
     * @return
     */
    private List<Long> queryAssigneeByRole(String assignee) {
        if (StringUtil.isBlank(assignee)) {
            logger.error("queryAssigneeByRole, assignee=null");
            return  null;
        }
        List<Long> roleIds = Arrays.stream(assignee.split(","))
                .map(Long::parseLong)
                .toList();
        RoleUserQuery roleUserQuery = new RoleUserQuery();
        roleUserQuery.setRoleIds(roleIds);
        roleUserQuery.setPage(false);
        List<RoleUserResult> roleUserList = roleUserDao.queryRoleUserList(roleUserQuery);
        if (CollectionUtil.isEmpty(roleUserList)) {
            logger.error("queryAssigneeByRole, roleUserList=null");
            return  null;
        }
        return roleUserList.stream()
                .map(RoleUserResult::getUserId)
                .toList();
    }

    /**
     * 从表单数据中获取审批人
     * @param instanceId
     * @param assignee
     * @return
     */
    private List<Long> queryAssigneeByFormData(String instanceId, String assignee) {
        if (StringUtil.isBlank(assignee) || StringUtil.isBlank(instanceId)) {
            logger.error("queryAssigneeByFormData, instanceId=null");
            return null;
        }
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setFlowableInstanceId(instanceId);
        FlowInstanceResult flowInstanceResult = instanceDao.queryInstance(instanceQuery);
        if (flowInstanceResult == null) {
            logger.error("queryAssigneeByFormData, flowInstanceResult=null");
            return null;
        }
        List<String> codes = Arrays.stream(assignee.split(",")).toList();
        FormObjValueQuery objValueQuery = new FormObjValueQuery();
        objValueQuery.setObjId(flowInstanceResult.getId());
        objValueQuery.setCodes(codes);
        List<FormObjValueResult> valueList = objValueDao.queryFormObjValueList(objValueQuery);
        if (CollectionUtil.isEmpty(valueList)) {
            logger.error("queryAssigneeByFormData, valueList=null");
            return null;
        }
        List<Long> assigneeIds = new ArrayList<>();
        for (FormObjValueResult valueResult : valueList) {
            String fieldType = valueResult.getType();
            String value = valueResult.getValue();
            if (StringUtil.isBlank(fieldType) || StringUtil.isBlank(value)) {
                logger.warn("queryAssigneeByFormData, fieldType=null, value=null");
                continue;
            }
            if (FormFieldTypeEnum.SELECT_USER.getValue().equals(fieldType)) {
                List<Long> userIds = Arrays.stream(value.split(","))
                        .filter(StringUtil::isNotBlank)
                        .map(Long::parseLong)
                        .toList();
                if (CollectionUtil.isNotEmpty(userIds)) {
                    assigneeIds.addAll(userIds);
                }
            } else if (FormFieldTypeEnum.SELECT_DEPT.getValue().equals(fieldType)) {
                List<Long> deptIds = Arrays.stream(value.split(","))
                        .filter(StringUtil::isNotBlank)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                UserQuery userQuery = new UserQuery();
                userQuery.setPage(false);
                userQuery.setDeptIds(deptIds);
                List<UserResult> userList = userDao.queryUserList(userQuery);
                if (CollectionUtil.isNotEmpty(userList)) {
                    List<Long> userIds = userList.stream()
                            .map(UserResult::getId)
                            .distinct()
                            .toList();
                    assigneeIds.addAll(userIds);
                }
            } else if (FormFieldTypeEnum.SELECT_ROLE.getValue().equals(fieldType)) {
                List<Long> roleIds = Arrays.stream(value.split(","))
                        .filter(StringUtil::isNotBlank)
                        .map(Long::parseLong)
                        .toList();
                RoleUserQuery roleUserQuery = new RoleUserQuery();
                roleUserQuery.setRoleIds(roleIds);
                roleUserQuery.setPage(false);
                List<RoleUserResult> roleUserList = roleUserDao.queryRoleUserList(roleUserQuery);
                if (CollectionUtil.isNotEmpty(roleUserList)) {
                    List<Long> userIds = roleUserList.stream()
                            .map(RoleUserResult::getUserId)
                            .toList();
                    assigneeIds.addAll(userIds);
                }
            } else {
                logger.warn("queryAssigneeByFormData, fieldType={}", fieldType);
            }
        }
        return assigneeIds;
    }

    /**
     * 构造催办任务实例
     *
     * @param urgeEnabled
     * @param urgeInterval
     * @param instanceId
     * @param instanceNodeId
     */
    private void generateFlowUrgeTaskInstance(Boolean urgeEnabled, Integer urgeInterval, Long instanceId, Long instanceNodeId) {
        if (!Boolean.TRUE.equals(urgeEnabled) || urgeInterval == null || urgeInterval == 0) {
            return;
        }
        TaskInstance urgeTask = new TaskInstance();
        urgeTask.setTaskType(TaskTypeEnum.FLOW_URGE.getValue());
        urgeTask.setObjId(instanceId);
        urgeTask.setObjType(ObjectTypeEnum.FLOW_INSTANCE.getValue());
        urgeTask.setSort(1);
        urgeTask.setTaskTime(DateUtil.offsetHour(new Date(), urgeInterval));
        urgeTask.setIntervalHours(urgeInterval);
        urgeTask.setStatus(TaskStatusEnum.PENDING.getValue());
        String setId = UUID.randomUUID().toString().replaceAll("-", "");
        urgeTask.setSetId(setId);
        int urgeCount = taskInstanceDao.insertDB(urgeTask);
        if (urgeCount < 1) {
            logger.error("onCreatedUserTask error, insert urge task fail");
            return;
        }
        TaskInstanceParam urgeTaskParam = new TaskInstanceParam();
        urgeTaskParam.setTaskId(urgeTask.getId());
        urgeTaskParam.setCode(TaskParamCode.FLOW_INSTANCE_NODE_ID);
        urgeTaskParam.setValue(instanceNodeId+"");
        int paramCount = taskInstanceParamDao.insertDB(urgeTaskParam);
        if (paramCount < 1) {
            logger.error("onCreatedUserTask error, insert urge task param fail");
        }
    }

    /**
     * 构造流程任务实例
     * @param instanceId
     * @param nodeTaskResult
     * @param params
     * @param formValueMap
     * @param formTxtMap
     * @param setId
     */
    private void generateFlowTaskInstance(Long instanceId, FlowTemplateNodeTaskResult nodeTaskResult, List<FlowTemplateNodeTaskParamResult> params, Map<String, String> formValueMap, Map<String, String> formTxtMap, String setId) {
        TaskTemplateQuery templateQuery = new TaskTemplateQuery();
        templateQuery.setId(nodeTaskResult.getTaskTemplateId());
        TaskTemplateResult taskTemplate = taskTemplateDao.queryTaskTemplate(templateQuery);
        if (taskTemplate == null) {
            logger.error("task template not exist, templateId={}", nodeTaskResult.getTaskTemplateId());
            return;
        }
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskType(taskTemplate.getTaskType());
        taskInstance.setObjId(instanceId);
        taskInstance.setObjType(ObjectTypeEnum.FLOW_INSTANCE.getValue());
        taskInstance.setSetId(setId);
        taskInstance.setSort(nodeTaskResult.getSort() == null ? 1 : nodeTaskResult.getSort());
        taskInstance.setTaskTime(new Date());
        taskInstance.setStatus(TaskStatusEnum.PENDING.getValue());
        int count = taskInstanceDao.insertDB(taskInstance);
        if (count < 1) {
            logger.error("insert task instance fail");
            return;
        }
        if (CollectionUtil.isEmpty(params)) {
            return;
        }
        for (FlowTemplateNodeTaskParamResult param : params) {
            TaskInstanceParam taskInstanceParam = new TaskInstanceParam();
            taskInstanceParam.setTaskId(taskInstance.getId());
            taskInstanceParam.setCode(param.getCode());
            String value = super.generateFlowValue(param.getValue(), null, formValueMap, formTxtMap);
            taskInstanceParam.setValue(value);
            count = taskInstanceParamDao.insertDB(taskInstanceParam);
            if (count < 1) {
                logger.error("insert task instance param fail");
            }
        }
    }

}

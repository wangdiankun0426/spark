package com.spark.flow.service.impl;

import com.spark.bean.base.ResultData;
import com.spark.bean.flow.entity.FlowInstanceAssignee;
import com.spark.bean.flow.entity.FlowInstanceNode;
import com.spark.bean.flow.query.FlowInstanceNodeQuery;
import com.spark.bean.flow.query.FlowInstanceQuery;
import com.spark.bean.flow.query.FlowTemplateNodeQuery;
import com.spark.bean.flow.result.FlowInstanceNodeResult;
import com.spark.bean.flow.result.FlowInstanceResult;
import com.spark.bean.flow.result.FlowTemplateNodeResult;
import com.spark.bean.form.query.FormObjValueQuery;
import com.spark.bean.form.result.FormObjValueResult;
import com.spark.bean.system.query.RoleUserQuery;
import com.spark.bean.system.query.UserQuery;
import com.spark.bean.system.result.RoleUserResult;
import com.spark.bean.system.result.UserResult;
import com.spark.dao.flow.FlowInstanceAssigneeDao;
import com.spark.dao.flow.FlowInstanceDao;
import com.spark.dao.flow.FlowInstanceNodeDao;
import com.spark.dao.flow.FlowTemplateNodeDao;
import com.spark.dao.form.FormObjValueDao;
import com.spark.dao.system.RoleUserDao;
import com.spark.dao.system.UserDao;
import com.spark.enums.*;
import com.spark.flow.service.FlowEventService;
import com.spark.flow.service.FlowMessageService;
import com.spark.flow.service.FlowableService;
import com.spark.utils.CollectionUtil;
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
public class FlowEventServiceImpl implements FlowEventService {
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
        logger.info("assigneeIds={},permission={}", assigneeIds, permission);
        if (CollectionUtil.isNotEmpty(assigneeIds)) {
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
            flowableService.setAssignee(taskId, setId);
            FlowInstanceNode instanceNode = new FlowInstanceNode();
            instanceNode.setId(instanceNodeResult.getId());
            instanceNode.setAssigneeSetId(setId);
            int count = instanceNodeDao.updateDBById(instanceNode);
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
}

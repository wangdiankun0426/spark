package com.spark.flow.service.impl;

import com.spark.common.bean.base.BaseContext;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.flow.entity.FlowInstance;
import com.spark.common.bean.flow.entity.FlowInstanceAssignee;
import com.spark.common.bean.flow.entity.FlowInstanceDiscuss;
import com.spark.common.bean.flow.query.*;
import com.spark.common.bean.flow.result.*;
import com.spark.common.bean.flow.vo.FlowInstanceVO;
import com.spark.common.bean.form.entity.FormObjValue;
import com.spark.common.bean.form.query.FormObjValueQuery;
import com.spark.common.bean.form.query.FormVersionQuery;
import com.spark.common.bean.form.result.FormObjValueResult;
import com.spark.common.bean.form.result.FormVersionResult;
import com.spark.common.bean.form.vo.FormObjValueVO;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.dao.flow.*;
import com.spark.dao.form.FormObjValueDao;
import com.spark.dao.form.FormVersionDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.FlowApproveTypeEnum;
import com.spark.common.enums.FlowInstanceLevelEnum;
import com.spark.common.enums.FlowInstanceStatusEnum;
import com.spark.common.enums.FlowTemplateNodePermissionEnum;
import com.spark.common.enums.MessageTypeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.flow.service.FlowMessageService;
import com.spark.flow.service.FlowableService;
import com.spark.flow.service.IFlowInstanceService;
import com.spark.form.service.IFormObjValueService;
import com.spark.manage.BaseService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.spark.common.utils.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.common.utils.BeanUtil;
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
 * @since 2025-11-02 15:47:22
 */
@LogPrint
@Service
public class InstanceServiceImpl extends BaseService<FlowInstanceQuery, FlowInstanceResult> implements IFlowInstanceService {
    private final static Logger logger = LoggerFactory.getLogger(InstanceServiceImpl.class);
    @Autowired
    private FlowInstanceDao instanceDao;
    @Autowired
    private FlowableService flowableService;
    @Autowired
    private IFormObjValueService formObjValueService;
    @Autowired
    private FlowInstanceNodeDao instanceNodeDao;
    @Autowired
    private FlowTemplateVersionDao templateVersionDao;
    @Autowired
    private FormVersionDao formVersionDao;
    @Autowired
    private FormObjValueDao formObjValueDao;
    @Autowired
    private FlowInstanceDiscussDao instanceDiscussDao;
    @Autowired
    private FlowInstanceAssigneeDao instanceAssigneeDao;
    @Autowired
    private FlowTemplateNodeDao templateNodeDao;
    @Autowired
    private FlowMessageService flowMessageService;

    /**
     * 创建流程实例
     * @param instanceVO 流程实例数据
     * @return 创建结果
     */
    @Override
    public ResultData<Void> createInstance(FlowInstanceVO instanceVO) {
        ResultData<Void> result = new ResultData<>();
        if (instanceVO == null || StringUtil.isBlank(instanceVO.getName()) || instanceVO.getTemplateId() == null || instanceVO.getTemplateRevId() == null
                || instanceVO.getFormId() == null || instanceVO.getFormRevId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FlowInstance instance = new FlowInstance();
        BeanUtil.copyProperties(instanceVO, instance);
        instance.setStatus(FlowInstanceStatusEnum.PENDING.getValue());
        Long instanceId = super.genObjectId(ObjectTypeEnum.FLOW_INSTANCE);
        instance.setId(instanceId);
        BaseContext baseContext = new BaseContext();
        baseContext.putVal(FlowInstance.class, instance);
        SessionHolder.setContext(baseContext);

        List<FormObjValue> values = instanceVO.getValues();
        Map<String, Object> variables = new HashMap<>();
        if (CollectionUtil.isNotEmpty(values)) {
            variables = values.stream().filter(v -> StringUtil.isNotBlank(v.getValue())).collect(Collectors.toMap(FormObjValue::getCode, FormObjValue::getValue, (v1, v2) -> v1));
            FormObjValueVO formObjValueVO = new FormObjValueVO();
            formObjValueVO.setValues(values);
            formObjValueVO.setFormId(instanceVO.getFormId());
            formObjValueVO.setObjId(instanceId);
            ResultData<Void> saveFormData = formObjValueService.saveFormObjValues(formObjValueVO);
            if (saveFormData.getCode() != ResultData.OK) {
                result.setCode(saveFormData.getCode());
                result.setMessage(saveFormData.getMessage());
                return result;
            }
        }
        ResultData<String> createInstanceData = flowableService.createInstance(instanceVO.getProcessId(), variables);
        if (createInstanceData.getCode() != ResultData.OK) {
            result.setCode(createInstanceData.getCode());
            result.setMessage(createInstanceData.getMessage());
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询流程实例详情
     * @param query 查询参数
     * @return 详情
     */
    @Override
    public ResultData<FlowInstanceResult> showInstanceDetail(FlowInstanceQuery query) {
        ResultData<FlowInstanceResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FlowInstanceResult instanceResult = instanceDao.queryInstance(query);
        if (instanceResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_EXIST);
            return result;
        }
        this.supplyCreatedByName(instanceResult);
        instanceResult.setLevelName(FlowInstanceLevelEnum.indexOf(instanceResult.getLevel()).getDesc());
        if (instanceResult.getDeptId() != null) {
            instanceResult.setDeptName(this.getObjName(instanceResult.getDeptId()));
        }
        FlowTemplateVersionQuery templateVersionQuery = new FlowTemplateVersionQuery();
        templateVersionQuery.setId(instanceResult.getTemplateRevId());
        FlowTemplateVersionResult templateVersionResult = templateVersionDao.queryTemplateVersion(templateVersionQuery);
        if (templateVersionResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_TEMPLATE_VERSION_NOT_EXIST);
            return result;
        }
        String bpmPath = templateVersionResult.getBpmPath();
        if (StringUtil.isNotBlank(bpmPath)) {
            ResultData<String> fromText = TextUtil.getFromText(bpmPath, false);
            instanceResult.setBpmJson(fromText.getData());
        }
        FormVersionQuery formVersionQuery = new FormVersionQuery();
        formVersionQuery.setId(instanceResult.getFormRevId());
        FormVersionResult formVersionResult = formVersionDao.queryFormVersion(formVersionQuery);
        if (formVersionResult == null) {
            result.setErrorCode(ErrorCodeEnum.FORM_VERSION_NOT_EXIST);
            return result;
        }
        String formPath = formVersionResult.getFilePath();
        if (StringUtil.isNotBlank(formPath)) {
            ResultData<String> fromText = TextUtil.getFromText(formPath, false);
            instanceResult.setFormJson(fromText.getData());
        }
        FormObjValueQuery formObjValueQuery = new FormObjValueQuery();
        formObjValueQuery.setObjId(instanceResult.getId());
        List<FormObjValueResult> formObjValueList = formObjValueDao.queryFormObjValueList(formObjValueQuery);
        instanceResult.setValues(formObjValueList);
        FlowInstanceNodeQuery instanceNodeQuery = new FlowInstanceNodeQuery();
        instanceNodeQuery.setInstanceId(instanceResult.getId());
        List<FlowInstanceNodeResult> instanceNodeList = instanceNodeDao.queryInstanceNodeList(instanceNodeQuery);
        this.supplyInstanceNodeList(instanceNodeList);
        instanceResult.setNodes(instanceNodeList);
        if (CollectionUtil.isNotEmpty(instanceNodeList)) {
            Optional<FlowInstanceNodeResult> processingNode = instanceNodeList.stream().filter(node -> FlowInstanceStatusEnum.PROCESSING.getValue().equals(node.getStatus())).findFirst();
            processingNode.ifPresent(instanceNodeResult -> {
                instanceResult.setNodeId(instanceNodeResult.getId());
                FlowTemplateNodeQuery templateNodeQuery = new FlowTemplateNodeQuery();
                templateNodeQuery.setNodeId(instanceNodeResult.getNodeId());
                templateNodeQuery.setTemplateId(instanceResult.getTemplateId());
                templateNodeQuery.setRevId(instanceResult.getTemplateRevId());
                FlowTemplateNodeResult flowTemplateNodeResult = templateNodeDao.queryTemplateNode(templateNodeQuery);
                if (flowTemplateNodeResult != null) {
                    instanceResult.setPermission(flowTemplateNodeResult.getPermission());
                }
            });
        }
        result.setData(instanceResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询我的申请列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    public ResultData<PageResult<FlowInstanceResult>> pageMyApplicationList(FlowInstanceQuery query) {
        ResultData<PageResult<FlowInstanceResult>> result = new ResultData<>();
        if (query == null) {
            query = new FlowInstanceQuery();
        }
        query.setCreatedBy(SessionHolder.getCurrentUserId());
        PageResult<FlowInstanceResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 获取我的待办列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    public ResultData<PageResult<FlowInstanceResult>> pageMyTodoList(FlowInstanceQuery query) {
        ResultData<PageResult<FlowInstanceResult>> result = new ResultData<>();
        if (query == null) {
            query = new FlowInstanceQuery();
        }
        Long userId = SessionHolder.getCurrentUserId();
        FlowInstanceAssigneeQuery instanceAssigneeQuery = new FlowInstanceAssigneeQuery();
        instanceAssigneeQuery.setAssigneeId(userId);
        instanceAssigneeQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        List<FlowInstanceAssigneeResult> instanceAssigneeList = instanceAssigneeDao.queryInstanceAssigneeList(instanceAssigneeQuery);
        if (CollectionUtil.isEmpty(instanceAssigneeList)) {
            result.setCode(ResultData.OK);
            return result;
        }
        List<String> assigneeSetIds = instanceAssigneeList.stream().map(FlowInstanceAssigneeResult::getAssigneeSetId).filter(StringUtil::isNotBlank).toList();
        if (CollectionUtil.isEmpty(assigneeSetIds)) {
            result.setCode(ResultData.OK);
            return result;
        }
        ResultData<List<String>> pendingInstanceIdsData = flowableService.queryPendingInstanceIds(assigneeSetIds);
        if (pendingInstanceIdsData.getCode() != ResultData.OK) {
            result.setCode(pendingInstanceIdsData.getCode());
            result.setMessage(pendingInstanceIdsData.getMessage());
            return result;
        }
        List<String> flowableInstanceIds = pendingInstanceIdsData.getData();
        if (CollectionUtil.isEmpty(flowableInstanceIds)) {
            result.setCode(ResultData.OK);
            return result;
        }
        query.setFlowableInstanceIds(flowableInstanceIds);
        PageResult<FlowInstanceResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 审批流程实例
     * @param instanceVO 审批参数
     * @return 响应
     */
    @Override
    public ResultData<Void> approveInstance(FlowInstanceVO instanceVO) {
        ResultData<Void> result = new ResultData<>();
        if (instanceVO == null || instanceVO.getId() == null || instanceVO.getStatus() == null || instanceVO.getNodeId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setId(instanceVO.getId());
        instanceQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        FlowInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        FlowInstanceNodeQuery instanceNodeQuery = new FlowInstanceNodeQuery();
        instanceNodeQuery.setId(instanceVO.getNodeId());
        instanceNodeQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        FlowInstanceNodeResult instanceNodeResult = instanceNodeDao.queryInstanceNode(instanceNodeQuery);
        if (instanceNodeResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        FlowTemplateNodeQuery templateNodeQuery = new FlowTemplateNodeQuery();
        templateNodeQuery.setNodeId(instanceNodeResult.getNodeId());
        templateNodeQuery.setTemplateId(instanceResult.getTemplateId());
        templateNodeQuery.setRevId(instanceResult.getTemplateRevId());
        FlowTemplateNodeResult flowTemplateNodeResult = templateNodeDao.queryTemplateNode(templateNodeQuery);
        if (flowTemplateNodeResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_TEMPLATE_NOT_EXIST);
            return result;
        }
        Integer permission = flowTemplateNodeResult.getPermission();
        if (permission == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        if (FlowInstanceStatusEnum.COMPLETED.getValue().equals(instanceVO.getStatus()) &&
                (permission & FlowTemplateNodePermissionEnum.ALLOW_APSS.getValue()) != FlowTemplateNodePermissionEnum.ALLOW_APSS.getValue()) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        if (FlowInstanceStatusEnum.REJECTED.getValue().equals(instanceVO.getStatus()) &&
                (permission & FlowTemplateNodePermissionEnum.ALLOW_REJECT.getValue()) != FlowTemplateNodePermissionEnum.ALLOW_REJECT.getValue()) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        FlowInstanceAssigneeQuery instanceAssigneeQuery = new FlowInstanceAssigneeQuery();
        instanceAssigneeQuery.setInstanceId(instanceResult.getId());
        instanceAssigneeQuery.setInstanceNodeId(instanceNodeResult.getId());
        instanceAssigneeQuery.setAssigneeId(SessionHolder.getCurrentUserId());
        instanceAssigneeQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        FlowInstanceAssigneeResult instanceAssigneeResult = instanceAssigneeDao.queryInstanceAssignee(instanceAssigneeQuery);
        if (instanceAssigneeResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        // 更新审批人状态
        FlowInstanceAssignee instanceAssignee = new FlowInstanceAssignee();
        instanceAssignee.setId(instanceAssigneeResult.getId());
        instanceAssignee.setStatus(instanceVO.getStatus());
        int count = instanceAssigneeDao.updateDBById(instanceAssignee);
        if (count < 1) {
            logger.error("approveInstance error, update db fail");
            return result;
        }
        FlowApproveTypeEnum approveTypeEnum = FlowApproveTypeEnum.indexOf(flowTemplateNodeResult.getApproveType());
        boolean completed = true;
        if (!FlowInstanceStatusEnum.REJECTED.getValue().equals(instanceVO.getStatus()) && !FlowApproveTypeEnum.OR_SIGN.equals(approveTypeEnum)) {
            if (FlowApproveTypeEnum.AND_SIGN.equals(approveTypeEnum)) {
                FlowInstanceAssigneeQuery remainQuery = new FlowInstanceAssigneeQuery();
                remainQuery.setAssigneeSetId(instanceAssigneeResult.getAssigneeSetId());
                remainQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
                List<FlowInstanceAssigneeResult> remainList = instanceAssigneeDao.queryInstanceAssigneeList(remainQuery);
                completed = CollectionUtil.isEmpty(remainList);
            } else {
                completed = !this.activateNextAssignee(instanceResult.getFlowableInstanceId(), instanceAssigneeResult.getAssigneeSetId());
            }
        }
        if (completed) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("status", instanceVO.getStatus());
            FormObjValueQuery formObjValueQuery = new FormObjValueQuery();
            formObjValueQuery.setObjId(instanceResult.getId());
            List<FormObjValueResult> formObjValueList = formObjValueDao.queryFormObjValueList(formObjValueQuery);
            if (CollectionUtil.isNotEmpty(formObjValueList)) {
                formObjValueList.forEach(formObjValueResult -> variables.put(formObjValueResult.getCode(), formObjValueResult.getValue()));
            }
            flowableService.completeTask(instanceResult.getFlowableInstanceId(), variables);
        }
        // 记录操作
        this.saveFlowDiscuss(instanceResult.getId(), instanceNodeResult.getId(), instanceVO.getStatus(), instanceVO.getDiscuss());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 催办流程实例
     * @param instanceVO 催办参数
     * @return 催办结果
     */
    @Override
    public ResultData<Void> urgeInstance(FlowInstanceVO instanceVO) {
        ResultData<Void> result = new ResultData<>();
        if (instanceVO == null || instanceVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setId(instanceVO.getId());
        instanceQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        FlowInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        if (!SessionHolder.getCurrentUserId().equals(instanceResult.getCreatedBy())) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        FlowInstanceNodeQuery instanceNodeQuery = new FlowInstanceNodeQuery();
        instanceNodeQuery.setInstanceId(instanceResult.getId());
        instanceNodeQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        FlowInstanceNodeResult instanceNodeResult = instanceNodeDao.queryInstanceNode(instanceNodeQuery);
        if (instanceNodeResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        FlowTemplateNodeQuery templateNodeQuery = new FlowTemplateNodeQuery();
        templateNodeQuery.setNodeId(instanceNodeResult.getNodeId());
        templateNodeQuery.setTemplateId(instanceResult.getTemplateId());
        templateNodeQuery.setRevId(instanceResult.getTemplateRevId());
        FlowTemplateNodeResult templateNodeResult = templateNodeDao.queryTemplateNode(templateNodeQuery);
        Integer permission = templateNodeResult == null ? null : templateNodeResult.getPermission();
        if (permission == null || (permission & FlowTemplateNodePermissionEnum.ALLOW_URGE.getValue()) != FlowTemplateNodePermissionEnum.ALLOW_URGE.getValue()) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        // 发送催办通知
        flowMessageService.sendFlowNotice(instanceResult.getFlowableInstanceId(), MessageTypeEnum.FLOW_URGE.getType());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 转办流程实例
     * @param instanceVO 转办参数
     * @return 转办结果
     */
    @Override
    public ResultData<Void> transferInstance(FlowInstanceVO instanceVO) {
        ResultData<Void> result = new ResultData<>();
        if (instanceVO == null || CollectionUtil.isEmpty(instanceVO.getAssigneeIds())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ResultData<FlowInstanceAssigneeResult> validateResult = this.validateAssigneePermission(instanceVO, FlowTemplateNodePermissionEnum.ALLOW_TRANSFER);
        if (validateResult.getCode() != ResultData.OK) {
            result.setCode(validateResult.getCode());
            result.setMessage(validateResult.getMessage());
            return result;
        }
        FlowInstanceAssigneeResult assigneeResult = validateResult.getData();
        // 更新审批人状态
        FlowInstanceAssignee updateAssignee = new FlowInstanceAssignee();
        updateAssignee.setId(assigneeResult.getId());
        updateAssignee.setStatus(FlowInstanceStatusEnum.TRANSFERRED.getValue());
        int count = instanceAssigneeDao.updateDBById(updateAssignee);
        if (count < 1) {
            logger.error("transferInstance error, update db fail");
            return result;
        }
        // 新审批人加入集合，继承当前审批顺序
        List<Long> userIds = instanceVO.getAssigneeIds().stream().distinct().toList();
        for (int i = 0; i < userIds.size(); i++) {
            this.insertAssignee(assigneeResult, userIds.get(i), FlowInstanceStatusEnum.PROCESSING.getValue(), assigneeResult.getSort() == null ? null : assigneeResult.getSort() + i);
        }
        // 记录操作
        this.saveFlowDiscuss(assigneeResult.getInstanceId(), assigneeResult.getInstanceNodeId(), FlowInstanceStatusEnum.TRANSFERRED.getValue(), "转办给：" + StringUtil.joinList(super.getObjNames(userIds), ","));
        // 发送待办
        flowMessageService.sendFlowNotice(assigneeResult.getFlowableInstanceId(), MessageTypeEnum.FLOW_TODO.getType());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 加签流程实例
     * @param instanceVO 加签参数
     * @return 加签结果
     */
    @Override
    public ResultData<Void> addSignInstance(FlowInstanceVO instanceVO) {
        ResultData<Void> result = new ResultData<>();
        if (instanceVO == null || CollectionUtil.isEmpty(instanceVO.getAssigneeIds())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ResultData<FlowInstanceAssigneeResult> validateResult = this.validateAssigneePermission(instanceVO, FlowTemplateNodePermissionEnum.ALLOW_ADD_SIGN);
        if (validateResult.getCode() != ResultData.OK) {
            result.setCode(validateResult.getCode());
            result.setMessage(validateResult.getMessage());
            return result;
        }
        FlowInstanceAssigneeResult assigneeResult = validateResult.getData();
        List<Long> userIds = instanceVO.getAssigneeIds().stream().distinct().toList();
        // 加签人加入集合
        for (Long userId : userIds) {
            this.insertAssignee(assigneeResult, userId, FlowInstanceStatusEnum.PROCESSING.getValue(), null);
        }
        // 记录操作
        this.saveFlowDiscuss(assigneeResult.getInstanceId(), assigneeResult.getInstanceNodeId(), FlowInstanceStatusEnum.ADDED_SIGN.getValue(), "加签给了：" + StringUtil.joinList(super.getObjNames(userIds), ","));
        // 发送待办
        flowMessageService.sendFlowNotice(assigneeResult.getFlowableInstanceId(), MessageTypeEnum.FLOW_TODO.getType());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询我的已办列表
     * @param query
     * @return
     */
    @Override
    public ResultData<PageResult<FlowInstanceResult>> pageMyDoneList(FlowInstanceQuery query) {
        ResultData<PageResult<FlowInstanceResult>> result = new ResultData<>();
        if (query == null) {
            query = new FlowInstanceQuery();
        }
        Long userId = SessionHolder.getCurrentUserId();
        FlowInstanceDiscussQuery instanceAssigneeQuery = new FlowInstanceDiscussQuery();
        instanceAssigneeQuery.setAssigneeId(userId);
        List<FlowInstanceDiscussResult> instanceDiscussList = instanceDiscussDao.queryInstanceDiscussList(instanceAssigneeQuery);
        if (CollectionUtil.isEmpty(instanceDiscussList)) {
            result.setCode(ResultData.OK);
            return result;
        }
        List<Long> instanceIds = instanceDiscussList.stream().map(FlowInstanceDiscussResult::getInstanceId).distinct().toList();
        query.setIds(instanceIds);
        PageResult<FlowInstanceResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询全部流程实例
     * @param query 查询参数
     * @return 列表
     */
    @Override
    @DataScope
    public ResultData<PageResult<FlowInstanceResult>> pageInstanceList(FlowInstanceQuery query) {
        ResultData<PageResult<FlowInstanceResult>> result = new ResultData<>();
        if (query == null) {
            query = new FlowInstanceQuery();
        }
        PageResult<FlowInstanceResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 撤回流程实例
     * @param instanceVO 撤回参数
     * @return 撤回结果
     */
    @Override
    public ResultData<Void> recallInstance(FlowInstanceVO instanceVO) {
        ResultData<Void> result = new ResultData<>();
        if (instanceVO == null || instanceVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        // 校验实例存在且状态为审批中
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setId(instanceVO.getId());
        instanceQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        FlowInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        // 校验当前用户是发起人
        if (!SessionHolder.getCurrentUserId().equals(instanceResult.getCreatedBy())) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        // 查询当前审批节点
        FlowInstanceNodeQuery instanceNodeQuery = new FlowInstanceNodeQuery();
        instanceNodeQuery.setInstanceId(instanceResult.getId());
        instanceNodeQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        FlowInstanceNodeResult instanceNodeResult = instanceNodeDao.queryInstanceNode(instanceNodeQuery);
        if (instanceNodeResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        // 校验当前节点是否允许撤回
        FlowTemplateNodeQuery recallTemplateNodeQuery = new FlowTemplateNodeQuery();
        recallTemplateNodeQuery.setNodeId(instanceNodeResult.getNodeId());
        recallTemplateNodeQuery.setTemplateId(instanceResult.getTemplateId());
        recallTemplateNodeQuery.setRevId(instanceResult.getTemplateRevId());
        FlowTemplateNodeResult recallTemplateNodeResult = templateNodeDao.queryTemplateNode(recallTemplateNodeQuery);
        Integer recallPermission = recallTemplateNodeResult == null ? null : recallTemplateNodeResult.getPermission();
        if (recallPermission == null || (recallPermission & FlowTemplateNodePermissionEnum.ALLOW_RECALL.getValue()) != FlowTemplateNodePermissionEnum.ALLOW_RECALL.getValue()) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        // 校验当前节点无人审批过（所有assignee仍为PROCESSING状态）
        if (StringUtil.isNotBlank(instanceNodeResult.getAssigneeSetId())) {
            FlowInstanceAssigneeQuery allAssigneeQuery = new FlowInstanceAssigneeQuery();
            allAssigneeQuery.setAssigneeSetId(instanceNodeResult.getAssigneeSetId());
            List<FlowInstanceAssigneeResult> allAssigneeList = instanceAssigneeDao.queryInstanceAssigneeList(allAssigneeQuery);
            if (CollectionUtil.isEmpty(allAssigneeList)) {
                result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
                return result;
            }
            boolean allProcessing = allAssigneeList.stream()
                    .allMatch(a -> FlowInstanceStatusEnum.PROCESSING.getValue().equals(a.getStatus()));
            if (!allProcessing) {
                // 有人已审批，不允许撤回
                result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
                return result;
            }
        }
        // 更新实例状态为已撤回
        FlowInstance updateInstance = new FlowInstance();
        updateInstance.setId(instanceResult.getId());
        updateInstance.setStatus(FlowInstanceStatusEnum.WITHDRAWN.getValue());
        int count = instanceDao.updateDBById(updateInstance);
        if (count < 1) {
            logger.error("recallInstance error, update db fail");
            return result;
        }
        // 删除Flowable实例，触发PROCESS_CANCELLED事件（因状态已为WITHDRAWN不会被覆盖）
        flowableService.deleteInstance(instanceResult.getFlowableInstanceId());
        // 保存撤回记录
        this.saveFlowDiscuss(instanceResult.getId(), instanceNodeResult.getId(), FlowInstanceStatusEnum.WITHDRAWN.getValue(), instanceVO.getDiscuss());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 完善节点信息
     * @param instanceNodeList 节点列表
     */
    private void supplyInstanceNodeList(List<FlowInstanceNodeResult> instanceNodeList) {
        if (CollectionUtil.isEmpty(instanceNodeList)) {
            return;
        }
        Long instanceId = instanceNodeList.get(0).getInstanceId();
        Map<Long, List<FlowInstanceDiscussResult>> instanceNodeDiscussMap = new HashMap<>();
        FlowInstanceDiscussQuery discussQuery = new FlowInstanceDiscussQuery();
        discussQuery.setInstanceId(instanceId);
        List<FlowInstanceDiscussResult> discussList = instanceDiscussDao.queryInstanceDiscussList(discussQuery);
        if (CollectionUtil.isNotEmpty(discussList)) {
            discussList.forEach(instanceDiscussResult -> {
                instanceDiscussResult.setAssigneeName(super.getObjName(instanceDiscussResult.getAssigneeId()));
                instanceDiscussResult.setStatusName(FlowInstanceStatusEnum.indexOf(instanceDiscussResult.getStatus()).getDesc());
            });
            instanceNodeDiscussMap = discussList.stream().collect(Collectors.groupingBy(FlowInstanceDiscussResult::getInstanceNodeId));
        }
        Map<Long, List<FlowInstanceDiscussResult>> finalInstanceNodeDiscussMap = instanceNodeDiscussMap;
        instanceNodeList.forEach(instanceNodeResult -> {
            List<FlowInstanceDiscussResult> instanceDiscussList = finalInstanceNodeDiscussMap.get(instanceNodeResult.getId());
            instanceNodeResult.setDiscusses(instanceDiscussList);
            FlowInstanceStatusEnum flowInstanceStatusEnum = FlowInstanceStatusEnum.indexOf(instanceNodeResult.getStatus());
            instanceNodeResult.setStatusName(flowInstanceStatusEnum.getDesc());
            if (!FlowInstanceStatusEnum.PROCESSING.getValue().equals(instanceNodeResult.getStatus())) {
                return;
            }
            String assigneeSetId = instanceNodeResult.getAssigneeSetId();
            if (StringUtil.isBlank(assigneeSetId)) {
                return;
            }
            FlowInstanceAssigneeQuery instanceAssigneeQuery = new FlowInstanceAssigneeQuery();
            instanceAssigneeQuery.setAssigneeSetId(assigneeSetId);
            instanceAssigneeQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
            List<FlowInstanceAssigneeResult> instanceAssigneeResults = instanceAssigneeDao.queryInstanceAssigneeList(instanceAssigneeQuery);
            if (CollectionUtil.isEmpty(instanceAssigneeResults)) {
               return;
            }
            List<Long> userIds = instanceAssigneeResults.stream().map(FlowInstanceAssigneeResult::getAssigneeId).toList();
            List<String> objNames = super.getObjNames(userIds);
            instanceNodeResult.setUnAssigneeName(StringUtil.joinList(objNames, ","));
        });
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<FlowInstanceResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        list.forEach(item -> {
            item.setStatusName(FlowInstanceStatusEnum.indexOf(item.getStatus()).getDesc());
            item.setLevelName(FlowInstanceLevelEnum.indexOf(item.getLevel()).getDesc());
            if (item.getDeptId() != null) {
                item.setDeptName(this.getObjName(item.getDeptId()));
            }
        });

    }

    /**
     * 校验审批人操作权限
     * @param instanceVO 操作参数
     * @param permissionEnum 所需节点权限
     * @return 校验结果，data为当前审批人
     */
    private ResultData<FlowInstanceAssigneeResult> validateAssigneePermission(FlowInstanceVO instanceVO, FlowTemplateNodePermissionEnum permissionEnum) {
        ResultData<FlowInstanceAssigneeResult> result = new ResultData<>();
        if (instanceVO == null || instanceVO.getId() == null || instanceVO.getNodeId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setId(instanceVO.getId());
        instanceQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        FlowInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        FlowInstanceNodeQuery instanceNodeQuery = new FlowInstanceNodeQuery();
        instanceNodeQuery.setId(instanceVO.getNodeId());
        instanceNodeQuery.setInstanceId(instanceResult.getId());
        instanceNodeQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        FlowInstanceNodeResult instanceNodeResult = instanceNodeDao.queryInstanceNode(instanceNodeQuery);
        if (instanceNodeResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        FlowTemplateNodeQuery templateNodeQuery = new FlowTemplateNodeQuery();
        templateNodeQuery.setNodeId(instanceNodeResult.getNodeId());
        templateNodeQuery.setTemplateId(instanceResult.getTemplateId());
        templateNodeQuery.setRevId(instanceResult.getTemplateRevId());
        FlowTemplateNodeResult templateNodeResult = templateNodeDao.queryTemplateNode(templateNodeQuery);
        Integer permission = templateNodeResult == null ? null : templateNodeResult.getPermission();
        if (permission == null || (permission & permissionEnum.getValue()) != permissionEnum.getValue()) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        FlowInstanceAssigneeQuery assigneeQuery = new FlowInstanceAssigneeQuery();
        assigneeQuery.setInstanceId(instanceResult.getId());
        assigneeQuery.setInstanceNodeId(instanceNodeResult.getId());
        assigneeQuery.setAssigneeId(SessionHolder.getCurrentUserId());
        assigneeQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        FlowInstanceAssigneeResult assigneeResult = instanceAssigneeDao.queryInstanceAssignee(assigneeQuery);
        if (assigneeResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
            return result;
        }
        assigneeResult.setFlowableInstanceId(instanceResult.getFlowableInstanceId());
        result.setData(assigneeResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 向当前审批人集合新增审批人
     * @param setAssignee 集合内既有审批人
     * @param assigneeId 审批人id
     * @param status 审批状态
     * @param sort 审批顺序
     */
    private void insertAssignee(FlowInstanceAssigneeResult setAssignee, Long assigneeId, Integer status, Integer sort) {
        FlowInstanceAssignee instanceAssignee = new FlowInstanceAssignee();
        instanceAssignee.setInstanceId(setAssignee.getInstanceId());
        instanceAssignee.setInstanceNodeId(setAssignee.getInstanceNodeId());
        instanceAssignee.setAssigneeSetId(setAssignee.getAssigneeSetId());
        instanceAssignee.setAssigneeId(assigneeId);
        instanceAssignee.setStatus(status);
        instanceAssignee.setSort(sort);
        int count = instanceAssigneeDao.insertDB(instanceAssignee);
        if (count < 1) {
            logger.error("insertAssignee error, insert db fail, assigneeId={}", assigneeId);
        }
    }

    /**
     * 保存审批记录
     * @param instanceId 流程实例id
     * @param instanceNodeId 流程实例节点id
     * @param status 操作状态
     * @param discuss 内容
     */
    private void saveFlowDiscuss(Long instanceId, Long instanceNodeId, Integer status, String discuss) {
        FlowInstanceDiscuss instanceDiscuss = new FlowInstanceDiscuss();
        instanceDiscuss.setInstanceId(instanceId);
        instanceDiscuss.setInstanceNodeId(instanceNodeId);
        instanceDiscuss.setDiscuss(discuss);
        instanceDiscuss.setAssigneeId(SessionHolder.getCurrentUserId());
        instanceDiscuss.setStatus(status);
        int count = instanceDiscussDao.insertDB(instanceDiscuss);
        if (count < 1) {
            logger.error("saveFlowDiscuss error, insert db fail");
        }
    }

    /**
     * 依次审批：激活下一位等待审批人并发送待办通知
     * @param flowableInstanceId flowable流程实例id
     * @param assigneeSetId 审批人集合id
     * @return true已激活下一位 false已无等待审批人
     */
    private boolean activateNextAssignee(String flowableInstanceId, String assigneeSetId) {
        FlowInstanceAssigneeQuery assigneeQuery = new FlowInstanceAssigneeQuery();
        assigneeQuery.setAssigneeSetId(assigneeSetId);
        assigneeQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        List<FlowInstanceAssigneeResult> processingList = instanceAssigneeDao.queryInstanceAssigneeList(assigneeQuery);
        if (CollectionUtil.isNotEmpty(processingList)) {
            // 还有没审批完的 直接返回true 继续审批
            return true;
        }
        assigneeQuery.setStatus(FlowInstanceStatusEnum.WAITING.getValue());
        List<FlowInstanceAssigneeResult> waitingList = instanceAssigneeDao.queryInstanceAssigneeList(assigneeQuery);
        if (CollectionUtil.isEmpty(waitingList)) {
            return false;
        }
        // 将加一个审批人置为审批中状态
        FlowInstanceAssigneeResult nextAssignee = waitingList.get(0);
        FlowInstanceAssignee updateAssignee = new FlowInstanceAssignee();
        updateAssignee.setId(nextAssignee.getId());
        updateAssignee.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        int count = instanceAssigneeDao.updateDBById(updateAssignee);
        if (count < 1) {
            logger.error("activateNextAssignee error, update db fail");
            return true;
        }
        flowMessageService.sendFlowNotice(flowableInstanceId, MessageTypeEnum.FLOW_TODO.getType());
        return true;
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return  数量
     */
    @Override
    protected int queryCount(FlowInstanceQuery query) {
        return instanceDao.queryInstanceCount(query);
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<FlowInstanceResult> queryList(FlowInstanceQuery query) {
        return instanceDao.queryInstanceList(query);
    }

    /**
     * 查询最大ID
     * @return 最大ID
     */
    @Override
    protected Long queryMaxId() {
        return instanceDao.queryInstanceMaxId();
    }
}

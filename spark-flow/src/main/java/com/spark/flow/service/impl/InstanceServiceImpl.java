package com.spark.flow.service.impl;

import com.spark.bean.base.BaseContext;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.base.SessionHolder;
import com.spark.bean.flow.entity.FlowInstance;
import com.spark.bean.flow.entity.FlowInstanceAssignee;
import com.spark.bean.flow.entity.FlowInstanceDiscuss;
import com.spark.bean.flow.query.*;
import com.spark.bean.flow.result.*;
import com.spark.bean.flow.vo.FlowInstanceVO;
import com.spark.bean.form.entity.FormObjValue;
import com.spark.bean.form.query.FormObjValueQuery;
import com.spark.bean.form.query.FormVersionQuery;
import com.spark.bean.form.result.FormObjValueResult;
import com.spark.bean.form.result.FormVersionResult;
import com.spark.bean.form.vo.FormObjValueVO;
import com.spark.dao.flow.*;
import com.spark.dao.form.FormObjValueDao;
import com.spark.dao.form.FormVersionDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.FlowInstanceStatusEnum;
import com.spark.enums.ObjectTypeEnum;
import com.spark.flow.service.FlowableService;
import com.spark.flow.service.IFlowInstanceService;
import com.spark.form.service.IFormObjValueService;
import com.spark.manage.BaseService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import com.spark.utils.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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

    /**
     * 创建流程实例
     * @param instanceVO 流程实例数据
     * @return 创建结果
     */
    @Override
    public ResultData<Void> createInstance(FlowInstanceVO instanceVO) {
        ResultData<Void> result = new ResultData<>();
        if (instanceVO == null || instanceVO.getTemplateId() == null || instanceVO.getTemplateRevId() == null
                || instanceVO.getFormId() == null || instanceVO.getFormRevId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FlowInstance instance = new FlowInstance();
        BeanUtils.copyProperties(instanceVO, instance);
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
            processingNode.ifPresent(instanceNodeResult -> instanceResult.setNodeId(instanceNodeResult.getId()));
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
    public ResultData<PageResult<FlowInstanceResult>> pageMyAppliedList(FlowInstanceQuery query) {
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
    public ResultData<PageResult<FlowInstanceResult>> pageMyPendingList(FlowInstanceQuery query) {
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
        if (instanceVO == null || instanceVO.getId() == null || instanceVO.getStatus() == null
                || instanceVO.getNodeId() == null) {
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
        Map<String, Object> variables = new HashMap<>();
        variables.put("status", instanceVO.getStatus());
        FormObjValueQuery formObjValueQuery = new FormObjValueQuery();
        formObjValueQuery.setObjId(instanceResult.getId());
        List<FormObjValueResult> formObjValueList = formObjValueDao.queryFormObjValueList(formObjValueQuery);
        if (CollectionUtil.isNotEmpty(formObjValueList)) {
            formObjValueList.forEach(formObjValueResult -> variables.put(formObjValueResult.getCode(), formObjValueResult.getValue()));
        }
        flowableService.completeTask(instanceResult.getFlowableInstanceId(), variables);
        FlowInstanceDiscuss instanceDiscuss = new FlowInstanceDiscuss();
        instanceDiscuss.setInstanceId(instanceResult.getId());
        instanceDiscuss.setInstanceNodeId(instanceNodeResult.getId());
        instanceDiscuss.setDiscuss(instanceVO.getDiscuss());
        instanceDiscuss.setAssigneeId(SessionHolder.getCurrentUserId());
        instanceDiscuss.setStatus(instanceVO.getStatus());
        int count = instanceDiscussDao.insertDB(instanceDiscuss);
        if (count < 1) {
            logger.error("approveInstance error, insert db fail");
            return result;
        }
        FlowInstanceAssignee instanceAssignee = new FlowInstanceAssignee();
        instanceAssignee.setId(instanceAssigneeResult.getId());
        instanceAssignee.setStatus(FlowInstanceStatusEnum.COMPLETED.getValue());
        instanceAssigneeDao.updateDBById(instanceAssignee);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询我的已办列表
     * @param query
     * @return
     */
    @Override
    public ResultData<PageResult<FlowInstanceResult>> pageMyPendedList(FlowInstanceQuery query) {
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
        list.forEach(item -> item.setStatusName(FlowInstanceStatusEnum.indexOf(item.getStatus()).getDesc()));

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

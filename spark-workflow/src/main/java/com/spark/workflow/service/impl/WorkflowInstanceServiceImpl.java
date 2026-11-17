package com.spark.workflow.service.impl;

import com.spark.common.bean.base.BaseContext;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.home.result.DashboardHotResult;
import com.spark.common.bean.workflow.entity.WfInstance;
import com.spark.common.bean.workflow.entity.WfInstanceNode;
import com.spark.common.bean.workflow.query.WfInstanceNodeQuery;
import com.spark.common.bean.workflow.query.WfInstanceQuery;
import com.spark.common.bean.workflow.query.WfTemplateQuery;
import com.spark.common.bean.workflow.query.WfTemplateVersionQuery;
import com.spark.common.bean.workflow.result.WfInstanceNodeResult;
import com.spark.common.bean.workflow.result.WfInstanceResult;
import com.spark.common.bean.workflow.result.WfInstanceReviewResult;
import com.spark.common.bean.workflow.result.WfTemplateResult;
import com.spark.common.bean.workflow.result.WfTemplateVersionResult;
import com.spark.common.bean.workflow.vo.WfReviewVO;
import com.spark.common.bean.workflow.vo.WfRunVO;
import com.spark.common.bean.form.entity.FormObjValue;
import com.spark.common.bean.form.query.FormObjValueQuery;
import com.spark.common.bean.form.query.FormQuery;
import com.spark.common.bean.form.result.FormObjValueResult;
import com.spark.common.bean.form.result.FormResult;
import com.spark.common.bean.form.vo.FormObjValueVO;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.dao.form.FormObjDao;
import com.spark.dao.form.FormObjValueDao;
import com.spark.dao.workflow.WfInstanceDao;
import com.spark.dao.workflow.WfInstanceNodeDao;
import com.spark.dao.workflow.WfTemplateDao;
import com.spark.dao.workflow.WfTemplateVersionDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.WorkflowInstanceStatusEnum;
import com.spark.common.enums.WorkflowNodeStatusEnum;
import com.spark.common.enums.WorkflowTemplateTypeEnum;
import com.spark.form.service.IFormObjValueService;
import com.spark.form.service.IFormService;
import com.spark.manage.BaseService;
import com.spark.common.utils.BeanUtil;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.StringUtil;
import com.spark.workflow.engine.DagExecutor;
import com.spark.workflow.service.IWorkflowInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 15:00:00
 * workFlow实例Service实现
 */
@Service
@LogPrint
public class WorkflowInstanceServiceImpl extends BaseService<WfInstanceQuery, WfInstanceResult> implements IWorkflowInstanceService {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowInstanceServiceImpl.class);
    @Autowired
    private WfInstanceDao instanceDao;
    @Autowired
    private WfInstanceNodeDao instanceNodeDao;
    @Autowired
    private WfTemplateDao templateDao;
    @Autowired
    private WfTemplateVersionDao templateVersionDao;
    @Autowired
    private DagExecutor dagExecutor;
    @Autowired
    private IFormObjValueService formObjValueService;
    @Autowired
    private IFormService formService;
    @Autowired
    private FormObjDao formObjDao;
    @Autowired
    private FormObjValueDao formObjValueDao;

    /**
     * 分页查询运行实例
     *
     * @param query 查询参数
     * @return 分页结果
     */
    @Override
    // join了wf_template，别名固定ri防止created_by/dept_id列名歧义
    @DataScope(tableAlias = "ri")
    public ResultData<PageResult<WfInstanceResult>> pageInstanceHistory(WfInstanceQuery query) {
        ResultData<PageResult<WfInstanceResult>> result = new ResultData<>();
        if (query == null) {
            query = new WfInstanceQuery();
        }
        PageResult<WfInstanceResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询运行实例详情
     *
     * @param query 查询参数
     * @return 实例详情
     */
    @Override
    public ResultData<WfInstanceResult> queryInstanceDetail(WfInstanceQuery query) {
        ResultData<WfInstanceResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        super.scopeTenant(query);
        WfInstanceResult instanceResult = instanceDao.queryInstance(query);
        if (instanceResult == null) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_RUN_NOT_FOUND);
            return result;
        }
        instanceResult.setStatusName(WorkflowInstanceStatusEnum.indexOf(instanceResult.getStatus()).getDesc());
        super.supplyCreatedByName(instanceResult);
        result.setData(instanceResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询实例节点执行记录
     *
     * @param query 查询参数
     * @return 节点执行记录列表
     */
    @Override
    public ResultData<List<WfInstanceNodeResult>> queryInstanceNodes(WfInstanceNodeQuery query) {
        ResultData<List<WfInstanceNodeResult>> result = new ResultData<>();
        if (query == null || query.getInstanceId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        WfInstanceQuery instanceQuery = new WfInstanceQuery();
        instanceQuery.setId(query.getInstanceId());
        super.scopeTenant(instanceQuery);
        WfInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_RUN_NOT_FOUND);
            return result;
        }
        query.setPage(false);
        List<WfInstanceNodeResult> nodes = instanceNodeDao.queryInstanceNodeList(query);
        if (CollectionUtil.isNotEmpty(nodes)) {
            nodes.forEach(node -> {
                String nodeStatusName = WorkflowNodeStatusEnum.indexOf(node.getStatus()).getDesc();
                boolean reviewWaiting = WorkflowTemplateTypeEnum.HUMAN_REVIEW.getValue().equals(node.getNodeType())
                        && WorkflowNodeStatusEnum.WAITING.getValue().equals(node.getStatus());
                if (reviewWaiting) {
                    nodeStatusName = "待审核";
                }
                node.setStatusName(nodeStatusName);
            });
        }
        result.setData(nodes);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 运行工作流
     *
     * @param runVO 运行入参
     * @return 运行实例
     */
    @Override
    public ResultData<WfInstance> runWorkflow(WfRunVO runVO) {
        ResultData<WfInstance> result = new ResultData<>();
        if (runVO == null || runVO.getTemplateId() == null || runVO.getFormId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long templateId = runVO.getTemplateId();
        WfTemplateQuery templateQuery = new WfTemplateQuery();
        templateQuery.setId(templateId);
        super.scopeTenant(templateQuery);
        WfTemplateResult template = templateDao.queryTemplate(templateQuery);
        if (template == null) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_NOT_FOUND);
            return result;
        }
        if (template.getRevId() == null) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_NO_PUBLISHED_VERSION);
            return result;
        }
        WfTemplateVersionQuery versionQuery = new WfTemplateVersionQuery();
        versionQuery.setId(template.getRevId());
        versionQuery.setTemplateId(templateId);
        WfTemplateVersionResult versionResult = templateVersionDao.queryVersion(versionQuery);
        if (versionResult == null) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_VERSION_NOT_FOUND);
            return result;
        }
        if (StringUtil.isBlank(versionResult.getDagJson())) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_DAG_INVALID);
            return result;
        }
        Long formId = template.getFormId();
        Long requestFormId = runVO.getFormId();
        if (!Objects.equals(formId, requestFormId)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            result.setMessage("工作流与输入表单绑定关系不一致");
            return result;
        }
        FormQuery formQuery = new FormQuery();
        formQuery.setId(formId);
        ResultData<FormResult> formDetailResult = formService.queryFormDetail(formQuery);
        if (formDetailResult.getCode() != ResultData.OK) {
            int errorCode = formDetailResult.getCode();
            String errorMessage = formDetailResult.getMessage();
            result.setCode(errorCode);
            result.setMessage(errorMessage);
            return result;
        }
        FormResult formResult = formDetailResult.getData();
        Long formRevId = formResult.getRevId();
        if (formRevId == null) {
            result.setErrorCode(ErrorCodeEnum.FORM_VERSION_NOT_EXIST);
            return result;
        }
        Long requestFormRevId = runVO.getFormRevId();
        if (requestFormRevId != null && !Objects.equals(requestFormRevId, formRevId)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            result.setMessage("输入表单版本已更新，请刷新后重试");
            return result;
        }
        Long instanceId = super.genObjectId(ObjectTypeEnum.WORKFLOW_INSTANCE);
        FormObjValueVO formObjValueVO = new FormObjValueVO();
        formObjValueVO.setObjId(instanceId);
        formObjValueVO.setFormId(formId);
        formObjValueVO.setFormRevId(formRevId);
        List<FormObjValue> originalValues = runVO.getValues();
        formObjValueVO.setValues(originalValues);
        ResultData<Void> saveFormData = formObjValueService.saveFormObjValues(formObjValueVO);
        if (saveFormData.getCode() != ResultData.OK) {
            result.setCode(saveFormData.getCode());
            result.setMessage(saveFormData.getMessage());
            return result;
        }
        List<FormObjValue> values = formObjValueVO.getValues();
        Map<String, String> valueMap = new HashMap<>();
        Map<String, String> showValueMap = new HashMap<>();
        Map<String, String> valueTypeMap = new HashMap<>();
        for (FormObjValue value : values) {
            String code = value.getCode();
            String fieldValue = value.getValue();
            String showValue = value.getShowValue();
            String fieldType = value.getType();
            valueMap.put(code, fieldValue);
            showValueMap.put(code, showValue);
            valueTypeMap.put(code, fieldType);
        }
        WfInstance instance = new WfInstance();
        instance.setId(instanceId);
        instance.setTemplateId(templateId);
        instance.setRevId(template.getRevId());
        instance.setRevNum(template.getRevNum());
        instance.setStatus(WorkflowInstanceStatusEnum.RUNNING.getValue());
        int count;
        try {
            count = instanceDao.insertDB(instance);
        } catch (RuntimeException e) {
            this.compensateRunWorkflowForm(instanceId);
            throw e;
        }
        if (count < 1) {
            this.compensateRunWorkflowForm(instanceId);
            logger.error("runWorkflow error, insert instance fail, instanceId={}", instanceId);
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        BaseContext context = new BaseContext();
        context.putVal(WfInstance.class, instance);
        context.put("valueMap", valueMap);
        context.put("showValueMap", showValueMap);
        context.put("valueTypeMap", valueTypeMap);
        String templateName = template.getName();
        context.put("templateName", templateName);
        String dagJson = versionResult.getDagJson();
        try {
            dagExecutor.executeAsync(context, dagJson);
        } catch (RuntimeException e) {
            this.markWorkflowFailed(instanceId, "工作流提交执行失败");
            logger.error("runWorkflow submit error, instanceId={}", instanceId, e);
            result.setErrorCode(ErrorCodeEnum.SYSTEM_ERROR);
            return result;
        }
        result.setData(instance);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询我常用的工作流
     *
     * @param limit 条数
     * @return 热门工作流列表
     */
    @Override
    public List<DashboardHotResult> queryMyHotWorkflows(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        Long userId = SessionHolder.getCurrentUserId();
        Long tenantId = SessionHolder.getCurrentTenantId();
        return instanceDao.queryMyHotWorkflows(userId, tenantId, limit);
    }

    /**
     * 提交人工审核，通过恢复执行，驳回终止实例
     *
     * @param reviewVO 审核入参
     * @return 审核结果
     */
    @Override
    public ResultData<Void> reviewInstance(WfReviewVO reviewVO) {
        ResultData<Void> result = new ResultData<>();
        if (reviewVO == null || reviewVO.getInstanceId() == null || reviewVO.getNodeRunId() == null || reviewVO.getApproved() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String comment = reviewVO.getComment();
        if (comment != null && comment.length() > 1000) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long instanceId = reviewVO.getInstanceId();
        WfInstanceQuery instanceQuery = new WfInstanceQuery();
        instanceQuery.setId(instanceId);
        WfInstanceResult instance = instanceDao.queryInstance(instanceQuery);
        if (instance == null) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_RUN_NOT_FOUND);
            return result;
        }
        Long tenantId = SessionHolder.getCurrentTenantId();
        boolean sameTenant = tenantId != null && tenantId.equals(instance.getTenantId());
        if (!SessionHolder.isSysAdmin() && !sameTenant) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        if (!WorkflowInstanceStatusEnum.PAUSED.getValue().equals(instance.getStatus())) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_REVIEW_INVALID);
            return result;
        }
        WfInstanceNodeQuery nodeQuery = new WfInstanceNodeQuery();
        nodeQuery.setInstanceId(instanceId);
        nodeQuery.setPage(false);
        List<WfInstanceNodeResult> nodeRows = instanceNodeDao.queryInstanceNodeList(nodeQuery);
        WfInstanceNodeResult reviewRow = this.locateWaitingReviewNode(nodeRows);
        if (reviewRow == null) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_REVIEW_INVALID);
            return result;
        }
        Long nodeRunId = reviewVO.getNodeRunId();
        Long currentNodeRunId = reviewRow.getId();
        if (!Objects.equals(nodeRunId, currentNodeRunId)) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_REVIEW_INVALID);
            return result;
        }
        Map<String, String> reviewInfo = JsonUtil.parseMap(reviewRow.getOutputJson());
        if (reviewInfo == null) {
            reviewInfo = new HashMap<>();
        }
        String reviewerIds = reviewInfo.get("reviewerIds");
        Long currentUserId = SessionHolder.getCurrentUserId();
        boolean isReviewer = this.checkReviewer(reviewerIds, currentUserId);
        if (!isReviewer) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        BaseContext resumeContext = null;
        if (reviewVO.getApproved()) {
            ResultData<BaseContext> prepareResult = this.prepareResumeDag(instance);
            if (prepareResult.getCode() != ResultData.OK) {
                int errorCode = prepareResult.getCode();
                String errorMessage = prepareResult.getMessage();
                result.setCode(errorCode);
                result.setMessage(errorMessage);
                return result;
            }
            resumeContext = prepareResult.getData();
        }
        Integer toStatus = WorkflowInstanceStatusEnum.RUNNING.getValue();
        if (!reviewVO.getApproved()) {
            toStatus = WorkflowInstanceStatusEnum.REJECTED.getValue();
        }
        // 实例状态与指定待审行必须在同一原子条件中有效，防止并发及跨节点重复审核
        Integer pausedStatus = WorkflowInstanceStatusEnum.PAUSED.getValue();
        Integer waitingStatus = WorkflowNodeStatusEnum.WAITING.getValue();
        int locked = instanceDao.updateInstanceStatusCas(instanceId, nodeRunId, pausedStatus, toStatus, waitingStatus, currentUserId);
        if (locked < 1) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_REVIEW_INVALID);
            return result;
        }
        // 审核行改写为成功记录，结论按节点前缀键写入输出供后继节点引用
        String approvedText = String.valueOf(reviewVO.getApproved());
        String commentText = comment == null ? "" : comment;
        Map<String, String> finalOutput = new HashMap<>(reviewInfo);
        String nodeKey = reviewRow.getNodeId();
        finalOutput.put(nodeKey + "approved", approvedText);
        finalOutput.put(nodeKey + "comment", commentText);
        finalOutput.put("approved", approvedText);
        finalOutput.put("comment", commentText);
        String finalJson = JsonUtil.toString(finalOutput);
        WfInstanceNode nodeUpdate = new WfInstanceNode();
        nodeUpdate.setId(currentNodeRunId);
        nodeUpdate.setStatus(WorkflowNodeStatusEnum.SUCCESS.getValue());
        nodeUpdate.setOutputJson(finalJson);
        int nodeCount;
        try {
            nodeCount = instanceNodeDao.updateDBById(nodeUpdate);
        } catch (RuntimeException e) {
            this.markWorkflowFailed(instanceId, "人工审核记录更新异常");
            throw e;
        }
        if (nodeCount < 1) {
            this.markWorkflowFailed(instanceId, "人工审核记录更新失败");
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        if (!reviewVO.getApproved()) {
            WfInstance meta = new WfInstance();
            meta.setId(instanceId);
            meta.setErrorMsg("人工审核驳回");
            long currentTime = System.currentTimeMillis();
            Timestamp finishedDt = new Timestamp(currentTime);
            meta.setFinishedDt(finishedDt);
            int metaCount;
            try {
                metaCount = instanceDao.updateDBById(meta);
            } catch (RuntimeException e) {
                this.markWorkflowFailed(instanceId, "人工审核驳回状态更新异常");
                throw e;
            }
            if (metaCount < 1) {
                this.markWorkflowFailed(instanceId, "人工审核驳回状态更新失败");
                result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
                return result;
            }
            result.setCode(ResultData.OK);
            return result;
        }
        if (resumeContext == null) {
            this.markWorkflowFailed(instanceId, "工作流恢复上下文缺失");
            result.setErrorCode(ErrorCodeEnum.SYSTEM_ERROR);
            return result;
        }
        Object dagObject = resumeContext.get("dagJson");
        String dagJson = String.valueOf(dagObject);
        try {
            dagExecutor.resumeAsync(resumeContext, dagJson);
        } catch (RuntimeException e) {
            this.markWorkflowFailed(instanceId, "工作流恢复提交失败");
            logger.error("reviewInstance resume submit error, instanceId={}", instanceId, e);
            result.setErrorCode(ErrorCodeEnum.SYSTEM_ERROR);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询待我审核的运行实例
     *
     * @param query 查询参数
     * @return 分页结果
     */
    @Override
    public ResultData<PageResult<WfInstanceReviewResult>> pageMyReviewList(WfInstanceQuery query) {
        ResultData<PageResult<WfInstanceReviewResult>> result = new ResultData<>();
        if (query == null) {
            query = new WfInstanceQuery();
        }
        Long tenantId = SessionHolder.getCurrentTenantId();
        Long currentUserId = SessionHolder.getCurrentUserId();
        WfInstanceNodeQuery nodeQuery = new WfInstanceNodeQuery();
        nodeQuery.setNodeType(WorkflowTemplateTypeEnum.HUMAN_REVIEW.getValue());
        nodeQuery.setStatus(WorkflowNodeStatusEnum.WAITING.getValue());
        nodeQuery.setInstanceStatus(WorkflowInstanceStatusEnum.PAUSED.getValue());
        nodeQuery.setTenantId(tenantId);
        List<WfInstanceNodeResult> candidates = instanceNodeDao.queryPendingReviewNodeList(nodeQuery);
        Map<Long, WfInstanceNodeResult> mineMap = new LinkedHashMap<>();
        Map<Long, Map<String, String>> reviewInfoMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(candidates)) {
            for (WfInstanceNodeResult row : candidates) {
                Map<String, String> reviewInfo = JsonUtil.parseMap(row.getOutputJson());
                if (reviewInfo == null) {
                    reviewInfo = new HashMap<>();
                }
                String reviewerIds = reviewInfo.get("reviewerIds");
                boolean matched = this.checkReviewer(reviewerIds, currentUserId);
                // 候选按id倒序返回，同一实例首条即最新待审行
                if (matched && row.getInstanceId() != null && !mineMap.containsKey(row.getInstanceId())) {
                    mineMap.put(row.getInstanceId(), row);
                    reviewInfoMap.put(row.getInstanceId(), reviewInfo);
                }
            }
        }
        PageResult<WfInstanceReviewResult> pageResult = new PageResult<>();
        List<WfInstanceReviewResult> rows = new ArrayList<>();
        pageResult.setRows(rows);
        if (mineMap.isEmpty()) {
            result.setData(pageResult);
            result.setCode(ResultData.OK);
            return result;
        }
        query.setIds(new ArrayList<>(mineMap.keySet()));
        query.setStatus(WorkflowInstanceStatusEnum.PAUSED.getValue());
        PageResult<WfInstanceResult> page = super.pageList(query);
        if (CollectionUtil.isNotEmpty(page.getRows())) {
            for (WfInstanceResult item : page.getRows()) {
                WfInstanceReviewResult reviewResult = new WfInstanceReviewResult();
                BeanUtil.copyProperties(item, reviewResult);
                reviewResult.setInstanceId(item.getId());
                WfInstanceNodeResult reviewRow = mineMap.get(item.getId());
                if (reviewRow != null) {
                    reviewResult.setNodeId(reviewRow.getNodeId());
                    reviewResult.setNodeRunId(reviewRow.getId());
                    reviewResult.setNodeName(reviewRow.getNodeName());
                    Map<String, String> reviewInfo = reviewInfoMap.get(item.getId());
                    String prompt = reviewInfo.get("prompt");
                    String reviewerIds = reviewInfo.get("reviewerIds");
                    reviewResult.setPrompt(prompt);
                    reviewResult.setReviewerIds(reviewerIds);
                }
                rows.add(reviewResult);
            }
        }
        pageResult.setTotal(page.getTotal());
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     *
     * @param list 列表
     */
    @Override
    protected void supplyList(List<WfInstanceResult> list) {
        if (CollectionUtil.isEmpty(list)) { return; }
        super.supplyCreatedByName(list);
        list.forEach(item -> item.setStatusName(WorkflowInstanceStatusEnum.indexOf(item.getStatus()).getDesc()));
    }

    /**
     * 查询数量
     *
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(WfInstanceQuery query) { return instanceDao.queryInstanceCount(query); }

    /**
     * 查询列表
     *
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<WfInstanceResult> queryList(WfInstanceQuery query) { return instanceDao.queryInstanceList(query); }

    /**
     * 查询最大ID
     *
     * @return 最大ID
     */
    @Override
    protected Long queryMaxId() { return instanceDao.queryInstanceMaxId(); }

    /**
     * 审核前准备恢复执行上下文
     * @param instance 实例查询结果
     * @return 恢复执行上下文
     */
    private ResultData<BaseContext> prepareResumeDag(WfInstanceResult instance) {
        ResultData<BaseContext> result = new ResultData<>();
        if (instance == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long instanceId = instance.getId();
        Long templateId = instance.getTemplateId();
        Long revId = instance.getRevId();
        if (instanceId == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (templateId == null || revId == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        WfTemplateVersionQuery versionQuery = new WfTemplateVersionQuery();
        versionQuery.setId(revId);
        versionQuery.setTemplateId(templateId);
        WfTemplateVersionResult versionResult = templateVersionDao.queryVersion(versionQuery);
        if (versionResult == null || StringUtil.isBlank(versionResult.getDagJson())) {
            logger.error("prepareResumeDag dag missing, instanceId={}", instanceId);
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_DAG_INVALID);
            return result;
        }
        FormObjValueQuery valueQuery = new FormObjValueQuery();
        valueQuery.setObjId(instanceId);
        valueQuery.setPage(false);
        ResultData<List<FormObjValueResult>> valueResult = formObjValueService.queryFormObjValueList(valueQuery);
        if (valueResult.getCode() != ResultData.OK) {
            int errorCode = valueResult.getCode();
            String errorMessage = valueResult.getMessage();
            result.setCode(errorCode);
            result.setMessage(errorMessage);
            return result;
        }
        Map<String, String> valueMap = new HashMap<>();
        Map<String, String> showValueMap = new HashMap<>();
        Map<String, String> valueTypeMap = new HashMap<>();
        List<FormObjValueResult> formValues = valueResult.getData();
        if (formValues != null) {
            for (FormObjValueResult formValue : formValues) {
                String code = formValue.getCode();
                if (StringUtil.isBlank(code)) {
                    continue;
                }
                String fieldValue = formValue.getValue();
                String showValue = formValue.getShowValue();
                String fieldType = formValue.getType();
                if (StringUtil.isNotBlank(fieldValue)) {
                    valueMap.put(code, fieldValue);
                }
                if (StringUtil.isNotBlank(showValue)) {
                    showValueMap.put(code, showValue);
                }
                if (StringUtil.isNotBlank(fieldType)) {
                    valueTypeMap.put(code, fieldType);
                }
            }
        }
        WfInstance instanceEntity = new WfInstance();
        instanceEntity.setId(instanceId);
        instanceEntity.setTemplateId(templateId);
        instanceEntity.setRevId(revId);
        BaseContext context = new BaseContext();
        context.putVal(WfInstance.class, instanceEntity);
        context.put("valueMap", valueMap);
        context.put("showValueMap", showValueMap);
        context.put("valueTypeMap", valueTypeMap);
        String templateName = instance.getTemplateName();
        context.put("templateName", templateName);
        String dagJson = versionResult.getDagJson();
        context.put("dagJson", dagJson);
        result.setData(context);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 定位节点记录中最新的等待审核人工审核行
     * @param nodeRows 节点记录列表，按id升序
     * @return 待审核行，无则返回null
     */
    private WfInstanceNodeResult locateWaitingReviewNode(List<WfInstanceNodeResult> nodeRows) {
        if (CollectionUtil.isEmpty(nodeRows)) {
            return null;
        }
        WfInstanceNodeResult latest = null;
        for (WfInstanceNodeResult row : nodeRows) {
            boolean reviewWaiting = WorkflowTemplateTypeEnum.HUMAN_REVIEW.getValue().equals(row.getNodeType())
                    && WorkflowNodeStatusEnum.WAITING.getValue().equals(row.getStatus());
            if (reviewWaiting) {
                latest = row;
            }
        }
        return latest;
    }

    /**
     * 判断当前用户是否在审核人列表中，审核人未配置视为不限制
     * @param reviewerIds 逗号分隔审核人ID串
     * @param currentUserId 当前用户ID
     * @return true允许审核
     */
    private boolean checkReviewer(String reviewerIds, Long currentUserId) {
        if (StringUtil.isBlank(reviewerIds)) {
            return true;
        }
        if (currentUserId == null) {
            return false;
        }
        List<String> ids = Arrays.asList(reviewerIds.split(","));
        String userIdText = String.valueOf(currentUserId);
        return ids.contains(userIdText);
    }

    /**
     * 清理实例创建失败前保存的表单对象
     * @param instanceId 实例id
     */
    private void compensateRunWorkflowForm(Long instanceId) {
        if (instanceId == null) {
            return;
        }
        try {
            formObjValueDao.deleteDBByObjId(instanceId);
        } catch (RuntimeException e) {
            logger.error("runWorkflow value compensation error, instanceId={}", instanceId, e);
        }
        try {
            formObjDao.deleteDBByObjId(instanceId);
        } catch (RuntimeException e) {
            logger.error("runWorkflow form compensation error, instanceId={}", instanceId, e);
        }
    }

    /**
     * 标记工作流实例失败
     * @param instanceId 实例id
     * @param errorMsg 失败原因
     */
    private void markWorkflowFailed(Long instanceId, String errorMsg) {
        if (instanceId == null) {
            return;
        }
        try {
            WfInstance failed = new WfInstance();
            failed.setId(instanceId);
            Integer failedStatus = WorkflowInstanceStatusEnum.FAILED.getValue();
            failed.setStatus(failedStatus);
            failed.setErrorMsg(errorMsg);
            long currentTime = System.currentTimeMillis();
            Timestamp finishedDt = new Timestamp(currentTime);
            failed.setFinishedDt(finishedDt);
            int count = instanceDao.updateDBById(failed);
            if (count < 1) {
                logger.error("markWorkflowFailed update fail, instanceId={}", instanceId);
            }
        } catch (RuntimeException e) {
            logger.error("markWorkflowFailed error, instanceId={}", instanceId, e);
        }
    }
}

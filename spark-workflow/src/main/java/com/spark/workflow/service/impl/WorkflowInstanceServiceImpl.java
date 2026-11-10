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
import com.spark.common.bean.form.result.FormObjValueResult;
import com.spark.common.bean.form.vo.FormObjValueVO;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.LogPrint;
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
import java.util.stream.Collectors;

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
        WfTemplateVersionResult versionResult = templateVersionDao.queryVersion(versionQuery);
        if (versionResult == null) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_VERSION_NOT_FOUND);
            return result;
        }
        if (StringUtil.isBlank(versionResult.getDagJson())) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_DAG_INVALID);
            return result;
        }
        Long instanceId = super.genObjectId(ObjectTypeEnum.WORKFLOW_INSTANCE);
        List<FormObjValue> values = runVO.getValues();
        Map<String, String> vlaueMap = new HashMap<>();
        Map<String, String> showVlaueMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(values)) {
            FormObjValueVO formObjValueVO = new FormObjValueVO();
            formObjValueVO.setObjId(instanceId);
            formObjValueVO.setFormId(runVO.getFormId());
            formObjValueVO.setValues(values);
            ResultData<Void> saveFormData = formObjValueService.saveFormObjValues(formObjValueVO);
            if (saveFormData.getCode() != ResultData.OK) {
                result.setCode(saveFormData.getCode());
                result.setMessage(saveFormData.getMessage());
                return result;
            }
            vlaueMap = values.stream().filter(v -> StringUtil.isNotBlank(v.getValue())).collect(Collectors.toMap(FormObjValue::getCode, FormObjValue::getValue, (v1, v2) -> v2));
            showVlaueMap = values.stream().filter(v -> StringUtil.isNotBlank(v.getShowValue())).collect(Collectors.toMap(FormObjValue::getCode, FormObjValue::getShowValue, (v1, v2) -> v2));
        }
        WfInstance instance = new WfInstance();
        instance.setId(instanceId);
        instance.setTemplateId(templateId);
        instance.setRevId(template.getRevId());
        instance.setRevNum(template.getRevNum());
        instance.setStatus(WorkflowInstanceStatusEnum.RUNNING.getValue());
        int count = instanceDao.insertDB(instance);
        if (count < 1) {
            logger.error("runWorkflow error, insert instance fail");
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        BaseContext context = new BaseContext();
        context.putVal(WfInstance.class, instance);
        context.put("valueMap", vlaueMap);
        context.put("showValueMap", showVlaueMap);
        context.put("templateName", template.getName());
        dagExecutor.executeAsync(context, versionResult.getDagJson());
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
        if (reviewVO == null || reviewVO.getInstanceId() == null || reviewVO.getApproved() == null) {
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
        Integer toStatus = WorkflowInstanceStatusEnum.RUNNING.getValue();
        if (!reviewVO.getApproved()) {
            toStatus = WorkflowInstanceStatusEnum.REJECTED.getValue();
        }
        // 条件更新抢锁，仅当前仍为已暂停才生效，防止并发重复审核
        int locked = instanceDao.updateInstanceStatusCas(instanceId, WorkflowInstanceStatusEnum.PAUSED.getValue(), toStatus, currentUserId);
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
        nodeUpdate.setId(reviewRow.getId());
        nodeUpdate.setStatus(WorkflowNodeStatusEnum.SUCCESS.getValue());
        nodeUpdate.setOutputJson(finalJson);
        instanceNodeDao.updateDBById(nodeUpdate);
        if (!reviewVO.getApproved()) {
            WfInstance meta = new WfInstance();
            meta.setId(instanceId);
            meta.setErrorMsg("人工审核驳回");
            meta.setFinishedDt(new Timestamp(System.currentTimeMillis()));
            instanceDao.updateDBById(meta);
            result.setCode(ResultData.OK);
            return result;
        }
        ResultData<Void> resumeResult = this.resumeDag(instance);
        if (resumeResult.getCode() != ResultData.OK) {
            return resumeResult;
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
     * 审核通过后重建执行上下文并异步恢复DAG
     * @param instance 实例查询结果
     * @return 恢复提交结果
     */
    private ResultData<Void> resumeDag(WfInstanceResult instance) {
        ResultData<Void> result = new ResultData<>();
        WfTemplateVersionQuery versionQuery = new WfTemplateVersionQuery();
        versionQuery.setId(instance.getRevId());
        WfTemplateVersionResult versionResult = templateVersionDao.queryVersion(versionQuery);
        if (versionResult == null || StringUtil.isBlank(versionResult.getDagJson())) {
            logger.error("resume dag failed, dag json missing, instanceId={}", instance.getId());
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_DAG_INVALID);
            return result;
        }
        FormObjValueQuery valueQuery = new FormObjValueQuery();
        valueQuery.setObjId(instance.getId());
        valueQuery.setPage(false);
        ResultData<List<FormObjValueResult>> valueResult = formObjValueService.queryFormObjValueList(valueQuery);
        Map<String, String> valueMap = new HashMap<>();
        Map<String, String> showValueMap = new HashMap<>();
        List<FormObjValueResult> formValues = valueResult.getData();
        if (valueResult.getCode() == ResultData.OK && CollectionUtil.isNotEmpty(formValues)) {
            valueMap = formValues.stream().filter(v -> StringUtil.isNotBlank(v.getValue())).collect(Collectors.toMap(FormObjValueResult::getCode, FormObjValueResult::getValue, (v1, v2) -> v2));
            showValueMap = formValues.stream().filter(v -> StringUtil.isNotBlank(v.getShowValue())).collect(Collectors.toMap(FormObjValueResult::getCode, FormObjValueResult::getShowValue, (v1, v2) -> v2));
        }
        WfInstance instanceEntity = new WfInstance();
        instanceEntity.setId(instance.getId());
        instanceEntity.setTemplateId(instance.getTemplateId());
        instanceEntity.setRevId(instance.getRevId());
        BaseContext context = new BaseContext();
        context.putVal(WfInstance.class, instanceEntity);
        context.put("valueMap", valueMap);
        context.put("showValueMap", showValueMap);
        context.put("templateName", instance.getTemplateName());
        String dagJson = versionResult.getDagJson();
        dagExecutor.resumeAsync(context, dagJson);
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
}

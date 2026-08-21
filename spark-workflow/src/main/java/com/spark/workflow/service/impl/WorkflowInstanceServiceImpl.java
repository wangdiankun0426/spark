package com.spark.workflow.service.impl;

import com.spark.bean.base.BaseContext;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.base.SessionHolder;
import com.spark.bean.system.entity.Session;
import com.spark.bean.workflow.entity.WfInstance;
import com.spark.bean.workflow.query.WfInstanceNodeQuery;
import com.spark.bean.workflow.query.WfInstanceQuery;
import com.spark.bean.workflow.query.WfTemplateQuery;
import com.spark.bean.workflow.query.WfTemplateVersionQuery;
import com.spark.bean.workflow.result.WfInstanceNodeResult;
import com.spark.bean.workflow.result.WfInstanceResult;
import com.spark.bean.workflow.result.WfTemplateResult;
import com.spark.bean.workflow.result.WfTemplateVersionResult;
import com.spark.bean.workflow.vo.WfRunVO;
import com.spark.bean.form.entity.FormObjValue;
import com.spark.bean.form.vo.FormObjValueVO;
import com.spark.dao.workflow.WfInstanceDao;
import com.spark.dao.workflow.WfInstanceNodeDao;
import com.spark.dao.workflow.WfTemplateDao;
import com.spark.dao.workflow.WfTemplateVersionDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.ObjectTypeEnum;
import com.spark.enums.WorkflowInstanceStatusEnum;
import com.spark.enums.WorkflowNodeStatusEnum;
import com.spark.form.service.IFormObjValueService;
import com.spark.manage.BaseService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import com.spark.workflow.engine.DagExecutor;
import com.spark.workflow.service.IWorkflowInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
 * AI工作流实例Service实现
 */
@Service
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
    public ResultData<PageResult<WfInstanceResult>> pageInstanceHistory(WfInstanceQuery query) {
        ResultData<PageResult<WfInstanceResult>> result = new ResultData<>();
        if (query == null) { query = new WfInstanceQuery(); }
        result.setData(super.pageList(query));
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
                node.setStatusName(WorkflowNodeStatusEnum.indexOf(node.getStatus()).getDesc());
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
            return result;
        }
        Session session = new Session();
        session.setUserId(SessionHolder.getCurrentUserId());
        BaseContext context = new BaseContext();
        context.putVal(WfInstance.class, instance);
        context.put("valueMap", vlaueMap);
        context.put("showValueMap", showVlaueMap);
        dagExecutor.executeAsync(session, context, versionResult.getDagJson());
        result.setData(instance);
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
}

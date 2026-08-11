package com.spark.workflow.service.impl;

import com.alibaba.fastjson2.JSON;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.base.SessionHolder;
import com.spark.bean.workflow.entity.WfInstance;
import com.spark.bean.workflow.query.WfInstanceNodeQuery;
import com.spark.bean.workflow.query.WfInstanceQuery;
import com.spark.bean.workflow.query.WfTemplateEndpointQuery;
import com.spark.bean.workflow.query.WfTemplateQuery;
import com.spark.bean.workflow.query.WfTemplateVersionQuery;
import com.spark.bean.workflow.result.WfInstanceNodeResult;
import com.spark.bean.workflow.result.WfInstanceResult;
import com.spark.bean.workflow.result.WfTemplateEndpointResult;
import com.spark.bean.workflow.result.WfTemplateResult;
import com.spark.bean.workflow.result.WfTemplateVersionResult;
import com.spark.dao.workflow.WfInstanceDao;
import com.spark.dao.workflow.WfInstanceNodeDao;
import com.spark.dao.workflow.WfTemplateDao;
import com.spark.dao.workflow.WfTemplateEndpointDao;
import com.spark.dao.workflow.WfTemplateVersionDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.ObjectTypeEnum;
import com.spark.enums.StatusEnum;
import com.spark.enums.WorkflowEndpointAuthTypeEnum;
import com.spark.enums.WorkflowInstanceStatusEnum;
import com.spark.enums.WorkflowTriggerTypeEnum;
import com.spark.manage.BaseService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import com.spark.workflow.engine.DagExecutor;
import com.spark.workflow.service.IWorkflowInstanceService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private WfTemplateEndpointDao endpointDao;
    @Autowired
    private WfTemplateDao templateDao;
    @Autowired
    private WfTemplateVersionDao templateVersionDao;
    @Autowired
    private DagExecutor dagExecutor;

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
        result.setData(nodes);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 执行工作流（公开端点调用入口）
     *
     * @param path 端点路径
     * @param params 输入参数
     * @param request HTTP请求
     * @return 执行结果
     */
    @Override
    public ResultData<Map<String, Object>> executeWorkflow(String path, Map<String, Object> params, HttpServletRequest request) {
        ResultData<Map<String, Object>> result = new ResultData<>();
        WfTemplateEndpointQuery nodeQuery = new WfTemplateEndpointQuery();
        nodeQuery.setPath(path);
        WfTemplateEndpointResult endpoint = endpointDao.queryEndpoint(nodeQuery);
        if (endpoint == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_FOUND);
            return result;
        }
        if (!StatusEnum.NORMAL.getValue().equals(endpoint.getEnabled())) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_ENDPOINT_DISABLED);
            return result;
        }
        if (WorkflowEndpointAuthTypeEnum.LOGIN.getValue().equals(endpoint.getAuthType())) {
            if (SessionHolder.getCurrentUserId() == null) {
                result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
                return result;
            }
        } else if (WorkflowEndpointAuthTypeEnum.APY_KEY.getValue().equals(endpoint.getAuthType())) {
            String authHeader = request.getHeader("Authorization");
            if (StringUtil.isBlank(authHeader) || !authHeader.equals("Bearer " + endpoint.getApiKey())) {
                result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
                return result;
            }
        }
        WfTemplateQuery templateQuery = new WfTemplateQuery();
        templateQuery.setId(endpoint.getTemplateId());
        WfTemplateResult template = templateDao.queryTemplate(templateQuery);
        if (template == null || template.getRevId() == null) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_NO_PUBLISHED_VERSION);
            return result;
        }
        Long instanceId = super.genObjectId(ObjectTypeEnum.WORKFLOW_INSTANCE);
        WfInstance instance = new WfInstance();
        instance.setId(instanceId);
        instance.setTemplateId(endpoint.getTemplateId());
        instance.setRevId(template.getRevId());
        instance.setRevNum(template.getRevNum());
        instance.setStatus(WorkflowInstanceStatusEnum.RUNNING.getValue());
        instance.setInputJson(JSON.toJSONString(params));
        instance.setTriggerType(WorkflowTriggerTypeEnum.API.getValue());
        int count = instanceDao.insertDB(instance);
        if (count < 1) {
            logger.error("executeWorkflow error, insert instance fail");
            return result;
        }
        WfTemplateVersionQuery versionQuery = new WfTemplateVersionQuery();
        versionQuery.setId(template.getRevId());
        WfTemplateVersionResult version = templateVersionDao.queryVersion(versionQuery);
        if (version == null || StringUtil.isBlank(version.getDagJson())) {
            instance.setStatus(WorkflowInstanceStatusEnum.FAILED.getValue());
            instance.setErrorMsg("已发布版本无DAG数据");
            instanceDao.updateDBById(instance);
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_DAG_INVALID);
            return result;
        }
        dagExecutor.executeAsync(instanceId, version.getDagJson(), params);
        Map<String, Object> output = new HashMap<>();
        output.put("instanceId", instanceId);
        output.put("status", "running");
        result.setData(output);
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

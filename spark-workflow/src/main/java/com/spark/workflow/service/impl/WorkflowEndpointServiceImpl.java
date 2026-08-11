package com.spark.workflow.service.impl;

import com.spark.bean.base.ResultData;
import com.spark.bean.workflow.entity.WfTemplateEndpoint;
import com.spark.bean.workflow.query.WfTemplateEndpointQuery;
import com.spark.bean.workflow.result.WfTemplateEndpointResult;
import com.spark.bean.workflow.vo.WfTemplateEndpointVO;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.dao.workflow.WfTemplateEndpointDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.OperateTypeEnum;
import com.spark.enums.StatusEnum;
import com.spark.enums.WorkflowEndpointAuthTypeEnum;
import com.spark.utils.StringUtil;
import com.spark.workflow.service.IWorkflowEndpointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 14:00:00
 */
@Service
public class WorkflowEndpointServiceImpl implements IWorkflowEndpointService {
    private final static Logger logger = LoggerFactory.getLogger(WorkflowEndpointServiceImpl.class);
    @Autowired
    private WfTemplateEndpointDao templateNodeDao;

    /**
     * 创建端点
     * @param nodeVO
     * @return
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.WORKFLOW_ENDPOINT_UPDATE)
    public ResultData<Void> createEndpoint(WfTemplateEndpointVO nodeVO) {
        ResultData<Void> result = new ResultData<>();
        if (nodeVO == null || nodeVO.getTemplateId() == null || StringUtil.isBlank(nodeVO.getPath())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        WfTemplateEndpointQuery checkQuery = new WfTemplateEndpointQuery();
        checkQuery.setPath(nodeVO.getPath());
        WfTemplateEndpointResult exist = templateNodeDao.queryEndpoint(checkQuery);
        if (exist != null) {
            result.setErrorCode(ErrorCodeEnum.WORKFLOW_ENDPOINT_DUPLICATE);
            return result;
        }
        WfTemplateEndpoint node = new WfTemplateEndpoint();
        BeanUtils.copyProperties(nodeVO, node);
        if (node.getEnabled() == null) {
            node.setEnabled(StatusEnum.NORMAL.getValue());
        }
        if (node.getAuthType() == null) {
            node.setAuthType(WorkflowEndpointAuthTypeEnum.NONE.getValue());
        }
        if (WorkflowEndpointAuthTypeEnum.APY_KEY.getValue().equals(node.getAuthType()) && StringUtil.isBlank(node.getApiKey())) {
            node.setApiKey("sk-" + UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        }
        int count = templateNodeDao.insertDB(node);
        if (count < 1) {
            logger.error("createEndpoint error, insert db fail");
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改端点
     * @param nodeVO
     * @return
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.WORKFLOW_ENDPOINT_UPDATE)
    public ResultData<Void> updateEndpoint(WfTemplateEndpointVO nodeVO) {
        ResultData<Void> result = new ResultData<>();
        if (nodeVO == null || nodeVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (StringUtil.isNotBlank(nodeVO.getPath())) {
            WfTemplateEndpointQuery checkQuery = new WfTemplateEndpointQuery();
            checkQuery.setPath(nodeVO.getPath());
            WfTemplateEndpointResult exist = templateNodeDao.queryEndpoint(checkQuery);
            if (exist != null && !exist.getId().equals(nodeVO.getId())) {
                result.setErrorCode(ErrorCodeEnum.WORKFLOW_ENDPOINT_DUPLICATE);
                return result;
            }
        }
        WfTemplateEndpoint node = new WfTemplateEndpoint();
        BeanUtils.copyProperties(nodeVO, node);
        int count = templateNodeDao.updateDBById(node);
        if (count < 1) {
            logger.error("updateEndpoint error, update db fail");
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除端点
     * @param nodeVO
     * @return
     */
    @Override
    public ResultData<Void> deleteEndpoint(WfTemplateEndpointVO nodeVO) {
        ResultData<Void> result = new ResultData<>();
        if (nodeVO == null || nodeVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        WfTemplateEndpoint node = new WfTemplateEndpoint();
        node.setId(nodeVO.getId());
        int count = templateNodeDao.deleteDBById(node);
        if (count < 1) {
            logger.error("deleteEndpoint error, delete db fail");
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询端点
     * @param query
     * @return
     */
    @Override
    public ResultData<WfTemplateEndpointResult> queryEndpoint(WfTemplateEndpointQuery query) {
        ResultData<WfTemplateEndpointResult> result = new ResultData<>();
        if (query == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        WfTemplateEndpointResult nodeResult = templateNodeDao.queryEndpoint(query);
        if (nodeResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        result.setData(nodeResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询端点列表
     * @param query
     * @return
     */
    @Override
    public ResultData<java.util.List<WfTemplateEndpointResult>> queryEndpointList(WfTemplateEndpointQuery query) {
        ResultData<java.util.List<WfTemplateEndpointResult>> result = new ResultData<>();
        if (query == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        query.setPage(false);
        List<WfTemplateEndpointResult> list = templateNodeDao.queryEndpointList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }
}

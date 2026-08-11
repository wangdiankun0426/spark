package com.spark.workflow.service;

import com.spark.bean.base.ResultData;
import com.spark.bean.workflow.query.WfTemplateEndpointQuery;
import com.spark.bean.workflow.result.WfTemplateEndpointResult;
import com.spark.bean.workflow.vo.WfTemplateEndpointVO;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 14:00:00
 */
public interface IWorkflowEndpointService {

    /**
     * 创建端点
     * @param nodeVO
     * @return
     */
    ResultData<Void> createEndpoint(WfTemplateEndpointVO nodeVO);

    /**
     * 修改端点
     * @param nodeVO
     * @return
     */
    ResultData<Void> updateEndpoint(WfTemplateEndpointVO nodeVO);

    /**
     * 删除端点
     * @param nodeVO
     * @return
     */
    ResultData<Void> deleteEndpoint(WfTemplateEndpointVO nodeVO);

    /**
     * 端点详情
     * @param query
     * @return
     */
    ResultData<WfTemplateEndpointResult> queryEndpoint(WfTemplateEndpointQuery query);

    /**
     * 端点列表
     * @param query
     * @return
     */
    ResultData<java.util.List<WfTemplateEndpointResult>> queryEndpointList(WfTemplateEndpointQuery query);

}

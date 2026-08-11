package com.spark.workflow.service;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.workflow.query.WfInstanceQuery;
import com.spark.bean.workflow.query.WfInstanceNodeQuery;
import com.spark.bean.workflow.result.WfInstanceResult;
import com.spark.bean.workflow.result.WfInstanceNodeResult;
import jakarta.servlet.http.HttpServletRequest;

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
 * AI工作流实例Service接口
 */
public interface IWorkflowInstanceService {

    /**
     * 分页查询运行实例
     *
     * @param query 查询参数
     * @return 分页结果
     */
    ResultData<PageResult<WfInstanceResult>> pageInstanceHistory(WfInstanceQuery query);

    /**
     * 查询运行实例详情
     *
     * @param query 查询参数
     * @return 实例详情
     */
    ResultData<WfInstanceResult> queryInstanceDetail(WfInstanceQuery query);

    /**
     * 查询实例节点执行记录
     *
     * @param query 查询参数
     * @return 节点执行记录列表
     */
    ResultData<List<WfInstanceNodeResult>> queryInstanceNodes(WfInstanceNodeQuery query);

    /**
     * 执行工作流（公开端点调用入口）
     *
     * @param path 端点路径
     * @param params 输入参数
     * @param request HTTP请求
     * @return 执行结果
     */
    ResultData<Map<String, Object>> executeWorkflow(String path, Map<String, Object> params, HttpServletRequest request);
}

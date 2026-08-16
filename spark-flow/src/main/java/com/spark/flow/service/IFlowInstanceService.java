package com.spark.flow.service;

import com.spark.bean.flow.query.FlowInstanceQuery;
import com.spark.bean.flow.result.FlowInstanceResult;
import com.spark.bean.flow.vo.FlowInstanceVO;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025-11-02 15:47:22
 */
public interface IFlowInstanceService {

    /**
     * 创建流程实例
     * @param instanceVO 流程实例创建参数
     * @return 创建结果
     */
    ResultData<Void> createInstance(FlowInstanceVO instanceVO);

    /**
     * 查询流程实例详情
     * @param query 查询参数
     * @return 查询结果
     */
    ResultData<FlowInstanceResult> showInstanceDetail(FlowInstanceQuery query);

    /**
     * 分页查询我的申请列表
     * @param query 查询参数
     * @return 查询结果
     */
    ResultData<PageResult<FlowInstanceResult>> pageMyAppliedList(FlowInstanceQuery query);

    /**
     * 分页查询我的待办列表
     * @param query 查询参数
     * @return 查询结果
     */
    ResultData<PageResult<FlowInstanceResult>> pageMyPendingList(FlowInstanceQuery query);

    /**
     * 审批流程实例
     * @param instanceVO 审批参数
     * @return 审批结果
     */
    ResultData<Void> approveInstance(FlowInstanceVO instanceVO);

    /**
     * 分页查询我的已办列表
     * @param query
     * @return
     */
    ResultData<PageResult<FlowInstanceResult>> pageMyPendedList(FlowInstanceQuery query);

    /**
     * 分页查询全部流程实例（管理端）
     * @param query 查询参数
     * @return 列表
     */
    ResultData<PageResult<FlowInstanceResult>> pageInstanceList(FlowInstanceQuery query);
}

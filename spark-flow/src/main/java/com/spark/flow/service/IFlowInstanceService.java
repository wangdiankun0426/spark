package com.spark.flow.service;

import com.spark.common.bean.flow.query.FlowInstanceQuery;
import com.spark.common.bean.flow.result.FlowInstanceResult;
import com.spark.common.bean.flow.vo.FlowInstanceVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;

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
    ResultData<PageResult<FlowInstanceResult>> pageMyApplicationList(FlowInstanceQuery query);

    /**
     * 分页查询我的待办列表
     * @param query 查询参数
     * @return 查询结果
     */
    ResultData<PageResult<FlowInstanceResult>> pageMyTodoList(FlowInstanceQuery query);

    /**
     * 审批流程实例
     * @param instanceVO 审批参数
     * @return 审批结果
     */
    ResultData<Void> approveInstance(FlowInstanceVO instanceVO);

    /**
     * 催办流程实例
     * @param instanceVO 催办参数
     * @return 催办结果
     */
    ResultData<Void> urgeInstance(FlowInstanceVO instanceVO);

    /**
     * 转办流程实例
     * @param instanceVO 转办参数
     * @return 转办结果
     */
    ResultData<Void> transferInstance(FlowInstanceVO instanceVO);

    /**
     * 加签流程实例
     * @param instanceVO 加签参数
     * @return 加签结果
     */
    ResultData<Void> addSignInstance(FlowInstanceVO instanceVO);

    /**
     * 分页查询我的已办列表
     * @param query
     * @return
     */
    ResultData<PageResult<FlowInstanceResult>> pageMyDoneList(FlowInstanceQuery query);

    /**
     * 分页查询全部流程实例（管理端）
     * @param query 查询参数
     * @return 列表
     */
    ResultData<PageResult<FlowInstanceResult>> pageInstanceList(FlowInstanceQuery query);

    /**
     * 撤回流程实例
     * @param instanceVO 撤回参数
     * @return 撤回结果
     */
    ResultData<Void> recallInstance(FlowInstanceVO instanceVO);
}

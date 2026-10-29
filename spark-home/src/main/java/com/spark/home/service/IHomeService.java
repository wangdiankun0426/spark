package com.spark.home.service;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.home.query.DashboardQuery;
import com.spark.common.bean.home.result.DashboardAssetResult;
import com.spark.common.bean.home.result.DashboardHotResult;
import com.spark.common.bean.home.result.DashboardUsageSummaryResult;
import com.spark.common.bean.home.result.FlowCountResult;
import com.spark.common.bean.flow.result.FlowInstanceResult;
import com.spark.common.bean.llm.result.ModelUsageTrendResult;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-29 19:00:00
 * 我的工作台统计服务，按卡片独立提供数据
 */
public interface IHomeService {

    /**
     * 我的用量汇总
     * @param query 查询参数
     * @return 用量汇总
     */
    ResultData<DashboardUsageSummaryResult> queryUsageSummary(DashboardQuery query);

    /**
     * 我的用量趋势
     * @param query 查询参数
     * @return 趋势列表
     */
    ResultData<List<ModelUsageTrendResult>> queryUsageTrend(DashboardQuery query);

    /**
     * 我的待办总数
     * @return 待办数
     */
    ResultData<Integer> countTodo();

    /**
     * 我的流程四项数量，待办、已办、申请、抄送
     * @return 流程数量
     */
    ResultData<FlowCountResult> queryFlowCount();

    /**
     * 可申请流程模板
     * @return 模板列表
     */
    ResultData<List<DashboardHotResult>> queryApplyTemplates();

    /**
     * 我的待办前N条
     * @return 待办列表
     */
    ResultData<List<FlowInstanceResult>> queryTodoList();

    /**
     * 综合推荐榜
     * @param query 查询参数
     * @return 推荐榜
     */
    ResultData<List<DashboardHotResult>> queryRecommend(DashboardQuery query);

    /**
     * 我的资产数量
     * @return 资产数量
     */
    ResultData<DashboardAssetResult> queryAsset();
}

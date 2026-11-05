package com.spark.home.service;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.home.query.DashboardQuery;
import com.spark.common.bean.home.result.DashboardHotResult;
import com.spark.common.bean.home.result.DashboardOverviewResult;
import com.spark.common.bean.home.result.FlowCountResult;
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
 * 工作台统计服务
 */
public interface IHomeService {

    /**
     * 工作台总览，模型用量与资产数量合并返回
     * @return 总览数据
     */
    ResultData<DashboardOverviewResult> queryOverview();

    /**
     * 我的用量趋势
     * @param query 查询参数
     * @return 趋势列表
     */
    ResultData<List<ModelUsageTrendResult>> queryUsageTrend(DashboardQuery query);

    /**
     * 我的流程四项数量，待办、已办、申请、抄送
     * @return 流程数量
     */
    ResultData<FlowCountResult> queryFlowCount();

    /**
     * 综合推荐榜
     * @return 推荐榜
     */
    ResultData<List<DashboardHotResult>> queryRecommend();
}

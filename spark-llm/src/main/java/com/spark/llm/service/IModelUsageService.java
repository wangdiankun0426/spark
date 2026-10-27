package com.spark.llm.service;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.llm.query.ModelUsageQuery;
import com.spark.common.bean.llm.result.ModelUsageResult;
import com.spark.common.bean.llm.result.ModelUsageSummaryResult;
import com.spark.common.bean.llm.result.ModelUsageTrendResult;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/10/27 10:00:00
 */
public interface IModelUsageService {

    /**
     * 分页查询模型用量明细
     * @param query 查询条件
     * @return 分页结果
     */
    ResultData<PageResult<ModelUsageResult>> pageUsageList(ModelUsageQuery query);

    /**
     * 按模型类型汇总用量
     * @param query 查询条件
     * @return 汇总列表
     */
    ResultData<List<ModelUsageSummaryResult>> queryUsageSummary(ModelUsageQuery query);

    /**
     * 按日期与类型查询用量趋势
     * @param query 查询条件
     * @return 趋势列表
     */
    ResultData<List<ModelUsageTrendResult>> queryUsageTrend(ModelUsageQuery query);

}

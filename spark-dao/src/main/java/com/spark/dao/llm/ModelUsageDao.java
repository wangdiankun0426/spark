package com.spark.dao.llm;

import com.spark.common.bean.home.result.DashboardHotResult;
import com.spark.common.bean.home.result.DashboardUsageSummaryResult;
import com.spark.common.bean.llm.entity.ModelUsage;
import com.spark.common.bean.llm.query.ModelUsageQuery;
import com.spark.common.bean.llm.result.ModelUsageResult;
import com.spark.common.bean.llm.result.ModelUsageSummaryResult;
import com.spark.common.bean.llm.result.ModelUsageTrendResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/10/27 10:00:00
 * 模型token用量明细DAO
 */
public interface ModelUsageDao extends BaseDao<ModelUsage> {

    /**
     * 批量新增用量明细
     * @param list 用量明细
     * @return 新增条数
     */
    int insertBatch(@Param("list") List<ModelUsage> list);

    /**
     * 查询用量明细数量
     * @param query 查询参数
     * @return 数量
     */
    int queryUsageCount(ModelUsageQuery query);

    /**
     * 查询用量明细列表
     * @param query 查询参数
     * @return 明细列表
     */
    List<ModelUsageResult> queryUsageList(ModelUsageQuery query);

    /**
     * 按模型类型汇总用量
     * @param query 查询参数
     * @return 汇总列表
     */
    List<ModelUsageSummaryResult> queryUsageSummary(ModelUsageQuery query);

    /**
     * 按日期与类型查询用量趋势
     * @param query 查询参数
     * @return 趋势列表
     */
    List<ModelUsageTrendResult> queryUsageTrend(ModelUsageQuery query);

    /**
     * 不区分类型的用量总计
     * @param query 查询参数
     * @return 用量总计
     */
    DashboardUsageSummaryResult queryUsageTotal(ModelUsageQuery query);

    /**
     * 按模型聚合调用次数
     * @param query 查询参数
     * @return 热门模型列表
     */
    List<DashboardHotResult> queryHotModels(ModelUsageQuery query);
}

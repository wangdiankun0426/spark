package com.spark.common.bean.kb.result;

import lombok.Data;

/**
 * 检索统计结果
 *
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-28 16:30:00
 */
@Data
public class RetrieveStatsResult {

    /**
     * 总检索次数
     */
    private Long totalRetrieveCount;

    /**
     * 平均相似度
     */
    private Double avgSimilarity;

    /**
     * 检索命中率(QA命中)
     */
    private Double qaHitRate;

    /**
     * 无结果查询次数
     */
    private Long noResultCount;

    /**
     * 平均反馈评分
     */
    private Double avgFeedbackScore;

    /**
     * 反馈数量
     */
    private Long feedbackCount;

    /**
     * 平均耗时(毫秒)
     */
    private Long avgCostTime;
}
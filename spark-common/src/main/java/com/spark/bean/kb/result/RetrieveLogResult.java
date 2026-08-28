package com.spark.bean.kb.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

/**
 * 检索日志结果
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
public class RetrieveLogResult extends BaseResult {

    /**
     * 知识库ID
     */
    private Long kbId;

    /**
     * 知识库名称
     */
    private String knowledgeName;

    /**
     * 检索查询内容
     */
    private String query;

    /**
     * 检索策略
     */
    private String strategy;

    /**
     * 召回数量
     */
    private Integer retrieveCount;

    /**
     * QA是否命中 0否 1是
     */
    private Integer qaHit;

    /**
     * QA命中名称
     */
    private String qaHitName;

    /**
     * 平均相似度
     */
    private Double avgSimilarity;

    /**
     * 最高相似度
     */
    private Double maxSimilarity;

    /**
     * 耗时(毫秒)
     */
    private Long costTime;

    /**
     * 用户反馈评分 1-5
     */
    private Integer feedbackScore;

    /**
     * 用户反馈备注
     */
    private String feedbackRemark;
}
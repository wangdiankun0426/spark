package com.spark.common.bean.kb.result;

import lombok.Data;

import java.util.List;

/**
 * 使用情况统计结果
 *
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-28 17:00:00
 */
@Data
public class UsageStatsResult {

    /**
     * 知识库数量
     */
    private Long knowledgeCount;

    /**
     * 文档数量
     */
    private Long documentCount;

    /**
     * 文档分片数量
     */
    private Long chunkCount;

    /**
     * 今日检索次数
     */
    private Long todayRetrieveCount;

    /**
     * 本周检索次数
     */
    private Long weekRetrieveCount;

    /**
     * 本月检索次数
     */
    private Long monthRetrieveCount;

    /**
     * 热门查询列表
     */
    private List<HotQueryResult> hotQueries;

    /**
     * 用户使用排行
     */
    private List<UserRankResult> userRankList;
}
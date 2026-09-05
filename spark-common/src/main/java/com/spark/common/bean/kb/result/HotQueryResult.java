package com.spark.common.bean.kb.result;

import lombok.Data;

/**
 * 热门查询结果
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
public class HotQueryResult {

    /**
     * 查询内容
     */
    private String query;

    /**
     * 查询次数
     */
    private Long queryCount;

    /**
     * 平均相似度
     */
    private Double avgSimilarity;
}
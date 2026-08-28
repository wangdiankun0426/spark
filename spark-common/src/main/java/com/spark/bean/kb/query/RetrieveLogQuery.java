package com.spark.bean.kb.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

/**
 * 检索日志查询
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
public class RetrieveLogQuery extends BaseQuery {

    /**
     * 知识库ID
     */
    private Long kbId;

    /**
     * 检索策略
     */
    private String strategy;

    /**
     * QA是否命中
     */
    private Integer qaHit;

    /**
     * 是否无结果查询
     */
    private Boolean noResult;
}
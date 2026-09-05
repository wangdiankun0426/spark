package com.spark.common.bean.kb.result;

import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-28 10:00:00
 * 检索详细结果（包含分数和元数据）
 */
@Data
public class RetrieveDetailResult {

    /**
     * 详细结果列表
     */
    private List<RetrieveDetailItem> items;

    /**
     * 是否命中QA
     */
    private Boolean qaHit;

    /**
     * 检索策略
     */
    private String strategy;
}

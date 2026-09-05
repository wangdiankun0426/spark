package com.spark.common.bean.kb.result;

import com.spark.common.bean.base.BaseResult;
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
 * 检索测试结果
 */
@Data
public class RetrieveTestResult extends BaseResult {

    /**
     * 查询文本
     */
    private String query;

    /**
     * 检索结果内容列表
     */
    private List<RetrieveItemResult> items;

    /**
     * 检索耗时(毫秒)
     */
    private Long costTime;

    /**
     * 召回数量
     */
    private Integer retrieveCount;

    /**
     * 检索策略
     */
    private String strategy;

    /**
     * QA是否命中
     */
    private Boolean qaHit;
}

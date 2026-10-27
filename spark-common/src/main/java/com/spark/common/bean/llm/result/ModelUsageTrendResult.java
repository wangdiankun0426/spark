package com.spark.common.bean.llm.result;

import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/10/27 10:00:00
 * 模型token用量每日趋势
 */
@Data
public class ModelUsageTrendResult {

    /**
     * 日期（yyyy-MM-dd）
     **/
    private String dt;

    /**
     * 模型类型：1语言，2向量，3排序
     **/
    private Integer type;

    /**
     * 总token数
     **/
    private Long totalTokens;

    /**
     * 调用次数
     **/
    private Long callCount;

}

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
 * 模型token用量按类型汇总
 */
@Data
public class ModelUsageSummaryResult {

    /**
     * 模型类型：1语言，2向量，3排序
     **/
    private Integer type;

    /**
     * 输入token总数
     **/
    private Long inputTokens;

    /**
     * 输出token总数
     **/
    private Long outputTokens;

    /**
     * 总token数
     **/
    private Long totalTokens;

    /**
     * 调用次数
     **/
    private Long callCount;

}

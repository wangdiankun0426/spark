package com.spark.common.bean.home.result;

import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-29 18:00:00
 * 我的模型用量汇总
 */
@Data
public class DashboardUsageSummaryResult {

    /**
     * 调用次数
     */
    private Integer callCount;

    /**
     * 输入token
     */
    private Long inputTokens;

    /**
     * 输出token
     */
    private Long outputTokens;

    /**
     * 总token
     */
    private Long totalTokens;
}

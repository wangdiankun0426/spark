package com.spark.bean.workflow.config;

import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-27 10:00:00
 * 重试配置
 */
@Data
public class RetryConfig {
    /**
     * 总尝试次数上限（含首次执行）
     */
    private int maxRetries = 3;

    /**
     * 初始间隔（毫秒）
     */
    private long initialInterval = 1000;

    /**
     * 指数退避倍数
     */
    private double multiplier = 2.0;

    /**
     * 最大间隔（毫秒）
     */
    private long maxInterval = 30000;
}

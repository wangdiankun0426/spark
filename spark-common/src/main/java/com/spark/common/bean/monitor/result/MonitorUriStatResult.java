package com.spark.common.bean.monitor.result;

import com.spark.common.bean.base.BaseResult;
import lombok.Data;

import java.math.BigDecimal;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * URI维度请求统计
 */
@Data
public class MonitorUriStatResult extends BaseResult {

    /**
     * 请求URI模板
     */
    private String uri;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求总数
     */
    private Long totalCount;

    /**
     * 2xx请求数
     */
    private Long count2xx;

    /**
     * 3xx请求数
     */
    private Long count3xx;

    /**
     * 4xx请求数
     */
    private Long count4xx;

    /**
     * 5xx请求数
     */
    private Long count5xx;

    /**
     * 总耗时
     */
    private Long totalCost;

    /**
     * 平均耗时
     */
    private Long avgCost;

    /**
     * 最大耗时
     */
    private Integer maxCost;

    /**
     * 错误请求数
     */
    private Long errorCount;

    /**
     * 错误率
     */
    private BigDecimal errorRate;
}

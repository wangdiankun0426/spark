package com.spark.common.bean.monitor.result;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.common.bean.base.BaseResult;
import lombok.Data;

import java.util.Date;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * 接口请求分钟趋势点
 */
@Data
public class MonitorApiTrendResult extends BaseResult {

    /**
     * 快照时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date snapshotDt;

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
}

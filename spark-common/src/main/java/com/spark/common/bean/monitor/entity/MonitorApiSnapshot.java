package com.spark.common.bean.monitor.entity;

import com.spark.common.bean.base.BaseEntity;
import lombok.Data;

import java.sql.Timestamp;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * 接口请求监控快照
 */
@Data
public class MonitorApiSnapshot extends BaseEntity {

    /**
     * 节点标识
     */
    private String nodeKey;

    /**
     * 快照时间
     */
    private Timestamp snapshotDt;

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
    private Integer totalCount;

    /**
     * 2xx请求数
     */
    private Integer count2xx;

    /**
     * 3xx请求数
     */
    private Integer count3xx;

    /**
     * 4xx请求数
     */
    private Integer count4xx;

    /**
     * 5xx请求数
     */
    private Integer count5xx;

    /**
     * 总耗时
     */
    private Long totalCost;

    /**
     * 最大耗时
     */
    private Integer maxCost;
}

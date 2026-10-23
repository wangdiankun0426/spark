package com.spark.common.bean.monitor.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * 监控快照查询参数
 */
@Data
public class MonitorSnapshotQuery extends BaseQuery {

    /**
     * 节点标识
     */
    private String nodeKey;

    /**
     * 起始时间（yyyy-MM-dd HH:mm:ss）
     */
    private String startTime;

    /**
     * 结束时间（yyyy-MM-dd HH:mm:ss）
     */
    private String endTime;

    /**
     * 请求URI
     */
    private String uri;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 队列名
     */
    private String queueName;
}

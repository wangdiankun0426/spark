package com.spark.common.bean.monitor.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
 * 消息队列监控快照
 */
@Data
public class MonitorMqSnapshot extends BaseEntity {

    /**
     * 实际采集节点标识
     */
    private String nodeKey;

    /**
     * 快照时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Timestamp snapshotDt;

    /**
     * 队列名
     */
    private String queueName;

    /**
     * 队列中文名
     */
    private String queueLabel;

    /**
     * 队列消息总数
     */
    private Long messageCount;

    /**
     * 消费者数
     */
    private Integer consumerCount;
}

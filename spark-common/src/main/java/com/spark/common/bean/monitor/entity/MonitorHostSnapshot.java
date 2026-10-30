package com.spark.common.bean.monitor.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.common.bean.base.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * 主机与JVM监控快照
 */
@Data
public class MonitorHostSnapshot extends BaseEntity {

    /**
     * 节点标识
     */
    private String nodeKey;

    /**
     * 快照时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Timestamp snapshotDt;

    /**
     * CPU使用率
     */
    private BigDecimal cpuUsage;

    /**
     * 物理内存总量
     */
    private Long memTotal;

    /**
     * 物理内存已用
     */
    private Long memUsed;

    /**
     * 物理内存使用率
     */
    private BigDecimal memUsage;

    /**
     * Swap总量
     */
    private Long swapTotal;

    /**
     * Swap已用
     */
    private Long swapUsed;

    /**
     * Swap使用率
     */
    private BigDecimal swapUsage;

    /**
     * 网卡下行速率
     */
    private Long netRxRate;

    /**
     * 网卡上行速率
     */
    private Long netTxRate;

    /**
     * JVM堆已用
     */
    private Long heapUsed;

    /**
     * JVM堆最大
     */
    private Long heapMax;

    /**
     * JVM堆使用率
     */
    private BigDecimal heapUsage;

    /**
     * JVM非堆已用
     */
    private Long nonheapUsed;

    /**
     * YoungGC累计次数
     */
    private Long youngGcCount;

    /**
     * YoungGC累计耗时
     */
    private Long youngGcCost;

    /**
     * OldGC累计次数
     */
    private Long oldGcCount;

    /**
     * OldGC累计耗时
     */
    private Long oldGcCost;

    /**
     * JVM线程总数
     */
    private Integer threadCount;

    /**
     * 阻塞线程数
     */
    private Integer blockedCount;

    /**
     * 死锁线程数
     */
    private Integer deadlockedCount;

    /**
     * 已加载类数
     */
    private Integer loadedClassCount;

    /**
     * 磁盘分区明细JSON
     */
    private String diskJson;

    /**
     * 主机与JVM静态信息JSON
     */
    private String hostInfoJson;
}

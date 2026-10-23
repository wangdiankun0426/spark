package com.spark.common.bean.monitor.entity;

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
 * Redis监控快照
 */
@Data
public class MonitorRedisSnapshot extends BaseEntity {

    /**
     * 实际采集节点标识
     */
    private String nodeKey;

    /**
     * 快照时间
     */
    private Timestamp snapshotDt;

    /**
     * Redis版本
     */
    private String version;

    /**
     * 运行天数
     */
    private Integer uptimeDays;

    /**
     * 连接客户端数
     */
    private Integer connectedClients;

    /**
     * 阻塞客户端数
     */
    private Integer blockedClients;

    /**
     * 每秒操作数
     */
    private Integer opsPerSec;

    /**
     * 命中率
     */
    private BigDecimal hitRate;

    /**
     * 已用内存
     */
    private Long usedMemory;

    /**
     * 常驻内存
     */
    private Long usedMemoryRss;

    /**
     * 内存峰值
     */
    private Long usedMemoryPeak;

    /**
     * 最大内存限制
     */
    private Long maxmemory;

    /**
     * 内存碎片率
     */
    private BigDecimal fragmentationRatio;

    /**
     * 累计驱逐键数
     */
    private Long evictedKeys;

    /**
     * 累计过期键数
     */
    private Long expiredKeys;

    /**
     * 当前库键总数
     */
    private Long dbSize;

    /**
     * 最近RDB保存状态
     */
    private String rdbStatus;

    /**
     * 是否开启AOF
     */
    private Integer aofEnabled;

    /**
     * 各db键空间明细JSON
     */
    private String keyspaceJson;

    /**
     * 命令统计Top10 JSON
     */
    private String commandJson;
}

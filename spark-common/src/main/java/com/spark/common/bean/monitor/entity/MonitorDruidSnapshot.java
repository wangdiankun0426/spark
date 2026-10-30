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
 * Druid慢SQL监控快照
 */
@Data
public class MonitorDruidSnapshot extends BaseEntity {

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
     * 两数据源慢SQL TopN JSON
     */
    private String sqlJson;
}

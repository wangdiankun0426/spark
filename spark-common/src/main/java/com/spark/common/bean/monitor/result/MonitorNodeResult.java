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
 * 监控节点
 */
@Data
public class MonitorNodeResult extends BaseResult {

    /**
     * 节点标识
     */
    private String nodeKey;

    /**
     * 最近快照时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date lastSnapshotDt;

    /**
     * 是否在线
     */
    private Boolean online;
}

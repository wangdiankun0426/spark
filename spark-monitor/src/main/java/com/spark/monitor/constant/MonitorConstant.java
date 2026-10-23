package com.spark.monitor.constant;

import com.spark.common.constant.MqQueueKey;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * 监控常量
 */
public class MonitorConstant {

    /**
     * 中间件采集分布式锁key
     */
    public static final String MIDDLEWARE_LOCK_KEY = "monitor:middleware:lock";

    /**
     * 中间件采集锁过期秒数
     */
    public static final long MIDDLEWARE_LOCK_SECONDS = 50L;

    /**
     * 节点在线判定阈值毫秒
     */
    public static final long ONLINE_THRESHOLD_MS = 3 * 60 * 1000L;

    /**
     * 主机快照保留天数
     */
    public static final int HOST_RETAIN_DAYS = 30;

    /**
     * 接口快照保留天数
     */
    public static final int API_RETAIN_DAYS = 15;

    /**
     * Redis快照保留天数
     */
    public static final int REDIS_RETAIN_DAYS = 30;

    /**
     * 消息队列快照保留天数
     */
    public static final int MQ_RETAIN_DAYS = 30;

    /**
     * Druid快照保留天数
     */
    public static final int DRUID_RETAIN_DAYS = 30;

    /**
     * Redis命令统计保留条数
     */
    public static final int COMMAND_TOP_LIMIT = 10;

    /**
     * Druid慢SQL每数据源保留条数
     */
    public static final int DRUID_SQL_LIMIT = 20;

    /**
     * 监控业务队列中文名
     */
    public static final Map<String, String> QUEUE_LABEL_MAP = new LinkedHashMap<>();

    static {
        QUEUE_LABEL_MAP.put(MqQueueKey.SYS_MSG, "系统消息队列");
        QUEUE_LABEL_MAP.put(MqQueueKey.LOGIN_LOG, "登录日志队列");
        QUEUE_LABEL_MAP.put(MqQueueKey.OPERATE_LOG, "操作日志队列");
    }

    private MonitorConstant() {
    }
}

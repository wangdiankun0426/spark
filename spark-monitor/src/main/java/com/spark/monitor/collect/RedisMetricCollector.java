package com.spark.monitor.collect;

import com.spark.common.bean.monitor.entity.MonitorRedisSnapshot;
import com.spark.common.utils.JsonUtil;
import com.spark.monitor.constant.MonitorConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * Redis指标采集器
 */
@Component
public class RedisMetricCollector {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * 采集一条Redis快照
     * @param nodeKey 实际采集节点标识
     * @return 快照
     */
    public MonitorRedisSnapshot buildSnapshot(String nodeKey) {
        RedisConnection connection = redisConnectionFactory.getConnection();
        try {
            Properties info = connection.commands().info();
            Properties commandStats = connection.commands().info("commandstats");
            Properties keyspace = connection.commands().info("keyspace");
            Long dbSize = connection.commands().dbSize();
            return assemble(nodeKey, info, commandStats, keyspace, dbSize);
        } finally {
            RedisConnectionUtils.releaseConnection(connection, redisConnectionFactory);
        }
    }

    /**
     * 组装Redis快照
     * @param nodeKey 节点标识
     * @param info 全量info
     * @param commandStats 命令统计
     * @param keyspace 键空间
     * @param dbSize 键总数
     * @return 快照
     */
    private MonitorRedisSnapshot assemble(String nodeKey, Properties info, Properties commandStats,
                                          Properties keyspace, Long dbSize) {
        MonitorRedisSnapshot snapshot = new MonitorRedisSnapshot();
        Timestamp snapshotDt = new Timestamp(System.currentTimeMillis());
        snapshot.setNodeKey(nodeKey);
        snapshot.setSnapshotDt(snapshotDt);
        snapshot.setVersion(info.getProperty("redis_version"));
        int uptimeDays = parseInt(info, "uptime_in_days");
        snapshot.setUptimeDays(uptimeDays);
        int connectedClients = parseInt(info, "connected_clients");
        snapshot.setConnectedClients(connectedClients);
        int blockedClients = parseInt(info, "blocked_clients");
        snapshot.setBlockedClients(blockedClients);
        int opsPerSec = parseInt(info, "instantaneous_ops_per_sec");
        snapshot.setOpsPerSec(opsPerSec);

        long hits = parseLong(info, "keyspace_hits");
        long misses = parseLong(info, "keyspace_misses");
        double hitRateValue = hits + misses > 0 ? (double) hits * 100 / (hits + misses) : 0D;
        BigDecimal hitRate = BigDecimal.valueOf(hitRateValue).setScale(2, RoundingMode.HALF_UP);
        snapshot.setHitRate(hitRate);

        long usedMemory = parseLong(info, "used_memory");
        snapshot.setUsedMemory(usedMemory);
        long usedMemoryRss = parseLong(info, "used_memory_rss");
        snapshot.setUsedMemoryRss(usedMemoryRss);
        long usedMemoryPeak = parseLong(info, "used_memory_peak");
        snapshot.setUsedMemoryPeak(usedMemoryPeak);
        long maxmemory = parseLong(info, "maxmemory");
        snapshot.setMaxmemory(maxmemory);
        BigDecimal fragmentationRatio = parseDecimal(info, "mem_fragmentation_ratio");
        snapshot.setFragmentationRatio(fragmentationRatio);
        long evictedKeys = parseLong(info, "evicted_keys");
        snapshot.setEvictedKeys(evictedKeys);
        long expiredKeys = parseLong(info, "expired_keys");
        snapshot.setExpiredKeys(expiredKeys);
        long dbSizeValue = dbSize == null ? 0L : dbSize;
        snapshot.setDbSize(dbSizeValue);
        snapshot.setRdbStatus(info.getProperty("rdb_last_bgsave_status"));
        int aofEnabled = parseInt(info, "aof_enabled");
        snapshot.setAofEnabled(aofEnabled);
        List<Map<String, Object>> keyspaceList = buildKeyspaceList(keyspace);
        snapshot.setKeyspaceJson(JsonUtil.toString(keyspaceList));
        List<Map<String, Object>> commandList = buildCommandList(commandStats);
        snapshot.setCommandJson(JsonUtil.toString(commandList));
        return snapshot;
    }

    /**
     * 解析命令统计TopN
     * @param commandStats 命令统计属性
     * @return 命令列表
     */
    private List<Map<String, Object>> buildCommandList(Properties commandStats) {
        List<Map<String, Object>> commandList = new ArrayList<>();
        if (commandStats == null) {
            return commandList;
        }
        for (String key : commandStats.stringPropertyNames()) {
            if (!key.startsWith("cmdstat_")) {
                continue;
            }
            String name = key.substring("cmdstat_".length());
            String statText = commandStats.getProperty(key);
            long calls = extractLong(statText, "calls");
            Map<String, Object> command = new LinkedHashMap<>();
            command.put("name", name);
            command.put("value", calls);
            commandList.add(command);
        }
        Comparator<Map<String, Object>> byCalls = Comparator
                .comparingLong((Map<String, Object> item) -> ((Number) item.get("value")).longValue())
                .reversed();
        commandList.sort(byCalls);
        if (commandList.size() <= MonitorConstant.COMMAND_TOP_LIMIT) {
            return commandList;
        }
        return new ArrayList<>(
                commandList.subList(0, MonitorConstant.COMMAND_TOP_LIMIT));
    }

    /**
     * 解析各db键空间
     * @param keyspace 键空间属性
     * @return 键空间列表
     */
    private List<Map<String, Object>> buildKeyspaceList(Properties keyspace) {
        List<Map<String, Object>> keyspaceList = new ArrayList<>();
        if (keyspace == null) {
            return keyspaceList;
        }
        for (String db : keyspace.stringPropertyNames()) {
            String value = keyspace.getProperty(db);
            long keys = extractLong(value, "keys");
            long expires = extractLong(value, "expires");
            long avgTtl = extractLong(value, "avg_ttl");
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("db", db);
            item.put("keys", keys);
            item.put("expires", expires);
            item.put("avgTtl", avgTtl);
            keyspaceList.add(item);
        }
        return keyspaceList;
    }

    /**
     * 从形如calls=1,usec=2的文本提取整数值
     * @param text 文本
     * @param field 字段名
     * @return 整数值
     */
    private long extractLong(String text, String field) {
        if (text == null) {
            return 0L;
        }
        String token = field + "=";
        int start = text.indexOf(token);
        if (start < 0) {
            return 0L;
        }
        start += token.length();
        int end = text.indexOf(',', start);
        String number = end > start ? text.substring(start, end) : text.substring(start);
        try {
            return Long.parseLong(number.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 解析整型属性
     * @param props 属性集
     * @param key 键
     * @return 值
     */
    private int parseInt(Properties props, String key) {
        return (int) parseLong(props, key);
    }

    /**
     * 解析长整属性
     * @param props 属性集
     * @param key 键
     * @return 值
     */
    private long parseLong(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 解析小数属性
     * @param props 属性集
     * @param key 键
     * @return 值
     */
    private BigDecimal parseDecimal(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}

package com.spark.monitor.collect;

import com.spark.common.bean.monitor.entity.MonitorApiSnapshot;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * 接口请求指标内存累计器
 */
@Component
public class ApiMetricsCollector {

    private final Map<ApiKey, Accumulator> accumulatorMap = new ConcurrentHashMap<>();

    /**
     * 累计一次请求
     * @param uri URI模板
     * @param method 请求方法
     * @param status HTTP状态码
     * @param costMs 耗时毫秒
     */
    public void record(String uri, String method, int status, int costMs) {
        if (uri == null || uri.isEmpty() || method == null) {
            return;
        }
        ApiKey apiKey = new ApiKey(uri, method);
        Accumulator accumulator = accumulatorMap.computeIfAbsent(apiKey, key -> new Accumulator());
        accumulator.add(status, costMs);
    }

    /**
     * 输出当前分钟快照并清空累计
     * @param nodeKey 节点标识
     * @return 快照列表
     */
    public List<MonitorApiSnapshot> flush(String nodeKey) {
        Timestamp snapshotDt = Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        List<MonitorApiSnapshot> snapshotList = new ArrayList<>();
        for (Map.Entry<ApiKey, Accumulator> entry : accumulatorMap.entrySet()) {
            ApiKey apiKey = entry.getKey();
            Accumulator accumulator = entry.getValue();
            if (accumulator.total == 0) {
                continue;
            }
            MonitorApiSnapshot snapshot = new MonitorApiSnapshot();
            snapshot.setNodeKey(nodeKey);
            snapshot.setSnapshotDt(snapshotDt);
            snapshot.setUri(apiKey.uri());
            snapshot.setMethod(apiKey.method());
            snapshot.setTotalCount(accumulator.total);
            snapshot.setCount2xx(accumulator.count2xx);
            snapshot.setCount3xx(accumulator.count3xx);
            snapshot.setCount4xx(accumulator.count4xx);
            snapshot.setCount5xx(accumulator.count5xx);
            snapshot.setTotalCost(accumulator.totalCost);
            snapshot.setMaxCost(accumulator.maxCost);
            snapshotList.add(snapshot);
        }
        accumulatorMap.clear();
        return snapshotList;
    }

    /**
     * 接口维度键
     * @param uri URI模板
     * @param method 请求方法
     */
    private record ApiKey(String uri, String method) {
    }

    /**
     * 单接口累计值
     */
    private static class Accumulator {
        private int total;
        private int count2xx;
        private int count3xx;
        private int count4xx;
        private int count5xx;
        private long totalCost;
        private int maxCost;

        /**
         * 累加一次请求
         * @param status HTTP状态码
         * @param costMs 耗时毫秒
         */
        private synchronized void add(int status, int costMs) {
            total++;
            switch (status / 100) {
                case 2 -> count2xx++;
                case 3 -> count3xx++;
                case 4 -> count4xx++;
                case 5 -> count5xx++;
                default -> count4xx++;
            }
            totalCost += costMs;
            if (costMs > maxCost) {
                maxCost = costMs;
            }
        }
    }
}

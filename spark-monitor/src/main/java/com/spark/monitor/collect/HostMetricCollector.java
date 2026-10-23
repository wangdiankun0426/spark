package com.spark.monitor.collect;

import com.spark.common.bean.monitor.entity.MonitorHostSnapshot;
import com.spark.common.utils.JsonUtil;
import jakarta.annotation.PostConstruct;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.VirtualMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * 主机与JVM指标采集器
 */
@Component
public class HostMetricCollector {

    private final static Logger logger = LoggerFactory.getLogger(HostMetricCollector.class);

    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
    private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    private final List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

    private SystemInfo systemInfo;
    private CentralProcessor processor;
    private HardwareAbstractionLayer hardware;
    private OperatingSystem operatingSystem;

    /**
     * CPU使用率
     */
    private volatile double cpuUsage;

    /**
     * 网卡下行速率
     */
    private volatile long netRxRate;

    /**
     * 网卡上行速率
     */
    private volatile long netTxRate;

    private long[] prevTicks;
    private long prevSampleMs;
    private long prevNetRxBytes;
    private long prevNetTxBytes;

    private String hostInfoJson;

    /**
     * 初始化oshi硬件句柄
     */
    @PostConstruct
    public void init() {
        systemInfo = new SystemInfo();
        hardware = systemInfo.getHardware();
        processor = hardware.getProcessor();
        operatingSystem = systemInfo.getOperatingSystem();
        prevTicks = processor.getSystemCpuLoadTicks();
        long[] netBytes = sumNetBytes();
        prevNetRxBytes = netBytes[0];
        prevNetTxBytes = netBytes[1];
        prevSampleMs = System.currentTimeMillis();
        hostInfoJson = buildHostInfoJson();
        sampleDeltaMetrics();
    }

    /**
     * 5秒差值采样CPU使用率与网卡速率
     */
    @Scheduled(fixedDelay = 5000L, initialDelay = 5000L)
    public void sampleDeltaMetrics() {
        long now = System.currentTimeMillis();
        long[] ticks = processor.getSystemCpuLoadTicks();
        double load = processor.getSystemCpuLoadBetweenTicks(prevTicks);
        prevTicks = ticks;
        // 采样间隔不足时 oshi 返回 -1，按 0 处理避免写入负使用率
        double usage = load < 0 ? 0 : round(load * 100, 2);
        cpuUsage = usage;

        long[] netBytes = sumNetBytes();
        long elapsed = now - prevSampleMs;
        if (elapsed > 0) {
            netRxRate = (netBytes[0] - prevNetRxBytes) * 1000 / elapsed;
            netTxRate = (netBytes[1] - prevNetTxBytes) * 1000 / elapsed;
        }
        prevNetRxBytes = netBytes[0];
        prevNetTxBytes = netBytes[1];
        prevSampleMs = now;
    }

    /**
     * 组装一条主机与JVM快照
     * @param nodeKey 节点标识
     * @return 快照
     */
    public MonitorHostSnapshot buildSnapshot(String nodeKey) {
        MonitorHostSnapshot snapshot = new MonitorHostSnapshot();
        Timestamp snapshotDt = new Timestamp(System.currentTimeMillis());
        snapshot.setNodeKey(nodeKey);
        snapshot.setSnapshotDt(snapshotDt);
        snapshot.setCpuUsage(BigDecimal.valueOf(cpuUsage));
        fillMemory(snapshot);
        snapshot.setNetRxRate(Math.max(netRxRate, 0));
        snapshot.setNetTxRate(Math.max(netTxRate, 0));
        List<Map<String, Object>> diskList = buildDiskList();
        snapshot.setDiskJson(JsonUtil.toString(diskList));
        snapshot.setHostInfoJson(hostInfoJson);
        fillJvm(snapshot);
        return snapshot;
    }

    /**
     * 填充物理内存与Swap
     * @param snapshot 快照
     */
    private void fillMemory(MonitorHostSnapshot snapshot) {
        GlobalMemory memory = hardware.getMemory();
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = Math.max(total - available, 0);
        BigDecimal memUsage = percent(used, total);
        snapshot.setMemTotal(total);
        snapshot.setMemUsed(used);
        snapshot.setMemUsage(memUsage);

        VirtualMemory virtualMemory = memory.getVirtualMemory();
        long swapTotal = virtualMemory.getSwapTotal();
        long swapUsed = virtualMemory.getSwapUsed();
        BigDecimal swapUsage = percent(swapUsed, swapTotal);
        snapshot.setSwapTotal(swapTotal);
        snapshot.setSwapUsed(swapUsed);
        snapshot.setSwapUsage(swapUsage);
    }

    /**
     * 填充JVM指标
     * @param snapshot 快照
     */
    private void fillJvm(MonitorHostSnapshot snapshot) {
        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
        long heapUsed = heap.getUsed();
        long heapMax = heap.getMax() > 0 ? heap.getMax() : heap.getCommitted();
        BigDecimal heapUsage = percent(heapUsed, heapMax);
        snapshot.setHeapUsed(heapUsed);
        snapshot.setHeapMax(heapMax);
        snapshot.setHeapUsage(heapUsage);
        long nonheapUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed();
        snapshot.setNonheapUsed(nonheapUsed);

        long youngCount = 0L;
        long youngCost = 0L;
        long oldCount = 0L;
        long oldCost = 0L;
        for (GarbageCollectorMXBean gcBean : gcMXBeans) {
            String name = gcBean.getName() == null ? "" : gcBean.getName().toLowerCase();
            long count = gcBean.getCollectionCount() > 0 ? gcBean.getCollectionCount() : 0L;
            long cost = gcBean.getCollectionTime() > 0 ? gcBean.getCollectionTime() : 0L;
            if (name.contains("old") || name.contains("major") || name.contains("zgc")) {
                oldCount += count;
                oldCost += cost;
            } else {
                youngCount += count;
                youngCost += cost;
            }
        }
        snapshot.setYoungGcCount(youngCount);
        snapshot.setYoungGcCost(youngCost);
        snapshot.setOldGcCount(oldCount);
        snapshot.setOldGcCost(oldCost);

        int blocked = 0;
        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds());
        if (threadInfos != null) {
            for (ThreadInfo threadInfo : threadInfos) {
                if (threadInfo != null && Thread.State.BLOCKED.equals(threadInfo.getThreadState())) {
                    blocked++;
                }
            }
        }
        long[] deadlockedIds = threadMXBean.findDeadlockedThreads();
        snapshot.setThreadCount(threadMXBean.getThreadCount());
        snapshot.setBlockedCount(blocked);
        snapshot.setDeadlockedCount(deadlockedIds == null ? 0 : deadlockedIds.length);
        snapshot.setLoadedClassCount(classLoadingMXBean.getLoadedClassCount());
    }

    /**
     * 构造磁盘分区明细
     * @return 分区列表
     */
    private List<Map<String, Object>> buildDiskList() {
        List<Map<String, Object>> diskList = new ArrayList<>();
        FileSystem fileSystem = operatingSystem.getFileSystem();
        List<OSFileStore> fileStores = fileSystem.getFileStores();
        for (OSFileStore store : fileStores) {
            long total = store.getTotalSpace();
            if (total <= 0) {
                continue;
            }
            long usable = store.getUsableSpace();
            long used = total - usable;
            BigDecimal usage = percent(used, total);
            Map<String, Object> disk = new LinkedHashMap<>();
            disk.put("mount", store.getMount());
            disk.put("name", store.getName());
            disk.put("type", store.getType());
            disk.put("total", total);
            disk.put("used", used);
            disk.put("free", usable);
            disk.put("usage", usage);
            diskList.add(disk);
        }
        return diskList;
    }

    /**
     * 构造主机与JVM静态信息
     * @return 静态信息JSON
     */
    private String buildHostInfoJson() {
        String hostName;
        String hostAddress;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            hostName = localHost.getHostName();
            hostAddress = localHost.getHostAddress();
        } catch (Exception e) {
            hostName = operatingSystem.getNetworkParams().getHostName();
            hostAddress = "";
            logger.warn("resolve local inetAddress error, errMsg={}", e.getMessage());
        }
        String cpuName = processor.getProcessorIdentifier().getName();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("hostName", hostName);
        info.put("os", operatingSystem.toString());
        info.put("arch", System.getProperty("os.arch"));
        info.put("cpuName", cpuName);
        info.put("cpuCores", processor.getLogicalProcessorCount());
        info.put("ip", hostAddress);
        info.put("jvmVersion", System.getProperty("java.version"));
        info.put("jvmName", runtimeMXBean.getVmName());
        info.put("jvmVendor", runtimeMXBean.getVmVendor());
        info.put("jvmStartTime", runtimeMXBean.getStartTime());
        return JsonUtil.toString(info);
    }

    /**
     * 汇总物理网卡收发包字节
     * @return 下标0接收，1发送
     */
    private long[] sumNetBytes() {
        long rx = 0L;
        long tx = 0L;
        List<NetworkIF> networkIFs = hardware.getNetworkIFs();
        for (NetworkIF networkIF : networkIFs) {
            if (networkIF.getSpeed() <= 0) {
                continue;
            }
            rx += networkIF.getBytesRecv();
            tx += networkIF.getBytesSent();
        }
        return new long[]{rx, tx};
    }

    /**
     * 计算百分比
     * @param part 部分值
     * @param total 总值
     * @return 百分比
     */
    private BigDecimal percent(long part, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO;
        }
        double value = (double) part * 100 / total;
        double rounded = round(value, 2);
        return BigDecimal.valueOf(rounded);
    }

    /**
     * 保留两位小数
     * @param value 原始值
     * @param scale 位数
     * @return 结果
     */
    private double round(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }
}

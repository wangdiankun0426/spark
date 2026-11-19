package com.spark.task;

import com.spark.monitor.collect.HostMetricCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 15:00:00
 * 主机指标定时采样任务
 */
@Service
public class HostMetricTask {
    @Autowired
    private HostMetricCollector hostMetricCollector;

    /**
     * 每5秒执行一次
     * 主机CPU与网卡差值采样
     */
    @Scheduled(fixedDelay = 5000L, initialDelay = 5000L)
    public void executeSample() {
        hostMetricCollector.sampleDeltaMetrics();
    }
}

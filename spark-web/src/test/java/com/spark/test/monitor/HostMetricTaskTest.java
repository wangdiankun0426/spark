package com.spark.test.monitor;

import com.spark.monitor.collect.HostMetricCollector;
import com.spark.task.HostMetricTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 15:00:00
 * 主机指标定时采样任务测试
 */
public class HostMetricTaskTest {
    private HostMetricTask hostMetricTask;
    private HostMetricCollector hostMetricCollector;

    @BeforeEach
    void setUp() {
        hostMetricTask = new HostMetricTask();
        hostMetricCollector = mock(HostMetricCollector.class);
        ReflectionTestUtils.setField(hostMetricTask, "hostMetricCollector", hostMetricCollector);
    }

    /**
     * 验证定时触发时委托采集器执行差值采样，确保调度迁移后CPU与网卡速率持续更新
     */
    @Test
    void shouldDelegateSampleToCollector() {
        hostMetricTask.executeSample();
        verify(hostMetricCollector).sampleDeltaMetrics();
    }
}

package com.spark.test.llm;

import com.spark.llm.usage.ModelUsageRecorder;
import com.spark.task.ModelUsageTask;
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
 * 模型用量定时落库任务测试
 */
public class ModelUsageTaskTest {
    private ModelUsageTask modelUsageTask;
    private ModelUsageRecorder modelUsageRecorder;

    @BeforeEach
    void setUp() {
        modelUsageTask = new ModelUsageTask();
        modelUsageRecorder = mock(ModelUsageRecorder.class);
        ReflectionTestUtils.setField(modelUsageTask, "modelUsageRecorder", modelUsageRecorder);
    }

    /**
     * 验证定时触发时委托采集器执行队列批量落库，确保调度迁移后用量落库链路不断
     */
    @Test
    void shouldDelegateFlushToRecorder() {
        modelUsageTask.executeFlush();
        verify(modelUsageRecorder).flush();
    }
}

package com.spark.task;

import com.spark.llm.usage.ModelUsageRecorder;
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
 * 模型用量定时落库任务
 */
@Service
public class ModelUsageTask {
    @Autowired
    private ModelUsageRecorder modelUsageRecorder;

    /**
     * 每10秒执行一次
     * 用量队列批量落库
     */
    @Scheduled(fixedDelay = 10000)
    public void executeFlush() {
        modelUsageRecorder.flush();
    }
}

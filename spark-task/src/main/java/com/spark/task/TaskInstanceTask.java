package com.spark.task;

import com.spark.bean.base.ResultData;
import com.spark.task.service.ITaskInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @since 2026-08-17 16:30:00
 * 通用定时任务调度器（每分钟扫描到期任务并按类型分发）
 */
@Service
public class TaskInstanceTask {
    private final static Logger logger = LoggerFactory.getLogger(TaskInstanceTask.class);
    @Autowired
    private ITaskInstanceService taskInstanceService;

    /**
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void executeTask() {
        logger.info("scheduled executeTask start");
        ResultData<Void> result = taskInstanceService.executeTask();
        logger.info("scheduled executeTask end, code={}", result.getCode());
    }
}

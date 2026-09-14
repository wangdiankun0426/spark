package com.spark.task;

import com.spark.common.bean.base.ResultData;
import com.spark.task.service.IDocumentTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/12/7 下午11:27
 */
@Service
public class DocumentTask {
    private final static Logger logger  = LoggerFactory.getLogger(DocumentTask.class);
    @Autowired
    private IDocumentTaskService documentTaskService;

    /**
     * 每分钟执行一次
     * 文档内容任务
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void executeContentTask() {
        documentTaskService.executeContentTask();
    }

    /**
     * 每分钟执行一次
     * 文档索引任务
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void executeIndexTask() {
        documentTaskService.executeIndexTask();
    }

    /**
     * 每分钟执行一次
     * 文档分块任务
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void executeChunkTask() {
        documentTaskService.executeChunkTask();
    }

    /**
     * 每分钟执行一次
     * 文档向量化任务
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void executeVectorTask() {
        documentTaskService.executeVectorTask();
    }

    /**
     * 每分钟执行一次
     * 知识图谱构建任务
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void executeGraphTask() {
        documentTaskService.executeGraphTask();
    }
}

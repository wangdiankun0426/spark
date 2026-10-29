package com.spark.task;

import com.spark.common.bean.base.ResultData;
import com.spark.common.utils.FileUtil;
import com.spark.task.service.IDocumentTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
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
    @Value("${docs.file.path}")
    private String docsPath;
    @Autowired
    private FileUtil fileUtil;

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

    /**
     * 每天凌晨2点执行一次
     * 清理超过24小时未完成的分片上传临时目录
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void executeCleanTask() {
        logger.info("scheduled executeCleanTask start");
        File chunkRootDir = new File(docsPath + File.separator + "chunk");
        File[] chunkDirs = chunkRootDir.listFiles(File::isDirectory);
        if (chunkDirs == null) {
            logger.info("scheduled executeCleanTask end, chunk dir not exist");
            return;
        }
        long expireTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000L;
        int cleanCount = 0;
        for (File chunkDir : chunkDirs) {
            if (chunkDir.lastModified() < expireTime) {
                fileUtil.deleteDir(chunkDir);
                cleanCount++;
            }
        }
        logger.info("scheduled executeCleanTask end, cleanCount={}", cleanCount);
    }
}

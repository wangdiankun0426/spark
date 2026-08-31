package com.spark.task;

import com.spark.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-31 15:30:00
 * 分片上传临时文件清理任务
 */
@Service
public class ChunkCleanTask {
    private final static Logger logger = LoggerFactory.getLogger(ChunkCleanTask.class);
    @Value("${docs.file.path}")
    private String docsPath;

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
                FileUtil.deleteDir(chunkDir);
                cleanCount++;
            }
        }
        logger.info("scheduled executeCleanTask end, cleanCount={}", cleanCount);
    }
}

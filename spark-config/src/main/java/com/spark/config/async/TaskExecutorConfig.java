package com.spark.config.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/10/13 15:59
 */
@Configuration
public class TaskExecutorConfig {
    private static final Logger logger = LoggerFactory.getLogger(TaskExecutorConfig.class);

    /**
     * 创建异步任务线程池
     * @return 线程池
     */
    @Bean("asyncTaskExecutor")
    public Executor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("asyncTask-");
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler((r, executor1) -> {
            logger.warn("Task {} rejected, executor: {}", r.toString(), executor1.toString());
        });
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setTaskDecorator(new TraceIdTaskDecorator());
        executor.initialize();
        return executor;
    }
}

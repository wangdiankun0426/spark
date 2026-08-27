package com.spark.workflow.engine;

import com.spark.bean.workflow.config.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-26 10:00:00
 * 重试策略：指数退避，参考 LangGraph RetryPolicy
 */
@Component
public class RetryStrategy {
    private static final Logger logger = LoggerFactory.getLogger(RetryStrategy.class);

    /**
     * 使用默认配置执行带重试的操作
     * @param action 待执行操作
     * @param <T> 返回类型
     * @return 执行结果
     */
    public <T> T executeWithRetry(Callable<T> action) {
        return executeWithRetry(action, new RetryConfig());
    }

    /**
     * 执行带重试的操作
     * maxRetries=0 表示不重试，直接失败
     * @param action 待执行操作
     * @param config 重试配置
     * @param <T> 返回类型
     * @return 执行结果
     */
    public <T> T executeWithRetry(Callable<T> action, RetryConfig config) {
        long interval = config.getInitialInterval();
        // maxRetries 为总尝试次数上限（含首次执行），首次失败后最多再尝试 maxRetries-1 次
        for (int attempt = 1; ; attempt++) {
            try {
                return action.call();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("执行被中断", e);
            } catch (Exception e) {
                if (attempt >= config.getMaxRetries()) {
                    logger.error("Max retries ({}) reached, giving up", config.getMaxRetries(), e);
                    throw new RuntimeException("执行失败，已尝试" + attempt + "次: " + e.getMessage(), e);
                }
                logger.warn("Retry attempt {}/{}, wait {}ms, error={}", attempt, config.getMaxRetries(), interval, e.getMessage());
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试等待被中断", ie);
                }
                // 指数退避，上限为 maxInterval
                interval = Math.min((long) (interval * config.getMultiplier()), config.getMaxInterval());
            }
        }
    }
}

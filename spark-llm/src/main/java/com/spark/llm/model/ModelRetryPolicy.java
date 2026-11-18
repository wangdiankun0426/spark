package com.spark.llm.model;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 09:00:00
 * 模型重试策略，统一额外次数与指数退避抖动计算
 */
public class ModelRetryPolicy {
    public static final int DEFAULT_MAX_RETRIES = 3;
    public static final int MIN_MAX_RETRIES = 0;
    public static final int MAX_MAX_RETRIES = 5;
    private static final long BASE_DELAY_MS = 500L;
    private static final long MAX_DELAY_MS = 5000L;
    private final DoubleSupplier jitterSupplier;

    public ModelRetryPolicy() {
        this(() -> ThreadLocalRandom.current().nextDouble());
    }

    public ModelRetryPolicy(DoubleSupplier jitterSupplier) {
        if (jitterSupplier == null) {
            throw new IllegalArgumentException("jitterSupplier must not be null");
        }
        this.jitterSupplier = jitterSupplier;
    }

    /**
     * 解析额外重试次数
     * @param maxRetries 配置次数
     * @return 有效次数
     */
    public static int resolveMaxRetries(Integer maxRetries) {
        if (maxRetries == null) {
            return DEFAULT_MAX_RETRIES;
        }
        if (maxRetries < MIN_MAX_RETRIES || maxRetries > MAX_MAX_RETRIES) {
            throw new IllegalArgumentException("maxRetries is out of range");
        }
        return maxRetries;
    }

    /**
     * 计算本次重试的实际等待时间
     * @param retryNumber 重试序号
     * @return 等待毫秒
     */
    public long nextDelayMs(int retryNumber) {
        double jitter = jitterSupplier.getAsDouble();
        return calculateDelayMs(retryNumber, jitter);
    }

    /**
     * 按固定抖动值计算等待时间
     * @param retryNumber 重试序号
     * @param jitter 抖动比例
     * @return 等待毫秒
     */
    public static long calculateDelayMs(int retryNumber, double jitter) {
        if (retryNumber < 1 || retryNumber > MAX_MAX_RETRIES) {
            throw new IllegalArgumentException("retryNumber is out of range");
        }
        if (jitter < 0 || jitter >= 1) {
            throw new IllegalArgumentException("jitter is out of range");
        }
        long exponentialDelay = BASE_DELAY_MS << (retryNumber - 1);
        long cappedDelay = Math.min(exponentialDelay, MAX_DELAY_MS);
        return (long) (cappedDelay * jitter);
    }
}

package com.spark.llm.model.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 16:00:00
 * 单个租户的限流状态机，固定分钟窗口计数请求与token，并发许可随调用释放
 */
public class TenantRateLimitState {
    private static final Logger logger = LoggerFactory.getLogger(TenantRateLimitState.class);
    /**
     * 限流统计窗口毫秒数
     */
    private static final long WINDOW_DURATION_MS = 60000L;
    private final Long tenantId;
    private long windowStartMs;
    private int requestCount;
    private long tokenCount;
    private int activeCount;

    public TenantRateLimitState(Long tenantId, long now) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        this.tenantId = tenantId;
        this.windowStartMs = now;
    }

    /**
     * 尝试获取调用许可，依次检查并发、每分钟请求数与每分钟token数
     * @param now 当前毫秒时间戳
     * @param maxConcurrent 最大并发，0不限
     * @param rpm 每分钟请求数，0不限
     * @param tpm 每分钟token数，0不限
     * @return 是否允许调用
     */
    public synchronized boolean tryAcquire(long now, int maxConcurrent, int rpm, int tpm) {
        this.rollWindow(now);
        if (maxConcurrent > 0 && activeCount >= maxConcurrent) {
            logger.warn("tenant rate limit rejected, tenantId={}, limitType=concurrent, limit={}", tenantId, maxConcurrent);
            return false;
        }
        if (rpm > 0 && requestCount >= rpm) {
            logger.warn("tenant rate limit rejected, tenantId={}, limitType=rpm, limit={}", tenantId, rpm);
            return false;
        }
        if (tpm > 0 && tokenCount >= tpm) {
            logger.warn("tenant rate limit rejected, tenantId={}, limitType=tpm, limit={}", tenantId, tpm);
            return false;
        }
        activeCount++;
        requestCount++;
        return true;
    }

    /**
     * 释放一个并发许可
     */
    public synchronized void release() {
        if (activeCount > 0) {
            activeCount--;
        }
    }

    /**
     * 累计本次调用消耗的token数
     * @param now 当前毫秒时间戳
     * @param tokens 消耗token数
     */
    public synchronized void recordTokens(long now, int tokens) {
        this.rollWindow(now);
        tokenCount += tokens;
    }

    /**
     * 窗口到期时清零请求与token计数
     * @param now 当前毫秒时间戳
     */
    private void rollWindow(long now) {
        if (now - windowStartMs >= WINDOW_DURATION_MS) {
            windowStartMs = now;
            requestCount = 0;
            tokenCount = 0;
        }
    }
}
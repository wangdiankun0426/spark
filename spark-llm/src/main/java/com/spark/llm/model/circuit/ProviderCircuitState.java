package com.spark.llm.model.circuit;

import com.spark.common.enums.CircuitStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 11:00:00
 * 单个供应商的熔断状态机，闭合累计失败打开，到期半开试探，成功恢复闭合
 */
public class ProviderCircuitState {
    private static final Logger logger = LoggerFactory.getLogger(ProviderCircuitState.class);
    private final Long providerId;
    private final int failureThreshold;
    private final long openDurationMs;
    private CircuitStateEnum state = CircuitStateEnum.CLOSED;
    private int consecutiveFailures;
    private long openedAt;

    public ProviderCircuitState(Long providerId, int failureThreshold, long openDurationMs) {
        if (providerId == null || failureThreshold < 1 || openDurationMs < 1) {
            throw new IllegalArgumentException("circuit state params are invalid");
        }
        this.providerId = providerId;
        this.failureThreshold = failureThreshold;
        this.openDurationMs = openDurationMs;
    }

    /**
     * 尝试获取调用许可，半开期仅放行唯一试探请求
     * @param now 当前毫秒时间戳
     * @return 是否允许调用
     */
    public synchronized boolean tryAcquire(long now) {
        if (state == CircuitStateEnum.CLOSED) {
            return true;
        }
        if (state == CircuitStateEnum.HALF_OPEN) {
            return false;
        }
        if (now - openedAt < openDurationMs) {
            return false;
        }
        state = CircuitStateEnum.HALF_OPEN;
        logger.info("provider circuit halfOpen, providerId={}", providerId);
        return true;
    }

    /**
     * 记录成功，恢复闭合状态并清零连续失败
     */
    public synchronized void recordSuccess() {
        if (state != CircuitStateEnum.CLOSED) {
            logger.info("provider circuit closed, providerId={}", providerId);
        }
        state = CircuitStateEnum.CLOSED;
        consecutiveFailures = 0;
    }

    /**
     * 记录可重试最终失败，达到阈值打开熔断，半开试探失败重新打开
     * @param now 当前毫秒时间戳
     */
    public synchronized void recordFailure(long now) {
        if (state == CircuitStateEnum.HALF_OPEN) {
            state = CircuitStateEnum.OPEN;
            openedAt = now;
            logger.warn("provider circuit reopened, providerId={}", providerId);
            return;
        }
        if (state == CircuitStateEnum.OPEN) {
            return;
        }
        consecutiveFailures++;
        if (consecutiveFailures >= failureThreshold) {
            state = CircuitStateEnum.OPEN;
            openedAt = now;
            logger.warn("provider circuit opened, providerId={}, consecutiveFailures={}", providerId, consecutiveFailures);
        }
    }

    /**
     * 获取当前熔断状态
     * @return 熔断状态枚举
     */
    public synchronized CircuitStateEnum getState() {
        return state;
    }
}
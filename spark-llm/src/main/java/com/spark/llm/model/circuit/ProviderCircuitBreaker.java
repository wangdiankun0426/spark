package com.spark.llm.model.circuit;

import com.spark.common.enums.CircuitStateEnum;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 11:00:00
 * 供应商级熔断器，连续可重试失败达到阈值打开，熔断期间拒绝调用，到期半开试探
 */
@Component
public class ProviderCircuitBreaker {
    /**
     * 触发熔断的连续失败次数
     */
    private static final int FAILURE_THRESHOLD = 5;
    /**
     * 熔断打开后进入半开的等待毫秒数
     */
    private static final long OPEN_DURATION_MS = 30000L;
    /**
     * 各供应商的熔断状态，key为供应商id
     */
    private final Map<Long, ProviderCircuitState> states = new ConcurrentHashMap<>();
    /**
     * 毫秒时钟，测试可注入假时钟
     */
    private LongSupplier clock = System::currentTimeMillis;

    /**
     * 尝试获取供应商调用许可
     * @param providerId 供应商id
     * @return 是否允许调用
     */
    public boolean tryAcquire(Long providerId) {
        if (providerId == null) {
            throw new IllegalArgumentException("providerId must not be null");
        }
        ProviderCircuitState state = this.resolveState(providerId);
        long now = clock.getAsLong();
        return state.tryAcquire(now);
    }

    /**
     * 记录供应商调用成功
     * @param providerId 供应商id
     */
    public void recordSuccess(Long providerId) {
        if (providerId == null) {
            return;
        }
        ProviderCircuitState state = states.get(providerId);
        if (state == null) {
            return;
        }
        state.recordSuccess();
    }

    /**
     * 记录供应商可重试最终失败
     * @param providerId 供应商id
     */
    public void recordFailure(Long providerId) {
        if (providerId == null) {
            return;
        }
        ProviderCircuitState state = this.resolveState(providerId);
        long now = clock.getAsLong();
        state.recordFailure(now);
    }

    /**
     * 查询供应商当前熔断状态
     * @param providerId 供应商id
     * @return 熔断状态枚举
     */
    public CircuitStateEnum getState(Long providerId) {
        if (providerId == null) {
            throw new IllegalArgumentException("providerId must not be null");
        }
        ProviderCircuitState state = states.get(providerId);
        if (state == null) {
            return CircuitStateEnum.CLOSED;
        }
        return state.getState();
    }

    /**
     * 重置供应商熔断状态，供应商配置变更或删除时调用
     * @param providerId 供应商id
     */
    public void reset(Long providerId) {
        if (providerId == null) {
            return;
        }
        states.remove(providerId);
    }

    /**
     * 获取或创建供应商熔断状态
     * @param providerId 供应商id
     * @return 熔断状态机
     */
    private ProviderCircuitState resolveState(Long providerId) {
        return states.computeIfAbsent(providerId, id -> new ProviderCircuitState(id, FAILURE_THRESHOLD, OPEN_DURATION_MS));
    }
}
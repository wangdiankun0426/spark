package com.spark.test.llm;

import com.spark.llm.model.error.ModelRetryPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 09:00:00
 * 模型重试策略测试
 */
public class ModelRetryPolicyTest {

    /**
     * 验证重试次数未配置时默认3，0到5合法，负数或大于5时直接拒绝
     */
    @Test
    void shouldResolveMaxRetries() {
        assertEquals(3, ModelRetryPolicy.resolveMaxRetries(null));
        assertEquals(0, ModelRetryPolicy.resolveMaxRetries(0));
        assertEquals(5, ModelRetryPolicy.resolveMaxRetries(5));
        assertThrows(IllegalArgumentException.class, () -> ModelRetryPolicy.resolveMaxRetries(-1));
        assertThrows(IllegalArgumentException.class, () -> ModelRetryPolicy.resolveMaxRetries(6));
    }

    /**
     * 验证第1到5次重试按500毫秒基数指数增长并受5000毫秒上限约束，固定抖动下结果可复现
     */
    @Test
    void shouldCalculateExponentialDelayWithJitter() {
        assertEquals(250L, ModelRetryPolicy.calculateDelayMs(1, 0.5));
        assertEquals(500L, ModelRetryPolicy.calculateDelayMs(2, 0.5));
        assertEquals(1000L, ModelRetryPolicy.calculateDelayMs(3, 0.5));
        assertEquals(2000L, ModelRetryPolicy.calculateDelayMs(4, 0.5));
        assertEquals(2500L, ModelRetryPolicy.calculateDelayMs(5, 0.5));
        assertEquals(0L, ModelRetryPolicy.calculateDelayMs(1, 0));
    }

    /**
     * 验证供应商等待时间大于本地退避时采用供应商值，较小时保留本地退避且不截断较长等待
     */
    @Test
    void shouldRespectRetryAfterWithoutWeakeningBackoff() {
        ModelRetryPolicy retryPolicy = new ModelRetryPolicy(() -> 0.5);
        assertEquals(2000L, retryPolicy.nextDelayMs(1, 2000L));
        assertEquals(250L, retryPolicy.nextDelayMs(1, 100L));
        assertEquals(250L, retryPolicy.nextDelayMs(1, null));
        assertEquals(10000L, retryPolicy.nextDelayMs(5, 10000L));
    }
}

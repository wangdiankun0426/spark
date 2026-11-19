package com.spark.test.llm;

import com.spark.common.enums.CircuitStateEnum;
import com.spark.llm.model.circuit.ProviderCircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 14:00:00
 * 供应商级熔断器状态机测试
 */
public class ProviderCircuitBreakerTest {
    private ProviderCircuitBreaker circuitBreaker;
    private AtomicLong nowMillis;

    @BeforeEach
    void setUp() {
        circuitBreaker = new ProviderCircuitBreaker();
        nowMillis = new AtomicLong(0L);
        LongSupplier clock = nowMillis::get;
        ReflectionTestUtils.setField(circuitBreaker, "clock", clock);
    }

    /**
     * 验证连续失败4次未达阈值时熔断保持闭合，调用许可正常放行
     */
    @Test
    void shouldStayClosedBeforeThreshold() {
        for (int i = 0; i < 4; i++) {
            circuitBreaker.recordFailure(11L);
        }
        assertEquals(CircuitStateEnum.CLOSED, circuitBreaker.getState(11L));
        assertTrue(circuitBreaker.tryAcquire(11L));
    }

    /**
     * 验证第5次连续失败打开熔断，打开后30秒窗口内所有调用许可被拒绝
     */
    @Test
    void shouldOpenAtThresholdAndRejectWithinWindow() {
        for (int i = 0; i < 5; i++) {
            circuitBreaker.recordFailure(11L);
        }
        assertEquals(CircuitStateEnum.OPEN, circuitBreaker.getState(11L));
        assertFalse(circuitBreaker.tryAcquire(11L));
        nowMillis.addAndGet(29999L);
        assertFalse(circuitBreaker.tryAcquire(11L));
        assertEquals(CircuitStateEnum.OPEN, circuitBreaker.getState(11L));
    }

    /**
     * 验证打开满30秒后仅放行唯一半开试探请求，试探在途时其余请求仍被拒绝
     */
    @Test
    void shouldAllowSingleProbeAfterWindow() {
        for (int i = 0; i < 5; i++) {
            circuitBreaker.recordFailure(11L);
        }
        nowMillis.addAndGet(30000L);
        assertTrue(circuitBreaker.tryAcquire(11L));
        assertEquals(CircuitStateEnum.HALF_OPEN, circuitBreaker.getState(11L));
        assertFalse(circuitBreaker.tryAcquire(11L));
    }

    /**
     * 验证半开试探成功后恢复闭合并清零计数，随后再连续失败4次仍不触发熔断
     */
    @Test
    void shouldCloseOnProbeSuccess() {
        for (int i = 0; i < 5; i++) {
            circuitBreaker.recordFailure(11L);
        }
        nowMillis.addAndGet(30000L);
        assertTrue(circuitBreaker.tryAcquire(11L));
        circuitBreaker.recordSuccess(11L);
        assertEquals(CircuitStateEnum.CLOSED, circuitBreaker.getState(11L));
        for (int i = 0; i < 4; i++) {
            circuitBreaker.recordFailure(11L);
        }
        assertEquals(CircuitStateEnum.CLOSED, circuitBreaker.getState(11L));
        assertTrue(circuitBreaker.tryAcquire(11L));
    }

    /**
     * 验证半开试探失败后重新打开并刷新30秒窗口，新窗口内拒绝、新窗口到期后再次放行试探
     */
    @Test
    void shouldReopenOnProbeFailure() {
        for (int i = 0; i < 5; i++) {
            circuitBreaker.recordFailure(11L);
        }
        nowMillis.addAndGet(30000L);
        assertTrue(circuitBreaker.tryAcquire(11L));
        circuitBreaker.recordFailure(11L);
        assertEquals(CircuitStateEnum.OPEN, circuitBreaker.getState(11L));
        nowMillis.addAndGet(29999L);
        assertFalse(circuitBreaker.tryAcquire(11L));
        nowMillis.addAndGet(1L);
        assertTrue(circuitBreaker.tryAcquire(11L));
        assertEquals(CircuitStateEnum.HALF_OPEN, circuitBreaker.getState(11L));
    }

    /**
     * 验证闭合期计数中途成功即清零，清零后需重新累计满5次失败才打开熔断
     */
    @Test
    void shouldResetCountOnMidwaySuccess() {
        for (int i = 0; i < 4; i++) {
            circuitBreaker.recordFailure(11L);
        }
        circuitBreaker.recordSuccess(11L);
        for (int i = 0; i < 4; i++) {
            circuitBreaker.recordFailure(11L);
        }
        assertEquals(CircuitStateEnum.CLOSED, circuitBreaker.getState(11L));
        circuitBreaker.recordFailure(11L);
        assertEquals(CircuitStateEnum.OPEN, circuitBreaker.getState(11L));
    }

    /**
     * 验证重置后熔断状态与失败计数全部清除，打开中的供应商恢复闭合且放行调用
     */
    @Test
    void shouldClearStateOnReset() {
        for (int i = 0; i < 5; i++) {
            circuitBreaker.recordFailure(11L);
        }
        assertEquals(CircuitStateEnum.OPEN, circuitBreaker.getState(11L));
        circuitBreaker.reset(11L);
        assertEquals(CircuitStateEnum.CLOSED, circuitBreaker.getState(11L));
        assertTrue(circuitBreaker.tryAcquire(11L));
    }

    /**
     * 验证无失败记录的供应商默认闭合放行，不同供应商的失败计数相互隔离
     */
    @Test
    void shouldIsolateProvidersAndDefaultClosed() {
        assertEquals(CircuitStateEnum.CLOSED, circuitBreaker.getState(99L));
        assertTrue(circuitBreaker.tryAcquire(99L));
        for (int i = 0; i < 5; i++) {
            circuitBreaker.recordFailure(11L);
        }
        assertEquals(CircuitStateEnum.CLOSED, circuitBreaker.getState(22L));
        assertTrue(circuitBreaker.tryAcquire(22L));
    }

    /**
     * 验证空供应商id的守护行为，许可与状态查询快速失败，记录与重置静默忽略
     */
    @Test
    void shouldGuardNullProviderId() {
        assertThrows(IllegalArgumentException.class, () -> circuitBreaker.tryAcquire(null));
        assertThrows(IllegalArgumentException.class, () -> circuitBreaker.getState(null));
        circuitBreaker.recordSuccess(null);
        circuitBreaker.recordFailure(null);
        circuitBreaker.reset(null);
        assertEquals(CircuitStateEnum.CLOSED, circuitBreaker.getState(11L));
    }
}

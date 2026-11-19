package com.spark.test.llm;

import com.spark.common.bean.base.ResultData;
import com.spark.common.enums.TenantConfigEnum;
import com.spark.llm.model.ratelimit.TenantRateLimiter;
import com.spark.manage.sys.ITenantConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 16:30:00
 * 租户级模型限流器测试
 */
public class TenantRateLimiterTest {
    private TenantRateLimiter rateLimiter;
    private ITenantConfigService tenantConfigService;
    private AtomicLong nowMillis;

    @BeforeEach
    void setUp() {
        rateLimiter = new TenantRateLimiter();
        tenantConfigService = mock(ITenantConfigService.class);
        ReflectionTestUtils.setField(rateLimiter, "tenantConfigService", tenantConfigService);
        nowMillis = new AtomicLong(0L);
        LongSupplier clock = nowMillis::get;
        ReflectionTestUtils.setField(rateLimiter, "clock", clock);
    }

    /**
     * 验证租户无任何配置时视为不限制，连续100次获取许可全部放行
     */
    @Test
    void shouldAllowUnlimitedWhenNoConfig() {
        for (int i = 0; i < 100; i++) {
            boolean acquired = rateLimiter.tryAcquire(103L);
            assertTrue(acquired);
        }
    }

    /**
     * 验证并发限额为1时第二次获取被拒绝，释放一个许可后再次获取立即放行
     */
    @Test
    void shouldRejectWhenConcurrentFullAndAllowAfterRelease() {
        this.stubLimits(103L, "1", "0", "0");
        boolean first = rateLimiter.tryAcquire(103L);
        assertTrue(first);
        boolean second = rateLimiter.tryAcquire(103L);
        assertFalse(second);
        rateLimiter.release(103L);
        boolean third = rateLimiter.tryAcquire(103L);
        assertTrue(third);
    }

    /**
     * 验证RPM限额为1时窗口内第二次获取被拒绝，假时钟推进60秒窗口滚动后放行
     */
    @Test
    void shouldRejectWhenRpmFullAndAllowAfterWindowRoll() {
        this.stubLimits(103L, "0", "1", "0");
        boolean first = rateLimiter.tryAcquire(103L);
        assertTrue(first);
        boolean second = rateLimiter.tryAcquire(103L);
        assertFalse(second);
        nowMillis.addAndGet(60000L);
        boolean third = rateLimiter.tryAcquire(103L);
        assertTrue(third);
    }

    /**
     * 验证TPM限额为100时累计消耗100token后获取被拒绝，窗口滚动清零token计数后放行
     */
    @Test
    void shouldRejectWhenTpmFullAndAllowAfterWindowRoll() {
        this.stubLimits(103L, "0", "0", "100");
        boolean first = rateLimiter.tryAcquire(103L);
        assertTrue(first);
        rateLimiter.recordTokens(103L, 100);
        boolean second = rateLimiter.tryAcquire(103L);
        assertFalse(second);
        nowMillis.addAndGet(60000L);
        boolean third = rateLimiter.tryAcquire(103L);
        assertTrue(third);
    }

    /**
     * 验证租户之间限流状态隔离，103租户RPM打满被拒绝时104租户不受影响仍放行
     */
    @Test
    void shouldIsolateTenants() {
        this.stubLimits(103L, "0", "1", "0");
        boolean first = rateLimiter.tryAcquire(103L);
        assertTrue(first);
        boolean second = rateLimiter.tryAcquire(103L);
        assertFalse(second);
        boolean other = rateLimiter.tryAcquire(104L);
        assertTrue(other);
    }

    /**
     * 验证租户id为null或0的系统调用直接放行，不读取租户配置也不产生限流状态
     */
    @Test
    void shouldSkipSystemTenant() {
        boolean nullTenant = rateLimiter.tryAcquire(null);
        assertTrue(nullTenant);
        boolean systemTenant = rateLimiter.tryAcquire(0L);
        assertTrue(systemTenant);
        rateLimiter.release(null);
        rateLimiter.release(0L);
        rateLimiter.recordTokens(null, 100);
        rateLimiter.recordTokens(0L, 100);
        verify(tenantConfigService, never()).queryTenantConfigMap(any());
    }

    /**
     * 验证配置值为非数字、负数、超长数字或空白时全部回退不限制，连续获取均放行
     */
    @Test
    void shouldTreatInvalidConfigAsUnlimited() {
        this.stubLimits(103L, "abc", "-5", "99999999999999999999");
        this.stubLimits(104L, "", " ", "0");
        for (int i = 0; i < 10; i++) {
            boolean acquired103 = rateLimiter.tryAcquire(103L);
            assertTrue(acquired103);
            boolean acquired104 = rateLimiter.tryAcquire(104L);
            assertTrue(acquired104);
        }
    }

    /**
     * 验证配置从RPM=1改为RPM=5后下一次获取立即按新限额放行，限流器不持有陈旧限额
     */
    @Test
    void shouldApplyChangedLimitImmediately() {
        this.stubLimits(103L, "0", "1", "0");
        boolean first = rateLimiter.tryAcquire(103L);
        assertTrue(first);
        boolean second = rateLimiter.tryAcquire(103L);
        assertFalse(second);
        this.stubLimits(103L, "0", "5", "0");
        boolean third = rateLimiter.tryAcquire(103L);
        assertTrue(third);
    }

    /**
     * 验证无状态租户的释放与token回填静默忽略，null与非正token不计数，合法token累计到限额后拒绝
     */
    @Test
    void shouldIgnoreReleaseAndTokensWithoutState() {
        rateLimiter.release(999L);
        rateLimiter.recordTokens(999L, 50);
        this.stubLimits(103L, "0", "0", "100");
        boolean first = rateLimiter.tryAcquire(103L);
        assertTrue(first);
        rateLimiter.recordTokens(103L, null);
        rateLimiter.recordTokens(103L, 0);
        rateLimiter.recordTokens(103L, 50);
        boolean second = rateLimiter.tryAcquire(103L);
        assertTrue(second);
        rateLimiter.recordTokens(103L, 50);
        boolean third = rateLimiter.tryAcquire(103L);
        assertFalse(third);
    }

    /**
     * 桩定租户三项限流配置
     */
    private void stubLimits(Long tenantId, String maxConcurrent, String rpm, String tpm) {
        Map<String, String> configMap = new HashMap<>();
        configMap.put(TenantConfigEnum.LLM_MAX_CONCURRENT.getKey(), maxConcurrent);
        configMap.put(TenantConfigEnum.LLM_RPM.getKey(), rpm);
        configMap.put(TenantConfigEnum.LLM_TPM.getKey(), tpm);
        ResultData<Map<String, String>> configResult = new ResultData<>();
        configResult.setData(configMap);
        configResult.setCode(ResultData.OK);
        when(tenantConfigService.queryTenantConfigMap(tenantId)).thenReturn(configResult);
    }
}
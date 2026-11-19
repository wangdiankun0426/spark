package com.spark.llm.model.ratelimit;

import com.spark.common.bean.base.ResultData;
import com.spark.common.constant.DefaultBaseParam;
import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.common.enums.TenantConfigEnum;
import com.spark.common.utils.StringUtil;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.manage.sys.ITenantConfigService;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @since 2026-11-19 16:00:00
 * 租户级模型限流器，按租户配置限制并发与每分钟请求数、token数，超限快速失败
 */
@Component
public class TenantRateLimiter {
    @Autowired
    private ITenantConfigService tenantConfigService;
    /**
     * 各租户的限流状态，key为租户id
     */
    private final Map<Long, TenantRateLimitState> states = new ConcurrentHashMap<>();
    /**
     * 毫秒时钟，测试可注入假时钟
     */
    private LongSupplier clock = System::currentTimeMillis;

    /**
     * 尝试获取租户调用许可，系统调用不限流
     * @param tenantId 租户id
     * @return 是否允许调用
     */
    public boolean tryAcquire(Long tenantId) {
        if (tenantId == null || DefaultBaseParam.SYSTEM_TENANT_ID.equals(tenantId)) {
            return true;
        }
        int maxConcurrent = 0;
        int rpm = 0;
        int tpm = 0;
        ResultData<Map<String, String>> configResult = tenantConfigService.queryTenantConfigMap(tenantId);
        if (configResult != null && configResult.getCode() == ResultData.OK && configResult.getData() != null) {
            Map<String, String> configMap = configResult.getData();
            maxConcurrent = this.parseLimit(configMap.get(TenantConfigEnum.LLM_MAX_CONCURRENT.getKey()));
            rpm = this.parseLimit(configMap.get(TenantConfigEnum.LLM_RPM.getKey()));
            tpm = this.parseLimit(configMap.get(TenantConfigEnum.LLM_TPM.getKey()));
        }
        TenantRateLimitState state = this.resolveState(tenantId);
        long now = clock.getAsLong();
        return state.tryAcquire(now, maxConcurrent, rpm, tpm);
    }

    /**
     * 释放租户一个并发许可
     * @param tenantId 租户id
     */
    public void release(Long tenantId) {
        if (tenantId == null || DefaultBaseParam.SYSTEM_TENANT_ID.equals(tenantId)) {
            return;
        }
        TenantRateLimitState state = states.get(tenantId);
        if (state == null) {
            return;
        }
        state.release();
    }

    /**
     * 累计租户本次调用消耗的token数
     * @param tenantId 租户id
     * @param totalTokens 消耗token数
     */
    public void recordTokens(Long tenantId, Integer totalTokens) {
        if (tenantId == null || DefaultBaseParam.SYSTEM_TENANT_ID.equals(tenantId)
                || totalTokens == null || totalTokens <= 0) {
            return;
        }
        TenantRateLimitState state = states.get(tenantId);
        if (state == null) {
            return;
        }
        long now = clock.getAsLong();
        state.recordTokens(now, totalTokens);
    }

    /**
     * 构造租户限流快速失败异常
     * @return 限流异常
     */
    public static ModelInvocationException rateLimitException() {
        return new ModelInvocationException(ModelErrorTypeEnum.RATE_LIMIT, 429,
                "tenant rate limit exceeded", null, "当前租户模型调用已达上限，请稍后重试");
    }

    /**
     * 获取或创建租户限流状态
     * @param tenantId 租户id
     * @return 限流状态机
     */
    private TenantRateLimitState resolveState(Long tenantId) {
        return states.computeIfAbsent(tenantId, id -> new TenantRateLimitState(id, clock.getAsLong()));
    }

    /**
     * 解析限额配置值，缺失或非法回退不限制
     * @param value 配置值
     * @return 限额，0表示不限制
     */
    private int parseLimit(String value) {
        if (StringUtil.isBlank(value) || !StringUtil.isNumeric(value.trim())) {
            return 0;
        }
        try {
            int limit = Integer.parseInt(value.trim());
            return Math.max(limit, 0);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
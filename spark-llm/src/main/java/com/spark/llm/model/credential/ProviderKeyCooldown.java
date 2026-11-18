package com.spark.llm.model.credential;

import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 11:00:00
 * 当前节点的凭据冷却记录
 */
@Data
public class ProviderKeyCooldown {
    /**
     * 冷却凭据所属的厂商id
     */
    private final Long providerId;
    /**
     * 当前节点计算的冷却截止毫秒时间戳
     */
    private final long cooldownUntil;
}

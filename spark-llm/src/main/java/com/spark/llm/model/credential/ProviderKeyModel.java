package com.spark.llm.model.credential;

import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 10:00:00
 * 凭据与底层模型客户端的绑定结果
 */
@Data
public class ProviderKeyModel<T> {
    /**
     * 当前物理调用使用的厂商id
     */
    private final Long providerId;
    /**
     * 当前物理调用使用的凭据id
     */
    private final Long providerKeyId;
    /**
     * 与当前凭据绑定的底层模型客户端
     */
    private final T delegate;
}

package com.spark.llm.model.credential;

import lombok.Data;
import lombok.ToString;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 10:00:00
 * 带凭据版本的底层模型客户端缓存项
 */
@Data
public class ProviderKeyModelCacheEntry<T> {
    /**
     * 构建当前客户端时使用的密文版本
     */
    @ToString.Exclude
    private final String encryptedKey;
    /**
     * 按当前密文版本创建的底层模型客户端
     */
    private final T delegate;
}

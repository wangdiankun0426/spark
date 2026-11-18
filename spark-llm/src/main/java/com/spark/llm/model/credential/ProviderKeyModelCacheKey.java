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
 * 模型与凭据维度的客户端缓存键
 */
@Data
public class ProviderKeyModelCacheKey {
    /**
     * 模型配置id
     */
    private final Long modelId;
    /**
     * 构建底层客户端时使用的凭据id
     */
    private final Long providerKeyId;
}

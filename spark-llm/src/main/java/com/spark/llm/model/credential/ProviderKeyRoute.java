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
 * 单次模型尝试选中的凭据
 */
@Data
public class ProviderKeyRoute {
    /**
     * 本次物理调用选中的凭据id
     */
    private final Long providerKeyId;
    /**
     * 选中凭据所属的厂商id
     */
    private final Long providerId;
    /**
     * 数据库密文，用于判断客户端缓存版本
     */
    @ToString.Exclude
    private final String encryptedKey;
    /**
     * 解密后的密钥，仅用于构建本次底层客户端
     */
    @ToString.Exclude
    private final String secretKey;
}

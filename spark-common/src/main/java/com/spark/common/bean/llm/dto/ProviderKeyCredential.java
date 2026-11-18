package com.spark.common.bean.llm.dto;

import lombok.Data;
import lombok.ToString;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 15:00:00
 * 模型厂商密钥凭据
 */
@Data
public class ProviderKeyCredential {

    /**
     * 密钥id
     */
    private Long id;

    /**
     * 厂商id
     */
    private Long providerId;

    /**
     * 加密后的厂商密钥
     */
    @ToString.Exclude
    private String secretKey;

}

package com.spark.common.bean.sys.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-12 16:00:00
 * 一次性加密密钥
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EncryptKey {

    /**
     * 密钥id
     */
    private String keyId;

    /**
     * 一次性密钥
     */
    private String key;
}

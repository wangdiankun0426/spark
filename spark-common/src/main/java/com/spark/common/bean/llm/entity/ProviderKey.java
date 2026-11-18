package com.spark.common.bean.llm.entity;

import com.spark.common.bean.base.BaseEntity;
import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 15:00:00
 * 模型厂商密钥实体
 */
@Data
public class ProviderKey extends BaseEntity {

    /**
     * 厂商id
     */
    private Long providerId;

    /**
     * 加密后的厂商密钥
     */
    @ToString.Exclude
    private String secretKey;

    /**
     * 密钥末四位
     */
    private String keyLast4;

    /**
     * 状态：1:启用，-1:禁用
     */
    private Integer status;

    /**
     * 冷却截止时间
     */
    private Timestamp cooldownUntil;

}

package com.spark.common.bean.llm.result;

import com.spark.common.bean.base.BaseResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 15:00:00
 * 模型厂商密钥查询结果
 */
@Data
public class ProviderKeyResult extends BaseResult {

    /**
     * 厂商id
     */
    private Long providerId;

    /**
     * 密钥末四位
     */
    private String keyLast4;

    /**
     * 密钥掩码
     */
    private String keyMask;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 冷却截止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Timestamp cooldownUntil;

}

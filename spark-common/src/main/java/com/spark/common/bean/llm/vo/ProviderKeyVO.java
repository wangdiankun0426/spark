package com.spark.common.bean.llm.vo;

import com.spark.common.bean.base.BaseVO;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * 模型厂商密钥请求参数
 */
@Data
public class ProviderKeyVO extends BaseVO {

    /**
     * 厂商id
     */
    private Long providerId;

    /**
     * 厂商密钥
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private String secretKey;

    /**
     * 状态：1:启用，-1:禁用
     */
    private Integer status;

}

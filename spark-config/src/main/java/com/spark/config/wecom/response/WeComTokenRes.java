package com.spark.config.wecom.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-28 14:00:00
 * 企业微信获取Token响应
 */
@Data
public class WeComTokenRes {

    /**
     * 错误码（0表示成功）
     */
    private Integer errcode;

    /**
     * 错误信息
     */
    private String errmsg;

    /**
     * 获取到的凭证
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * 凭证有效时间（秒）
     */
    @JsonProperty("expires_in")
    private Integer expiresIn;
}
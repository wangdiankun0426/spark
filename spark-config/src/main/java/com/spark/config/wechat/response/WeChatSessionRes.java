package com.spark.config.wechat.response;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-03 10:00:00
 * 微信jscode2session响应
 */
@Data
public class WeChatSessionRes {

    /**
     * 错误码（0表示成功）
     */
    private Integer errcode;

    /**
     * 错误信息
     */
    private String errmsg;

    /**
     * 会话密钥
     */
    @JSONField(name = "session_key")
    private String sessionKey;

    /**
     * 用户openid
     */
    private String openid;

    /**
     * 用户unionid
     */
    private String unionid;
}

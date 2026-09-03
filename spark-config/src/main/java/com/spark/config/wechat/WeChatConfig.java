package com.spark.config.wechat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-03 10:00:00
 * 微信配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "spark.wechat")
public class WeChatConfig {
    /**
     * 小程序appId
     */
    private String appId;

    /**
     * 小程序appSecret
     */
    private String appSecret;
}

package com.spark.config.wecom;

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
 * @since 2026/7/28 11:34
 */
@Data
@Component
@ConfigurationProperties(prefix = "spark.wecom")
public class WeComConfig {
    /**
     * 企业id
     */
    private String corpId;

    /**
     * 消息token
     */
    private String msgToken;

    /**
     * 消息密钥
     */
    private String msgEncodingAESKey;

    /**
     * 企业微信应用的Secret
     */
    private String corpSecret;

    /**
     * 企业微信应用ID（AgentId）
     */
    private Long agentId;
}

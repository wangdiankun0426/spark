package com.spark.config.shortMsg;

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
 * @since 2025/12/7 09:36
 */
@Data
@Component
@ConfigurationProperties(prefix = "spark.sm.alibaba")
public class AlibabaShortMsgConfig {

    /**
     * 短信key
     */
    private String accessKeyId;

    /**
     * 短信密钥
     */
    private String accessKeySecret;

    /**
     * 短信签名
     */
    private String signName;

    /**
     * 短信模板
     */
    private String templateCode;

}

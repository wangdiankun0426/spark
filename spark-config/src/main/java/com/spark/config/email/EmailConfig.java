package com.spark.config.email;

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
 * @since 2025/12/7 09:03
 */
@Data
@Component
@ConfigurationProperties(prefix = "spark.email")
public class EmailConfig {

    // 发件人 同邮箱地址
    private String user = "w15610077197@163.com";

    // 授权码 开启SMTP时显示
    private String password = "RCKRIHBNSOSBEJWF";

    // 邮箱主机
    private String host = "smtp.163.com";

    // 是否需要验证
    private boolean enableAuth  = true;
}

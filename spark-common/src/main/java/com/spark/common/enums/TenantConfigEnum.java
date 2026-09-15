package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-11 10:00:00
 * 租户默认配置
 */
public enum TenantConfigEnum {
    // 默认密码
    DEFAULT_PASSWORD("default_password", "默认密码", "123456"),
    // 企微企业id
    WECOM_CORP_ID("wecom_corp_id", "企微企业ID", ""),
    // 企微应用id
    WECOM_AGENT_ID("wecom_agent_id", "企微应用ID", ""),
    // 企微应用密钥
    WECOM_CORP_SECRET("wecom_corp_secret", "企微应用密钥", ""),
    // 企微消息token
    WECOM_MSG_TOKEN("wecom_msg_token", "企微消息Token", ""),
    // 企微消息密钥
    WECOM_MSG_ENCODING_AES_KEY("wecom_msg_encoding_aes_key", "企微消息密钥", ""),
    // 密码最小长度
    PWD_MIN_LENGTH("pwd.min.length", "密码最小长度", "6"),
    // 密码最大长度
    PWD_MAX_LENGTH("pwd.max.length", "密码最大长度", "12"),

    ;

    private String key;

    private String name;

    private String value;

    TenantConfigEnum(String key, String name, String value) {
        this.key = key;
        this.name = name;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}

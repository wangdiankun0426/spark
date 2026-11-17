package com.spark.common.enums;

import lombok.Getter;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-17 11:00:00
 * 模型调用错误分类
 */
@Getter
public enum ModelErrorTypeEnum {
    RATE_LIMIT(true, "模型服务当前繁忙，请稍后重试"),
    AUTH(false, "模型服务认证失败，请联系管理员检查模型配置"),
    TIMEOUT(true, "模型响应超时，请稍后重试"),
    SERVER(true, "模型服务暂时异常，请稍后重试"),
    NETWORK(true, "模型服务连接失败，请检查网络后重试"),
    INVALID(false, "模型请求或配置无效，请检查输入与配置"),
    UNKNOWN(false, "当前模型服务异常，请稍后重试"),
    ;
    private final boolean retryable;
    private final String userMessage;

    ModelErrorTypeEnum(boolean retryable, String userMessage) {
        this.retryable = retryable;
        this.userMessage = userMessage;
    }
}

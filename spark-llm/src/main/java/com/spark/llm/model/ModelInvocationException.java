package com.spark.llm.model;

import com.spark.common.enums.ModelErrorTypeEnum;
import lombok.Getter;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-17 11:00:00
 * 模型调用统一异常，保留标准分类与原始异常
 */
@Getter
public class ModelInvocationException extends RuntimeException {
    private final ModelErrorTypeEnum errorType;
    private final Integer httpStatus;
    private final String userMessage;

    public ModelInvocationException(ModelErrorTypeEnum errorType, Integer httpStatus,
                                    String message, Throwable cause, String userMessage) {
        super(message, cause);
        if (errorType == null) {
            throw new IllegalArgumentException("errorType must not be null");
        }
        this.errorType = errorType;
        this.httpStatus = httpStatus;
        this.userMessage = userMessage;
    }

    /**
     * 判断是否允许应用层重试
     * @return 是否可重试
     */
    public boolean isRetryable() {
        return errorType.isRetryable();
    }
}

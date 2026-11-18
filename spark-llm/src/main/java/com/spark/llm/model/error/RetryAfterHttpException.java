package com.spark.llm.model.error;

import dev.langchain4j.exception.HttpException;
import lombok.Getter;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 18:00:00
 * 携带供应商重试等待时间的HTTP异常
 */
@Getter
public class RetryAfterHttpException extends HttpException {
    /**
     * 供应商要求的最短等待毫秒
     */
    private final long retryAfterMs;

    public RetryAfterHttpException(int statusCode, String message, long retryAfterMs) {
        super(statusCode, message);
        if (retryAfterMs < 0) {
            throw new IllegalArgumentException("retryAfterMs must not be negative");
        }
        this.retryAfterMs = retryAfterMs;
    }
}

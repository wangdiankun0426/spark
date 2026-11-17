package com.spark.llm.model;

import lombok.Getter;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-17 10:00:00
 * 模型流式空闲超时异常
 */
@Getter
public class ModelTimeoutException extends RuntimeException {
    private final Long modelId;
    private final long timeoutMs;

    public ModelTimeoutException(Long modelId, long timeoutMs) {
        super("model streaming idle timeout, modelId=" + modelId + ", timeoutMs=" + timeoutMs);
        this.modelId = modelId;
        this.timeoutMs = timeoutMs;
    }
}

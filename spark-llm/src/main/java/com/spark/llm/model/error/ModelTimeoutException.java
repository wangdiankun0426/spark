package com.spark.llm.model.error;

import com.spark.common.enums.ModelErrorTypeEnum;
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
public class ModelTimeoutException extends ModelInvocationException {
    private final Long modelId;
    private final long timeoutMs;

    public ModelTimeoutException(Long modelId, long timeoutMs) {
        super(ModelErrorTypeEnum.TIMEOUT, null,
                "model streaming idle timeout, modelId=" + modelId + ", timeoutMs=" + timeoutMs,
                null, buildUserMessage(timeoutMs));
        this.modelId = modelId;
        this.timeoutMs = timeoutMs;
    }

    /**
     * 构造流式空闲超时提示
     * @param timeoutMs 超时毫秒
     * @return 用户提示
     */
    private static String buildUserMessage(long timeoutMs) {
        long timeoutSeconds = timeoutMs / 1000;
        return "模型响应超过 " + timeoutSeconds + " 秒无输出，本次对话已结束，请重试";
    }
}

package com.spark.llm.model.fallback;

import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.llm.model.error.ModelInvocationException;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 09:00:00
 * 模型目标间降级策略
 */
public final class ModelFallbackPolicy {

    private ModelFallbackPolicy() {
    }

    /**
     * 判断当前目标结束后是否允许尝试备用模型
     * @param exception 当前目标最终异常
     * @return 是否允许降级
     */
    public static boolean shouldFallback(ModelInvocationException exception) {
        if (exception == null) {
            return false;
        }
        ModelErrorTypeEnum errorType = exception.getErrorType();
        return errorType == ModelErrorTypeEnum.AUTH || errorType.isRetryable();
    }
}

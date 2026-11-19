package com.spark.llm.model.fallback;

import com.spark.common.bean.llm.dto.ModelInfo;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 09:00:00
 * 降级链中的单模型调用目标
 * @param <T> 模型接口类型
 */
@Data
public class FallbackModelTarget<T> {
    /**
     * 当前目标的模型元数据
     */
    private final ModelInfo modelInfo;

    /**
     * 带当前模型独立重试能力的调用目标
     */
    private final T delegate;
}

package com.spark.common.bean.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/10/27 10:00:00
 * 用量采集用模型元数据，模型创建时固化
 */
@Data
@AllArgsConstructor
public class ModelInfo {

    /**
     * 模型id
     */
    private final Long modelId;

    /**
     * 模型名称
     */
    private final String modelName;

    /**
     * 厂商id
     */
    private final Long providerId;

    /**
     * 模型类型：1语言，2向量，3排序
     */
    private final Integer type;
}

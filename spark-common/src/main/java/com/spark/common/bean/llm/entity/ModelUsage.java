package com.spark.common.bean.llm.entity;

import com.spark.common.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/10/27 10:00:00
 * 模型token用量明细
 */
@Data
public class ModelUsage extends BaseEntity {

    /**
     * 使用人id（无主系统调用为101）
     **/
    private Long userId;

    /**
     * 模型id
     **/
    private Long modelId;

    /**
     * 模型名称（冗余）
     **/
    private String modelName;

    /**
     * 厂商id
     **/
    private Long providerId;

    /**
     * 模型类型：1语言，2向量，3排序
     **/
    private Integer type;

    /**
     * 调用场景编码
     **/
    private String scene;

    /**
     * 输入token数
     **/
    private Integer inputTokens;

    /**
     * 输出token数
     **/
    private Integer outputTokens;

    /**
     * 总token数
     **/
    private Integer totalTokens;

}

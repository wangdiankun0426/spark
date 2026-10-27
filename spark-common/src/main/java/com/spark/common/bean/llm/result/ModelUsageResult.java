package com.spark.common.bean.llm.result;

import com.spark.common.bean.base.BaseResult;
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
public class ModelUsageResult extends BaseResult {

    /**
     * 租户id
     **/
    private Long tenantId;

    /**
     * 使用人id
     **/
    private Long userId;

    /**
     * 使用人名称
     **/
    private String userName;

    /**
     * 模型id
     **/
    private Long modelId;

    /**
     * 模型名称
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
     * 模型类型名称
     **/
    private String typeName;

    /**
     * 调用场景编码
     **/
    private String scene;

    /**
     * 调用场景名称
     **/
    private String sceneName;

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

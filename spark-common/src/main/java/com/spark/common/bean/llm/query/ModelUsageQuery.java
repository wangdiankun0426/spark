package com.spark.common.bean.llm.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/10/27 10:00:00
 * 模型token用量查询参数
 */
@Data
public class ModelUsageQuery extends BaseQuery {

    /**
     * 模型id
     **/
    private Long modelId;

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
     * 使用人id
     **/
    private Long userId;

    /**
     * 起始时间（yyyy-MM-dd HH:mm:ss）
     **/
    private String startTime;

    /**
     * 结束时间（yyyy-MM-dd HH:mm:ss）
     **/
    private String endTime;

}

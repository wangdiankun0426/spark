package com.spark.bean.llm.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-05-14 09:45:43
 */
@Data
public class ModelQuery extends BaseQuery {

    /**
     * 供应商id
     */
    private Long providerId;

    /**
     * 模型类型
     */
    private Integer type;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否开启思考模式：1:开启，-1:不开启
     */
    private Integer enableThinking;

    /**
     * 温度参数：控制生成内容的随机性，范围 0.0-2.0
     */
    private Double temperature;

    /**
     * 状态：1:已开启，-1:已关闭
     */
    private Integer status;

}

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
 * @since 2026/07/19 15:10:00
 */
@Data
public class McpQuery extends BaseQuery {

    /**
     * 名称
     */
    private String name;

    /**
     * 传输类型
     */
    private Integer transport;

    /**
     * 厂商id
     */
    private Long providerId;

    /**
     * 状态
     */
    private Integer status;

}

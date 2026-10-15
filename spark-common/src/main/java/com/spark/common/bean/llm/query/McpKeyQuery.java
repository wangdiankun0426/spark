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
 * @since 2026-10-15 14:00:00
 */
@Data
public class McpKeyQuery extends BaseQuery {

    /**
     * 密钥名称
     */
    private String name;

    /**
     * API密钥
     */
    private String keyValue;

    /**
     * 绑定用户id
     */
    private Long userId;

    /**
     * 状态
     */
    private Integer status;

}

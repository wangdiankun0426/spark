package com.spark.common.bean.llm.entity;

import com.spark.common.bean.base.BaseEntity;
import lombok.Data;

import java.sql.Timestamp;

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
public class McpKey extends BaseEntity {

    /**
     * API密钥
     **/
    private String keyValue;

    /**
     * 密钥名称
     **/
    private String name;

    /**
     * 绑定用户id
     **/
    private Long userId;

    /**
     * 过期时间
     **/
    private Timestamp expireDt;

    /**
     * 最近使用时间
     **/
    private Timestamp lastUsedDt;

    /**
     * 使用次数
     **/
    private Long useCount;

    /**
     * 状态：1:启用，-1:禁用
     **/
    private Integer status;

    /**
     * 描述
     **/
    private String description;

}

package com.spark.common.bean.llm.result;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.common.bean.base.BaseResult;
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
public class McpKeyResult extends BaseResult {

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
     * 绑定用户名称
     **/
    private String userName;

    /**
     * 过期时间
     **/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Timestamp expireDt;

    /**
     * 最近使用时间
     **/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
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
     * 状态名称
     **/
    private String statusName;

    /**
     * 描述
     **/
    private String description;

    /**
     * 租户id
     **/
    private Long tenantId;

}

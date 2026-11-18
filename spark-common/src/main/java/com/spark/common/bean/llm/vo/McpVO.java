package com.spark.common.bean.llm.vo;

import com.spark.common.bean.base.BaseVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

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
public class McpVO extends BaseVO {

    /**
     * 名称（唯一标识）
     **/
    private String name;

    /**
     * 传输类型：2:HTTP
     **/
    private Integer transport;

    /**
     * MCP服务密钥
     **/
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private String secretKey;

    /**
     * 服务URL
     **/
    private String url;

    /**
     * 连接超时秒
     **/
    private Long timeout;

    /**
     * 描述
     **/
    private String description;

    /**
     * 状态：1:启用，-1:禁用
     **/
    private Integer status;

}

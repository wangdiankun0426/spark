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
 * @since 2026/07/19 15:10:00
 */
@Data
public class McpResult extends BaseResult {

    /**
     * 名称（唯一标识）
     **/
    private String name;

    /**
     * 传输类型：2:HTTP
     **/
    private Integer transport;

    /**
     * 传输类型名称
     **/
    private String transportName;

    /**
     * 厂商id
     **/
    private Long providerId;

    /**
     * 厂商名称
     **/
    private String providerName;

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

    /**
     * 状态名称
     **/
    private String statusName;

}

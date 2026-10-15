package com.spark.common.bean.llm.vo;

import com.spark.common.bean.base.BaseVO;
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
     * 厂商id
     **/
    private Long providerId;

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

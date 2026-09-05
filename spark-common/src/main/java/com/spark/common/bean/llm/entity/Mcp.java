package com.spark.common.bean.llm.entity;

import com.spark.common.bean.base.BaseEntity;
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
public class Mcp extends BaseEntity {

    /**
     * 名称（唯一标识）
     **/
    private String name;

    /**
     * 传输类型：1:STDIO，2:SSE
     **/
    private Integer transport;

    /**
     * 厂商id（SSE模式必填）
     **/
    private Long providerId;

    /**
     * STDIO命令
     **/
    private String command;

    /**
     * STDIO命令参数，JSON数组
     **/
    private String args;

    /**
     * SSE模式URL
     **/
    private String url;

    /**
     * 环境变量，JSON对象
     **/
    private String env;

    /**
     * 连接超时毫秒
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

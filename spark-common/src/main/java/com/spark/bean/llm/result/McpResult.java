package com.spark.bean.llm.result;

import com.spark.bean.base.BaseResult;
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
     * 传输类型：1:STDIO，2:SSE
     **/
    private Integer transport;

    /**
     * 传输类型名称
     **/
    private String transportName;

    /**
     * 厂商id（SSE模式必填）
     **/
    private Long providerId;

    /**
     * 厂商名称
     **/
    private String providerName;

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

    /**
     * 状态名称
     **/
    private String statusName;

}

package com.spark.bean.log.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/3 15:16
 */
@Data
public class LogLoginResult extends BaseResult {

    /**
     * ip地址
     */
    private String ipaddress;

    /**
     * 登录类型
     */
    private Integer loginType;

    /**
     * 登录类型名称
     */
    private String loginTypeName;

    /**
     * 登录平台名称
     */
    private Integer loginPlatform;

    /**
     * 登录平台
     */
    private String loginPlatformName;

    /**
     * session ID
     */
    private String sessionId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;
}

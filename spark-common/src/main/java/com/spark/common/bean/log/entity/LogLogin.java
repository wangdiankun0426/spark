package com.spark.common.bean.log.entity;

import com.spark.common.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/3 13:29
 */
@Data
public class LogLogin extends BaseEntity {
    /**
     * ip地址
     */
    private String ipaddress;

    /**
     * 登录类型
     */
    private Integer loginType;

    /**
     * 登录平台
     */
    private Integer loginPlatform;

    /**
     * session ID
     */
    private String sessionId;
}

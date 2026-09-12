package com.spark.common.bean.sys.vo;

import com.spark.common.bean.base.BaseVO;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/3/5 13:15
 * 用户登录
 */
@Data
public class LoginVO extends BaseVO {
    /**
     * 登录名
     */
    private String loginName;

    /**
     * 密码
     */
    private String password;

    /**
     * IP地址
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
     * 验证码id
     */
    private String validateId;

    /**
     * 验证码值
     */
    private String validateValue;

    /**
     * 手机号
     */
    private String phone;

    /**
     * session ID
     */
    private String sessionId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 微信登录code
     */
    private String wxCode;

    /**
     * 一次性加密密钥ID
     */
    private String keyId;
}

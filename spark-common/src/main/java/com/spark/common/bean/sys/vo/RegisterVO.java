package com.spark.common.bean.sys.vo;

import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-25 10:00:00
 * 用户注册参数
 */
@Data
public class RegisterVO {

    /**
     * 登录名
     */
    private String loginName;

    /**
     * 密码（DES加密传输）
     */
    private String password;

    /**
     * 验证码ID
     */
    private String validateId;

    /**
     * 验证码值
     */
    private String validateValue;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 一次性加密密钥ID
     */
    private String keyId;
}

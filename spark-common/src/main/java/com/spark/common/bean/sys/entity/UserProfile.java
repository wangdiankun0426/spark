package com.spark.common.bean.sys.entity;

import com.spark.common.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-25 12:00:00
 * 用户扩展信息表
 */
@Data
public class UserProfile extends BaseEntity {

    /**
     * 企业微信用户ID
     */
    private String wecomId;

    /**
     * 微信小程序openid
     */
    private String wxOpenId;

    /**
     * 微信unionid
     */
    private String wxUnionId;

}

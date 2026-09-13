package com.spark.common.bean.sys.vo;

import com.spark.common.bean.base.BaseVO;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 18:48
 */
@Data
public class UserVO extends BaseVO {

    /**
     * 名称
     */
    private String name;

    /**
     * ids
     */
    private List<Long> ids;

    /**
     * 登录名
     */
    private String loginName;

    /**
     * 归属租户
     */
    private Long tenantId;

    /**
     * 所属部门
     */
    private Long deptId;

    /**
     * 角色
     */
    private List<Long> roleIds;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 账号类型
     */
    private Integer accountType;

    /**
     * 企业微信用户ID
     */
    private String wecomId;

    /**
     * 原密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 一次性加密密钥ID
     */
    private String keyId;

    /**
     * 微信小程序openid
     */
    private String wxOpenId;

    /**
     * 微信unionid
     */
    private String wxUnionId;

    /**
     * 密码
     */
    private String password;
}

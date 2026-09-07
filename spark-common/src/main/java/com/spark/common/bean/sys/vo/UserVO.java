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
     * 角色类型
     */
    private Integer roleType;

    /**
     * 企业微信用户ID
     */
    private String wecomId;

    /**
     *
     */
    private String oldPassword;

    /**
     *
     */
    private String newPassword;
}

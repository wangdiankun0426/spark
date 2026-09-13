package com.spark.common.bean.sys.entity;

import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/3/6 21:29
 */
@Data
public class Session {

    /**
     * 登录后sessionId
     */
    private String sessionId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 部门id
     */
    private Long deptId;

    /**
     * 部门ids
     */
    private String deptIds;

    /**
     * 数据权限
     */
    private Integer dataScope;

    /**
     * 角色类型
     */
    private Integer roleType;

    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 账号类型
     */
    private Integer accountType;

    /**
     * 登录平台
     */
    private Integer loginPlatform;
}

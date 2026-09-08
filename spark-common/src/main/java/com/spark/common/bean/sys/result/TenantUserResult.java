package com.spark.common.bean.sys.result;

import com.spark.common.bean.base.BaseResult;
import com.spark.common.enums.RoleTypeEnum;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-08 14:00:00
 * 租户用户
 */
@Data
public class TenantUserResult extends BaseResult {

    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 登录名
     */
    private String loginName;

    /**
     * 角色类型
     */
    private Integer roleType;

    /**
     * 部门id
     */
    private Long deptId;

    /**
     * 角色类型
     */
    private String roleTypeName;

    public String getRoleTypeName() {
        if (this.roleType == null) {
            return null;
        }
        return RoleTypeEnum.indexOf(this.roleType).getDesc();
    }
}

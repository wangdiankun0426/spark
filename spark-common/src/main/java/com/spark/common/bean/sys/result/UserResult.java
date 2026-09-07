package com.spark.common.bean.sys.result;

import com.spark.common.bean.base.BaseResult;
import com.spark.common.enums.RoleTypeEnum;
import com.spark.common.enums.SexEnum;
import com.spark.common.enums.StatusEnum;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/19 21:49
 */
@Data
public class UserResult extends BaseResult {

    /**
     * 名称
     */
    private String name;

    /**
     * 登录名
     */
    private String loginName;

    /**
     * 密码
     */
    private String password;

    /**
     * 部门id
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门全路径
     */
    private String deptPath;

    /**
     * 角色id
     */
    private List<Long> roleIds;

    /**
     * 角色名称
     */
    private List<String> roleNames;

    /**
     * 具有权限的菜单id
     */
    private List<Long> menuIds;

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
     * 头像
     */
    private String avatar;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 角色类型
     */
    private Integer roleType;

    /**
     * 角色类型
     */
    private String roleTypeName;

    /**
     * 企业微信用户ID
     */
    private String wecomId;

    /**
     * 性别
     */
    private String sexName;

    /**
     * 状态
     */
    private String statusName;

    public String getSexName() {
        if (this.sex == null) {
            return null;
        }
        return SexEnum.indexOf(this.sex).getDesc();
    }

    public String getStatusName() {
        if (this.status == null) {
            return null;
        }
        return StatusEnum.indexOf(this.status).getDesc();
    }

    public String getRoleTypeName() {
        if (this.roleType == null) {
            return null;
        }
        return RoleTypeEnum.indexOf(this.roleType).getDesc();
    }
}

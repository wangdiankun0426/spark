package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-07 15:30:00
 * 用户角色类型
 */
public enum RoleTypeEnum {
    UNKNOWN(0, "未知类型"),
    COMMON(1, "普通用户"),
    ORG_ADMIN(2, "组织管理员"),
    ;

    private int value;

    private String desc;

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    RoleTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static RoleTypeEnum indexOf(int value){
        for (RoleTypeEnum item:values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return UNKNOWN;
    }

    /**
     * 判断 roleType 位掩码是否包含指定权限位
     * @param roleType 用户角色位掩码
     * @param role 要判断的权限位
     * @return 是否包含
     */
    public static boolean hasRole(Integer roleType, RoleTypeEnum role) {
        return roleType != null && (roleType & role.getValue()) == role.getValue();
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

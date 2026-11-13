package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-13 18:00:00
 * 菜单平台归属
 */
public enum MenuPlatformEnum {
    TENANT(0, "租户菜单"),
    SITE(1, "站点菜单"),
    HOME(2, "公共菜单"),
    ORG(3, "组织菜单"),
    ;

    private Integer value;

    private String desc;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    MenuPlatformEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static MenuPlatformEnum indexOf(Integer value) {
        for (MenuPlatformEnum item : values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return null;
    }
}

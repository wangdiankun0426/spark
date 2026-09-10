package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/9/10 14:58
 */
public enum MenuTypeEnum {
    UNKNOWN(0, "未知类型"),
    MODULE(1, "模块"),
    MENU(2, "菜单"),

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

    MenuTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static MenuTypeEnum indexOf(Integer value) {
        for (MenuTypeEnum item : values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return UNKNOWN;
    }
}

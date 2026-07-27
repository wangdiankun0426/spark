package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/7/17 14:00
 */
public enum DocumentSearchTypeEnum {
    UNKNOWN("", "未知"),
    CONTENT("keyword", "内容检索"),
    SEMANTIC("semantic", "语义检索"),
    ;
    private String value;

    private String desc;

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    DocumentSearchTypeEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static DocumentSearchTypeEnum indexOf(String value) {
        for (DocumentSearchTypeEnum item : values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return UNKNOWN;
    }
}

package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/8/7 17:02
 */
public enum FlowTemplateNodePermissionEnum {
    ALLOW_APSS(1 , "允许审批人审批通过"),
    ALLOW_REJECT(2 , "允许审批人审批驳回"),
    AUTO_APSS(4 , "没有审批人时自动通过"),

    ;


    private Integer value;

    private String desc;

    FlowTemplateNodePermissionEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

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
}

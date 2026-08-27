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
    ALLOW_URGE(8 , "允许申请人催办"),
    ALLOW_TRANSFER(16 , "允许审批人转办"),
    ALLOW_ADD_SIGN(32 , "允许审批人加签"),
    ALLOW_COPY(64 , "允许审批人抄送"),
    ALLOW_RECALL(128 , "允许发起人撤回"),

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

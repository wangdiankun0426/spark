package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-31 10:00:00
 * 知识图谱实体审核状态枚举
 */
public enum KgAuditStatusEnum {
    UNKNOWN(-1, "未知"),
    PENDING(0, "待审核"),
    APPROVED(1, "已审核"),
    REJECTED(2, "已拒绝"),
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

    KgAuditStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static KgAuditStatusEnum indexOf(Integer value) {
        for (KgAuditStatusEnum item : values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return null;
    }
}

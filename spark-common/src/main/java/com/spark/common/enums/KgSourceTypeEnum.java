package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-31 10:00:00
 * 知识图谱实体来源类型枚举
 */
public enum KgSourceTypeEnum {
    UNKNOWN(0, "未知"),
    LLM(1, "LLM抽取"),
    MANUAL(2, "人工录入"),
    RULE(3, "规则抽取"),
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

    KgSourceTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static KgSourceTypeEnum indexOf(Integer value) {
        for (KgSourceTypeEnum item : values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return UNKNOWN;
    }
}

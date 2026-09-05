package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 15:30:00
 * 流程类型枚举
 */
public enum FlowTypeEnum {
    NORMAL(1, "普通流程"),
    KB_ARCHIVE(2, "知识库归档流程"),
    ;

    private Integer value;

    private String desc;

    FlowTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static FlowTypeEnum indexOf(Integer type) {
        for (FlowTypeEnum flowTypeEnum : FlowTypeEnum.values()) {
            if (flowTypeEnum.getValue().equals(type)) {
                return flowTypeEnum;
            }
        }
        return NORMAL;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

}

package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/8/7
 */
public enum FlowInstanceLevelEnum {
    GENERAL(1, "一般"),
    IMPORTANT(2, "重要"),
    URGENT(3, "紧急");

    private Integer value;

    private String desc;

    FlowInstanceLevelEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static FlowInstanceLevelEnum indexOf(Integer level) {
        for (FlowInstanceLevelEnum flowInstanceLevelEnum : FlowInstanceLevelEnum.values()) {
            if (flowInstanceLevelEnum.getValue().equals(level)) {
                return flowInstanceLevelEnum;
            }
        }
        return GENERAL;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

}

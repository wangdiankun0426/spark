package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-17 10:30:00
 * 流程节点审批类型枚举
 */
public enum FlowApproveTypeEnum {
    OR_SIGN(1, "或签"),
    AND_SIGN(2, "会签"),
    SEQUENTIAL(3, "依次审批");

    private Integer value;

    private String name;

    FlowApproveTypeEnum(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static FlowApproveTypeEnum indexOf(Integer value) {
        for (FlowApproveTypeEnum approveTypeEnum : FlowApproveTypeEnum.values()) {
            if (approveTypeEnum.getValue().equals(value)) {
                return approveTypeEnum;
            }
        }
        return OR_SIGN;
    }
}

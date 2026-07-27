package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/4 12:30
 */
public enum FlowAssigneeTypeEnum {
    USER(1, "指定用户"),
    FLOW_APPROVER(2, "流程发起人"),
    SYSTEM(3, "自动通过");

    private Integer value;

    private String name;

    FlowAssigneeTypeEnum(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static FlowAssigneeTypeEnum indexOf(Integer value) {
        for (FlowAssigneeTypeEnum assigneeTypeEnum : FlowAssigneeTypeEnum.values()) {
            if (assigneeTypeEnum.getValue().equals(value)) {
                return assigneeTypeEnum;
            }
        }
        return null;
    }
}

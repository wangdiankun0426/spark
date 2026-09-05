package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/4 12:30
 * 流程节点审批人类型枚举
 */
public enum FlowAssigneeTypeEnum {
    FLOW_APPROVER(1, "流程发起人"),
    SYSTEM(2, "系统自动通过"),
    USER(3, "指定用户"),
    DEPT(4, "指定部门"),
    ROLE(5, "指定角色"),
    FORM_DATA(6, "表单数据");

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
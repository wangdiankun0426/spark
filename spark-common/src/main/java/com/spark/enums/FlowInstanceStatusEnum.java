package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/2 17:42
 */
public enum FlowInstanceStatusEnum {
    UNKNOWN(0, "未知状态"),
    PENDING(1, "待处理"),
    PROCESSING(2, "审批中"),
    COMPLETED(3, "审批通过"),
    REJECTED(4, "审批驳回"),
    AUTO_PASS(5, "自动通过");

    private Integer value;

    private String desc;

    FlowInstanceStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static FlowInstanceStatusEnum indexOf(Integer status) {
        for (FlowInstanceStatusEnum flowInstanceStatusEnum : FlowInstanceStatusEnum.values()) {
            if (flowInstanceStatusEnum.getValue().equals(status)) {
                return flowInstanceStatusEnum;
            }
        }
        return UNKNOWN;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

}

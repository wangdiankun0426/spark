package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-13 10:00:00
 * AI工作流运行实例状态枚举
 */
public enum WorkflowInstanceStatusEnum {
    UNKNOWN(0, "未知"),
    RUNNING(1, "运行中"),
    SUCCESS(2, "成功"),
    FAILED(3, "失败"),
    ;

    private Integer value;

    private String desc;

    WorkflowInstanceStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 根据状态值查找枚举
     *
     * @param value 状态值
     * @return 枚举
     */
    public static WorkflowInstanceStatusEnum indexOf(Integer value) {
        for (WorkflowInstanceStatusEnum item : WorkflowInstanceStatusEnum.values()) {
            if (item.getValue().equals(value)) {
                return item;
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

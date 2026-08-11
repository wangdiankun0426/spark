package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-13 10:00:00
 * AI工作流触发方式枚举
 */
public enum WorkflowTriggerTypeEnum {
    UNKNOWN(0, "未知"),
    MANUAL(1, "手动"),
    API(2, "API"),
    SCHEDULED(3, "定时");

    private Integer value;

    private String desc;

    WorkflowTriggerTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 根据触发方式值查找枚举
     *
     * @param value 触发方式值
     * @return 枚举
     */
    public static WorkflowTriggerTypeEnum indexOf(Integer value) {
        for (WorkflowTriggerTypeEnum item : WorkflowTriggerTypeEnum.values()) {
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

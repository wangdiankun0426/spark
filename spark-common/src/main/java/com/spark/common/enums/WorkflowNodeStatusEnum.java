package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-13 10:00:00
 * workFlow节点执行状态枚举
 */
public enum WorkflowNodeStatusEnum {
    UNKNOWN(0, "未知"),
    WAITING(1, "等待"),
    RUNNING(2, "运行中"),
    SUCCESS(3, "成功"),
    FAILED(4, "失败"),
    SKIPPED(5, "跳过");

    private Integer value;

    private String desc;

    WorkflowNodeStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 根据状态值查找枚举
     *
     * @param value 状态值
     * @return 枚举
     */
    public static WorkflowNodeStatusEnum indexOf(Integer value) {
        for (WorkflowNodeStatusEnum item : WorkflowNodeStatusEnum.values()) {
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

package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 15:00:00
 * 流程节点任务执行时机枚举
 */
public enum FlowNodeTaskExecuteEnum {
    NODE_START(1, "节点开始执行"),
    NODE_END(2, "节点结束执行");

    private Integer value;

    private String desc;

    FlowNodeTaskExecuteEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static FlowNodeTaskExecuteEnum indexOf(Integer value) {
        for (FlowNodeTaskExecuteEnum executeEnum : FlowNodeTaskExecuteEnum.values()) {
            if (executeEnum.getValue().equals(value)) {
                return executeEnum;
            }
        }
        return null;
    }
}

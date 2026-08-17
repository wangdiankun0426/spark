package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/8/17 21:35
 */
public enum TaskStatusEnum {
    UNKNOWN(0, "未知状态"),
    PENDING(1, "待处理"),
    SUCCESS(2, "成功"),
    FAIL(3, "失败"),
    ;

    private Integer value;

    private String desc;

    TaskStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static TaskStatusEnum indexOf(Integer status) {
        for (TaskStatusEnum taskStatusEnum : TaskStatusEnum.values()) {
            if (taskStatusEnum.getValue().equals(status)) {
                return taskStatusEnum;
            }
        }
        return UNKNOWN;
    }
}

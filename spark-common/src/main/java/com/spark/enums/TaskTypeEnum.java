package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-17 16:30:00
 * 通用定时任务类型枚举
 */
public enum TaskTypeEnum {

    FLOW_URGE(1, "流程催办任务"),
    KB_FILE_ARCHIVE(2, "知识库文档归档任务");

    private Integer value;

    private String desc;

    TaskTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static TaskTypeEnum indexOf(Integer value) {
        for (TaskTypeEnum taskTypeEnum : TaskTypeEnum.values()) {
            if (taskTypeEnum.getValue().equals(value)) {
                return taskTypeEnum;
            }
        }
        return null;
    }
}

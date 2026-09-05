package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 10:00:00
 * 任务模板参数类型枚举
 */
public enum TaskParamTypeEnum {

    CONSTANT(1, "常量"),
    FORM(2, "表单数据");

    private Integer value;

    private String desc;

    TaskParamTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static TaskParamTypeEnum indexOf(Integer value) {
        for (TaskParamTypeEnum paramTypeEnum : TaskParamTypeEnum.values()) {
            if (paramTypeEnum.getValue().equals(value)) {
                return paramTypeEnum;
            }
        }
        return null;
    }
}

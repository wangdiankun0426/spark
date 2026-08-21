package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/7/27 12:03
 */
public enum FormTypeEnum {
    UNKNOWN(0, "未知类型"),
    COMMON(1, "普通表单"),
    FLOW(2, "流程表表单"),
    METADATA(3, "元数据表单"),
    WORKFLOW(4, "workflow表单"),
    ;

    private Integer value;

    private String desc;

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

    FormTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static FormTypeEnum indexOf(Integer value) {
        for (FormTypeEnum item : values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return UNKNOWN;
    }
}

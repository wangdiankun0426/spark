package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/4 12:30
 * 表单字段类型枚举
 */
public enum FormFieldTypeEnum {
    INPUT("input", "单行文本"),
    TEXTAREA("textarea", "多行文本"),
    RADIO("radio", "单选框"),
    SELECT("select", "下拉选择"),
    SELECT_USER("select-user", "选择用户"),
    SELECT_DEPT("select-dept", "选择部门"),
    SELECT_ROLE("select-role", "选择角色");

    private String value;

    private String name;

    FormFieldTypeEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static FormFieldTypeEnum findByValue(String value) {
        for (FormFieldTypeEnum fieldTypeEnum : FormFieldTypeEnum.values()) {
            if (fieldTypeEnum.getValue().equals(value)) {
                return fieldTypeEnum;
            }
        }
        return null;
    }
}
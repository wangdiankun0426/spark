package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-11 10:20:00
 * 聊天空间类型枚举
 */
public enum ChatSpaceTypeEnum {
    UNKNOWN(0, "未知类型"),
    USER(1, "用户对话"),
    MODEL(2, "模型对话"),
    AGENT(3, "智能体对话"),
    ;

    private Integer value;
    private String desc;

    ChatSpaceTypeEnum(Integer value, String desc) {
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
}

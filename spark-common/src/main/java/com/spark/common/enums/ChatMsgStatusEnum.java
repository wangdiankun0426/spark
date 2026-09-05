package com.spark.common.enums;

/**
 *   /\_/\
 * =( °w° )=
 *   )   (
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * @author wangdiankun
 * @since 2024/4/18 22:15
 */
public enum ChatMsgStatusEnum {
    NO(-1, "未签收/未读"),
    OK(1, "已签收/已读")

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

    ChatMsgStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}

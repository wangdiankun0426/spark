package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-21 16:30:00
 * 聊天消息附件类型枚举
 */
public enum ChatMsgAttTypeEnum {
    UNKNOWN(0, "未知类型"),
    RAG_REFERENCE(1, "rag引用文件"),
    ;

    private Integer value;
    private String desc;

    ChatMsgAttTypeEnum(Integer value, String desc) {
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

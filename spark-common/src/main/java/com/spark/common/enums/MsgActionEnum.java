package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/14 20:57
 */
public enum MsgActionEnum {
    CONNECT(1, "第一次连接/重连"),
    CHAT_MSG(2, "聊天消息"),
    SIGN_MSG(3, "消息签收"),
    READ_MSG(4, "消息已读"),
    PONG(5, "客户端保持心跳"),
    THINKING(6, "思考状态"),
    ;


    private Integer value;
    private String desc;

    MsgActionEnum(Integer value, String desc) {
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

package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/6/19 13:04
 */
public enum MessageTypeEnum {
    UNKNOWN(0, "未知类型", "未知类型"),
    LOGIN(1, "登录通知", "尊敬的用户%s，你好，您的账号于北京时间%s登录系统，请知悉！"),
    FLOW_TODO(2, "流程待办通知", "您有新的流程待办，请及时处理！"),
    FLOW_COMPLETED(3, "流程完结通知", "您申请的流程已审批完结！"),
    FLOW_REJECTED(4, "流程驳回通知", "您申请的流程已被驳回！"),
    FLOW_URGE(5, "流程催办通知", "您有流程待催办，请及时处理！"),
    ;


    private Integer type;

    private String title;

    private String content;

    MessageTypeEnum(Integer type, String title, String content) {
        this.type = type;
        this.title = title;
        this.content = content;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static MessageTypeEnum indexOf(Integer type) {
        for (MessageTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return UNKNOWN;
    }
}

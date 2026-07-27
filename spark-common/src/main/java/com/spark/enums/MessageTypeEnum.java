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
    LOGIN(1, "登录通知", "尊敬的用户%s，你好，您的账号于北京时间%s登录系统，请知悉！"),
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
}

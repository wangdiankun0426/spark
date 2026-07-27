package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/7/13 23:00
 */
public enum NoticeStatusEnum {
    UNKNOWN(0, "未知"),
    TO_BE_RELEASED(1, "待发布"),
    RELEASED(2, "已发布"),
    DELISTED(3, "已下架"),
    ;

    private Integer value;

    private String name;

    NoticeStatusEnum(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static NoticeStatusEnum indexOf(Integer value){
        for(NoticeStatusEnum item:values()){
            if(item.getValue().equals(value)){
                return item;
            }
        }
        return UNKNOWN;
    }
}

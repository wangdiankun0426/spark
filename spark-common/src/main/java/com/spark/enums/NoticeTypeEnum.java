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
public enum NoticeTypeEnum {
    UNKNOWN(0, "未知"),
    RENZHIGONGSHI(1, "任职公示"),
    FANGJIATONGZHI(2, "放假通知"),
    ;

    private Integer value;

    private String name;

    NoticeTypeEnum(Integer value, String name) {
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

    public static NoticeTypeEnum indexOf(Integer value){
        for(NoticeTypeEnum item:values()){
            if(item.getValue().equals(value)){
                return item;
            }
        }
        return UNKNOWN;
    }
}

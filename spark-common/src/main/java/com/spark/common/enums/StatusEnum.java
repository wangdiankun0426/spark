package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/3/30 18:38
 */
public enum StatusEnum {
    UNKNOWN(0,"未知"),
    NORMAL(1,"已开启"),
    ABNORMAL(-1,"已关闭"),
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

    StatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static StatusEnum indexOf(Integer value){
        for(StatusEnum item:values()){
            if(item.getValue().equals(value)){
                return item;
            }
        }
        return UNKNOWN;
    }
}

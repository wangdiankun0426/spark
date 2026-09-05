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
public enum SexEnum {
    UNKNOWN(0,"未知"),
    MAN(1,"男"),
    WOMAN(2,"女"),
    DMS_AI(3,"文档助手"),

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

    SexEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static SexEnum indexOf(Integer value){
        for(SexEnum item:values()){
            if(item.getValue().equals(value)){
                return item;
            }
        }
        return UNKNOWN;
    }
}

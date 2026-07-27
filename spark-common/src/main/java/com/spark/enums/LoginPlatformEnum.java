package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/8 12:15
 */
public enum LoginPlatformEnum {
    COMMON(1, "PC浏览器端"),
    MESSAGE(2, "移动端"),

    UNKNOWN(10, "未知")
    ;

    LoginPlatformEnum(Integer value, String desc) {
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

    private Integer value;

    private String desc;

    public static LoginPlatformEnum indexOf(Integer value){
        for(LoginPlatformEnum item:values()){
            if(item.getValue().equals(value)){
                return item;
            }
        }
        return UNKNOWN;
    }
}

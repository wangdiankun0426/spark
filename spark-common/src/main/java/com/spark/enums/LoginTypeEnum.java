package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/8 11:22
 */
public enum LoginTypeEnum {
    PASSWORD(1, "密码登录"),
    MESSAGE(2, "短信登录"),
    EMAIL(3, "邮箱登录"),
    UNKNOWN(10 ,"未知")
    ;

    LoginTypeEnum(Integer value, String desc) {
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

    public static LoginTypeEnum indexOf(Integer value){
        for(LoginTypeEnum item:values()){
            if(item.getValue().equals(value)){
                return item;
            }
        }
        return UNKNOWN;
    }
}

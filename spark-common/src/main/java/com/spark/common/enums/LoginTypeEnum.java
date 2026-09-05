package com.spark.common.enums;

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
    UNKNOWN(0 ,"未知"),
    PASSWORD(1, "密码登录"),
    MESSAGE(2, "短信登录"),
    EMAIL(3, "邮箱登录"),
    WECOM_OAUTH(4, "企微oauth2登录"),
    WECHAT(5, "微信小程序免密登录"),

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

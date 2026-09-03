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
    PC(1, "PC浏览器端"),
    MOBILE(2, "H5移动端"),
    PC_CLIENT(3, "PC客户端"),
    WECOM_PC(4, "企微PC端"),
    WECOM_MOBILE(5, "企微移动端"),
    WECHAT_MINIAPP(6, "微信小程序端"),
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

package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/8 13:30
 */
public enum LoginStatusEnum {
    ONLINE(1, "在线"),
    NOT_ONLINE(-1, "离线"),

    UNKNOWN(10, "未知")
    ;

    LoginStatusEnum(Integer value, String desc) {
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

    public static LoginStatusEnum indexOf(Integer value){
        for(LoginStatusEnum item:values()){
            if(item.getValue().equals(value)){
                return item;
            }
        }
        return UNKNOWN;
    }
}

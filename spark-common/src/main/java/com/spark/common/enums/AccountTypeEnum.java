package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/9/9 18:04
 */
public enum AccountTypeEnum {
    UNKNOWN(0, "未知类型"),
    COMMON(1, "普通账号"),
    SITE_ADMIN(2, "管理员账号"),

    ;


    private Integer value;
    private String desc;

    AccountTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static AccountTypeEnum indexOf(Integer value){
        for(AccountTypeEnum item:values()){
            if(item.getValue().equals(value)){
                return item;
            }
        }
        return UNKNOWN;
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
}

package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/3/25 17:16
 */
public enum DataScopeEnum {
    UNKNOWN(0, "未知权限"),
    ONLY_ONESELF(1, "仅看自己"),
    ONLY_DEPART(2, "本部门权限"),
    DEPART_AND_SUB_DEPART(3, "本部门及以下部门权限"),
    ALL_ACCESS(4, "所有权限"),
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

    DataScopeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static DataScopeEnum indexOf(Integer value){
        for(DataScopeEnum item:values()){
            if(item.getValue().equals(value)){
                return item;
            }
        }
        return UNKNOWN;
    }
}

package com.spark.common.enums;

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
    ONLY_ONESELF(1, "仅本人数据"),
    ONLY_DEPART(2, "仅本部门数据"),
    DEPART_AND_SUB_DEPART(3, "本部门及以下部门数据"),
    ALL_ACCESS(4, "所有数据"),
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

package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/5/14 10:04
 */
public enum ModelTypeEnum {
    UNKNOWN(0, "未知模型"),
    LANGUAGE(1, "语言模型"),
    VECTOR(2, "向量模型"),
    RERANK(3, "排序模型"),
    ;
    private Integer type;

    private String desc;

    ModelTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static ModelTypeEnum indexOf(Integer value){
        for (ModelTypeEnum item:values()) {
            if (item.getType().equals(value)) {
                return item;
            }
        }
        return UNKNOWN;
    }
}

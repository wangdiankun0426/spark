package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/12/7 下午11:00
 */
public enum DocumentEventStatusEnum {
    UNKNOWN(0, "未知"),
    PENDING(1, "待处理"),
    SUCCESS(2, "成功"),
    FAIL(3, "失败"),
    NO_EXECUTE(4, "无需执行"),

    ;
    private Integer value;

    private String desc;

    public String getDesc() {
        return desc;
    }

    public Integer getValue() {
        return value;
    }

    DocumentEventStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static DocumentEventStatusEnum indexOf(Integer value){
        for(DocumentEventStatusEnum item:values()){
            if(item.getValue().equals(value)){
                return item;
            }
        }
        return UNKNOWN;
    }
}

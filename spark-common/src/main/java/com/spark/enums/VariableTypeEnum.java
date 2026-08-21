package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/8/6 22:26
 */
public enum VariableTypeEnum {
    BASE(1,"基础数据", "base"),
    FORM(2,"表单数据", "form"),
    FORM_TXT(3,"表单显示值", "formTxt"),
    NODE(4,"节点数据", "node"),

    ;

    private Integer value;
    private String desc;
    private String type;

    VariableTypeEnum(Integer value, String desc, String type){
        this.value = value;
        this.desc = desc;
        this.type = type;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public String getType() {return  type;}

    public static VariableTypeEnum indexOf(Integer value){
        for(VariableTypeEnum item:values()){
            if(item.getValue().equals(value)){
                return item;
            }
        }
        return null;
    }
}

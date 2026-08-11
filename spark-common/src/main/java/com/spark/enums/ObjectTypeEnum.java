package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 19:12
 */
public enum ObjectTypeEnum {
    UNKNOWN(0,"未知类型"),
    USER(1,"用户"),
    DEPARTMENT(2,"部门"),
    ROLE(4,"角色"),
    CHAT_SPACE(5,"聊天空间"),
    FORM(6,"表单"),
    DOCUMENT(9,"文档"),
    FLOW_TEMPLATE(11,"流程模板"),
    FLOW_INSTANCE(12,"流程实例"),
    AGENT(13,"智能体"),
    KNOWLEDGE(14,"知识库"),
    MODEL(15,"模型"),
    KG_GRAPH(16,"知识图谱"),
    WORKFLOW(17,"AI工作流"),
    WORKFLOW_INSTANCE(18,"AI工作流实例"),
    ;

    private Integer value;
    private String desc;

    ObjectTypeEnum(int value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static ObjectTypeEnum indexOf(Integer value){
        for(ObjectTypeEnum item:values()){
            if(item.getValue().equals(value)){
                return item;
            }
        }
        return UNKNOWN;
    }
}


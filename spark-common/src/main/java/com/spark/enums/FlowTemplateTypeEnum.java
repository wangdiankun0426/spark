package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/4 12:30
 * 流程模板节点类型枚举
 */
public enum FlowTemplateTypeEnum {
    START_EVENT("startEvent", "开始事件"),
    END_EVENT("endEvent", "结束事件"),
    EXCLUSIVE_GATEWAY("exclusiveGateway", "排他网关"),
    USER_TASK("userTask", "用户任务"),
    LLM_TASK("llmTask", "LLM模型"),
    ;

    private String value;

    private String name;

    FlowTemplateTypeEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static FlowTemplateTypeEnum findByValue(String value) {
        for (FlowTemplateTypeEnum typeEnum : FlowTemplateTypeEnum.values()) {
            if (typeEnum.getValue().equals(value)) {
                return typeEnum;
            }
        }
        return null;
    }
}
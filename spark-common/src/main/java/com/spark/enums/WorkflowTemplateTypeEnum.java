package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-27 10:00:00
 * AI工作流节点类型枚举
 */
public enum WorkflowTemplateTypeEnum {
    START_EVENT("startEvent", "开始事件"),
    END_EVENT("endEvent", "结束事件"),
    EXCLUSIVE_GATEWAY("exclusiveGateway", "排他网关"),
    PARALLEL_GATEWAY("parallelGateway", "并行网关"),
    LLM_TASK("llmTask", "LLM模型"),
    AGENT_TASK("agentTask", "Agent任务"),
    KB_SEARCH("kbSearch", "知识库检索"),
    KG_SEARCH("kgSearch", "知识图谱检索"),
    DOC_PARSE("docParse", "文档解析"),
    NOTIFY("notify", "发送通知"),
    KB_ARCHIVE("kbArchive", "知识库归档"),
    VARIABLE_OP("variableOp", "变量操作"),
    CODE_EXECUTE("codeExecute", "代码执行"),
    HTTP_REQUEST("httpRequest", "HTTP请求"),
    HUMAN_REVIEW("humanReview", "人工审核"),
    ;

    private String value;

    private String name;

    WorkflowTemplateTypeEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static WorkflowTemplateTypeEnum findByValue(String value) {
        for (WorkflowTemplateTypeEnum typeEnum : WorkflowTemplateTypeEnum.values()) {
            if (typeEnum.getValue().equals(value)) {
                return typeEnum;
            }
        }
        return null;
    }
}

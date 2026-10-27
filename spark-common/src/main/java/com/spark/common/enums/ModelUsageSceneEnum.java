package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/10/27 10:00:00
 * 模型调用场景枚举
 */
public enum ModelUsageSceneEnum {

    CHAT("CHAT", "模型对话"),
    AGENT_CHAT("AGENT_CHAT", "智能体对话"),
    WORKFLOW_LLM("WORKFLOW_LLM", "工作流模型节点"),
    WORKFLOW_AGENT("WORKFLOW_AGENT", "工作流智能体节点"),
    KG_REASON("KG_REASON", "图谱推理"),
    KG_EXTRACT("KG_EXTRACT", "图谱抽取"),
    KG_ENTITY_EXTRACT("KG_ENTITY_EXTRACT", "提问实体抽取"),
    QA_GENERATE("QA_GENERATE", "问答对生成"),
    DOCUMENT_VECTOR("DOCUMENT_VECTOR", "文档向量化"),
    RETRIEVE_VECTOR("RETRIEVE_VECTOR", "检索向量化"),
    RETRIEVE_RERANK("RETRIEVE_RERANK", "检索重排"),
    KG_ENTITY_VECTOR("KG_ENTITY_VECTOR", "图谱实体向量化"),
    OTHER("OTHER", "其他"),
    ;

    private final String code;
    private final String desc;

    ModelUsageSceneEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 按编码匹配枚举，未命中返回OTHER
     * @param code 场景编码
     * @return 场景枚举
     */
    public static ModelUsageSceneEnum indexOf(String code) {
        for (ModelUsageSceneEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return OTHER;
    }
}

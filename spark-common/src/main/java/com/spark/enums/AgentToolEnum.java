package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/5/16 22:22
 */
public enum AgentToolEnum {
    UNKNOWN("unknow", "未知工具"),
    SEARCH_KB("search_kb", "检索知识库"),
    SEARCH_KG("search_kg", "检索知识图谱"),
    ;

    private String type;

    private String desc;

    AgentToolEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static AgentToolEnum indexOf(String type) {
        for (AgentToolEnum value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return UNKNOWN;
    }
}

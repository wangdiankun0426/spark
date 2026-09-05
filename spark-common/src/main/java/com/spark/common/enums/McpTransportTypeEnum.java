package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/07/19 15:00:00
 * MCP传输类型枚举
 */
public enum McpTransportTypeEnum {
    UNKNOWN(0, "未知"),
    STDIO(1, "STDIO"),
    SSE(2, "SSE"),
    ;
    private Integer value;
    private String desc;

    McpTransportTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static McpTransportTypeEnum indexOf(Integer value) {
        for (McpTransportTypeEnum item : values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return UNKNOWN;
    }
}

package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-28 10:00:00
 * 分块策略枚举（使用LangChain4j内置分割器）
 */
public enum ChunkStrategyEnum {

    /**
     * 按段落分割
     */
    PARAGRAPH("paragraph", "按段落分割"),

    /**
     * 按行分割
     */
    LINE("line", "按行分割"),

    /**
     * 按句子分割
     */
    SENTENCE("sentence", "按句子分割"),

    /**
     * 按单词分割
     */
    WORD("word", "按单词分割"),

    /**
     * 按字符分割
     */
    CHARACTER("character", "按字符分割");

    private final String code;
    private final String desc;

    ChunkStrategyEnum(String code, String desc) {
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
     * 根据code查找枚举
     * @param code 策略代码
     * @return 枚举实例，未找到返回默认PARAGRAPH
     */
    public static ChunkStrategyEnum indexOf(String code) {
        if (code == null) {
            return PARAGRAPH;
        }
        for (ChunkStrategyEnum strategy : values()) {
            if (strategy.getCode().equals(code)) {
                return strategy;
            }
        }
        return PARAGRAPH;
    }
}

package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-28 10:00:00
 * 检索策略枚举
 */
public enum RetrieveStrategyEnum {

    /**
     * QA检索
     */
    QA("qa", "QA检索"),

    /**
     * 向量检索
     */
    VECTOR("vector", "向量检索"),

    /**
     * BM25检索
     */
    BM25("bm25", "BM25检索"),

    /**
     * 向量+BM25混合检索
     */
    VECTOR_BM25("vector+bm25", "向量+BM25混合检索"),

    /**
     * 向量+BM25+Rerank检索
     */
    VECTOR_BM25_RERANK("vector+bm25+rerank", "向量+BM25+Rerank检索"),

    /**
     * 空结果
     */
    EMPTY("empty", "无结果");

    private final String code;
    private final String desc;

    RetrieveStrategyEnum(String code, String desc) {
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
     * @return 枚举实例，未找到返回null
     */
    public static RetrieveStrategyEnum indexOf(String code) {
        if (code == null) {
            return null;
        }
        for (RetrieveStrategyEnum strategy : values()) {
            if (strategy.getCode().equals(code)) {
                return strategy;
            }
        }
        return null;
    }

}

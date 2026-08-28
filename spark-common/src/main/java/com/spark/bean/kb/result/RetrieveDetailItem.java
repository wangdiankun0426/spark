package com.spark.bean.kb.result;

import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-28 10:00:00
 * 检索详细结果项（包含分数信息）
 */
@Data
public class RetrieveDetailItem {

    /**
     * 文档ID
     */
    private Long docId;

    /**
     * 分块ID
     */
    private Long chunkId;

    /**
     * 分块索引
     */
    private Integer chunkIndex;

    /**
     * 分块内容
     */
    private String content;

    /**
     * 相似度分数
     */
    private Double score;

    /**
     * 来源类型(vector/bm25/qa)
     */
    private String sourceType;
}

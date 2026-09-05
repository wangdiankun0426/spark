package com.spark.common.bean.kb.result;

import com.spark.common.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-28 10:00:00
 * 检索单项结果
 */
@Data
public class RetrieveItemResult extends BaseResult {

    /**
     * 文档ID
     */
    private Long docId;

    /**
     * 文档名称
     */
    private String docName;

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
     * 排名
     */
    private Integer rank;

    /**
     * 来源类型(vector/bm25/qa)
     */
    private String sourceType;
}

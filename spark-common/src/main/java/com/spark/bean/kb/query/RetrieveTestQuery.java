package com.spark.bean.kb.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-28 10:00:00
 * 检索测试查询条件
 */
@Data
public class RetrieveTestQuery extends BaseQuery {

    /**
     * 知识库ID
     */
    private Long kbId;

    /**
     * 查询文本
     */
    private String query;

    /**
     * 召回数量
     */
    private Integer topK;

    /**
     * 最小相似度
     */
    private Double minSimilarity;

    /**
     * 是否启用QA检索
     */
    private Integer enableQa;

    /**
     * 是否启用Rerank
     */
    private Integer enableRerank;
}

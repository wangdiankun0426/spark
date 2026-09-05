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
 * @since 2026-07-17 10:00:00
 * 知识库结果
 */
@Data
public class KnowledgeResult extends BaseResult {

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 父块大小
     */
    private Integer parentChunkSize;

    /**
     * 子块大小
     */
    private Integer childChunkSize;

    /**
     * 父块重叠
     */
    private Integer parentOverlap;

    /**
     * 子块重叠
     */
    private Integer childOverlap;

    /**
     * 分块策略: paragraph/line/sentence/word/character
     */
    private String chunkStrategy;

    /**
     * 分块策略名称
     */
    private String chunkStrategyName;

    /**
     * 是否生成QA
     */
    private Integer enableQa;

    /**
     * 是否生成QA
     */
    private String enableQaName;

    /**
     * 子块召回数
     */
    private Integer retrieveTopK;

    /**
     * 最小相似度
     */
    private Double minSimilarity;

    /**
     * 向量模型id
     */
    private Long vectorModelId;

    /**
     * 排序模型id
     */
    private Long rerankModelId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 向量模型名称
     */
    private String vectorModelName;

    /**
     * 排序模型名称
     */
    private String rerankModelName;
}

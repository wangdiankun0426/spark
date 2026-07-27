package com.spark.bean.kb.entity;

import com.spark.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-17 10:00:00
 * 知识库实体
 */
@Data
public class Knowledge extends BaseEntity {

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
     * 是否生成QA
     */
    private Integer enableQa;

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

}

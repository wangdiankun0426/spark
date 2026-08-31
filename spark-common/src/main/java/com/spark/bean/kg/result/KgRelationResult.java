package com.spark.bean.kg.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 11:00:00
 * 知识图谱关系查询结果
 */
@Data
public class KgRelationResult extends BaseResult {

    /**
     * 图谱 id
     */
    private Long graphId;

    /**
     * 图谱名称
     */
    private String graphName;

    /**
     * 头实体 id
     */
    private Long headEntityId;

    /**
     * 头实体名称
     */
    private String headEntityName;

    /**
     * 尾实体 id
     */
    private Long tailEntityId;

    /**
     * 尾实体名称
     */
    private String tailEntityName;

    /**
     * 关系类型
     */
    private String relationType;

    /**
     * 关系权重
     */
    private Double weight;

    /**
     * 来源 id：接口创建存用户 id，文档解析存文档 id
     */
    private Long sourceId;

    /**
     * 来源类型
     */
    private Integer sourceType;

    /**
     * 来源类型名称
     */
    private String sourceTypeName;

    /**
     * 置信度分数
     */
    private Double confidence;

    /**
     * 状态（0-无效 1-有效）
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

}

package com.spark.common.bean.kg.entity;

import com.spark.common.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 11:00:00
 * 知识图谱实体（节点）
 */
@Data
public class KgEntity extends BaseEntity {

    /**
     * 图谱 id
     */
    private Long graphId;

    /**
     * 实体名称
     */
    private String name;

    /**
     * 实体类型
     */
    private String type;

    /**
     * 实体描述
     */
    private String description;

    /**
     * 来源 id：接口创建存用户 id，文档解析存文档 id
     */
    private Long sourceId;

    /**
     * 来源类型：1-LLM抽取 2-人工录入 3-规则抽取
     */
    private Integer sourceType;

    /**
     * 置信度分数 (0.0-1.0)
     */
    private Double confidence;

    /**
     * 审核状态：0-待审核 1-已审核 2-已拒绝
     */
    private Integer auditStatus;

    /**
     * 状态（0-无效 1-有效）
     */
    private Integer status;

}

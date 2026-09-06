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
     * 来源类型 KgSourceTypeEnum
     */
    private Integer sourceType;

    /**
     * 置信度分数 (0.0-1.0)
     */
    private Double confidence;

    /**
     * 审核状态 KgAuditStatusEnum
     */
    private Integer auditStatus;

    /**
     * 状态
     */
    private Integer status;

}

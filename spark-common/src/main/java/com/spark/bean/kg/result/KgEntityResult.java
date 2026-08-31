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
 * 知识图谱实体查询结果
 */
@Data
public class KgEntityResult extends BaseResult {

    /**
     * 图谱 id
     */
    private Long graphId;

    /**
     * 图谱名称
     */
    private String graphName;

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
     * 来源 id
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
     * 审核状态
     */
    private Integer auditStatus;

    /**
     * 审核状态名称
     */
    private String auditStatusName;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

}

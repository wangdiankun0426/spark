package com.spark.bean.kg.vo;

import com.spark.bean.base.BaseVO;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 11:00:00
 * 知识图谱关系 VO
 */
@Data
public class KgRelationVO extends BaseVO {

    /**
     * 图谱 id
     */
    private Long graphId;

    /**
     * 头实体 id
     */
    private Long headEntityId;

    /**
     * 尾实体 id
     */
    private Long tailEntityId;

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
     * 置信度分数
     */
    private Double confidence;

    /**
     * 状态
     */
    private Integer status;

}

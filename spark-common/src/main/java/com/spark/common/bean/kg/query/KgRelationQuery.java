package com.spark.common.bean.kg.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 11:00:00
 * 知识图谱关系查询条件
 */
@Data
public class KgRelationQuery extends BaseQuery {

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
     * 来源 id
     */
    private Long sourceId;

    /**
     * 状态
     */
    private Integer status;

}

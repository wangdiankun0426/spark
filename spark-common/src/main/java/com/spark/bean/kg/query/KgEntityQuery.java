package com.spark.bean.kg.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 11:00:00
 * 知识图谱实体查询条件
 */
@Data
public class KgEntityQuery extends BaseQuery {

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
     * 来源 id
     */
    private Long sourceId;

    /**
     * 来源类型
     */
    private Integer sourceType;

    /**
     * 审核状态
     */
    private Integer auditStatus;

    /**
     * 状态
     */
    private Integer status;

}

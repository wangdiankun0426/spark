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
     * 来源 id：接口创建存用户 id，文档解析存文档 id
     */
    private Long sourceId;

    /**
     * 状态（0-无效 1-有效）
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

}

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
 * 知识图谱实体
 */
@Data
public class KgGraph extends BaseEntity {

    /**
     * 图谱名称
     */
    private String name;

    /**
     * 图谱描述
     */
    private String description;

    /**
     * 实体类型 schema（JSON 数组，如 ["人物","组织","地点"]）
     */
    private String entityTypes;

    /**
     * 关系类型 schema（JSON 数组，如 ["就职于","投资","位于"]）
     */
    private String relationTypes;

    /**
     * 抽取模型 id
     */
    private Long extractModelId;

    /**
     * 块大小
     */
    private Integer chunkSize;

    /**
     * 块重叠
     */
    private Integer overlap;

    /**
     * 状态（0-禁用 1-启用）
     */
    private Integer status;

}

package com.spark.bean.kg.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 10:30:00
 * 图谱边（关系）数据结构，用于写入 Neo4j
 */
@Data
public class RelationEdge implements Serializable {

    /**
     * 关系 id（与 MySQL kg_relation.id 一致）
     */
    private Long id;

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
     * 扩展属性
     */
    private Map<String, Object> properties;

}

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
 * 知识图谱查询结果
 */
@Data
public class KgGraphResult extends BaseResult {

    /**
     * 所属部门 id
     */
    private Long deptId;

    /**
     * 图谱名称
     */
    private String name;

    /**
     * 图谱描述
     */
    private String description;

    /**
     * 实体类型 schema（JSON 数组）
     */
    private String entityTypes;

    /**
     * 关系类型 schema（JSON 数组）
     */
    private String relationTypes;

    /**
     * 抽取模型 id
     */
    private Long extractModelId;

    /**
     * 抽取模型名称
     */
    private String extractModelName;

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

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 实体数量
     */
    private Integer entityCount;

    /**
     * 关系数量
     */
    private Integer relationCount;

    /**
     * 文档数量
     */
    private Integer docCount;

}

package com.spark.common.bean.kg.result;

import com.spark.common.bean.base.BaseResult;
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
     * 状态
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;
}

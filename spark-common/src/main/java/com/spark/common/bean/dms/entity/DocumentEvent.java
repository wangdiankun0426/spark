package com.spark.common.bean.dms.entity;

import com.spark.common.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024-12-07 22:55:32
 */
@Data
public class DocumentEvent extends BaseEntity {

    /**
     * 文档id
     **/
    private Long docId;

    /**
     * 提取文件内容状态
     **/
    private Integer contentStatus;

    /**
     * 提取文件内容备注
     **/
    private String contentRemark;

    /**
     * 创建索引状态
     **/
    private Integer indexStatus;

    /**
     * 创建索引备注
     **/
    private String indexRemark;

    /**
     * 分块状态
     **/
    private Integer chunkStatus;

    /**
     * 分块备注
     **/
    private String chunkRemark;

    /**
     * 向量化状态
     **/
    private Integer vectorStatus;

    /**
     * 向量化备注
     **/
    private String vectorRemark;

    /**
     * 构建知识图谱状态
     **/
    private Integer graphStatus;

    /**
     * 构建知识图谱备注
     **/
    private String graphRemark;

}

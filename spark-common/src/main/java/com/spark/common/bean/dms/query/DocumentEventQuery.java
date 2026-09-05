package com.spark.common.bean.dms.query;

import com.spark.common.bean.base.BaseQuery;
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
public class DocumentEventQuery extends BaseQuery {

    /**
     * 文档id
     */
    private Long docId;

    /**
     * 提取文件内容状态
     **/
    private Integer contentStatus;

    /**
     * 提取文件内容状态
     */
    private Integer noContentStatus;

    /**
     * 创建索引状态
     */
    private Integer indexStatus;

    /**
     * 分块状态
     **/
    private Integer chunkStatus;

    /**
     * 分块状态（取反）
     */
    private Integer noChunkStatus;

    /**
     * 向量化状态
     **/
    private Integer vectorStatus;

    /**
     * 构建知识图谱状态
     **/
    private Integer graphStatus;

}

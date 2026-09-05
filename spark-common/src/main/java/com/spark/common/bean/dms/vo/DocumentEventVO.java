package com.spark.common.bean.dms.vo;

import com.spark.common.bean.base.BaseVO;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-18 10:00:00
 * 文档事件VO
 */
@Data
public class DocumentEventVO extends BaseVO {

    /**
     * 文档id
     */
    private Long docId;

    /**
     * 创建内容状态
     */
    private Integer contentStatus;

    /**
     * 创建索引状态
     */
    private Integer indexStatus;

    /**
     * 分块状态
     */
    private Integer chunkStatus;

    /**
     * 向量化状态
     */
    private Integer vectorStatus;


    /**
     * 构建知识图谱状态
     */
    private Integer graphStatus;

}

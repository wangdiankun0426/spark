package com.spark.common.bean.dms.result;

import com.spark.common.bean.base.BaseResult;
import com.spark.common.enums.DocumentEventStatusEnum;
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
public class DocumentEventResult extends BaseResult {

    /**
     * 文档id
     **/
    private Long docId;

    /**
     * 提取文件内容状态
     **/
    private Integer contentStatus;

    /**
     * 提取文件内容状态
     */
    private String contentStatusName;

    /**
     * 提取文件内容备注
     **/
    private String contentRemark;

    /**
     * 创建索引状态
     **/
    private Integer indexStatus;

    /**
     * 创建索引状态
     */
    private String indexStatusName;

    /**
     * 创建索引备注
     **/
    private String indexRemark;

    /**
     * 分块状态
     **/
    private Integer chunkStatus;

    /**
     * 分块状态
     **/
    private String chunkStatusName;

    /**
     * 分块备注
     **/
    private String chunkRemark;

    /**
     * 向量化状态
     **/
    private Integer vectorStatus;

    /**
     * 向量化状态
     **/
    private Integer vectorStatusName;

    /**
     * 向量化备注
     **/
    private String vectorRemark;

    /**
     * 构建知识图谱状态
     **/
    private Integer graphStatus;

    /**
     * 构建知识图谱状态
     **/
    private String graphStatusName;

    /**
     * 构建知识图谱备注
     **/
    private String graphRemark;

    public String getContentStatusName() {
        if (contentStatus == null) {
            return "";
        }
        DocumentEventStatusEnum documentEventStatusEnum = DocumentEventStatusEnum.indexOf(contentStatus);
        return documentEventStatusEnum.getDesc();
    }

    public String getIndexStatusName() {
        if (indexStatus == null) {
            return "";
        }
        DocumentEventStatusEnum documentEventStatusEnum = DocumentEventStatusEnum.indexOf(indexStatus);
        return documentEventStatusEnum.getDesc();
    }

    public String getChunkStatusName() {
        if (chunkStatus == null) {
            return "";
        }
        DocumentEventStatusEnum documentEventStatusEnum = DocumentEventStatusEnum.indexOf(chunkStatus);
        return documentEventStatusEnum.getDesc();
    }

    public String getVectorStatusName() {
        if (vectorStatus == null) {
            return "";
        }
        DocumentEventStatusEnum documentEventStatusEnum = DocumentEventStatusEnum.indexOf(vectorStatus);
        return documentEventStatusEnum.getDesc();
    }

    public String getGraphStatusName() {
        if (graphStatus == null) {
            return "";
        }
        DocumentEventStatusEnum documentEventStatusEnum = DocumentEventStatusEnum.indexOf(graphStatus);
        return documentEventStatusEnum.getDesc();
    }
}

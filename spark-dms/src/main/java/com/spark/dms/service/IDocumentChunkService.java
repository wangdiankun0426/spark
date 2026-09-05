package com.spark.dms.service;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.dms.query.DocumentQuery;

import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/7/16 13:26
 */
public interface IDocumentChunkService {

    /**
     * 分页查询文档分片列表
     * @param query 查询参数
     * @return 文档分片结果
     */
    ResultData<PageResult<Map>> pageDocumentChunkList(DocumentQuery query);

    /**
     * 分页查询文档分片QA列表
     * @param query 查询参数
     * @return 文档分片qa结果
     */
    ResultData<PageResult<Map>> pageDocumentChunksQAList(DocumentQuery query);

    /**
     * 查询文档分块统计信息
     * @param docId 文档ID
     * @return 统计信息
     */
    ResultData<Map<String, Object>> getDocumentChunkStats(Long docId);

    /**
     * 重新分块文档
     * @param docId 文档ID
     * @return 操作结果
     */
    ResultData<Void> rechunkDocument(Long docId);

    /**
     * 编辑分块内容
     * @param docId 文档ID
     * @param chunkIndex 分块序号
     * @param content 分块内容
     * @return 操作结果
     */
    ResultData<Void> updateChunk(Long docId, Integer chunkIndex, String content);

}

package com.spark.kb.service;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.kb.query.DocumentQuery;

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

}

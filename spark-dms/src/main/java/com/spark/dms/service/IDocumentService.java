package com.spark.dms.service;

import com.spark.common.bean.dms.query.DocumentQuery;
import com.spark.common.bean.dms.query.DocumentSearchQuery;
import com.spark.common.bean.dms.result.DocumentResult;
import com.spark.common.bean.dms.vo.DocumentVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;

import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024-12-07 19:08:01
 */
public interface IDocumentService {

    /**
     * 根据系统附件归档文档
     * @param attId 附件id
     * @param prtId 父ID
     * @return 归档结果
     */
    ResultData<Long> fileDocument(Long attId, Long prtId);

    /**
     * 修改文档
     * @param documentVO 修改的文档
     * @return 修改结果
     */
    ResultData<Void> updateDocument(DocumentVO documentVO);

    /**
     * 删除文档
     * @param documentVO 删除的文档
     * @return 删除结果
     */
    ResultData<Void> deleteDocument(DocumentVO documentVO);

    /**
     * 分页查询文档
     * @param query 查询参数
     * @return 文档列表
     */
    ResultData<PageResult<DocumentResult>> pageDocumentList(DocumentQuery query);

    /**
     * 查询文档详情
     * @param query 查询参数
     * @return 文档详情
     */
    ResultData<DocumentResult> queryDocumentDetail(DocumentQuery query);

    /**
     * 搜索文档
     * @param query 搜索参数
     * @return 搜索结果
     */
    ResultData<PageResult<Map>> searchDocument(DocumentSearchQuery query);

    /**
     * 下载文档
     * @param query 查询参数
     * @return 文档结果
     */
    ResultData<DocumentResult> downloadDocument(DocumentQuery query);

    /**
     * 批量删除文档
     * @param ids 文档ID列表
     * @return 删除结果
     */
    ResultData<Void> batchDeleteDocument(List<Long> ids);

    /**
     * 批量重新处理文档
     * @param ids 文档ID列表
     * @return 处理结果
     */
    ResultData<Void> batchReprocessDocument(List<Long> ids);
}

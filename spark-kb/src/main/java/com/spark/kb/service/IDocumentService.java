package com.spark.kb.service;

import com.spark.bean.kb.query.DocumentQuery;
import com.spark.bean.kb.query.DocumentSearchQuery;
import com.spark.bean.kb.result.DocumentResult;
import com.spark.bean.kb.vo.DocumentVO;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import org.springframework.web.multipart.MultipartFile;

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
     * 上传文档
     * @param file  文件
     * @param prtId 父ID
     * @return 上传结果
     */
    ResultData<Void> uploadDocument(MultipartFile file, Long prtId);

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
     * @param query 查询参数（含文档id和后缀）
     * @return 文档结果（含文件路径和文件名）
     */
    ResultData<DocumentResult> downloadDocument(DocumentQuery query);
}

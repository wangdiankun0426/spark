package com.spark.web.rest.controller.dms;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.dms.query.DocumentQuery;
import com.spark.dms.service.IDocumentChunkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/7/16 13:24
 */
@RestController
@RequestMapping("dms/document/chunk")
public class DocumentChunkController {
    @Autowired
    private IDocumentChunkService documentChunkService;

    /**
     * 分页查询文档分片列表
     * @param query 查询参数
     * @return 文档分片信息
     */
    @GetMapping("pageList")
    public ResultData<PageResult<Map>> pageDocumentChunkList(DocumentQuery query) {
        return documentChunkService.pageDocumentChunkList(query);
    }

    /**
     * 分页查询文档分片QA列表
     * @param query 查询参数
     * @return 文档分片qa
     */
    @GetMapping("pageQAList")
    public ResultData<PageResult<Map>> pageDocumentChunksQAList(DocumentQuery query) {
        return documentChunkService.pageDocumentChunksQAList(query);
    }

    /**
     * 查询文档分块统计信息
     * @param docId 文档ID
     * @return 统计信息
     */
    @GetMapping("stats")
    public ResultData<Map<String, Object>> getDocumentChunkStats(Long docId) {
        return documentChunkService.getDocumentChunkStats(docId);
    }

    /**
     * 重新分块文档
     * @param docId 文档ID
     * @return 操作结果
     */
    @GetMapping("rechunk")
    public ResultData<Void> rechunkDocument(Long docId) {
        return documentChunkService.rechunkDocument(docId);
    }

    /**
     * 编辑分块内容
     * @param docId 文档ID
     * @param chunkIndex 分块序号
     * @param content 分块内容
     * @return 操作结果
     */
    @PostMapping("update")
    public ResultData<Void> updateChunk(Long docId, Integer chunkIndex, String content) {
        return documentChunkService.updateChunk(docId, chunkIndex, content);
    }
}

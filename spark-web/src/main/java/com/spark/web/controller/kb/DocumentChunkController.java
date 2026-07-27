package com.spark.web.controller.kb;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.kb.query.DocumentQuery;
import com.spark.kb.service.IDocumentChunkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("kb/document/chunk")
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
}

package com.spark.web.controller.kb;

import com.spark.bean.kb.query.DocumentQuery;
import com.spark.bean.kb.query.DocumentSearchQuery;
import com.spark.bean.kb.result.DocumentResult;
import com.spark.bean.kb.vo.DocumentVO;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.kb.service.IDocumentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
@RestController
@RequestMapping("kb/document")
public class DocumentController {
    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    @Autowired
    private IDocumentService documentService;

    /**
     * 根据系统附件归档文档
     * @param attId 附件ID
     * @param prtId 父ID
     * @return 归档结果（含文档ID）
     */
    @PostMapping("fileDocument")
    public ResultData<Long> fileDocument(@RequestParam("attId") Long attId, @RequestParam("prtId") Long prtId) {
        return documentService.fileDocument(attId, prtId);
    }

    /**
     * 修改文档
     * @param documentVO 修改的文档
     * @return 修改结果
     */
    @PostMapping("update")
    public ResultData<Void> updateDocument(DocumentVO documentVO) {
        return documentService.updateDocument(documentVO);
    }

    /**
     * 删除文档
     * @param documentVO 删除的文档
     * @return 删除结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteDocument(DocumentVO documentVO) {
        return documentService.deleteDocument(documentVO);
    }

    /**
     * 分页查询文档
     * @param query 查询参数
     * @return 文档列表
     */
    @GetMapping("pageList")
    public ResultData<PageResult<DocumentResult>> pageDocumentList(DocumentQuery query) {
        return documentService.pageDocumentList(query);
    }

    /**
     * 查询文档详情
     * @param query 查询参数
     * @return 文档详情
     */
    @GetMapping("detail")
    public ResultData<DocumentResult> queryDocumentDetail(DocumentQuery query) {
        return documentService.queryDocumentDetail(query);
    }

    /**
     * 搜索文件
     * @param query 搜索参数
     * @return 搜索结果
     */
    @GetMapping("search")
    public ResultData<PageResult<Map>> searchDocument(DocumentSearchQuery query) {
        return documentService.searchDocument(query);
    }

    /**
     * 下载文档
     * @param response 响应
     * @param query 查询参数
     */
    @GetMapping("download")
    public void downloadDocument(HttpServletRequest request, HttpServletResponse response, DocumentQuery query) {
        ResultData<DocumentResult> result = documentService.downloadDocument(query);
        if (result.getCode() != ResultData.OK) {
            return;
        }
        DocumentResult documentResult = result.getData();
        try {
            File file = new File(documentResult.getPath());
            String filename = documentResult.getName();
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStream fis = new BufferedInputStream(fileInputStream);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            response.setCharacterEncoding("UTF-8");
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.addHeader("Content-Length", "" + file.length());
            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            outputStream.write(buffer);
            outputStream.flush();
        } catch (IOException ex) {
            logger.error("downloadDocument error", ex);
        }
    }

    /**
     * 批量删除文档
     * @param ids 文档ID列表
     * @return 删除结果
     */
    @PostMapping("batchDelete")
    public ResultData<Void> batchDeleteDocument(@RequestParam List<Long> ids) {
        return documentService.batchDeleteDocument(ids);
    }

    /**
     * 批量重新处理文档
     * @param ids 文档ID列表
     * @return 处理结果
     */
    @PostMapping("batchReprocess")
    public ResultData<Void> batchReprocessDocument(@RequestParam List<Long> ids) {
        return documentService.batchReprocessDocument(ids);
    }
}

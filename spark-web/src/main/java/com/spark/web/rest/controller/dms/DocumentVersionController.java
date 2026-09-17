package com.spark.web.rest.controller.dms;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.dms.query.DocumentVersionQuery;
import com.spark.common.bean.dms.result.DocumentVersionResult;
import com.spark.common.bean.dms.vo.DocumentVersionVO;
import com.spark.dms.service.IDocumentVersionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-17 10:00:00
 */
@RestController
@RequestMapping("dms/document/version")
public class DocumentVersionController {
    private static final Logger logger = LoggerFactory.getLogger(DocumentVersionController.class);
    @Autowired
    private IDocumentVersionService documentVersionService;

    /**
     * 回滚文档版本
     * @param documentVersionVO 版本参数
     * @return 处理结果
     */
    @PostMapping("rollback")
    public ResultData<Void> rollbackVersion(DocumentVersionVO documentVersionVO) {
        return documentVersionService.rollbackVersion(documentVersionVO);
    }

    /**
     * 删除文档历史版本
     * @param documentVersionVO 版本参数
     * @return 删除结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteVersion(DocumentVersionVO documentVersionVO) {
        return documentVersionService.deleteVersion(documentVersionVO);
    }

    /**
     * 分页查询文档版本
     * @param query 查询参数
     * @return 版本列表
     */
    @GetMapping("pageList")
    public ResultData<PageResult<DocumentVersionResult>> pageVersionList(DocumentVersionQuery query) {
        return documentVersionService.pageVersionList(query);
    }

    /**
     * 下载文档历史版本
     * @param response 响应
     * @param query 查询参数
     */
    @GetMapping("download")
    public void downloadVersion(HttpServletRequest request, HttpServletResponse response, DocumentVersionQuery query) {
        ResultData<DocumentVersionResult> result = documentVersionService.downloadVersion(query);
        if (result.getCode() != ResultData.OK) {
            return;
        }
        DocumentVersionResult versionResult = result.getData();
        try {
            File file = new File(versionResult.getPath());
            String filename = versionResult.getName();
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
            logger.error("downloadVersion error", ex);
        }
    }
}

package com.spark.web.rest.controller.dms;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.query.AttachmentQuery;
import com.spark.common.bean.sys.result.AttachmentResult;
import com.spark.dms.service.IAttachmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
 * @since 2026-08-20 14:45:00
 * 系统附件控制器
 */
@RestController
@RequestMapping("dms/attachment")
public class AttachmentController {
    private static final Logger logger = LoggerFactory.getLogger(AttachmentController.class);
    @Autowired
    private IAttachmentService attachmentService;

    /**
     * 查询系统附件详情
     * @param query 查询参数
     * @return 附件详情
     */
    @GetMapping("detail")
    private ResultData<AttachmentResult> queryAttachmentDetail(AttachmentQuery query) {
        return attachmentService.queryAttachmentDetail(query);
    }

    /**
     * 下载系统附件
     * @param response 响应
     * @param query 查询参数
     */
    @GetMapping("download")
    private void downloadAttachment(HttpServletRequest request, HttpServletResponse response, AttachmentQuery query) {
        ResultData<AttachmentResult> result = attachmentService.downloadAttachment(query);
        if (result.getCode() != ResultData.OK) {
            return;
        }
        AttachmentResult attachmentResult = result.getData();
        try {
            File file = new File(attachmentResult.getPath());
            String filename = attachmentResult.getName();
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
            logger.error("downloadSystemAttachment error", ex);
        }
    }
}

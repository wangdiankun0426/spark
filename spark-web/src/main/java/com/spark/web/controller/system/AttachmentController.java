package com.spark.web.controller.system;

import com.spark.bean.base.ResultData;
import com.spark.bean.system.query.AttachmentQuery;
import com.spark.bean.system.result.AttachmentResult;
import com.spark.manage.system.IAttachmentService;
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
import org.springframework.web.multipart.MultipartFile;

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
@RequestMapping("system/attachment")
public class AttachmentController {
    private static final Logger logger = LoggerFactory.getLogger(AttachmentController.class);
    @Autowired
    private IAttachmentService attachmentService;

    /**
     * 上传系统附件
     * @param file 文件
     * @return 上传结果
     */
    @PostMapping("upload")
    private ResultData<AttachmentResult> uploadAttachment(@RequestParam("file") MultipartFile file) {
        return attachmentService.uploadAttachment(file);
    }

    /**
     * 初始化分片上传会话
     * @param fileName 文件名称
     * @param fileSize 文件大小
     * @param totalChunks 分片总数
     * @return 上传会话id
     */
    @PostMapping("upload/init")
    private ResultData<String> initUploadChunk(@RequestParam("fileName") String fileName, @RequestParam("fileSize") Long fileSize, @RequestParam("totalChunks") Integer totalChunks) {
        return attachmentService.initUploadChunk(fileName, fileSize, totalChunks);
    }

    /**
     * 上传单个分片
     * @param file 分片文件
     * @param uploadId 上传会话id
     * @param chunkIndex 分片序号
     * @return 上传结果
     */
    @PostMapping("upload/chunk")
    private ResultData<Void> uploadChunk(@RequestParam("file") MultipartFile file, @RequestParam("uploadId") String uploadId, @RequestParam("chunkIndex") Integer chunkIndex) {
        return attachmentService.uploadChunk(uploadId, chunkIndex, file);
    }

    /**
     * 查询已上传的分片序号
     * @param uploadId 上传会话id
     * @return 分片序号列表
     */
    @GetMapping("upload/chunks")
    private ResultData<List<Integer>> queryUploadChunks(@RequestParam("uploadId") String uploadId) {
        return attachmentService.queryUploadChunks(uploadId);
    }

    /**
     * 合并分片并保存为系统附件
     * @param uploadId 上传会话id
     * @return 附件结果
     */
    @PostMapping("upload/merge")
    private ResultData<AttachmentResult> mergeUploadChunk(@RequestParam("uploadId") String uploadId) {
        return attachmentService.mergeUploadChunk(uploadId);
    }

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

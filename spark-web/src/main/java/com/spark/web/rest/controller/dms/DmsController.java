package com.spark.web.rest.controller.dms;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.result.AttachmentResult;
import com.spark.dms.service.IDmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-17 11:00:00
 * 文档管理通用控制器
 */
@RestController
@RequestMapping("dms")
public class DmsController {
    @Autowired
    private IDmsService dmsService;

    /**
     * 通用上传文件，携带docId生成文档新版本，归档目标为知识库或知识图谱时直接创建文档，否则保存为附件
     * @param file 文件
     * @param prtId 归档目标父id
     * @param docId 文档id
     * @return 上传结果
     */
    @PostMapping("upload")
    private ResultData<AttachmentResult> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam(value = "prtId", required = false) Long prtId, @RequestParam(value = "docId", required = false) Long docId) {
        return dmsService.uploadFile(file, prtId, docId);
    }

    /**
     * 初始化分片上传会话
     * @param fileName 文件名称
     * @param fileSize 文件大小
     * @param totalChunks 分片总数
     * @param prtId 归档目标父id
     * @param docId 文档id
     * @return 上传会话id
     */
    @PostMapping("upload/init")
    private ResultData<String> initUpload(@RequestParam("fileName") String fileName, @RequestParam("fileSize") Long fileSize, @RequestParam("totalChunks") Integer totalChunks, @RequestParam(value = "prtId", required = false) Long prtId, @RequestParam(value = "docId", required = false) Long docId) {
        return dmsService.initUpload(fileName, fileSize, totalChunks, prtId, docId);
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
        return dmsService.uploadChunk(uploadId, chunkIndex, file);
    }

    /**
     * 查询已上传的分片序号
     * @param uploadId 上传会话id
     * @return 分片序号列表
     */
    @GetMapping("upload/chunks")
    private ResultData<List<Integer>> queryUploadChunks(@RequestParam("uploadId") String uploadId) {
        return dmsService.queryUploadChunks(uploadId);
    }

    /**
     * 合并分片，归档目标为知识库或知识图谱时直接创建文档，否则保存为附件
     * @param uploadId 上传会话id
     * @return 上传结果
     */
    @PostMapping("upload/merge")
    private ResultData<AttachmentResult> mergeUpload(@RequestParam("uploadId") String uploadId) {
        return dmsService.mergeUpload(uploadId);
    }
}

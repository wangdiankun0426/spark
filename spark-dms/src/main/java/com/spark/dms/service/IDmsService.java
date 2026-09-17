package com.spark.dms.service;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.result.AttachmentResult;
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
 * 文档管理通用服务
 */
public interface IDmsService {

    /**
     * 通用上传文件，携带docId生成文档新版本，归档目标为知识库或知识图谱时直接创建文档，否则保存为附件
     * @param file 文件
     * @param prtId 归档目标父id
     * @param docId 文档id
     * @return 上传结果
     */
    ResultData<AttachmentResult> uploadFile(MultipartFile file, Long prtId, Long docId);

    /**
     * 初始化分片上传会话
     * @param fileName 文件名称
     * @param fileSize 文件大小
     * @param totalChunks 分片总数
     * @param prtId 归档目标父id
     * @param docId 文档id
     * @return 上传会话id
     */
    ResultData<String> initUpload(String fileName, Long fileSize, Integer totalChunks, Long prtId, Long docId);

    /**
     * 上传单个分片
     * @param uploadId 上传会话id
     * @param chunkIndex 分片序号
     * @param file 分片文件
     * @return 上传结果
     */
    ResultData<Void> uploadChunk(String uploadId, Integer chunkIndex, MultipartFile file);

    /**
     * 查询已上传的分片序号
     * @param uploadId 上传会话id
     * @return 分片序号列表
     */
    ResultData<List<Integer>> queryUploadChunks(String uploadId);

    /**
     * 合并分片，归档目标为知识库或知识图谱时直接创建文档，否则保存为附件
     * @param uploadId 上传会话id
     * @return 上传结果
     */
    ResultData<AttachmentResult> mergeUpload(String uploadId);
}

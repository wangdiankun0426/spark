package com.spark.manage.system;

import com.spark.bean.base.ResultData;
import com.spark.bean.system.query.AttachmentQuery;
import com.spark.bean.system.result.AttachmentResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 14:40:00
 * 系统附件服务
 */
public interface IAttachmentService {

    /**
     * 上传系统附件
     * @param file 文件
     * @return 上传结果
     */
    ResultData<AttachmentResult> uploadAttachment(MultipartFile file);

    /**
     * 下载系统附件
     * @param query 查询参数
     * @return 附件结果
     */
    ResultData<AttachmentResult> downloadAttachment(AttachmentQuery query);

    /**
     * 查询系统附件详情
     * @param query 查询参数
     * @return 附件详情
     */
    ResultData<AttachmentResult> queryAttachmentDetail(AttachmentQuery query);

    /**
     * 初始化分片上传会话
     * @param fileName 文件名称
     * @param fileSize 文件大小
     * @param totalChunks 分片总数
     * @return 上传会话id
     */
    ResultData<String> initUploadChunk(String fileName, Long fileSize, Integer totalChunks);

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
     * 合并分片并保存为系统附件
     * @param uploadId 上传会话id
     * @return 附件结果
     */
    ResultData<AttachmentResult> mergeUploadChunk(String uploadId);
}

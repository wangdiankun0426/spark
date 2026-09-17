package com.spark.dms.service;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.query.AttachmentQuery;
import com.spark.common.bean.sys.result.AttachmentResult;
import org.springframework.web.multipart.MultipartFile;

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
     * 保存附件记录
     * @param fileName 文件名称
     * @param fileExt 文件后缀
     * @param fileSize 文件大小
     * @param filePath 存储路径
     * @return 附件结果
     */
    ResultData<AttachmentResult> saveAttachment(String fileName, String fileExt, Long fileSize, String filePath);

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
}

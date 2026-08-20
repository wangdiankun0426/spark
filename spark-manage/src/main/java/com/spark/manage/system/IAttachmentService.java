package com.spark.manage.system;

import com.spark.bean.base.ResultData;
import com.spark.bean.system.query.AttachmentQuery;
import com.spark.bean.system.result.AttachmentResult;
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
     * @return 上传结果（含附件id、名称、后缀）
     */
    ResultData<AttachmentResult> uploadAttachment(MultipartFile file);

    /**
     * 下载系统附件
     * @param query 查询参数（含附件id）
     * @return 附件结果（含存储路径和文件名）
     */
    ResultData<AttachmentResult> downloadAttachment(AttachmentQuery query);

    /**
     * 查询系统附件详情
     * @param query 查询参数（含附件id）
     * @return 附件详情（含名称、后缀、大小）
     */
    ResultData<AttachmentResult> queryAttachmentDetail(AttachmentQuery query);
}

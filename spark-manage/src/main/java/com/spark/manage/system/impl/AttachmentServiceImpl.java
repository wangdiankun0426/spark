package com.spark.manage.system.impl;

import com.spark.bean.base.ResultData;
import com.spark.bean.base.SessionHolder;
import com.spark.bean.system.entity.Attachment;
import com.spark.bean.system.query.AttachmentQuery;
import com.spark.bean.system.result.AttachmentResult;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.dao.system.AttachmentDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.ObjectTypeEnum;
import com.spark.enums.OperateTypeEnum;
import com.spark.manage.BaseService;
import com.spark.manage.system.IAttachmentService;
import com.spark.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 14:40:00
 * 系统附件服务实现
 */
@Service
public class AttachmentServiceImpl extends BaseService<AttachmentQuery, AttachmentResult> implements IAttachmentService {
    private final static Logger logger = LoggerFactory.getLogger(AttachmentServiceImpl.class);
    @Autowired
    private AttachmentDao attachmentDao;
    @Value("${docs.file.path}")
    private String docsPath;

    /**
     * 上传系统附件
     * @param file 文件
     * @return 上传结果（含附件id、名称、后缀）
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.ATTACHMENT_UPLOAD)
    public ResultData<AttachmentResult> uploadAttachment(MultipartFile file) {
        ResultData<AttachmentResult> result = new ResultData<>();
        if (file == null) {
            result.setErrorCode(ErrorCodeEnum.UPLOAD_FILE_NOT_EXIST);
            return result;
        }
        String filename = file.getOriginalFilename();
        String fileExt = FileUtil.getFileExt(filename);
        String filePath = FileUtil.generateFilePath(docsPath, UUID.randomUUID() + "." + fileExt);
        if (filePath == null) {
            result.setErrorCode(ErrorCodeEnum.FILE_CREATE_FAIL);
            return result;
        }
        // 写入磁盘
        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Attachment attachment = new Attachment();
        Long attId = super.genObjectId(ObjectTypeEnum.ATTACHMENT);
        attachment.setId(attId);
        attachment.setName(filename);
        attachment.setExt(fileExt);
        attachment.setSize(file.getSize());
        attachment.setPath(filePath);
        attachment.setOwnerId(SessionHolder.getCurrentUserId());
        int count = attachmentDao.insertDB(attachment);
        if (count < 1) {
            logger.error("uploadAttachment error, insert db fail");
            return result;
        }
        AttachmentResult attachmentResult = new AttachmentResult();
        attachmentResult.setId(attId);
        attachmentResult.setName(filename);
        attachmentResult.setExt(fileExt);
        attachmentResult.setSize(file.getSize());
        attachmentResult.setOwnerId(SessionHolder.getCurrentUserId());
        attachmentResult.setOwnerName(super.getObjName(SessionHolder.getCurrentUserId()));
        attachmentResult.setDeptId(attachment.getDeptId());
        result.setData(attachmentResult);
        result.setObjId(attId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 下载系统附件
     * @param query 查询参数（含附件id）
     * @return 附件结果（含存储路径和文件名）
     */
    @Override
    public ResultData<AttachmentResult> downloadAttachment(AttachmentQuery query) {
        ResultData<AttachmentResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        AttachmentResult attachmentResult = attachmentDao.queryAttachment(query);
        if (attachmentResult == null) {
            result.setErrorCode(ErrorCodeEnum.FILE_NOT_EXIST);
            return result;
        }
        File file = new File(attachmentResult.getPath());
        if (!file.exists()) {
            logger.error("downloadAttachment file not exist, filePath={}", attachmentResult.getPath());
            result.setErrorCode(ErrorCodeEnum.FILE_NOT_EXIST);
            return result;
        }
        result.setData(attachmentResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询系统附件详情
     * @param query 查询参数（含附件id）
     * @return 附件详情（含名称、后缀、大小）
     */
    @Override
    public ResultData<AttachmentResult> queryAttachmentDetail(AttachmentQuery query) {
        ResultData<AttachmentResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        AttachmentResult attachmentResult = attachmentDao.queryAttachment(query);
        if (attachmentResult == null) {
            result.setErrorCode(ErrorCodeEnum.FILE_NOT_EXIST);
            return result;
        }
        // 存储路径不对外暴露
        attachmentResult.setPath(null);
        result.setData(attachmentResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询最大id
     * @return 最大id
     */
    @Override
    protected Long queryMaxId() {
        return attachmentDao.queryAttachmentMaxId();
    }
}

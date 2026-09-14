package com.spark.dms.service.impl;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.sys.entity.Attachment;
import com.spark.common.bean.sys.query.AttachmentQuery;
import com.spark.common.bean.sys.result.AttachmentResult;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.common.constant.ObjectCacheKey;
import com.spark.dao.dms.AttachmentDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.dms.service.IAttachmentService;
import com.spark.manage.BaseService;
import com.spark.config.redis.RedisService;
import com.spark.common.utils.FileUtil;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@LogPrint
public class AttachmentServiceImpl extends BaseService<AttachmentQuery, AttachmentResult> implements IAttachmentService {
    private final static Logger logger = LoggerFactory.getLogger(AttachmentServiceImpl.class);
    private final static long MAX_CHUNK_SIZE = 1L * 1024 * 1024;
    // 元数据过期时间：24小时（秒
    private final static long META_EXPIRE_SECONDS = 24 * 60 * 60;
    @Autowired
    private AttachmentDao attachmentDao;
    @Autowired
    private RedisService redisService;
    @Value("${docs.file.path}")
    private String docsPath;
    @Autowired
    private FileUtil fileUtil;

    /**
     * 上传系统附件
     * @param file 文件
     * @return 上传结果（含附件id、名称、后缀）
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.ATTACHMENT_UPLOAD)
    public ResultData<AttachmentResult> uploadAttachment(MultipartFile file) {
        ResultData<AttachmentResult> result = new ResultData<>();
        if (file == null) {
            result.setErrorCode(ErrorCodeEnum.UPLOAD_FILE_NOT_EXIST);
            return result;
        }
        String filename = file.getOriginalFilename();
        String fileExt = fileUtil.getFileExt(filename);
        String filePath = fileUtil.generateFilePath(docsPath, UUID.randomUUID() + "." + fileExt);
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
        return saveAttachment(filename, fileExt, file.getSize(), filePath);
    }

    /**
     * 下载系统附件
     * @param query 查询参数（含附件id）
     * @return 附件结果（含存储路径和文件名）
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.ATTACHMENT_DOWNLOAD)
    public ResultData<AttachmentResult> downloadAttachment(AttachmentQuery query) {
        ResultData<AttachmentResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        AttachmentResult attachmentResult = attachmentDao.queryAttachment(query);
        if (attachmentResult == null) {
            result.setErrorCode(ErrorCodeEnum.ATTACHMENT_NOT_EXIST);
            return result;
        }
        File file = new File(attachmentResult.getPath());
        if (!file.exists()) {
            logger.error("downloadAttachment file not exist, filePath={}", attachmentResult.getPath());
            result.setErrorCode(ErrorCodeEnum.FILE_NOT_EXIST);
            return result;
        }
        result.setData(attachmentResult);
        result.setObjId(attachmentResult.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询系统附件详情
     * @param query 查询参数（含附件id）
     * @return 附件详情（含名称、后缀、大小）
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.ATTACHMENT_DETAIL)
    public ResultData<AttachmentResult> queryAttachmentDetail(AttachmentQuery query) {
        ResultData<AttachmentResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        AttachmentResult attachmentResult = attachmentDao.queryAttachment(query);
        if (attachmentResult == null) {
            result.setErrorCode(ErrorCodeEnum.ATTACHMENT_NOT_EXIST);
            return result;
        }
        // 存储路径不对外暴露
        attachmentResult.setPath(null);
        result.setData(attachmentResult);
        result.setObjId(attachmentResult.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 初始化分片上传会话
     * @param fileName 文件名称
     * @param fileSize 文件大小
     * @param totalChunks 分片总数
     * @return 上传会话id
     */
    @Override
    public ResultData<String> initUploadChunk(String fileName, Long fileSize, Integer totalChunks) {
        ResultData<String> result = new ResultData<>();
        if (StringUtil.isBlank(fileName) || fileSize == null || fileSize < 1 || totalChunks == null || totalChunks < 1) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String uploadId = UUID.randomUUID().toString().replace("-", "");
        File chunkDir = buildChunkDir(uploadId);
        if (!chunkDir.mkdirs()) {
            logger.error("initUploadChunk error, mkdir fail, chunkDir={}", chunkDir.getPath());
            result.setErrorCode(ErrorCodeEnum.FILE_CREATE_FAIL);
            return result;
        }
        // 将元数据写入 Redis
        Map<String, Object> meta = new HashMap<>();
        meta.put("fileName", fileName);
        meta.put("fileSize", fileSize);
        meta.put("totalChunks", totalChunks);
        String metaJson = JsonUtil.toString(meta);
        redisService.setStr(ObjectCacheKey.UPLOAD_CHUNK_META + uploadId, metaJson, META_EXPIRE_SECONDS);
        result.setData(uploadId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 上传单个分片
     * @param uploadId 上传会话id
     * @param chunkIndex 分片序号
     * @param file 分片文件
     * @return 上传结果
     */
    @Override
    public ResultData<Void> uploadChunk(String uploadId, Integer chunkIndex, MultipartFile file) {
        ResultData<Void> result = new ResultData<>();
        if (StringUtil.isBlank(uploadId) || chunkIndex == null || file == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Map<String, String> meta = this.readUploadMeta(uploadId);
        if (meta == null) {
            result.setErrorCode(ErrorCodeEnum.UPLOAD_SESSION_NOT_EXIST);
            return result;
        }
        int totalChunks = Integer.parseInt(meta.get("totalChunks"));
        if (chunkIndex < 0 || chunkIndex >= totalChunks) {
            result.setErrorCode(ErrorCodeEnum.UPLOAD_CHUNK_INDEX_INVALID);
            return result;
        }
        if (file.getSize() > MAX_CHUNK_SIZE) {
            result.setErrorCode(ErrorCodeEnum.UPLOAD_CHUNK_SIZE_EXCEED);
            return result;
        }
        File chunkDir = buildChunkDir(uploadId);
        File partFile = new File(chunkDir, chunkIndex + ".part");
        // 同序号分片覆盖写
        if (partFile.exists() && !partFile.delete()) {
            logger.error("uploadChunk error, delete old part fail, partPath={}", partFile.getPath());
            result.setErrorCode(ErrorCodeEnum.FILE_CREATE_FAIL);
            return result;
        }
        try {
            file.transferTo(partFile);
        } catch (IOException e) {
            logger.error("uploadChunk error, transfer fail, partPath={}", partFile.getPath(), e);
            result.setErrorCode(ErrorCodeEnum.FILE_CREATE_FAIL);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询已上传的分片序号
     * @param uploadId 上传会话id
     * @return 分片序号列表
     */
    @Override
    public ResultData<List<Integer>> queryUploadChunks(String uploadId) {
        ResultData<List<Integer>> result = new ResultData<>();
        if (StringUtil.isBlank(uploadId)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Map<String, String> meta = this.readUploadMeta(uploadId);
        if (meta == null) {
            result.setErrorCode(ErrorCodeEnum.UPLOAD_SESSION_NOT_EXIST);
            return result;
        }
        File chunkDir = buildChunkDir(uploadId);
        File[] partFiles = chunkDir.listFiles((dir, name) -> name.endsWith(".part"));
        List<Integer> chunkIndexList = new ArrayList<>();
        if (partFiles != null) {
            for (File partFile : partFiles) {
                Integer chunkIndex = parseChunkIndex(partFile.getName());
                if (chunkIndex != null) {
                    chunkIndexList.add(chunkIndex);
                }
            }
        }
        Collections.sort(chunkIndexList);
        result.setData(chunkIndexList);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 合并分片并保存为系统附件
     * @param uploadId 上传会话id
     * @return 附件结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.ATTACHMENT_UPLOAD)
    public ResultData<AttachmentResult> mergeUploadChunk(String uploadId) {
        ResultData<AttachmentResult> result = new ResultData<>();
        if (StringUtil.isBlank(uploadId)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Map<String, String> meta = this.readUploadMeta(uploadId);
        if (meta == null) {
            result.setErrorCode(ErrorCodeEnum.UPLOAD_SESSION_NOT_EXIST);
            return result;
        }
        int totalChunks = Integer.parseInt(meta.get("totalChunks"));
        String fileName = meta.get("fileName");
        Long fileSize = Long.parseLong(meta.get("fileSize"));
        File chunkDir = buildChunkDir(uploadId);
        for (int i = 0; i < totalChunks; i++) {
            File partFile = new File(chunkDir, i + ".part");
            if (!partFile.exists()) {
                logger.error("mergeUploadChunk error, part not exist, uploadId={}, chunkIndex={}", uploadId, i);
                result.setErrorCode(ErrorCodeEnum.UPLOAD_CHUNK_NOT_COMPLETE);
                return result;
            }
        }
        String fileExt = fileUtil.getFileExt(fileName);
        String filePath = fileUtil.generateFilePath(docsPath, UUID.randomUUID() + "." + fileExt);
        if (filePath == null) {
            result.setErrorCode(ErrorCodeEnum.FILE_CREATE_FAIL);
            return result;
        }
        boolean bo = mergeChunkFiles(chunkDir, totalChunks, filePath);
        if (!bo) {
            result.setErrorCode(ErrorCodeEnum.FILE_CREATE_FAIL);
            return result;
        }
        // 合并成功后删除临时分片目录
        fileUtil.deleteDir(chunkDir);
        // 删除 Redis 中的会话元数据
        redisService.del(ObjectCacheKey.UPLOAD_CHUNK_META + uploadId);
        return saveAttachment(fileName, fileExt, fileSize, filePath);
    }

    /**
     * 保存附件记录并返回附件结果
     * @param fileName 文件名称
     * @param fileExt 文件后缀
     * @param fileSize 文件大小
     * @param filePath 存储路径
     * @return 附件结果
     */
    private ResultData<AttachmentResult> saveAttachment(String fileName, String fileExt, Long fileSize, String filePath) {
        ResultData<AttachmentResult> result = new ResultData<>();
        Attachment attachment = new Attachment();
        Long attId = super.genObjectId(ObjectTypeEnum.ATTACHMENT);
        attachment.setId(attId);
        attachment.setName(fileName);
        attachment.setExt(fileExt);
        attachment.setSize(fileSize);
        attachment.setPath(filePath);
        attachment.setOwnerId(SessionHolder.getCurrentUserId());
        int count = attachmentDao.insertDB(attachment);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        AttachmentResult attachmentResult = new AttachmentResult();
        attachmentResult.setId(attId);
        attachmentResult.setName(fileName);
        attachmentResult.setExt(fileExt);
        attachmentResult.setSize(fileSize);
        attachmentResult.setOwnerId(attachment.getOwnerId());
        attachmentResult.setOwnerName(super.getObjName(attachment.getOwnerId()));
        attachmentResult.setDeptId(attachment.getDeptId());
        result.setData(attachmentResult);
        result.setObjId(attId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 构造分片临时目录
     * @param uploadId 上传会话id
     * @return 分片临时目录
     */
    private File buildChunkDir(String uploadId) {
        String chunkDirPath = docsPath + File.separator + "chunk" + File.separator + uploadId;
        return new File(chunkDirPath);
    }

    /**
     * 读取上传会话元数据
     * @param uploadId 上传会话id
     * @return 元数据，会话不存在时返回 null
     */
    private Map<String, String> readUploadMeta(String uploadId) {
        if (StringUtil.isBlank(uploadId)) {
            return null;
        }
        String redisKey = ObjectCacheKey.UPLOAD_CHUNK_META + uploadId;
        String metaJson = redisService.getValue(redisKey);
        if (StringUtil.isBlank(metaJson)) {
            return null;
        }
        return JsonUtil.parseMap(metaJson);
    }

    /**
     * 顺序合并分片文件
     * @param chunkDir 分片临时目录
     * @param totalChunks 分片总数
     * @param destPath 合并目标路径
     * @return 是否成功
     */
    private boolean mergeChunkFiles(File chunkDir, int totalChunks, String destPath) {
        byte[] buffer = new byte[8192];
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destPath))) {
            for (int i = 0; i < totalChunks; i++) {
                File partFile = new File(chunkDir, i + ".part");
                try (InputStream inputStream = new BufferedInputStream(new FileInputStream(partFile))) {
                    int len = inputStream.read(buffer);
                    while (len != -1) {
                        outputStream.write(buffer, 0, len);
                        len = inputStream.read(buffer);
                    }
                }
            }
            outputStream.flush();
        } catch (IOException e) {
            logger.error("mergeChunkFiles error, destPath={}", destPath, e);
            return false;
        }
        return true;
    }

    /**
     * 解析分片文件名中的序号
     * @param partFileName 分片文件名
     * @return 分片序号，格式非法时返回 null
     */
    private Integer parseChunkIndex(String partFileName) {
        String indexText = partFileName.replace(".part", "");
        try {
            return Integer.valueOf(indexText);
        } catch (NumberFormatException e) {
            return null;
        }
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

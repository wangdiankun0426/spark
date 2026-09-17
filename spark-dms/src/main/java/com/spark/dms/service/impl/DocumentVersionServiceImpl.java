package com.spark.dms.service.impl;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.dms.entity.Document;
import com.spark.common.bean.dms.entity.DocumentEvent;
import com.spark.common.bean.dms.entity.DocumentVersion;
import com.spark.common.bean.dms.query.DocumentEventQuery;
import com.spark.common.bean.dms.query.DocumentQuery;
import com.spark.common.bean.dms.query.DocumentVersionQuery;
import com.spark.common.bean.dms.result.DocumentEventResult;
import com.spark.common.bean.dms.result.DocumentResult;
import com.spark.common.bean.dms.result.DocumentVersionResult;
import com.spark.common.bean.dms.vo.DocumentVersionVO;
import com.spark.common.enums.DocumentEventStatusEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.utils.FileUtil;
import com.spark.common.utils.StringUtil;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.dao.dms.DocumentDao;
import com.spark.dao.dms.DocumentEventDao;
import com.spark.dao.dms.DocumentVersionDao;
import com.spark.dms.service.IDocumentVersionService;
import com.spark.manage.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-17 10:00:00
 */
@Service
@LogPrint
public class DocumentVersionServiceImpl extends BaseService<DocumentVersionQuery, DocumentVersionResult> implements IDocumentVersionService {
    private final static Logger logger = LoggerFactory.getLogger(DocumentVersionServiceImpl.class);
    @Autowired
    private DocumentVersionDao documentVersionDao;
    @Autowired
    private DocumentDao documentDao;
    @Autowired
    private DocumentEventDao documentEventDao;
    @Autowired
    private FileUtil fileUtil;
    @Value("${docs.file.path}")
    private String docsPath;

    /**
     * 上传文档新版本
     * @param docId 文档id
     * @param name 名称
     * @param ext 拓展名
     * @param size 大小
     * @param path 存储路径
     * @return 处理结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.DOCUMENT_VERSION_INSERT)
    public ResultData<Void> uploadVersion(Long docId, String name, String ext, Long size, String path) {
        ResultData<Void> result = new ResultData<>();
        if (docId == null || StringUtil.isBlank(name) || StringUtil.isBlank(ext) || size == null || StringUtil.isBlank(path)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        DocumentResult documentResult = this.queryDocument(docId);
        if (documentResult == null) {
            result.setErrorCode(ErrorCodeEnum.DOCUMENT_NOT_EXIST);
            return result;
        }
        // 新版本文件格式必须与原文档一致
        if (!ext.equalsIgnoreCase(documentResult.getExt())) {
            result.setErrorCode(ErrorCodeEnum.DOCUMENT_VERSION_EXT_NOT_MATCH);
            return result;
        }
        // 更新文档为新版本
        Integer versionNo = this.currentVersionNo(documentResult) + 1;
        Document document = new Document();
        document.setId(docId);
        document.setName(name);
        document.setSize(size);
        document.setPath(path);
        document.setExt(ext);
        document.setVersionNo(versionNo);
        int count = documentDao.updateDBById(document);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        // 新版本记录入库
        count = this.insertVersion(docId, versionNo, name, size, path, ext);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        // 重置文档事件触发重新处理
        this.resetEvent(documentResult);
        result.setObjId(docId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 回滚文档版本
     * @param documentVersionVO 版本参数
     * @return 处理结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.DOCUMENT_VERSION_ROLLBACK)
    public ResultData<Void> rollbackVersion(DocumentVersionVO documentVersionVO) {
        ResultData<Void> result = new ResultData<>();
        if (documentVersionVO == null || documentVersionVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        DocumentVersionQuery versionQuery = new DocumentVersionQuery();
        versionQuery.setId(documentVersionVO.getId());
        DocumentVersionResult versionResult = documentVersionDao.queryDocumentVersion(versionQuery);
        if (versionResult == null) {
            result.setErrorCode(ErrorCodeEnum.DOCUMENT_VERSION_NOT_EXIST);
            return result;
        }
        DocumentResult documentResult = this.queryDocument(versionResult.getDocId());
        if (documentResult == null) {
            result.setErrorCode(ErrorCodeEnum.DOCUMENT_NOT_EXIST);
            return result;
        }
        // 复制目标版本文件为新当前文件
        String newPath = fileUtil.generateFilePath(docsPath, UUID.randomUUID() + "." + versionResult.getExt());
        if (newPath == null) {
            result.setErrorCode(ErrorCodeEnum.FILE_CREATE_FAIL);
            return result;
        }
        boolean bo = fileUtil.copyFile(versionResult.getPath(), newPath);
        if (!bo) {
            result.setErrorCode(ErrorCodeEnum.FILE_NOT_EXIST);
            return result;
        }
        // 复制实体文件 pdf格式
        fileUtil.copyCompanionFile(versionResult.getPath(), newPath, "pdf");
        // 复制实体文件 txt格式
        fileUtil.copyCompanionFile(versionResult.getPath(), newPath, "txt");
        // 更新文档为目标版本
        Integer versionNo = this.currentVersionNo(documentResult) + 1;
        Document document = new Document();
        document.setId(documentResult.getId());
        document.setName(versionResult.getName());
        document.setSize(versionResult.getSize());
        document.setPath(newPath);
        document.setExt(versionResult.getExt());
        document.setVersionNo(versionNo);
        int count = documentDao.updateDBById(document);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        // 新版本记录入库
        count = this.insertVersion(documentResult.getId(), versionNo, versionResult.getName(), versionResult.getSize(), newPath, versionResult.getExt());
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        // 重置文档事件触发重新处理
        this.resetEvent(documentResult);
        result.setObjId(documentResult.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除文档历史版本
     * @param documentVersionVO 版本参数
     * @return 删除结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.DOCUMENT_VERSION_DELETE)
    public ResultData<Void> deleteVersion(DocumentVersionVO documentVersionVO) {
        ResultData<Void> result = new ResultData<>();
        if (documentVersionVO == null || documentVersionVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        DocumentVersionQuery versionQuery = new DocumentVersionQuery();
        versionQuery.setId(documentVersionVO.getId());
        DocumentVersionResult versionResult = documentVersionDao.queryDocumentVersion(versionQuery);
        if (versionResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        // 当前版本不允许删除
        DocumentResult documentResult = this.queryDocument(versionResult.getDocId());
        if (documentResult != null && versionResult.getVersionNo() != null && this.currentVersionNo(documentResult) == versionResult.getVersionNo()) {
            result.setErrorCode(ErrorCodeEnum.DOCUMENT_VERSION_CURRENT_NOT_ALLOW);
            return result;
        }
        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setId(documentVersionVO.getId());
        int count = documentVersionDao.deleteDBById(documentVersion);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        result.setObjId(documentVersionVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询文档版本
     * @param query 查询参数
     * @return 版本列表
     */
    @Override
    @DataScope
    public ResultData<PageResult<DocumentVersionResult>> pageVersionList(DocumentVersionQuery query) {
        ResultData<PageResult<DocumentVersionResult>> result = new ResultData<>();
        if (query == null || query.getDocId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        PageResult<DocumentVersionResult> pageResult = super.pageList(query);
        // 标记当前版本
        DocumentResult documentResult = this.queryDocument(query.getDocId());
        if (documentResult != null && pageResult.getRows() != null) {
            Integer versionNo = this.currentVersionNo(documentResult);
            for (DocumentVersionResult item : pageResult.getRows()) {
                item.setCurrentFlag(versionNo.equals(item.getVersionNo()));
            }
        }
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 下载文档历史版本
     * @param query 查询参数
     * @return 版本结果
     */
    @Override
    public ResultData<DocumentVersionResult> downloadVersion(DocumentVersionQuery query) {
        ResultData<DocumentVersionResult> result = new ResultData<>();
        if (query == null || query.getId() == null || StringUtil.isBlank(query.getExt())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        DocumentVersionResult versionResult = documentVersionDao.queryDocumentVersion(query);
        if (versionResult == null) {
            result.setErrorCode(ErrorCodeEnum.DOCUMENT_VERSION_NOT_EXIST);
            return result;
        }
        String filePath = fileUtil.getFileNameWithoutExt(versionResult.getPath()) + "." + query.getExt();
        File file = new File(filePath);
        if (!file.exists()) {
            logger.error("downloadVersion file not exist, filePath={}", filePath);
            result.setErrorCode(ErrorCodeEnum.FILE_NOT_EXIST);
            return result;
        }
        versionResult.setPath(filePath);
        result.setData(versionResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表数据
     */
    @Override
    protected void supplyList(List<DocumentVersionResult> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (DocumentVersionResult item : list) {
            item.setPath(null);
        }
        super.supplyCreatedByName(list);
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(DocumentVersionQuery query) {
        return documentVersionDao.queryDocumentVersionCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表数据
     */
    @Override
    protected List<DocumentVersionResult> queryList(DocumentVersionQuery query) {
        return documentVersionDao.queryDocumentVersionList(query);
    }

    /**
     * 查询文档
     * @param docId 文档id
     * @return 文档
     */
    private DocumentResult queryDocument(Long docId) {
        DocumentQuery documentQuery = new DocumentQuery();
        documentQuery.setId(docId);
        return documentDao.queryDocument(documentQuery);
    }

    /**
     * 获取文档当前版本号
     * @param documentResult 文档
     * @return 当前版本号
     */
    private int currentVersionNo(DocumentResult documentResult) {
        if (documentResult.getVersionNo() == null) {
            return 1;
        }
        return documentResult.getVersionNo();
    }

    /**
     * 版本记录入库
     * @param docId 文档id
     * @param versionNo 版本号
     * @param name 名称
     * @param size 大小
     * @param path 存储路径
     * @param ext 拓展名
     * @return 影响行数
     */
    private int insertVersion(Long docId, Integer versionNo, String name, Long size, String path, String ext) {
        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setDocId(docId);
        documentVersion.setVersionNo(versionNo);
        documentVersion.setName(name);
        documentVersion.setSize(size);
        documentVersion.setPath(path);
        documentVersion.setExt(ext);
        return documentVersionDao.insertDB(documentVersion);
    }

    /**
     * 重置文档事件触发重新处理
     * @param documentResult 文档
     */
    private void resetEvent(DocumentResult documentResult) {
        DocumentEventQuery eventQuery = new DocumentEventQuery();
        eventQuery.setDocId(documentResult.getId());
        DocumentEventResult eventResult = documentEventDao.queryDocumentEvent(eventQuery);
        if (eventResult == null) {
            logger.warn("resetEvent skip, document event not exist, docId={}", documentResult.getId());
            return;
        }
        DocumentEvent documentEvent = new DocumentEvent();
        documentEvent.setId(eventResult.getId());
        documentEvent.setContentStatus(DocumentEventStatusEnum.PENDING.getValue());
        documentEvent.setIndexStatus(DocumentEventStatusEnum.PENDING.getValue());
        documentEvent.setChunkStatus(DocumentEventStatusEnum.PENDING.getValue());
        Integer documentType = documentResult.getDocumentType();
        if (ObjectTypeEnum.KNOWLEDGE.getValue().equals(documentType)) {
            documentEvent.setVectorStatus(DocumentEventStatusEnum.PENDING.getValue());
        } else {
            documentEvent.setVectorStatus(DocumentEventStatusEnum.NO_EXECUTE.getValue());
        }
        if (ObjectTypeEnum.KG_GRAPH.getValue().equals(documentType)) {
            documentEvent.setGraphStatus(DocumentEventStatusEnum.PENDING.getValue());
        } else {
            documentEvent.setGraphStatus(DocumentEventStatusEnum.NO_EXECUTE.getValue());
        }
        documentEventDao.updateDBById(documentEvent);
    }
}

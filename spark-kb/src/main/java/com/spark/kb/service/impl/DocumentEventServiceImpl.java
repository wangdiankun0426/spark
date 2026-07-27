package com.spark.kb.service.impl;

import com.spark.bean.base.ResultData;
import com.spark.bean.kb.entity.DocumentEvent;
import com.spark.bean.kb.query.DocumentEventQuery;
import com.spark.bean.kb.result.DocumentEventResult;
import com.spark.bean.kb.vo.DocumentEventVO;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.dao.kb.DocumentEventDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.OperateTypeEnum;
import com.spark.kb.service.IDocumentEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/12/7 下午11:24
 */
@Service
public class DocumentEventServiceImpl implements IDocumentEventService {
    private final static Logger logger = LoggerFactory.getLogger(DocumentEventServiceImpl.class);
    @Autowired
    private DocumentEventDao documentEventDao;

    /**
     * 查询文档事件详情
     * @param query 查询参数
     * @return 文档事件详情
     */
    @Override
    public ResultData<DocumentEventResult> queryDocumentEventDetail(DocumentEventQuery query) {
        ResultData<DocumentEventResult> result = new ResultData<>();
        if (query == null || query.getDocId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        DocumentEventResult documentEventResult = documentEventDao.queryDocumentEvent(query);
        if (documentEventResult == null) {
            result.setErrorCode(ErrorCodeEnum.DOCUMENT_EVENT_NOT_EXIST);
            return result;
        }
        result.setData(documentEventResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改文档事件
     * @param documentEventVO 文档事件参数
     * @return 修改结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.DOCUMENT_EVENT_UPDATE)
    public ResultData<Void> updateDocumentEvent(DocumentEventVO documentEventVO) {
        ResultData<Void> result = new ResultData<>();
        if (documentEventVO == null || documentEventVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        DocumentEventQuery eventQuery = new DocumentEventQuery();
        eventQuery.setId(documentEventVO.getId());
        DocumentEventResult documentEventResult = documentEventDao.queryDocumentEvent(eventQuery);
        if (documentEventResult == null) {
            result.setErrorCode(ErrorCodeEnum.DOCUMENT_EVENT_NOT_EXIST);
            return result;
        }
        DocumentEvent documentEvent = new DocumentEvent();
        BeanUtils.copyProperties(documentEventVO, documentEvent);
        int count = documentEventDao.updateDBById(documentEvent);
        if (count < 1) {
            logger.error("updateDocumentEvent error, update db fail");
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }
}

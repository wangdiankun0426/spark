package com.spark.dms.service.impl;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.dms.entity.DocumentEvent;
import com.spark.common.bean.dms.query.DocumentEventQuery;
import com.spark.common.bean.dms.result.DocumentEventResult;
import com.spark.common.bean.dms.vo.DocumentEventVO;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.dao.dms.DocumentEventDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.dms.service.IDocumentEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.common.utils.BeanUtil;
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
@LogPrint
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
    @OperateLog(operateType = OperateTypeEnum.DOCUMENT_EVENT_DETAIL)
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
        result.setObjId(documentEventResult.getDocId());
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
        BeanUtil.copyProperties(documentEventVO, documentEvent);
        int count = documentEventDao.updateDBById(documentEvent);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setObjId(documentEventVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }
}

package com.spark.dms.service;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.dms.query.DocumentEventQuery;
import com.spark.common.bean.dms.result.DocumentEventResult;
import com.spark.common.bean.dms.vo.DocumentEventVO;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/12/7 下午11:24
 */
public interface IDocumentEventService {

    /**
     * 查询文档事件详情
     * @param query
     * @return 文档事件详情
     */
    ResultData<DocumentEventResult> queryDocumentEventDetail(DocumentEventQuery query);

    /**
     * 修改文档事件
     * @param documentEventVO 文档事件参数
     * @return 修改结果
     */
    ResultData<Void> updateDocumentEvent(DocumentEventVO documentEventVO);
}

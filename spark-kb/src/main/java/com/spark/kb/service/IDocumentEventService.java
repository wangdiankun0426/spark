package com.spark.kb.service;

import com.spark.bean.base.ResultData;
import com.spark.bean.kb.query.DocumentEventQuery;
import com.spark.bean.kb.result.DocumentEventResult;
import com.spark.bean.kb.vo.DocumentEventVO;

import java.util.concurrent.CompletableFuture;

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

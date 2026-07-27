package com.spark.dao.kb;

import com.spark.bean.kb.entity.DocumentEvent;
import com.spark.bean.kb.query.DocumentEventQuery;
import com.spark.bean.kb.result.DocumentEventResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024-12-07 22:55:32
 */
public interface DocumentEventDao extends BaseDao<DocumentEvent> {
    /**
     * 插入数据
     * @param documentEvent
     * @return
     */
    @Override
    int insert(DocumentEvent documentEvent);

    /**
     * 删除数据
     * @param documentEvent
     * @return
     */
    @Override
    int deleteById(DocumentEvent documentEvent);

    /**
     * 修改数据
     * @param documentEvent
     * @return
     */
    @Override
    int updateById(DocumentEvent documentEvent);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryDocumentEventCount(DocumentEventQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<DocumentEventResult> queryDocumentEventList(DocumentEventQuery query);

    /**
     * 查询单条
     * @param query
     * @return
     */
    DocumentEventResult queryDocumentEvent(DocumentEventQuery query);
}

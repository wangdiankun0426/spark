package com.spark.dao.dms;

import com.spark.common.bean.dms.entity.DocumentVersion;
import com.spark.common.bean.dms.query.DocumentVersionQuery;
import com.spark.common.bean.dms.result.DocumentVersionResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-17 10:00:00
 */
public interface DocumentVersionDao extends BaseDao<DocumentVersion> {
    /**
     * 插入数据
     * @param documentVersion
     * @return
     */
    @Override
    int insert(DocumentVersion documentVersion);

    /**
     * 删除数据
     * @param documentVersion
     * @return
     */
    @Override
    int deleteById(DocumentVersion documentVersion);

    /**
     * 修改数据
     * @param documentVersion
     * @return
     */
    @Override
    int updateById(DocumentVersion documentVersion);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryDocumentVersionCount(DocumentVersionQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<DocumentVersionResult> queryDocumentVersionList(DocumentVersionQuery query);

    /**
     * 查询单条
     * @param query
     * @return
     */
    DocumentVersionResult queryDocumentVersion(DocumentVersionQuery query);
}

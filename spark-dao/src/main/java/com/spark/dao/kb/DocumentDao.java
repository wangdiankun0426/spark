package com.spark.dao.kb;

import com.spark.bean.kb.entity.Document;
import com.spark.bean.kb.query.DocumentQuery;
import com.spark.bean.kb.result.DocumentCountResult;
import com.spark.bean.kb.result.DocumentResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024-12-07 19:08:01
 */
public interface DocumentDao extends BaseDao<Document> {
    /**
     * 插入数据
     * @param document
     * @return
     */
    @Override
    int insert(Document document);

    /**
     * 删除数据
     * @param document
     * @return
     */
    @Override
    int deleteById(Document document);

    /**
     * 修改数据
     * @param document
     * @return
     */
    @Override
    int updateById(Document document);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryDocumentCount(DocumentQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<DocumentResult> queryDocumentList(DocumentQuery query);

    /**
     * 查询单条
     * @param query
     * @return
     */
    DocumentResult queryDocument(DocumentQuery query);

    /**
     * 查询最大id
     * @return
     */
    @Select("select max(id) from kb_document")
    Long queryDocumentMaxId();

    /**
     * 按父ID列表分组统计文档数量
     * @param prtIds 父ID列表
     * @return 每条记录包含 prtId 与对应文档数量
     */
    List<DocumentCountResult> queryDocumentCountByPrtIds(@Param("prtIds") List<Long> prtIds);
}

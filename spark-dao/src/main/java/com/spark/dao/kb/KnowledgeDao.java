package com.spark.dao.kb;

import com.spark.bean.kb.entity.Knowledge;
import com.spark.bean.kb.query.KnowledgeQuery;
import com.spark.bean.kb.result.KnowledgeResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-17 10:00:00
 */
public interface KnowledgeDao extends BaseDao<Knowledge> {

    /**
     * 插入数据
     * @param knowledge 数据对象
     * @return 影响行数
     */
    @Override
    int insert(Knowledge knowledge);

    /**
     * 删除数据
     * @param knowledge 数据对象
     * @return 影响行数
     */
    @Override
    int deleteById(Knowledge knowledge);

    /**
     * 修改数据
     * @param knowledge 数据对象
     * @return 影响行数
     */
    @Override
    int updateById(Knowledge knowledge);

    /**
     * 查询数量
     * @param query 查询条件
     * @return 数量
     */
    int queryKnowledgeCount(KnowledgeQuery query);

    /**
     * 查询列表
     * @param query 查询条件
     * @return 列表
     */
    List<KnowledgeResult> queryKnowledgeList(KnowledgeQuery query);

    /**
     * 查询单条
     * @param query 查询条件
     * @return 单条数据
     */
    KnowledgeResult queryKnowledge(KnowledgeQuery query);

    /**
     * 查询最大ID
     * @return 最大ID
     */
    @Select("select max(id) from kb_knowledge")
    Long queryKnowledgeMaxId();
}

package com.spark.dao.kg;

import com.spark.common.bean.kg.entity.KgGraph;
import com.spark.common.bean.kg.query.KgGraphQuery;
import com.spark.common.bean.kg.result.KgGraphResult;
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
 * @since 2026-07-22 14:00:00
 */
public interface KgGraphDao extends BaseDao<KgGraph> {

    /**
     * 插入数据
     * @param kgGraph 数据对象
     * @return 影响行数
     */
    @Override
    int insert(KgGraph kgGraph);

    /**
     * 删除数据
     * @param kgGraph 数据对象
     * @return 影响行数
     */
    @Override
    int deleteById(KgGraph kgGraph);

    /**
     * 修改数据
     * @param kgGraph 数据对象
     * @return 影响行数
     */
    @Override
    int updateById(KgGraph kgGraph);

    /**
     * 查询数量
     * @param query 查询条件
     * @return 数量
     */
    int queryKgGraphCount(KgGraphQuery query);

    /**
     * 查询列表
     * @param query 查询条件
     * @return 列表
     */
    List<KgGraphResult> queryKgGraphList(KgGraphQuery query);

    /**
     * 查询单条
     * @param query 查询条件
     * @return 单条数据
     */
    KgGraphResult queryKgGraph(KgGraphQuery query);

    /**
     * 查询最大ID
     * @return 最大ID
     */
    @Select("select max(id) from kg_graph")
    Long queryKgGraphMaxId();
}

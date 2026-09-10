package com.spark.dao.kg;

import com.spark.common.bean.kg.entity.KgEntity;
import com.spark.common.bean.kg.query.KgEntityQuery;
import com.spark.common.bean.kg.result.KgEntityResult;
import com.spark.dao.BaseDao;

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
public interface KgEntityDao extends BaseDao<KgEntity> {

    /**
     * 插入数据
     * @param kgEntity 数据对象
     * @return 影响行数
     */
    @Override
    int insert(KgEntity kgEntity);

    /**
     * 删除数据
     * @param kgEntity 数据对象
     * @return 影响行数
     */
    @Override
    int deleteById(KgEntity kgEntity);

    /**
     * 修改数据
     * @param kgEntity 数据对象
     * @return 影响行数
     */
    @Override
    int updateById(KgEntity kgEntity);

    /**
     * 查询数量
     * @param query 查询条件
     * @return 数量
     */
    int queryKgEntityCount(KgEntityQuery query);

    /**
     * 查询列表
     * @param query 查询条件
     * @return 列表
     */
    List<KgEntityResult> queryKgEntityList(KgEntityQuery query);

    /**
     * 查询单条
     * @param query 查询条件
     * @return 单条数据
     */
    KgEntityResult queryKgEntity(KgEntityQuery query);
}

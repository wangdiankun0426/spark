package com.spark.dao.kg;

import com.spark.common.bean.kg.entity.KgEntity;
import com.spark.common.bean.kg.query.KgEntityQuery;
import com.spark.common.bean.kg.result.KgEntityCountResult;
import com.spark.common.bean.kg.result.KgEntityResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;

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

    /**
     * 按 id 批量软删除实体
     * @param ids 实体 id 列表
     * @return 影响行数
     */
    int deleteDBByIds(List<Long> ids);

    /**
     * 按图谱 id 批量统计实体数量
     * @param graphIds 图谱 id 列表
     * @return 每条记录包含 graphId 与对应实体数量
     */
    List<KgEntityCountResult> queryKgEntityCountByGraphIds(@Param("graphIds") List<Long> graphIds);
}

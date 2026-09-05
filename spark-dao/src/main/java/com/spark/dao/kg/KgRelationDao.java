package com.spark.dao.kg;

import com.spark.common.bean.kg.entity.KgRelation;
import com.spark.common.bean.kg.query.KgRelationQuery;
import com.spark.common.bean.kg.result.KgRelationCountResult;
import com.spark.common.bean.kg.result.KgRelationResult;
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
 * @since 2026-07-22 14:00:00
 */
public interface KgRelationDao extends BaseDao<KgRelation> {

    /**
     * 插入数据
     * @param kgRelation 数据对象
     * @return 影响行数
     */
    @Override
    int insert(KgRelation kgRelation);

    /**
     * 删除数据
     * @param kgRelation 数据对象
     * @return 影响行数
     */
    @Override
    int deleteById(KgRelation kgRelation);

    /**
     * 修改数据
     * @param kgRelation 数据对象
     * @return 影响行数
     */
    @Override
    int updateById(KgRelation kgRelation);

    /**
     * 查询数量
     * @param query 查询条件
     * @return 数量
     */
    int queryKgRelationCount(KgRelationQuery query);

    /**
     * 查询列表
     * @param query 查询条件
     * @return 列表
     */
    List<KgRelationResult> queryKgRelationList(KgRelationQuery query);

    /**
     * 查询单条
     * @param query 查询条件
     * @return 单条数据
     */
    KgRelationResult queryKgRelation(KgRelationQuery query);

    /**
     * 查询最大ID
     * @return 最大ID
     */
    @Select("select max(id) from kg_relation")
    Long queryKgRelationMaxId();

    /**
     * 批量更新头实体 id（用于实体合并）
     * @param oldEntityId 旧实体 id
     * @param newEntityId 新实体 id
     * @return 影响行数
     */
    int updateHeadEntityId(@org.apache.ibatis.annotations.Param("oldEntityId") Long oldEntityId,
                           @org.apache.ibatis.annotations.Param("newEntityId") Long newEntityId);

    /**
     * 批量更新尾实体 id（用于实体合并）
     * @param oldEntityId 旧实体 id
     * @param newEntityId 新实体 id
     * @return 影响行数
     */
    int updateTailEntityId(@Param("oldEntityId") Long oldEntityId,
                           @Param("newEntityId") Long newEntityId);

    /**
     * 按图谱 id 批量统计关系数量
     * @param graphIds 图谱 id 列表
     * @return 每条记录包含 graphId 与对应关系数量
     */
    List<KgRelationCountResult> queryKgRelationCountByGraphIds(@Param("graphIds") List<Long> graphIds);
}

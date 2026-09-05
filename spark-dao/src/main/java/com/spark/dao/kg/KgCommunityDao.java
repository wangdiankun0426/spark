package com.spark.dao.kg;

import com.spark.common.bean.kg.entity.KgCommunity;
import com.spark.common.bean.kg.query.KgCommunityQuery;
import com.spark.common.bean.kg.result.KgCommunityResult;
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
 * @since 2026-08-31 18:00:00
 */
public interface KgCommunityDao extends BaseDao<KgCommunity> {

    /**
     * 插入数据
     * @param kgCommunity 数据对象
     * @return 影响行数
     */
    @Override
    int insert(KgCommunity kgCommunity);

    /**
     * 删除数据
     * @param kgCommunity 数据对象
     * @return 影响行数
     */
    @Override
    int deleteById(KgCommunity kgCommunity);

    /**
     * 修改数据
     * @param kgCommunity 数据对象
     * @return 影响行数
     */
    @Override
    int updateById(KgCommunity kgCommunity);

    /**
     * 查询数量
     * @param query 查询条件
     * @return 数量
     */
    int queryKgCommunityCount(KgCommunityQuery query);

    /**
     * 查询列表
     * @param query 查询条件
     * @return 列表
     */
    List<KgCommunityResult> queryKgCommunityList(KgCommunityQuery query);

    /**
     * 查询单条
     * @param query 查询条件
     * @return 单条数据
     */
    KgCommunityResult queryKgCommunity(KgCommunityQuery query);

    /**
     * 按图谱 id 批量逻辑删除社区
     * @param graphId 图谱 id
     * @param updatedBy 修改人 id
     * @return 影响行数
     */
    int deleteByGraphId(@Param("graphId") Long graphId, @Param("updatedBy") Long updatedBy);
}

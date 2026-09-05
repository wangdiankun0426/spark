package com.spark.dao.llm;

import com.spark.common.bean.llm.entity.Provider;
import com.spark.common.bean.llm.query.ProviderQuery;
import com.spark.common.bean.llm.result.ProviderResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-05-13 20:52:57
 */
public interface ProviderDao extends BaseDao<Provider> {

    /**
     * 插入数据
     * @param provider 数据对象
     * @return 影响行数
     */
    @Override
    int insert(Provider provider);

    /**
     * 删除数据
     * @param provider 数据对象
     * @return 影响行数
     */
    @Override
    int deleteById(Provider provider);

    /**
     * 修改数据
     * @param provider 数据对象
     * @return 影响行数
     */
    @Override
    int updateById(Provider provider);

    /**
     * 查询数量
     * @param query 查询条件
     * @return  数量
     */
    int queryProviderCount(ProviderQuery query);

    /**
     * 查询列表
     * @param query 查询条件
     * @return 列表
     */
    List<ProviderResult> queryProviderList(ProviderQuery query);

    /**
     * 查询单条
     * @param query 查询条件
     * @return 单条数据
     */
    ProviderResult queryProvider(ProviderQuery query);
}

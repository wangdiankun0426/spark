package com.spark.dao.sys;

import com.spark.common.bean.sys.entity.TenantConfig;
import com.spark.common.bean.sys.query.TenantConfigQuery;
import com.spark.common.bean.sys.result.TenantConfigResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-11 10:00:00
 * 租户配置
 */
public interface TenantConfigDao extends BaseDao<TenantConfig> {

    /**
     * 插入数据
     * @param tenantConfig 租户配置
     * @return 影响行数
     */
    @Override
    int insert(TenantConfig tenantConfig);

    /**
     * 修改租户配置
     * @param tenantConfig 租户配置
     * @return 影响行数
     */
    @Override
    int updateById(TenantConfig tenantConfig);

    /**
     * 逻辑删除租户配置
     * @param tenantConfig 租户配置
     * @return 影响行数
     */
    @Override
    int deleteById(TenantConfig tenantConfig);

    /**
     * 查询租户配置列表
     * @param query 查询参数
     * @return 租户配置列表
     */
    List<TenantConfigResult> queryTenantConfigList(TenantConfigQuery query);

    /**
     * 查询租户配置单条
     * @param query 查询参数
     * @return 租户配置
     */
    TenantConfigResult queryTenantConfig(TenantConfigQuery query);

    /**
     * 查询租户配置数量
     * @param query 查询参数
     * @return 租户配置数量
     */
    int queryTenantConfigCount(TenantConfigQuery query);
}

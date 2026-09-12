package com.spark.manage.sys;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.query.TenantConfigQuery;
import com.spark.common.bean.sys.result.TenantConfigResult;
import com.spark.common.bean.sys.vo.TenantConfigVO;

import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-11 10:00:00
 * 租户配置服务
 */
public interface ITenantConfigService {

    /**
     * 创建租户配置
     * @param tenantConfigVO 租户配置参数
     * @return 创建结果
     */
    ResultData<Void> createTenantConfig(TenantConfigVO tenantConfigVO);

    /**
     * 修改租户配置
     * @param tenantConfigVO 租户配置参数
     * @return 修改结果
     */
    ResultData<Void> updateTenantConfig(TenantConfigVO tenantConfigVO);

    /**
     * 删除租户配置
     * @param tenantConfigVO 租户配置参数
     * @return 删除结果
     */
    ResultData<Void> deleteTenantConfig(TenantConfigVO tenantConfigVO);

    /**
     * 分页查询租户配置列表
     * @param query 查询参数
     * @return 分页结果
     */
    ResultData<PageResult<TenantConfigResult>> pageTenantConfigList(TenantConfigQuery query);

    /**
     * 创建租户默认配置
     * @param tenantId 租户id
     * @return 创建结果
     */
    ResultData<Integer> createDefaultConfig(Long tenantId);

    /**
     * 查询租户配置value
     * @param tenantId
     * @param key
     * @return
     */
    ResultData<String> queryTenantConfigValue(Long tenantId, String key);

    /**
     * 查询租户全部配置
     * @param tenantId 租户id
     * @return 配置key-value
     */
    ResultData<Map<String, String>> queryTenantConfigMap(Long tenantId);
}

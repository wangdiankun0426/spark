package com.spark.manage.sys;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.query.TenantQuery;
import com.spark.common.bean.sys.result.TenantResult;
import com.spark.common.bean.sys.vo.TenantVO;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-08 10:00:00
 * 租户服务
 */
public interface ITenantService {

    /**
     * 创建租户
     * @param tenantVO 租户参数
     * @return 创建结果
     */
    ResultData<Void> createTenant(TenantVO tenantVO);

    /**
     * 分页查询租户列表
     * @param query 查询参数
     * @return 分页结果
     */
    ResultData<PageResult<TenantResult>> pageTenantList(TenantQuery query);

    /**
     * 修改租户
     * @param tenantVO 租户参数
     * @return 修改结果
     */
    ResultData<Void> updateTenant(TenantVO tenantVO);

    /**
     * 删除租户
     * @param tenantVO 租户参数
     * @return 删除结果
     */
    ResultData<Void> deleteTenant(TenantVO tenantVO);

    /**
     * 批量关闭超期租户
     * @return 关闭结果
     */
    ResultData<Integer> closeExpiredTenant();
}

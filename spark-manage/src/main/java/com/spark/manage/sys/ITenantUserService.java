package com.spark.manage.sys;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.query.TenantUserQuery;
import com.spark.common.bean.sys.result.TenantResult;
import com.spark.common.bean.sys.result.TenantUserResult;
import com.spark.common.bean.sys.vo.TenantUserVO;
import com.spark.common.bean.sys.vo.TenantVO;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-08 14:00:00
 * 租户用户服务
 */
public interface ITenantUserService {

    /**
     * 添加用户
     * @param tenantUserVO 添加用户参数
     * @return 添加结果
     */
    ResultData<Void> addUser(TenantUserVO tenantUserVO);

    /**
     * 移除用户
     * @param tenantUserVO 移除用户参数
     * @return 移除结果
     */
    ResultData<Void> removeUser(TenantUserVO tenantUserVO);

    /**
     * 修改租户用户角色类型
     * @param tenantUserVO 修改参数
     * @return 修改结果
     */
    ResultData<Void> updateTenantUserRoleType(TenantUserVO tenantUserVO);

    /**
     * 分页查询租户用户列表
     * @param query 查询参数
     * @return 分页结果
     */
    ResultData<PageResult<TenantUserResult>> pageTenantUserList(TenantUserQuery query);

    /**
     * 查询当前用户已加入且可用的租户列表
     * @return 租户列表
     */
    ResultData<List<TenantResult>> queryMyTenantList();

    /**
     * 切换当前用户租户
     * @param tenantVO 租户参数
     * @return 切换结果
     */
    ResultData<Void> switchTenant(TenantVO tenantVO);
}

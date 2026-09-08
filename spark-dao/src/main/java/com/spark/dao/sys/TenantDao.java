package com.spark.dao.sys;

import com.spark.common.bean.sys.entity.Tenant;
import com.spark.common.bean.sys.query.TenantQuery;
import com.spark.common.bean.sys.result.TenantResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-08 10:00:00
 * 租户
 */
public interface TenantDao extends BaseDao<Tenant> {

    /**
     * 插入数据
     * @param tenant 租户
     * @return 影响行数
     */
    @Override
    int insert(Tenant tenant);

    /**
     * 修改租户
     * @param tenant 租户
     * @return 影响行数
     */
    @Override
    int updateById(Tenant tenant);

    /**
     * 逻辑删除租户
     * @param tenant 租户
     * @return 影响行数
     */
    @Override
    int deleteById(Tenant tenant);

    /**
     * 查询相同名称的租户
     * @param name 租户名
     * @return 租户
     */
    TenantResult querySameNameTenant(@Param("name") String name);

    /**
     * 查询租户列表
     * @param query 查询参数
     * @return 租户列表
     */
    List<TenantResult> queryTenantList(TenantQuery query);

    /**
     * 查询租户单条
     * @param query 查询参数
     * @return 租户
     */
    TenantResult queryTenant(TenantQuery query);

    /**
     * 查询租户数量
     * @param query 查询参数
     * @return 租户数量
     */
    int queryTenantCount(TenantQuery query);

    /**
     * 查询最大id
     * @return 最大id
     */
    @Select("select max(id) from sys_tenant")
    Long queryTenantMaxId();

    /**
     * 批量关闭超期租户
     * 状态为启用且截止时间已过的有效租户置为停用
     * @return 影响行数
     */
    @Update("update sys_tenant set status = -1, updated_dt = now() where delete_flag = 1 and status = 1 and deadline is not null and deadline <= now()")
    int closeExpiredTenant();
}

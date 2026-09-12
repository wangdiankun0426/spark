package com.spark.dao.sys;

import com.spark.common.bean.sys.entity.TenantUser;
import com.spark.common.bean.sys.query.TenantUserQuery;
import com.spark.common.bean.sys.result.TenantUserResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-08 14:00:00
 * 租户用户
 */
public interface TenantUserDao extends BaseDao<TenantUser> {

    /**
     * 查询用户租户
     * @param query 查询参数
     * @return 租户用户列表
     */
    TenantUserResult queryTenantUser(TenantUserQuery query);

    /**
     * 查询列表
     * @param query 查询参数
     * @return 租户用户列表
     */
    List<TenantUserResult> queryTenantUserList(TenantUserQuery query);

    /**
     * 查询条数
     * @param query 查询参数
     * @return 租户用户条数
     */
    int queryTenantUserCount(TenantUserQuery query);

    /**
     * 按租户批量插入用户
     * @param tenantId 租户id
     * @param deptId 所属部门
     * @param userIds 用户id数组
     * @param createdBy 创建人
     * @return 影响行数
     */
    int batchInsertByTenantId(@Param("tenantId") Long tenantId, @Param("deptId") Long deptId, @Param("userIds") List<Long> userIds, @Param("createdBy") Long createdBy);

//    /**
//     * 删除租户用户关联
//     * @param tenantId 租户id
//     * @param userId 用户id
//     * @param updatedBy 修改人
//     * @return 影响行数
//     */
//    @Update("update sys_tenant_user set delete_flag = -1, updated_by = #{updatedBy}, updated_dt = now() where tenant_id = #{tenantId} and user_id = #{userId}")
//    int deleteTenantUser(@Param("tenantId") Long tenantId, @Param("userId") Long userId, @Param("updatedBy") Long updatedBy);

    /**
     * 修改数据
     * @param tenantUser
     * @return
     */
    @Override
    int updateById(TenantUser tenantUser);

    /**
     * 删除数据
     * @param tenantUser
     * @return
     */
    @Override
    int deleteById(TenantUser tenantUser);

    /**
     * 删除租户用户关联
     * @param userId 用户id
     * @param updatedBy 修改人
     * @return 影响行数
     */
    @Update("update sys_tenant_user set delete_flag = -1, updated_by = #{updatedBy}, updated_dt = now() where user_id = #{userId}")
    int deleteTenantUserV2(@Param("userId") Long userId, @Param("updatedBy") Long updatedBy);
}

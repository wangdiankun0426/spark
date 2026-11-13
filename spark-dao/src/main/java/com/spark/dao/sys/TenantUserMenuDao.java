package com.spark.dao.sys;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-14 10:00:00
 * 租户用户组织菜单关联，重授权物理删除后批量插入，避免逻辑删除与唯一键冲突
 */
public interface TenantUserMenuDao {

    /**
     * 按租户用户批量插入组织菜单关联
     * @param tenantId 租户id
     * @param tenantUserId 租户用户id
     * @param menuIds 菜单id集合
     * @param createdBy 创建人id
     * @return 影响行数
     */
    int batchInsertByTenantUserId(@Param("tenantId") Long tenantId, @Param("tenantUserId") Long tenantUserId,
                                  @Param("menuIds") List<Long> menuIds, @Param("createdBy") Long createdBy);

    /**
     * 按租户用户物理删除全部组织菜单关联
     * @param tenantUserId 租户用户id
     * @return 影响行数
     */
    int deleteByTenantUserId(@Param("tenantUserId") Long tenantUserId);

    /**
     * 查询租户用户已勾选的组织菜单id集合
     * @param tenantUserId 租户用户id
     * @return 菜单id集合
     */
    List<Long> queryMenuIdsByTenantUserId(@Param("tenantUserId") Long tenantUserId);

    /**
     * 统计菜单被存活租户用户关联的数量，菜单删除前校验，逻辑删除用户的孤儿关联不计入
     * @param menuId 菜单id
     * @return 关联数量
     */
    int countByMenuId(@Param("menuId") Long menuId);
}

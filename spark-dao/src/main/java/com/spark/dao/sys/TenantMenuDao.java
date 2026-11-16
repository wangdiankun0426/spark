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
 * @since 2026-11-16 10:00:00
 * 租户菜单关联，重授权物理删除后批量插入，避免逻辑删除与唯一键冲突
 */
public interface TenantMenuDao {

    /**
     * 按租户批量插入菜单关联
     * @param tenantId 租户id
     * @param menuIds 菜单id集合
     * @param createdBy 创建人id
     * @return 影响行数
     */
    int batchInsertByTenantId(@Param("tenantId") Long tenantId, @Param("menuIds") List<Long> menuIds,
                              @Param("createdBy") Long createdBy);

    /**
     * 按租户物理删除全部菜单关联
     * @param tenantId 租户id
     * @return 影响行数
     */
    int deleteByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 查询租户已授权的菜单id集合
     * @param tenantId 租户id
     * @return 菜单id集合
     */
    List<Long> queryMenuIdsByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 统计菜单被存活租户关联的数量，菜单删除前校验，逻辑删除租户的孤儿关联不计入
     * @param menuId 菜单id
     * @return 关联数量
     */
    int countByMenuId(@Param("menuId") Long menuId);
}
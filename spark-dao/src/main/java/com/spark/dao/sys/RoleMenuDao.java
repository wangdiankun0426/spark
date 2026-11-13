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
 * @since 2026-11-13 16:30:00
 * 角色菜单关联，重授权物理删除后批量插入，避免逻辑删除与唯一键冲突
 */
public interface RoleMenuDao {

    /**
     * 按角色批量插入菜单关联
     * @param tenantId 租户id
     * @param roleId 角色id
     * @param menuIds 菜单id集合
     * @param createdBy 创建人id
     * @return 影响行数
     */
    int batchInsertByRoleId(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId,
                            @Param("menuIds") List<Long> menuIds, @Param("createdBy") Long createdBy);

    /**
     * 按角色物理删除全部菜单关联
     * @param roleId 角色id
     * @return 影响行数
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询角色授权的菜单id集合
     * @param roleId 角色id
     * @return 菜单id集合
     */
    List<Long> queryMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 统计菜单被角色关联的数量，菜单删除前校验
     * @param menuId 菜单id
     * @return 关联数量
     */
    int countByMenuId(@Param("menuId") Long menuId);
}
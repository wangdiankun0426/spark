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
 * 角色部门关联，自定义数据范围勾选部门，重授权物理删除后批量插入
 */
public interface RoleDeptDao {

    /**
     * 按角色批量插入部门关联
     * @param tenantId 租户id
     * @param roleId 角色id
     * @param deptIds 部门id集合
     * @param createdBy 创建人id
     * @return 影响行数
     */
    int batchInsertByRoleId(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId,
                            @Param("deptIds") List<Long> deptIds, @Param("createdBy") Long createdBy);

    /**
     * 按角色物理删除全部部门关联
     * @param roleId 角色id
     * @return 影响行数
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询角色自定义数据范围的部门id集合
     * @param roleId 角色id
     * @return 部门id集合
     */
    List<Long> queryDeptIdsByRoleId(@Param("roleId") Long roleId);
}
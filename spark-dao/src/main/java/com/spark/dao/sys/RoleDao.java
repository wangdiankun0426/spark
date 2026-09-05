package com.spark.dao.sys;

import com.spark.common.bean.sys.entity.Role;
import com.spark.common.bean.sys.query.RoleQuery;
import com.spark.common.bean.sys.result.RoleResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/25 21:40
 */
public interface RoleDao extends BaseDao<Role> {

    /**
     * 查询相同名称的角色
     * @param name
     * @return
     */
    RoleResult querySameNameRole(@Param("name") String name);

    /**
     * 插入数据
     * @param role
     * @return
     */
    @Override
    int insert(Role role);

    /**
     * 查询角色列表
     * @param query
     * @return
     */
    List<RoleResult> queryRoleList(RoleQuery query);

    /**
     * 查询角色单条
     * @param query
     * @return
     */
    RoleResult queryRole(RoleQuery query);

    /**
     * 修改角色
     * @param role
     * @return
     */
    @Override
    int updateById(Role role);

    /**
     * 删除角色
     * @param role
     * @return
     */
    @Override
    int deleteById(Role role);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryRoleCount(RoleQuery query);

    /**
     * 查询当前用户最大数据权限
     * @param userId
     * @return
     */
    int queryUserMaxDataScope(@Param("userId") Long userId);

    /**
     * 查询最大id
     * @return
     */
    @Select("select max(id) from sys_role")
    Long queryRoleMaxId();
}

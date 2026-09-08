package com.spark.dao.sys;

import com.spark.common.bean.sys.entity.DepartmentProfile;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-08 16:00:00
 * 部门扩展信息DAO
 */
public interface DepartmentProfileDao extends BaseDao<DepartmentProfile> {

    /**
     * 插入数据
     * @param departmentProfile 部门扩展信息
     * @return 影响行数
     */
    @Override
    int insert(DepartmentProfile departmentProfile);

    /**
     * 更新数据
     * @param departmentProfile 部门扩展信息
     * @return 影响行数
     */
    @Override
    int updateById(DepartmentProfile departmentProfile);

    /**
     * 根据部门ID删除（逻辑删除）
     * @param departmentProfile 部门扩展信息
     * @return 影响行数
     */
    @Override
    int deleteById(DepartmentProfile departmentProfile);

    /**
     * 根据部门ID查询
     * @param deptId 部门ID
     * @return 部门扩展信息
     */
    @Select("select * from sys_department_profile where id = #{deptId} and delete_flag = 1 limit 1")
    DepartmentProfile queryByDeptId(@Param("deptId") Long deptId);

    /**
     * 按部门层级码逻辑删除（包含子部门）
     * @param code 部门层级码
     * @param updatedBy 更新人
     * @return 影响行数
     */
    @Update("update sys_department_profile set delete_flag = -1, updated_by = #{updatedBy}, updated_dt = now() " +
            "where id in (select id from sys_department where code like concat(#{code}, '%'))")
    int deleteByDeptCode(@Param("code") String code, @Param("updatedBy") Long updatedBy);
}

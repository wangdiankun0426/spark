package com.spark.dao.system;

import com.spark.bean.system.entity.Department;
import com.spark.bean.system.query.DepartmentQuery;
import com.spark.bean.system.result.DepartmentResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Delete;
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
 * @since 2024/2/17 19:22
 */
public interface DepartmentDao extends BaseDao<Department> {

    /**
     * 查询最大id
     * @return
     */
    @Select("select max(id) from sys_department")
    Long queryDeptMaxId();

    /**
     * 获取部门最大的code
     *
     * @param newPrtId
     * @return
     */
    @Select("select max(code) from sys_department where prt_id=#{value}")
    String getMaxCode(Long newPrtId);

    /**
     * 查询单条
     * @param orgQuery
     * @return
     */
    DepartmentResult queryDepartment(DepartmentQuery orgQuery);

    /**
     * 插入数据
     * @param department
     * @return
     */
    @Override
    int insert(Department department);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<DepartmentResult> queryDepartmentList(DepartmentQuery query);

    /**
     * 删除子部门（包含自身）
     * @param code
     * @param updatedBy
     * @return
     */
    @Delete("update sys_department set delete_flag = -1 , updated_by = #{updatedBy}, updated_dt = now() where code like concat(#{code},'%')")
    int deleteSubDepartment(@Param("code") String code, @Param("updatedBy") Long updatedBy);

    /**
     * 修改部门
     * @param department
     * @return
     */
    @Override
    int updateById(Department department);

    /**
     * 根据企业微信部门ID查询部门
     * @param wecomId 企业微信部门ID
     * @return 部门信息
     */
    @Select("select * from sys_department where wecom_id = #{wecomId} limit 1")
    DepartmentResult queryByWecomId(@Param("wecomId") Long wecomId);
}

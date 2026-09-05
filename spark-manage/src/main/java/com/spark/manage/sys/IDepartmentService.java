package com.spark.manage.sys;

import com.spark.common.bean.sys.vo.DepartmentVO;
import com.spark.common.bean.sys.result.DepartmentResult;
import com.spark.common.bean.sys.tree.DepartmentTree;
import com.spark.common.bean.base.ResultData;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 19:39
 */
public interface IDepartmentService {

    /**
     * 创建部门
     * @param departmentVO 部门信息
     * @return 创建结果
     */
    ResultData<Void> createDepartment(DepartmentVO departmentVO);

    /**
     * 查询部门树
     * @return 部门树
     */
    ResultData<List<DepartmentTree>> queryDepartmentTree();

    /**
     * 修改部门
     * @param departmentVO 部门信息
     * @return 修改结果
     */
    ResultData<Void> updateDepartment(DepartmentVO departmentVO);

    /**
     * 删除部门
     * @param departmentVO 部门信息
     * @return 删除结果
     */
    ResultData<Void> deleteDepartment(DepartmentVO departmentVO);

    /**
     * 补充部门全路径
     *
     * @param departmentResult 部门信息
     * @return 部门信息
     */
    ResultData<String> supplyDeptPath(DepartmentResult departmentResult);

    }

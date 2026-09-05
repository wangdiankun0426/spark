package com.spark.web.rest.controller.manage.sys;

import com.spark.common.bean.sys.vo.DepartmentVO;
import com.spark.common.bean.sys.tree.DepartmentTree;
import com.spark.common.bean.base.ResultData;
import com.spark.manage.sys.IDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RestController
@RequestMapping("sys/dept")
public class DepartmentController {
    @Autowired
    private IDepartmentService departmentService;

    /**
     * 创建部门
     * @param departmentVO 部门信息
     * @return 创建结果
     */
    @PostMapping("create")
    private ResultData<Void> createDepartment(DepartmentVO departmentVO) {
        return departmentService.createDepartment(departmentVO);
    }

    /**
     * 查询部门树
     * @return 部门树
     */
    @GetMapping("tree")
    private ResultData<List<DepartmentTree>> queryDepartmentTree() {
        return departmentService.queryDepartmentTree();
    }

    /**
     * 修改部门
     * @param departmentVO 部门信息
     * @return 修改结果
     */
    @PostMapping("update")
    private ResultData<Void> updateDepartment(DepartmentVO departmentVO) {
        return departmentService.updateDepartment(departmentVO);
    }

    /**
     * 删除部门
     * @param departmentVO 部门信息
     * @return 删除结果
     */
    @PostMapping("delete")
    private ResultData<Void> deleteDepartment(DepartmentVO departmentVO) {
        return departmentService.deleteDepartment(departmentVO);
    }

}

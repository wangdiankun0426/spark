package com.spark.web.controller.system;

import com.spark.bean.system.vo.RoleVO;
import com.spark.bean.system.query.RoleQuery;
import com.spark.bean.system.result.RoleResult;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.manage.system.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/25 21:35
 */
@RestController
@RequestMapping("system/role")
public class RoleController {
    @Autowired
    private IRoleService roleService;

    /**
     * 创建角色
     * @param roleVO 角色参数
     * @return 创建结果
     */
    @PostMapping("create")
    public ResultData<Void> createRole(RoleVO roleVO) {
        return roleService.createRole(roleVO);
    }

    /**
     * 分页查询列表
     * @param query 查询参数
     * @return 结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<RoleResult>> pageRoleList(RoleQuery query) {
        return roleService.pageRoleList(query);
    }

    /**
     * 修改角色
     * @param roleVO 修改的参数
     * @return 修改结果
     */
    @PostMapping("update")
    public ResultData<Void> updateRole(RoleVO roleVO) {
        return roleService.updateRole(roleVO);
    }

    /**
     * 删除角色
     * @param roleVO 删除的参数
     * @return 删除结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteRole(RoleVO roleVO) {
        return roleService.deleteRole(roleVO);
    }
}

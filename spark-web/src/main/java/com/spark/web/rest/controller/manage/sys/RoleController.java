package com.spark.web.rest.controller.manage.sys;

import com.spark.common.bean.sys.vo.RoleVO;
import com.spark.common.bean.sys.query.RoleQuery;
import com.spark.common.bean.sys.result.RoleResult;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.manage.sys.IRoleService;
import com.spark.config.aspectj.annotation.RequiresPermi;
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
@RequestMapping("sys/role")
public class RoleController {
    @Autowired
    private IRoleService roleService;

    /**
     * 创建角色
     * @param roleVO 角色参数
     * @return 创建结果
     */
    @RequiresPermi("sys:role:create")
    @PostMapping("create")
    public ResultData<Void> createRole(RoleVO roleVO) {
        return roleService.createRole(roleVO);
    }

    /**
     * 分页查询列表
     * @param query 查询参数
     * @return 结果
     */
    @RequiresPermi("sys:role:query")
    @GetMapping("pageList")
    public ResultData<PageResult<RoleResult>> pageRoleList(RoleQuery query) {
        return roleService.pageRoleList(query);
    }

    /**
     * 查询角色详情
     * @param query 查询参数
     * @return 角色详情
     */
    @RequiresPermi("sys:role:query")
    @GetMapping("detail")
    public ResultData<RoleResult> queryRoleDetail(RoleQuery query) {
        return roleService.queryRoleDetail(query);
    }

    /**
     * 修改角色
     * @param roleVO 修改的参数
     * @return 修改结果
     */
    @RequiresPermi("sys:role:update")
    @PostMapping("update")
    public ResultData<Void> updateRole(RoleVO roleVO) {
        return roleService.updateRole(roleVO);
    }

    /**
     * 删除角色
     * @param roleVO 删除的参数
     * @return 删除结果
     */
    @RequiresPermi("sys:role:delete")
    @PostMapping("delete")
    public ResultData<Void> deleteRole(RoleVO roleVO) {
        return roleService.deleteRole(roleVO);
    }
}

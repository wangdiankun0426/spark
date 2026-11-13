package com.spark.web.rest.controller.manage.sys;

import com.spark.common.bean.sys.query.RoleUserQuery;
import com.spark.common.bean.sys.result.RoleUserResult;
import com.spark.common.bean.sys.vo.RoleUserVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.manage.sys.IRoleUserService;
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
 * @since 2024/3/26 16:27
 */
@RestController
@RequestMapping("sys/role/user")
public class RoleUserController {
    @Autowired
    private IRoleUserService roleUserService;

    /**
     * 添加用户
     *
     * @param roleUserVO 添加用户参数
     * @return 验证结果
     */
    @RequiresPermi("sys:roleUser:add")
    @PostMapping("add")
    public ResultData<Void> addUser(RoleUserVO roleUserVO) {
        return roleUserService.addUser(roleUserVO);
    }

    /**
     * 移除用户
     *
     * @param roleUserVO 移除用户参数
     * @return 验证结果
     */
    @RequiresPermi("sys:roleUser:remove")
    @PostMapping("remove")
    public ResultData<Void> removeUser(RoleUserVO roleUserVO) {
        return roleUserService.removeUser(roleUserVO);
    }

    /**
     * 分页查询角色用户列表
     *
     * @param query 查询参数
     * @return 结果
     */
    @RequiresPermi("sys:roleUser:query")
    @GetMapping("pageList")
    public ResultData<PageResult<RoleUserResult>> pageRoleUserList(RoleUserQuery query) {
        return roleUserService.pageRoleUserList(query);
    }

}

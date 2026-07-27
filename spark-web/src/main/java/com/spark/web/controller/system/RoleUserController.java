package com.spark.web.controller.system;

import com.spark.bean.system.query.RoleUserQuery;
import com.spark.bean.system.result.RoleUserResult;
import com.spark.bean.system.vo.RoleUserVO;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.manage.system.IRoleUserService;
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
@RequestMapping("system/role/user")
public class RoleUserController {
    @Autowired
    private IRoleUserService roleUserService;

    /**
     * 添加用户
     *
     * @param roleUserVO 添加用户参数
     * @return 验证结果
     */
    @PostMapping("add")
    private ResultData<Void> addUser(RoleUserVO roleUserVO) {
        return roleUserService.addUser(roleUserVO);
    }

    /**
     * 移除用户
     *
     * @param roleUserVO 移除用户参数
     * @return 验证结果
     */
    @PostMapping("remove")
    private ResultData<Void> removeUser(RoleUserVO roleUserVO) {
        return roleUserService.removeUser(roleUserVO);
    }

    /**
     * 分页查询角色用户列表
     *
     * @param query 查询参数
     * @return 结果
     */
    @GetMapping("pageList")
    private ResultData<PageResult<RoleUserResult>> pageRoleUserList(RoleUserQuery query) {
        return roleUserService.pageRoleUserList(query);
    }

}

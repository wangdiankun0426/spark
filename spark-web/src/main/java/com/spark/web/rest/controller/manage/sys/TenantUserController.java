package com.spark.web.rest.controller.manage.sys;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.query.TenantUserQuery;
import com.spark.common.bean.sys.result.TenantResult;
import com.spark.common.bean.sys.result.TenantUserResult;
import com.spark.common.bean.sys.vo.TenantUserVO;
import com.spark.common.bean.sys.vo.TenantVO;
import com.spark.manage.sys.ITenantUserService;
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
 * @since 2026-09-08 14:00:00
 * 租户用户
 */
@RestController
@RequestMapping("sys/tenant/user")
public class TenantUserController {
    @Autowired
    private ITenantUserService tenantUserService;

    /**
     * 添加用户到租户
     * @param tenantUserVO 添加用户参数
     * @return 添加结果
     */
    @PostMapping("add")
    private ResultData<Void> addUser(TenantUserVO tenantUserVO) {
        return tenantUserService.addUser(tenantUserVO);
    }

    /**
     * 从租户移除用户
     * @param tenantUserVO 移除用户参数
     * @return 移除结果
     */
    @PostMapping("remove")
    private ResultData<Void> removeUser(TenantUserVO tenantUserVO) {
        return tenantUserService.removeUser(tenantUserVO);
    }

    /**
     * 修改租户用户角色类型
     * @param tenantUserVO 修改参数
     * @return 修改结果
     */
    @PostMapping("updateRoleType")
    private ResultData<Void> updateTenantUserRoleType(TenantUserVO tenantUserVO) {
        return tenantUserService.updateTenantUserRoleType(tenantUserVO);
    }

    /**
     * 分页查询租户用户列表
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("pageList")
    private ResultData<PageResult<TenantUserResult>> pageTenantUserList(TenantUserQuery query) {
        return tenantUserService.pageTenantUserList(query);
    }

    /**
     * 查询当前用户已加入且可用的租户列表
     * @return 租户列表
     */
    @GetMapping("myList")
    private ResultData<List<TenantResult>> queryMyTenantList() {
        return tenantUserService.queryMyTenantList();
    }

    /**
     * 切换当前用户租户
     * @param tenantVO 租户参数
     * @return 切换结果
     */
    @PostMapping("switch")
    private ResultData<Void> switchTenant(TenantVO tenantVO) {
        return tenantUserService.switchTenant(tenantVO);
    }
}

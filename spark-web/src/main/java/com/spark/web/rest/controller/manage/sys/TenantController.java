package com.spark.web.rest.controller.manage.sys;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.query.TenantQuery;
import com.spark.common.bean.sys.result.TenantResult;
import com.spark.common.bean.sys.vo.TenantVO;
import com.spark.manage.sys.ITenantService;
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
 * @since 2026-09-08 10:00:00
 * 租户
 */
@RestController
@RequestMapping("sys/tenant")
public class TenantController {
    @Autowired
    private ITenantService tenantService;

    /**
     * 创建租户
     * @param tenantVO 租户参数
     * @return 创建结果
     */
    @PostMapping("create")
    public ResultData<Void> createTenant(TenantVO tenantVO) {
        return tenantService.createTenant(tenantVO);
    }

    /**
     * 分页查询租户列表
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<TenantResult>> pageTenantList(TenantQuery query) {
        return tenantService.pageTenantList(query);
    }

    /**
     * 修改租户
     * @param tenantVO 租户参数
     * @return 修改结果
     */
    @PostMapping("update")
    public ResultData<Void> updateTenant(TenantVO tenantVO) {
        return tenantService.updateTenant(tenantVO);
    }

    /**
     * 删除租户
     * @param tenantVO 租户参数
     * @return 删除结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteTenant(TenantVO tenantVO) {
        return tenantService.deleteTenant(tenantVO);
    }

    /**
     * 查询租户已授权的菜单id集合
     * @param tenantVO 租户参数
     * @return 菜单id集合
     */
    @GetMapping("menuIds")
    public ResultData<List<Long>> queryTenantMenuIds(TenantVO tenantVO) {
        return tenantService.queryTenantMenuIds(tenantVO);
    }

    /**
     * 保存租户菜单授权
     * @param tenantVO 租户参数
     * @return 保存结果
     */
    @PostMapping("saveMenu")
    public ResultData<Void> saveTenantMenus(TenantVO tenantVO) {
        return tenantService.saveTenantMenus(tenantVO);
    }

}

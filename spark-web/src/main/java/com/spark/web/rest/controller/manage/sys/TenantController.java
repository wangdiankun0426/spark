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
    private ResultData<Void> createTenant(TenantVO tenantVO) {
        return tenantService.createTenant(tenantVO);
    }

    /**
     * 分页查询租户列表
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("pageList")
    private ResultData<PageResult<TenantResult>> pageTenantList(TenantQuery query) {
        return tenantService.pageTenantList(query);
    }

    /**
     * 修改租户
     * @param tenantVO 租户参数
     * @return 修改结果
     */
    @PostMapping("update")
    private ResultData<Void> updateTenant(TenantVO tenantVO) {
        return tenantService.updateTenant(tenantVO);
    }

    /**
     * 删除租户
     * @param tenantVO 租户参数
     * @return 删除结果
     */
    @PostMapping("delete")
    private ResultData<Void> deleteTenant(TenantVO tenantVO) {
        return tenantService.deleteTenant(tenantVO);
    }

}

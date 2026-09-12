package com.spark.web.rest.controller.manage.sys;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.query.TenantConfigQuery;
import com.spark.common.bean.sys.result.TenantConfigResult;
import com.spark.common.bean.sys.vo.TenantConfigVO;
import com.spark.manage.sys.ITenantConfigService;
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
 * @since 2026-09-11 10:00:00
 * 租户配置
 */
@RestController
@RequestMapping("sys/tenant/config")
public class TenantConfigController {
    @Autowired
    private ITenantConfigService tenantConfigService;

    /**
     * 创建租户配置
     * @param tenantConfigVO 租户配置参数
     * @return 创建结果
     */
    @PostMapping("create")
    private ResultData<Void> createTenantConfig(TenantConfigVO tenantConfigVO) {
        return tenantConfigService.createTenantConfig(tenantConfigVO);
    }

    /**
     * 分页查询租户配置列表
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("pageList")
    private ResultData<PageResult<TenantConfigResult>> pageTenantConfigList(TenantConfigQuery query) {
        return tenantConfigService.pageTenantConfigList(query);
    }

    /**
     * 修改租户配置
     * @param tenantConfigVO 租户配置参数
     * @return 修改结果
     */
    @PostMapping("update")
    private ResultData<Void> updateTenantConfig(TenantConfigVO tenantConfigVO) {
        return tenantConfigService.updateTenantConfig(tenantConfigVO);
    }

    /**
     * 删除租户配置
     * @param tenantConfigVO 租户配置参数
     * @return 删除结果
     */
    @PostMapping("delete")
    private ResultData<Void> deleteTenantConfig(TenantConfigVO tenantConfigVO) {
        return tenantConfigService.deleteTenantConfig(tenantConfigVO);
    }

}

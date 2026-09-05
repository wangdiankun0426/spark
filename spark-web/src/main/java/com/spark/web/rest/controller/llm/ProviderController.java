package com.spark.web.rest.controller.llm;

import com.spark.common.bean.llm.query.ProviderQuery;
import com.spark.common.bean.llm.result.ProviderResult;
import com.spark.common.bean.llm.vo.ProviderVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.llm.service.IProviderService;
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
 * @since 2026-05-13 21:30:11
 */
@RestController
@RequestMapping("llm/provider")
public class ProviderController {

    @Autowired
    private IProviderService providerService;

    /**
     * 创建模型厂商
     * @param providerVO  创建模型厂商参数
     * @return 响应结果
     */
    @PostMapping("create")
    public ResultData<Void> createProvider(ProviderVO providerVO) {
        return providerService.createProvider(providerVO);
    }

    /**
     * 修改模型厂商
     * @param providerVO 修改模型厂商参数
     * @return 响应结果
     */
    @PostMapping("update")
    public ResultData<Void> updateProvider(ProviderVO providerVO) {
        return providerService.updateProvider(providerVO);
    }

    /**
     * 删除模型厂商
     * @param providerVO 删除模型厂商参数
     * @return 响应结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteProvider(ProviderVO providerVO) {
        return providerService.deleteProvider(providerVO);
    }

    /**
     * 分页查询模型厂商
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<ProviderResult>> pageProviderList(ProviderQuery query) {
        return providerService.pageProviderList(query);
    }

    /**
     * 查询模型厂商详情
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("detail")
    public ResultData<ProviderResult> queryProviderDetail(ProviderQuery query) {
        return providerService.queryProviderDetail(query);
    }
}

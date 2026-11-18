package com.spark.web.rest.controller.llm;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.llm.query.ProviderKeyQuery;
import com.spark.common.bean.llm.result.ProviderKeyResult;
import com.spark.common.bean.llm.vo.ProviderKeyVO;
import com.spark.config.aspectj.annotation.RequiresPermi;
import com.spark.llm.service.IProviderKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 15:00:00
 * 模型厂商密钥控制器
 */
@RestController
@RequestMapping("llm/providerKey")
public class ProviderKeyController {

    @Autowired
    private IProviderKeyService providerKeyService;

    /**
     * 创建模型厂商密钥
     * @param providerKeyVO 创建参数
     * @return 响应结果
     */
    @RequiresPermi("llm:providerKey:create")
    @PostMapping("create")
    public ResultData<Void> createProviderKey(@RequestBody ProviderKeyVO providerKeyVO) {
        return providerKeyService.createProviderKey(providerKeyVO);
    }

    /**
     * 修改模型厂商密钥
     * @param providerKeyVO 修改参数
     * @return 响应结果
     */
    @RequiresPermi("llm:providerKey:update")
    @PostMapping("update")
    public ResultData<Void> updateProviderKey(@RequestBody ProviderKeyVO providerKeyVO) {
        return providerKeyService.updateProviderKey(providerKeyVO);
    }

    /**
     * 删除模型厂商密钥
     * @param providerKeyVO 删除参数
     * @return 响应结果
     */
    @RequiresPermi("llm:providerKey:delete")
    @PostMapping("delete")
    public ResultData<Void> deleteProviderKey(ProviderKeyVO providerKeyVO) {
        return providerKeyService.deleteProviderKey(providerKeyVO);
    }

    /**
     * 分页查询模型厂商密钥
     * @param query 查询参数
     * @return 响应结果
     */
    @RequiresPermi("llm:providerKey:query")
    @GetMapping("pageList")
    public ResultData<PageResult<ProviderKeyResult>> pageProviderKeyList(ProviderKeyQuery query) {
        return providerKeyService.pageProviderKeyList(query);
    }

    /**
     * 查询模型厂商密钥详情
     * @param query 查询参数
     * @return 响应结果
     */
    @RequiresPermi("llm:providerKey:detail")
    @GetMapping("detail")
    public ResultData<ProviderKeyResult> queryProviderKeyDetail(ProviderKeyQuery query) {
        return providerKeyService.queryProviderKeyDetail(query);
    }

}

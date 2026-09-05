package com.spark.llm.service;

import com.spark.common.bean.llm.query.ProviderQuery;
import com.spark.common.bean.llm.result.ProviderResult;
import com.spark.common.bean.llm.vo.ProviderVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-05-13 21:08:10
 */
public interface IProviderService {

    /**
     * 创建模型厂商
     * @param providerVO 模型厂商数据
     * @return 创建结果
     */
    ResultData<Void> createProvider(ProviderVO providerVO);

    /**
     * 修改模型厂商
     * @param providerVO 模型厂商数据
     * @return 修改结果
     */
    ResultData<Void> updateProvider(ProviderVO providerVO);

    /**
     * 删除模型厂商
     * @param providerVO 模型厂商数据
     * @return  删除结果
     */
    ResultData<Void> deleteProvider(ProviderVO providerVO);

    /**
     * 分页查询模型厂商
     * @param query 查询模型厂商条件
     * @return 分页结果
     */
    ResultData<PageResult<ProviderResult>> pageProviderList(ProviderQuery query);

    /**
     * 查询模型厂商详情
     * @param query 查询模型厂商条件
     * @return 详情
     */
    ResultData<ProviderResult> queryProviderDetail(ProviderQuery query);

}

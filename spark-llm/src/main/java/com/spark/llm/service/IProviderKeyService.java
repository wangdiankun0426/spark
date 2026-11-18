package com.spark.llm.service;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.llm.query.ProviderKeyQuery;
import com.spark.common.bean.llm.result.ProviderKeyResult;
import com.spark.common.bean.llm.vo.ProviderKeyVO;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 15:00:00
 * 模型厂商密钥服务接口
 */
public interface IProviderKeyService {

    /**
     * 创建模型厂商密钥
     * @param providerKeyVO 模型厂商密钥数据
     * @return 创建结果
     */
    ResultData<Void> createProviderKey(ProviderKeyVO providerKeyVO);

    /**
     * 修改模型厂商密钥
     * @param providerKeyVO 模型厂商密钥数据
     * @return 修改结果
     */
    ResultData<Void> updateProviderKey(ProviderKeyVO providerKeyVO);

    /**
     * 删除模型厂商密钥
     * @param providerKeyVO 模型厂商密钥数据
     * @return 删除结果
     */
    ResultData<Void> deleteProviderKey(ProviderKeyVO providerKeyVO);

    /**
     * 分页查询模型厂商密钥
     * @param query 查询条件
     * @return 分页结果
     */
    ResultData<PageResult<ProviderKeyResult>> pageProviderKeyList(ProviderKeyQuery query);

    /**
     * 查询模型厂商密钥详情
     * @param query 查询条件
     * @return 详情
     */
    ResultData<ProviderKeyResult> queryProviderKeyDetail(ProviderKeyQuery query);

}

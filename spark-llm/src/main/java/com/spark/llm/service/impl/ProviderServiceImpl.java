package com.spark.llm.service.impl;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.llm.entity.Provider;
import com.spark.bean.llm.query.ProviderQuery;
import com.spark.bean.llm.result.ProviderResult;
import com.spark.bean.llm.vo.ProviderVO;
import com.spark.dao.llm.ProviderDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.llm.model.ModelFactory;
import com.spark.manage.BaseService;
import com.spark.llm.service.IProviderService;
import com.spark.utils.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-05-13 21:30:11
 */
@Service
public class ProviderServiceImpl extends BaseService<ProviderQuery, ProviderResult> implements IProviderService {
    private final static Logger logger = LoggerFactory.getLogger(ProviderServiceImpl.class);
    @Autowired
    private ProviderDao providerDao;
    @Autowired
    private ModelFactory modelFactory;

    /**
     * 创建模型厂商
     * @param providerVO 模型厂商数据
     * @return 创建结果
     */
    @Override
    public ResultData<Void> createProvider(ProviderVO providerVO) {
        ResultData<Void> result = new ResultData<>();
        if (providerVO == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Provider provider = new Provider();
        BeanUtils.copyProperties(providerVO, provider);
        int count = providerDao.insertDB(provider);
        if (count < 1) {
            logger.error("createProvider error, insert db fail");
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改模型厂商
     * @param providerVO 模型厂商数据
     * @return 修改结果
     */
    @Override
    public ResultData<Void> updateProvider(ProviderVO providerVO) {
        ResultData<Void> result = new ResultData<>();
        if (providerVO == null || providerVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ProviderQuery providerQuery = new ProviderQuery();
        providerQuery.setId(providerVO.getId());
        ProviderResult providerResult = providerDao.queryProvider(providerQuery);
        if (providerResult == null) {
            result.setErrorCode(ErrorCodeEnum.PROVIDER_NOT_EXIST);
            return result;
        }
        Provider provider = new Provider();
        BeanUtils.copyProperties(providerVO, provider);
        int count = providerDao.updateDBById(provider);
        if (count < 1) {
            logger.error("updateProvider error, update db fail");
            return result;
        }
        // 厂商密钥变更后清空该厂商下所有模型缓存，确保下次使用时使用最新密钥
        modelFactory.clearModelCacheByProvider(providerVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除模型厂商
     * @param providerVO 模型厂商数据
     * @return  删除结果
     */
    @Override
    public ResultData<Void> deleteProvider(ProviderVO providerVO) {
        ResultData<Void> result = new ResultData<>();
        if (providerVO == null || providerVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ProviderQuery providerQuery = new ProviderQuery();
        providerQuery.setId(providerVO.getId());
        ProviderResult providerResult = providerDao.queryProvider(providerQuery);
        if (providerResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        Provider provider = new Provider();
        provider.setId(providerVO.getId());
        int count = providerDao.deleteDBById(provider);
        if (count < 1) {
            logger.error("deleteProvider error, delete db fail");
            return result;
        }
        // 厂商删除后清空该厂商下所有模型缓存
        modelFactory.clearModelCacheByProvider(providerVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询模型厂商
     * @param query 查询模型厂商条件
     * @return 分页结果
     */
    @Override
    public ResultData<PageResult<ProviderResult>> pageProviderList(ProviderQuery query) {
        ResultData<PageResult<ProviderResult>> result = new ResultData<>();
        if (query == null) {
            query = new ProviderQuery();
        }
        PageResult<ProviderResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询模型厂商详情
     * @param query 查询模型厂商条件
     * @return 详情
     */
    @Override
    public ResultData<ProviderResult> queryProviderDetail(ProviderQuery query) {
        ResultData<ProviderResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ProviderResult providerResult = providerDao.queryProvider(query);
        if (providerResult == null) {
            result.setErrorCode(ErrorCodeEnum.PROVIDER_NOT_EXIST);
            return result;
        }
        result.setData(providerResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<ProviderResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(ProviderQuery query) {
        return providerDao.queryProviderCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<ProviderResult> queryList(ProviderQuery query) {
        return providerDao.queryProviderList(query);
    }
}

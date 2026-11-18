package com.spark.llm.service.impl;

import com.spark.common.bean.base.BaseException;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.llm.entity.Provider;
import com.spark.common.bean.llm.entity.ProviderKey;
import com.spark.common.bean.llm.query.ProviderQuery;
import com.spark.common.bean.llm.result.ProviderResult;
import com.spark.common.bean.llm.vo.ProviderVO;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.dao.llm.ProviderDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.dao.llm.ProviderKeyDao;
import com.spark.llm.agent.AgentFactory;
import com.spark.llm.model.factory.ModelFactory;
import com.spark.llm.security.LlmApiKeyCipher;
import com.spark.llm.service.IProviderService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.spark.manage.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.common.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private ProviderKeyDao providerKeyDao;
    @Autowired
    private ModelFactory modelFactory;
    @Autowired
    private AgentFactory agentFactory;
    @Autowired
    private LlmApiKeyCipher llmApiKeyCipher;

    /**
     * 创建模型厂商
     * @param providerVO 模型厂商数据
     * @return 创建结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.PROVIDER_INSERT)
    @Transactional(transactionManager = "primaryTransactionManager", rollbackFor = Exception.class)
    public ResultData<Void> createProvider(ProviderVO providerVO) {
        ResultData<Void> result = new ResultData<>();
        if (providerVO == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (StringUtil.isBlank(providerVO.getSecretKey())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (providerVO.getSecretKey().length() > 256) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String plainKey = providerVO.getSecretKey();
        String encryptedKey = llmApiKeyCipher.encrypt(plainKey);
        if (encryptedKey.length() > 512) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Provider provider = new Provider();
        BeanUtil.copyProperties(providerVO, provider);
        // 非站点管理员强制归属当前租户
        if (!SessionHolder.isSysAdmin()) {
            provider.setTenantId(SessionHolder.getCurrentTenantId());
        }
        int count = providerDao.insertDB(provider);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        ProviderKey providerKey = new ProviderKey();
        Long providerId = provider.getId();
        providerKey.setProviderId(providerId);
        providerKey.setSecretKey(encryptedKey);
        String keyLast4 = llmApiKeyCipher.last4(plainKey);
        providerKey.setKeyLast4(keyLast4);
        providerKey.setStatus(StatusEnum.NORMAL.getValue());
        int keyCount = providerKeyDao.insertDB(providerKey);
        if (keyCount < 1) {
            throw new BaseException(ErrorCodeEnum.INSERT_DATA_FAIL);
        }
        result.setObjId(providerId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改模型厂商
     * @param providerVO 模型厂商数据
     * @return 修改结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.PROVIDER_UPDATE)
    public ResultData<Void> updateProvider(ProviderVO providerVO) {
        ResultData<Void> result = new ResultData<>();
        if (providerVO == null || providerVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (providerVO.getSecretKey() != null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ProviderQuery providerQuery = new ProviderQuery();
        providerQuery.setId(providerVO.getId());
        super.scopeTenant(providerQuery);
        ProviderResult providerResult = providerDao.queryProvider(providerQuery);
        if (providerResult == null) {
            result.setErrorCode(ErrorCodeEnum.PROVIDER_NOT_EXIST);
            return result;
        }
        Provider provider = new Provider();
        BeanUtil.copyProperties(providerVO, provider);
        int count = providerDao.updateDBById(provider);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        // 厂商配置变更后清空关联缓存
        modelFactory.clearModelCacheByProvider(providerVO.getId());
        agentFactory.clearAllAgentCache();
        result.setObjId(providerVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除模型厂商
     * @param providerVO 模型厂商数据
     * @return  删除结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.PROVIDER_DELETE)
    public ResultData<Void> deleteProvider(ProviderVO providerVO) {
        ResultData<Void> result = new ResultData<>();
        if (providerVO == null || providerVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ProviderQuery providerQuery = new ProviderQuery();
        providerQuery.setId(providerVO.getId());
        super.scopeTenant(providerQuery);
        ProviderResult providerResult = providerDao.queryProvider(providerQuery);
        if (providerResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        Provider provider = new Provider();
        provider.setId(providerVO.getId());
        int count = providerDao.deleteDBById(provider);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        // 厂商删除后清空该厂商下所有模型缓存
        modelFactory.clearModelCacheByProvider(providerVO.getId());
        agentFactory.clearAllAgentCache();
        result.setObjId(providerVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询模型厂商
     * @param query 查询模型厂商条件
     * @return 分页结果
     */
    @Override
    @DataScope
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
    @LogOperate(operateType = OperateTypeEnum.PROVIDER_DELETE)
    public ResultData<ProviderResult> queryProviderDetail(ProviderQuery query) {
        ResultData<ProviderResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        super.scopeTenant(query);
        ProviderResult providerResult = providerDao.queryProvider(query);
        if (providerResult == null) {
            result.setErrorCode(ErrorCodeEnum.PROVIDER_NOT_EXIST);
            return result;
        }
        result.setData(providerResult);
        result.setObjId(providerResult.getId());
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

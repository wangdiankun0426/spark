package com.spark.llm.service.impl;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.llm.entity.ProviderKey;
import com.spark.common.bean.llm.query.ProviderKeyQuery;
import com.spark.common.bean.llm.query.ProviderQuery;
import com.spark.common.bean.llm.result.ProviderKeyResult;
import com.spark.common.bean.llm.result.ProviderResult;
import com.spark.common.bean.llm.vo.ProviderKeyVO;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.common.utils.BeanUtil;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.dao.llm.ProviderDao;
import com.spark.dao.llm.ProviderKeyDao;
import com.spark.llm.agent.AgentFactory;
import com.spark.llm.model.factory.ModelFactory;
import com.spark.llm.security.LlmApiKeyCipher;
import com.spark.llm.service.IProviderKeyService;
import com.spark.manage.BaseService;
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
 * @since 2026-11-18 15:00:00
 * 模型厂商密钥服务实现
 */
@Service
public class ProviderKeyServiceImpl extends BaseService<ProviderKeyQuery, ProviderKeyResult> implements IProviderKeyService {
    private static final int SECRET_KEY_MAX_LENGTH = 256;
    @Autowired
    private ProviderKeyDao providerKeyDao;
    @Autowired
    private ProviderDao providerDao;
    @Autowired
    private LlmApiKeyCipher llmApiKeyCipher;
    @Autowired
    private ModelFactory modelFactory;
    @Autowired
    private AgentFactory agentFactory;

    /**
     * 创建模型厂商密钥
     * @param providerKeyVO 模型厂商密钥数据
     * @return 创建结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.PROVIDER_KEY_INSERT)
    public ResultData<Void> createProviderKey(ProviderKeyVO providerKeyVO) {
        ResultData<Void> result = new ResultData<>();
        if (providerKeyVO == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (providerKeyVO.getProviderId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (StringUtil.isBlank(providerKeyVO.getSecretKey())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (providerKeyVO.getSecretKey().length() > SECRET_KEY_MAX_LENGTH) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Integer status = providerKeyVO.getStatus();
        if (status != null && StatusEnum.indexOf(status) == StatusEnum.UNKNOWN) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        boolean providerExists = this.checkProviderExists(providerKeyVO.getProviderId());
        if (!providerExists) {
            result.setErrorCode(ErrorCodeEnum.PROVIDER_NOT_EXIST);
            return result;
        }
        ProviderKey providerKey = new ProviderKey();
        BeanUtil.copyProperties(providerKeyVO, providerKey);
        String secretKey = providerKeyVO.getSecretKey();
        String encryptedKey = llmApiKeyCipher.encrypt(secretKey);
        if (encryptedKey.length() > 512) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String keyLast4 = llmApiKeyCipher.last4(secretKey);
        providerKey.setSecretKey(encryptedKey);
        providerKey.setKeyLast4(keyLast4);
        if (status == null) {
            providerKey.setStatus(StatusEnum.NORMAL.getValue());
        }
        int count = providerKeyDao.insertDB(providerKey);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        modelFactory.clearModelCacheByProvider(providerKeyVO.getProviderId());
        agentFactory.clearAllAgentCache();
        result.setObjId(providerKey.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改模型厂商密钥
     * @param providerKeyVO 模型厂商密钥数据
     * @return 修改结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.PROVIDER_KEY_UPDATE)
    public ResultData<Void> updateProviderKey(ProviderKeyVO providerKeyVO) {
        ResultData<Void> result = new ResultData<>();
        if (providerKeyVO == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (providerKeyVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String secretKey = providerKeyVO.getSecretKey();
        if (StringUtil.isNotBlank(secretKey) && secretKey.length() > SECRET_KEY_MAX_LENGTH) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Integer status = providerKeyVO.getStatus();
        if (status != null && StatusEnum.indexOf(status) == StatusEnum.UNKNOWN) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ProviderKeyQuery providerKeyQuery = new ProviderKeyQuery();
        providerKeyQuery.setId(providerKeyVO.getId());
        ProviderKeyResult providerKeyResult = providerKeyDao.queryProviderKey(providerKeyQuery);
        if (providerKeyResult == null) {
            result.setErrorCode(ErrorCodeEnum.PROVIDER_KEY_NOT_EXIST);
            return result;
        }
        Long providerId = providerKeyResult.getProviderId();
        boolean providerExists = this.checkProviderExists(providerId);
        if (!providerExists) {
            result.setErrorCode(ErrorCodeEnum.PROVIDER_NOT_EXIST);
            return result;
        }
        ProviderKey providerKey = new ProviderKey();
        BeanUtil.copyProperties(providerKeyVO, providerKey);
        if (StringUtil.isNotBlank(secretKey)) {
            String encryptedKey = llmApiKeyCipher.encrypt(secretKey);
            if (encryptedKey.length() > 512) {
                result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
                return result;
            }
            String keyLast4 = llmApiKeyCipher.last4(secretKey);
            providerKey.setSecretKey(encryptedKey);
            providerKey.setKeyLast4(keyLast4);
            providerKey.setCooldownUntil(null);
        } else {
            providerKey.setSecretKey(null);
        }
        int count = providerKeyDao.updateDBById(providerKey);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        modelFactory.clearModelCacheByProvider(providerId);
        agentFactory.clearAllAgentCache();
        result.setObjId(providerKeyVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除模型厂商密钥
     * @param providerKeyVO 模型厂商密钥数据
     * @return 删除结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.PROVIDER_KEY_DELETE)
    public ResultData<Void> deleteProviderKey(ProviderKeyVO providerKeyVO) {
        ResultData<Void> result = new ResultData<>();
        if (providerKeyVO == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (providerKeyVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ProviderKeyQuery providerKeyQuery = new ProviderKeyQuery();
        providerKeyQuery.setId(providerKeyVO.getId());
        ProviderKeyResult providerKeyResult = providerKeyDao.queryProviderKey(providerKeyQuery);
        if (providerKeyResult == null) {
            result.setErrorCode(ErrorCodeEnum.PROVIDER_KEY_NOT_EXIST);
            return result;
        }
        Long providerId = providerKeyResult.getProviderId();
        boolean providerExists = this.checkProviderExists(providerId);
        if (!providerExists) {
            result.setErrorCode(ErrorCodeEnum.PROVIDER_NOT_EXIST);
            return result;
        }
        ProviderKey providerKey = new ProviderKey();
        providerKey.setId(providerKeyVO.getId());
        int count = providerKeyDao.deleteDBById(providerKey);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        modelFactory.clearModelCacheByProvider(providerId);
        agentFactory.clearAllAgentCache();
        result.setObjId(providerKeyVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询模型厂商密钥
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    public ResultData<PageResult<ProviderKeyResult>> pageProviderKeyList(ProviderKeyQuery query) {
        ResultData<PageResult<ProviderKeyResult>> result = new ResultData<>();
        if (query == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (query.getProviderId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        boolean providerExists = this.checkProviderExists(query.getProviderId());
        if (!providerExists) {
            result.setErrorCode(ErrorCodeEnum.PROVIDER_NOT_EXIST);
            return result;
        }
        PageResult<ProviderKeyResult> pageResult = super.pageList(query);
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询模型厂商密钥详情
     * @param query 查询条件
     * @return 详情
     */
    @Override
    public ResultData<ProviderKeyResult> queryProviderKeyDetail(ProviderKeyQuery query) {
        ResultData<ProviderKeyResult> result = new ResultData<>();
        if (query == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ProviderKeyResult providerKeyResult = providerKeyDao.queryProviderKey(query);
        if (providerKeyResult == null) {
            result.setErrorCode(ErrorCodeEnum.PROVIDER_KEY_NOT_EXIST);
            return result;
        }
        Long providerId = providerKeyResult.getProviderId();
        boolean providerExists = this.checkProviderExists(providerId);
        if (!providerExists) {
            result.setErrorCode(ErrorCodeEnum.PROVIDER_NOT_EXIST);
            return result;
        }
        result.setData(providerKeyResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<ProviderKeyResult> list) {
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
    protected int queryCount(ProviderKeyQuery query) {
        return providerKeyDao.queryProviderKeyCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<ProviderKeyResult> queryList(ProviderKeyQuery query) {
        return providerKeyDao.queryProviderKeyList(query);
    }

    /**
     * 校验父厂商是否可见
     * @param providerId 厂商id
     * @return 是否存在且可见
     */
    private boolean checkProviderExists(Long providerId) {
        if (providerId == null) {
            return false;
        }
        ProviderQuery providerQuery = new ProviderQuery();
        providerQuery.setId(providerId);
        super.scopeTenant(providerQuery);
        ProviderResult providerResult = providerDao.queryProvider(providerQuery);
        return providerResult != null;
    }

}

package com.spark.llm.service.impl;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.llm.entity.Model;
import com.spark.common.bean.llm.query.ProviderQuery;
import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.result.ProviderResult;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.common.bean.llm.vo.ModelVO;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.dao.llm.ModelDao;
import com.spark.dao.llm.ProviderDao;
import com.spark.common.enums.DeleteFlagEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.ModelTypeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.llm.agent.AgentFactory;
import com.spark.llm.model.factory.ModelFactory;
import com.spark.llm.model.fallback.ModelFallbackChainParser;
import com.spark.llm.model.invocation.ModelInvoker;
import com.spark.llm.model.error.ModelRetryPolicy;
import com.spark.llm.service.IModelService;
import com.spark.common.utils.CollectionUtil;
import com.spark.manage.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.common.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-05-14 09:49:10
 */
@Service
public class ModelServiceImpl extends BaseService<ModelQuery, ModelResult> implements IModelService {
    private final static Logger logger = LoggerFactory.getLogger(ModelServiceImpl.class);
    @Autowired
    private ModelDao modelDao;
    @Autowired
    private ModelFactory modelFactory;
    @Autowired
    private ProviderDao providerDao;
    @Autowired
    private AgentFactory agentFactory;

    /**
     * 创建模型
     * @param modelVO 模型数据
     * @return 创建结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.MODEL_INSERT)
    public ResultData<Void> createModel(ModelVO modelVO) {
        ResultData<Void> result = new ResultData<>();
        if (modelVO == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Integer timeoutMs = modelVO.getTimeoutMs();
        if (!this.isTimeoutValid(timeoutMs)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Integer maxRetries = modelVO.getMaxRetries();
        if (!this.isMaxRetriesValid(maxRetries)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Integer contextLength = modelVO.getContextLength();
        if (!this.isContextLengthValid(contextLength)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String fallbackChain = modelVO.getFallbackModelIds();
        Long tenantId = modelVO.getTenantId();
        if (!SessionHolder.isSysAdmin() || tenantId == null) {
            tenantId = SessionHolder.getCurrentTenantId();
        }
        Integer modelType = modelVO.getType();
        List<Long> fallbackModelIds = this.resolveFallbackModelIds(fallbackChain, null, tenantId, modelType);
        if (fallbackModelIds == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String normalizedFallbackModelIds = ModelFallbackChainParser.normalize(fallbackModelIds);
        Model model = new Model();
        BeanUtil.copyProperties(modelVO, model);
        model.setFallbackModelIds(normalizedFallbackModelIds);
        // 非站点管理员强制归属当前租户
        if (!SessionHolder.isSysAdmin()) {
            model.setTenantId(SessionHolder.getCurrentTenantId());
        }
        Long id = super.genObjectId(ObjectTypeEnum.MODEL);
        model.setId(id);
        int count = modelDao.insertDB(model);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        logger.info("model created with id={}, cache cleared", model.getId());
        result.setObjId(model.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改模型
     * @param modelVO 模型数据
     * @return 修改结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.MODEL_UPDATE)
    public ResultData<Void> updateModel(ModelVO modelVO) {
        ResultData<Void> result = new ResultData<>();
        if (modelVO == null || modelVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Integer timeoutMs = modelVO.getTimeoutMs();
        if (!this.isTimeoutValid(timeoutMs)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Integer maxRetries = modelVO.getMaxRetries();
        if (!this.isMaxRetriesValid(maxRetries)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Integer contextLength = modelVO.getContextLength();
        if (!this.isContextLengthValid(contextLength)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ModelQuery modelQuery = new ModelQuery();
        modelQuery.setId(modelVO.getId());
        super.scopeTenant(modelQuery);
        ModelResult modelResult = modelDao.queryModel(modelQuery);
        if (modelResult == null) {
            result.setErrorCode(ErrorCodeEnum.MODEL_NOT_EXIST);
            return result;
        }
        Integer effectiveType = modelVO.getType();
        if (effectiveType == null) {
            effectiveType = modelResult.getType();
        }
        String fallbackChain = modelVO.getFallbackModelIds();
        Long rootId = modelVO.getId();
        Long tenantId = modelResult.getTenantId();
        List<Long> fallbackModelIds = this.resolveFallbackModelIds(fallbackChain, rootId, tenantId, effectiveType);
        if (fallbackModelIds == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String normalizedFallbackModelIds = ModelFallbackChainParser.normalize(fallbackModelIds);
        Model model = new Model();
        BeanUtil.copyProperties(modelVO, model);
        model.setFallbackModelIds(normalizedFallbackModelIds);
        int count = modelDao.updateDBById(model);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        // 清除模型缓存，确保下次获取时使用最新配置
        modelFactory.clearModelCache(modelVO.getId());
        agentFactory.clearAllAgentCache();
        logger.info("model updated with id={}, cache cleared", modelVO.getId());
        result.setObjId(modelVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除模型
     * @param modelVO 模型数据
     * @return  删除结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.MODEL_DELETE)
    public ResultData<Void> deleteModel(ModelVO modelVO) {
        ResultData<Void> result = new ResultData<>();
        if (modelVO == null || modelVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ModelQuery modelQuery = new ModelQuery();
        modelQuery.setId(modelVO.getId());
        super.scopeTenant(modelQuery);
        ModelResult modelResult = modelDao.queryModel(modelQuery);
        if (modelResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        Model model = new Model();
        model.setId(modelVO.getId());
        int count = modelDao.deleteDBById(model);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        // 清除模型缓存
        modelFactory.clearModelCache(modelVO.getId());
        agentFactory.clearAllAgentCache();
        logger.info("model deleted with id={}, cache cleared", modelVO.getId());
        result.setObjId(modelVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询模型
     * @param query 查询模型条件
     * @return 分页结果
     */
    @Override
    @DataScope
    public ResultData<PageResult<ModelResult>> pageModelList(ModelQuery query) {
        ResultData<PageResult<ModelResult>> result = new ResultData<>();
        if (query == null) {
            query = new ModelQuery();
        }
        PageResult<ModelResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询模型详情
     * @param query 查询模型条件
     * @return 详情
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.MODEL_DETAIL)
    public ResultData<ModelResult> queryModelDetail(ModelQuery query) {
        ResultData<ModelResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        super.scopeTenant(query);
        ModelResult modelResult = modelDao.queryModel(query);
        if (modelResult == null) {
            result.setErrorCode(ErrorCodeEnum.MODEL_NOT_EXIST);
            return result;
        }
        result.setData(modelResult);
        result.setObjId(modelResult.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 统计可用模型数量，与模型列表同口径
     * @param query 查询参数
     * @return 数量结果
     */
    @Override
    @DataScope
    public ResultData<Integer> countMyModels(ModelQuery query) {
        ResultData<Integer> result = new ResultData<>();
        if (query == null) {
            query = new ModelQuery();
        }
        if (!SessionHolder.isSysAdmin()) {
            query.setTenantId(SessionHolder.getCurrentTenantId());
        }
        int count = modelDao.queryModelCount(query);
        result.setData(count);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<ModelResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
        List<Long> providerIds = list.stream().map(ModelResult::getProviderId).distinct().toList();
        ProviderQuery providerQuery = new ProviderQuery();
        providerQuery.setIds(providerIds);
        providerQuery.setPage(false);
        List<ProviderResult> providerList = providerDao.queryProviderList(providerQuery);
        Map<Long, String> providerNameMap = providerList.stream().collect(Collectors.toMap(ProviderResult::getId, ProviderResult::getName));
        for (ModelResult modelResult : list) {
            modelResult.setProviderName(providerNameMap.get(modelResult.getProviderId()));
            modelResult.setTypeName(ModelTypeEnum.indexOf(modelResult.getType()).getDesc());
            modelResult.setStatusName(StatusEnum.indexOf(modelResult.getStatus()).getDesc());
        }

    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(ModelQuery query) {
        return modelDao.queryModelCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<ModelResult> queryList(ModelQuery query) {
        return modelDao.queryModelList(query);
    }

    /**
     * 查询模型最大id
     * @return 模型最大id
     */
    @Override
    protected Long queryMaxId() {
        return modelDao.queryModelMaxId();
    }

    /**
     * 校验模型调用超时范围
     * @param timeoutMs 超时毫秒
     * @return 是否有效
     */
    private boolean isTimeoutValid(Integer timeoutMs) {
        if (timeoutMs == null) {
            return true;
        }
        return timeoutMs >= ModelInvoker.MIN_TIMEOUT_MS && timeoutMs <= ModelInvoker.MAX_TIMEOUT_MS;
    }

    /**
     * 校验额外重试次数
     * @param maxRetries 重试次数
     * @return 是否有效
     */
    private boolean isMaxRetriesValid(Integer maxRetries) {
        if (maxRetries == null) {
            return true;
        }
        return maxRetries >= ModelRetryPolicy.MIN_MAX_RETRIES && maxRetries <= ModelRetryPolicy.MAX_MAX_RETRIES;
    }

    /**
     * 校验模型上下文窗口
     * @param contextLength 上下文窗口token数
     * @return 是否有效
     */
    private boolean isContextLengthValid(Integer contextLength) {
        if (contextLength == null) {
            return true;
        }
        return contextLength >= 1;
    }

    /**
     * 解析降级链并校验候选模型
     * @param fallbackChain 降级模型id列表
     * @param rootId 主模型id
     * @param tenantId 原模型租户id
     * @param type 有效模型类型
     * @return 有序模型id列表
     */
    private List<Long> resolveFallbackModelIds(String fallbackChain, Long rootId, Long tenantId, Integer type) {
        if (!ModelFallbackChainParser.isValidStrict(fallbackChain, rootId)) {
            return null;
        }
        List<Long> fallbackModelIds = ModelFallbackChainParser.parseStrict(fallbackChain, rootId);
        if (fallbackModelIds.isEmpty()) {
            return fallbackModelIds;
        }
        if (tenantId == null || ModelTypeEnum.indexOf(type) == ModelTypeEnum.UNKNOWN) {
            return null;
        }
        ModelQuery candidateQuery = new ModelQuery();
        candidateQuery.setIds(fallbackModelIds);
        candidateQuery.setTenantId(tenantId);
        candidateQuery.setType(type);
        candidateQuery.setStatus(StatusEnum.NORMAL.getValue());
        candidateQuery.setDeleteFlag(1);
        candidateQuery.setPage(false);
        List<ModelResult> candidateList = modelDao.queryModelList(candidateQuery);
        if (CollectionUtil.isEmpty(candidateList) || candidateList.size() != fallbackModelIds.size()) {
            return null;
        }
        Set<Long> candidateIds = candidateList.stream().map(ModelResult::getId).collect(Collectors.toSet());
        if (!candidateIds.containsAll(fallbackModelIds)) {
            return null;
        }
        for (ModelResult candidate : candidateList) {
            if (!DeleteFlagEnum.NORMAL.getValue().equals(candidate.getDeleteFlag())) {
                return null;
            }
        }
        return fallbackModelIds;
    }
}

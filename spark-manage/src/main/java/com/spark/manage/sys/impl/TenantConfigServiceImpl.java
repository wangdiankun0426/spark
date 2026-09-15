package com.spark.manage.sys.impl;

import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.entity.TenantConfig;
import com.spark.common.bean.sys.query.TenantConfigQuery;
import com.spark.common.bean.sys.result.PasswordRuleResult;
import com.spark.common.bean.sys.result.TenantConfigResult;
import com.spark.common.bean.sys.vo.TenantConfigVO;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.TenantConfigEnum;
import com.spark.common.utils.BeanUtil;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.spark.dao.sys.TenantConfigDao;
import com.spark.manage.BaseService;
import com.spark.manage.sys.ITenantConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-11 10:00:00
 * 租户配置服务
 */
@Service
@LogPrint
public class TenantConfigServiceImpl extends BaseService<TenantConfigQuery, TenantConfigResult> implements ITenantConfigService {
    private final static Logger logger = LoggerFactory.getLogger(TenantConfigServiceImpl.class);
    @Autowired
    private TenantConfigDao tenantConfigDao;

    /**
     * 创建租户配置
     * @param tenantConfigVO 租户配置参数
     * @return 创建结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.TENANT_CONFIG_INSERT)
    public ResultData<Void> createTenantConfig(TenantConfigVO tenantConfigVO) {
        ResultData<Void> result = new ResultData<>();
        if (tenantConfigVO == null || tenantConfigVO.getTenantId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (StringUtil.isBlank(tenantConfigVO.getName())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (StringUtil.isBlank(tenantConfigVO.getKey())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        TenantConfigQuery tenantConfigQuery = new TenantConfigQuery();
        tenantConfigQuery.setTenantId(tenantConfigVO.getTenantId());
        tenantConfigQuery.setKey(tenantConfigVO.getKey());
        TenantConfigResult existConfig = tenantConfigDao.queryTenantConfig(tenantConfigQuery);
        if (existConfig != null) {
            result.setErrorCode(ErrorCodeEnum.TENANT_CONFIG_KEY_EXIST);
            return result;
        }
        TenantConfig tenantConfig = new TenantConfig();
        BeanUtil.copyProperties(tenantConfigVO, tenantConfig);
        int count = tenantConfigDao.insertDB(tenantConfig);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        result.setObjId(tenantConfig.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改租户配置
     * @param tenantConfigVO 租户配置参数
     * @return 修改结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.TENANT_CONFIG_UPDATE)
    public ResultData<Void> updateTenantConfig(TenantConfigVO tenantConfigVO) {
        ResultData<Void> result = new ResultData<>();
        if (tenantConfigVO == null || tenantConfigVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (StringUtil.isBlank(tenantConfigVO.getName())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        TenantConfig tenantConfig = new TenantConfig();
        BeanUtil.copyProperties(tenantConfigVO, tenantConfig);
        int count = tenantConfigDao.updateDBById(tenantConfig);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setObjId(tenantConfigVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除租户配置
     * @param tenantConfigVO 租户配置参数
     * @return 删除结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.TENANT_CONFIG_DELETE)
    public ResultData<Void> deleteTenantConfig(TenantConfigVO tenantConfigVO) {
        ResultData<Void> result = new ResultData<>();
        if (tenantConfigVO == null || tenantConfigVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        TenantConfig tenantConfig = new TenantConfig();
        tenantConfig.setId(tenantConfigVO.getId());
        int count = tenantConfigDao.deleteDBById(tenantConfig);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        result.setObjId(tenantConfigVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询租户配置列表
     * @param query 查询参数
     * @return 分页结果
     */
    @Override
    public ResultData<PageResult<TenantConfigResult>> pageTenantConfigList(TenantConfigQuery query) {
        ResultData<PageResult<TenantConfigResult>> result = new ResultData<>();
        if (query == null) {
            query = new TenantConfigQuery();
        }
        PageResult<TenantConfigResult> pageResult = super.pageList(query);
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 创建租户默认配置
     * @param tenantId 租户id
     * @return 创建结果
     */
    @Override
    public ResultData<Integer> createDefaultConfig(Long tenantId) {
        ResultData<Integer> result = new ResultData<>();
        if (tenantId == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        int count = 0;
        for (TenantConfigEnum tenantConfigEnum : TenantConfigEnum.values()) {
            TenantConfig tenantConfig = new TenantConfig();
            tenantConfig.setTenantId(tenantId);
            tenantConfig.setName(tenantConfigEnum.getName());
            tenantConfig.setKey(tenantConfigEnum.getKey());
            tenantConfig.setValue(tenantConfigEnum.getValue());
            count += tenantConfigDao.insertDB(tenantConfig);
        }
        result.setData(count);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询租户配置value
     * @param tenantId
     * @param key
     * @return
     */
    @Override
    public ResultData<String> queryTenantConfigValue(Long tenantId, String key) {
        ResultData<String> result = new ResultData<>();
        if (tenantId == null || StringUtil.isBlank(key)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        TenantConfigQuery tenantConfigQuery = new TenantConfigQuery();
        tenantConfigQuery.setTenantId(tenantId);
        tenantConfigQuery.setKey(key);
        TenantConfigResult tenantConfigResult = tenantConfigDao.queryTenantConfig(tenantConfigQuery);
        if (tenantConfigResult == null) {
            result.setErrorCode(ErrorCodeEnum.TENANT_CONFIG_NOT_EXIST);
            return result;
        }
        result.setData(tenantConfigResult.getValue());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询租户全部配置
     * @param tenantId 租户id
     * @return 配置key-value
     */
    @Override
    public ResultData<Map<String, String>> queryTenantConfigMap(Long tenantId) {
        ResultData<Map<String, String>> result = new ResultData<>();
        if (tenantId == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        TenantConfigQuery tenantConfigQuery = new TenantConfigQuery();
        tenantConfigQuery.setTenantId(tenantId);
        tenantConfigQuery.setPage(false);
        List<TenantConfigResult> tenantConfigList = tenantConfigDao.queryTenantConfigList(tenantConfigQuery);
        if (CollectionUtil.isEmpty(tenantConfigList)) {
            result.setErrorCode(ErrorCodeEnum.TENANT_CONFIG_NOT_EXIST);
            return result;
        }
        Map<String, String> configMap = tenantConfigList.stream()
                .filter(v -> StringUtil.isNotBlank(v.getKey()) && StringUtil.isNotBlank(v.getValue()))
                .collect(Collectors.toMap(TenantConfigResult::getKey, TenantConfigResult::getValue, (v1, v2) -> v2));
        result.setData(configMap);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询密码长度规则
     * @param tenantId 租户id，为空时使用枚举默认值
     * @return 密码长度规则
     */
    @Override
    public ResultData<PasswordRuleResult> queryPasswordRule(Long tenantId) {
        ResultData<PasswordRuleResult> result = new ResultData<>();
        Integer minLength = this.queryPwdLengthValue(tenantId, TenantConfigEnum.PWD_MIN_LENGTH);
        Integer maxLength = this.queryPwdLengthValue(tenantId, TenantConfigEnum.PWD_MAX_LENGTH);
        if (minLength > maxLength) {
            logger.warn("queryPasswordRule invalid pwd length config, tenantId={}, minLength={}, maxLength={}", tenantId, minLength, maxLength);
            minLength = Integer.valueOf(TenantConfigEnum.PWD_MIN_LENGTH.getValue());
            maxLength = Integer.valueOf(TenantConfigEnum.PWD_MAX_LENGTH.getValue());
        }
        PasswordRuleResult passwordRuleResult = new PasswordRuleResult();
        passwordRuleResult.setMinLength(minLength);
        passwordRuleResult.setMaxLength(maxLength);
        result.setData(passwordRuleResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询密码长度配置值，缺失或非法时回退枚举默认值
     * @param tenantId 租户id，为空时直接使用枚举默认值
     * @param tenantConfigEnum 密码长度配置枚举
     * @return 密码长度
     */
    private Integer queryPwdLengthValue(Long tenantId, TenantConfigEnum tenantConfigEnum) {
        Integer defaultValue = Integer.valueOf(tenantConfigEnum.getValue());
        if (tenantId == null) {
            return defaultValue;
        }
        ResultData<String> valueResult = this.queryTenantConfigValue(tenantId, tenantConfigEnum.getKey());
        if (valueResult.getCode() != ResultData.OK) {
            return defaultValue;
        }
        String value = valueResult.getData();
        if (StringUtil.isBlank(value) || !StringUtil.isNumeric(value.trim())) {
            return defaultValue;
        }
        return Integer.valueOf(value.trim());
    }

    /**
     * 补充列表数据
     * @param list 租户配置列表
     */
    @Override
    protected void supplyList(List<TenantConfigResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
    }

    /**
     * 分页查询总数
     * @param query 查询参数
     * @return 总数
     */
    @Override
    protected int queryCount(TenantConfigQuery query) {
        return tenantConfigDao.queryTenantConfigCount(query);
    }

    /**
     * 分页查询列表
     * @param query 查询参数
     * @return 租户配置列表
     */
    @Override
    protected List<TenantConfigResult> queryList(TenantConfigQuery query) {
        return tenantConfigDao.queryTenantConfigList(query);
    }
}

package com.spark.form.service.impl;

import com.spark.common.bean.base.BaseException;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.form.entity.FormObj;
import com.spark.common.bean.form.entity.FormObjValue;
import com.spark.common.bean.form.query.FormObjQuery;
import com.spark.common.bean.form.query.FormObjValueQuery;
import com.spark.common.bean.form.query.FormQuery;
import com.spark.common.bean.form.result.FormObjResult;
import com.spark.common.bean.form.result.FormObjValueResult;
import com.spark.common.bean.form.result.FormResult;
import com.spark.common.bean.form.vo.FormObjValueVO;
import com.spark.dao.form.FormDao;
import com.spark.dao.form.FormObjDao;
import com.spark.dao.form.FormObjValueDao;
import com.spark.common.enums.DeleteFlagEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.FormTypeEnum;
import com.spark.form.service.IFormObjValueService;
import com.spark.form.service.IFormService;
import com.spark.form.util.FormSchemaUtil;
import com.spark.manage.BaseService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/7/28 20:52
 */
@Service
public class FormObjValueServiceImpl extends BaseService<FormObjQuery, FormObjResult> implements IFormObjValueService {
    private final static Logger logger = LoggerFactory.getLogger(FormObjValueServiceImpl.class);
    @Autowired
    private FormObjValueDao formObjValueDao;
    @Autowired
    private FormObjDao formObjDao;
    @Autowired
    private FormDao formDao;
    @Autowired
    private IFormService formService;

    /**
     * 创建表单对象值
     * @param valueVO 表单对象值
     * @return 创建结果
     */
    // 独立事务在流程引擎启动前提交，表单值写入失败整体回滚避免孤儿 form_obj，同时保住引擎写即可见
    @Override
    @Transactional(transactionManager = "primaryTransactionManager", rollbackFor = Exception.class)
    public ResultData<Void> saveFormObjValues(FormObjValueVO valueVO) {
        return this.saveFormObjValuesInternal(valueVO, false);
    }

    /**
     * 创建或更新表单对象值
     * @param valueVO 表单对象值
     * @return 保存结果
     */
    @Override
    @Transactional(transactionManager = "primaryTransactionManager", rollbackFor = Exception.class)
    public ResultData<Void> saveOrUpdateFormObjValues(FormObjValueVO valueVO) {
        return this.saveFormObjValuesInternal(valueVO, true);
    }

    /**
     * 查询对象表单值列表
     * @param query 查询参数
     * @return 表单值列表
     */
    @Override
    public ResultData<List<FormObjValueResult>> queryFormObjValueList(FormObjValueQuery query) {
        ResultData<List<FormObjValueResult>> result = new ResultData<>();
        if (query == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long formId = query.getFormId();
        if (formId == null && query.getObjId() != null) {
            FormObjQuery formObjQuery = new FormObjQuery();
            formObjQuery.setObjId(query.getObjId());
            FormObjResult formObjResult = formObjDao.queryFormObj(formObjQuery);
            if (formObjResult != null) {
                formId = formObjResult.getFormId();
            }
        }
        if (!this.isFormInTenant(formId)) {
            result.setErrorCode(ErrorCodeEnum.FORM_NOT_EXIST);
            return result;
        }
        List<FormObjValueResult> list = formObjValueDao.queryFormObjValueList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询对象表单值
     * @param query
     * @return
     */
    @Override
    public ResultData<FormObjResult> queryFormObjDetail(FormObjQuery query) {
        ResultData<FormObjResult> result = new ResultData<>();
        if (query == null || query.getObjId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FormObjResult formObjResult = formObjDao.queryFormObj(query);
        if (formObjResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        if (!this.isFormInTenant(formObjResult.getFormId())) {
            result.setErrorCode(ErrorCodeEnum.FORM_NOT_EXIST);
            return result;
        }
        Long objId = formObjResult.getObjId();
        Long formId = formObjResult.getFormId();
        Long formRevId = formObjResult.getFormRevId();
        FormQuery formQuery = new FormQuery();
        formQuery.setId(formId);
        formQuery.setRevId(formRevId);
        ResultData<String> formResult = formService.queryFormJson(formQuery);
        if (formResult.getCode() != ResultData.OK) {
            int errorCode = formResult.getCode();
            String errorMessage = formResult.getMessage();
            result.setCode(errorCode);
            result.setMessage(errorMessage);
            return result;
        }
        String formJson = formResult.getData();
        formObjResult.setFormJson(formJson);
        FormObjValueQuery objValueQuery = new FormObjValueQuery();
        objValueQuery.setObjId(objId);
        objValueQuery.setFormId(formId);
        List<FormObjValueResult> list = formObjValueDao.queryFormObjValueList(objValueQuery);
        formObjResult.setValues(list);
        result.setData(formObjResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 保存表单对象值
     * @param valueVO 表单对象值
     * @param allowUpdate 是否允许更新
     * @return 保存结果
     */
    private ResultData<Void> saveFormObjValuesInternal(FormObjValueVO valueVO, boolean allowUpdate) {
        ResultData<Void> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        if (valueVO == null || valueVO.getObjId() == null || valueVO.getFormId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long formId = valueVO.getFormId();
        Long objId = valueVO.getObjId();
        FormResult formResult = this.queryActiveFormInTenant(formId);
        if (formResult == null) {
            result.setErrorCode(ErrorCodeEnum.FORM_NOT_EXIST);
            return result;
        }
        FormObjQuery formObjQuery = new FormObjQuery();
        formObjQuery.setObjId(objId);
        FormObjResult formObjResult = formObjDao.queryFormObj(formObjQuery);
        if (!allowUpdate && formObjResult != null) {
            result.setErrorCode(ErrorCodeEnum.FORM_OBJ_EXIST);
            return result;
        }
        Long formRevId;
        if (formObjResult == null) {
            formRevId = valueVO.getFormRevId();
            if (formRevId == null) {
                formRevId = formResult.getRevId();
                valueVO.setFormRevId(formRevId);
            }
        } else {
            Long boundFormId = formObjResult.getFormId();
            if (!Objects.equals(formId, boundFormId)) {
                result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
                result.setMessage("表单绑定关系不允许修改");
                return result;
            }
            Integer formType = formResult.getType();
            Integer metadataType = FormTypeEnum.METADATA.getValue();
            if (!metadataType.equals(formType)) {
                result.setErrorCode(ErrorCodeEnum.FORM_OBJ_EXIST);
                return result;
            }
            Long requestFormRevId = valueVO.getFormRevId();
            formRevId = formObjResult.getFormRevId();
            if (requestFormRevId != null && !Objects.equals(requestFormRevId, formRevId)) {
                result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
                result.setMessage("表单版本绑定关系不允许修改");
                return result;
            }
        }
        if (formRevId == null) {
            result.setErrorCode(ErrorCodeEnum.FORM_VERSION_NOT_EXIST);
            result.setMessage("历史数据缺少表单版本，无法保存");
            return result;
        }
        FormQuery formQuery = new FormQuery();
        formQuery.setId(formId);
        formQuery.setRevId(formRevId);
        ResultData<String> schemaResult = formService.queryFormJson(formQuery);
        if (schemaResult.getCode() != ResultData.OK) {
            int errorCode = schemaResult.getCode();
            String errorMessage = schemaResult.getMessage();
            result.setCode(errorCode);
            result.setMessage(errorMessage);
            return result;
        }
        String formJson = schemaResult.getData();
        if (StringUtil.isBlank(formJson)) {
            result.setErrorCode(ErrorCodeEnum.FORM_VERSION_NOT_EXIST);
            result.setMessage("表单版本内容不存在");
            return result;
        }
        List<FormObjValue> originalValues = valueVO.getValues();
        ResultData<List<FormObjValue>> normalizeResult = FormSchemaUtil.normalizeValues(formJson, originalValues);
        if (normalizeResult.getCode() != ResultData.OK) {
            int errorCode = normalizeResult.getCode();
            String errorMessage = normalizeResult.getMessage();
            result.setCode(errorCode);
            result.setMessage(errorMessage);
            return result;
        }
        List<FormObjValue> list = normalizeResult.getData();
        valueVO.setValues(list);
        int count;
        if (formObjResult == null) {
            FormObj formObj = new FormObj();
            formObj.setFormId(formId);
            formObj.setFormRevId(formRevId);
            formObj.setObjId(objId);
            count = formObjDao.insertDB(formObj);
            if (count < 1) {
                logger.error("insert data error");
                throw new BaseException(ErrorCodeEnum.INSERT_DATA_FAIL);
            }
        } else {
            count = formObjValueDao.deleteDBByObjId(objId);
            if (count < 0) {
                throw new BaseException(ErrorCodeEnum.DELETE_DATA_FAIL);
            }
        }
        for (FormObjValue value : list) {
            value.setObjId(objId);
            value.setFormId(formId);
            value.setCreatedBy(userId);
        }
        if (CollectionUtil.isNotEmpty(list)) {
            count = formObjValueDao.batchInsert(list);
            if (count < 1) {
                throw new BaseException(ErrorCodeEnum.INSERT_DATA_FAIL);
            }
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 校验表单归属当前租户
     * @param formId 表单id
     * @return true归属当前租户
     */
    private boolean isFormInTenant(Long formId) {
        FormResult formResult = this.queryFormInTenant(formId);
        return formResult != null;
    }

    /**
     * 查询当前租户内可继续写入的表单
     * @param formId 表单id
     * @return 存活表单详情
     */
    private FormResult queryActiveFormInTenant(Long formId) {
        if (formId == null) {
            return null;
        }
        FormQuery formQuery = new FormQuery();
        formQuery.setId(formId);
        super.scopeTenant(formQuery);
        FormResult formResult = formDao.queryFormForUpdate(formQuery);
        if (formResult == null) {
            return null;
        }
        Integer deleteFlag = formResult.getDeleteFlag();
        Integer normalFlag = DeleteFlagEnum.NORMAL.getValue();
        if (!normalFlag.equals(deleteFlag)) {
            return null;
        }
        return formResult;
    }

    /**
     * 查询当前租户可见表单
     * @param formId 表单id
     * @return 表单详情
     */
    private FormResult queryFormInTenant(Long formId) {
        if (formId == null) {
            return null;
        }
        FormQuery formQuery = new FormQuery();
        formQuery.setId(formId);
        super.scopeTenant(formQuery);
        return formDao.queryForm(formQuery);
    }
}

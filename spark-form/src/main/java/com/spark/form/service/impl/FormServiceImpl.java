package com.spark.form.service.impl;

import com.spark.common.bean.base.BaseException;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.form.entity.Form;
import com.spark.common.bean.form.entity.FormVersion;
import com.spark.common.bean.form.query.FormQuery;
import com.spark.common.bean.form.query.FormVersionQuery;
import com.spark.common.bean.form.result.FormResult;
import com.spark.common.bean.form.result.FormVersionResult;
import com.spark.common.bean.form.vo.FormFieldVO;
import com.spark.common.bean.form.vo.FormVO;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.dao.form.FormDao;
import com.spark.dao.form.FormFieldDao;
import com.spark.dao.form.FormVersionDao;
import com.spark.common.enums.DeleteFlagEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.FormTypeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.form.service.IFormService;
import com.spark.form.util.FormSchemaUtil;
import com.spark.manage.BaseService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.spark.common.utils.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.common.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024-07-01 09:11:13
 */
@Service
@LogPrint
public class FormServiceImpl extends BaseService<FormQuery, FormResult> implements IFormService {
    private final static Logger logger = LoggerFactory.getLogger(FormServiceImpl.class);
    @Autowired
    private FormDao formDao;
    @Autowired
    private FormVersionDao formVersionDao;
    @Value("${form.file.path}")
    private String formFilePath;
    @Autowired
    private FormFieldDao formFieldDao;

    /**
     * 创建表单
     * @param formVO 表单参数
     * @return 创建结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.FORM_INSERT)
    @Transactional(transactionManager = "primaryTransactionManager", rollbackFor = Exception.class)
    public ResultData<Form> createForm(FormVO formVO) {
        ResultData<Form> result = new ResultData<>();
        if (formVO == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long formId = super.genObjectId(ObjectTypeEnum.FORM);
        FormVersion formVersion = new FormVersion();
        formVersion.setFormId(formId);
        formVersion.setRevCode(1);
        formVersion.setRevNum("0.1");
        int count = formVersionDao.insertDB(formVersion);
        if (count < 1) {
            logger.error("createForm error, insert form version db fail");
            throw new BaseException(ErrorCodeEnum.INSERT_DATA_FAIL);
        }
        Form form = new Form();
        BeanUtil.copyProperties(formVO, form);
        // 非站点管理员强制归属当前租户
        if (!SessionHolder.isSysAdmin()) {
            form.setTenantId(SessionHolder.getCurrentTenantId());
        }
        form.setId(formId);
        form.setRevId(formVersion.getId());
        form.setRevNum(formVersion.getRevNum());
        if (form.getType() == null) {
            form.setType(FormTypeEnum.COMMON.getValue());
        }
        if (form.getOrderNum() == null) {
            form.setOrderNum(99);
        }
        count = formDao.insertDB(form);
        if (count < 1) {
            logger.error("createForm error, insert form db fail");
            throw new BaseException(ErrorCodeEnum.INSERT_DATA_FAIL);
        }
        result.setData(form);
        result.setObjId(formId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改表单
     * @param formVO 表单参数
     * @return 修改结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.FORM_UPDATE)
    public ResultData<Void> updateForm(FormVO formVO) {
        ResultData<Void> result = new ResultData<>();
        if (formVO == null || formVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FormQuery formQuery = new FormQuery();
        formQuery.setId(formVO.getId());
        super.scopeTenant(formQuery);
        FormResult formResult = formDao.queryForm(formQuery);
        if (formResult == null || !this.isActiveForm(formResult)) {
            result.setErrorCode(ErrorCodeEnum.FORM_NOT_EXIST);
            return result;
        }
        Form form = new Form();
        BeanUtil.copyProperties(formVO, form);
        int count = formDao.updateDBById(form);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setObjId(form.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除表单
     * @param formVO 表单参数
     * @return  删除结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.FORM_DELETE)
    public ResultData<Void> deleteForm(FormVO formVO) {
        ResultData<Void> result = new ResultData<>();
        if (formVO == null || formVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FormQuery formQuery = new FormQuery();
        formQuery.setId(formVO.getId());
        super.scopeTenant(formQuery);
        FormResult formResult = formDao.queryForm(formQuery);
        if (formResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        Form form = new Form();
        form.setId(formVO.getId());
        int count = formDao.deleteDBById(form);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        result.setObjId(form.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询表单
     * @param query 查询参数
     * @return 表单列表
     */
    @Override
    public ResultData<PageResult<FormResult>> pageFormList(FormQuery query) {
        ResultData<PageResult<FormResult>> result = new ResultData<>();
        if (query == null) {
            query = new FormQuery();
        }
        PageResult<FormResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询表单详情
     * @param query 查询参数
     * @return 表单详情
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.FORM_DETAIL)
    public ResultData<FormResult> queryFormDetail(FormQuery query) {
        ResultData<FormResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        super.scopeTenant(query);
        FormResult formResult = formDao.queryForm(query);
        if (formResult == null || !this.isActiveForm(formResult)) {
            result.setErrorCode(ErrorCodeEnum.FORM_NOT_EXIST);
            return result;
        }
        result.setData(formResult);
        result.setObjId(formResult.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询表单json
     * @param query 查询参数
     * @return 表单json
     */
    @Override
    public ResultData<String> queryFormJson(FormQuery query) {
        ResultData<String> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        super.scopeTenant(query);
        FormResult formResult = formDao.queryForm(query);
        if (formResult == null) {
            result.setErrorCode(ErrorCodeEnum.FORM_NOT_EXIST);
            return result;
        }
        Long revId = query.getRevId();
        if (revId == null) {
            revId = formResult.getRevId();
        }
        FormVersionQuery versionQuery = new FormVersionQuery();
        versionQuery.setId(revId);
        versionQuery.setFormId(formResult.getId());
        FormVersionResult formVersionResult = formVersionDao.queryFormVersion(versionQuery);
        if (formVersionResult == null) {
            result.setErrorCode(ErrorCodeEnum.FORM_VERSION_NOT_EXIST);
            return result;
        }
        String filePath = formVersionResult.getFilePath();
        if (StringUtil.isBlank(filePath))  {
            result.setCode(ResultData.OK);
            return result;
        }
        result = TextUtil.getFromText(filePath, true);
        return result;
    }

    /**
     * 保存表单json
     * @param formVO 表单参数
     * @return 保存结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.FORM_SAVE)
    public ResultData<Void> saveFormJson(FormVO formVO) {
        ResultData<Void> result = new ResultData<>();
        if (formVO == null || formVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        Long formId = formVO.getId();
        FormQuery formQuery = new FormQuery();
        formQuery.setId(formId);
        super.scopeTenant(formQuery);
        FormResult formResult = formDao.queryForm(formQuery);
        if (formResult == null || !this.isActiveForm(formResult)) {
            result.setErrorCode(ErrorCodeEnum.FORM_NOT_EXIST);
            return result;
        }
        Long expectedRevId = formResult.getRevId();
        ResultData<List<FormFieldVO>> schemaResult = FormSchemaUtil.analyzeFormFields(formVO.getFormJson());
        if (schemaResult.getCode() != ResultData.OK) {
            int errorCode = schemaResult.getCode();
            String errorMessage = schemaResult.getMessage();
            result.setCode(errorCode);
            result.setMessage(errorMessage);
            return result;
        }
        List<FormFieldVO> fieldList = schemaResult.getData();
        UUID uuid = UUID.randomUUID();
        String filePath = formFilePath + uuid;
        ResultData<Void> writeTextResult = TextUtil.writeToText(filePath, formVO.getFormJson());
        if (writeTextResult.getCode() != ResultData.OK) {
            return writeTextResult;
        }
        FormVersion formVersion = new FormVersion();
        formVersion.setFormId(formId);
        formVersion.setFilePath(filePath);
        int maxCode = formVersionDao.queryMaxFormVersionCode(formId);
        int revCode = maxCode + 1;
        String revNum = this.convertRevNum(revCode);
        formVersion.setRevCode(revCode);
        formVersion.setRevNum(revNum);
        Long revId = null;
        try {
            int count = formVersionDao.insertDB(formVersion);
            if (count < 1) {
                this.compensateSaveFormJson(formVersion, revId, filePath);
                result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
                return result;
            }
            revId = formVersion.getId();
            if (CollectionUtil.isNotEmpty(fieldList)) {
                count = formFieldDao.batchInsert(formId, revId, userId, fieldList);
                if (count < 1) {
                    this.compensateSaveFormJson(formVersion, revId, filePath);
                    result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
                    return result;
                }
            }
            count = formDao.updateFormRevIdCasDB(formId, expectedRevId, revId, revNum);
            if (count < 1) {
                this.compensateSaveFormJson(formVersion, revId, filePath);
                result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
                result.setMessage("表单版本已更新，请刷新后重试");
                return result;
            }
        } catch (RuntimeException e) {
            this.compensateSaveFormJson(formVersion, revId, filePath);
            throw e;
        }
        result.setObjId(formId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询表单列表
     * @param query 查询参数
     * @return 表单列表
     */
    @Override
    public ResultData<List<FormResult>> queryFormList(FormQuery query) {
        ResultData<List<FormResult>> result = new ResultData<>();
        if (query == null) {
            query = new FormQuery();
        }
        query.setPage(false);
        super.scopeTenant(query);
        List<FormResult> formList = formDao.queryFormList(query);
        this.supplyList(formList);
        result.setData(formList);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表数据
     */
    @Override
    protected void supplyList(List<FormResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
        for (FormResult formResult : list) {
            formResult.setTypeName(FormTypeEnum.indexOf(formResult.getType()).getDesc());
        }
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return  数量
     */
    @Override
    protected int queryCount(FormQuery query) {
        return formDao.queryFormCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<FormResult> queryList(FormQuery query) {
        return formDao.queryFormList(query);
    }

    /**
     * 查询最大id
     * @return 最大id
     */
    @Override
    protected Long queryMaxId() {
        return formDao.queryFormMaxId();
    }

    /**
     * 判断表单是否允许当前读写
     * @param formResult 表单详情
     * @return true表单存活
     */
    private boolean isActiveForm(FormResult formResult) {
        Integer deleteFlag = formResult.getDeleteFlag();
        Integer normalFlag = DeleteFlagEnum.NORMAL.getValue();
        return normalFlag.equals(deleteFlag);
    }

    /**
     * 将revCode转成revNum
     * @param revCode 表单版本号
     * @return revNum
     */
    private String convertRevNum(int revCode) {
        return String.format("%.1f", revCode / 10.0);
    }

    /**
     * 补偿未生效的表单版本和新文件
     * @param formVersion 新表单版本
     * @param revId 新版本id
     * @param filePath 新文件路径
     */
    private void compensateSaveFormJson(FormVersion formVersion, Long revId, String filePath) {
        if (revId != null) {
            try {
                formFieldDao.deleteDBByRevId(revId);
            } catch (RuntimeException e) {
                logger.error("saveFormJson field compensation error, revId={}", revId, e);
            }
        }
        Long versionId = formVersion.getId();
        if (versionId != null) {
            try {
                FormVersion deleteVersion = new FormVersion();
                deleteVersion.setId(versionId);
                int count = formVersionDao.deleteDBById(deleteVersion);
                if (count < 1) {
                    logger.warn("saveFormJson version compensation missing, revId={}", versionId);
                }
            } catch (RuntimeException e) {
                logger.error("saveFormJson version compensation error, revId={}", versionId, e);
            }
        }
        if (StringUtil.isBlank(filePath)) {
            return;
        }
        File file = new File(filePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                logger.error("saveFormJson file compensation error, filePath={}", filePath);
            }
        }
    }
}

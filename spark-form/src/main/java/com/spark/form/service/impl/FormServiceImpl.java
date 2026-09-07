package com.spark.form.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.dao.form.FormDao;
import com.spark.dao.form.FormFieldDao;
import com.spark.dao.form.FormVersionDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.FormTypeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.form.service.IFormService;
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

import java.util.ArrayList;
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
    @OperateLog(operateType = OperateTypeEnum.FORM_INSERT)
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
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        Form form = new Form();
        BeanUtil.copyProperties(formVO, form);
        form.setId(formId);
        form.setRevId(formVersion.getId());
        form.setRevNum(formVersion.getRevNum());
        if (form.getType() == null) {
            form.setType(FormTypeEnum.COMMON.getValue());
        }
        if (form.getOrderNum() == null) {
            form.setOrderNum(999);
        }
        count = formDao.insertDB(form);
        if (count < 1) {
            logger.error("createForm error, insert form db fail");
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
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
    @OperateLog(operateType = OperateTypeEnum.FORM_UPDATE)
    public ResultData<Void> updateForm(FormVO formVO) {
        ResultData<Void> result = new ResultData<>();
        if (formVO == null || formVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
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
    @OperateLog(operateType = OperateTypeEnum.FORM_DELETE)
    public ResultData<Void> deleteForm(FormVO formVO) {
        ResultData<Void> result = new ResultData<>();
        if (formVO == null || formVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
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
    @OperateLog(operateType = OperateTypeEnum.FORM_DETAIL)
    public ResultData<FormResult> queryFormDetail(FormQuery query) {
        ResultData<FormResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FormResult formResult = formDao.queryForm(query);
        if (formResult == null) {
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
        Long revId = query.getRevId();
        if (revId == null) {
            FormResult formResult = formDao.queryForm(query);
            if (formResult == null) {
                result.setErrorCode(ErrorCodeEnum.FORM_NOT_EXIST);
                return result;
            }
            revId = formResult.getRevId();
        }
        FormVersionQuery versionQuery = new FormVersionQuery();
        versionQuery.setId(revId);
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
    @OperateLog(operateType = OperateTypeEnum.FORM_SAVE)
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
        FormResult formResult = formDao.queryForm(formQuery);
        if (formResult == null) {
            result.setErrorCode(ErrorCodeEnum.FORM_NOT_EXIST);
            return result;
        }
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
        int revCode = maxCode+1;
        String revNum = this.convertRevNum(revCode);
        formVersion.setRevCode(revCode);
        formVersion.setRevNum(revNum);
        int count = formVersionDao.insertDB(formVersion);
        if (count < 1) {
            logger.error("saveFormJson error, insert form version db fail");
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        Long revId = formVersion.getId();
        List<FormFieldVO> fieldList = this.analyzeFormField(formVO.getFormJson());
        if (CollectionUtil.isNotEmpty(fieldList)) {
            count = formFieldDao.batchInsert(formId, revId, userId, fieldList);
            if (count < 1) {
                logger.error("saveFormJson error, insert from field db fail");
                return result;
            }
        }
        Form form = new Form();
        form.setId(formVO.getId());
        form.setRevId(revId);
        form.setRevNum(revNum);
        count = formDao.updateDBById(form);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
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
            query.setPage(false);
        }
        List<FormResult> formList = formDao.queryFormList(query);
        this.supplyList(formList);
        result.setData(formList);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 解析formField
     * @param formJson 表单json
     * @return 表单字段列表
     */
    private List<FormFieldVO> analyzeFormField(String formJson) {
        List<FormFieldVO> list = new ArrayList<>();
        if (StringUtil.isBlank(formJson)) {
            return list;
        }
        JSONObject jsonObject = JSONObject.parseObject(formJson);
        JSONArray widgetList = jsonObject.getJSONArray("widgetList");
        for (Object widget : widgetList) {
            JSONObject widgetJson = JSONObject.parseObject(widget.toString());
            String type = widgetJson.getString("type");
            JSONObject config = widgetJson.getJSONObject("config");
            String code = config.getString("code");
            String label = config.getString("label");
            FormFieldVO formFieldVO = new FormFieldVO();
            formFieldVO.setCode(code);
            formFieldVO.setLabel(label);
            formFieldVO.setType(type);
            list.add(formFieldVO);
        }
        return list;
    }

    /**
     * 将revCode转成revNum
     * @param revCode 表单版本号
     * @return revNum
     */
    private String convertRevNum(int revCode) {
        return String.format("%.1f", revCode/10.0);
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
}

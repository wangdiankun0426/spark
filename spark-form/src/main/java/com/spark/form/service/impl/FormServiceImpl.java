package com.spark.form.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.base.SessionHolder;
import com.spark.bean.form.entity.Form;
import com.spark.bean.form.entity.FormVersion;
import com.spark.bean.form.query.FormQuery;
import com.spark.bean.form.query.FormVersionQuery;
import com.spark.bean.form.result.FormResult;
import com.spark.bean.form.result.FormVersionResult;
import com.spark.bean.form.vo.FormFieldVO;
import com.spark.bean.form.vo.FormVO;
import com.spark.dao.form.FormDao;
import com.spark.dao.form.FormFieldDao;
import com.spark.dao.form.FormVersionDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.FormTypeEnum;
import com.spark.enums.ObjectTypeEnum;
import com.spark.form.service.IFormService;
import com.spark.manage.BaseService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import com.spark.utils.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
            return result;
        }
        Form form = new Form();
        BeanUtils.copyProperties(formVO, form);
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
            return result;
        }
        result.setData(form);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改表单
     * @param formVO 表单参数
     * @return 修改结果
     */
    @Override
    public ResultData<Void> updateForm(FormVO formVO) {
        ResultData<Void> result = new ResultData<>();
        if (formVO == null || formVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Form form = new Form();
        BeanUtils.copyProperties(formVO, form);
        int count = formDao.updateDBById(form);
        if (count < 1) {
            logger.error("updateForm error, update db fail");
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除表单
     * @param formVO 表单参数
     * @return  删除结果
     */
    @Override
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
            logger.error("deleteForm error, delete db fail");
            return result;
        }
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
            logger.error("saveFormJson error, update from db fail");
            return result;
        }
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

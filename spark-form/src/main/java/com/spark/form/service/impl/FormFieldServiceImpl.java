package com.spark.form.service.impl;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.form.query.FormFieldQuery;
import com.spark.common.bean.form.query.FormQuery;
import com.spark.common.bean.form.query.FormVersionQuery;
import com.spark.common.bean.form.result.FormFieldResult;
import com.spark.common.bean.form.result.FormResult;
import com.spark.common.bean.form.result.FormVersionResult;
import com.spark.dao.form.FormDao;
import com.spark.dao.form.FormFieldDao;
import com.spark.dao.form.FormVersionDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.form.service.IFormFieldService;
import com.spark.manage.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @since 2025/11/1 23:47
 */
@Service
public class FormFieldServiceImpl extends BaseService<FormFieldQuery, FormFieldResult> implements IFormFieldService {
    private final static Logger logger = LoggerFactory.getLogger(FormFieldServiceImpl.class);
    @Autowired
    private FormFieldDao formFieldDao;
    @Autowired
    private FormDao formDao;
    @Autowired
    private FormVersionDao formVersionDao;

    /**
     * 查询表单字段列表
     *
     * @param query 查询参数
     * @return 表单字段列表
     */
    @Override
    public ResultData<List<FormFieldResult>> queryFormFieldList(FormFieldQuery query) {
        ResultData<List<FormFieldResult>> result = new ResultData<>();
        if (query == null || query.getFormId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FormQuery formQuery = new FormQuery();
        Long formId = query.getFormId();
        formQuery.setId(formId);
        super.scopeTenant(formQuery);
        FormResult formResult = formDao.queryForm(formQuery);
        if (formResult == null) {
            result.setErrorCode(ErrorCodeEnum.FORM_NOT_EXIST);
            return result;
        }
        Long revId = query.getRevId();
        // 默认查询最新版本的字段名
        if (revId == null) {
            revId = formResult.getRevId();
            query.setRevId(revId);
        } else {
            FormVersionQuery versionQuery = new FormVersionQuery();
            versionQuery.setId(revId);
            versionQuery.setFormId(formId);
            FormVersionResult versionResult = formVersionDao.queryFormVersion(versionQuery);
            if (versionResult == null) {
                result.setErrorCode(ErrorCodeEnum.FORM_VERSION_NOT_EXIST);
                return result;
            }
        }
        List<FormFieldResult> list = formFieldDao.queryFormFieldList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }
}

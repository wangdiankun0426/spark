package com.spark.form.service.impl;

import com.spark.bean.base.ResultData;
import com.spark.bean.form.query.FormFieldQuery;
import com.spark.bean.form.query.FormQuery;
import com.spark.bean.form.result.FormFieldResult;
import com.spark.bean.form.result.FormResult;
import com.spark.dao.form.FormDao;
import com.spark.dao.form.FormFieldDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.form.service.IFormFieldService;
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
public class FormFieldServiceImpl implements IFormFieldService {
    private final static Logger logger = LoggerFactory.getLogger(FormFieldServiceImpl.class);
    @Autowired
    private FormFieldDao formFieldDao;
    @Autowired
    private FormDao formDao;

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
        formQuery.setId(query.getFormId());
        FormResult formResult = formDao.queryForm(formQuery);
        if (formResult == null) {
            result.setErrorCode(ErrorCodeEnum.FORM_NOT_EXIST);
            return result;
        }
        // 默认查询最新版本的字段名
        if (query.getRevId() == null) {
            query.setRevId(formResult.getRevId());
        }
        List<FormFieldResult> list = formFieldDao.queryFormFieldList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }
}

package com.spark.form.service.impl;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.form.entity.FormObj;
import com.spark.common.bean.form.entity.FormObjValue;
import com.spark.common.bean.form.query.FormObjQuery;
import com.spark.common.bean.form.query.FormObjValueQuery;
import com.spark.common.bean.form.query.FormQuery;
import com.spark.common.bean.form.result.FormObjResult;
import com.spark.common.bean.form.result.FormObjValueResult;
import com.spark.common.bean.form.vo.FormObjValueVO;
import com.spark.dao.form.FormObjDao;
import com.spark.dao.form.FormObjValueDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.form.service.IFormObjValueService;
import com.spark.form.service.IFormService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
public class FormObjValueServiceImpl implements IFormObjValueService {
    private final static Logger logger = LoggerFactory.getLogger(FormObjValueServiceImpl.class);
    @Autowired
    private FormObjValueDao formObjValueDao;
    @Autowired
    private FormObjDao formObjDao;
    @Autowired
    private IFormService formService;

    /**
     * 创建表单对象值
     * @param valueVO 表单对象值
     * @return 创建结果
     */
    @Override
    public ResultData<Void> saveFormObjValues(FormObjValueVO valueVO) {
        ResultData<Void> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        if (valueVO == null || valueVO.getObjId() == null || valueVO.getFormId() == null
                || CollectionUtil.isEmpty(valueVO.getValues())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FormObjQuery formObjQuery = new FormObjQuery();
        formObjQuery.setObjId(valueVO.getObjId());
        FormObjResult formObjResult = formObjDao.queryFormObj(formObjQuery);
        if (formObjResult != null) {
            result.setErrorCode(ErrorCodeEnum.FORM_OBJ_EXIST);
            return result;
        }
        FormObj formObj = new FormObj();
        formObj.setFormId(valueVO.getFormId());
        formObj.setObjId(valueVO.getObjId());
        int count = formObjDao.insertDB(formObj);
        if (count < 1) {
            logger.error("insert data error");
            return result;
        }
        List<FormObjValue> list = new ArrayList<>();
        for (FormObjValue value : valueVO.getValues()) {
            if (StringUtil.isBlank(value.getType()) || StringUtil.isBlank(value.getCode())
                    || StringUtil.isBlank(value.getValue()) || StringUtil.isBlank(value.getShowValue())) {
                continue;
            }
            value.setObjId(valueVO.getObjId());
            value.setFormId(valueVO.getFormId());
            value.setCreatedBy(userId);
            list.add(value);
        }
        if (CollectionUtil.isEmpty(list)) {
            result.setCode(ResultData.OK);
            return result;
        }
        count = formObjValueDao.batchInsert(list);
        if (count < 1) {
            logger.error("saveFormObjValues batch insert db fail!");
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
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
        Long objId = formObjResult.getObjId();
        Long formId = formObjResult.getFormId();
        FormQuery formQuery = new FormQuery();
        formQuery.setId(formId);
        ResultData<String> formResult = formService.queryFormJson(formQuery);
        formObjResult.setFormJson(formResult.getData());
        FormObjValueQuery objValueQuery = new FormObjValueQuery();
        objValueQuery.setObjId(objId);
        objValueQuery.setFormId(formId);
        List<FormObjValueResult> list = formObjValueDao.queryFormObjValueList(objValueQuery);
        formObjResult.setValues(list);
        result.setData(formObjResult);
        result.setCode(ResultData.OK);
        return result;
    }
}

package com.spark.form.service;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.form.query.FormFieldQuery;
import com.spark.common.bean.form.result.FormFieldResult;

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
public interface IFormFieldService {

    /**
     * 查询表单字段列表
     *
     * @param query 查询参数
     * @return 表单字段列表
     */
    ResultData<List<FormFieldResult>> queryFormFieldList(FormFieldQuery query);
}

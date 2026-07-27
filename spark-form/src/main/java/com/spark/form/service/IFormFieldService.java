package com.spark.form.service;

import com.spark.bean.base.ResultData;
import com.spark.bean.form.query.FormFieldQuery;
import com.spark.bean.form.result.FormFieldResult;

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

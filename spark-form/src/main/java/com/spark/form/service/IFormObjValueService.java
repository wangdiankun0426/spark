package com.spark.form.service;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.form.query.FormObjQuery;
import com.spark.common.bean.form.query.FormObjValueQuery;
import com.spark.common.bean.form.result.FormObjResult;
import com.spark.common.bean.form.result.FormObjValueResult;
import com.spark.common.bean.form.vo.FormObjValueVO;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/7/28 20:51
 */
public interface IFormObjValueService {

    /**
     * 创建表单对象值
     * @param valueVO 表单对象值
     * @return 创建结果
     */
    ResultData<Void> saveFormObjValues(FormObjValueVO valueVO);

    /**
     * 查询对象表单值列表
     * @param query 查询参数
     * @return 表单对象值列表
     */
    ResultData<List<FormObjValueResult>> queryFormObjValueList(FormObjValueQuery query);

    /**
     * 查询对象表单值
     * @param query
     * @return
     */
    ResultData<FormObjResult> queryFormObjDetail(FormObjQuery query);
}

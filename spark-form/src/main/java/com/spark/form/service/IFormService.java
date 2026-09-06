package com.spark.form.service;

import com.spark.common.bean.form.entity.Form;
import com.spark.common.bean.form.query.FormQuery;
import com.spark.common.bean.form.result.FormResult;
import com.spark.common.bean.form.vo.FormVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024-07-01 09:11:13
 */
public interface IFormService {

    /**
     * 创建表单
     * @param formVO 表单数据
     * @return 创建结果
     */
    ResultData<Form> createForm(FormVO formVO);

    /**
     * 修改表单
     * @param formVO 表单数据
     * @return 修改结果
     */
    ResultData<Void> updateForm(FormVO formVO);

    /**
     * 删除表单
     * @param formVO 表单数据
     * @return 删除结果
     */
    ResultData<Void> deleteForm(FormVO formVO);

    /**
     * 分页查询表单
     * @param query 查询参数
     * @return 表单列表
     */
    ResultData<PageResult<FormResult>> pageFormList(FormQuery query);

    /**
     * 查询表单详情
     * @param query 查询参数
     * @return 表单详情
     */
    ResultData<FormResult> queryFormDetail(FormQuery query);

    /**
     * 查询表单json
     * @param query 查询参数
     * @return 表单json
     */
    ResultData<String> queryFormJson(FormQuery query);

    /**
     * 保存表单json
     * @param formVO 表单数据
     * @return 保存结果
     */
    ResultData<Void> saveFormJson(FormVO formVO);

    /**
     * 查询表单列表
     * @param query 查询参数
     * @return 表单列表
     */
    ResultData<List<FormResult>> queryFormList(FormQuery query);
}

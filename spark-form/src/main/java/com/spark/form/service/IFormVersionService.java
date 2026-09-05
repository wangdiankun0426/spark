package com.spark.form.service;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.form.query.FormVersionQuery;
import com.spark.common.bean.form.result.FormVersionResult;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/7/3 17:32
 */
public interface IFormVersionService {

    /**
     * 分页查询
     * @param query 查询参数
     * @return 表单版本列表
     */
    ResultData<PageResult<FormVersionResult>> pageFormVersionList(FormVersionQuery query);
}

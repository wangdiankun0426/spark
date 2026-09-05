package com.spark.llm.service;

import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.common.bean.llm.vo.ModelVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-05-14 09:49:10
 */
public interface IModelService {

    /**
     * 创建模型
     * @param modelVO 模型数据
     * @return 创建结果
     */
    ResultData<Void> createModel(ModelVO modelVO);

    /**
     * 修改模型
     * @param modelVO 模型数据
     * @return 修改结果
     */
    ResultData<Void> updateModel(ModelVO modelVO);

    /**
     * 删除模型
     * @param modelVO 模型数据
     * @return  删除结果
     */
    ResultData<Void> deleteModel(ModelVO modelVO);

    /**
     * 分页查询模型
     * @param query 查询模型条件
     * @return 分页结果
     */
    ResultData<PageResult<ModelResult>> pageModelList(ModelQuery query);

    /**
     * 查询模型详情
     * @param query 查询模型条件
     * @return 详情
     */
    ResultData<ModelResult> queryModelDetail(ModelQuery query);

}

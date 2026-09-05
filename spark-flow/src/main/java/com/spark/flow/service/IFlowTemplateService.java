package com.spark.flow.service;

import com.spark.common.bean.flow.query.FlowTemplateQuery;
import com.spark.common.bean.flow.result.FlowTemplateResult;
import com.spark.common.bean.flow.vo.FlowTemplateVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025-10-29 20:57:44
 */
public interface IFlowTemplateService {
    /**
     * 创建流程模板
     * @param templateVO 流程模板创建参数
     * @return 创建结果
     */
    ResultData<Void> createTemplate(FlowTemplateVO templateVO);

    /**
     * 修改流程模板
     * @param templateVO 模板修改参数
     * @return 修改结果
     */
    ResultData<Void> updateTemplate(FlowTemplateVO templateVO);

    /**
     * 删除流程模板
     * @param templateVO 模板删除参数
     * @return 删除结果
     */
    ResultData<Void> deleteTemplate(FlowTemplateVO templateVO);

    /**
     * 分页查询流程模板
     * @param query 查询参数
     * @return 查询结果
     */
    ResultData<PageResult<FlowTemplateResult>> pageTemplateList(FlowTemplateQuery query);

    /**
     * 查询流程模板详情
     * @param query 查询参数
     * @return 查询结果
     */
    ResultData<FlowTemplateResult> queryTemplateDetail(FlowTemplateQuery query);

    /**
     * 显示流程模板详情
     * @param query 显示参数
     * @return 显示结果
     */
    ResultData<FlowTemplateResult> showTemplateDetail(FlowTemplateQuery query);
}

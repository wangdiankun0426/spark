package com.spark.flow.service;

import com.spark.common.bean.flow.query.FlowTemplateVersionQuery;
import com.spark.common.bean.flow.result.FlowTemplateVersionResult;
import com.spark.common.bean.flow.vo.FlowTemplateVersionVO;
import com.spark.common.bean.base.ResultData;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025-11-01 13:34:43
 */
public interface IFlowTemplateVersionService {
    /**
     * 创建流程模板版本
     * @param templateVersionVO 创建流程模板版本参数
     * @return 创建结果
     */
    ResultData<Void> createTemplateVersion(FlowTemplateVersionVO templateVersionVO);

    /**
     * 查询流程模板版本详情
     * @param query 查询参数
     * @return 查询结果
     */
    ResultData<FlowTemplateVersionResult> queryTemplateVersionDetail(FlowTemplateVersionQuery query);

}

package com.spark.workflow.service;

import com.spark.bean.base.ResultData;
import com.spark.bean.workflow.query.WfTemplateVersionQuery;
import com.spark.bean.workflow.result.WfTemplateVersionResult;
import com.spark.bean.workflow.vo.WfTemplateVersionVO;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 * AI工作流模板版本Service接口
 */
public interface IWorkflowVersionService {

    /**
     * 保存版本
     *
     * @param versionVO 版本参数
     * @return 保存结果
     */
    ResultData<Void> saveVersion(WfTemplateVersionVO versionVO);

    /**
     * 查询详情
     * @param query
     * @return
     */
    ResultData<WfTemplateVersionResult> queryVersionDetail(WfTemplateVersionQuery query);

}

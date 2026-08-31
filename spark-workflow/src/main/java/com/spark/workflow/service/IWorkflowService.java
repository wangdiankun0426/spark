package com.spark.workflow.service;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.workflow.query.WfTemplateQuery;
import com.spark.bean.workflow.result.WfTemplateResult;
import com.spark.bean.workflow.vo.WfTemplateVO;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 * workFlow模板Service接口
 */
public interface IWorkflowService {

    /**
     * 创建工作流
     * @param templateVO
     * @return
     */
    ResultData<Void> createWorkflow(WfTemplateVO templateVO);

    /**
     * 修改工作流
     * @param templateVO
     * @return
     */
    ResultData<Void> updateWorkflow(WfTemplateVO templateVO);

    /**
     * 删除工作流
     * @param templateVO
     * @return
     */
    ResultData<Void> deleteWorkflow(WfTemplateVO templateVO);

    /**
     * 分页查询工作流
     * @param query
     * @return
     */
    ResultData<PageResult<WfTemplateResult>> pageWorkflowList(WfTemplateQuery query);

    /**
     * 查询工作流详情
     * @param query
     * @return
     */
    ResultData<WfTemplateResult> queryWorkflowDetail(WfTemplateQuery query);

}

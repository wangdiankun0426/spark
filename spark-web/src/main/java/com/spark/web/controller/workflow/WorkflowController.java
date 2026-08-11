package com.spark.web.controller.workflow;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.workflow.query.WfTemplateQuery;
import com.spark.bean.workflow.result.WfTemplateResult;
import com.spark.bean.workflow.vo.WfTemplateVO;
import com.spark.workflow.service.IWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 * AI工作流管理Controller
 */
@RestController
@RequestMapping("workflow")
public class WorkflowController {

    @Autowired
    private IWorkflowService workflowService;

    /**
     * 创建AI工作流
     */
    @PostMapping("create")
    public ResultData<Void> createWorkflow(WfTemplateVO templateVO) {
        return workflowService.createWorkflow(templateVO);
    }

    /**
     * 修改AI工作流基本信息
     */
    @PostMapping("update")
    public ResultData<Void> updateWorkflow(WfTemplateVO templateVO) {
        return workflowService.updateWorkflow(templateVO);
    }

    /**
     * 删除AI工作流
     */
    @PostMapping("delete")
    public ResultData<Void> deleteWorkflow(WfTemplateVO templateVO) {
        return workflowService.deleteWorkflow(templateVO);
    }

    /**
     * 分页查询AI工作流列表
     */
    @GetMapping("pageList")
    public ResultData<PageResult<WfTemplateResult>> pageWorkflowList(WfTemplateQuery query) {
        return workflowService.pageWorkflowList(query);
    }

    /**
     * 查询AI工作流详情
     */
    @GetMapping("detail")
    public ResultData<WfTemplateResult> queryWorkflowDetail(WfTemplateQuery query) {
        return workflowService.queryWorkflowDetail(query);
    }

}

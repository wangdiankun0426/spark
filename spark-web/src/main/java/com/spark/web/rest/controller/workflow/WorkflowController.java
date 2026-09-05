package com.spark.web.rest.controller.workflow;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.workflow.query.WfTemplateQuery;
import com.spark.common.bean.workflow.result.WfTemplateResult;
import com.spark.common.bean.workflow.vo.WfTemplateVO;
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
 * workFlow管理Controller
 */
@RestController
@RequestMapping("workflow")
public class WorkflowController {

    @Autowired
    private IWorkflowService workflowService;

    /**
     * 创建workFlow
     */
    @PostMapping("create")
    public ResultData<Void> createWorkflow(WfTemplateVO templateVO) {
        return workflowService.createWorkflow(templateVO);
    }

    /**
     * 修改workFlow基本信息
     */
    @PostMapping("update")
    public ResultData<Void> updateWorkflow(WfTemplateVO templateVO) {
        return workflowService.updateWorkflow(templateVO);
    }

    /**
     * 删除workFlow
     */
    @PostMapping("delete")
    public ResultData<Void> deleteWorkflow(WfTemplateVO templateVO) {
        return workflowService.deleteWorkflow(templateVO);
    }

    /**
     * 分页查询workFlow列表
     */
    @GetMapping("pageList")
    public ResultData<PageResult<WfTemplateResult>> pageWorkflowList(WfTemplateQuery query) {
        return workflowService.pageWorkflowList(query);
    }

    /**
     * 查询workFlow详情
     */
    @GetMapping("detail")
    public ResultData<WfTemplateResult> queryWorkflowDetail(WfTemplateQuery query) {
        return workflowService.queryWorkflowDetail(query);
    }

}

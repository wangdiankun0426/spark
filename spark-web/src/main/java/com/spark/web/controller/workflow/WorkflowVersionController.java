package com.spark.web.controller.workflow;

import com.spark.bean.base.ResultData;
import com.spark.bean.workflow.query.WfTemplateVersionQuery;
import com.spark.bean.workflow.result.WfTemplateVersionResult;
import com.spark.bean.workflow.vo.WfTemplateVersionVO;
import com.spark.workflow.service.IWorkflowVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
 * workFlow版本管理Controller
 */
@RestController
@RequestMapping("workflow/version")
public class WorkflowVersionController {

    @Autowired
    private IWorkflowVersionService workflowVersionService;

    /**
     * 保存版本
     */
    @PostMapping("saveVersion")
    public ResultData<Void> saveVersion(@RequestBody WfTemplateVersionVO versionVO) {
        return workflowVersionService.saveVersion(versionVO);
    }

    /**
     * 查询版本详情
     */
    @GetMapping("detail")
    public ResultData<WfTemplateVersionResult> queryVersionDetail(WfTemplateVersionQuery query) {
        return workflowVersionService.queryVersionDetail(query);
    }
}

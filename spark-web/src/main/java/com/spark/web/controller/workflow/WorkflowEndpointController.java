package com.spark.web.controller.workflow;

import com.spark.bean.base.ResultData;
import com.spark.bean.workflow.query.WfTemplateEndpointQuery;
import com.spark.bean.workflow.result.WfTemplateEndpointResult;
import com.spark.bean.workflow.vo.WfTemplateEndpointVO;
import com.spark.workflow.service.IWorkflowEndpointService;
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
 * @since 2026-08-11 14:00:00
 */
@RestController
@RequestMapping("workflow/endpoint")
public class WorkflowEndpointController {

    @Autowired
    private IWorkflowEndpointService endpointService;

    /**
     * 创建端点
     * @param nodeVO
     * @return
     */
    @PostMapping("create")
    public ResultData<Void> createEndpoint(WfTemplateEndpointVO nodeVO) {
        return endpointService.createEndpoint(nodeVO);
    }

    /**
     * 修改端点
     * @param nodeVO
     * @return
     */
    @PostMapping("update")
    public ResultData<Void> updateEndpoint(WfTemplateEndpointVO nodeVO) {
        return endpointService.updateEndpoint(nodeVO);
    }

    /**
     * 删除端点
     * @param nodeVO
     * @return
     */
    @PostMapping("delete")
    public ResultData<Void> deleteEndpoint(WfTemplateEndpointVO nodeVO) {
        return endpointService.deleteEndpoint(nodeVO);
    }

    /**
     * 端点详情
     * @param query
     * @return
     */
    @GetMapping("detail")
    public ResultData<WfTemplateEndpointResult> queryEndpoint(WfTemplateEndpointQuery query) {
        return endpointService.queryEndpoint(query);
    }

    /**
     * 端点列表
     * @param query
     * @return
     */
    @GetMapping("list")
    public ResultData<java.util.List<WfTemplateEndpointResult>> queryEndpointList(WfTemplateEndpointQuery query) {
        return endpointService.queryEndpointList(query);
    }
}
